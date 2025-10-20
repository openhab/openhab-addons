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
package org.openhab.binding.boschshc.internal.devices.lightcontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.AbstractPowerSwitchHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.childprotection.dto.ChildProtectionServiceState;
import org.openhab.binding.boschshc.internal.services.multilevelswitch.dto.MultiLevelSwitchServiceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link DimmerHandler}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class DimmerHandlerTest extends AbstractPowerSwitchHandlerTest<DimmerHandler> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<MultiLevelSwitchServiceState> multiLevelSwitchServiceStateCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<ChildProtectionServiceState> childProtectionServiceStateCaptor;

    @Override
    protected DimmerHandler createFixture() {
        return new DimmerHandler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_DIMMER;
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:60b647fffec5a9d8";
    }

    @Test
    void testUpdateChannelMultiLevelSwitchState() {
        JsonElement jsonObject = JsonParser.parseString("{\"@type\":\"multiLevelSwitchState\",\"level\":16}");
        getFixture().processUpdate("MultiLevelSwitch", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_BRIGHTNESS), new PercentType(16));
    }

    @Test
    void testUpdateChannelsChildProtectionService() {
        String json = """
                {
                    "@type": "ChildProtectionState",
                    "childLockActive": true
                }
                """;
        JsonElement jsonObject = JsonParser.parseString(json);

        getFixture().processUpdate("ChildProtection", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION), OnOffType.ON);
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
    void testUpdateChannelCommunicationQualityService() {
        String json = """
                {
                    "@type": "communicationQualityState",
                    "quality": "UNKNOWN"
                }
                """;
        JsonElement jsonObject = JsonParser.parseString(json);

        getFixture().processUpdate("CommunicationQuality", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH),
                new DecimalType(0));

        json = """
                {
                    "@type": "communicationQualityState",
                    "quality": "GOOD"
                }
                """;
        jsonObject = JsonParser.parseString(json);

        getFixture().processUpdate("CommunicationQuality", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH),
                new DecimalType(4));
    }

    @Test
    void testHandleCommandChildProtection()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION), OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("ChildProtection"),
                childProtectionServiceStateCaptor.capture());
        ChildProtectionServiceState state = childProtectionServiceStateCaptor.getValue();
        assertTrue(state.childLockActive);
    }

    @Test
    void testHandleCommandChildProtectionInvalidCommand()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION),
                DecimalType.ZERO);
        verify(getBridgeHandler(), times(0)).putState(eq(getDeviceID()), eq("ChildProtection"),
                childProtectionServiceStateCaptor.capture());
    }
}
