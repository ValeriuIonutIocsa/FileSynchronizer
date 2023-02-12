package com.personal.file_sync;

import com.personal.file_sync.dist.FileSynchronizerHttpsServer;
import com.personal.file_sync.settings.FactoryFileSynchronizerServerSettings;
import com.personal.file_sync.settings.FileSynchronizerServerSettings;

final class AppStartFileSynchronizerServer {

	private AppStartFileSynchronizerServer() {
	}

	public static void main(
			final String[] args) {

		final FileSynchronizerServerSettings fileSynchronizerServerSettings =
				FactoryFileSynchronizerServerSettings.newInstance(args);
		if (fileSynchronizerServerSettings != null) {

			final FileSynchronizerHttpsServer fileSynchronizerHttpsServer =
					new FileSynchronizerHttpsServer(fileSynchronizerServerSettings);
			fileSynchronizerHttpsServer.start();
		}
	}
}
