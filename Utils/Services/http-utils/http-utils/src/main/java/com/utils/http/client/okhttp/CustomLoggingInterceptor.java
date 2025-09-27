package com.utils.http.client.okhttp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;
import com.utils.log.Logger;
import com.utils.string.StrUtils;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

class CustomLoggingInterceptor implements Interceptor {

	private final boolean formatJson;

	CustomLoggingInterceptor(
			final boolean formatJson) {

		this.formatJson = formatJson;
	}

	@NotNull
	@Override
	public Response intercept(
			@NotNull final Chain chain) throws IOException {

		return interceptChain(chain);
	}

	private Response interceptChain(
			final Chain chain) throws IOException {

		final Response response;
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (PrintStream printStream = new PrintStream(
				byteArrayOutputStream, false, Charset.defaultCharset())) {

			response = writeRequestChain(chain, printStream);
		}
		final String str = byteArrayOutputStream.toString(Charset.defaultCharset());
		Logger.printLine(str);
		return response;
	}

	private Response writeRequestChain(
			final Chain chain,
			final PrintStream printStream) throws IOException {

		final Request request = chain.request();

		final HttpUrl requestUrl = request.url();
		final String requestUrlString = requestUrl.toString();
		printStream.print("sending HTTP request " + requestUrlString);
		printStream.println();

		writeRequestParameters(requestUrl, printStream);

		final Headers requestHeaders = request.headers();
		writeRequestHeaders(requestHeaders, printStream);

		writeRequestBody(request, printStream);

		Response response = chain.proceed(request);

		final Request responseRequest = response.request();
		final HttpUrl responseRequestUrl = responseRequest.url();
		final String responseRequestUrlString = responseRequestUrl.toString();

		final long durationMs = response.receivedResponseAtMillis() - response.sentRequestAtMillis();
		final String durationString = StrUtils.timeMsToString(durationMs);

		printStream.print("received HTTP response for " + responseRequestUrlString + " in " + durationString);
		printStream.println();

		final Headers responseHeaders = response.headers();
		writeResponseHeaders(responseHeaders, printStream);

		final ResponseBody responseBody = response.body();
		final byte[] responseBodyByteArray = responseBody.bytes();
		final MediaType responseBodyContentType = responseBody.contentType();

		writeResponseBody(responseBodyByteArray, printStream);

		final ResponseBody newResponseBody =
				ResponseBody.create(responseBodyByteArray, responseBodyContentType);
		response = response.newBuilder().body(newResponseBody).build();

		return response;
	}

	private static void writeRequestParameters(
			final HttpUrl requestUrl,
			final PrintStream printStream) {

		final Set<String> queryParameterNameSet = requestUrl.queryParameterNames();
		if (!queryParameterNameSet.isEmpty()) {

			printStream.print("HTTP REQUEST PARAMETERS BEGIN");
			printStream.println();
			for (final String queryParameterName : queryParameterNameSet) {

				final String queryParameterValue = requestUrl.queryParameter(queryParameterName);
				printStream.print(queryParameterName + " = " + queryParameterValue);
				printStream.println();
			}
			printStream.print("HTTP REQUEST PARAMETERS END");

		} else {
			printStream.print("HTTP REQUEST PARAMETERS EMPTY");
		}
		printStream.println();
	}

	private static void writeRequestHeaders(
			final Headers requestHeaders,
			final PrintStream printStream) {

		String requestHeadersString = requestHeaders.toString();
		requestHeadersString = requestHeadersString.trim();

		if (StringUtils.isNotBlank(requestHeadersString)) {

			printStream.print("HTTP REQUEST HEADERS BEGIN");
			printStream.println();
			printStream.print(requestHeadersString);
			printStream.println();
			printStream.print("HTTP REQUEST HEADERS END");

		} else {
			printStream.print("HTTP REQUEST HEADERS EMPTY");
		}
		printStream.println();
	}

	private void writeRequestBody(
			final Request request,
			final PrintStream printStream) {

		final String requestBodyString = createRequestBodyString(request);
		if (StringUtils.isNotBlank(requestBodyString)) {

			printStream.print("HTTP REQUEST BODY BEGIN");
			printStream.println();
			printStream.print(requestBodyString);
			printStream.println();
			printStream.print("HTTP REQUEST BODY END");

		} else {
			printStream.print("HTTP REQUEST BODY EMPTY");
		}
		printStream.println();
	}

	private String createRequestBodyString(
			final Request request) {

		String requestBodyString = "";
		try {
			final Request copyRequest = request.newBuilder().build();
			final RequestBody requestBody = copyRequest.body();
			if (requestBody != null) {

				final Buffer buffer = new Buffer();
				requestBody.writeTo(buffer);
				final String tmpRequestBodyString = buffer.readUtf8();
				if (formatJson) {
					requestBodyString = formatJsonString(tmpRequestBodyString);
				} else {
					requestBodyString = tmpRequestBodyString;
				}
			}

		} catch (final Throwable throwable) {
			Logger.printThrowable(throwable);
		}
		return requestBodyString;
	}

	private static String formatJsonString(
			final String jsonString) {

		String formattedJsonString = jsonString;
		try {
			if (StringUtils.isNotBlank(jsonString)) {

				try (Buffer buffer = new Buffer()) {

					buffer.writeUtf8(jsonString);
					final JsonReader jsonReader = JsonReader.of(buffer);
					final Object value = jsonReader.readJsonValue();
					final JsonAdapter<Object> adapter =
							new Moshi.Builder().build().adapter(Object.class).indent("    ");
					formattedJsonString = adapter.toJson(value);
				}
			}

		} catch (final Throwable throwable) {
			Logger.printThrowable(throwable);
		}
		return formattedJsonString;
	}

	private static void writeResponseHeaders(
			final Headers responseHeaders,
			final PrintStream printStream) {

		String responseHeadersString = responseHeaders.toString();
		responseHeadersString = responseHeadersString.trim();

		if (StringUtils.isNotBlank(responseHeadersString)) {

			printStream.print("HTTP RESPONSE HEADERS BEGIN");
			printStream.println();
			printStream.print(responseHeadersString);
			printStream.println();
			printStream.print("HTTP RESPONSE HEADERS END");

		} else {
			printStream.print("HTTP RESPONSE HEADERS EMPTY");
		}
		printStream.println();
	}

	private void writeResponseBody(
			final byte[] responseBodyByteArray,
			final PrintStream printStream) {

		String responseBodyString = new String(responseBodyByteArray, StandardCharsets.UTF_8);
		if (formatJson) {
			responseBodyString = formatJsonString(responseBodyString);
		}

		if (StringUtils.isNotBlank(responseBodyString)) {

			printStream.print("HTTP RESPONSE BODY BEGIN");
			printStream.println();
			printStream.print(responseBodyString);
			printStream.println();
			printStream.print("HTTP RESPONSE BODY END");

		} else {
			printStream.print("HTTP RESPONSE BODY EMPTY");
		}
	}
}
