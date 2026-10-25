package com.personal.file_sync.dist;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

	static final String STATUS_PREPARING = "PREPARING";
	static final String STATUS_READY = "READY";
	static final String STATUS_FAILED = "FAILED";

	private final FileSynchronizerServerSettings fileSynchronizerServerSettings;

	private final Map<String, DownloadJob> idToDownloadJobMap;

	HttpHandlerDownload(
			final FileSynchronizerServerSettings fileSynchronizerServerSettings) {

		this.fileSynchronizerServerSettings = fileSynchronizerServerSettings;

		idToDownloadJobMap = new ConcurrentHashMap<>();
	}

	@Override
	public void handle(
			final HttpExchange httpExchange) {

		try (httpExchange) {

			final Headers requestHeaders = httpExchange.getRequestHeaders();
			final String action = requestHeaders.getFirst("action");
			switch (action) {
				case "prepare" -> handlePrepare(httpExchange);
				case "status" -> handleStatus(httpExchange);
				case "fetch" -> handleFetch(httpExchange);
				case null, default -> {
					Logger.printWarning("unknown download action: " + action);
					httpExchange.sendResponseHeaders(400, -1);
				}
			}

		} catch (final Throwable throwable) {
			Logger.printError("failed to handle download request");
			Logger.printThrowable(throwable);
		}
	}

	private void handlePrepare(
			final HttpExchange httpExchange) throws Exception {

		Logger.printNewLine();
		Logger.printStatus("Received download request at " +
				StrUtils.createDisplayDateTimeString(Instant.now()));

		final Headers requestHeaders = httpExchange.getRequestHeaders();
		final String encodedFilePathString = requestHeaders.getFirst("filePathString");
		final String filePathString = FileSynchronizerUtils.decodeFilePathString(encodedFilePathString);
		final boolean useSandbox = Boolean.parseBoolean(requestHeaders.getFirst("useSandbox"));

		final DownloadJob downloadJob = new DownloadJob(useSandbox);
		final String jobId = UUID.randomUUID().toString();
		idToDownloadJobMap.put(jobId, downloadJob);

		final Thread thread = new Thread(() -> prepareZip(downloadJob, filePathString),
				"download-prepare-" + jobId);
		thread.setDaemon(true);
		thread.start();

		httpExchange.getResponseHeaders().set("jobId", jobId);
		httpExchange.sendResponseHeaders(200, -1);
	}

	private void prepareZip(
			final DownloadJob downloadJob,
			final String requestedFilePathString) {

		boolean preparedRequestedFile = false;
		try {
			String filePathString = requestedFilePathString;
			final boolean useSandbox = downloadJob.isUseSandbox();
			Logger.printLine("use sandbox: " + useSandbox);

			if (useSandbox) {

				final String sandboxFilePathString =
						fileSynchronizerServerSettings.getSandboxFilePathString();
				filePathString = PathUtils.computePath(sandboxFilePathString,
						Strings.CS.replace(filePathString, ":", ""));
			}
			downloadJob.setFilePathString(filePathString);

			Logger.printLine("File path:");
			Logger.printLine(filePathString);

			if (!IoUtils.fileExists(filePathString)) {
				Logger.printWarning("the requested file does not exist:" +
						System.lineSeparator() + filePathString);

			} else {
				final String fileName = PathUtils.computeFileName(filePathString);

				final String tmpFilePathString = fileSynchronizerServerSettings.getTmpFilePathString();
				final String tmpZipFilePathString = PathUtils.computePath(tmpFilePathString,
						String.valueOf(System.nanoTime()), fileName + ".zip");
				downloadJob.setTmpZipFilePathString(tmpZipFilePathString);

				final String sevenZipExecutablePathString =
						fileSynchronizerServerSettings.getSevenZipExecutablePathString();
				final int sevenZipThreadCount = fileSynchronizerServerSettings.getSevenZipThreadCount();
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
		if (preparedRequestedFile) {
			downloadJob.setStatus(STATUS_READY);
		} else {
			downloadJob.setStatus(STATUS_FAILED);
		}
	}

	private void handleStatus(
			final HttpExchange httpExchange) throws Exception {

		final String jobId = httpExchange.getRequestHeaders().getFirst("jobId");

		final DownloadJob downloadJob;
		if (jobId != null) {
			downloadJob = idToDownloadJobMap.get(jobId);
		} else {
			downloadJob = null;
		}

		final String status;
		if (downloadJob == null) {
			status = STATUS_FAILED;
		} else {
			status = downloadJob.getStatus();
			if (STATUS_FAILED.equals(status)) {
				idToDownloadJobMap.remove(jobId);
				deleteTmpZipFile(downloadJob);
			}
		}

		httpExchange.getResponseHeaders().set("status", status);
		httpExchange.sendResponseHeaders(200, -1);
	}

	private void handleFetch(
			final HttpExchange httpExchange) throws Exception {

		final String jobId = httpExchange.getRequestHeaders().getFirst("jobId");

		final DownloadJob downloadJob;
		if (jobId != null) {
			downloadJob = idToDownloadJobMap.remove(jobId);
		} else {
			downloadJob = null;
		}

		if (downloadJob == null || !STATUS_READY.equals(downloadJob.getStatus())) {
			httpExchange.getResponseHeaders().set("preparedRequestedFile", "false");
			httpExchange.sendResponseHeaders(200, -1);

		} else {
			try {
				final String tmpZipFilePathString = downloadJob.getTmpZipFilePathString();
				final long zipFileLength = new File(tmpZipFilePathString).length();
				httpExchange.getResponseHeaders().set("preparedRequestedFile", "true");
				httpExchange.sendResponseHeaders(200, zipFileLength);

				try (InputStream inputStream = new ProgressInputStream(
						StreamUtils.openInputStream(tmpZipFilePathString),
						zipFileLength, new ProgressListenerConsole());
						OutputStream outputStream = httpExchange.getResponseBody()) {

					final byte[] buffer = new byte[FileSynchronizerUtils.BUFFER_SIZE];
					IOUtils.copyLarge(inputStream, outputStream, buffer);
				}

				final String filePathString = downloadJob.getFilePathString();
				Logger.printStatus("Download completed successfully for file path:" +
						System.lineSeparator() + filePathString);

				final Instant start = downloadJob.getStart();
				Logger.printStatus("Response sent successfully after " + StrUtils.durationToString(start));

			} finally {
				deleteTmpZipFile(downloadJob);
			}
		}
	}

	private static void deleteTmpZipFile(
			final DownloadJob downloadJob) {

		final String tmpZipFilePathString = downloadJob.getTmpZipFilePathString();
		if (!downloadJob.isUseSandbox() && tmpZipFilePathString != null &&
				IoUtils.fileExists(tmpZipFilePathString)) {
			FactoryFileDeleter.getInstance().deleteFile(tmpZipFilePathString, true, true);
		}
	}
}
