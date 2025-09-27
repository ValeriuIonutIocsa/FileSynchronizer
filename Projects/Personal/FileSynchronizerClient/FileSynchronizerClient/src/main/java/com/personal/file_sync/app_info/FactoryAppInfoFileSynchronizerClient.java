package com.personal.file_sync.app_info;

import com.utils.app_info.AppInfo;
import com.utils.app_info.FactoryAppInfo;

public final class FactoryAppInfoFileSynchronizerClient {

	private FactoryAppInfoFileSynchronizerClient() {
	}

	public static AppInfo newInstance() {

		final String appTitleDefault = "File Synchronizer Client";
		final String appVersionDefault = "1.0.0";
		return FactoryAppInfo.computeInstance(appTitleDefault, appVersionDefault);
	}
}
