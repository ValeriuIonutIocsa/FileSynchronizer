package com.personal.file_sync.dist;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

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
import com.utils.io.progress.ProgressInputStream;
import com.utils.io.progress.listeners.ProgressListenerConsole;
import com.utils.io.zip.ZipFileCreator;
import com.utils.log.Logger;

class HttpHandlerDownload implements HttpHandler {

	private final FileSynchronizerServerSettings fileSynchronizerServerSettings;

	HttpHandlerDownload(
			final FileSynchronizerServerSettings fileSynchronizerServerSettings) {

		this.fileSynchronizerServerSettings = fileSynchronizerServerSettings;
	}

	@Override
	public void handle(
			final HttpExchange httpExchange) {

		String tmpZipFilePathString = null;
		try {
			Logger.printNewLine();
			Logger.printStatus("Received download request");

			boolean preparedRequestedFile = false;
			boolean folder = false;
			String filePathString = null;
			try {
				final Headers requestHeaders = httpExchange.getRequestHeaders();

				filePathString = requestHeaders.getFirst("filePathString");

				final String useFileCacheString = requestHeaders.getFirst("useFileCache");
				final boolean useFileCache = Boolean.parseBoolean(useFileCacheString);
				Logger.printLine("use file cache: " + useFileCache);

				final String useSandboxString = requestHeaders.getFirst("useSandbox");
				final boolean useSandbox = Boolean.parseBoolean(useSandboxString);
				Logger.printLine("use sandbox: " + useSandbox);

				if (useSandbox) {

					final String sandboxFilePathString =
							fileSynchronizerServerSettings.getSandboxFilePathString();
					filePathString = PathUtils.computePath(sandboxFilePathString,
							StringUtils.replace(filePathString, ":", ""));
				}

				Logger.printLine("File path:");
				Logger.printLine(filePathString);

				if (!IoUtils.fileExists(filePathString)) {
					Logger.printWarning("the requested file does not exist:" +
							System.lineSeparator() + filePathString);

				} else {
					final String tmpFilePathString =
							fileSynchronizerServerSettings.getTmpFilePathString();
					tmpZipFilePathString =
							PathUtils.computePath(tmpFilePathString, System.nanoTime() + ".zip");

					final ZipFileCreator zipFileCreator = new ZipFileCreator(
							filePathString, tmpZipFilePathString, useFileCache, true, 12, false, false);
					zipFileCreator.work();

					preparedRequestedFile = zipFileCreator.isSuccess();
					folder = zipFileCreator.isFolder();
				}

			} catch (final Exception exc) {
				Logger.printError("failed to prepare requested file");
				Logger.printException(exc);
			}

			Logger.printLine("preparedRequestedFile: " + preparedRequestedFile);

			final Headers responseHeaders = httpExchange.getResponseHeaders();
			responseHeaders.set("preparedRequestedFile", String.valueOf(preparedRequestedFile));
			responseHeaders.set("folder", String.valueOf(folder));

			if (!preparedRequestedFile) {
				httpExchange.sendResponseHeaders(200, 0);

			} else {
				final long zipFileLength = new File(tmpZipFilePathString).length();
				httpExchange.sendResponseHeaders(200, zipFileLength);

				final OutputStream outputStream = httpExchange.getResponseBody();
				try (InputStream inputStream = new ProgressInputStream(
						StreamUtils.openInputStream(tmpZipFilePathString),
						zipFileLength, new ProgressListenerConsole())) {

					final byte[] buffer = new byte[FileSynchronizerUtils.BUFFER_SIZE];
					IOUtils.copyLarge(inputStream, outputStream, buffer);
				}
			}

			Logger.printStatus("Download completed successfully for file path:" +
					System.lineSeparator() + filePathString);

			Logger.printStatus("Response sent successfully.");

		} catch (final Exception exc) {
			Logger.printError("failed to handle download request");
			Logger.printException(exc);

		} finally {
			if (IoUtils.fileExists(tmpZipFilePathString)) {
				FactoryFileDeleter.getInstance().deleteFile(tmpZipFilePathString, true);
			}
		}
	}
}
