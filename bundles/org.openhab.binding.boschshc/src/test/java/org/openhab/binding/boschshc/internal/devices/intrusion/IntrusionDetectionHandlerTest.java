/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.intrusion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.AbstractBoschSHCHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.services.intrusion.actions.arm.dto.ArmActionRequest;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit test for {@link IntrusionDetectionHandler}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class IntrusionDetectionHandlerTest extends AbstractBoschSHCHandlerTest<IntrusionDetectionHandler> {

    private @Captor @NonNullByDefault({}) ArgumentCaptor<ArmActionRequest> armActionRequestCaptor;

    @Override
    protected IntrusionDetectionHandler createFixture() {
        return new IntrusionDetectionHandler(getThing());
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_INTRUSION_DETECTION_SYSTEM;
    }

    @Test
    void testHandleCommandArmAction() throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_ARM_ACTION),
                new StringType("0"));
        verify(getBridgeHandler()).postAction(eq("intrusion/actions/arm"), armActionRequestCaptor.capture());
        ArmActionRequest armRequest = armActionRequestCaptor.getValue();
        assertEquals("0", armRequest.profileId);
    }

    @Test
    void testHandleCommandDisarmAction() throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_DISARM_ACTION),
                OnOffType.ON);
        verify(getBridgeHandler()).postAction("intrusion/actions/disarm");
    }

    @Test
    void testHandleCommandMuteAction() throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_MUTE_ACTION),
                OnOffType.ON);
        verify(getBridgeHandler()).postAction("intrusion/actions/mute");
    }

    @Test
    void testUpdateChannelsIntrusionDetectionSystemState() {
        JsonElement jsonObject = JsonParser.parseString("{\n" + "     \"@type\": \"systemState\",\n"
                + "     \"systemAvailability\": {\n" + "         \"@type\": \"systemAvailabilityState\",\n"
                + "         \"available\": true,\n" + "         \"deleted\": false\n" + "     },\n"
                + "     \"armingState\": {\n" + "         \"@type\": \"armingState\",\n"
                + "         \"state\": \"SYSTEM_DISARMED\",\n" + "         \"deleted\": false\n" + "     },\n"
                + "     \"alarmState\": {\n" + "         \"@type\": \"alarmState\",\n"
                + "         \"value\": \"ALARM_OFF\",\n" + "         \"incidents\": [],\n"
                + "         \"deleted\": false\n" + "     },\n" + "     \"activeConfigurationProfile\": {\n"
                + "         \"@type\": \"activeConfigurationProfile\",\n" + "         \"deleted\": false\n"
                + "     },\n" + "     \"securityGapState\": {\n" + "         \"@type\": \"securityGapState\",\n"
                + "         \"securityGaps\": [],\n" + "         \"deleted\": false\n" + "     },\n"
                + "     \"deleted\": false\n" + " }\n");
        getFixture().processUpdate(BoschSHCBindingConstants.SERVICE_INTRUSION_DETECTION, jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_SYSTEM_AVAILABILITY),
                OnOffType.ON);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_ARMING_STATE),
                new StringType("SYSTEM_DISARMED"));
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_ALARM_STATE),
                new StringType("ALARM_OFF"));
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_ACTIVE_CONFIGURATION_PROFILE),
                new StringType(null));
    }

    @Test
    void testUpdateChannelsIntrusionDetectionControlState() {
        JsonElement jsonObject = JsonParser.parseString("{\n" + "   \"@type\": \"intrusionDetectionControlState\",\n"
                + "   \"activeProfile\": \"0\",\n" + "   \"alarmActivationDelayTime\": 30,\n" + "   \"actuators\": [\n"
                + "     {\n" + "       \"readonly\": false,\n" + "       \"active\": true,\n"
                + "       \"id\": \"intrusion:video\"\n" + "     },\n" + "     {\n" + "       \"readonly\": false,\n"
                + "       \"active\": false,\n" + "       \"id\": \"intrusion:siren\"\n" + "     }\n" + "   ],\n"
                + "   \"remainingTimeUntilArmed\": 29559,\n" + "   \"armActivationDelayTime\": 30,\n"
                + "   \"triggers\": [\n" + "     {\n" + "       \"readonly\": false,\n" + "       \"active\": true,\n"
                + "       \"id\": \"hdm:ZigBee:000d6f0012f02378\"\n" + "     }\n" + "   ],\n"
                + "   \"value\": \"SYSTEM_ARMING\"\n" + " }");
        getFixture().processUpdate("IntrusionDetectionControl", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_ARMING_STATE),
                new StringType("SYSTEM_ARMING"));
    }

    @Test
    void testUpdateChannelsSurveillanceAlarmState() {
        JsonElement jsonObject = JsonParser.parseString("{\n" + "   \"@type\": \"surveillanceAlarmState\",\n"
                + "   \"incidents\": [\n" + "     {\n" + "       \"triggerName\": \"Motion Detector\",\n"
                + "       \"locationId\": \"hz_5\",\n" + "       \"location\": \"Living Room\",\n"
                + "       \"id\": \"hdm:ZigBee:000d6f0012f02342\",\n" + "       \"time\": 1652615755336,\n"
                + "       \"type\": \"INTRUSION\"\n" + "     }\n" + "   ],\n" + "   \"value\": \"ALARM_ON\"\n" + " }");
        getFixture().processUpdate("SurveillanceAlarm", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_ALARM_STATE),
                new StringType("ALARM_ON"));
    }
}
