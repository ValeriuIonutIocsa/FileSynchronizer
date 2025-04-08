package com.personal.file_sync;

import com.personal.file_sync.dist.FileSynchronizerHttpServer;
import com.personal.file_sync.settings.FactoryFileSynchronizerServerSettings;
import com.personal.file_sync.settings.FileSynchronizerServerSettings;
import com.utils.io.file_deleters.FactoryFileDeleter;
import com.utils.io.file_deleters.FileDeleterWin;
import com.utils.io.folder_deleters.FactoryFolderDeleter;
import com.utils.io.folder_deleters.FolderDeleterWin;
import com.utils.log.progress.ProgressIndicatorConsole;
import com.utils.log.progress.ProgressIndicators;

final class AppStartFileSynchronizerServer {

	private AppStartFileSynchronizerServer() {
	}

	public static void main(
			final String[] args) {

		ProgressIndicators.setInstance(ProgressIndicatorConsole.INSTANCE);

		FactoryFolderDeleter.setInstance(new FolderDeleterWin());
		FactoryFileDeleter.setInstance(new FileDeleterWin());

		final FileSynchronizerServerSettings fileSynchronizerServerSettings =
				FactoryFileSynchronizerServerSettings.newInstance(args);
		if (fileSynchronizerServerSettings != null) {

			final FileSynchronizerHttpServer fileSynchronizerHttpServer =
					new FileSynchronizerHttpServer(fileSynchronizerServerSettings);
			fileSynchronizerHttpServer.start();
		}
	}
}
