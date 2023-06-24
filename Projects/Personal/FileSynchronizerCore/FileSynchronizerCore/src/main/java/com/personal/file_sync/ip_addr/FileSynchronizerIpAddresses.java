package com.personal.file_sync.ip_addr;

import java.util.Map;

public class FileSynchronizerIpAddresses {

	private final Map<String, String> nameToIpAddrMap;

	FileSynchronizerIpAddresses(
			final Map<String, String> nameToIpAddrMap) {

		this.nameToIpAddrMap = nameToIpAddrMap;
	}

	public String computeIpAddr(
			final String name) {

		String ipAddr = name;
		if (nameToIpAddrMap.containsKey(name)) {
			ipAddr = nameToIpAddrMap.get(name);
		}
		return ipAddr;
	}
}
