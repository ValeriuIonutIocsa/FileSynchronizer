package com.personal.file_sync.settings;

import com.utils.string.StrUtils;

public class FileSynchronizerServerSettings {

	private final String ipAddr;
	private final int port;
	private final boolean ssl;
	private final int backlog;
	private final int threadCount;
	private final String tmpFilePathString;
	private final String sandboxFilePathString;
	private final String sevenZipExecutablePathString;
	private final int sevenZipThreadCount;

	FileSynchronizerServerSettings(
			final String ipAddr,
			final int port,
			final boolean ssl,
			final int backlog,
			final int threadCount,
			final String tmpFilePathString,
			final String sandboxFilePathString,
			final String sevenZipExecutablePathString,
			final int sevenZipThreadCount) {

		this.ipAddr = ipAddr;
		this.port = port;
		this.ssl = ssl;
		this.backlog = backlog;
		this.threadCount = threadCount;
		this.tmpFilePathString = tmpFilePathString;
		this.sandboxFilePathString = sandboxFilePathString;
		this.sevenZipExecutablePathString = sevenZipExecutablePathString;
		this.sevenZipThreadCount = sevenZipThreadCount;
	}

	@Override
	public String toString() {
		return StrUtils.reflectionToString(this);
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public int getPort() {
		return port;
	}

	public boolean isSsl() {
		return ssl;
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

	public String getSevenZipExecutablePathString() {
		return sevenZipExecutablePathString;
	}

	public int getSevenZipThreadCount() {
		return sevenZipThreadCount;
	}
}
