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
package org.openhab.binding.lutron.internal.hw;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.core.io.console.Console;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link HwConsoleCommandExtension}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
@Timeout(60)
public class HwConsoleCommandExtensionTest {
    private static final String UID = "lutron:hwtelnetbridge:test";

    private final ThingRegistry registry = mock(ThingRegistry.class);
    private final Console console = mock(Console.class);
    private final Thing thing = mock(Thing.class);
    private final HwBridgeHandler bridge = mock(HwBridgeHandler.class);
    private final HwConsoleCommandExtension extension = new HwConsoleCommandExtension(registry);

    @BeforeEach
    public void setUp() {
        when(registry.get(new ThingUID(UID))).thenReturn(thing);
        when(thing.getHandler()).thenReturn(bridge);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(thing.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
    }

    @Test
    public void sendPrintsCommandAndRepliesThenStopsListening() {
        AtomicReference<Consumer<String>> listener = new AtomicReference<>();
        doAnswer(invocation -> {
            listener.set(invocation.getArgument(0));
            return null;
        }).when(bridge).addLineListener(any());
        doAnswer(invocation -> {
            listener.get().accept("Keypad button monitoring enabled");
            return null;
        }).when(bridge).sendCommand("KBMON");

        extension.execute(new String[] { "hw", "send", UID, "KBMON" }, console);

        verify(console).println("> KBMON");
        verify(console).println("< Keypad button monitoring enabled");
        verify(console).println("Done.");
        verify(bridge).removeLineListener(listener.get());
    }

    @Test
    public void sendJoinsTheRemainingWordsIntoOneCommand() {
        extension.execute(new String[] { "hw", "send", UID, "RDL,", "[01:01:03:04:02]" }, console);

        verify(bridge).sendCommand("RDL, [01:01:03:04:02]");
    }

    @Test
    public void offlineBridgeIsRefused() {
        when(thing.getStatus()).thenReturn(ThingStatus.OFFLINE);

        extension.execute(new String[] { "hw", "send", UID, "KBMON" }, console);

        verify(bridge, never()).sendCommand(anyString());
    }

    @Test
    public void unknownThingIsReported() {
        extension.execute(new String[] { "hw", "send", "lutron:hwtelnetbridge:other", "KBMON" }, console);

        verify(console).println("No thing with UID 'lutron:hwtelnetbridge:other'");
        verify(bridge, never()).sendCommand(anyString());
    }

    @Test
    public void invalidSecondsPrintsUsageWithoutListening() {
        extension.execute(new String[] { "hw", "listen", UID, "soon" }, console);

        verify(console).println("Invalid number of seconds 'soon'");
        verify(bridge, never()).addLineListener(any());
    }

    @Test
    public void incompleteCommandPrintsUsage() {
        extension.execute(new String[] { "hw", "send" }, console);

        verify(bridge, never()).sendCommand(anyString());
    }
}
