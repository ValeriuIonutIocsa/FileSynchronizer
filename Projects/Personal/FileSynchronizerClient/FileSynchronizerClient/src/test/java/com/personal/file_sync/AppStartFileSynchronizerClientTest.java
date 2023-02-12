package com.personal.file_sync;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.personal.file_sync.settings.modes.Mode;

class AppStartFileSynchronizerClientTest {

	@Test
	void testMain() {

		final List<String> cliArgList = new ArrayList<>();
		cliArgList.add("--debug=" + Boolean.TRUE);

		final int inputMode = Integer.parseInt("2");
		final Mode mode;
		if (inputMode == 1) {
			mode = Mode.DOWNLOAD;
		} else if (inputMode == 2) {
			mode = Mode.UPLOAD;
		} else {
			throw new RuntimeException();
		}
		cliArgList.add("--mode=" + mode);

		cliArgList.add("--useSandbox=" + Boolean.TRUE);
		cliArgList.add("--ipAddr=localhost");
		cliArgList.add("--port=8090");

		final String filePathString;
		final int inputFilePathString = Integer.parseInt("11");
		if (inputFilePathString == 1) {
			filePathString = "D:\\tmp\\GradleSrcMan\\RegexGenerator";

		} else if (inputFilePathString == 11) {
			filePathString = "D:\\tmp\\GradleSrcMan\\RegexGenerator\\RegexGenerator_ReportFilePathLengths.csv";

		} else {
			throw new RuntimeException();
		}
		cliArgList.add("--filePath=" + filePathString);

		final String[] args = cliArgList.toArray(new String[0]);
		AppStartFileSynchronizerClient.main(args);
	}
}
