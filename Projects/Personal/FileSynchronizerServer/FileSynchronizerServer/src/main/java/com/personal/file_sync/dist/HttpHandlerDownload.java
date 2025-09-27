package com.personal.file_sync.dist;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Strings;

import com.personal.file_sync.settings.FileSynchronizerServerSettings;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.utils.io.IoUtils;
import com.utils.io.PathUtils;
import com.utils.io.StreamUtils;
import com.utils.io.file_deleters.FactoryFileDeleter;
import com.utils.io.progress.ProgressInputStream;
import com.utils.io.progress.listeners.ProgressListenerConsole;
import com.utils.io.zip.ZipFileCreator7z;
import com.utils.log.Logger;
import com.utils.string.StrUtils;

class HttpHandlerDownload implements HttpHandler {

	private final FileSynchronizerServerSettings fileSynchronizerServerSettings;

	HttpHandlerDownload(
			final FileSynchronizerServerSettings fileSynchronizerServerSettings) {

		this.fileSynchronizerServerSettings = fileSynchronizerServerSettings;
	}

	@Override
	public void handle(
			final HttpExchange httpExchange) {

		boolean useSandbox = false;

		String tmpZipFilePathString = null;
		try (httpExchange) {

			Logger.printNewLine();
			final Instant start = Instant.now();
			Logger.printStatus("Received download request at " + StrUtils.createDisplayDateTimeString(start));

			boolean preparedRequestedFile = false;
			String filePathString = null;
			try {
				final Headers requestHeaders = httpExchange.getRequestHeaders();

				final String encodedFilePathString = requestHeaders.getFirst("filePathString");
				filePathString = FileSynchronizerUtils.decodeFilePathString(encodedFilePathString);

				final String useSandboxString = requestHeaders.getFirst("useSandbox");
				useSandbox = Boolean.parseBoolean(useSandboxString);
				Logger.printLine("use sandbox: " + useSandbox);

				if (useSandbox) {

					final String sandboxFilePathString =
							fileSynchronizerServerSettings.getSandboxFilePathString();
					filePathString = PathUtils.computePath(sandboxFilePathString,
							Strings.CS.replace(filePathString, ":", ""));
				}

				Logger.printLine("File path:");
				Logger.printLine(filePathString);

				if (!IoUtils.fileExists(filePathString)) {
					Logger.printWarning("the requested file does not exist:" +
							System.lineSeparator() + filePathString);

				} else {
					final String fileName = PathUtils.computeFileName(filePathString);

					final String tmpFilePathString =
							fileSynchronizerServerSettings.getTmpFilePathString();
					tmpZipFilePathString = PathUtils.computePath(tmpFilePathString,
							String.valueOf(System.nanoTime()), fileName + ".zip");

					final String sevenZipExecutablePathString =
							fileSynchronizerServerSettings.getSevenZipExecutablePathString();
					final int sevenZipThreadCount =
							fileSynchronizerServerSettings.getSevenZipThreadCount();
					final ZipFileCreator7z zipFileCreator7z =
							new ZipFileCreator7z(sevenZipExecutablePathString, sevenZipThreadCount,
									filePathString, tmpZipFilePathString, false);
					zipFileCreator7z.work();

					preparedRequestedFile = zipFileCreator7z.isSuccess();
				}

			} catch (final Throwable throwable) {
				Logger.printError("failed to prepare requested file");
				Logger.printThrowable(throwable);
			}

			Logger.printLine("preparedRequestedFile: " + preparedRequestedFile);

			final Headers responseHeaders = httpExchange.getResponseHeaders();
			responseHeaders.set("preparedRequestedFile", String.valueOf(preparedRequestedFile));

			if (!preparedRequestedFile) {
				httpExchange.sendResponseHeaders(200, -1);

			} else {
				final long zipFileLength = new File(tmpZipFilePathString).length();
				httpExchange.sendResponseHeaders(200, zipFileLength);

				try (InputStream inputStream = new ProgressInputStream(
						StreamUtils.openInputStream(tmpZipFilePathString),
						zipFileLength, new ProgressListenerConsole());
						OutputStream outputStream = httpExchange.getResponseBody()) {

					final byte[] buffer = new byte[FileSynchronizerUtils.BUFFER_SIZE];
					IOUtils.copyLarge(inputStream, outputStream, buffer);
				}
			}

			Logger.printStatus("Download completed successfully for file path:" +
					System.lineSeparator() + filePathString);

			Logger.printStatus("Response sent successfully after " + StrUtils.durationToString(start));

		} catch (final Throwable throwable) {
			Logger.printError("failed to handle download request");
			Logger.printThrowable(throwable);

		} finally {
			if (!useSandbox && IoUtils.fileExists(tmpZipFilePathString)) {
				FactoryFileDeleter.getInstance().deleteFile(tmpZipFilePathString, true, true);
			}
		}
	}
}
