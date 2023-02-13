package com.personal.file_sync.settings;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.SystemUtils;

import com.utils.io.IoUtils;
import com.utils.io.PathUtils;
import com.utils.io.StreamUtils;
import com.utils.log.Logger;

public final class FactoryFileSynchronizerSettings {

	private FactoryFileSynchronizerSettings() {
	}

	public static FileSynchronizerSettings newInstance() {

		FileSynchronizerSettings fileSynchronizerSettings = null;
		try {
			final String fileSynchronizerSettingsPathString =
					FactoryFileSynchronizerSettings.createFileSynchronizerSettingsPathString();
			if (IoUtils.fileExists(fileSynchronizerSettingsPathString)) {

				Logger.printProgress("loading settings from:");
				Logger.printLine(fileSynchronizerSettingsPathString);

				try (InputStream inputStream =
						StreamUtils.openBufferedInputStream(fileSynchronizerSettingsPathString)) {

					final Properties properties = new Properties();
					properties.load(inputStream);

					final String serverIpAddr = properties.getProperty("ServerIpAddr");
					final String clientIpAddr = properties.getProperty("ClientIpAddr");
					fileSynchronizerSettings = new FileSynchronizerSettings(serverIpAddr, clientIpAddr);
				}
			}

		} catch (final Exception exc) {
			Logger.printError("failed to load the settings");
			Logger.printException(exc);
		}
		return fileSynchronizerSettings;
	}

	public static FileSynchronizerSettings newInstanceBlank() {

		return new FileSynchronizerSettings("", "");
	}

	static String createFileSynchronizerSettingsPathString() {

		return PathUtils.computePath(SystemUtils.USER_HOME,
				"FileSynchronizer", "FileSynchronizer.properties");
	}
}
