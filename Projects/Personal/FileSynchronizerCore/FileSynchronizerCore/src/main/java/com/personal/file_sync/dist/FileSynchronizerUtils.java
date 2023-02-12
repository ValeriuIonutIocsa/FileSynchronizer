package com.personal.file_sync.dist;

import java.io.InputStream;

import com.utils.io.ResourceFileUtils;
import com.utils.log.Logger;
import com.utils.net.ssl.CustomSslContext;
import com.utils.net.ssl.FactoryCustomSslContext;

final class FileSynchronizerUtils {

	private static final String PASSWORD = "CROCROCRO";

	static final int BUFFER_SIZE = 8192;

	private FileSynchronizerUtils() {
	}

	static CustomSslContext createSslContext(
			final String keyStoreResourcePath,
			final String trustStoreResourcePath) {

		CustomSslContext customSslContext = null;
		try (InputStream keyStoreInputStream =
				ResourceFileUtils.resourceFileToInputStream(keyStoreResourcePath);
				InputStream trustStoreInputStream =
						ResourceFileUtils.resourceFileToInputStream(trustStoreResourcePath)) {

			customSslContext = FactoryCustomSslContext.newInstance(
					FactoryCustomSslContext.KeyStoreType.JKS, keyStoreInputStream,
					PASSWORD, PASSWORD, trustStoreInputStream, PASSWORD);

		} catch (final Exception exc) {
			Logger.printError("failed to create CustomSSLContext!");
			Logger.printException(exc);
		}
		return customSslContext;
	}
}
