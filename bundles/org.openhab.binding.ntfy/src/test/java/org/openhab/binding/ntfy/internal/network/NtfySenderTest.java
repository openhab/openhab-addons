/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ntfy.internal.network;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.ntfy.internal.NtfyConnectionConfiguration;
import org.openhab.binding.ntfy.internal.NtfyConnectionHandler;
import org.openhab.binding.ntfy.internal.models.MessageEvent;

/**
 * Unit tests for {@link NtfySender}.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class NtfySenderTest {

    private @Mock @Nullable Request request;
    private @Mock @Nullable HttpClient httpClient;
    private @Mock @Nullable ContentResponse response;
    private @Mock @Nullable NtfyConnectionHandler handler;

    private static void setConfiguration(NtfySender sender, NtfyConnectionConfiguration cfg) throws Exception {
        Field f = NtfySender.class.getDeclaredField("configuration");
        f.setAccessible(true);
        f.set(sender, cfg);
    }

    @BeforeEach
    public void setupMocks() {
        request = mock(Request.class);
        httpClient = mock(HttpClient.class);
        response = mock(ContentResponse.class);
        handler = mock(NtfyConnectionHandler.class);
    }

    /**
     * Verifies that a successful HTTP POST returns a deserialized MessageEvent
     * with the expected fields.
     */
    @Test
    public void sendMessageSuccessReturnsDeserializedMessageEvent() throws Exception {
        final HttpClient httpClient = java.util.Objects.requireNonNull(this.httpClient);
        final Request request = java.util.Objects.requireNonNull(this.request);
        final ContentResponse response = java.util.Objects.requireNonNull(this.response);

        when(httpClient.newRequest(any(URI.class))).thenReturn(request);
        when(request.method(HttpMethod.POST)).thenReturn(request);
        when(request.content(any())).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        when(request.send()).thenReturn(response);

        String json = "{\"sequence_id\":\"seq-1\",\"message\":\"hello\",\"priority\":5,\"event\":\"message\"}";
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(json);

        final NtfyConnectionHandler handler = java.util.Objects.requireNonNull(this.handler);

        NtfySender sender = new NtfySender("topic", httpClient, handler);

        NtfyConnectionConfiguration cfg = new NtfyConnectionConfiguration();
        cfg.hostname = "http://example.org";
        setConfiguration(sender, cfg);

        NtfyMessage m = new NtfyMessage();
        m.setMessage("hello");

        @Nullable
        MessageEvent result = sender.sendMessage(m);

        assertNotNull(result, "Expected non-null MessageEvent on success");
        assertEquals("seq-1", result.getSequenceId());
        assertEquals("hello", result.getMessage());
        assertEquals(5, result.getPriority());
    }

    /**
     * Verifies that a successful HTTP DELETE request results in a true return
     * value from deleteMessage.
     */
    @Test
    public void deleteMessageSuccessReturnsTrue() throws Exception {
        final HttpClient httpClient = java.util.Objects.requireNonNull(this.httpClient);
        final Request request = java.util.Objects.requireNonNull(this.request);
        final ContentResponse response = java.util.Objects.requireNonNull(this.response);

        when(httpClient.newRequest(any(URI.class))).thenReturn(request);
        when(request.method(HttpMethod.DELETE)).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);

        final NtfyConnectionHandler handler = java.util.Objects.requireNonNull(this.handler);
        NtfySender sender = new NtfySender("topic", httpClient, handler);

        NtfyConnectionConfiguration cfg = new NtfyConnectionConfiguration();
        cfg.hostname = "http://example.org";
        setConfiguration(sender, cfg);

        assertTrue(sender.deleteMessage("seq-123"), "Expected deleteMessage to return true for 2xx status");
    }

    /**
     * Verifies that when the POST send() throws an exception, sendMessage
     * returns null and the bridge handler's connectionError is invoked.
     */
    @Test
    public void sendMessageSendThrowsCallsConnectionError() throws Exception {
        final HttpClient httpClient = java.util.Objects.requireNonNull(this.httpClient);
        final Request request = java.util.Objects.requireNonNull(this.request);

        when(httpClient.newRequest(any(URI.class))).thenReturn(request);
        when(request.method(HttpMethod.POST)).thenReturn(request);
        when(request.content(any())).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        when(request.send()).thenThrow(new ExecutionException(new RuntimeException("boom")));

        final NtfyConnectionHandler handler = java.util.Objects.requireNonNull(this.handler);
        NtfySender sender = new NtfySender("topic", httpClient, handler);

        NtfyConnectionConfiguration cfg = new NtfyConnectionConfiguration();
        cfg.hostname = "http://example.org";
        setConfiguration(sender, cfg);

        NtfyMessage m = new NtfyMessage();
        m.setMessage("x");

        @Nullable
        MessageEvent result = sender.sendMessage(m);

        assertNull(result, "Expected null result when send throws");
        verify(handler).connectionError(any());
    }

    /**
     * Verifies that uploading a local file via sendFile results in a deserialized
     * MessageEvent when the server responds with a success status.
     */
    @Test
    public void sendFileUploadsFileAndReturnsMessageEvent() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        Request request = mock(Request.class);
        ContentResponse response = mock(ContentResponse.class);

        when(httpClient.newRequest(any(URI.class))).thenReturn(request);
        when(request.method(HttpMethod.PUT)).thenReturn(request);
        when(request.content(any())).thenReturn(request);
        when(request.send()).thenReturn(response);

        String json = "{\"sequence_id\":\"seq-file-1\",\"message\":\"uploaded\",\"priority\":4,\"event\":\"message\"}";
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(json);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);

        NtfyConnectionHandler handler = mock(NtfyConnectionHandler.class);

        NtfySender sender = new NtfySender("topic", httpClient, handler);

        NtfyConnectionConfiguration cfg = new NtfyConnectionConfiguration();
        cfg.hostname = "http://example.org";
        setConfiguration(sender, cfg);

        Path tmp = Files.createTempFile("ntfy-test-", ".bin");
        try {
            Files.writeString(tmp, "file-contents", StandardCharsets.UTF_8);

            MessageEvent result = sender.sendFile(tmp.toString(), "file.bin", "seq-file-1");

            assertNotNull(result, "Expected non-null MessageEvent on successful file upload");
            assertEquals("seq-file-1", result.getSequenceId());
            assertEquals("uploaded", result.getMessage());
            assertEquals(4, result.getPriority());
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
