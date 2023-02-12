package com.personal.file_sync;

import java.time.Instant;

import com.personal.file_sync.dist.FileSynchronizerHttpsClient;
import com.personal.file_sync.settings.FactoryFileSynchronizerClientSettings;
import com.personal.file_sync.settings.FileSynchronizerClientSettings;
import com.utils.log.Logger;

final class AppStartFileSynchronizerClient {

	private AppStartFileSynchronizerClient() {
	}

	public static void main(
			final String[] args) {

		final Instant start = Instant.now();

		final FileSynchronizerClientSettings fileSynchronizerClientSettings =
				FactoryFileSynchronizerClientSettings.newInstance(args);
		if (fileSynchronizerClientSettings != null) {

			final FileSynchronizerHttpsClient fileSynchronizerHttpsClient =
					new FileSynchronizerHttpsClient(fileSynchronizerClientSettings);
			fileSynchronizerHttpsClient.work();
		}

		Logger.printFinishMessage(start);
	}
}
