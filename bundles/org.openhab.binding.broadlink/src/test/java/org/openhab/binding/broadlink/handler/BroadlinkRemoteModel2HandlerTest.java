/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import static org.mockito.Mockito.verify;
import static org.openhab.binding.broadlink.handler.BroadlinkSocketModel2Handler.*;

import java.io.IOException;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * Tests the Remote Model 2 handler.
 * 
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkRemoteModel2HandlerTest extends AbstractBroadlinkThingHandlerTest {

    @BeforeEach
    public void setUp() {
        configureUnderlyingThing(BroadlinkBindingConstants.THING_TYPE_RM2, "rm2-test");
        Mockito.when(mockSocket.sendAndReceive(Mockito.any(byte[].class), Mockito.anyString())).thenReturn(response);
    }

    @Test
    public void mergeOnOffBitsAllZero() {
        int result = mergeOnOffBits(OnOffType.OFF, OnOffType.OFF);
        assertEquals(0x00, result);
    }

    @Test
    public void mergeOnOffBitsPowerOn() {
        int result = mergeOnOffBits(OnOffType.ON, OnOffType.OFF);
        assertEquals(0x01, result);
    }

    @Test
    public void mergeOnOffBitsNightlightOn() {
        int result = mergeOnOffBits(OnOffType.OFF, OnOffType.ON);
        assertEquals(0x02, result);
    }

    @Test
    public void mergeOnOffBitsAllOn() {
        int result = mergeOnOffBits(OnOffType.ON, OnOffType.ON);
        assertEquals(0x03, result);
    }

    @Test
    public void derivePowerStateBitsOff() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x00 };
        OnOffType result = derivePowerStateFromStatusByte(payload);
        assertEquals(OnOffType.OFF, result);
    }

    @Test
    public void derivePowerStateBitsOn1() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x01 };
        OnOffType result = derivePowerStateFromStatusByte(payload);
        assertEquals(OnOffType.ON, result);
    }

    @Test
    public void derivePowerStateBitsOn3() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x03 };
        OnOffType result = derivePowerStateFromStatusByte(payload);
        assertEquals(OnOffType.ON, result);
    }

    @Test
    public void derivePowerStateBitsOnFD() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, (byte) 0xFD };
        OnOffType result = derivePowerStateFromStatusByte(payload);
        assertEquals(OnOffType.ON, result);
    }

    @Test
    public void deriveNightLightStateBitsOff() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x00 };
        OnOffType result = deriveNightLightStateFromStatusByte(payload);
        assertEquals(OnOffType.OFF, result);
    }

    @Test
    public void deriveNightLightStateBitsOn2() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x02 };
        OnOffType result = deriveNightLightStateFromStatusByte(payload);
        assertEquals(OnOffType.ON, result);
    }

    @Test
    public void deriveNightLightStateBitsOn3() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x03 };
        OnOffType result = deriveNightLightStateFromStatusByte(payload);
        assertEquals(OnOffType.ON, result);
    }

    @Test
    public void deriveNightLightStateBitsOnFF() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, (byte) 0xFF };
        OnOffType result = deriveNightLightStateFromStatusByte(payload);
        assertEquals(OnOffType.ON, result);
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
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);
        ArgumentCaptor<byte[]> byteArrayCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler model2 = new BroadlinkRemoteModel2Handler(thing);
        setMocksForTesting(model2);
        model2.getStatusFromDevice();

        verify(trafficObserver).onCommandSent(commandCaptor.capture());
        assertEquals(0x6a, commandCaptor.getValue().byteValue());

        verify(trafficObserver).onBytesSent(byteArrayCaptor.capture());

        byte[] sentBytes = byteArrayCaptor.getValue();
        assertEquals(16, sentBytes.length);
        assertEquals(0x01, sentBytes[0]);
    }

    @Test
    public void setsTheTemperatureChannelAfterGettingStatus() {
        byte[] response = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x03, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, };
        Mockito.when(mockSocket.sendAndReceive(Mockito.any(byte[].class), Mockito.anyString())).thenReturn(response);
        BroadlinkRemoteHandler model2 = new BroadlinkRemoteModel2Handler(thing);
        setMocksForTesting(model2);

        model2.getStatusFromDevice();

        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        ChannelUID expectedTemperatureChannel = new ChannelUID(thing.getUID(),
                BroadlinkBindingConstants.CHANNEL_TEMPERATURE);
        assertEquals(expectedTemperatureChannel, channelCaptor.getValue());

        QuantityType<Temperature> expectedTemperature = new QuantityType<>(106.0, SIUnits.CELSIUS);
        assertEquals(expectedTemperature, stateCaptor.getValue());
    }

    @Test
    public void sendsExpectedBytesWhenSendingCode() throws IOException {
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);
        ArgumentCaptor<byte[]> byteCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler model2 = new BroadlinkRemoteModel2Handler(thing);
        setMocksForTesting(model2);
        // Note the length is 12 so as to not require padding
        byte[] code = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a };
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
        ArgumentCaptor<Byte> commandCaptor = ArgumentCaptor.forClass(Byte.class);
        ArgumentCaptor<byte[]> byteCaptor = ArgumentCaptor.forClass(byte[].class);
        BroadlinkRemoteHandler model2 = new BroadlinkRemoteModel2Handler(thing);
        setMocksForTesting(model2);
        // Note the length is such that padding up to the next multiple of 16 will be needed
        byte[] code = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10,
                0x11 };
        model2.sendCode(code);

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
