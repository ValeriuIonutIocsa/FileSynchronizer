package com.personal.file_sync;

import java.time.Instant;

import com.personal.file_sync.dist.FileSynchronizerHttpClient;
import com.personal.file_sync.settings.FactoryFileSynchronizerClientSettings;
import com.personal.file_sync.settings.FileSynchronizerClientSettings;
import com.utils.log.Logger;
import com.utils.log.progress.ProgressIndicatorConsole;
import com.utils.log.progress.ProgressIndicators;

final class AppStartFileSynchronizerClient {

	private AppStartFileSynchronizerClient() {
	}

	public static void main(
			final String[] args) {

		final Instant start = Instant.now();
		ProgressIndicators.setInstance(ProgressIndicatorConsole.INSTANCE);

		final FileSynchronizerClientSettings fileSynchronizerClientSettings =
				FactoryFileSynchronizerClientSettings.newInstance(args);
		if (fileSynchronizerClientSettings != null) {

			final FileSynchronizerHttpClient fileSynchronizerHttpClient =
					new FileSynchronizerHttpClient(fileSynchronizerClientSettings);
			fileSynchronizerHttpClient.work();
		}

		Logger.printFinishMessage(start);
	}
}
