package com.personal.file_sync.settings;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.utils.cli.CliUtils;
import com.utils.io.PathUtils;
import com.utils.log.Logger;
import com.utils.string.StrUtils;

public final class FactoryFileSynchronizerServerSettings {

	private FactoryFileSynchronizerServerSettings() {
	}

	public static FileSynchronizerServerSettings newInstance(
			final String[] args) {

		final FileSynchronizerServerSettings fileSynchronizerServerSettings;

		final Map<String, String> cliArgsByNameMap = new HashMap<>();
		CliUtils.fillCliArgsByNameMap(args, cliArgsByNameMap);

		final String debugModeString = cliArgsByNameMap.get("debug");
		final boolean debugMode = Boolean.parseBoolean(debugModeString);
		Logger.setDebugMode(debugMode);

		final String hostname = cliArgsByNameMap.get("hostname");
		if (StringUtils.isBlank(hostname)) {

			Logger.printWarning("missing CLI argument \"hostname\"");
			fileSynchronizerServerSettings = null;

		} else {
			final String portString = cliArgsByNameMap.get("port");
			final int port = StrUtils.tryParsePositiveInt(portString);
			if (port < 0) {

				Logger.printWarning("missing or invalid CLI argument \"port\"");
				fileSynchronizerServerSettings = null;

			} else {
				final String backlogString = cliArgsByNameMap.get("backlog");
				final int backlog = StrUtils.tryParsePositiveInt(backlogString);
				if (backlog < 0) {

					Logger.printWarning("missing or invalid CLI argument \"backlog\"");
					fileSynchronizerServerSettings = null;

				} else {
					final String threadCountString = cliArgsByNameMap.get("threadCount");
					final int threadCount = StrUtils.tryParseInt(threadCountString);
					if (threadCount < 0) {

						Logger.printWarning("missing or invalid CLI argument \"threadCount\"");
						fileSynchronizerServerSettings = null;

					} else {
						final String tmpFolderPathString = PathUtils.computePath(SystemUtils.USER_HOME,
								"FileSynchronizer", "tmp", "server");
						final String sandboxFolderPathString = PathUtils.computePath(SystemUtils.USER_HOME,
								"FileSynchronizer", "uploads");

						fileSynchronizerServerSettings = new FileSynchronizerServerSettings(
								hostname, port, backlog, threadCount,
								tmpFolderPathString, sandboxFolderPathString);
					}
				}
			}
		}

		return fileSynchronizerServerSettings;
	}
}
