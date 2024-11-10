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
 * Tests the Socket Model 3S (SP3S) handler.
 *
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class BroadlinkSocketModel3SHandlerTest extends AbstractBroadlinkThingHandlerTest {

    private final BroadlinkSocketModel3SHandler model3s;

    public BroadlinkSocketModel3SHandlerTest() {
        super();
        configureUnderlyingThing(BroadlinkBindingConstants.THING_TYPE_SP3S, "sp3s-test");
        model3s = new BroadlinkSocketModel3SHandler(thing);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void deriveSp3sPowerConsumptionTooShort() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x33 };
        double result = model3s.deriveSP3sPowerConsumption(payload);
        assertEquals(0D, result, 0.1D);
    }

    @Test
    public void deriveSp3sPowerConsumptionCorrectSmallValue() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x19, 0x02, 0x00 };
        double result = model3s.deriveSP3sPowerConsumption(payload);
        assertEquals(2.19D, result, 0.1D);
    }

    @Test
    public void deriveSp3sPowerConsumptionCorrectMediumValue() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x33, 0x75, 0x00 };
        double result = model3s.deriveSP3sPowerConsumption(payload);
        assertEquals(75.33D, result, 0.1D);
    }

    @Test
    public void deriveSp3sPowerConsumptionCorrectLargeValue() {
        byte[] payload = { 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x99, (byte) 0x88, 0x07 };
        double result = model3s.deriveSP3sPowerConsumption(payload);
        assertEquals(788.99D, result, 0.1D);
    }

    @Test
    public void setsThePowerChannelAndConsumptionAfterGettingStatusOnSP3S() {
        // Power bytes are 5, 6, 7 (little-endian) in BCD
        // So here it's 0x38291 => 38291, divided by 100 ==> 382.91W
        byte[] payload = { 0x08, 0x00, 0x11, 0x22, 0x01, (byte) 0x91, (byte) 0x82, 0x3, 0x16, 0x27, 0x28, 0x01, 0x02,
                0x04, 0x05, 0x16 };
        byte[] responseMessage = generateReceivedBroadlinkMessage(payload);
        Mockito.when(mockSocket.sendAndReceive(ArgumentMatchers.any(byte[].class), ArgumentMatchers.anyString()))
                .thenReturn(responseMessage);
        setMocksForTesting(model3s);

        reset(mockCallback);

        try {
            model3s.getStatusFromDevice();
        } catch (IOException e) {
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

        QuantityType<Power> expectedPower = new QuantityType<>(382.91,
                BroadlinkBindingConstants.BROADLINK_POWER_CONSUMPTION_UNIT);
        assertEquals(expectedPower, states.get(1));
    }
}
