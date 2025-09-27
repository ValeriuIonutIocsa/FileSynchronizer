package com.personal.file_sync.app_info;

import com.utils.app_info.AppInfo;

public final class FactoryAppInfoFileSynchronizerServer {

	private FactoryAppInfoFileSynchronizerServer() {
	}

	public static AppInfo newInstance() {

		final String appTitleDefault = "File Synchronizer Server";
		final String appVersionDefault = "1.0.0";
		return com.utils.app_info.FactoryAppInfo.computeInstance(appTitleDefault, appVersionDefault);
	}
}
