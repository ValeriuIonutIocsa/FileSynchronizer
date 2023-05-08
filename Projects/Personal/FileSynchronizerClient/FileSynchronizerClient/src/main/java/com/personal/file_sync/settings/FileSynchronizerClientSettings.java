package com.personal.file_sync.settings;

import org.apache.commons.lang3.SystemUtils;

import com.personal.file_sync.settings.modes.Mode;
import com.utils.io.PathUtils;
import com.utils.string.StrUtils;

public class FileSynchronizerClientSettings {

	private final Mode mode;
	private final boolean useSandbox;
	private final String filePathString;
	private final String ipAddr;
	private final int port;
	private final String sevenZipExecutablePathString;

	private final String tmpFilePathString;

	FileSynchronizerClientSettings(
			final Mode mode,
			final boolean useSandbox,
			final String filePathString,
			final String ipAddr,
			final int port,
			final String sevenZipExecutablePathString) {

		this.mode = mode;
		this.useSandbox = useSandbox;
		this.filePathString = filePathString;
		this.ipAddr = ipAddr;
		this.port = port;
		this.sevenZipExecutablePathString = sevenZipExecutablePathString;

		tmpFilePathString = PathUtils.computePath(SystemUtils.USER_HOME,
				"FileSynchronizer", "tmp", "client");
	}

	@Override
	public String toString() {
		return StrUtils.reflectionToString(this);
	}

	public Mode getMode() {
		return mode;
	}

	public boolean isUseSandbox() {
		return useSandbox;
	}

	public String getFilePathString() {
		return filePathString;
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public int getPort() {
		return port;
	}

	public String getSevenZipExecutablePathString() {
		return sevenZipExecutablePathString;
	}

	public String getTmpFilePathString() {
		return tmpFilePathString;
	}
}
