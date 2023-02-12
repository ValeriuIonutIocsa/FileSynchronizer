package com.personal.file_sync.settings.modes;

import org.apache.commons.lang3.StringUtils;

public final class FactoryMode {

	private static final Mode[] VALUES = Mode.values();

	private FactoryMode() {
	}

	public static Mode newInstance(
            final String nameParam) {

		Mode mode = null;
		for (final Mode aMode : VALUES) {

			final String name = aMode.name();
			if (StringUtils.equals(name, nameParam)) {
				mode = aMode;
			}
		}
		return mode;
	}
}
