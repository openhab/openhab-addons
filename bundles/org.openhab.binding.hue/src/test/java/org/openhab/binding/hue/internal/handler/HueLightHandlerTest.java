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
package org.openhab.binding.hue.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.hue.internal.api.dto.clip1.FullConfig;
import org.openhab.binding.hue.internal.api.dto.clip1.FullLight;
import org.openhab.binding.hue.internal.api.dto.clip1.State.ColorMode;
import org.openhab.binding.hue.internal.api.dto.clip1.StateUpdate;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;

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
 * @author Jacob Laursen - Add workaround for LK Wiser products
 */
@NonNullByDefault
public class HueLightHandlerTest {

    private static final int MIN_COLOR_TEMPERATURE = 153;
    private static final int MAX_COLOR_TEMPERATURE = 500;
    private static final int COLOR_TEMPERATURE_RANGE = MAX_COLOR_TEMPERATURE - MIN_COLOR_TEMPERATURE;

    private static final String OSRAM = "OSRAM";
    private static final String OSRAM_MODEL_TYPE = HueLightHandler.OSRAM_PAR16_50_TW_MODEL_ID;
    private static final String OSRAM_MODEL_TYPE_ID = HueLightHandler.OSRAM_PAR16_50_TW_MODEL_ID;

    private final Gson gson = new Gson();

    @Test
    public void assertCommandForOsramPar1650ForColorTemperatureChannelOn() {
        String expectedReply = "{\"on\" : true, \"bri\" : 254}";
        assertSendCommandForColorTempForPar16(OnOffType.ON, new HueLightState(OSRAM_MODEL_TYPE, OSRAM), expectedReply);
    }

    @Test
    public void assertCommandForOsramPar1650ForColorTemperatureChannelOff() {
        String expectedReply = "{\"on\" : false, \"transitiontime\" : 0}";
        assertSendCommandForColorTempForPar16(OnOffType.OFF, new HueLightState(OSRAM_MODEL_TYPE, OSRAM), expectedReply);
    }

    @Test
    public void assertCommandForOsramPar1650ForBrightnessChannelOn() {
        String expectedReply = "{\"on\" : true, \"bri\" : 254}";
        assertSendCommandForBrightnessForPar16(OnOffType.ON, new HueLightState(OSRAM_MODEL_TYPE, OSRAM), expectedReply);
    }

    @Test
    public void assertCommandForOsramPar1650ForBrightnessChannelOff() {
        String expectedReply = "{\"on\" : false, \"transitiontime\" : 0}";
        assertSendCommandForBrightnessForPar16(OnOffType.OFF, new HueLightState(OSRAM_MODEL_TYPE, OSRAM),
                expectedReply);
    }

