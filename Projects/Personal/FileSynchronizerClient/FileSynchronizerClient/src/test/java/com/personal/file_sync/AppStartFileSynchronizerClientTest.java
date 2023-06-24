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

		final String filePathString;
		final int inputFilePathString = Integer.parseInt("21");
		if (inputFilePathString == 1) {
			filePathString = "D:\\tmp\\GradleSrcMan\\RegexGenerator";

		} else if (inputFilePathString == 11) {
			filePathString = "D:\\tmp\\GradleSrcMan\\RegexGenerator\\RegexGenerator_ReportFilePathLengths.csv";

		} else if (inputFilePathString == 21) {
			filePathString = "D:\\IVI_MISC\\Misc\\mnf\\test\\folder with spaces\\second folder with spaces";

		} else {
			throw new RuntimeException();
		}
		cliArgList.add("--filePath=" + filePathString);

		cliArgList.add("--port=8090");
		cliArgList.add("--7z_executable_path=C:\\LocalApps\\FileSynchronizer\\7z.exe");

		cliArgList.add("--ipAddr=localhost");

		final String[] args = cliArgList.toArray(new String[0]);
		AppStartFileSynchronizerClient.main(args);
	}
}
