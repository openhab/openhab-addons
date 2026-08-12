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
package org.openhab.binding.melcloud.internal.home.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MelCloudHomeWebSocketListener}, focused on {@link MelCloudHomeWebSocketListener#extractUnitIds}
 * and callback dispatch — the parsing logic is the most bug-prone part of the realtime accelerator (ADR-007), since
 * the exact wire format of a MELCloud Home push frame is confirmed against a reference implementation, not a formal
 * spec.
 *
 * <p>
 * {@code @SuppressWarnings("null")}: Mockito ({@code mock}/{@code verify}) is not designed with null type
 * annotations in mind, so combining it with this {@code @NonNullByDefault} test class produces "unsafe
 * interpretation" compiler advisories with no null-safety benefit.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class MelCloudHomeWebSocketListenerTest {

    private static MelCloudHomeWebSocketListener newListener(List<String> receivedUnitIds, List<Boolean> openClosed) {
        return new MelCloudHomeWebSocketListener(receivedUnitIds::add, () -> openClosed.add(true),
                () -> openClosed.add(false));
    }

    @Test
    void whenSingleObjectFrameHasUnitStateChangedThenUnitIdIsExtracted() {
        // Arrange
        List<String> received = new ArrayList<>();
        MelCloudHomeWebSocketListener listener = newListener(received, new ArrayList<>());

        // Act
        listener.onWebSocketText("{\"messageType\":\"unitStateChanged\",\"data\":{\"id\":\"unit-1\"}}");

        // Assert
        assertEquals(List.of("unit-1"), received);
    }

    @Test
    void whenArrayFrameHasMultipleUnitStateChangedItemsThenAllUnitIdsAreExtracted() {
        // Arrange
        List<String> received = new ArrayList<>();
        MelCloudHomeWebSocketListener listener = newListener(received, new ArrayList<>());

        // Act
        listener.onWebSocketText("[{\"messageType\":\"unitStateChanged\",\"data\":{\"id\":\"unit-1\"}},"
                + "{\"messageType\":\"unitStateChanged\",\"data\":{\"id\":\"unit-2\"}}]");

        // Assert
        assertEquals(List.of("unit-1", "unit-2"), received);
    }

    @Test
    void whenFrameUsesCapitalizedDataKeyThenUnitIdIsStillExtracted() {
        // Arrange
        List<String> received = new ArrayList<>();
        MelCloudHomeWebSocketListener listener = newListener(received, new ArrayList<>());

        // Act
        listener.onWebSocketText("{\"messageType\":\"unitStateChanged\",\"Data\":{\"id\":\"unit-1\"}}");

        // Assert
        assertEquals(List.of("unit-1"), received);
    }

    @Test
    void whenMessageTypeIsNotUnitStateChangedThenItIsIgnored() {
        // Arrange
        List<String> received = new ArrayList<>();
        MelCloudHomeWebSocketListener listener = newListener(received, new ArrayList<>());

        // Act
        listener.onWebSocketText("{\"messageType\":\"unitCommunicationLost\",\"data\":{\"id\":\"unit-1\"}}");

        // Assert
        assertEquals(List.of(), received);
    }

    @Test
    void whenFrameIsMalformedJsonThenItIsIgnoredWithoutThrowing() {
        // Arrange
        List<String> received = new ArrayList<>();
        MelCloudHomeWebSocketListener listener = newListener(received, new ArrayList<>());

        // Act
        listener.onWebSocketText("not json");

        // Assert
        assertEquals(List.of(), received);
    }

    @Test
    void whenOneItemInArrayIsMalformedThenOtherItemsAreStillProcessed() {
        // Arrange
        List<String> received = new ArrayList<>();
        MelCloudHomeWebSocketListener listener = newListener(received, new ArrayList<>());

        // Act
        listener.onWebSocketText(
                "[\"not an object\"," + "{\"messageType\":\"unitStateChanged\",\"data\":{\"id\":\"unit-1\"}}]");

        // Assert
        assertEquals(List.of("unit-1"), received);
    }

    @Test
    void whenSessionConnectsThenOnOpenCallbackFires() {
        // Arrange
        List<Boolean> openClosed = new ArrayList<>();
        MelCloudHomeWebSocketListener listener = newListener(new ArrayList<>(), openClosed);
        Session session = mock(Session.class);

        // Act
        listener.onWebSocketConnect(session);

        // Assert
        assertEquals(List.of(true), openClosed);
    }

    @Test
    void whenBinaryFrameArrivesThenItIsIgnoredWithoutThrowing() {
        // Arrange
        List<String> received = new ArrayList<>();
        MelCloudHomeWebSocketListener listener = newListener(received, new ArrayList<>());

        // Act
        listener.onWebSocketBinary(new byte[] { 1, 2, 3 }, 0, 3);

        // Assert
        assertEquals(List.of(), received);
    }

    @Test
    void whenSessionClosesThenOnClosedCallbackFires() {
        // Arrange
        List<Boolean> openClosed = new ArrayList<>();
        MelCloudHomeWebSocketListener listener = newListener(new ArrayList<>(), openClosed);

        // Act
        listener.onWebSocketClose(1000, "normal closure");

        // Assert
        assertEquals(List.of(false), openClosed);
    }

    @Test
    void whenSessionErrorsThenOnClosedCallbackFires() {
        // Arrange
        List<Boolean> openClosed = new ArrayList<>();
        MelCloudHomeWebSocketListener listener = newListener(new ArrayList<>(), openClosed);

        // Act
        listener.onWebSocketError(new RuntimeException("boom"));

        // Assert
        assertEquals(List.of(false), openClosed);
    }
}
