package com.utils.http.server;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.utils.concurrency.ThreadUtils;
import com.utils.log.Logger;

public abstract class AbstractHttpServer<
		HttpHandlerWorkerT extends HttpHandlerWorker> {

	private final String displayName;
	private final int startingServerPort;
	private final int endServerPort;
	private final HttpHandlerWorkerT[] httpHandlerWorkerArray;

	protected AbstractHttpServer(
			final String displayName,
			final int startingServerPort,
			final int endServerPort,
			final HttpHandlerWorkerT[] httpHandlerWorkerArray) {

		this.displayName = displayName;
		this.startingServerPort = startingServerPort;
		this.endServerPort = endServerPort;
		this.httpHandlerWorkerArray = httpHandlerWorkerArray;
	}

	public void start() {

		try {
			Logger.printProgress("starting " + displayName + " server");

			HttpServer httpServer = null;
			int serverPort = -1;
			for (int port = startingServerPort; port <= endServerPort; port++) {

				try {
					final InetSocketAddress inetSocketAddress =
							new InetSocketAddress("0.0.0.0", port);
					httpServer = HttpServer.create(inetSocketAddress, 0);
					serverPort = port;
					break;

				} catch (final Exception ignored) {
				}
			}
			if (httpServer == null) {
				Logger.printError("failed to start " + displayName + " server");

			} else {
				Logger.printLine(displayName + " server port: " + serverPort);

				final Set<String> pathSet = new HashSet<>();
				for (final HttpHandlerWorkerT httpHandlerWorker : httpHandlerWorkerArray) {

					final String name = httpHandlerWorker.getName();
					final String path = "/" + name;
					final boolean notAddedAlready = pathSet.add(path);
					if (!notAddedAlready) {
						Logger.printError("duplicate endpoint path: " + path);

					} else {
						final HttpHandler httpHandler = new HttpHandlerImpl(httpHandlerWorker, false);
						httpServer.createContext(path, httpHandler);
					}
				}

				httpServer.setExecutor(Executors.newFixedThreadPool(12));
				httpServer.start();

				final boolean keepGoing = afterStartHook(serverPort);
				if (keepGoing) {

					Logger.printStatus(displayName + " server has started");
					ThreadUtils.trySleep(Long.MAX_VALUE);
					Logger.printStatus(displayName + " server has stopped");
				}
			}

		} catch (final Exception exc) {
			Logger.printError("failed to start " + displayName + " server");
			Logger.printException(exc);
		}
	}

	protected abstract boolean afterStartHook(
			int serverPort);
}
