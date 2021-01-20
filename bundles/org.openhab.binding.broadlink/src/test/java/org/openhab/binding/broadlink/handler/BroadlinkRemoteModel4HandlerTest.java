/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * Tests the Remote Model 4 handler.
 * 
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkRemoteModel4HandlerTest extends AbstractBroadlinkThingHandlerTest {

    private byte[] response = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, };

    @BeforeEach
    public void setUp() {
        configureUnderlyingThing(BroadlinkBindingConstants.THING_TYPE_RM4, "rm4-test");
        MockitoAnnotations.initMocks(this);
        Mockito.when(mockSocket.sendAndReceive(Mockito.any(byte[].class), Mockito.anyString())).thenReturn(response);
    }

    @Test
    public void sendsExpectedBytesWhenGettingDeviceStatus() {
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);

        ArgumentCaptor<byte[]> byteCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler model4 = new BroadlinkRemoteModel4Handler(thing);
        setMocksForTesting(model4);
        model4.getStatusFromDevice();

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
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);
        ArgumentCaptor<byte[]> byteCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler model4 = new BroadlinkRemoteModel4Handler(thing);
        setMocksForTesting(model4);
        // Note the length is 10 so as to not require padding (6 byte preamble)
        byte[] code = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a };
        model4.sendCode(code);

        verify(trafficObserver).onCommandSent(commandCaptor.capture());
        assertEquals(0x6a, commandCaptor.getValue().byteValue());

        verify(trafficObserver).onBytesSent(byteCaptor.capture());

        byte[] sentBytes = byteCaptor.getValue();
        assertEquals(16, sentBytes.length);

        assertEquals((byte) 0xd0, (byte) sentBytes[0]);
        assertEquals(0x00, sentBytes[1]);

        assertEquals(0x02, sentBytes[2]); // 0x00, 0x00, 0x00

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
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);
        ArgumentCaptor<byte[]> byteCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler model4 = new BroadlinkRemoteModel4Handler(thing);
        setMocksForTesting(model4);
        // Note the length is such that padding up to the next multiple of 16 will be needed
        byte[] code = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b };
        model4.sendCode(code);

        verify(trafficObserver).onCommandSent(commandCaptor.capture());
        assertEquals(0x6a, commandCaptor.getValue().byteValue());

        verify(trafficObserver).onBytesSent(byteCaptor.capture());

        byte[] sentBytes = byteCaptor.getValue();
        assertEquals(32, sentBytes.length);

        assertEquals((byte) 0xd0, (byte) sentBytes[0]);
        assertEquals(0x00, sentBytes[1]);

        assertEquals(0x02, sentBytes[2]); // 0x00, 0x00, 0x00

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
        assertEquals(0x0b, sentBytes[16]);
        assertEquals(0x00, sentBytes[17]);
    }

    @Test
    public void setsTheTemperatureAndHumidityChannelsAfterGettingStatus() {
        byte[] response = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, };
        Mockito.when(mockSocket.sendAndReceive(Mockito.any(byte[].class), Mockito.anyString())).thenReturn(response);
        BroadlinkRemoteHandler model4 = new BroadlinkRemoteModel4Handler(thing);
        setMocksForTesting(model4);

        model4.getStatusFromDevice();

        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, times(2)).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<ChannelUID> channelCaptures = channelCaptor.getAllValues();
        List<State> stateCaptures = stateCaptor.getAllValues();

        ChannelUID expectedTemperatureChannel = new ChannelUID(thing.getUID(), "temperature");
        assertEquals(expectedTemperatureChannel, channelCaptures.get(0));

        DecimalType expectedTemperature = new DecimalType(84.16999816894531D);
        assertEquals(expectedTemperature, stateCaptures.get(0));

        ChannelUID expectedHumidityChannel = new ChannelUID(thing.getUID(), "humidity");
        assertEquals(expectedHumidityChannel, channelCaptures.get(1));

        DecimalType expectedHumidity = new DecimalType(-85.81999969482422D);
        assertEquals(expectedHumidity, stateCaptures.get(1));
    }
}
