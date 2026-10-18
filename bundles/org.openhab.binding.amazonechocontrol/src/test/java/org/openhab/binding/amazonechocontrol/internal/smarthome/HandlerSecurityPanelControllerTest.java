/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.smarthome;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.binding.amazonechocontrol.internal.smarthome.InterfaceHandler.UpdateChannelResult;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests the {@link HandlerSecurityPanelController} against the two value shapes Amazon sends.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class HandlerSecurityPanelControllerTest {
    // shaped like the trace in openhab/openhab-addons#20091, armState a string and alarms objects, values varied
    private static final String ARM_STATE_AS_STRING = "{\"namespace\":\"Alexa.SecurityPanelController\","
            + "\"name\":\"armState\",\"value\":\"DISARMED\",\"timeOfSample\":\"2026-01-17T16:52:06Z\"}";
    private static final String ARM_STATE_AS_OBJECT = "{\"namespace\":\"Alexa.SecurityPanelController\","
            + "\"name\":\"armState\",\"value\":{\"value\":\"ARMED_AWAY\"},\"timeOfSample\":\"2026-01-17T16:52:06Z\"}";
    private static final String FIRE_ALARM_OK = "{\"namespace\":\"Alexa.SecurityPanelController\","
            + "\"name\":\"fireAlarm\",\"value\":{\"value\":\"OK\"},\"timeOfSample\":\"2026-01-17T16:52:06Z\"}";
    private static final String BURGLARY_ALARM_TRIGGERED = "{\"namespace\":\"Alexa.SecurityPanelController\","
            + "\"name\":\"burglaryAlarm\",\"value\":{\"value\":\"ALARM\"},\"timeOfSample\":\"2026-01-17T16:52:06Z\"}";
    private static final String CARBON_MONOXIDE_ALARM_OK = "{\"namespace\":\"Alexa.SecurityPanelController\","
            + "\"name\":\"carbonMonoxideAlarm\",\"value\":{\"value\":\"OK\"},"
            + "\"timeOfSample\":\"2026-01-17T16:52:06Z\"}";

    private final SmartHomeDeviceHandler deviceHandler = mock(SmartHomeDeviceHandler.class);
    private final HandlerSecurityPanelController handler = new HandlerSecurityPanelController(deviceHandler);

    @Test
    public void armStateSentAsStringUpdatesEveryChannel() {
        handler.updateChannels(HandlerSecurityPanelController.INTERFACE,
                states(ARM_STATE_AS_STRING, FIRE_ALARM_OK, BURGLARY_ALARM_TRIGGERED, CARBON_MONOXIDE_ALARM_OK),
                new UpdateChannelResult());

        verify(deviceHandler).updateState("armState", new StringType("DISARMED"));
        verify(deviceHandler).updateState("fireAlarm", OpenClosedType.OPEN);
        verify(deviceHandler).updateState("burglaryAlarm", OpenClosedType.CLOSED);
        verify(deviceHandler).updateState("carbonMonoxideAlarm", OpenClosedType.OPEN);
        verify(deviceHandler).updateState("waterAlarm", UnDefType.UNDEF);
    }

    @Test
    public void armStateSentAsObjectStillUpdatesEveryChannel() {
        handler.updateChannels(HandlerSecurityPanelController.INTERFACE, states(ARM_STATE_AS_OBJECT, FIRE_ALARM_OK),
                new UpdateChannelResult());

        verify(deviceHandler).updateState("armState", new StringType("ARMED_AWAY"));
        verify(deviceHandler).updateState("fireAlarm", OpenClosedType.OPEN);
        verify(deviceHandler).updateState("burglaryAlarm", UnDefType.UNDEF);
    }

    @Test
    public void firstArmStateWins() {
        handler.updateChannels(HandlerSecurityPanelController.INTERFACE,
                states(ARM_STATE_AS_STRING, ARM_STATE_AS_OBJECT), new UpdateChannelResult());

        verify(deviceHandler).updateState("armState", new StringType("DISARMED"));
    }

    @Test
    public void statesWithoutValueAreSkipped() {
        handler.updateChannels(HandlerSecurityPanelController.INTERFACE, states("{\"name\":\"armState\"}",
                "{\"name\":\"armState\",\"value\":null}", "{\"name\":\"burglaryAlarm\",\"value\":{}}", FIRE_ALARM_OK),
                new UpdateChannelResult());

        verify(deviceHandler).updateState("armState", UnDefType.UNDEF);
        verify(deviceHandler).updateState("burglaryAlarm", UnDefType.UNDEF);
        verify(deviceHandler).updateState("fireAlarm", OpenClosedType.OPEN);
    }

    private static List<JsonObject> states(String... json) {
        return Arrays.stream(json).map(s -> JsonParser.parseString(s).getAsJsonObject()).toList();
    }
}
