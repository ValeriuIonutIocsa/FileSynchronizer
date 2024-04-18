package com.personal.file_sync.dist;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

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
			Logger.printError("failed to create CustomSSLContext");
			Logger.printException(exc);
		}
		return customSslContext;
	}

	public static String encodeFilePathString(
			String filePathString) {

		final String userHomePathString = SystemUtils.USER_HOME;
		return StringUtils.replace(filePathString, userHomePathString, "%USER_HOME%");
	}

	public static String decodeFilePathString(
			String filePathString) {

		final String userHomePathString = SystemUtils.USER_HOME;
		return StringUtils.replace(filePathString, "%USER_HOME%", userHomePathString);
	}
}
