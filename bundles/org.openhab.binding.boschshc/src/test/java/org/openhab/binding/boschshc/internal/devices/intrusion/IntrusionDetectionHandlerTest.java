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
        JsonElement jsonObject = JsonParser.parseString("""
                {
                     "@type": "systemState",
                     "systemAvailability": {
                         "@type": "systemAvailabilityState",
                         "available": true,
                         "deleted": false
                     },
                     "armingState": {
                         "@type": "armingState",
                         "state": "SYSTEM_DISARMED",
                         "deleted": false
                     },
                     "alarmState": {
                         "@type": "alarmState",
                         "value": "ALARM_OFF",
                         "incidents": [],
                         "deleted": false
                     },
                     "activeConfigurationProfile": {
                         "@type": "activeConfigurationProfile",
                         "deleted": false
                     },
                     "securityGapState": {
                         "@type": "securityGapState",
                         "securityGaps": [],
                         "deleted": false
                     },
                     "deleted": false
                 }
                """);
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
        JsonElement jsonObject = JsonParser.parseString("""
                {
                   "@type": "intrusionDetectionControlState",
                   "activeProfile": "0",
                   "alarmActivationDelayTime": 30,
                   "actuators": [
                     {
                       "readonly": false,
                       "active": true,
                       "id": "intrusion:video"
                     },
                     {
                       "readonly": false,
                       "active": false,
                       "id": "intrusion:siren"
                     }
                   ],
                   "remainingTimeUntilArmed": 29559,
                   "armActivationDelayTime": 30,
                   "triggers": [
                     {
                       "readonly": false,
                       "active": true,
                       "id": "hdm:ZigBee:000d6f0012f02378"
                     }
                   ],
                   "value": "SYSTEM_ARMING"
                 }\
                """);
        getFixture().processUpdate("IntrusionDetectionControl", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_ARMING_STATE),
                new StringType("SYSTEM_ARMING"));
    }

    @Test
    void testUpdateChannelsSurveillanceAlarmState() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                   "@type": "surveillanceAlarmState",
                   "incidents": [
                     {
                       "triggerName": "Motion Detector",
                       "locationId": "hz_5",
                       "location": "Living Room",
                       "id": "hdm:ZigBee:000d6f0012f02342",
                       "time": 1652615755336,
                       "type": "INTRUSION"
                     }
                   ],
                   "value": "ALARM_ON"
                 }\
                """);
        getFixture().processUpdate("SurveillanceAlarm", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_ALARM_STATE),
                new StringType("ALARM_ON"));
    }
}
