package com.personal.file_sync.dist;

import java.io.BufferedOutputStream;
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
import com.utils.io.folder_creators.FactoryFolderCreator;
import com.utils.io.progress.ProgressInputStream;
import com.utils.io.progress.listeners.ProgressListenerConsole;
import com.utils.io.zip.ZipFileExtractor;
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

		String tmpZipFilePathString = null;
		try {
			Logger.printNewLine();
			Logger.printStatus("Received upload request");

			boolean uploadCompletedSuccessfully = false;
			try {
				final Headers requestHeaders = httpExchange.getRequestHeaders();

				String filePathString = requestHeaders.getFirst("filePathString");

				final String folderString = requestHeaders.getFirst("folder");
				final boolean folder = Boolean.parseBoolean(folderString);

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

				final String contentLengthString = requestHeaders.getFirst("Content-length");
				final long contentLength = StrUtils.tryParsePositiveLong(contentLengthString);

				final String tmpFilePathString = fileSynchronizerServerSettings.getTmpFilePathString();
				tmpZipFilePathString = PathUtils.computePath(tmpFilePathString, System.nanoTime() + ".zip");

				FactoryFolderCreator.getInstance().createParentDirectories(tmpZipFilePathString, true);

				final InputStream inputStream = new ProgressInputStream(httpExchange.getRequestBody(),
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
					deleteExisting = false;
					filePathString = PathUtils.computeParentPath(filePathString);
				}

				final ZipFileExtractor zipFileExtractor = new ZipFileExtractor(
						tmpZipFilePathString, filePathString, useFileCache, deleteExisting, 12, false, false);
				zipFileExtractor.work();

				uploadCompletedSuccessfully = zipFileExtractor.isSuccess();
				if (uploadCompletedSuccessfully) {
					Logger.printStatus("Upload completed successfully for file path:" +
							System.lineSeparator() + filePathString);
				}

			} catch (final Exception exc) {
				Logger.printError("failed to complete the upload request");
				Logger.printException(exc);
			}

			httpExchange.getResponseHeaders().set("Content-type", "text/plain");
			httpExchange.getResponseHeaders().set("uploadCompletedSuccessfully",
					String.valueOf(uploadCompletedSuccessfully));
			httpExchange.sendResponseHeaders(200, 0);

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
