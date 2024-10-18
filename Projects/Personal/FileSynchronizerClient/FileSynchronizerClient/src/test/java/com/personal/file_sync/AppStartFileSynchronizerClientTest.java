package com.personal.file_sync;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.personal.file_sync.settings.modes.Mode;
import com.utils.test.TestInputUtils;

class AppStartFileSynchronizerClientTest {

	private static Mode mode;
	private static String filePathString;

	@BeforeAll
	static void beforeAll() {

		mode = configureMode();
		filePathString = configureFilePathString();
	}

	private static Mode configureMode() {

		final int inputMode = TestInputUtils.parseTestInputNumber("1");
		final Mode mode;
		if (inputMode == 1) {
			mode = Mode.DOWNLOAD;
		} else if (inputMode == 2) {
			mode = Mode.UPLOAD;
		} else {
			throw new RuntimeException();
		}
		return mode;
	}

	private static String configureFilePathString() {

		final String filePathString;
		final int inputFilePathString = TestInputUtils.parseTestInputNumber("31");
		if (inputFilePathString == 1) {
			filePathString = "D:\\tmp\\GradleSrcMan\\RegexGenerator";

		} else if (inputFilePathString == 11) {
			filePathString = "D:\\tmp\\GradleSrcMan\\RegexGenerator\\RegexGenerator_ReportFilePathLengths.csv";

		} else if (inputFilePathString == 21) {
			filePathString = "D:\\IVI_MISC\\Misc\\mnf\\test\\folder with spaces\\second folder with spaces";

		} else if (inputFilePathString == 31) {
			filePathString = "D:\\VT_IVI_MISC\\Tmp\\CRO\\ProjectAnalyzer\\" +
					"AutomatedTesting\\PaTestData\\LocalVsRawDataTestDiff";

		} else {
			throw new RuntimeException();
		}
		return filePathString;
	}

	@Test
	void testMain() {

		final String ipAddr;
		final int input = TestInputUtils.parseTestInputNumber("2");
		if (input == 1) {
			ipAddr = "localhost";
		} else if (input == 2) {
			ipAddr = "main_bench";
		} else {
			throw new RuntimeException();
		}

		final boolean ssl = TestInputUtils.parseTestInputBoolean("false");

		final List<String> cliArgList = new ArrayList<>();
		cliArgList.add("--debug=" + Boolean.TRUE);
		cliArgList.add("--mode=" + mode);
		cliArgList.add("--useSandbox=" + Boolean.TRUE);

		cliArgList.add("--filePath=" + filePathString);
		cliArgList.add("--7z_executable_path=C:\\LocalApps\\FileSynchronizer\\7z.exe");

		cliArgList.add("--ipAddr=" + ipAddr);
		cliArgList.add("--port=8090");
		cliArgList.add("--ssl=" + ssl);

		final String[] args = cliArgList.toArray(new String[0]);
		AppStartFileSynchronizerClient.main(args);
	}
}
