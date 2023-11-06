/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.sse;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceDisconnectSseException;

/**
 * @author Björn Lange - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class SseStreamParserTest {
    @Mock
    @NonNullByDefault({})
    private Consumer<ServerSentEvent> serverSentEventCallback;

    @Mock
    @NonNullByDefault({})
    private Consumer<@Nullable Throwable> streamClosedCallback;

    private InputStream getInputStreamReadingUtf8Data(String data) {
        return new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void whenNoEventIsProvidedThenTheStreamClosedCallbackIsInvoked() {
        // given:
        InputStream inputStream = getInputStreamReadingUtf8Data("");

        SseStreamParser parser = new SseStreamParser(inputStream, serverSentEventCallback, streamClosedCallback);

        // when:
        parser.parseAndDispatchEvents();

        // then:
        verify(streamClosedCallback).accept(null);
        verifyNoMoreInteractions(streamClosedCallback);
        verifyNoInteractions(serverSentEventCallback);
    }

    @Test
    public void whenNoEventAndOnlyWhitespaceIsProvidedThenTheStreamClosedCallbackIsInvoked() {
        // given:
        InputStream inputStream = getInputStreamReadingUtf8Data("\r\n");

        SseStreamParser parser = new SseStreamParser(inputStream, serverSentEventCallback, streamClosedCallback);

        // when:
        parser.parseAndDispatchEvents();

        // then:
        verify(streamClosedCallback).accept(null);
        verifyNoMoreInteractions(streamClosedCallback);
        verifyNoInteractions(serverSentEventCallback);
    }

    @Test
    public void whenAnEventIsProvidedThenItIsPassedToTheCallback() {
        // given:
        InputStream inputStream = getInputStreamReadingUtf8Data("event: ping\r\ndata: pong\r\n");

        SseStreamParser parser = new SseStreamParser(inputStream, serverSentEventCallback, streamClosedCallback);

        // when:
        parser.parseAndDispatchEvents();

        // then:
        verify(streamClosedCallback).accept(null);
        verify(serverSentEventCallback).accept(new ServerSentEvent("ping", "pong"));
        verifyNoMoreInteractions(streamClosedCallback, serverSentEventCallback);
    }

    @Test
    public void whenALineWithInvalidKeyIsProvidedThenItIsIgnored() {
        // given:
        InputStream inputStream = getInputStreamReadingUtf8Data("name: ping\r\n");

        SseStreamParser parser = new SseStreamParser(inputStream, serverSentEventCallback, streamClosedCallback);

        // when:
        parser.parseAndDispatchEvents();

        // then:
        verify(streamClosedCallback).accept(null);
        verifyNoMoreInteractions(streamClosedCallback);
        verifyNoInteractions(serverSentEventCallback);
    }

    @Test
    public void whenDataWithoutEventIsProvidedThenItIsIgnored() {
        // given:
        InputStream inputStream = getInputStreamReadingUtf8Data("data: ping\r\n");

        SseStreamParser parser = new SseStreamParser(inputStream, serverSentEventCallback, streamClosedCallback);

        // when:
        parser.parseAndDispatchEvents();

        // then:
        verify(streamClosedCallback).accept(null);
        verifyNoMoreInteractions(streamClosedCallback);
        verifyNoInteractions(serverSentEventCallback);
    }

    @Test
    public void whenTheEventStreamBreaksThenTheStreamClosedCallbackIsNotifiedWithTheCause() throws IOException {
        // given:
        InputStream inputStream = mock(InputStream.class);
        TimeoutException timeoutException = new TimeoutException();
        when(inputStream.read(any(), anyInt(), anyInt())).thenThrow(new IOException(timeoutException));

        SseStreamParser parser = new SseStreamParser(inputStream, serverSentEventCallback, streamClosedCallback);

        // when:
        parser.parseAndDispatchEvents();

        // then:
        verify(streamClosedCallback).accept(timeoutException);
        verifyNoMoreInteractions(streamClosedCallback);
        verifyNoInteractions(serverSentEventCallback);
    }

    @Test
    public void whenTheEventStreamBreaksBecauseOfAnSseDisconnectThenTheStreamCloseCallbackIsNotNotifiedToPreventSseReconnect()
            throws IOException {
        // given:
        InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(), anyInt(), anyInt()))
                .thenThrow(new IOException(new MieleWebserviceDisconnectSseException()));

        SseStreamParser parser = new SseStreamParser(inputStream, serverSentEventCallback, streamClosedCallback);

        // when:
        parser.parseAndDispatchEvents();

        // then:
        verifyNoInteractions(streamClosedCallback, serverSentEventCallback);
    }

    @Test
    public void whenTheEventStreamBreaksAndTheResourceCleanupFailsThenItIsIgnored() throws IOException {
        // given:
        InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(), anyInt(), anyInt()))
                .thenThrow(new IOException(new MieleWebserviceDisconnectSseException()));
        doThrow(new IOException()).when(inputStream).close();

        SseStreamParser parser = new SseStreamParser(inputStream, serverSentEventCallback, streamClosedCallback);

        // when:
        parser.parseAndDispatchEvents();

        // then:
        verifyNoInteractions(streamClosedCallback, serverSentEventCallback);
    }
}
