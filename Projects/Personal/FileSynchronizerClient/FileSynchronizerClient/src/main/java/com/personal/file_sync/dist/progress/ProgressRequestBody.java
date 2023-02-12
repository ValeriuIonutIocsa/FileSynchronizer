package com.personal.file_sync.dist.progress;

import java.io.IOException;

import com.utils.io.progress.listeners.ProgressListener;
import com.utils.log.Logger;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;

public class ProgressRequestBody extends RequestBody {

	private final RequestBody requestBody;
	private final ProgressListener progressListener;

	private final long contentLength;

	public ProgressRequestBody(
			final RequestBody requestBody,
			final ProgressListener progressListener) {

		this.requestBody = requestBody;
		this.progressListener = progressListener;

		contentLength = computeContentLength(requestBody);
	}

	private long computeContentLength(
			final RequestBody requestBody) {

		long contentLength = -1;
		try {
			contentLength = requestBody.contentLength();
		} catch (final Exception exc) {
			Logger.printException(exc);
		}
		return contentLength;
	}

	@Override
	public MediaType contentType() {
		return requestBody.contentType();
	}

	@Override
	public long contentLength() {
		return contentLength;
	}

	@Override
	public void writeTo(
			final BufferedSink sink) throws IOException {

		final CountingSink countingSink = new CountingSink(sink, this);
		final BufferedSink bufferedSink = Okio.buffer(countingSink);
		requestBody.writeTo(bufferedSink);
		bufferedSink.flush();
	}

	ProgressListener getProgressListener() {
		return progressListener;
	}

	long getContentLength() {
		return contentLength;
	}
}
