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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.mkChannelId;

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
import org.openhab.binding.shelly.internal.handler.ShellyColorUtils;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
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

        v2.handleStatusUpdate(sensorUpdates, rollerPosDesc(), 0, posSensor, updates, new ShellyColorUtils());

        assertThat(updates.containsKey(mkChannelId(CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_POS)), is(false));
        assertThat(updates.containsKey(mkChannelId(CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_CONTROL)), is(false));
    }

    @Test
    void rollerPositionAppliedWhenStopped() {
        Shelly1CoIoTVersion2 v2 = newProtocol();
        CoIotSensor posSensor = rollerPosSensor(30);
        List<CoIotSensor> sensorUpdates = List.of(rollerStateSensor("stop"), posSensor);
        Map<String, State> updates = new HashMap<>();

        v2.handleStatusUpdate(sensorUpdates, rollerPosDesc(), 0, posSensor, updates, new ShellyColorUtils());

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

        v2.handleStatusUpdate(sensorUpdates, rollerPosDesc(), 0, posSensor, updates, new ShellyColorUtils());

        assertThat(updates.containsKey(mkChannelId(CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_POS)), is(true));
    }
}
