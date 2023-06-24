package com.personal.file_sync.ip_addr;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import com.utils.io.IoUtils;
import com.utils.io.PathUtils;
import com.utils.io.StreamUtils;
import com.utils.log.Logger;

public final class FactoryFileSynchronizerIpAddresses {

	private FactoryFileSynchronizerIpAddresses() {
	}

	public static FileSynchronizerIpAddresses newInstance(
            final String settingsFolderPathString) {

		FileSynchronizerIpAddresses fileSynchronizerIpAddresses = null;
		try {
			final String fileSynchronizerIpAddressesPathString =
					PathUtils.computePath(settingsFolderPathString, "FileSynchronizerIpAddresses.properties");
			if (IoUtils.fileExists(fileSynchronizerIpAddressesPathString)) {

				Logger.printProgress("loading ip addresses from:");
				Logger.printLine(fileSynchronizerIpAddressesPathString);

				try (InputStream inputStream =
						StreamUtils.openBufferedInputStream(fileSynchronizerIpAddressesPathString)) {

					final Properties properties = new Properties();
					properties.load(inputStream);

					final Map<String, String> nameToIpAddrMap = new LinkedHashMap<>();
					final Enumeration<?> propertyNamesEnumeration = properties.propertyNames();
					while (propertyNamesEnumeration.hasMoreElements()) {

						final Object nameElement = propertyNamesEnumeration.nextElement();
						final String name = nameElement.toString();
						final String ipAddr = properties.getProperty(name);
						nameToIpAddrMap.put(name, ipAddr);
					}
					fileSynchronizerIpAddresses = new FileSynchronizerIpAddresses(nameToIpAddrMap);
				}
			}

		} catch (final Exception exc) {
			Logger.printError("failed to load the ip addresses");
			Logger.printException(exc);
		}
		return fileSynchronizerIpAddresses;
	}
}
