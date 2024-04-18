package com.personal.file_sync.dist;

import org.junit.jupiter.api.Test;

import com.utils.log.Logger;

class FileSynchronizerUtilsTest {

	@Test
	void testEncodeFilePathString() {

		final String filePathString = "C:\\Users\\uid39522\\ProjectAnalyzer";
		final String encodedFilePathString =
				FileSynchronizerUtils.encodeFilePathString(filePathString);
		Logger.printLine("encodedFilePathString=" + encodedFilePathString);
	}

	@Test
	void testDecodeFilePathString() {

		final String encodedFilePathString = "%USER_HOME%\\ProjectAnalyzer";
		final String filePathString = FileSynchronizerUtils.decodeFilePathString(encodedFilePathString);
		Logger.printLine("filePathString=" + filePathString);
	}
}
