package com.personal.file_sync.dist.progress;

import java.io.IOException;

import com.utils.io.progress.listeners.ProgressListener;

import okio.Buffer;
import okio.ForwardingSink;
import okio.Sink;

class CountingSink extends ForwardingSink {

	private final ProgressRequestBody progressRequestBody;

	private long bytesWritten;

	CountingSink(
			final Sink delegate,
			final ProgressRequestBody progressRequestBody) {

		super(delegate);

		this.progressRequestBody = progressRequestBody;
	}

	@Override
	public void write(
			final Buffer source,
			final long byteCount) throws IOException {

		super.write(source, byteCount);

		bytesWritten += byteCount;

		final ProgressListener progressListener = progressRequestBody.getProgressListener();
		final long contentLength = progressRequestBody.getContentLength();
		progressListener.transferred(bytesWritten, contentLength);
	}
}
