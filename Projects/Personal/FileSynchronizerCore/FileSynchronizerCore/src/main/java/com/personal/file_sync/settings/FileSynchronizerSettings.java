package com.personal.file_sync.settings;

import java.io.OutputStream;
import java.util.Properties;

import com.utils.io.StreamUtils;
import com.utils.log.Logger;

public class FileSynchronizerSettings {

	private String serverIpAddr;
	private String clientIpAddr;

	FileSynchronizerSettings(
			final String serverIpAddr,
			final String clientIpAddr) {

		this.serverIpAddr = serverIpAddr;
		this.clientIpAddr = clientIpAddr;
	}

	public void save() {

		try {
			final String fileSynchronizerSettingsPathString =
					FactoryFileSynchronizerSettings.createFileSynchronizerSettingsPathString();
			Logger.printProgress("saving settings to:");
			Logger.printLine(fileSynchronizerSettingsPathString);

			try (OutputStream outputStream =
					StreamUtils.openBufferedOutputStream(fileSynchronizerSettingsPathString)) {

				final Properties properties = new Properties();
				properties.setProperty("ServerIpAddr", serverIpAddr);
				properties.setProperty("ClientIpAddr", clientIpAddr);
				properties.store(outputStream, "");
			}

		} catch (final Exception exc) {
			Logger.printError("failed to save the settings");
			Logger.printException(exc);
		}
	}

	public void setServerIpAddr(
			final String serverIpAddr) {
		this.serverIpAddr = serverIpAddr;
	}

	public String getServerIpAddr() {
		return serverIpAddr;
	}

	public void setClientIpAddr(
			final String clientIpAddr) {
		this.clientIpAddr = clientIpAddr;
	}

	public String getClientIpAddr() {
		return clientIpAddr;
	}
}
