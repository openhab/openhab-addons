/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mycroft.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mycroft.internal.api.dto.BaseMessage;
import org.openhab.binding.mycroft.internal.api.dto.MessageSpeak;

/**
 * This class provides tests for mycroft binding
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@NonNullByDefault
public class MycroftConnectionTest {

    private @Mock @NonNullByDefault({}) MycroftConnectionListener mycroftConnectionListener;
    private @Mock @NonNullByDefault({}) Session sessionMock;

    @Test
    public void testConnectionOK() throws IOException {

        MycroftConnection mycroftConnection = new MycroftConnection(mycroftConnectionListener, new WebSocketClient());
        Mockito.when(sessionMock.getRemoteAddress()).thenReturn(new InetSocketAddress(1234));
        mycroftConnection.onConnect(sessionMock);

        Mockito.verify(mycroftConnectionListener, Mockito.times(1)).connectionEstablished();
    }

    @Test
    public void testAnyListener() throws UnsupportedEncodingException, IOException {
        MycroftConnection mycroftConnection = new MycroftConnection(mycroftConnectionListener, new WebSocketClient());

        Mockito.when(sessionMock.getRemoteAddress()).thenReturn(new InetSocketAddress(1234));
        mycroftConnection.onConnect(sessionMock);

        @SuppressWarnings("unchecked")
        MycroftMessageListener<MessageSpeak> mockListener = Mockito.mock(MycroftMessageListener.class);
        ArgumentCaptor<BaseMessage> argCaptorMessage = ArgumentCaptor.forClass(BaseMessage.class);

        // given we register any listener
        mycroftConnection.registerListener(MessageType.any, mockListener);

        // when we send speak message
        @SuppressWarnings("null")
        String speakMessageJson = new String(
                MycroftConnectionTest.class.getResourceAsStream("speak.json").readAllBytes(), "UTF-8");
        mycroftConnection.onMessage(sessionMock, speakMessageJson);

        // then message is correctly received by listener
        Mockito.verify(mockListener, Mockito.times(1)).baseMessageReceived(ArgumentMatchers.any());
        Mockito.verify(mockListener).baseMessageReceived(argCaptorMessage.capture());

        assertEquals(argCaptorMessage.getValue().message, speakMessageJson);
    }

    @Test
    public void testSpeakListener() throws IOException {

        MycroftConnection mycroftConnection = new MycroftConnection(mycroftConnectionListener, new WebSocketClient());

        Mockito.when(sessionMock.getRemoteAddress()).thenReturn(new InetSocketAddress(1234));
        mycroftConnection.onConnect(sessionMock);

        @SuppressWarnings("unchecked")
        MycroftMessageListener<MessageSpeak> mockListener = Mockito.mock(MycroftMessageListener.class);
        ArgumentCaptor<MessageSpeak> argCaptorMessage = ArgumentCaptor.forClass(MessageSpeak.class);

        // given we register speak listener
        mycroftConnection.registerListener(MessageType.speak, mockListener);

        // when we send speak message
        @SuppressWarnings("null")
        String speakMessageJson = new String(
                MycroftConnectionTest.class.getResourceAsStream("speak.json").readAllBytes(), "UTF-8");
        mycroftConnection.onMessage(sessionMock, speakMessageJson);

        // then message is correctly received by listener
        Mockito.verify(mockListener).baseMessageReceived(argCaptorMessage.capture());

        assertEquals(argCaptorMessage.getValue().data.utterance, "coucou");
    }
}
