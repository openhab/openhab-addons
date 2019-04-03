/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.handler;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.util.Collections;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.hue.internal.FullConfig;
import org.openhab.binding.hue.internal.FullLight;
import org.openhab.binding.hue.internal.State.ColorMode;
import org.openhab.binding.hue.internal.StateUpdate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Tests for {@link HueLightHandler}.
 *
 * @author Oliver Libutzki - Initial contribution
 * @author Michael Grammling - Initial contribution
 * @author Markus Mazurczak - Added test for OSRAM Par16 50 TW bulbs
 * @author Andre Fuechsel - modified tests after introducing the generic thing types
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author Simon Kaufmann - migrated to plain Java test
 * @author Christoph Weitkamp - Added support for bulbs using CIE XY colormode only
 */
public class HueLightHandlerTest {

    private static final int MIN_COLOR_TEMPERATURE = 153;
    private static final int MAX_COLOR_TEMPERATURE = 500;
    private static final int COLOR_TEMPERATURE_RANGE = MAX_COLOR_TEMPERATURE - MIN_COLOR_TEMPERATURE;

    private static final String OSRAM_MODEL_TYPE = "PAR16 50 TW";
    private static final String OSRAM_MODEL_TYPE_ID = "PAR16_50_TW";

    private Gson gson;

    @Before
    public void setUp() {
        gson = new Gson();
    }

    @Test
    public void assertCommandForOsramPar16_50ForColorTemperatureChannelOn() {
        String expectedReply = "{\"on\" : true, \"bri\" : 254}";
        assertSendCommandForColorTempForPar16(OnOffType.ON, new HueLightState(OSRAM_MODEL_TYPE), expectedReply);
    }

    @Test
    public void assertCommandForOsramPar16_50ForColorTemperatureChannelOff() {
        String expectedReply = "{\"on\" : false, \"transitiontime\" : 0}";
        assertSendCommandForColorTempForPar16(OnOffType.OFF, new HueLightState(OSRAM_MODEL_TYPE), expectedReply);
    }

    @Test
    public void assertCommandForOsramPar16_50ForBrightnessChannelOn() {
        String expectedReply = "{\"on\" : true, \"bri\" : 254}";
        assertSendCommandForBrightnessForPar16(OnOffType.ON, new HueLightState(OSRAM_MODEL_TYPE), expectedReply);
    }

