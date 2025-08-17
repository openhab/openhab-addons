/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.smartbulb;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.AbstractBoschSHCDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.binaryswitch.dto.BinarySwitchServiceState;
import org.openhab.binding.boschshc.internal.services.hsbcoloractuator.dto.HSBColorActuatorServiceState;
import org.openhab.binding.boschshc.internal.services.multilevelswitch.dto.MultiLevelSwitchServiceState;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link SmartBulbHandler}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class SmartBulbHandlerTest extends AbstractBoschSHCDeviceHandlerTest<SmartBulbHandler> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<BinarySwitchServiceState> binarySwitchServiceStateCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<MultiLevelSwitchServiceState> multiLevelSwitchServiceStateCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<HSBColorActuatorServiceState> hsbColorActuatorServiceStateCaptor;

    @Override
    protected SmartBulbHandler createFixture() {
        return new SmartBulbHandler(getThing());
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:f0d1b80000f2a3e9";
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_SMART_BULB;
    }

    @Test
    void testHandleCommandBinarySwitch()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_POWER_SWITCH),
                OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("BinarySwitch"),
                binarySwitchServiceStateCaptor.capture());
        BinarySwitchServiceState state = binarySwitchServiceStateCaptor.getValue();
        assertTrue(state.on);

        getFixture().handleCommand(new ChannelUID(new ThingUID(getThingTypeUID(), "abcdef"),
                BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), OnOffType.OFF);
        verify(getBridgeHandler(), times(2)).putState(eq(getDeviceID()), eq("BinarySwitch"),
                binarySwitchServiceStateCaptor.capture());
        state = binarySwitchServiceStateCaptor.getValue();
        assertFalse(state.on);
    }

    @Test
    void testHandleCommandMultiLevelSwitch()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_BRIGHTNESS),
                new PercentType(42));
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("MultiLevelSwitch"),
                multiLevelSwitchServiceStateCaptor.capture());
        MultiLevelSwitchServiceState state = multiLevelSwitchServiceStateCaptor.getValue();
        assertEquals(42, state.level);
    }

    @Test
    void testHandleCommandHSBColorActuator()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_COLOR),
                HSBType.BLUE);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("HSBColorActuator"),
                hsbColorActuatorServiceStateCaptor.capture());
        HSBColorActuatorServiceState state = hsbColorActuatorServiceStateCaptor.getValue();
        assertEquals(-16776961, state.rgb);
    }

    @Test
    void testUpdateChannelBinarySwitchState() {
        JsonElement jsonObject = JsonParser.parseString("{\"@type\":\"binarySwitchState\",\"on\":true}");
        getFixture().processUpdate("BinarySwitch", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), OnOffType.ON);

        jsonObject = JsonParser.parseString("{\"@type\":\"binarySwitchState\",\"on\":false}");
        getFixture().processUpdate("BinarySwitch", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_POWER_SWITCH), OnOffType.OFF);
    }

    @Test
    void testUpdateChannelMultiLevelSwitchState() {
        JsonElement jsonObject = JsonParser.parseString("{\"@type\":\"multiLevelSwitchState\",\"level\":16}");
        getFixture().processUpdate("MultiLevelSwitch", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_BRIGHTNESS), new PercentType(16));
    }

    @Test
    void testUpdateChannelHSBColorActuatorState() {
        JsonElement jsonObject = JsonParser.parseString("""
                {"colorTemperatureRange": {
                        "minCt": 153,
                        "maxCt": 526
                    },
                    "@type": "colorState",
                    "gamut": "LEDVANCE_GAMUT_A",
                    "rgb": -12427}\
                """);
        getFixture().processUpdate("HSBColorActuator", jsonObject);
        verify(getCallback()).stateUpdated(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_COLOR),
                HSBType.fromRGB(255, 207, 117));
    }
}
