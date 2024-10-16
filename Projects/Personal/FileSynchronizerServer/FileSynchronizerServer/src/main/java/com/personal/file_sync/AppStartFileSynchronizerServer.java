package com.personal.file_sync;

import com.personal.file_sync.dist.FileSynchronizerHttpsServer;
import com.personal.file_sync.settings.FactoryFileSynchronizerServerSettings;
import com.personal.file_sync.settings.FileSynchronizerServerSettings;
import com.utils.log.progress.ProgressIndicatorConsole;
import com.utils.log.progress.ProgressIndicators;

final class AppStartFileSynchronizerServer {

	private AppStartFileSynchronizerServer() {
	}

	public static void main(
			final String[] args) {

		ProgressIndicators.setInstance(ProgressIndicatorConsole.INSTANCE);

		final FileSynchronizerServerSettings fileSynchronizerServerSettings =
				FactoryFileSynchronizerServerSettings.newInstance(args);
		if (fileSynchronizerServerSettings != null) {

			final FileSynchronizerHttpsServer fileSynchronizerHttpsServer =
					new FileSynchronizerHttpsServer(fileSynchronizerServerSettings);
			fileSynchronizerHttpsServer.start();
		}
	}
}
