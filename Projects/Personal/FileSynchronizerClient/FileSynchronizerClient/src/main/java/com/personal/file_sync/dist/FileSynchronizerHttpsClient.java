package com.personal.file_sync.dist;

import java.io.BufferedOutputStream;
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
import com.utils.io.zip.ZipFileCreator;
import com.utils.io.zip.ZipFileExtractor;
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

public class FileSynchronizerHttpsClient {

	private final FileSynchronizerClientSettings fileSynchronizerClientSettings;

	public FileSynchronizerHttpsClient(
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

		final String fileSynchronizerFolderPathString =
				PathUtils.computePath(SystemUtils.USER_HOME, "FileSynchronizer");
		Logger.printProgress("cleaning folder:");
		Logger.printLine(fileSynchronizerFolderPathString);
		FactoryFolderDeleter.getInstance().deleteFolder(fileSynchronizerFolderPathString, false, true);
	}

	public void executeDownloadRequest() {

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
			final boolean useFileCache = fileSynchronizerClientSettings.isUseFileCache();
			Logger.printLine("use file cache: " + useFileCache);
			final boolean useSandbox = fileSynchronizerClientSettings.isUseSandbox();
			Logger.printLine("use sandbox: " + useSandbox);

			final OkHttpClient okHttpClient = createOkHttpClient();

			final Request.Builder requestBuilder = new Request.Builder();
			final String url = "https://" + ipAddr + ":" + port + "/download";
			requestBuilder.url(url);
			requestBuilder.header("useFileCache", String.valueOf(useFileCache));
			requestBuilder.header("useSandbox", String.valueOf(useSandbox));
			requestBuilder.header("filePathString", filePathString);
			final Request request = requestBuilder.build();

			final Call call = okHttpClient.newCall(request);
			try (Response response = call.execute()) {

				final String preparedRequestedFileString =
						response.header("preparedRequestedFile");
				final boolean preparedRequestedFile =
						Boolean.parseBoolean(preparedRequestedFileString);

				final String folderString = response.header("folder");
				final boolean folder = Boolean.parseBoolean(folderString);

				if (!preparedRequestedFile) {
					Logger.printWarning("did not find the requested file on the server");

				} else {
					final ResponseBody responseBody = response.body();

					Logger.printProgress("retrieving response body");

					final String contentLengthString = response.header("Content-length");
					final long contentLength = StrUtils.tryParsePositiveLong(contentLengthString);

					final String tmpFilePathString = fileSynchronizerClientSettings.getTmpFilePathString();
					tmpZipFilePathString = PathUtils.computePath(tmpFilePathString, System.nanoTime() + ".zip");

					FactoryFolderCreator.getInstance().createParentDirectories(tmpZipFilePathString, false, true);

					final InputStream inputStream = new ProgressInputStream(responseBody.byteStream(),
							contentLength, new ProgressListenerConsole());
					try (OutputStream outputStream = new BufferedOutputStream(
							StreamUtils.openOutputStream(tmpZipFilePathString))) {

						final byte[] buffer = new byte[FileSynchronizerUtils.BUFFER_SIZE];
						IOUtils.copyLarge(inputStream, outputStream, buffer);
					}

					final boolean deleteExisting;
					if (folder) {
						deleteExisting = true;
					} else {
						filePathString = PathUtils.computeParentPath(filePathString);
						deleteExisting = false;
					}

					final ZipFileExtractor zipFileExtractor = new ZipFileExtractor(
							tmpZipFilePathString, filePathString, useFileCache, deleteExisting, 12, false, false);
					zipFileExtractor.work();

					final boolean extractZipSuccess = zipFileExtractor.isSuccess();
					if (extractZipSuccess) {
						Logger.printStatus("Download request completed successfully for file path:" +
								System.lineSeparator() + filePathString);
					}
				}
			}

		} catch (final Exception exc) {
			Logger.printError("failed to execute download request");
			Logger.printException(exc);

		} finally {
			if (IoUtils.fileExists(tmpZipFilePathString)) {
				FactoryFileDeleter.getInstance().deleteFile(tmpZipFilePathString, false, true);
			}
		}
	}

	public void executeUploadRequest() {

		String tmpZipFilePathString = null;
		try {
			final OkHttpClient okHttpClient = createOkHttpClient();

			Logger.printLine("File path string:");
			final String filePathString = fileSynchronizerClientSettings.getFilePathString();
			Logger.printLine(filePathString);
			final String ipAddr = fileSynchronizerClientSettings.getIpAddr();
			Logger.printLine("IP address: " + ipAddr);
			final int port = fileSynchronizerClientSettings.getPort();
			Logger.printLine("port: " + port);
			final boolean useFileCache = fileSynchronizerClientSettings.isUseFileCache();
			Logger.printLine("use file cache: " + useFileCache);
			final boolean useSandbox = fileSynchronizerClientSettings.isUseSandbox();
			Logger.printLine("use sandbox: " + useSandbox);

			final String tmpFilePathString = fileSynchronizerClientSettings.getTmpFilePathString();
			tmpZipFilePathString = PathUtils.computePath(tmpFilePathString, System.nanoTime() + ".zip");

			final ZipFileCreator zipFileCreator = new ZipFileCreator(
					filePathString, tmpZipFilePathString, useFileCache, true, 12, false, false);
			zipFileCreator.work();

			final boolean createZipSuccess = zipFileCreator.isSuccess();
			if (createZipSuccess) {

				final Request.Builder requestBuilder = new Request.Builder();
				final String url = "https://" + ipAddr + ":" + port + "/upload";
				requestBuilder.url(url);
				requestBuilder.header("useFileCache", String.valueOf(useFileCache));
				requestBuilder.header("useSandbox", String.valueOf(useSandbox));
				requestBuilder.header("filePathString", filePathString);
				final boolean folder = zipFileCreator.isFolder();
				requestBuilder.header("folder", String.valueOf(folder));

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

		} catch (final Exception exc) {
			Logger.printError("failed to execute upload request");
			Logger.printException(exc);

		} finally {
			if (IoUtils.fileExists(tmpZipFilePathString)) {
				FactoryFileDeleter.getInstance().deleteFile(tmpZipFilePathString, false, true);
			}
		}
	}

	private static OkHttpClient createOkHttpClient() {

		final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

		okHttpClientBuilder.connectTimeout(50, TimeUnit.HOURS);
		okHttpClientBuilder.writeTimeout(50, TimeUnit.HOURS);
		okHttpClientBuilder.readTimeout(50, TimeUnit.HOURS);

		final OkHttpBuilderProxyConfigurator okHttpBuilderProxyConfigurator =
				FactoryOkHttpBuilderProxyConfigurator.newInstance();
		okHttpBuilderProxyConfigurator.configureProxy(okHttpClientBuilder);

		final CustomSslContext customSslContext = FileSynchronizerUtils.createSslContext(
				"ssl/client.keystore", "ssl/client.truststore");
		final SSLContext sslContext = customSslContext.getSslContext();
		final SSLSocketFactory socketFactory = sslContext.getSocketFactory();
		final X509TrustManager x509TrustManager = customSslContext.getX509TrustManager();
		okHttpClientBuilder.sslSocketFactory(socketFactory, x509TrustManager);
		okHttpClientBuilder.hostnameVerifier((
				hostname,
				session) -> true);

		return okHttpClientBuilder.build();
	}

	@Override
	public String toString() {
		return StrUtils.reflectionToString(this);
	}
}
