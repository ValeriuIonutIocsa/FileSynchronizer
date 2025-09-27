package com.personal.file_sync.dist;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import com.personal.file_sync.dist.progress.ProgressRequestBody;
import com.personal.file_sync.settings.FileSynchronizerClientSettings;
import com.personal.file_sync.settings.modes.Mode;
import com.utils.io.IoUtils;
import com.utils.io.PathUtils;
import com.utils.io.StreamUtils;
import com.utils.io.file_deleters.FactoryFileDeleter;
import com.utils.io.folder_creators.FactoryFolderCreator;
import com.utils.io.folder_deleters.FactoryFolderDeleter;
import com.utils.io.progress.ProgressInputStream;
import com.utils.io.progress.listeners.ProgressListenerConsole;
import com.utils.io.zip.ZipFileCreator7z;
import com.utils.io.zip.ZipFileExtractor7z;
import com.utils.log.Logger;
import com.utils.net.proxy.ok_http.FactoryOkHttpBuilderProxyConfigurator;
import com.utils.net.proxy.ok_http.OkHttpBuilderProxyConfigurator;
import com.utils.net.ssl.CustomSslContext;
import com.utils.string.StrUtils;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FileSynchronizerHttpClient {

	private final FileSynchronizerClientSettings fileSynchronizerClientSettings;

	public FileSynchronizerHttpClient(
			final FileSynchronizerClientSettings fileSynchronizerClientSettings) {

		this.fileSynchronizerClientSettings = fileSynchronizerClientSettings;
	}

	public void work() {

		final Mode mode = fileSynchronizerClientSettings.getMode();
		if (mode == Mode.CLEAN) {
			clean();
		} else if (mode == Mode.DOWNLOAD) {
			executeDownloadRequest();
		} else if (mode == Mode.UPLOAD) {
			executeUploadRequest();
		}
	}

	private static void clean() {

		final String fileSynchronizerTmpFolderPathString =
				PathUtils.computePath(SystemUtils.USER_HOME, "FileSynchronizer", "tmp");

		Logger.printProgress("cleaning folder:");
		Logger.printLine(fileSynchronizerTmpFolderPathString);

		FactoryFolderDeleter.getInstance()
				.deleteFolder(fileSynchronizerTmpFolderPathString, false, true);
	}

	public void executeDownloadRequest() {

		final boolean useSandbox = fileSynchronizerClientSettings.isUseSandbox();

		String tmpZipFilePathString = null;
		try {
			Logger.printProgress("executing download request");

			Logger.printLine("File path string:");
			String filePathString = fileSynchronizerClientSettings.getFilePathString();
			Logger.printLine(filePathString);
			final String ipAddr = fileSynchronizerClientSettings.getIpAddr();
			Logger.printLine("IP address: " + ipAddr);
			final int port = fileSynchronizerClientSettings.getPort();
			Logger.printLine("port: " + port);
			final boolean ssl = fileSynchronizerClientSettings.isSsl();
			Logger.printLine("ssl: " + ssl);
			Logger.printLine("use sandbox: " + useSandbox);
			final String sevenZipExecutablePathString =
					fileSynchronizerClientSettings.getSevenZipExecutablePathString();
			Logger.printLine("sevenZipExecutablePathString: " + sevenZipExecutablePathString);
			final int sevenZipThreadCount = fileSynchronizerClientSettings.getSevenZipThreadCount();
			Logger.printLine("sevenZipThreadCount: " + sevenZipThreadCount);

			final OkHttpClient okHttpClient = createOkHttpClient(ssl);

			final Request.Builder requestBuilder = new Request.Builder();
			final String url;
			if (ssl) {
				url = "https://" + ipAddr + ":" + port + "/download";
			} else {
				url = "http://" + ipAddr + ":" + port + "/download";
			}
			requestBuilder.url(url);
			requestBuilder.header("useSandbox", String.valueOf(useSandbox));
			final String encodedFilePathString =
					FileSynchronizerUtils.encodeFilePathString(filePathString);
			requestBuilder.header("filePathString", encodedFilePathString);
			final Request request = requestBuilder.build();

			final Call call = okHttpClient.newCall(request);
			try (Response response = call.execute()) {

				final String preparedRequestedFileString =
						response.header("preparedRequestedFile");
				final boolean preparedRequestedFile =
						Boolean.parseBoolean(preparedRequestedFileString);

				if (!preparedRequestedFile) {
					Logger.printWarning("did not find the requested file on the server");

				} else {
					Logger.printProgress("retrieving response body");

					final String contentLengthString = response.header("Content-length");
					final long contentLength = StrUtils.tryParsePositiveLong(contentLengthString);

					final String fileName = PathUtils.computeFileName(filePathString);

					final String tmpFilePathString = fileSynchronizerClientSettings.getTmpFilePathString();
					tmpZipFilePathString = PathUtils.computePath(tmpFilePathString,
							String.valueOf(System.nanoTime()), fileName + ".zip");

					final boolean createParentFolderSuccess = FactoryFolderCreator.getInstance()
							.createParentDirectories(tmpZipFilePathString, false, true);
					if (createParentFolderSuccess) {

						final ResponseBody responseBody = response.body();
						try (InputStream inputStream = new ProgressInputStream(responseBody.byteStream(),
								contentLength, new ProgressListenerConsole());
								OutputStream outputStream = StreamUtils.openOutputStream(tmpZipFilePathString)) {

							final byte[] buffer = new byte[FileSynchronizerUtils.BUFFER_SIZE];
							IOUtils.copyLarge(inputStream, outputStream, buffer);
						}

						filePathString = PathUtils.computeParentPath(filePathString);

						final ZipFileExtractor7z zipFileExtractor7z =
								new ZipFileExtractor7z(sevenZipExecutablePathString, sevenZipThreadCount,
										tmpZipFilePathString, filePathString, true);
						zipFileExtractor7z.work();

						final boolean extractZipSuccess = zipFileExtractor7z.isSuccess();
						if (extractZipSuccess) {
							Logger.printStatus("Download request completed successfully for file path:" +
									System.lineSeparator() + filePathString);
						}
					}
				}
			}

		} catch (final Throwable throwable) {
			Logger.printError("failed to execute download request");
			Logger.printThrowable(throwable);

		} finally {
			if (!useSandbox && IoUtils.fileExists(tmpZipFilePathString)) {
				FactoryFileDeleter.getInstance().deleteFile(tmpZipFilePathString, false, true);
			}
		}
	}

	public void executeUploadRequest() {

		final boolean useSandbox = fileSynchronizerClientSettings.isUseSandbox();

		String tmpZipFilePathString = null;
		try {
			Logger.printProgress("executing upload request");

			Logger.printLine("File path string:");
			final String filePathString = fileSynchronizerClientSettings.getFilePathString();
			Logger.printLine(filePathString);
			final String ipAddr = fileSynchronizerClientSettings.getIpAddr();
			Logger.printLine("IP address: " + ipAddr);
			final int port = fileSynchronizerClientSettings.getPort();
			Logger.printLine("port: " + port);
			final boolean ssl = fileSynchronizerClientSettings.isSsl();
			Logger.printLine("ssl: " + ssl);
			Logger.printLine("use sandbox: " + useSandbox);
			final String sevenZipExecutablePathString =
					fileSynchronizerClientSettings.getSevenZipExecutablePathString();
			Logger.printLine("sevenZipExecutablePathString: " + sevenZipExecutablePathString);
			final int sevenZipThreadCount = fileSynchronizerClientSettings.getSevenZipThreadCount();
			Logger.printLine("sevenZipThreadCount: " + sevenZipThreadCount);

			final String fileName = PathUtils.computeFileName(filePathString);

			final String tmpFilePathString = fileSynchronizerClientSettings.getTmpFilePathString();
			tmpZipFilePathString = PathUtils.computePath(tmpFilePathString,
					String.valueOf(System.nanoTime()), fileName + ".zip");

			final ZipFileCreator7z zipFileCreator7z =
					new ZipFileCreator7z(sevenZipExecutablePathString, sevenZipThreadCount,
							filePathString, tmpZipFilePathString, false);
			zipFileCreator7z.work();

			final boolean createZipSuccess = zipFileCreator7z.isSuccess();
			if (createZipSuccess) {

				final OkHttpClient okHttpClient = createOkHttpClient(ssl);

				final Request.Builder requestBuilder = new Request.Builder();
				final String url;
				if (ssl) {
					url = "https://" + ipAddr + ":" + port + "/upload";
				} else {
					url = "http://" + ipAddr + ":" + port + "/upload";
				}
				requestBuilder.url(url);
				requestBuilder.header("useSandbox", String.valueOf(useSandbox));
				final String encodedFilePathString =
						FileSynchronizerUtils.encodeFilePathString(filePathString);
				requestBuilder.header("filePathString", encodedFilePathString);

				final RequestBody requestBody =
						RequestBody.create(new File(tmpZipFilePathString), MediaType.parse("text/plain"));
				final ProgressRequestBody progressRequestBody =
						new ProgressRequestBody(requestBody, new ProgressListenerConsole());
				requestBuilder.post(progressRequestBody);

				final Request request = requestBuilder.build();

				final Call call = okHttpClient.newCall(request);
				try (Response response = call.execute()) {

					final String uploadCompletedSuccessfullyString =
							response.header("uploadCompletedSuccessfully");
					final boolean uploadCompletedSuccessfully =
							Boolean.parseBoolean(uploadCompletedSuccessfullyString);
					if (uploadCompletedSuccessfully) {
						Logger.printStatus("Upload request completed successfully for file path:" +
								System.lineSeparator() + filePathString);
					}
				}
			}

		} catch (final Throwable throwable) {
			Logger.printError("failed to execute upload request");
			Logger.printThrowable(throwable);

		} finally {
			if (!useSandbox && IoUtils.fileExists(tmpZipFilePathString)) {
				FactoryFileDeleter.getInstance().deleteFile(tmpZipFilePathString, true, true);
			}
		}
	}

	private static OkHttpClient createOkHttpClient(
			final boolean ssl) {

		final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

		okHttpClientBuilder.connectTimeout(50, TimeUnit.HOURS);
		okHttpClientBuilder.writeTimeout(50, TimeUnit.HOURS);
		okHttpClientBuilder.readTimeout(50, TimeUnit.HOURS);

		final OkHttpBuilderProxyConfigurator okHttpBuilderProxyConfigurator =
				FactoryOkHttpBuilderProxyConfigurator.newInstance();
		okHttpBuilderProxyConfigurator.configureProxy(okHttpClientBuilder);

		if (ssl) {

			final CustomSslContext customSslContext = FileSynchronizerUtils.createSslContext(
					"ssl/client.keystore", "ssl/client.truststore");
			final SSLContext sslContext = customSslContext.getSslContext();
			final SSLSocketFactory socketFactory = sslContext.getSocketFactory();
			final X509TrustManager x509TrustManager = customSslContext.getX509TrustManager();
			okHttpClientBuilder.sslSocketFactory(socketFactory, x509TrustManager);
			okHttpClientBuilder.hostnameVerifier((
					hostname,
					session) -> true);
		}

		return okHttpClientBuilder.build();
	}

	@Override
	public String toString() {
		return StrUtils.reflectionToString(this);
	}
}
