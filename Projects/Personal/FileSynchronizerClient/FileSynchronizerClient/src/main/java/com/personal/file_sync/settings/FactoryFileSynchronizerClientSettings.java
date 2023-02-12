package com.personal.file_sync.settings;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.personal.file_sync.settings.modes.FactoryMode;
import com.personal.file_sync.settings.modes.Mode;
import com.utils.cli.CliUtils;
import com.utils.io.PathUtils;
import com.utils.log.Logger;
import com.utils.string.StrUtils;

public final class FactoryFileSynchronizerClientSettings {

	private FactoryFileSynchronizerClientSettings() {
	}

	public static FileSynchronizerClientSettings newInstance(
			final String[] args) {

		final FileSynchronizerClientSettings fileSynchronizerClientSettings;

		final Map<String, String> cliArgsByNameMap = new HashMap<>();
		CliUtils.fillCliArgsByNameMap(args, cliArgsByNameMap);

		final String debugModeString = cliArgsByNameMap.get("debug");
		final boolean debugMode = Boolean.parseBoolean(debugModeString);
		Logger.setDebugMode(debugMode);

		final String modeString = cliArgsByNameMap.get("mode");
		final Mode mode = FactoryMode.newInstance(modeString);
		if (mode == null) {

			Logger.printWarning("missing or invalid CLI argument \"mode\"");
			fileSynchronizerClientSettings = null;

		} else {
			final boolean keepGoing;
			final boolean useSandbox;
			String filePathString;
			final String hostname;
			final int port;
			if (mode == Mode.CLEAN) {

				keepGoing = true;
				useSandbox = false;
				filePathString = null;
				hostname = null;
				port = -1;

			} else {
				final String useSandboxString = cliArgsByNameMap.get("useSandbox");
				if (useSandboxString != null) {
					useSandbox = Boolean.parseBoolean(useSandboxString);
				} else {
					useSandbox = true;
				}

				filePathString = cliArgsByNameMap.get("filePath");
				if (StringUtils.isBlank(filePathString)) {
					filePathString = PathUtils.computeAbsolutePath(null, null, "");
				} else {
					filePathString = PathUtils.computeAbsolutePath(null, null, filePathString);
				}

				hostname = cliArgsByNameMap.get("hostname");
				if (StringUtils.isBlank(hostname)) {

					Logger.printWarning("missing or invalid CLI argument \"hostname\"");
					keepGoing = false;
					port = -1;

				} else {
					final String portString = cliArgsByNameMap.get("port");
					port = StrUtils.tryParsePositiveInt(portString);
					if (port < 0) {

						Logger.printWarning("missing or invalid CLI argument \"port\"");
						keepGoing = false;

					} else {
						keepGoing = true;
					}
				}
			}
			if (keepGoing) {
				fileSynchronizerClientSettings = new FileSynchronizerClientSettings(
						mode, useSandbox, filePathString, hostname, port);
			} else {
				fileSynchronizerClientSettings = null;
			}
		}

		return fileSynchronizerClientSettings;
	}

	public static FileSynchronizerClientSettings newInstance(
			final Mode mode,
			final boolean useSandbox,
			final String folderPathString,
			final String hostname,
			final int port) {

		return new FileSynchronizerClientSettings(mode, useSandbox, folderPathString, hostname, port);
	}
}
