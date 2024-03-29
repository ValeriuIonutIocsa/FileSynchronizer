package com.personal.file_sync;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.utils.concurrency.ThreadUtils;

class AppStartFileSynchronizerServerTest {

	@Test
	void testMain() {

		final List<String> cliArgList = new ArrayList<>();
		cliArgList.add("--debug=" + Boolean.TRUE);
		cliArgList.add("--ipAddr=localhost");
		cliArgList.add("--port=8090");
		cliArgList.add("--backlog=0");
		cliArgList.add("--threadCount=12");
		cliArgList.add("--7z_executable_path=C:\\LocalApps\\FileSynchronizer\\7z.exe");

		final String[] args = cliArgList.toArray(new String[0]);
		AppStartFileSynchronizerServer.main(args);

		ThreadUtils.trySleep(Long.MAX_VALUE);
	}
}
