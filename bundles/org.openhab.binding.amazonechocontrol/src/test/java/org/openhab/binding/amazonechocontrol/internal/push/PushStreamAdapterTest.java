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
package org.openhab.binding.amazonechocontrol.internal.push;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.amazonechocontrol.internal.push.PushStreamAdapter.spannedFrames;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.http2.frames.PingFrame;
import org.eclipse.jetty.util.Callback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushMessageTO;
import org.openhab.binding.amazonechocontrol.internal.util.NonNullListTypeAdapterFactory;
import org.openhab.binding.amazonechocontrol.internal.util.SerializeNullTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link PushStreamAdapterTest} contains tests for re-assembling push messages from HTTP/2 DATA frames
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class PushStreamAdapterTest {
    private static final String BOUNDARY = "------abcde13";
    private static final String CONTENT_TYPE = "multipart/related; boundary=" + BOUNDARY + "; type=application/json";
    private static final String MESSAGE_JSON = buildMessageJson();
    private static final String MESSAGE_PART = "Content-Type: application/json\r\n\r\n" + MESSAGE_JSON + "\r\n"
            + BOUNDARY + "\r\n";

    private final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new NonNullListTypeAdapterFactory())
            .registerTypeAdapterFactory(new SerializeNullTypeAdapterFactory()).create();
    private final Session session = mock(Session.class);
    private final Stream stream = mock(Stream.class);
    private final List<PushMessageTO.RenderingUpdateTO> receivedUpdates = new ArrayList<>();
    private final AtomicInteger succeededCallbacks = new AtomicInteger();

    private PushStreamAdapter adapter = new PushStreamAdapter(gson, session, receivedUpdates::add);

    @BeforeEach
    public void setUp() {
        adapter = new PushStreamAdapter(gson, session, receivedUpdates::add);
        adapter.onHeaders(stream, headersFrame(CONTENT_TYPE));
    }

    @Test
    public void completeMessageInSingleFrameIsProcessed() {
        sendData(MESSAGE_PART);

        assertThat(receivedUpdates, hasSize(1));
        assertThat(receivedUpdates.get(0).route, is("DeeAppMessage"));
        assertThat(succeededCallbacks.get(), is(1));
    }

    @Test
    public void messageSplitAcrossFramesIsReassembled() {
        // the reported failure: the first frame ends inside the resourceMetadata string
        int cut = MESSAGE_PART.indexOf("volumeSetting");
        sendData(MESSAGE_PART.substring(0, cut));
        assertThat(receivedUpdates, hasSize(0));

        sendData(MESSAGE_PART.substring(cut));
        assertThat(receivedUpdates, hasSize(1));
        assertThat(succeededCallbacks.get(), is(2));
    }

    @Test
    public void messageSplitInsideMultiByteCharacterIsReassembled() {
        String part = MESSAGE_PART.replace("\"route\":\"DeeAppMessage\"", "\"route\":\"Küchen-Echo\"");
        byte[] bytes = part.getBytes(StandardCharsets.UTF_8);
        // cut between the two UTF-8 bytes of the 'ü'
        int cut = indexOf(bytes, "K".getBytes(StandardCharsets.UTF_8)) + 2;

        sendData(slice(bytes, 0, cut));
        sendData(slice(bytes, cut, bytes.length));

        assertThat(receivedUpdates, hasSize(1));
        assertThat(receivedUpdates.get(0).route, is("Küchen-Echo"));
    }

    @Test
    public void boundarySplitAcrossFramesIsReassembled() {
        int cut = MESSAGE_PART.lastIndexOf(BOUNDARY) + 4;
        sendData(MESSAGE_PART.substring(0, cut));
        assertThat(receivedUpdates, hasSize(0));

        sendData(MESSAGE_PART.substring(cut));
        assertThat(receivedUpdates, hasSize(1));
    }

    @Test
    public void multipleMessagesInOneFrameAreAllProcessed() {
        sendData(MESSAGE_PART + MESSAGE_PART);

        assertThat(receivedUpdates, hasSize(2));
    }

    @Test
    public void bareBoundaryTriggersPing() {
        sendData(BOUNDARY + "\r\n");

        ArgumentCaptor<PingFrame> pingCaptor = ArgumentCaptor.forClass(PingFrame.class);
        verify(session, times(1)).ping(pingCaptor.capture(), any(Callback.class));
        assertThat(pingCaptor.getValue().isReply(), is(false));
        assertThat(receivedUpdates, hasSize(0));
    }

    @Test
    public void consumedPartsAreNotDeliveredAgain() {
        sendData(MESSAGE_PART);
        sendData(BOUNDARY + "\r\n");
        sendData(MESSAGE_PART);

        assertThat(receivedUpdates, hasSize(2));
        verify(session, times(1)).ping(any(PingFrame.class), any(Callback.class));
    }

    @Test
    public void unprocessablePartDoesNotWedgeTheStream() {
        // the explicit null overwrites the field initializer and the dispatch throws
        sendData("Content-Type: application/json\r\n\r\n{\"directive\":null}\r\n" + BOUNDARY + "\r\n");
        sendData(MESSAGE_PART);

        assertThat(receivedUpdates, hasSize(1));
        assertThat(succeededCallbacks.get(), is(2));
    }

    @Test
    public void rfc2046DelimiterWithDashPrefixIsAccepted() {
        String delimiter = "--" + BOUNDARY;
        sendData("Content-Type: application/json\r\n\r\n" + MESSAGE_JSON + "\r\n" + delimiter + "\r\n");
        sendData(delimiter + "\r\n");

        assertThat(receivedUpdates, hasSize(1));
        verify(session, times(1)).ping(any(PingFrame.class), any(Callback.class));
    }

    @Test
    public void jsonSpreadOverSeveralLinesIsJoined() {
        sendData(MESSAGE_PART.replace("\"renderingUpdates\":", "\"renderingUpdates\":\r\n"));

        assertThat(receivedUpdates, hasSize(1));
    }

    @Test
    public void nonJsonPartWithParseableBodyIsIgnored() {
        sendData("Content-Type: text/plain\r\n\r\n" + MESSAGE_JSON + "\r\n" + BOUNDARY + "\r\n");

        assertThat(receivedUpdates, hasSize(0));
    }

    @Test
    public void messageReassemblesAtEverySplitPosition() {
        byte[] bytes = MESSAGE_PART.getBytes(StandardCharsets.UTF_8);
        for (int cut = 1; cut < bytes.length; cut++) {
            receivedUpdates.clear();
            adapter = new PushStreamAdapter(gson, session, receivedUpdates::add);
            adapter.onHeaders(stream, headersFrame(CONTENT_TYPE));
            sendData(slice(bytes, 0, cut));
            sendData(slice(bytes, cut, bytes.length));
            assertThat("split at byte " + cut, receivedUpdates, hasSize(1));
        }
    }

    @Test
    public void keepAliveAndMessageInOneFrameAreBothProcessed() {
        sendData(BOUNDARY + "\r\n" + MESSAGE_PART);

        verify(session, times(1)).ping(any(PingFrame.class), any(Callback.class));
        assertThat(receivedUpdates, hasSize(1));
    }

    @Test
    public void dataWithoutBoundaryHeaderIsDiscarded() {
        adapter = new PushStreamAdapter(gson, session, receivedUpdates::add);
        sendData(MESSAGE_PART);

        assertThat(receivedUpdates, hasSize(0));
        assertThat(succeededCallbacks.get(), is(1));
    }

    @Test
    public void boundaryAsLastContentTypeParameterIsAccepted() {
        adapter = new PushStreamAdapter(gson, session, receivedUpdates::add);
        adapter.onHeaders(stream, headersFrame("multipart/related; type=application/json; boundary=" + BOUNDARY));
        sendData(MESSAGE_PART);

        assertThat(receivedUpdates, hasSize(1));
    }

    @Test
    public void malformedJsonInCompletePartDoesNotBreakFollowingMessages() {
        sendData("Content-Type: application/json\r\n\r\n{\"directive\":{\"header\":[]}}\r\n" + BOUNDARY + "\r\n");
        sendData(MESSAGE_PART);

        assertThat(receivedUpdates, hasSize(1));
    }

    @Test
    public void unknownPartTypeIsIgnored() {
        sendData("Content-Type: text/plain\r\n\r\nsomething\r\n" + BOUNDARY + "\r\n");

        assertThat(receivedUpdates, hasSize(0));
        verify(session, never()).ping(any(PingFrame.class), any(Callback.class));
    }

    @Test
    public void overlongDataWithoutBoundaryIsDiscarded() {
        byte[] filler = new byte[PushStreamAdapter.MAX_BUFFER_SIZE + 1];
        sendData(filler);
        sendData(MESSAGE_PART.getBytes(StandardCharsets.UTF_8));

        // without the discard the part would start with half a megabyte of filler and not parse
        assertThat(receivedUpdates, hasSize(1));
    }

    private void sendData(String content) {
        sendData(content.getBytes(StandardCharsets.UTF_8));
    }

    private void sendData(byte[] content) {
        adapter.onData(stream, new DataFrame(1, ByteBuffer.wrap(content), false), new Callback() {
            @Override
            public void succeeded() {
                succeededCallbacks.incrementAndGet();
            }
        });
    }

    private HeadersFrame headersFrame(String contentType) {
        HttpFields fields = new HttpFields();
        fields.put(HttpHeader.CONTENT_TYPE, contentType);
        return new HeadersFrame(new MetaData(HttpVersion.HTTP_2, fields), null, false);
    }

    // reconstructed from the log in openhab/openhab-addons#21426 with all ids replaced; the doubly nested
    // escaping (a JSON document inside resourceMetadata, another one inside its payload) is built with Gson
    // because exactly this long escaped string is where the reported frame split happened
    private static String buildMessageJson() {
        String innerPayload = "{\"destinationUserId\":\"A1PY8QQU9P0FJP\",\"dsn\":\"G000AA0000000000\","
                + "\"volumeSetting\":48000}";
        String resourceMetadata = "{\"command\":\"PUSH_VOLUME_CHANGE\",\"payload\":" + new Gson().toJson(innerPayload)
                + ",\"error\":false,\"errorMessage\":null,"
                + "\"mediaReferenceId\":\"aca2df8c-c1b8-4a3a-904b-7f4842cefecb:1\"}";
        return "{\"directive\":{\"header\":{\"namespace\":\"DeeAppMessagingGateway\",\"name\":\"ProcessNotification\","
                + "\"messageId\":\"11111111-2222-3333-4444-555555555555\"},\"payload\":{\"renderingUpdates\":"
                + "[{\"route\":\"DeeAppMessage\",\"resourceId\":\"resource-id\",\"resourceMetadata\":"
                + new Gson().toJson(resourceMetadata) + "}]}}}";
    }

    @Test
    public void aPartUsingBytesFromAnEarlierFrameCountsAsSpanningFrames() {
        assertThat(spannedFrames(true, 42), is(true));
    }

    @Test
    public void aPartAfterTheFirstOneOfAFrameNeverCountsAsSpanningFrames() {
        assertThat(spannedFrames(false, 42), is(false));
    }

    @Test
    public void aPartFromAnEmptyBufferDoesNotCountAsSpanningFrames() {
        assertThat(spannedFrames(true, 0), is(false));
    }

    private static byte[] slice(byte[] data, int from, int to) {
        byte[] result = new byte[to - from];
        System.arraycopy(data, from, result, 0, result.length);
        return result;
    }

    private static int indexOf(byte[] data, byte[] pattern) {
        for (int i = 0; i <= data.length - pattern.length; i++) {
            int j = 0;
            while (j < pattern.length && data[i + j] == pattern[j]) {
                j++;
            }
            if (j == pattern.length) {
                return i;
            }
        }
        return -1;
    }
}
