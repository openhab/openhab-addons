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
package org.openhab.binding.loxone.internal.controls;

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.openhab.binding.loxone.internal.types.LxTags;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;

/**
 * An Intelligent Room Controller V2.
 * This implementation does not support the timers which describe temperature zones during the day.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlIRoomControllerV2 extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlIRoomControllerV2(uuid);
        }

        @Override
        String getType() {
            return "iroomcontrollerv2";
        }
    }

    private static final String STATE_ACTIVE_MODE = "activemode";
    private static final String STATE_OPERATING_MODE = "operatingmode";
    private static final String STATE_PREPARE_STATE = "preparestate";
    private static final String STATE_OPEN_WINDOW = "openwindow";
    private static final String STATE_TEMP_ACTUAL = "tempactual";
    private static final String STATE_TEMP_TARGET = "temptarget";
    private static final String STATE_COMFORT_TEMPERATURE = "comforttemperature";
    private static final String STATE_COMFORT_TEMPERATURE_OFFSET = "comforttemperatureoffset";
    private static final String STATE_COMFORT_TOLERANCE = "comforttolerance";
    private static final String STATE_ABSENT_MIN_OFFSET = "absentminoffset";
    private static final String STATE_ABSENT_MAX_OFFSET = "absentmaxoffset";
    private static final String STATE_FROST_PROTECT_TEMPERATURE = "frostprotecttemperature";
    private static final String STATE_HEAT_PROTECT_TEMPERATURE = "heatprotecttemperature";

    private static final String CMD_SET_OPERATING_MODE = "setOperatingMode/";
    private static final String CMD_SET_COMFORT_TOLERANCE = "setComfortTolerance/";
    private static final String CMD_SET_COMFORT_TEMPERATURE = "setComfortTemperature/";
    private static final String CMD_SET_COMFORT_TEMPERATURE_OFFSET = "setComfortModeTemp/";
    private static final String CMD_SET_ABSENT_MIN_TEMPERATURE = "setAbsentMinTemperature/";
    private static final String CMD_SET_ABSENT_MAX_TEMPERATURE = "setAbsentMaxTemperature/";
    private static final String CMD_SET_MANUAL_TEMPERATURE = "setManualTemperature/";

    private LxControlIRoomControllerV2(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        ChannelUID cid;
        Set<String> tempTags = new HashSet<>(tags);
        tempTags.addAll(LxTags.TEMPERATURE);
        String format = details.format;
        StateDescriptionFragment patternRoFragment = null;
        StateDescriptionFragment patternRwFragment = null;
        if (format != null) {
            patternRoFragment = StateDescriptionFragmentBuilder.create().withPattern(format).withReadOnly(true).build();
            patternRwFragment = StateDescriptionFragmentBuilder.create().withPattern(format).withReadOnly(false)
                    .build();
        }

        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_IROOM_V2_ACTIVE_MODE),
                defaultChannelLabel + "/ Active Mode", "Active mode", tags, null,
                () -> getStateDecimalValue(STATE_ACTIVE_MODE));

        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_IROOM_V2_OPERATING_MODE),
                defaultChannelLabel + "/ Operating Mode", "Operating mode", tags, this::setOperatingMode,
                () -> getStateDecimalValue(STATE_OPERATING_MODE));

        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_IROOM_V2_PREPARE_STATE),
                defaultChannelLabel + "/ Prepare State", "Prepare state", tags, null,
                () -> getStateDecimalValue(STATE_PREPARE_STATE));

        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_SWITCH),
                defaultChannelLabel + "/ Open Window", "Open window", tags, null,
                () -> getSwitchState(STATE_OPEN_WINDOW));

        cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_ANALOG),
                defaultChannelLabel + "/ Current Temperature", "Current temperature", tempTags, null,
                () -> getStateDecimalValue(STATE_TEMP_ACTUAL));
        addChannelStateDescriptionFragment(cid, patternRoFragment);

        // manual temperature will affect value of target temperature only in manual operating modes
        cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_NUMBER),
                defaultChannelLabel + "/ Target Temperature", "Target temperature", tempTags,
                (c) -> setTemperature(c, CMD_SET_MANUAL_TEMPERATURE), () -> getStateDecimalValue(STATE_TEMP_TARGET));
        addChannelStateDescriptionFragment(cid, patternRwFragment);

        cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_NUMBER),
                defaultChannelLabel + "/ Comfort Temperature", "Comfort temperature", tempTags,
                (c) -> setTemperature(c, CMD_SET_COMFORT_TEMPERATURE),
                () -> getStateDecimalValue(STATE_COMFORT_TEMPERATURE));
        addChannelStateDescriptionFragment(cid, patternRwFragment);

        cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_NUMBER),
                defaultChannelLabel + "/ Comfort Temperature Offset", "Comfort temperature offset", tempTags,
                (c) -> setTemperature(c, CMD_SET_COMFORT_TEMPERATURE_OFFSET),
                () -> getStateDecimalValue(STATE_COMFORT_TEMPERATURE_OFFSET));
        addChannelStateDescriptionFragment(cid, patternRwFragment);

        cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_IROOM_V2_COMFORT_TOLERANCE),
                defaultChannelLabel + "/ Comfort Tolerance", "Comfort tolerance", tempTags,
                (c) -> setTemperature(c, CMD_SET_COMFORT_TOLERANCE),
                () -> getStateDecimalValue(STATE_COMFORT_TOLERANCE));
        addChannelStateDescriptionFragment(cid, patternRwFragment);

        cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_NUMBER),
                defaultChannelLabel + "/ Absent Min Offset", "Absent minimum temperature offset", tempTags,
                (c) -> setTemperature(c, CMD_SET_ABSENT_MIN_TEMPERATURE),
                () -> getStateDecimalValue(STATE_ABSENT_MIN_OFFSET));
        addChannelStateDescriptionFragment(cid, patternRwFragment);

        cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_NUMBER),
                defaultChannelLabel + "/ Absent Max Offset", "Absent maximum temperature offset", tempTags,
                (c) -> setTemperature(c, CMD_SET_ABSENT_MAX_TEMPERATURE),
                () -> getStateDecimalValue(STATE_ABSENT_MAX_OFFSET));
        addChannelStateDescriptionFragment(cid, patternRwFragment);

        cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_ANALOG),
                defaultChannelLabel + "/ Frost Protect Temperature", "Frost protect temperature", tempTags, null,
                () -> getStateDecimalValue(STATE_FROST_PROTECT_TEMPERATURE));
        addChannelStateDescriptionFragment(cid, patternRoFragment);

        cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_ANALOG),
                defaultChannelLabel + "/ Heat Protect Temperature", "Heat protect temperature", tempTags, null,
                () -> getStateDecimalValue(STATE_HEAT_PROTECT_TEMPERATURE));
        addChannelStateDescriptionFragment(cid, patternRoFragment);
    }

    private State getSwitchState(String name) {
        return LxControlSwitch.convertSwitchState(getStateDoubleValue(name));
    }

    private void setOperatingMode(Command command) throws IOException {
        if (command instanceof DecimalType mode) {
            sendAction(CMD_SET_OPERATING_MODE + mode.intValue());
        }
    }

    private void setTemperature(Command command, String prefix) throws IOException {
        if (command instanceof DecimalType temp) {
            sendAction(prefix + temp.doubleValue());
        }
    }
}
