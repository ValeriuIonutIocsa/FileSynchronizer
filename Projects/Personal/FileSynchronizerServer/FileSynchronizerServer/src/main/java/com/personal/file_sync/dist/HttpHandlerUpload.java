package com.personal.file_sync.dist;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.personal.file_sync.settings.FileSynchronizerServerSettings;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.utils.io.IoUtils;
import com.utils.io.PathUtils;
import com.utils.io.StreamUtils;
import com.utils.io.file_deleters.FactoryFileDeleter;
import com.utils.io.folder_creators.FactoryFolderCreator;
import com.utils.io.progress.ProgressInputStream;
import com.utils.io.progress.listeners.ProgressListenerConsole;
import com.utils.io.zip.ZipFileExtractor7z;
import com.utils.log.Logger;
import com.utils.string.StrUtils;

class HttpHandlerUpload implements HttpHandler {

	private final FileSynchronizerServerSettings fileSynchronizerServerSettings;

	HttpHandlerUpload(
			final FileSynchronizerServerSettings fileSynchronizerServerSettings) {

		this.fileSynchronizerServerSettings = fileSynchronizerServerSettings;
	}

	@Override
	public void handle(
			final HttpExchange httpExchange) {

		boolean useSandbox = false;

		String tmpZipFilePathString = null;
		final OutputStream resposeBodyOutputStream = httpExchange.getResponseBody();
		try {
			Logger.printNewLine();
			Instant start = Instant.now();
			Logger.printStatus("Received upload request at " + StrUtils.createDisplayDateTimeString(start));

			boolean uploadCompletedSuccessfully = false;
			try {
				final Headers requestHeaders = httpExchange.getRequestHeaders();

				final String encodedFilePathString = requestHeaders.getFirst("filePathString");
				String filePathString = FileSynchronizerUtils.decodeFilePathString(encodedFilePathString);

				final String useSandboxString = requestHeaders.getFirst("useSandbox");
				useSandbox = Boolean.parseBoolean(useSandboxString);
				Logger.printLine("use sandbox: " + useSandbox);

				if (useSandbox) {

					final String sandboxFilePathString =
							fileSynchronizerServerSettings.getSandboxFilePathString();
					filePathString = PathUtils.computePath(sandboxFilePathString,
							StringUtils.replace(filePathString, ":", ""));
				}

				Logger.printLine("File path:");
				Logger.printLine(filePathString);

				final String contentLengthString = requestHeaders.getFirst("Content-length");
				final long contentLength = StrUtils.tryParsePositiveLong(contentLengthString);

				final String fileName = PathUtils.computeFileName(filePathString);

				final String tmpFilePathString = fileSynchronizerServerSettings.getTmpFilePathString();
				tmpZipFilePathString = PathUtils.computePath(tmpFilePathString,
						String.valueOf(System.nanoTime()), fileName + ".zip");

				final boolean createParentDirectoriesSuccess = FactoryFolderCreator.getInstance()
						.createParentDirectories(tmpZipFilePathString, false, true);
				if (createParentDirectoriesSuccess) {

					final InputStream inputStream = new ProgressInputStream(httpExchange.getRequestBody(),
							contentLength, new ProgressListenerConsole());
					try (OutputStream outputStream = new BufferedOutputStream(
							StreamUtils.openOutputStream(tmpZipFilePathString))) {

						final byte[] buffer = new byte[FileSynchronizerUtils.BUFFER_SIZE];
						IOUtils.copyLarge(inputStream, outputStream, buffer);
					}

					filePathString = PathUtils.computeParentPath(filePathString);

					final String sevenZipExecutablePathString =
							fileSynchronizerServerSettings.getSevenZipExecutablePathString();
					final ZipFileExtractor7z zipFileExtractor7z = new ZipFileExtractor7z(
							sevenZipExecutablePathString, tmpZipFilePathString, filePathString, true);
					zipFileExtractor7z.work();

					uploadCompletedSuccessfully = zipFileExtractor7z.isSuccess();
					if (uploadCompletedSuccessfully) {
						Logger.printStatus("Upload completed successfully for file path:" +
								System.lineSeparator() + filePathString);
					}
				}

			} catch (final Exception exc) {
				Logger.printError("failed to complete the upload request");
				Logger.printException(exc);
			}

			final Headers responseHeaders = httpExchange.getResponseHeaders();
			responseHeaders.set("Content-type", "text/plain");
			responseHeaders.set("uploadCompletedSuccessfully",
					String.valueOf(uploadCompletedSuccessfully));

			httpExchange.sendResponseHeaders(200, 0);

			Logger.printStatus("Response sent successfully after " + StrUtils.durationToString(start));

		} catch (final Exception exc) {
			Logger.printError("failed to handle download request");
			Logger.printException(exc);

		} finally {
			try {
				resposeBodyOutputStream.close();

			} catch (final Exception exc) {
				Logger.printError("failed to close response output stream");
				Logger.printException(exc);
			}
			if (!useSandbox && IoUtils.fileExists(tmpZipFilePathString)) {
				FactoryFileDeleter.getInstance().deleteFile(tmpZipFilePathString, false, true);
			}
		}
	}
}
