/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.http.internal.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link HttpResponseListenerTest}.
 *
 * @author Corubba Smith - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class HttpResponseListenerTest {

    private Request request = mock(Request.class);
    private Response response = mock(Response.class);

    // ******** Common methods ******** //

    /**
     * Run the given listener with the given result.
     */
    private void run(HttpResponseListener listener, Result result) {
        listener.onComplete(result);
    }

    /**
     * Return a default Result using the request- and response-mocks and no failure.
     */
    private Result createResult() {
        return new Result(request, response);
    }

    /**
     * Run the given listener with a default result.
     */
    private void run(HttpResponseListener listener) {
        run(listener, createResult());
    }

    /**
     * Set the given payload as body of the response in the buffer of the given listener.
     */
    private void setPayload(HttpResponseListener listener, byte[] payload) {
        listener.onContent(null, ByteBuffer.wrap(payload));
    }

    /**
     * Run a default listener with the given result and the given payload.
     */
    private CompletableFuture<@Nullable Content> run(Result result, byte @Nullable [] payload) {
        CompletableFuture<@Nullable Content> future = new CompletableFuture<>();
        HttpResponseListener listener = new HttpResponseListener(future, null, 1024 * 1024);
        if (null != payload) {
            setPayload(listener, payload);
        }
        run(listener, result);
        return future;
    }

    /**
     * Run a default listener with the given result.
     */
    private CompletableFuture<@Nullable Content> run(Result result) {
        return run(result, null);
    }

    /**
     * Run a default listener with a default result and the given payload.
     */
    private CompletableFuture<@Nullable Content> run(byte @Nullable [] payload) {
        return run(createResult(), payload);
    }

    /**
     * Run a default listener with a default result.
     */
    private CompletableFuture<@Nullable Content> run() {
        return run(createResult());
    }

    @BeforeEach
    void init() {
        // required for the request trace
        when(response.getHeaders()).thenReturn(new HttpFields());
    }

    // ******** Tests ******** //

    /**
     * When an exception is thrown during the request phase, the future completes unexceptionally
     * with no value.
     */
    @Test
    public void requestException() {
        RuntimeException requestFailure = new RuntimeException("The request failed!");
        Result result = new Result(request, requestFailure, response);

        CompletableFuture<@Nullable Content> future = run(result);

        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertNull(future.join());
    }

    /**
     * When an exception is thrown during the response phase, the future completes unexceptionally
     * with no value.
     */
    @Test
    public void responseException() {
        RuntimeException responseFailure = new RuntimeException("The response failed!");
        Result result = new Result(request, response, responseFailure);

        CompletableFuture<@Nullable Content> future = run(result);

        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertNull(future.join());
    }

    /**
     * When the remote side does not send any payload, the future completes normally and contains a
     * empty Content.
     */
    @Test
    public void okWithNoBody() {
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);

        CompletableFuture<@Nullable Content> future = run();

        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());

        Content content = future.join();
        assertNotNull(content);
        assertNotNull(content.getRawContent());
        assertEquals(0, content.getRawContent().length);
        assertNull(content.getMediaType());
    }

    /**
     * When the remote side sends a payload, the future completes normally and contains a Content
     * object with the payload.
     */
    @Test
    public void okWithBody() {
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);

        final String textPayload = "foobar";
        CompletableFuture<@Nullable Content> future = run(textPayload.getBytes());

        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());

        Content content = future.join();
        assertNotNull(content);
        assertNotNull(content.getRawContent());
        assertEquals(textPayload, new String(content.getRawContent()));
        assertNull(content.getMediaType());
    }

    /**
     * When the remote side sends a payload and encoding header, the future completes normally
     * and contains a Content object with the payload. The payload gets decoded using the encoding
     * the remote sent.
     */
    @Test
    public void okWithEncodedBody() throws UnsupportedEncodingException {
        final String encodingName = "UTF-16LE";
        final String fallbackEncodingName = "UTF-8";

        CompletableFuture<@Nullable Content> future = new CompletableFuture<>();
        HttpResponseListener listener = new HttpResponseListener(future, fallbackEncodingName, 1024 * 1024);

        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/plain; charset=" + encodingName);
        when(response.getRequest()).thenReturn(request);
        listener.onHeaders(response);

        final String textPayload = "漢字編碼方法";
        setPayload(listener, textPayload.getBytes(encodingName));

        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        run(listener);

        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());

        Content content = future.join();
        assertNotNull(content);
        assertNotNull(content.getRawContent());
        assertEquals(textPayload, new String(content.getRawContent(), encodingName));
        assertEquals(textPayload, content.getAsString());
        assertEquals("text/plain", content.getMediaType());
    }

    /**
     * When the remote side sends a payload but no encoding, the future completes normally and
     * contains a Content object with the payload. The payload gets decoded using the fallback
     * encoding of the listener.
     */
    @Test
    public void okWithEncodedBodyFallback() throws UnsupportedEncodingException {
        final String encodingName = "UTF-16BE";

        CompletableFuture<@Nullable Content> future = new CompletableFuture<>();
        HttpResponseListener listener = new HttpResponseListener(future, encodingName, 1024 * 1024);

        final String textPayload = "汉字编码方法";
        setPayload(listener, textPayload.getBytes(encodingName));

        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        run(listener);

        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());

        Content content = future.join();
        assertNotNull(content);
        assertNotNull(content.getRawContent());
        assertEquals(textPayload, new String(content.getRawContent(), encodingName));
        assertEquals(textPayload, content.getAsString());
        assertNull(content.getMediaType());
    }

    /**
     * When the remote side response with a HTTP/204 and no payload, the future completes normally
     * and contains an empty Content.
     */
    @Test
    public void nocontent() {
        when(response.getStatus()).thenReturn(HttpStatus.NO_CONTENT_204);

        CompletableFuture<@Nullable Content> future = run();

        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());

        Content content = future.join();
        assertNotNull(content);
        assertNotNull(content.getRawContent());
        assertEquals(0, content.getRawContent().length);
        assertNull(content.getMediaType());
    }

    /**
     * When the remote side response with a HTTP/401, the future completes exceptionally with a
     * HttpAuthException.
     */
    @Test
    public void unauthorized() {
        when(response.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED_401);

        CompletableFuture<@Nullable Content> future = run();

        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());

        @Nullable
        CompletionException exceptionWrapper = assertThrows(CompletionException.class, () -> future.join());
        assertNotNull(exceptionWrapper);

        Throwable exception = exceptionWrapper.getCause();
        assertNotNull(exception);
        assertTrue(exception instanceof HttpAuthException);
    }

    /**
     * When the remote side responds with anything we don't expect (in this case a HTTP/500), the
     * future completes exceptionally with an IllegalStateException.
     */
    @Test
    public void unexpectedStatus() {
        when(response.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR_500);

        CompletableFuture<@Nullable Content> future = run();

        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());

        @Nullable
        CompletionException exceptionWrapper = assertThrows(CompletionException.class, () -> future.join());
        assertNotNull(exceptionWrapper);

        Throwable exception = exceptionWrapper.getCause();
        assertNotNull(exception);
        assertTrue(exception instanceof IllegalStateException);
        assertEquals("Response - Code500", exception.getMessage());
    }
}
