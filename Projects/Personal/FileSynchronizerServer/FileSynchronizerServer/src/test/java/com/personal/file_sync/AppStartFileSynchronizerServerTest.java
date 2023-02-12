package com.personal.file_sync;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class AppStartFileSynchronizerServerTest {

	@Test
	void testMain() {

		final List<String> cliArgList = new ArrayList<>();
		cliArgList.add("--debug=" + Boolean.TRUE);
		cliArgList.add("--ipAddr=localhost");
		cliArgList.add("--port=8090");
		cliArgList.add("--backlog=0");
		cliArgList.add("--threadCount=12");

		final String[] args = cliArgList.toArray(new String[0]);
		AppStartFileSynchronizerServer.main(args);
	}
}
