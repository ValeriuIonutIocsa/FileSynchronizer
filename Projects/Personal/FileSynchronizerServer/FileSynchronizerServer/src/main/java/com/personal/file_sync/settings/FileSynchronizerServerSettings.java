package com.personal.file_sync.settings;

import com.utils.string.StrUtils;

public class FileSynchronizerServerSettings {

	private final String hostname;
	private final int port;
	private final int backlog;
	private final int threadCount;
	private final String tmpFilePathString;
	private final String sandboxFilePathString;

	FileSynchronizerServerSettings(
			final String hostname,
			final int port,
			final int backlog,
			final int threadCount,
			final String tmpFilePathString,
			final String sandboxFilePathString) {

		this.hostname = hostname;
		this.port = port;
		this.backlog = backlog;
		this.threadCount = threadCount;
		this.tmpFilePathString = tmpFilePathString;
		this.sandboxFilePathString = sandboxFilePathString;
	}

	@Override
	public String toString() {
		return StrUtils.reflectionToString(this);
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public int getBacklog() {
		return backlog;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public String getTmpFilePathString() {
		return tmpFilePathString;
	}

	public String getSandboxFilePathString() {
		return sandboxFilePathString;
	}
}
