package com.personal.file_sync.settings;

import java.io.OutputStream;
import java.util.Properties;

import com.utils.io.StreamUtils;
import com.utils.io.folder_creators.FactoryFolderCreator;
import com.utils.io.ro_flag_clearers.FactoryReadOnlyFlagClearer;
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

	public void save(
			final String settingsFolderPathString) {

		try {
			final String fileSynchronizerSettingsPathString = FactoryFileSynchronizerSettings
					.createFileSynchronizerSettingsPathString(settingsFolderPathString);
			Logger.printProgress("saving settings to:");
			Logger.printLine(fileSynchronizerSettingsPathString);

			final boolean createParentDirectoriesSuccess = FactoryFolderCreator.getInstance()
					.createParentDirectories(fileSynchronizerSettingsPathString, false, true);
			if (createParentDirectoriesSuccess) {

				final boolean clearReadOnlyFlagFileSuccess = FactoryReadOnlyFlagClearer.getInstance()
						.clearReadOnlyFlagFile(fileSynchronizerSettingsPathString, false, true);
				if (clearReadOnlyFlagFileSuccess) {

					try (OutputStream outputStream =
							StreamUtils.openBufferedOutputStream(fileSynchronizerSettingsPathString)) {

						final Properties properties = new Properties();
						properties.setProperty("ServerIpAddr", serverIpAddr);
						properties.setProperty("ClientIpAddr", clientIpAddr);
						properties.store(outputStream, "");
					}
				}
			}

		} catch (final Throwable throwable) {
			Logger.printError("failed to save the settings");
			Logger.printThrowable(throwable);
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
