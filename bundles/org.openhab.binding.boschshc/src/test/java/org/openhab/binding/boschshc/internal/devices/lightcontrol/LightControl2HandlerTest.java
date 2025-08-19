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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.AbstractBoschSHCDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.childprotection.dto.ChildProtectionServiceState;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchService;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchState;
import org.openhab.binding.boschshc.internal.services.powerswitch.dto.PowerSwitchServiceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link LightControl2Handler}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class LightControl2HandlerTest extends AbstractBoschSHCDeviceHandlerTest<LightControl2Handler> {

    private static final String CHILD_DEVICE_ID_1 = "hdm:ZigBee:70ac08fffefead2d#2";
    private static final String CHILD_DEVICE_ID_2 = "hdm:ZigBee:70ac08fffefead2d#3";

    private @Captor @NonNullByDefault({}) ArgumentCaptor<QuantityType<Power>> powerCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<QuantityType<Energy>> energyCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<ChildProtectionServiceState> childProtectionServiceStateCaptor;

    private @Captor @NonNullByDefault({}) ArgumentCaptor<PowerSwitchServiceState> powerSwitchStateCaptor;

    @Override
    protected LightControl2Handler createFixture() {
        return new LightControl2Handler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_LIGHT_CONTROL_2;
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:70ac08fcfefa5197";
    }

    @Override
    protected void configureDevice(Device device) {
        super.configureDevice(device);

        // order is reversed to test child ID sorting during initialization
        device.childDeviceIds = List.of(CHILD_DEVICE_ID_2, CHILD_DEVICE_ID_1);
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
    void testUpdateChannelPowerMeterServiceState() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                  "@type": "powerMeterState",
                  "powerConsumption": "23",
                  "energyConsumption": 42
                }\
                """);
        getFixture().processUpdate("PowerMeter", jsonObject);

        verify(getCallback()).stateUpdated(eq(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_CONSUMPTION)),
                powerCaptor.capture());
        QuantityType<Power> powerValue = powerCaptor.getValue();
        assertEquals(23, powerValue.intValue());

        verify(getCallback()).stateUpdated(eq(getChannelUID(BoschSHCBindingConstants.CHANNEL_ENERGY_CONSUMPTION)),
                energyCaptor.capture());
        QuantityType<Energy> energyValue = energyCaptor.getValue();
        assertEquals(42, energyValue.intValue());
    }

    @Test
    void testHandleCommandPowerSwitchChannelChildDevice1()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH_1), OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(CHILD_DEVICE_ID_1), eq("PowerSwitch"), powerSwitchStateCaptor.capture());
        PowerSwitchServiceState state = powerSwitchStateCaptor.getValue();
        assertSame(PowerSwitchState.ON, state.switchState);

        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH_1), OnOffType.OFF);
        verify(getBridgeHandler(), times(2)).putState(eq(CHILD_DEVICE_ID_1), eq("PowerSwitch"),
                powerSwitchStateCaptor.capture());
        state = powerSwitchStateCaptor.getValue();
        assertSame(PowerSwitchState.OFF, state.switchState);
    }

    @Test
    void testHandleCommandPowerSwitchChannelChildDevice2()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH_2), OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(CHILD_DEVICE_ID_2), eq("PowerSwitch"), powerSwitchStateCaptor.capture());
        PowerSwitchServiceState state = powerSwitchStateCaptor.getValue();
        assertSame(PowerSwitchState.ON, state.switchState);

        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH_2), OnOffType.OFF);
        verify(getBridgeHandler(), times(2)).putState(eq(CHILD_DEVICE_ID_2), eq("PowerSwitch"),
                powerSwitchStateCaptor.capture());
        state = powerSwitchStateCaptor.getValue();
        assertSame(PowerSwitchState.OFF, state.switchState);
    }

    @Test
    void testUpdateChannelPowerSwitchStateChildDevice1() {
        JsonElement jsonObject = JsonParser
                .parseString("{\n" + "  \"@type\": \"powerSwitchState\",\n" + "  \"switchState\": \"ON\"\n" + "}");
        getFixture().processChildUpdate(CHILD_DEVICE_ID_1, PowerSwitchService.POWER_SWITCH_SERVICE_NAME, jsonObject);
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH_1),
                OnOffType.ON);

        jsonObject = JsonParser
                .parseString("{\n" + "  \"@type\": \"powerSwitchState\",\n" + "  \"switchState\": \"OFF\"\n" + "}");
        getFixture().processChildUpdate(CHILD_DEVICE_ID_1, PowerSwitchService.POWER_SWITCH_SERVICE_NAME, jsonObject);
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH_1),
                OnOffType.OFF);
    }

    @Test
    void testUpdateChannelPowerSwitchStateChildDevice2() {
        JsonElement jsonObject = JsonParser
                .parseString("{\n" + "  \"@type\": \"powerSwitchState\",\n" + "  \"switchState\": \"ON\"\n" + "}");
        getFixture().processChildUpdate(CHILD_DEVICE_ID_2, PowerSwitchService.POWER_SWITCH_SERVICE_NAME, jsonObject);
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH_2),
                OnOffType.ON);

        jsonObject = JsonParser
                .parseString("{\n" + "  \"@type\": \"powerSwitchState\",\n" + "  \"switchState\": \"OFF\"\n" + "}");
        getFixture().processChildUpdate(CHILD_DEVICE_ID_2, PowerSwitchService.POWER_SWITCH_SERVICE_NAME, jsonObject);
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH_2),
                OnOffType.OFF);
    }

    @Test
    void testUpdateChannelsChildProtectionServiceChildDevice1() {
        String json = """
                {
                    "@type": "ChildProtectionState",
                    "childLockActive": true
                }
                """;
        JsonElement jsonObject = JsonParser.parseString(json);

        getFixture().processChildUpdate(CHILD_DEVICE_ID_1, "ChildProtection", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION_1), OnOffType.ON);
    }

    @Test
    void testUpdateChannelsChildProtectionServiceChildDevice2() {
        String json = """
                {
                    "@type": "ChildProtectionState",
                    "childLockActive": true
                }
                """;
        JsonElement jsonObject = JsonParser.parseString(json);

        getFixture().processChildUpdate(CHILD_DEVICE_ID_2, "ChildProtection", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION_2), OnOffType.ON);
    }

    @Test
    void testHandleCommandChildProtectionServiceChildDevice1()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION_1), OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(CHILD_DEVICE_ID_1), eq("ChildProtection"),
                childProtectionServiceStateCaptor.capture());
        ChildProtectionServiceState state = childProtectionServiceStateCaptor.getValue();
        assertTrue(state.childLockActive);
    }

    @Test
    void testHandleCommandChildProtectionServiceChildDevice2()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION_2), OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(CHILD_DEVICE_ID_2), eq("ChildProtection"),
                childProtectionServiceStateCaptor.capture());
        ChildProtectionServiceState state = childProtectionServiceStateCaptor.getValue();
        assertTrue(state.childLockActive);
    }

    @Test
    void testInitializeNoChildIDsInDeviceInfo() {
        getDevice().childDeviceIds = null;

        getFixture().initialize();

        verify(getCallback()).statusUpdated(same(getThing()),
                argThat(status -> status.getStatus().equals(ThingStatus.OFFLINE)
                        && status.getStatusDetail().equals(ThingStatusDetail.CONFIGURATION_ERROR)));
    }

    @Test
    void testInitializeServicesNoChildIDsInDeviceInfo() {
        getDevice().childDeviceIds = null;

        LightControl2Handler lFixture = new LightControl2Handler(getThing());
        lFixture.setCallback(getCallback());

        // this call will return before reaching initializeServices()
        lFixture.initialize();

        assertThrows(BoschSHCException.class, () -> lFixture.initializeServices());
    }
}
