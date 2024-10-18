package com.personal.file_sync;

import com.personal.file_sync.dist.FileSynchronizerHttpServer;
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

			final FileSynchronizerHttpServer fileSynchronizerHttpServer =
					new FileSynchronizerHttpServer(fileSynchronizerServerSettings);
			fileSynchronizerHttpServer.start();
		}
	}
}
