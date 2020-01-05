/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescriptionFragment;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.loxone.internal.types.LxIRCTemperature;
import org.openhab.binding.loxone.internal.types.LxTags;
import org.openhab.binding.loxone.internal.types.LxUuid;

/**
 * An Intelligent Room Controller V1.
 * This implementation does not support the timers which describe temperature zones during the day.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlIRoomController extends LxControl {
    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlIRoomController(uuid);
        }

        @Override
        String getType() {
            return "iroomcontroller";
        }
    }

    private State manualTemp = UnDefType.UNDEF;

    private static final String STATE_TEMP_TARGET = "temptarget";
    private static final String STATE_TEMP_ACTUAL = "tempactual";
    private static final String STATE_MODE = "mode";
    private static final String STATE_SERVICE_MODE = "servicemode";
    private static final String STATE_MANUAL_MODE = "manualmode";
    private static final String STATE_OPEN_WINDOW = "openwindow";
    private static final String STATE_TEMPERATURE_PREFIX = "temperatures-";
    private static final String STATE_STOP = "stop";
    private static final String STATE_OVERRIDE = "override";
    private static final String STATE_OVERRIDE_TOTAL = "overridetotal";

    private static final String STATE_CURRENT_HEAT_TEMP_IDX = "currheattempix";
    private static final String STATE_CURRENT_COOL_TEMP_IDX = "currcooltempix";

    private static final String CMD_MODE = "mode/";
    private static final String CMD_SERVICE = "service/";
    private static final String CMD_SET_TEMP = "settemp/";
    private static final String CMD_START_TIMER = "starttimer/";
    private static final String CMD_STOP_TIMER = "stoptimer";

    private LxIRCTemperature overrideTemperature = LxIRCTemperature.UNKNOWN;
    private DecimalType overrideTime = DecimalType.ZERO;

    private LxControlIRoomController(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        ChannelUID cid;
        Set<String> tempTags = new HashSet<>(tags);
        tempTags.addAll(LxTags.TEMPERATURE);
        String format = null;
        if (details != null) {
            format = this.details.format;
        }
        StateDescriptionFragment patternRoFragment = null;
        StateDescriptionFragment patternRwFragment = null;
        if (format != null) {
            patternRoFragment = StateDescriptionFragmentBuilder.create().withPattern(format).withReadOnly(true).build();
            patternRwFragment = StateDescriptionFragmentBuilder.create().withPattern(format).withReadOnly(false)
                    .build();
        }
        cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_ANALOG),
                defaultChannelLabel + " / Current Temperature", "Current temperature", tempTags, null,
                () -> getStateDecimalValue(STATE_TEMP_ACTUAL));
        addChannelStateDescriptionFragment(cid, patternRoFragment);
        cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_ANALOG),
                defaultChannelLabel + " / Target Temperature", "Target temperature", tempTags, null,
                () -> getStateDecimalValue(STATE_TEMP_TARGET));
        addChannelStateDescriptionFragment(cid, patternRoFragment);
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_IROOM_MODE),
                defaultChannelLabel + " / Mode", "Mode", tags, this::setMode, () -> getStateDecimalValue(STATE_MODE));
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_IROOM_SERVICE_MODE),
                defaultChannelLabel + " / Service Mode", "Service mode", tags, this::setServiceMode,
                () -> getStateDecimalValue(STATE_SERVICE_MODE));
        addChannel("String", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_TEXT),
                defaultChannelLabel + " / Manual Mode", "Manual mode", tags, null, this::getManualModeState);
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_SWITCH),
                defaultChannelLabel + " / Open Window", "Open window", tags, null,
                () -> getSwitchState(STATE_OPEN_WINDOW));
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_SWITCH),
                defaultChannelLabel + " / Outputs Disabled", "All outputs disabled", tags, null,
                () -> getSwitchState(STATE_STOP));
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_ANALOG),
                defaultChannelLabel + " / Override Time", "Remaining override time", tags, null,
                () -> getStateDecimalValue(STATE_OVERRIDE));
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_ANALOG),
                defaultChannelLabel + " / Override Total Time", "Total override time", tags, null,
                () -> getStateDecimalValue(STATE_OVERRIDE_TOTAL));
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_IROOM_TIMER_TEMP_TYPE),
                defaultChannelLabel + " / Manual Override Temperature",
                "Temperature target for the next manual override", tags, this::handleOverrideTempCommand,
                () -> overrideTemperature.getIndexState());
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_NUMBER),
                defaultChannelLabel + " / Manual Override Time", "Timer's value for the next manual override", tags,
                this::handleOverrideTimeCommand, () -> overrideTime);
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH),
                defaultChannelLabel + " / Override", "Enable manual override or override is active", tags,
                this::handleOverrideCommand, this::getOverrideState);
        addChannel("String", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_TEXT),
                defaultChannelLabel + " / Current Heating Temperature", "Current heating temperature", tags, null,
                () -> getTemperatureIndexLabel(STATE_CURRENT_HEAT_TEMP_IDX));
        addChannel("String", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_TEXT),
                defaultChannelLabel + " / Current Cooling Temperature", "Current cooling temperature", tags, null,
                () -> getTemperatureIndexLabel(STATE_CURRENT_COOL_TEMP_IDX));
        for (int i = 0; i <= 6; i++) {
            addAutoTempChannel(i, tempTags, patternRwFragment);
        }
        addTempChannel(7, tempTags, patternRwFragment, (c) -> {
            manualTemp = c instanceof DecimalType ? (DecimalType) c : UnDefType.NULL;
            setTemperature(c, 7);
        }, () -> manualTemp);
    }

    private State getTemperatureIndexLabel(String state) {
        DecimalType index = (DecimalType) getStateDecimalValue(state);
        if (index != null) {
            String label = LxIRCTemperature.fromIndex(index.intValue()).getLabel();
            return new StringType(label);
        }
        return UnDefType.UNDEF;
    }

    private void addTempChannel(int index, Set<String> tags, @Nullable StateDescriptionFragment fragment,
            CommandCallback cmdCallback, StateCallback stateCallback) {
        String label = LxIRCTemperature.fromIndex(index).getLabel();
        ChannelUID cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_NUMBER),
                defaultChannelLabel + " / Temperature / " + label, label + " temperature", tags, cmdCallback,
                stateCallback);
        addChannelStateDescriptionFragment(cid, fragment);
    }

    private void addAutoTempChannel(int index, Set<String> tags, @Nullable StateDescriptionFragment fragment) {
        addTempChannel(index, tags, fragment, (c) -> setTemperature(c, index),
                () -> getStateDecimalValue(STATE_TEMPERATURE_PREFIX + index));
    }

    private @Nullable State getSwitchState(String name) {
        return LxControlSwitch.convertSwitchState(getStateDoubleValue(name));
    }

    private void setMode(Command command) throws IOException {
        if (command instanceof DecimalType) {
            int mode = ((DecimalType) command).intValue();
            // autopilot modes 1 and 2 are not to be selected, API says to use 3 and 4 instead
            if (mode == 1) {
                mode = 3;
            } else if (mode == 2) {
                mode = 4;
            }
            sendAction(CMD_MODE + String.valueOf(mode));
        }
    }

    private void setServiceMode(Command command) throws IOException {
        if (command instanceof DecimalType) {
            int mode = ((DecimalType) command).intValue();
            sendAction(CMD_SERVICE + String.valueOf(mode));
        }
    }

    private void setTemperature(Command command, int index) throws IOException {
        if (command instanceof DecimalType) {
            DecimalType temp = (DecimalType) command;
            sendAction(CMD_SET_TEMP + index + "/" + String.valueOf(temp.doubleValue()));
        }
    }

    private void handleOverrideTempCommand(Command command) {
        if (command instanceof DecimalType) {
            overrideTemperature = LxIRCTemperature.fromIndex(((DecimalType) command).intValue());
        }
    }

    private void handleOverrideTimeCommand(Command command) {
        if (command instanceof DecimalType) {
            DecimalType time = (DecimalType) command;
            if (time.compareTo(DecimalType.ZERO) > 0) {
                overrideTime = time;
            } else {
                overrideTime = DecimalType.ZERO;
            }
        }
    }

    private void handleOverrideCommand(Command command) throws IOException {
        if (command instanceof OnOffType) {
            if ((OnOffType) command == OnOffType.OFF) {
                sendAction(CMD_STOP_TIMER);
            } else {
                State temp = overrideTemperature.getIndexState();
                if (temp instanceof DecimalType && overrideTime.compareTo(DecimalType.ZERO) > 0) {
                    sendAction(CMD_START_TIMER + ((DecimalType) temp).intValue() + "/" + overrideTime.intValue());
                }
            }
        }
    }

    private State getOverrideState() {
        Double time = getStateDoubleValue(STATE_OVERRIDE);
        if (time != null && time > 0.0) {
            return OnOffType.ON;
        }
        return OnOffType.OFF;
    }

    private State getManualModeState() {
        Double val = getStateDoubleValue(STATE_MANUAL_MODE);
        if (val != null) {
            switch (val.intValue()) {
                case 0:
                    return new StringType("Off");
                case 1:
                    return new StringType("Comfort overriding");
                case 2:
                    return new StringType("Economy overriding");
                case 3:
                    return new StringType("Timer overriding");
                case 4:
                    return new StringType("Movement/presence");
            }
        }
        return UnDefType.UNDEF;
    }
}
