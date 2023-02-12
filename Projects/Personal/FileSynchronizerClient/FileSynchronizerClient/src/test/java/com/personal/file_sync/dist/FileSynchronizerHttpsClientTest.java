package com.personal.file_sync.dist;

import org.junit.jupiter.api.Test;

import com.personal.file_sync.settings.FactoryFileSynchronizerClientSettings;
import com.personal.file_sync.settings.FileSynchronizerClientSettings;
import com.personal.file_sync.settings.modes.Mode;

class FileSynchronizerHttpsClientTest {

	private static final String SERVER_IP_ADDRESS_PUBLIC = "79.113.71.62";
	private static final String SERVER_IP_ADDRESS_PRIVATE = "192.168.1.6";

	@Test
	void testExecuteUploadRequest() {

		final String folderPathString;
		final String hostname;
		final boolean useSandbox;
		final int input = Integer.parseInt("3");
		if (input == 1) {

			folderPathString = "D:\\tmp\\FileSynchronizer\\inputs";
			hostname = "localhost";
			useSandbox = true;

		} else if (input == 2) {

			folderPathString = "D:\\tmp\\FileSynchronizer\\inputs";
			hostname = SERVER_IP_ADDRESS_PRIVATE;
			useSandbox = false;

		} else if (input == 3) {

			folderPathString = "C:\\IVI\\Conti\\Main";
			hostname = SERVER_IP_ADDRESS_PUBLIC;
			useSandbox = false;

		} else {
			throw new RuntimeException();
		}

		final FileSynchronizerClientSettings fileSynchronizerClientSettings =
				FactoryFileSynchronizerClientSettings.newInstance(
						Mode.DOWNLOAD, useSandbox, folderPathString, hostname, 8090);

		final FileSynchronizerHttpsClient fileSynchronizerHttpsClient =
				new FileSynchronizerHttpsClient(fileSynchronizerClientSettings);
		fileSynchronizerHttpsClient.executeUploadRequest();
	}

	@Test
	void testExecuteDownloadRequest() {

		final String folderPathString;
		final String hostname;
		final boolean useSandbox;
		final int input = Integer.parseInt("3");
		if (input == 1) {

			folderPathString = "D:\\tmp\\FileSynchronizer\\inputs";
			hostname = "localhost";
			useSandbox = true;

		} else if (input == 2) {

			folderPathString = "D:\\tmp\\FileSynchronizer\\inputs";
			hostname = SERVER_IP_ADDRESS_PRIVATE;
			useSandbox = false;

		} else if (input == 3) {

			folderPathString = "C:\\IVI\\Conti\\Main";
			hostname = SERVER_IP_ADDRESS_PUBLIC;
			useSandbox = false;

		} else {
			throw new RuntimeException();
		}

		final FileSynchronizerClientSettings fileSynchronizerClientSettings =
				FactoryFileSynchronizerClientSettings.newInstance(
						Mode.UPLOAD, useSandbox, folderPathString, hostname, 8090);

		final FileSynchronizerHttpsClient fileSynchronizerHttpsClient =
				new FileSynchronizerHttpsClient(fileSynchronizerClientSettings);
		fileSynchronizerHttpsClient.executeDownloadRequest();
	}
}
