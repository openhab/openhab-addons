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
package org.openhab.binding.boschshc.internal.devices.waterleakage;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Message;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.MessageCode;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.dto.EnabledDisabledState;
import org.openhab.binding.boschshc.internal.services.waterleakagesensortilt.dto.WaterLeakageSensorTiltServiceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link WaterLeakageSensorHandler}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class WaterLeakageSensorHandlerTest extends AbstractBatteryPoweredDeviceHandlerTest<WaterLeakageSensorHandler> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<WaterLeakageSensorTiltServiceState> waterLeakageTiltServiceStateCaptor;

    @Override
    protected WaterLeakageSensorHandler createFixture() {
        return new WaterLeakageSensorHandler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_WATER_DETECTOR;
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:f0d1b80001d639d5";
    }

    @Test
    void updateChannelsWaterLeakageSensorServiceState() {
        JsonElement jsonObject = JsonParser
                .parseString("{\"@type\":\"waterLeakageSensorState\",\"state\": \"NO_LEAKAGE\"}");
        getFixture().processUpdate("WaterLeakageSensor", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_WATER_LEAKAGE), OnOffType.OFF);

        jsonObject = JsonParser.parseString("{\"@type\":\"waterLeakageSensorState\",\"state\": \"LEAKAGE_DETECTED\"}");
        getFixture().processUpdate("WaterLeakageSensor", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_WATER_LEAKAGE), OnOffType.ON);
    }

    @Test
    void updateChannelsWaterLeakageSensorTiltServiceState() {
        JsonElement jsonObject = JsonParser.parseString(
                "{\"@type\":\"waterLeakageSensorTiltState\",\"pushNotificationState\": \"DISABLED\",\"acousticSignalState\": \"DISABLED\"}");
        getFixture().processUpdate("WaterLeakageSensorTilt", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PUSH_NOTIFICATIONS_ON_MOVE),
                OnOffType.OFF);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_ACOUSTIC_SIGNALS_ON_MOVE),
                OnOffType.OFF);

        jsonObject = JsonParser.parseString(
                "{\"@type\":\"waterLeakageSensorTiltState\",\"pushNotificationState\": \"ENABLED\",\"acousticSignalState\": \"ENABLED\"}");
        getFixture().processUpdate("WaterLeakageSensorTilt", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PUSH_NOTIFICATIONS_ON_MOVE),
                OnOffType.ON);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_ACOUSTIC_SIGNALS_ON_MOVE),
                OnOffType.ON);
    }

    @Test
    void testUpdateChannelsCommunicationQualityService() {
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
    void updateChannelsWaterLeakageSensorCheckServiceState() {
        JsonElement jsonObject = JsonParser
                .parseString("{\"@type\":\"waterLeakageSensorCheckState\",\"result\": \"OK\"}");
        getFixture().processUpdate("WaterLeakageSensorCheck", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_WATER_LEAKAGE_SENSOR_CHECK),
                new StringType("OK"));
    }

    @Test
    void testHandleCommandPushNotificationsNoPreviousState()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PUSH_NOTIFICATIONS_ON_MOVE),
                OnOffType.ON);
        verify(getBridgeHandler(), times(0)).putState(any(), any(), any());
    }

    @Test
    void testHandleCommandPushNotifications()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        JsonElement jsonObject = JsonParser.parseString(
                "{\"@type\":\"waterLeakageSensorTiltState\",\"pushNotificationState\": \"DISABLED\",\"acousticSignalState\": \"DISABLED\"}");
        getFixture().processUpdate("WaterLeakageSensorTilt", jsonObject);

        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PUSH_NOTIFICATIONS_ON_MOVE),
                OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("WaterLeakageSensorTilt"),
                waterLeakageTiltServiceStateCaptor.capture());
        WaterLeakageSensorTiltServiceState state = waterLeakageTiltServiceStateCaptor.getValue();
        assertSame(EnabledDisabledState.ENABLED, state.pushNotificationState);
        assertSame(EnabledDisabledState.DISABLED, state.acousticSignalState);
    }

    @Test
    void testHandleCommandAcousticSignals()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        JsonElement jsonObject = JsonParser.parseString(
                "{\"@type\":\"waterLeakageSensorTiltState\",\"pushNotificationState\": \"DISABLED\",\"acousticSignalState\": \"DISABLED\"}");
        getFixture().processUpdate("WaterLeakageSensorTilt", jsonObject);

        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_ACOUSTIC_SIGNALS_ON_MOVE),
                OnOffType.ON);
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("WaterLeakageSensorTilt"),
                waterLeakageTiltServiceStateCaptor.capture());
        WaterLeakageSensorTiltServiceState state = waterLeakageTiltServiceStateCaptor.getValue();
        assertSame(EnabledDisabledState.DISABLED, state.pushNotificationState);
        assertSame(EnabledDisabledState.ENABLED, state.acousticSignalState);
    }

    @Test
    void testHandleCommandPushNotificationsInvalidCommandType()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_PUSH_NOTIFICATIONS_ON_MOVE),
                new StringType("test"));
        verify(getBridgeHandler(), times(0)).putState(any(), any(), any());
    }

    @Test
    void testHandleCommandAcousticSignalsInvalidCommandType()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_ACOUSTIC_SIGNALS_ON_MOVE),
                new StringType("test"));
        verify(getBridgeHandler(), times(0)).putState(any(), any(), any());
    }

    @Test
    void testHandleCommandPushNotificationsInvalidChannel()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_POWER_SWITCH),
                OnOffType.ON);
        verify(getBridgeHandler(), times(0)).putState(any(), any(), any());
    }

    @Test
    void processMessageTiltDetected() {
        Message message = new Message();
        message.sourceType = Message.SOURCE_TYPE_DEVICE;
        MessageCode messageCode = new MessageCode();
        messageCode.name = WaterLeakageSensorHandler.MESSAGE_CODE_TILT_DETECTED;
        messageCode.category = "WARNING";
        message.messageCode = messageCode;

        getFixture().processMessage(message);

        verify(getCallback()).channelTriggered(getThing(),
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SENSOR_MOVED), "");
    }
}