    @Test
    public void assertCommandForOsramPar16_50ForBrightnessChannelOff() {
        String expectedReply = "{\"on\" : false, \"transitiontime\" : 0}";
        assertSendCommandForBrightnessForPar16(OnOffType.OFF, new HueLightState(OSRAM_MODEL_TYPE), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelOn() {
        String expectedReply = "{\"on\" : true}";
        assertSendCommandForColor(OnOffType.ON, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannelOn() {
        String expectedReply = "{\"on\" : true}";
        assertSendCommandForColorTemp(OnOffType.ON, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelOff() {
        String expectedReply = "{\"on\" : false}";
        assertSendCommandForColor(OnOffType.OFF, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannelOff() {
        String expectedReply = "{\"on\" : false}";
        assertSendCommandForColorTemp(OnOffType.OFF, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannel0Percent() {
        String expectedReply = "{\"ct\" : 153, \"transitiontime\" : 4}";
        assertSendCommandForColorTemp(new PercentType(0), new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannel50Percent() {
        String expectedReply = "{\"ct\" : 327, \"transitiontime\" : 4}";
        assertSendCommandForColorTemp(new PercentType(50), new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorTemperatureChannel1000Percent() {
        String expectedReply = "{\"ct\" : 500, \"transitiontime\" : 4}";
        assertSendCommandForColorTemp(new PercentType(100), new HueLightState(), expectedReply);
    }

    @Test
    public void assertPercentageValueOfColorTemperatureWhenCt153() {
        int expectedReply = 0;
        asserttoColorTemperaturePercentType(153, expectedReply);
    }

    @Test
    public void assertPercentageValueOfColorTemperatureWhenCt326() {
        int expectedReply = 50;
        asserttoColorTemperaturePercentType(326, expectedReply);
    }

    @Test
    public void assertPercentageValueOfColorTemperatureWhenCt500() {
        int expectedReply = 100;
        asserttoColorTemperaturePercentType(500, expectedReply);
    }

    @Test
    public void assertCommandForColorChannel0Percent() {
        String expectedReply = "{\"on\" : false, \"transitiontime\" : 4}";
        assertSendCommandForColor(new PercentType(0), new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannel50Percent() {
        String expectedReply = "{\"bri\" : 127, \"on\" : true, \"transitiontime\" : 4}";
        assertSendCommandForColor(new PercentType(50), new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannel100Percent() {
        String expectedReply = "{\"bri\" : 254, \"on\" : true, \"transitiontime\" : 4}";
        assertSendCommandForColor(new PercentType(100), new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelBlack() {
        String expectedReply = "{\"on\" : false}";
        assertSendCommandForColor(HSBType.BLACK, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelRed() {
        String expectedReply = "{\"bri\" : 254, \"sat\" : 254, \"hue\" : 0, \"transitiontime\" : 4}";
        assertSendCommandForColor(HSBType.RED, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelGreen() {
        String expectedReply = "{\"bri\" : 254, \"sat\" : 254, \"hue\" : 21845, \"transitiontime\" : 4}";
        assertSendCommandForColor(HSBType.GREEN, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelBlue() {
        String expectedReply = "{\"bri\" : 254, \"sat\" : 254, \"hue\" : 43690, \"transitiontime\" : 4}";
        assertSendCommandForColor(HSBType.BLUE, new HueLightState(), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelWhite() {
        String expectedReply = "{\"bri\" : 254, \"sat\" : 0, \"hue\" : 0, \"transitiontime\" : 4}";
        assertSendCommandForColor(HSBType.WHITE, new HueLightState(), expectedReply);
    }

    @Test
    public void assertXYCommandForColorChannelBlack() {
        String expectedReply = "{\"on\" : false}";
        assertSendCommandForColor(HSBType.BLACK, new HueLightState().colormode(ColorMode.XY), expectedReply);
    }

    @Test
    public void assertXYCommandForColorChannelWhite() {
        String expectedReply = "{\"xy\" : [ 0.31271592 , 0.32900152 ], \"bri\" : 254, \"transitiontime\" : 4}";
        assertSendCommandForColor(HSBType.WHITE, new HueLightState().colormode(ColorMode.XY), expectedReply);
    }

    @Test
    public void assertXYCommandForColorChannelColorful() {
        String expectedReply = "{\"xy\" : [ 0.16969365 , 0.12379659 ], \"bri\" : 127, \"transitiontime\" : 4}";
        assertSendCommandForColor(new HSBType("220,90,50"), new HueLightState().colormode(ColorMode.XY), expectedReply);
    }

    @Test
    public void asserCommandForColorChannelIncrease() {
        HueLightState currentState = new HueLightState().bri(1).on(false);
        String expectedReply = "{\"bri\" : 30, \"on\" : true, \"transitiontime\" : 4}";
        assertSendCommandForColor(IncreaseDecreaseType.INCREASE, currentState, expectedReply);

        currentState.bri(200).on(true);
        expectedReply = "{\"bri\" : 230, \"transitiontime\" : 4}";
        assertSendCommandForColor(IncreaseDecreaseType.INCREASE, currentState, expectedReply);

        currentState.bri(230);
        expectedReply = "{\"bri\" : 254, \"transitiontime\" : 4}";
        assertSendCommandForColor(IncreaseDecreaseType.INCREASE, currentState, expectedReply);
    }

    @Test
    public void asserCommandForColorChannelDecrease() {
        HueLightState currentState = new HueLightState().bri(200);
        String expectedReply = "{\"bri\" : 170, \"transitiontime\" : 4}";
        assertSendCommandForColor(IncreaseDecreaseType.DECREASE, currentState, expectedReply);

        currentState.bri(20);
        expectedReply = "{\"on\" : false, \"transitiontime\" : 4}";
        assertSendCommandForColor(IncreaseDecreaseType.DECREASE, currentState, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannel50Percent() {
        HueLightState currentState = new HueLightState();
        String expectedReply = "{\"bri\" : 127, \"on\" : true, \"transitiontime\" : 4}";
        assertSendCommandForBrightness(new PercentType(50), currentState, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannelIncrease() {
        HueLightState currentState = new HueLightState().bri(1).on(false);
        String expectedReply = "{\"bri\" : 30, \"on\" : true, \"transitiontime\" : 4}";
        assertSendCommandForBrightness(IncreaseDecreaseType.INCREASE, currentState, expectedReply);

        currentState.bri(200).on(true);
        expectedReply = "{\"bri\" : 230, \"transitiontime\" : 4}";
        assertSendCommandForBrightness(IncreaseDecreaseType.INCREASE, currentState, expectedReply);

        currentState.bri(230);
        expectedReply = "{\"bri\" : 254, \"transitiontime\" : 4}";
        assertSendCommandForBrightness(IncreaseDecreaseType.INCREASE, currentState, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannelDecrease() {
        HueLightState currentState = new HueLightState().bri(200);
        String expectedReply = "{\"bri\" : 170, \"transitiontime\" : 4}";
        assertSendCommandForBrightness(IncreaseDecreaseType.DECREASE, currentState, expectedReply);

        currentState.bri(20);
        expectedReply = "{\"on\" : false, \"transitiontime\" : 4}";
        assertSendCommandForBrightness(IncreaseDecreaseType.DECREASE, currentState, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannelOff() {
        HueLightState currentState = new HueLightState();
        String expectedReply = "{\"on\" : false}";
        assertSendCommandForBrightness(OnOffType.OFF, currentState, expectedReply);
    }

    @Test
    public void assertCommandForBrightnessChannelOn() {
        HueLightState currentState = new HueLightState();
        String expectedReply = "{\"on\" : true}";
        assertSendCommandForBrightness(OnOffType.ON, currentState, expectedReply);
    }

    @Test
    public void assertCommandForAlertChannel() {
        HueLightState currentState = new HueLightState().alert("NONE");
        String expectedReply = "{\"alert\" : \"none\"}";
        assertSendCommandForAlert(new StringType("NONE"), currentState, expectedReply);

        currentState.alert("NONE");
        expectedReply = "{\"alert\" : \"select\"}";
        assertSendCommandForAlert(new StringType("SELECT"), currentState, expectedReply);

        currentState.alert("LSELECT");
        expectedReply = "{\"alert\" : \"lselect\"}";
        assertSendCommandForAlert(new StringType("LSELECT"), currentState, expectedReply);
    }

    @Test
    public void assertCommandForEffectChannel() {
        HueLightState currentState = new HueLightState().effect("ON");
        String expectedReply = "{\"effect\" : \"colorloop\"}";
        assertSendCommandForEffect(OnOffType.ON, currentState, expectedReply);

        currentState.effect("OFF");
        expectedReply = "{\"effect\" : \"none\"}";
        assertSendCommandForEffect(OnOffType.OFF, currentState, expectedReply);
    }

    private void assertSendCommandForColorTempForPar16(Command command, HueLightState currentState,
            String expectedReply) {
        assertSendCommand(CHANNEL_COLORTEMPERATURE, command, currentState, expectedReply, OSRAM_MODEL_TYPE_ID, "OSRAM");
    }

    private void assertSendCommandForBrightnessForPar16(Command command, HueLightState currentState,
            String expectedReply) {
        assertSendCommand(CHANNEL_BRIGHTNESS, command, currentState, expectedReply, OSRAM_MODEL_TYPE_ID, "OSRAM");
    }

    private void assertSendCommandForColor(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_COLOR, command, currentState, expectedReply);
    }

    private void assertSendCommandForColorTemp(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_COLORTEMPERATURE, command, currentState, expectedReply);
    }

    private void asserttoColorTemperaturePercentType(int ctValue, int expectedPercent) {
        int percent = (int) Math.round(((ctValue - MIN_COLOR_TEMPERATURE) * 100.0) / COLOR_TEMPERATURE_RANGE);
        assertEquals(percent, expectedPercent);
    }

    private void assertSendCommandForBrightness(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_BRIGHTNESS, command, currentState, expectedReply);
    }

    private void assertSendCommandForAlert(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_ALERT, command, currentState, expectedReply);
    }

    private void assertSendCommandForEffect(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_EFFECT, command, currentState, expectedReply);
    }

    private void assertSendCommand(String channel, Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(channel, command, currentState, expectedReply, "LCT001", "Philips");
    }

    private void assertSendCommand(String channel, Command command, HueLightState currentState, String expectedReply,
            String expectedModel, String expectedVendor) {
        FullLight light = gson.fromJson(currentState.toString(), FullConfig.class).getLights().get(0);

        Bridge mockBridge = mock(Bridge.class);
        when(mockBridge.getStatus()).thenReturn(ThingStatus.ONLINE);

        Thing mockThing = mock(Thing.class);
        when(mockThing.getConfiguration()).thenReturn(new Configuration(Collections.singletonMap(LIGHT_ID, "1")));

        HueClient mockClient = mock(HueClient.class);
        when(mockClient.getLightById(any())).thenReturn(light);

        HueLightHandler hueLightHandler = new HueLightHandler(mockThing) {
            @Override
            protected synchronized HueClient getHueClient() {
                return mockClient;
            }

            @Override
            protected Bridge getBridge() {
                return mockBridge;
            }
        };
        hueLightHandler.initialize();

        verify(mockThing).setProperty(eq(Thing.PROPERTY_MODEL_ID), eq(expectedModel));
        verify(mockThing).setProperty(eq(Thing.PROPERTY_VENDOR), eq(expectedVendor));

        hueLightHandler.handleCommand(new ChannelUID(new ThingUID("hue::test"), channel), command);

        ArgumentCaptor<StateUpdate> captorStateUpdate = ArgumentCaptor.forClass(StateUpdate.class);
        verify(mockClient).updateLightState(any(FullLight.class), captorStateUpdate.capture());
        assertJson(expectedReply, captorStateUpdate.getValue().toJson());
    }

    private void assertJson(String expected, String actual) {
        JsonParser parser = new JsonParser();
        JsonElement jsonExpected = parser.parse(expected);
        JsonElement jsonActual = parser.parse(actual);
        assertEquals(jsonExpected, jsonActual);
    }

}
