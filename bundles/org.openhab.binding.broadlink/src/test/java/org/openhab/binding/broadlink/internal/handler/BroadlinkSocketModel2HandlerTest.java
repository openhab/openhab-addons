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

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * Tests the Socket Model 2 handler.
 *
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class BroadlinkSocketModel2HandlerTest extends AbstractBroadlinkThingHandlerTest {

    private final byte[] response = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, };

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        configureUnderlyingThing(BroadlinkBindingConstants.THING_TYPE_SP2, "sp2-test");
        MockitoAnnotations.openMocks(this).close();
        Mockito.when(mockSocket.sendAndReceive(ArgumentMatchers.any(byte[].class), ArgumentMatchers.anyString()))
                .thenReturn(response);
    }

    @Test
    public void derivePowerStateBitsOff() {
        BroadlinkSocketModel2Handler model2 = new BroadlinkSocketModel2Handler(thing, false);
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x00 };
        OnOffType result = model2.derivePowerStateFromStatusBytes(payload);
        assertEquals(OnOffType.OFF, result);
    }

    @Test
    public void derivePowerStateBitsOn1() {
        BroadlinkSocketModel2Handler model2 = new BroadlinkSocketModel2Handler(thing, false);
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x01 };
        OnOffType result = model2.derivePowerStateFromStatusBytes(payload);
        assertEquals(OnOffType.ON, result);
    }

    @Test
    public void derivePowerStateBitsOn3() {
        BroadlinkSocketModel2Handler model2 = new BroadlinkSocketModel2Handler(thing, false);
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x03 };
        OnOffType result = model2.derivePowerStateFromStatusBytes(payload);
        assertEquals(OnOffType.ON, result);
    }

    @Test
    public void derivePowerStateBitsOnFD() {
        BroadlinkSocketModel2Handler model2 = new BroadlinkSocketModel2Handler(thing, false);
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, (byte) 0xFD };
        OnOffType result = model2.derivePowerStateFromStatusBytes(payload);
        assertEquals(OnOffType.ON, result);
    }

    @Test
    public void derivePowerConsumptionFromStatusBytesTooShort() throws IOException {
        BroadlinkSocketModel2Handler model2 = new BroadlinkSocketModel2Handler(thing, false);
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x33 };
        double result = model2.derivePowerConsumption(payload);
        assertEquals(0D, result, 0.1D);
    }

    @Test
    public void derivePowerConsumptionFromStatusBytesCorrect() throws IOException {
        BroadlinkSocketModel2Handler model2 = new BroadlinkSocketModel2Handler(thing, false);
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x03, 0x02, 0x01, 0x00 };
        double result = model2.derivePowerConsumption(payload);
        assertEquals(66.051D, result, 0.1D);
    }

    @Test
    public void setsThePowerChannelOnlyAfterGettingStatusOnSP2() {
        byte[] response = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x03, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, };
        Mockito.when(mockSocket.sendAndReceive(ArgumentMatchers.any(byte[].class), ArgumentMatchers.anyString()))
                .thenReturn(response);
        BroadlinkSocketHandler model2 = new BroadlinkSocketModel2Handler(thing, false);
        setMocksForTesting(model2);

        reset(mockCallback);

        try {
            model2.getStatusFromDevice();
        } catch (IOException | BroadlinkException e) {
            fail("Unexpected exception: " + e.getClass().getCanonicalName());
        }

        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        ChannelUID expectedPowerChannel = new ChannelUID(thing.getUID(), COMMAND_POWER_ON);
        assertEquals(expectedPowerChannel, channelCaptor.getValue());

        assertEquals(OnOffType.ON, stateCaptor.getValue());
    }

    @Test
    public void setsThePowerAndPowerConsumptionAfterGettingStatusOnSP2S() {
        // Power bytes are 4, 5, 6 (little-endian)
        // So here it's 0x38291 => 230033, divided by 1000 ==> 230.033W
        byte[] payload = { 0x08, 0x00, 0x11, 0x22, (byte) 0x91, (byte) 0x82, 0x3, 0x16, 0x27, 0x28, 0x01, 0x02, 0x03,
                0x04, 0x05, 0x16 };
        byte[] responseMessage = generateReceivedBroadlinkMessage(payload);
        Mockito.when(mockSocket.sendAndReceive(ArgumentMatchers.any(byte[].class), ArgumentMatchers.anyString()))
                .thenReturn(responseMessage);
        BroadlinkSocketHandler model2s = new BroadlinkSocketModel2Handler(thing, true);
        setMocksForTesting(model2s);

        reset(mockCallback);

        try {
            model2s.getStatusFromDevice();
        } catch (IOException | BroadlinkException e) {
            fail("Unexpected exception: " + e.getClass().getCanonicalName());
        }

        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, Mockito.times(2)).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<ChannelUID> channels = channelCaptor.getAllValues();
        List<State> states = stateCaptor.getAllValues();

        ChannelUID expectedPowerChannel = new ChannelUID(thing.getUID(), COMMAND_POWER_ON);
        assertEquals(expectedPowerChannel, channels.get(0));

        assertEquals(OnOffType.ON, states.get(0));

        ChannelUID expectedConsumptionChannel = new ChannelUID(thing.getUID(), POWER_CONSUMPTION_CHANNEL);
        assertEquals(expectedConsumptionChannel, channels.get(1));

        QuantityType<Power> expectedPower = new QuantityType<>(230.033,
                BroadlinkBindingConstants.BROADLINK_POWER_CONSUMPTION_UNIT);
        assertEquals(expectedPower, states.get(1));
    }
}
