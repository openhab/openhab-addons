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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.core.test.storage.VolatileStorageService;

/**
 * Tests the Remote Model Pro handler.
 *
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class BroadlinkRemoteModelProHandlerTest extends AbstractBroadlinkThingHandlerTest {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        configureUnderlyingThing(BroadlinkBindingConstants.THING_TYPE_RM_PRO, "rm_pro-test");
        MockitoAnnotations.openMocks(this).close();
        Mockito.when(mockSocket.sendAndReceive(ArgumentMatchers.any(byte[].class), ArgumentMatchers.anyString()))
                .thenReturn(response);
    }

    private byte[] response = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, };

    @Test
    public void sendsExpectedBytesWhenGettingDeviceStatus() {
        VolatileStorageService storageService = new VolatileStorageService();
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);
        ArgumentCaptor<byte[]> byteArrayCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler model2 = new BroadlinkRemoteModelProHandler(thing, commandDescriptionProvider,
                storageService);
        setMocksForTesting(model2);
        reset(trafficObserver);
        try {
            model2.getStatusFromDevice();
        } catch (IOException | BroadlinkException e) {
            fail("Unexpected exception: " + e.getClass().getCanonicalName());
        }

        verify(trafficObserver).onCommandSent(commandCaptor.capture());
        assertEquals(0x6a, commandCaptor.getValue().byteValue());

        verify(trafficObserver).onBytesSent(byteArrayCaptor.capture());

        byte[] sentBytes = byteArrayCaptor.getValue();
        assertEquals(16, sentBytes.length);
        assertEquals(0x01, sentBytes[0]);
    }

    @Test
    public void sendsExpectedBytesWhenSendingCode() throws IOException {
        VolatileStorageService storageService = new VolatileStorageService();
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);
        ArgumentCaptor<byte[]> byteCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler model2 = new BroadlinkRemoteModelProHandler(thing, commandDescriptionProvider,
                storageService);
        setMocksForTesting(model2);
        // Note the length is 12 so as to not require padding
        byte[] code = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a };

        reset(trafficObserver);
        model2.sendCode(code);

        verify(trafficObserver).onCommandSent(commandCaptor.capture());
        assertEquals(0x6a, commandCaptor.getValue().byteValue());

        verify(trafficObserver).onBytesSent(byteCaptor.capture());

        byte[] sentBytes = byteCaptor.getValue();
        assertEquals(16, sentBytes.length);

        assertEquals(0x02, sentBytes[0]); // 0x00, 0x00, 0x00

        assertEquals(0x01, sentBytes[4]);
        assertEquals(0x02, sentBytes[5]);
        assertEquals(0x03, sentBytes[6]);
        assertEquals(0x04, sentBytes[7]);
        assertEquals(0x05, sentBytes[8]);
        assertEquals(0x06, sentBytes[9]);
        assertEquals(0x07, sentBytes[10]);
        assertEquals(0x08, sentBytes[11]);
        assertEquals(0x09, sentBytes[12]);
        assertEquals(0x0a, sentBytes[13]);
    }

    @Test
    public void sendsExpectedBytesWhenSendingCodeIncludingPadding() throws IOException {
        VolatileStorageService storageService = new VolatileStorageService();
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);
        ArgumentCaptor<byte[]> byteCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler modelPro = new BroadlinkRemoteModelProHandler(thing, commandDescriptionProvider,
                storageService);
        setMocksForTesting(modelPro);
        // Note the length is such that padding up to the next multiple of 16 will be needed
        byte[] code = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10,
                0x11 };
        reset(trafficObserver);
        modelPro.sendCode(code);

        verify(trafficObserver).onCommandSent(commandCaptor.capture());
        assertEquals(0x6a, commandCaptor.getValue().byteValue());

        verify(trafficObserver).onBytesSent(byteCaptor.capture());

        byte[] sentBytes = byteCaptor.getValue();
        assertEquals(32, sentBytes.length);

        assertEquals(0x02, sentBytes[0]); // 0x00, 0x00, 0x00

        assertEquals(0x01, sentBytes[4]);
        assertEquals(0x02, sentBytes[5]);
        assertEquals(0x03, sentBytes[6]);
        assertEquals(0x04, sentBytes[7]);
        assertEquals(0x05, sentBytes[8]);
        assertEquals(0x06, sentBytes[9]);
        assertEquals(0x07, sentBytes[10]);
        assertEquals(0x08, sentBytes[11]);
        assertEquals(0x09, sentBytes[12]);
        assertEquals(0x0a, sentBytes[13]);
        assertEquals(0x0b, sentBytes[14]);
        assertEquals(0x0c, sentBytes[15]);
        assertEquals(0x0d, sentBytes[16]);
        assertEquals(0x0e, sentBytes[17]);
        assertEquals(0x0f, sentBytes[18]);
        assertEquals(0x10, sentBytes[19]);
        assertEquals(0x11, sentBytes[20]);
        assertEquals(0x00, sentBytes[21]);
        assertEquals(0x00, sentBytes[31]);
    }
}
