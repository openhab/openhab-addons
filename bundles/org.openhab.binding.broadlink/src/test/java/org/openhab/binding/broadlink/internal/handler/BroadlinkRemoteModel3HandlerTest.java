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
package org.openhab.binding.broadlink.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.LEARNING_CONTROL_COMMAND_LEARN;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.core.test.storage.VolatileStorageService;

/**
 * Tests the Remote Model 3 handler.
 * 
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class BroadlinkRemoteModel3HandlerTest extends AbstractBroadlinkThingHandlerTest {

    @BeforeEach
    public void setUp() throws Exception {
        configureUnderlyingThing(BroadlinkBindingConstants.THING_TYPE_RM3, "rm3-test");
        MockitoAnnotations.openMocks(this).close();
        Mockito.when(mockSocket.sendAndReceive(Mockito.any(byte[].class), Mockito.anyString())).thenReturn(response);
    }

    private byte[] response = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, };

    @Test
    public void sendsExpectedBytesWhenEnteringLearnMode() throws IOException {
        VolatileStorageService storageService = new VolatileStorageService();
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);
        ArgumentCaptor<byte[]> byteCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler model3 = new BroadlinkRemoteModel3Handler(thing, commandDescriptionProvider,
                storageService);
        setMocksForTesting(model3);

        reset(trafficObserver);
        model3.handleLearningCommand(LEARNING_CONTROL_COMMAND_LEARN);

        verify(trafficObserver).onCommandSent(commandCaptor.capture());
        assertEquals(0x6a, commandCaptor.getValue().byteValue());

        verify(trafficObserver).onBytesSent(byteCaptor.capture());

        byte[] sentBytes = byteCaptor.getValue();
        assertEquals(16, sentBytes.length);

        assertEquals(0x03, sentBytes[0]); // 0x03, then fifteen zeroes
        assertEquals(0x00, sentBytes[1]);
        assertEquals(0x00, sentBytes[2]);
        assertEquals(0x00, sentBytes[3]);
        assertEquals(0x00, sentBytes[4]);
        assertEquals(0x00, sentBytes[5]);
        assertEquals(0x00, sentBytes[6]);
        assertEquals(0x00, sentBytes[7]);
        assertEquals(0x00, sentBytes[8]);
        assertEquals(0x00, sentBytes[9]);
        assertEquals(0x00, sentBytes[10]);
        assertEquals(0x00, sentBytes[11]);
        assertEquals(0x00, sentBytes[12]);
        assertEquals(0x00, sentBytes[13]);
        assertEquals(0x00, sentBytes[14]);
        assertEquals(0x00, sentBytes[15]);
    }
}
