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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ApplicationGroup;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputChannelEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputModeEnum;
import org.openhab.binding.digitalstrom.internal.lib.util.JsonModel;
import org.openhab.binding.digitalstrom.internal.lib.util.OutputChannel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Test class for certain code in {@link DeviceImpl}
 *
 * @author Rouven Schürch - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class DeviceImplTest {

    private static final List<OutputChannel> EMPTY_CHANNEL = new ArrayList<>();

    private static final List<OutputChannel> SHADE_ANGLE_CHANNELS = Arrays.asList(
            new OutputChannel(OutputChannelEnum.SHADE_OPENING_ANGLE_OUTSIDE),
            new OutputChannel(OutputChannelEnum.SHADE_OPENING_ANGLE_INDOOR));

    private static final List<OutputChannel> SHADE_POSITION_CHANNELS = Arrays.asList(
            new OutputChannel(OutputChannelEnum.SHADE_POSITION_INDOOR),
            new OutputChannel(OutputChannelEnum.SHADE_POSITION_OUTSIDE));

    private static final List<OutputChannel> NON_SHADE_CHANNEL = Arrays
            .asList(new OutputChannel(OutputChannelEnum.BRIGHTNESS));

    private static final List<OutputChannel> MIXED_SHADE_CHANNEL = Arrays.asList(
            new OutputChannel(OutputChannelEnum.BRIGHTNESS),
            new OutputChannel(OutputChannelEnum.SHADE_OPENING_ANGLE_OUTSIDE));

    @Test
    void isBlindSwitchShadeChannel() {
        JsonObject jsonObject = createJsonObject(OutputModeEnum.SINGLE_SWITCH, SHADE_ANGLE_CHANNELS);
        DeviceImpl deviceImpl = new DeviceImpl(jsonObject);
        assertThat(deviceImpl.isBlind(), is(false));
    }

    @Test
    void isBlindSwitchNoShadeChannel() {
        JsonObject jsonObject = createJsonObject(OutputModeEnum.SINGLE_SWITCH, NON_SHADE_CHANNEL);
        DeviceImpl deviceImpl = new DeviceImpl(jsonObject);
        assertThat(deviceImpl.isBlind(), is(false));
    }

    @Test
    void isBlindSwitchMixedShadeChannel() {
        JsonObject jsonObject = createJsonObject(OutputModeEnum.SINGLE_SWITCH, MIXED_SHADE_CHANNEL);
        DeviceImpl deviceImpl = new DeviceImpl(jsonObject);
        assertThat(deviceImpl.isBlind(), is(false));
    }

    @Test
    void isBlindPositionConUsNoChannel() {
        JsonObject jsonObject = createJsonObject(OutputModeEnum.POSITION_CON_US, EMPTY_CHANNEL);
        DeviceImpl deviceImpl = new DeviceImpl(jsonObject);
        assertThat(deviceImpl.isBlind(), is(false));
    }

    @Test
    void isBlindPositionConUsNonShadeChannel() {
        JsonObject jsonObject = createJsonObject(OutputModeEnum.POSITION_CON_US, NON_SHADE_CHANNEL);
        DeviceImpl deviceImpl = new DeviceImpl(jsonObject);
        assertThat(deviceImpl.isBlind(), is(false));
    }

    @Test
    void isBlindPositionConUsShadePositionChannels() {
        JsonObject jsonObject = createJsonObject(OutputModeEnum.POSITION_CON_US, SHADE_POSITION_CHANNELS);
        DeviceImpl deviceImpl = new DeviceImpl(jsonObject);
        assertThat(deviceImpl.isBlind(), is(false));
    }

    @Test
    void isBlindPositionConUsShadeAngleChannels() {
        JsonObject jsonObject = createJsonObject(OutputModeEnum.POSITION_CON_US, SHADE_ANGLE_CHANNELS);
        DeviceImpl deviceImpl = new DeviceImpl(jsonObject);
        assertThat(deviceImpl.isBlind(), is(true));
    }

    @Test
    void isBlindPositionConUsMixedChannels() {
        JsonObject jsonObject = createJsonObject(OutputModeEnum.POSITION_CON_US, MIXED_SHADE_CHANNEL);
        DeviceImpl deviceImpl = new DeviceImpl(jsonObject);
        assertThat(deviceImpl.isBlind(), is(true));
    }

    @Test
    void isBlindPositionConNoChannel() {
        JsonObject jsonObject = createJsonObject(OutputModeEnum.POSITION_CON, EMPTY_CHANNEL);
        DeviceImpl deviceImpl = new DeviceImpl(jsonObject);
        assertThat(deviceImpl.isBlind(), is(false));
    }

    @Test
    void isBlindPositionConNonShadeChannel() {
        JsonObject jsonObject = createJsonObject(OutputModeEnum.POSITION_CON, NON_SHADE_CHANNEL);
        DeviceImpl deviceImpl = new DeviceImpl(jsonObject);
        assertThat(deviceImpl.isBlind(), is(false));
    }

    @Test
    void isBlindPositionConShadePositionChannels() {
        JsonObject jsonObject = createJsonObject(OutputModeEnum.POSITION_CON, SHADE_POSITION_CHANNELS);
        DeviceImpl deviceImpl = new DeviceImpl(jsonObject);
        assertThat(deviceImpl.isBlind(), is(false));
    }

    @Test
    void isBlindPositionConShadeAngleChannels() {
        JsonObject jsonObject = createJsonObject(OutputModeEnum.POSITION_CON, SHADE_ANGLE_CHANNELS);
        DeviceImpl deviceImpl = new DeviceImpl(jsonObject);
        assertThat(deviceImpl.isBlind(), is(true));
    }

    @Test
    void isBlindPositionConMixedChannels() {
        JsonObject jsonObject = createJsonObject(OutputModeEnum.POSITION_CON, MIXED_SHADE_CHANNEL);
        DeviceImpl deviceImpl = new DeviceImpl(jsonObject);
        assertThat(deviceImpl.isBlind(), is(true));
    }

    @Test
    @DisplayName("No functional color group set, expect a null value returned")
    void noFunctionalColorGroupSet() {
        DeviceImpl deviceImpl = new DeviceImpl(new JsonObject());
        ApplicationGroup functionalColorGroup = deviceImpl.getFunctionalColorGroup();
        assertNull(functionalColorGroup);
    }

    @Test
    @DisplayName("Multiple functional color groups set, expect the first group returned which had previously been added")
    void multipleFunctionalColorGroupSet() {
        DeviceImpl deviceImpl = new DeviceImpl(new JsonObject());
        deviceImpl.addGroup(ApplicationGroup.JOKER.getId());
        deviceImpl.addGroup(ApplicationGroup.ACCESS.getId());

        assertThat(deviceImpl.getFunctionalColorGroup(), is(ApplicationGroup.JOKER));
    }

    private static JsonObject createJsonObject(OutputModeEnum outputMode, List<OutputChannel> channels) {
        JsonModel model = new JsonModel(outputMode.getMode(), channels);

        Gson gson = new Gson();
        String json = gson.toJson(model);

        return JsonParser.parseString(json).getAsJsonObject();
    }
}
