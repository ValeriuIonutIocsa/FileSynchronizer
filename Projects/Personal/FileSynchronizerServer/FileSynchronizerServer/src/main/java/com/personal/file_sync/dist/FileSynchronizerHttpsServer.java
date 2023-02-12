package com.personal.file_sync.dist;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import com.personal.file_sync.settings.FileSynchronizerServerSettings;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import com.utils.log.Logger;
import com.utils.net.ssl.CustomSslContext;

public class FileSynchronizerHttpsServer {

	private final FileSynchronizerServerSettings fileSynchronizerServerSettings;

	public FileSynchronizerHttpsServer(
			final FileSynchronizerServerSettings fileSynchronizerServerSettings) {

		this.fileSynchronizerServerSettings = fileSynchronizerServerSettings;
	}

	public void start() {

		try {
			Logger.printProgress("starting FileSynchronizer server");
			final String ipAddr = fileSynchronizerServerSettings.getIpAddr();
			Logger.printLine("IP address: " + ipAddr);
			final int port = fileSynchronizerServerSettings.getPort();
			Logger.printLine("port: " + port);
			final int backlog = fileSynchronizerServerSettings.getBacklog();
			Logger.printLine("backlog: " + backlog);
			final int threadCount = fileSynchronizerServerSettings.getThreadCount();
			Logger.printLine("threadCount: " + threadCount);

			final InetSocketAddress inetSocketAddress = new InetSocketAddress(ipAddr, port);
			final HttpsServer httpsServer = HttpsServer.create(inetSocketAddress, backlog);

			final CustomSslContext customSslContext = FileSynchronizerUtils.createSslContext(
					"ssl/server.keystore", "ssl/server.truststore");
			final SSLContext sslContext = customSslContext.getSslContext();
			final HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext);
			httpsServer.setHttpsConfigurator(httpsConfigurator);

			final HttpHandler httpHandlerUpload = new HttpHandlerUpload(fileSynchronizerServerSettings);
			httpsServer.createContext("/upload", httpHandlerUpload);

			final HttpHandler httpHandlerDownload = new HttpHandlerDownload(fileSynchronizerServerSettings);
			httpsServer.createContext("/download", httpHandlerDownload);

			final ExecutorService executorService;
			if (threadCount == 0) {
				executorService = Executors.newCachedThreadPool();
			} else {
				executorService = Executors.newFixedThreadPool(threadCount);
			}
			httpsServer.setExecutor(executorService);

			httpsServer.start();

		} catch (final Exception exc) {
			Logger.printError("failed to start HTTPS server");
			Logger.printException(exc);
		}
	}
}
