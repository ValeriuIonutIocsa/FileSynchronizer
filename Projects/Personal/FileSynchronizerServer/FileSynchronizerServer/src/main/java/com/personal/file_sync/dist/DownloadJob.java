package com.personal.file_sync.dist;

import java.time.Instant;

final class DownloadJob {

	private final boolean useSandbox;

	private final Instant start;

	private volatile String status;
	private volatile String filePathString;
	private volatile String tmpZipFilePathString;

	DownloadJob(
			final boolean useSandbox) {

		this.useSandbox = useSandbox;

		start = Instant.now();

		status = HttpHandlerDownload.STATUS_PREPARING;
	}

	boolean isUseSandbox() {
		return useSandbox;
	}

	Instant getStart() {
		return start;
	}

	void setStatus(
			final String status) {
		this.status = status;
	}

	String getStatus() {
		return status;
	}

	void setFilePathString(
			final String filePathString) {
		this.filePathString = filePathString;
	}

	String getFilePathString() {
		return filePathString;
	}

	void setTmpZipFilePathString(
			final String tmpZipFilePathString) {
		this.tmpZipFilePathString = tmpZipFilePathString;
	}

	String getTmpZipFilePathString() {
		return tmpZipFilePathString;
	}
}
