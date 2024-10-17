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
import static org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.*;

import java.io.IOException;
import java.util.List;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * Tests the Remote Model 4 handler.
 *
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class BroadlinkRemoteModel4HandlerTest extends AbstractBroadlinkThingHandlerTest {

    private byte[] response = { (byte) 0x5a, (byte) 0xa5, (byte) 0xaa, (byte) 0x55, (byte) 0x5a, (byte) 0xa5,
            (byte) 0xaa, (byte) 0x55, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0xf6, (byte) 0xcd, (byte) 0x00, (byte) 0x00, (byte) 0x14, (byte) 0x27,
            (byte) 0x6a, (byte) 0x00, (byte) 0x63, (byte) 0x00, (byte) 0x11, (byte) 0x22, (byte) 0x11, (byte) 0x22,
            (byte) 0x11, (byte) 0x22, (byte) 0x11, (byte) 0x22, (byte) 0x11, (byte) 0x22, (byte) 0x00, (byte) 0xc0,
            (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x9a, (byte) 0x60, (byte) 0xfb, (byte) 0x72, (byte) 0x07,
            (byte) 0x4f, (byte) 0x89, (byte) 0xf8, (byte) 0xb4, (byte) 0xdb, (byte) 0xb0, (byte) 0x72, (byte) 0xe7,
            (byte) 0x1f, (byte) 0x86, };

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        configureUnderlyingThing(BroadlinkBindingConstants.THING_TYPE_RM4_MINI, "rm4-test");
        MockitoAnnotations.openMocks(this).close();
        Mockito.when(mockSocket.sendAndReceive(ArgumentMatchers.any(byte[].class), ArgumentMatchers.anyString()))
                .thenReturn(response);
    }

    @Test
    public void sendsExpectedBytesWhenGettingDeviceStatus() {
        VolatileStorageService storageService = new VolatileStorageService();
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);
        ArgumentCaptor<byte[]> byteCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler model4 = new BroadlinkRemoteModel4MiniHandler(thing, commandDescriptionProvider,
                storageService);
        setMocksForTesting(model4);
        reset(trafficObserver);
        try {
            model4.getStatusFromDevice();
        } catch (IOException | BroadlinkException e) {
            fail("Unexpected exception: " + e.getClass().getCanonicalName());
        }

        verify(trafficObserver).onCommandSent(commandCaptor.capture());
        assertEquals(0x6a, commandCaptor.getValue().byteValue());

        verify(trafficObserver).onBytesSent(byteCaptor.capture());

        byte[] sentBytes = byteCaptor.getValue();
        assertEquals(16, sentBytes.length);

        assertEquals(0x04, sentBytes[0]);
        assertEquals(0x00, sentBytes[1]);
        assertEquals(0x24, sentBytes[2]);
    }

    @Test
    public void sendsExpectedBytesWhenSendingCode() throws IOException {
        VolatileStorageService storageService = new VolatileStorageService();
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);
        ArgumentCaptor<byte[]> byteCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler model4 = new BroadlinkRemoteModel4MiniHandler(thing, commandDescriptionProvider,
                storageService);
        setMocksForTesting(model4);
        // Note the length is 10 so as to not require padding (6 byte preamble)
        byte[] code = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a };

        reset(trafficObserver);
        model4.sendCode(code);

        verify(trafficObserver).onCommandSent(commandCaptor.capture());
        assertEquals(0x6a, commandCaptor.getValue().byteValue());

        verify(trafficObserver).onBytesSent(byteCaptor.capture());

        byte[] sentBytes = byteCaptor.getValue();
        assertEquals(16, sentBytes.length);

        assertEquals((byte) 0x0e, sentBytes[0]);
        assertEquals(0x00, sentBytes[1]);

        assertEquals(0x02, sentBytes[2]);
        assertEquals(0x00, sentBytes[3]);
        assertEquals(0x00, sentBytes[4]);
        assertEquals(0x00, sentBytes[5]);

        assertEquals(0x01, sentBytes[6]);
        assertEquals(0x02, sentBytes[7]);
        assertEquals(0x03, sentBytes[8]);
        assertEquals(0x04, sentBytes[9]);
        assertEquals(0x05, sentBytes[10]);
        assertEquals(0x06, sentBytes[11]);
        assertEquals(0x07, sentBytes[12]);
        assertEquals(0x08, sentBytes[13]);
        assertEquals(0x09, sentBytes[14]);
        assertEquals(0x0a, sentBytes[15]);
    }

    @Test
    public void sendsExpectedBytesWhenSendingCodeIncludingPadding() throws IOException {
        VolatileStorageService storageService = new VolatileStorageService();
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);
        ArgumentCaptor<byte[]> byteCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler model4 = new BroadlinkRemoteModel4MiniHandler(thing, commandDescriptionProvider,
                storageService);
        setMocksForTesting(model4);
        // Note the length is such that padding up to the next multiple of 16 will be needed
        byte[] code = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b };
        reset(trafficObserver);
        model4.sendCode(code);

        verify(trafficObserver).onCommandSent(commandCaptor.capture());
        assertEquals(0x6a, commandCaptor.getValue().byteValue());

        verify(trafficObserver).onBytesSent(byteCaptor.capture());

        byte[] sentBytes = byteCaptor.getValue();
        assertEquals(32, sentBytes.length);

        // Calculated length field is len(data) + 4 ===> 11 + 4 = 15 = 0x0f

        assertEquals((byte) 0x0f, sentBytes[0]);
        assertEquals(0x00, sentBytes[1]);

        assertEquals(0x02, sentBytes[2]); // The "send code" command

        assertEquals(0x01, sentBytes[6]); // The payload
        assertEquals(0x02, sentBytes[7]);
        assertEquals(0x03, sentBytes[8]);
        assertEquals(0x04, sentBytes[9]);
        assertEquals(0x05, sentBytes[10]);
        assertEquals(0x06, sentBytes[11]);
        assertEquals(0x07, sentBytes[12]);
        assertEquals(0x08, sentBytes[13]);
        assertEquals(0x09, sentBytes[14]);
        assertEquals(0x0a, sentBytes[15]);
        assertEquals(0x0b, sentBytes[16]);
        assertEquals(0x00, sentBytes[17]);
    }

    @Test
    public void setsTheTemperatureAndHumidityChannelsAfterGettingStatus() {
        VolatileStorageService storageService = new VolatileStorageService();
        BroadlinkRemoteHandler model4 = new BroadlinkRemoteModel4MiniHandler(thing, commandDescriptionProvider,
                storageService);
        setMocksForTesting(model4);
        reset(mockCallback);

        try {
            model4.getStatusFromDevice();
        } catch (IOException | BroadlinkException e) {
            fail("Unexpected exception: " + e.getClass().getCanonicalName());
        }

        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, times(2)).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<ChannelUID> channelCaptures = channelCaptor.getAllValues();
        List<State> stateCaptures = stateCaptor.getAllValues();

        ChannelUID expectedTemperatureChannel = new ChannelUID(thing.getUID(), TEMPERATURE_CHANNEL);
        assertEquals(expectedTemperatureChannel, channelCaptures.get(0));

        QuantityType<Temperature> expectedTemperature = new QuantityType<>(21.22,
                BroadlinkBindingConstants.BROADLINK_TEMPERATURE_UNIT);
        assertEquals(expectedTemperature, stateCaptures.get(0));

        ChannelUID expectedHumidityChannel = new ChannelUID(thing.getUID(), HUMIDITY_CHANNEL);
        assertEquals(expectedHumidityChannel, channelCaptures.get(1));

        QuantityType<Dimensionless> expectedHumidity = new QuantityType<>(39.4,
                BroadlinkBindingConstants.BROADLINK_HUMIDITY_UNIT);
        assertEquals(expectedHumidity, stateCaptures.get(1));
    }

    @Test
    public void sendsExpectedBytesWhenEnteringLearnMode() throws IOException {
        VolatileStorageService storageService = new VolatileStorageService();
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);
        ArgumentCaptor<byte[]> byteCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler model4 = new BroadlinkRemoteModel4MiniHandler(thing, commandDescriptionProvider,
                storageService);
        setMocksForTesting(model4);

        reset(trafficObserver);
        model4.handleLearningCommand(LEARNING_CONTROL_COMMAND_LEARN);

        verify(trafficObserver).onCommandSent(commandCaptor.capture());
        assertEquals(0x6a, commandCaptor.getValue().byteValue());

        verify(trafficObserver).onBytesSent(byteCaptor.capture());

        byte[] sentBytes = byteCaptor.getValue();
        assertEquals(16, sentBytes.length);

        // Expecting:
        // PLl, PLh, <commandByte> 0 0 0 then padding for the rest up to 16
        // Where PL = length(data) + 4 - so in this case, 4
        assertEquals(0x04, sentBytes[0]); // Low length byte
        assertEquals(0x00, sentBytes[1]); // High length byte
        assertEquals(0x03, sentBytes[2]);
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