    @Test
    public void assertCommandForLkWiserForBrightnessChannelOff() {
        final String expectedReply = "{\"on\" : false, \"transitiontime\" : 0}";
        final String vendor = "Schneider Electric";
        assertSendCommand(CHANNEL_BRIGHTNESS, OnOffType.OFF,
                new HueLightState(HueLightHandler.LK_WISER_MODEL_ID, vendor), expectedReply,
                HueLightHandler.LK_WISER_MODEL_ID, vendor);
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
    public void assertDecimalTypeCommandForColorTemperatureAbsChannel6500Kelvin() {
        String expectedReply = "{\"ct\" : 153, \"transitiontime\" : 4}";
        assertSendCommandForColorTempAbs(new DecimalType(6500), new HueLightState(), expectedReply);
    }

    @Test
    public void assertDecimalTypeCommandForColorTemperatureAbsChannel4500Kelvin() {
        String expectedReply = "{\"ct\" : 222, \"transitiontime\" : 4}";
        assertSendCommandForColorTempAbs(new DecimalType(4500), new HueLightState(), expectedReply);
    }

    @Test
    public void assertDecimalTypeCommandForColorTemperatureAbsChannel2000Kelvin() {
        String expectedReply = "{\"ct\" : 500, \"transitiontime\" : 4}";
        assertSendCommandForColorTempAbs(new DecimalType(2000), new HueLightState(), expectedReply);
    }

    @Test
    public void assertQuantityTypeCommandForColorTemperatureAbsChannel6500Kelvin() {
        String expectedReply = "{\"ct\" : 153, \"transitiontime\" : 4}";
        assertSendCommandForColorTempAbs(new QuantityType<>(6500, Units.KELVIN), new HueLightState(), expectedReply);
    }

    @Test
    public void assertQuantityTypeCommandForColorTemperatureAbsChannel4500Kelvin() {
        String expectedReply = "{\"ct\" : 222, \"transitiontime\" : 4}";
        assertSendCommandForColorTempAbs(new QuantityType<>(4500, Units.KELVIN), new HueLightState(), expectedReply);
    }

    @Test
    public void assertQuantityTypeCommandForColorTemperatureAbsChannel2000Kelvin() {
        String expectedReply = "{\"ct\" : 500, \"transitiontime\" : 4}";
        assertSendCommandForColorTempAbs(new QuantityType<>(2000, Units.KELVIN), new HueLightState(), expectedReply);
    }

    @Test
    public void assertQuantityTypeCommandForColorTemperatureAbsChannel500Mired() {
        String expectedReply = "{\"ct\" : 500, \"transitiontime\" : 4}";
        assertSendCommandForColorTempAbs(new QuantityType<>(500, Units.MIRED), new HueLightState(), expectedReply);
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
        String expectedReply = "{\"xy\" : [ 0.3227 , 0.32900003 ], \"bri\" : 254, \"transitiontime\" : 4}";
        assertSendCommandForColor(HSBType.WHITE, new HueLightState().colormode(ColorMode.XY), expectedReply);
    }

    @Test
    public void assertXYCommandForColorChannelColorful() {
        String expectedReply = "{\"xy\" : [ 0.14649999 , 0.115600005 ], \"bri\" : 127, \"transitiontime\" : 4}";
        assertSendCommandForColor(new HSBType("220,90,50"), new HueLightState().colormode(ColorMode.XY), expectedReply);
    }

    @Test
    public void assertCommandForColorChannelIncrease() {
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
    public void assertCommandForColorChannelDecrease() {
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
        assertSendCommand(CHANNEL_COLORTEMPERATURE, command, currentState, expectedReply, OSRAM_MODEL_TYPE_ID, OSRAM);
    }

    private void assertSendCommandForBrightnessForPar16(Command command, HueLightState currentState,
            String expectedReply) {
        assertSendCommand(CHANNEL_BRIGHTNESS, command, currentState, expectedReply, OSRAM_MODEL_TYPE_ID, OSRAM);
    }

    private void assertSendCommandForColor(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_COLOR, command, currentState, expectedReply);
    }

    private void assertSendCommandForColorTemp(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_COLORTEMPERATURE, command, currentState, expectedReply);
    }

    private void assertSendCommandForColorTempAbs(Command command, HueLightState currentState, String expectedReply) {
        assertSendCommand(CHANNEL_COLORTEMPERATURE_ABS, command, currentState, expectedReply);
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

    @SuppressWarnings("null")
    private void assertSendCommand(String channel, Command command, HueLightState currentState, String expectedReply,
            String expectedModel, String expectedVendor) {
        FullLight light = gson.fromJson(currentState.toString(), FullConfig.class).getLights().get(0);

        Bridge mockBridge = mock(Bridge.class);
        when(mockBridge.getStatus()).thenReturn(ThingStatus.ONLINE);

        Thing mockThing = mock(Thing.class);
        when(mockThing.getConfiguration()).thenReturn(new Configuration(Map.of(LIGHT_ID, "1")));

        HueClient mockClient = mock(HueClient.class);
        when(mockClient.getLightById(any())).thenReturn(light);

        long fadeTime = 400;

        HueLightHandler hueLightHandler = new HueLightHandler(mockThing, mock(HueStateDescriptionProvider.class)) {
            @Override
            protected synchronized @Nullable HueClient getHueClient() {
                return mockClient;
            }

            @Override
            protected @Nullable Bridge getBridge() {
                return mockBridge;
            }
        };
        hueLightHandler.setCallback(mock(ThingHandlerCallback.class));
        hueLightHandler.initialize();

        verify(mockThing).setProperty(eq(Thing.PROPERTY_MODEL_ID), eq(expectedModel));
        verify(mockThing).setProperty(eq(Thing.PROPERTY_VENDOR), eq(expectedVendor));

        hueLightHandler.handleCommand(new ChannelUID(new ThingUID("hue::test"), channel), command);

        ArgumentCaptor<StateUpdate> captorStateUpdate = ArgumentCaptor.forClass(StateUpdate.class);
        verify(mockClient).updateLightState(any(LightStatusListener.class), any(FullLight.class),
                captorStateUpdate.capture(), eq(fadeTime));
        assertJson(expectedReply, captorStateUpdate.getValue().toJson());
    }

    private void assertJson(String expected, String actual) {
        JsonElement jsonExpected = JsonParser.parseString(expected);
        JsonElement jsonActual = JsonParser.parseString(actual);
        assertEquals(jsonExpected, jsonActual);
    }
}
