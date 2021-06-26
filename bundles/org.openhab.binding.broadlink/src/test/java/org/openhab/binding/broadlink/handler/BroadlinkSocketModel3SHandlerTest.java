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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.broadlink.BroadlinkBindingConstants.*;

import java.util.List;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.broadlink.BroadlinkBindingConstants;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * Tests the Socket Model 3S (SP3S) handler.
 * 
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class BroadlinkSocketModel3SHandlerTest extends AbstractBroadlinkThingHandlerTest {

    private final byte[] response = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x03, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, };

    private final BroadlinkSocketModel3SHandler model3s;

    public BroadlinkSocketModel3SHandlerTest() {
        super();
        configureUnderlyingThing(BroadlinkBindingConstants.THING_TYPE_SP3S, "sp3s-test");
        model3s = new BroadlinkSocketModel3SHandler(thing);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(mockSocket.sendAndReceive(Mockito.any(byte[].class), Mockito.anyString())).thenReturn(response);
    }

    @Test
    public void derivePowerConsumptionFromStatusByteTooShort() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x33 };
        double result = model3s.deriveSP3sPowerConsumption(payload);
        assertEquals(0D, result, 0.1D);
    }

    @Test
    public void derivePowerConsumptionFromStatusByteCorrect() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x19, (byte) 0xCD };
        double result = model3s.deriveSP3sPowerConsumption(payload);
        assertEquals(66.051D, result, 0.1D);
    }

    @Test
    public void setsThePowerChannelAndConsumptionAfterGettingStatusOnSP3S() {
        Mockito.when(mockSocket.sendAndReceive(Mockito.any(byte[].class), Mockito.anyString())).thenReturn(response);
        setMocksForTesting(model3s);

        reset(mockCallback);

        model3s.getStatusFromDevice();

        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, Mockito.times(2)).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<ChannelUID> channels = channelCaptor.getAllValues();
        List<State> states = stateCaptor.getAllValues();

        ChannelUID expectedPowerChannel = new ChannelUID(thing.getUID(), COMMAND_POWER_ON);
        assertEquals(expectedPowerChannel, channels.get(0));

        assertEquals(OnOffType.ON, states.get(0));

        ChannelUID expectedConsumptionChannel = new ChannelUID(thing.getUID(), CHANNEL_POWER_CONSUMPTION);
        assertEquals(expectedConsumptionChannel, channels.get(1));

        QuantityType<Power> expectedPower = new QuantityType<>(14933790.87,
                BroadlinkBindingConstants.BROADLINK_POWER_CONSUMPTION_UNIT);
        assertEquals(expectedPower, states.get(1));
    }
}
