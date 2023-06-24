package com.personal.file_sync.settings;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.personal.file_sync.ip_addr.FactoryFileSynchronizerIpAddresses;
import com.personal.file_sync.ip_addr.FileSynchronizerIpAddresses;
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

		FileSynchronizerClientSettings fileSynchronizerClientSettings = null;

		final Map<String, String> cliArgsByNameMap = new HashMap<>();
		CliUtils.fillCliArgsByNameMap(args, cliArgsByNameMap);

		final String debugModeString = cliArgsByNameMap.get("debug");
		final boolean debugMode = Boolean.parseBoolean(debugModeString);
		Logger.setDebugMode(debugMode);

		final String modeString = cliArgsByNameMap.get("mode");
		final Mode mode = FactoryMode.newInstance(modeString);
		if (mode == null) {
			Logger.printWarning("missing or invalid CLI argument \"mode\"");

		} else {
			final boolean keepGoing;
			boolean useSandbox = false;
			String filePathString = null;
			String ipAddr = null;
			int port = -1;
			String sevenZipExecutablePathString = null;
			if (mode == Mode.CLEAN) {
				keepGoing = true;

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

				sevenZipExecutablePathString = cliArgsByNameMap.get("7z_executable_path");
				if (StringUtils.isBlank(sevenZipExecutablePathString)) {

					Logger.printWarning("missing or invalid CLI argument \"7z_executable_path\"");
					keepGoing = false;

				} else {
					final String settingsFolderPathString =
							PathUtils.computeParentPath(sevenZipExecutablePathString);

					ipAddr = cliArgsByNameMap.get("ipAddr");
					FileSynchronizerSettings fileSynchronizerSettings = null;
					if (StringUtils.isBlank(ipAddr)) {

						fileSynchronizerSettings =
								FactoryFileSynchronizerSettings.newInstance(settingsFolderPathString);
						if (fileSynchronizerSettings != null) {
							ipAddr = fileSynchronizerSettings.getClientIpAddr();
						}
					}
					if (StringUtils.isBlank(ipAddr)) {

						Logger.printWarning("missing CLI argument \"ipAddr\" and also IP address is not cached");
						keepGoing = false;

					} else {
						if (fileSynchronizerSettings == null) {

							fileSynchronizerSettings =
									FactoryFileSynchronizerSettings.newInstance(settingsFolderPathString);
							if (fileSynchronizerSettings == null) {
								fileSynchronizerSettings = FactoryFileSynchronizerSettings.newInstanceBlank();
							}
							fileSynchronizerSettings.setClientIpAddr(ipAddr);
							fileSynchronizerSettings.save(settingsFolderPathString);
						}

						final FileSynchronizerIpAddresses fileSynchronizerIpAddresses =
								FactoryFileSynchronizerIpAddresses.newInstance(settingsFolderPathString);
						ipAddr = fileSynchronizerIpAddresses.computeIpAddr(ipAddr);

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
			}
			if (keepGoing) {
				fileSynchronizerClientSettings = new FileSynchronizerClientSettings(
						mode, useSandbox, filePathString, ipAddr, port, sevenZipExecutablePathString);
			}
		}

		return fileSynchronizerClientSettings;
	}
}
