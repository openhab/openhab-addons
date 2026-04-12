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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.binding.amazonechocontrol.internal.smarthome.InterfaceHandler.UpdateChannelResult;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link HandlerSecurityPanelControllerTest} tests {@link HandlerSecurityPanelController}
 *
 * @author openHAB contributors - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class HandlerSecurityPanelControllerTest {

    @Mock
    @NonNullByDefault({})
    private SmartHomeDeviceHandler handler;

    private JsonObject stateObject(String name, String valueJson) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", name);
        obj.add("value", JsonParser.parseString(valueJson));
        return obj;
    }

    @Test
    public void testArmStatePrimitive() {
        HandlerSecurityPanelController sut = new HandlerSecurityPanelController(handler);
        List<JsonObject> states = List.of(stateObject("armState", "\"DISARMED\""),
                stateObject("fireAlarm", "{\"value\":\"OK\"}"), stateObject("burglaryAlarm", "{\"value\":\"OK\"}"),
                stateObject("carbonMonoxideAlarm", "{\"value\":\"OK\"}"));

        sut.updateChannels(HandlerSecurityPanelController.INTERFACE, states, new UpdateChannelResult());

        verify(handler).updateState(eq("armState"), eq(new StringType("DISARMED")));
        verify(handler).updateState(eq("fireAlarm"), eq(OpenClosedType.OPEN));
        verify(handler).updateState(eq("burglaryAlarm"), eq(OpenClosedType.OPEN));
        verify(handler).updateState(eq("carbonMonoxideAlarm"), eq(OpenClosedType.OPEN));
    }

    @Test
    public void testArmStateAlarm() {
        HandlerSecurityPanelController sut = new HandlerSecurityPanelController(handler);
        List<JsonObject> states = List.of(stateObject("armState", "\"ARMED_AWAY\""),
                stateObject("burglaryAlarm", "{\"value\":\"ALARM\"}"));

        sut.updateChannels(HandlerSecurityPanelController.INTERFACE, states, new UpdateChannelResult());

        verify(handler).updateState(eq("armState"), eq(new StringType("ARMED_AWAY")));
        verify(handler).updateState(eq("burglaryAlarm"), eq(OpenClosedType.CLOSED));
    }
}
