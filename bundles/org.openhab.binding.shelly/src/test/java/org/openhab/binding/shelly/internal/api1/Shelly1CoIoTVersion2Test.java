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
package org.openhab.binding.shelly.internal.api1;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.handler.ShellyLightModel.RGBX.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.CoIotDescrBlk;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.CoIotDescrSen;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.CoIotSensor;
import org.openhab.binding.shelly.internal.handler.LightModelAccessor;
import org.openhab.binding.shelly.internal.handler.LightModelAccessor.LightModels;
import org.openhab.binding.shelly.internal.handler.ShellyLightHandler;
import org.openhab.binding.shelly.internal.handler.ShellyLightModel;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.State;

/**
 * Tests for {@link Shelly1CoIoTVersion2#handleStatusUpdate}, specifically the roller position ("1103") handling:
 * the device keeps reporting the pre-move position while the roller is moving, which must not be published as a
 * channel flicker.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly1CoIoTVersion2Test {
    private static final double STEP = 10.0;

    private Shelly1CoIoTVersion2 newProtocol() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(new ShellyDeviceProfile(THING_TYPE_SHELLY25_ROLLER));
        when(handler.getApi()).thenReturn(mock(ShellyApiInterface.class));
        Map<String, CoIotDescrBlk> blkMap = new HashMap<>();
        Map<String, CoIotDescrSen> sensorMap = new HashMap<>();
        return new Shelly1CoIoTVersion2("test", handler, blkMap, sensorMap);
    }

    private CoIotSensor rollerStateSensor(String state) {
        CoIotSensor s = new CoIotSensor();
        s.id = "1102";
        s.valueStr = state;
        return s;
    }

    private CoIotSensor rollerPosSensor(double value) {
        CoIotSensor s = new CoIotSensor();
        s.id = "1103";
        s.value = value;
        return s;
    }

    private CoIotDescrSen rollerPosDesc() {
        CoIotDescrSen sen = new CoIotDescrSen();
        sen.id = "1103";
        sen.desc = "rollerPos";
        sen.type = "S";
        sen.links = "";
        return sen;
    }

    @Test
    void rollerPositionSkippedWhileMoving() {
        Shelly1CoIoTVersion2 v2 = newProtocol();
        CoIotSensor posSensor = rollerPosSensor(0);
        List<CoIotSensor> sensorUpdates = List.of(rollerStateSensor("open"), posSensor);
        Map<String, State> updates = new HashMap<>();
        LightModels lightModel = mock(LightModelAccessor.LightModels.class);

        v2.handleStatusUpdate(sensorUpdates, rollerPosDesc(), 0, posSensor, updates, lightModel);

        assertThat(updates.containsKey(mkChannelId(CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_POS)), is(false));
        assertThat(updates.containsKey(mkChannelId(CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_CONTROL)), is(false));
    }

    @Test
    void rollerPositionAppliedWhenStopped() {
        Shelly1CoIoTVersion2 v2 = newProtocol();
        CoIotSensor posSensor = rollerPosSensor(30);
        List<CoIotSensor> sensorUpdates = List.of(rollerStateSensor("stop"), posSensor);
        Map<String, State> updates = new HashMap<>();
        LightModels lightModel = mock(LightModelAccessor.LightModels.class);

        v2.handleStatusUpdate(sensorUpdates, rollerPosDesc(), 0, posSensor, updates, lightModel);

        State pos = updates.get(mkChannelId(CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_POS));
        State control = updates.get(mkChannelId(CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_CONTROL));
        Assertions.assertNotNull(pos);
        Assertions.assertNotNull(control);
        assertThat(pos.toString(), is("30 %"));
        assertThat(control.toString(), is("70 %"));
    }

    @Test
    void rollerPositionAppliedWhenNoSiblingStateInBatch() {
        // "1102" absent from this batch: isRollerMoving() must default to "not moving" so an isolated
        // "1103" update (e.g. a periodic poll) still applies.
        Shelly1CoIoTVersion2 v2 = newProtocol();
        CoIotSensor posSensor = rollerPosSensor(50);
        List<CoIotSensor> sensorUpdates = List.of(posSensor);
        Map<String, State> updates = new HashMap<>();
        LightModels lightModel = mock(LightModelAccessor.LightModels.class);

        v2.handleStatusUpdate(sensorUpdates, rollerPosDesc(), 0, posSensor, updates, lightModel);

        assertThat(updates.containsKey(mkChannelId(CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_POS)), is(true));
    }

    @Test
    void redGreenBlueSensorsUpdateLightModel() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYBULB);
        profile.inColor = true;
        ShellyLightHandler handler = mockLightHandler(THING_TYPE_SHELLYBULB);
        Map<String, CoIotDescrBlk> blkMap = Map.of("1", lightBlk("1", "light_0"));
        Shelly1CoIoTVersion2 v2 = newLightProtocol(profile, blkMap, new HashMap<>(), handler);
        ShellyLightModel lightModel = ShellyLightModel.create(handler, 1, profile, STEP);
        LightModels lightModels = mock(LightModelAccessor.LightModels.class);
        when(lightModels.getByChannelGroupSuffix(0)).thenReturn(lightModel);
        Map<String, State> updates = new HashMap<>();

        CoIotSensor red = lightSensor("5105", 10);
        CoIotSensor green = lightSensor("5106", 20);
        CoIotSensor blue = lightSensor("5107", 30);

        assertThat(v2.handleStatusUpdate(List.of(red), lightDesc("5105", "red", "1"), 0, red, updates, lightModels),
                is(true));
        assertThat(
                v2.handleStatusUpdate(List.of(green), lightDesc("5106", "green", "1"), 0, green, updates, lightModels),
                is(true));
        assertThat(v2.handleStatusUpdate(List.of(blue), lightDesc("5107", "blue", "1"), 0, blue, updates, lightModels),
                is(true));

        verify(lightModels, times(3)).getByChannelGroupSuffix(0);
        assertThat(lightModel.getColor(R), is(10));
        assertThat(lightModel.getColor(G), is(20));
        assertThat(lightModel.getColor(B), is(30));
        assertThat(lightModel.getMode(), is(ShellyLightModel.Mode.COLOR));
        assertThat(updates.isEmpty(), is(true));
    }

    @Test
    void gainSensorUpdatesLightModel() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYBULB);
        profile.inColor = true;
        ShellyLightHandler handler = mockLightHandler(THING_TYPE_SHELLYBULB);
        Map<String, CoIotDescrBlk> blkMap = Map.of("1", lightBlk("1", "light_0"));
        Shelly1CoIoTVersion2 v2 = newLightProtocol(profile, blkMap, new HashMap<>(), handler);
        ShellyLightModel lightModel = ShellyLightModel.create(handler, 1, profile, STEP);
        LightModels lightModels = mock(LightModelAccessor.LightModels.class);
        when(lightModels.getByChannelGroupSuffix(0)).thenReturn(lightModel);
        Map<String, State> updates = new HashMap<>();

        CoIotSensor gain = lightSensor("5102", 40);

        assertThat(v2.handleStatusUpdate(List.of(gain), lightDesc("5102", "gain", "1"), 0, gain, updates, lightModels),
                is(true));

        verify(lightModels).getByChannelGroupSuffix(0);
        assertThat(lightModel.getGainState(), is(new PercentType(40)));
        assertThat(lightModel.getMode(), is(ShellyLightModel.Mode.COLOR));
        assertThat(updates.isEmpty(), is(true));
    }

    @Test
    void effectSensorUpdatesLightModel() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYBULB);
        profile.inColor = true;
        ShellyLightHandler handler = mockLightHandler(THING_TYPE_SHELLYBULB);
        Map<String, CoIotDescrBlk> blkMap = Map.of("1", lightBlk("1", "light_0"));
        Shelly1CoIoTVersion2 v2 = newLightProtocol(profile, blkMap, new HashMap<>(), handler);
        ShellyLightModel lightModel = ShellyLightModel.create(handler, 1, profile, STEP);
        LightModels lightModels = mock(LightModelAccessor.LightModels.class);
        when(lightModels.getByChannelGroupSuffix(0)).thenReturn(lightModel);
        Map<String, State> updates = new HashMap<>();

        CoIotSensor effect = lightSensor("5104", 3);

        assertThat(v2.handleStatusUpdate(List.of(effect), lightDesc("5104", "effect", "1"), 0, effect, updates,
                lightModels), is(true));

        verify(lightModels).getByChannelGroupSuffix(0);
        assertThat(lightModel.getEffectState(), is(new DecimalType(3)));
        assertThat(updates.isEmpty(), is(true));
    }

    @Test
    void colorTempSensorUpdatesLightModel() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYDUO);
        profile.inColor = false;
        ShellyLightHandler handler = mockLightHandler(THING_TYPE_SHELLYDUO);
        Map<String, CoIotDescrBlk> blkMap = Map.of("1", lightBlk("1", "light_0"));
        Shelly1CoIoTVersion2 v2 = newLightProtocol(profile, blkMap, new HashMap<>(), handler);
        ShellyLightModel lightModel = ShellyLightModel.create(handler, 1, profile, STEP);
        LightModels lightModels = mock(LightModelAccessor.LightModels.class);
        when(lightModels.getByChannelGroupSuffix(0)).thenReturn(lightModel);
        Map<String, State> updates = new HashMap<>();

        CoIotSensor colorTemp = lightSensor("5103", 4200);

        assertThat(v2.handleStatusUpdate(List.of(colorTemp), lightDesc("5103", "colorTemp", "1"), 0, colorTemp, updates,
                lightModels), is(true));

        verify(lightModels).getByChannelGroupSuffix(0);
        Assertions.assertInstanceOf(QuantityType.class, lightModel.getColorTemperatureAbsoluteState());
        QuantityType<?> colorTemperature = (QuantityType<?>) lightModel.getColorTemperatureAbsoluteState();
        QuantityType<?> kelvin = colorTemperature.toUnit(Units.KELVIN);
        Assertions.assertNotNull(kelvin);
        assertThat(kelvin.intValue(), is(4200));
        assertThat(lightModel.getMode(), is(ShellyLightModel.Mode.WHITE));
        assertThat(updates.isEmpty(), is(true));
    }

    private Shelly1CoIoTVersion2 newLightProtocol(ShellyDeviceProfile profile, Map<String, CoIotDescrBlk> blkMap,
            Map<String, CoIotDescrSen> sensorMap, ShellyLightHandler handler) {
        when(handler.getProfile()).thenReturn(profile);
        when(handler.getApi()).thenReturn(mock(ShellyApiInterface.class));
        return new Shelly1CoIoTVersion2("test", handler, blkMap, sensorMap);
    }

    private ShellyLightHandler mockLightHandler(ThingTypeUID thingTypeUID) {
        ShellyLightHandler handler = mock(ShellyLightHandler.class);
        Thing thing = mock(Thing.class);
        when(thing.getLabel()).thenReturn("Test Thing");
        when(thing.getThingTypeUID()).thenReturn(thingTypeUID);
        when(handler.getThing()).thenReturn(thing);
        return handler;
    }

    private CoIotSensor lightSensor(String id, double value) {
        CoIotSensor sensor = new CoIotSensor();
        sensor.id = id;
        sensor.value = value;
        return sensor;
    }

    private CoIotDescrSen lightDesc(String id, String desc, String links) {
        CoIotDescrSen sen = new CoIotDescrSen();
        sen.id = id;
        sen.desc = desc;
        sen.type = "S";
        sen.links = links;
        return sen;
    }

    private CoIotDescrBlk lightBlk(String id, String desc) {
        CoIotDescrBlk blk = new CoIotDescrBlk();
        blk.id = id;
        blk.desc = desc;
        return blk;
    }
}
