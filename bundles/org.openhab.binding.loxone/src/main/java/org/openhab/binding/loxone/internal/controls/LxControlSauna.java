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

import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Loxone Miniserver's Sauna
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlSauna extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlSauna(uuid);
        }

        @Override
        String getType() {
            return "sauna";
        }
    }

    private static final String STATE_ACTIVE = "active";
    private static final String STATE_POWER_LEVEL = "power";
    private static final String STATE_TEMP_ACTUAL = "tempactual";
    private static final String STATE_TEMP_BENCH = "tempbench";
    private static final String STATE_TEMP_TARGET = "temptarget";
    private static final String STATE_FAN = "fan";
    private static final String STATE_DRYING = "drying";
    private static final String STATE_DOOR_CLOSED = "doorclosed";
    private static final String STATE_ERROR = "error";
    private static final String STATE_VAPOR_POWER_LEVEL = "vaporpower";
    private static final String STATE_SAUNA_ERROR = "saunaerror";
    private static final String STATE_TIMER = "timer";
    private static final String STATE_TIMER_TOTAL = "timertotal";
    private static final String STATE_OUT_OF_WATER = "lesswater";
    private static final String STATE_HUMIDITY_ACTUAL = "humidityactual";
    private static final String STATE_HUMIDITY_TARGET = "humiditytarget";
    private static final String STATE_EVAPORATOR_MODE = "mode";

    private static final String CMD_ON = "on";
    private static final String CMD_OFF = "off";
    private static final String CMD_FAN_ON = "fanon";
    private static final String CMD_FAN_OFF = "fanoff";
    private static final String CMD_SET_TEMP_TARGET = "temp/";
    private static final String CMD_SET_HUMIDITY_TARGET = "humidity/";
    private static final String CMD_SET_EVAPORATOR_MODE = "mode/";
    private static final String CMD_NEXT_STATE = "pulse";
    private static final String CMD_START_TIMER = "starttimer";

    LxControlSauna(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH),
                defaultChannelLabel + " / Active", "Sauna Active", tags, this::handleSaunaActivateCommands,
                () -> getStateOnOffValue(STATE_ACTIVE));
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Power", "Sauna Power Level", tags, null,
                () -> getStatePercentValue(STATE_POWER_LEVEL));
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Temperature / Actual", "Actual Temperature", tags, null,
                () -> getStateDecimalValue(STATE_TEMP_ACTUAL));
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Temperature / Bench", "Bench Temperature", tags, null,
                () -> getStateDecimalValue(STATE_TEMP_BENCH));
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_NUMBER),
                defaultChannelLabel + " / Temperature / Target", "Target Temperature", tags,
                (cmd) -> handleSetNumberCommands(cmd, CMD_SET_TEMP_TARGET),
                () -> getStateDecimalValue(STATE_TEMP_TARGET));
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH),
                defaultChannelLabel + " / Fan", "Fan", tags, this::handleFanCommands,
                () -> getStateOnOffValue(STATE_FAN));
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_SWITCH),
                defaultChannelLabel + " / Drying", "Drying", tags, null, () -> getStateOnOffValue(STATE_DRYING));
        if (details != null && details.hasDoorSensor != null && details.hasDoorSensor) {
            addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_SWITCH),
                    defaultChannelLabel + " / Door Closed", "Door Closed", tags, null,
                    () -> getStateOnOffValue(STATE_DOOR_CLOSED));
        }
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Error Code", "Error Code", tags, null, () -> getStateErrorValue());
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Timer / Current", "Current Timer Value", tags, null,
                () -> getStateDecimalValue(STATE_TIMER));
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH),
                defaultChannelLabel + " / Timer / Trigger", "Start Timer", tags,
                (cmd) -> handleTriggerCommands(cmd, CMD_START_TIMER), () -> OnOffType.OFF);
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Timer / Total", "Total Timer Value", tags, null,
                () -> getStateDecimalValue(STATE_TIMER_TOTAL));
        if (details != null && details.hasVaporizer != null && details.hasVaporizer) {
            addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                    defaultChannelLabel + " / Evaporator / Power", "Evaporator Power Level", tags, null,
                    () -> getStatePercentValue(STATE_VAPOR_POWER_LEVEL));
            addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_SWITCH),
                    defaultChannelLabel + " / Evaporator / Out Of Water", "Evaporator Out Of Water", tags, null,
                    () -> getStateOnOffValue(STATE_OUT_OF_WATER));
            addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                    defaultChannelLabel + " / Evaporator / Humidity / Actual", "Actual Humidity", tags, null,
                    () -> getStateDecimalValue(STATE_HUMIDITY_ACTUAL));
            addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_NUMBER),
                    defaultChannelLabel + " / Evaporator / Humidity / Target", "Target Humidity", tags,
                    (cmd) -> handleSetNumberCommands(cmd, CMD_SET_HUMIDITY_TARGET),
                    () -> getStateDecimalValue(STATE_HUMIDITY_TARGET));
            addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_NUMBER),
                    defaultChannelLabel + " / Evaporator / Mode", "Evaporator Mode", tags, this::handleModeCommands,
                    () -> getStateDecimalValue(STATE_EVAPORATOR_MODE));
        }
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH),
                defaultChannelLabel + " / Next State", "Trigger Next State", tags,
                (cmd) -> handleTriggerCommands(cmd, CMD_NEXT_STATE), () -> OnOffType.OFF);
    }

    private void handleSaunaActivateCommands(Command command) throws IOException {
        if (command instanceof OnOffType onOffCommand) {
            if (onOffCommand == OnOffType.ON) {
                sendAction(CMD_ON);
            } else {
                sendAction(CMD_OFF);
            }
        }
    }

    private void handleSetNumberCommands(Command command, String prefix) throws IOException {
        if (command instanceof DecimalType decimalCommand) {
            Double value = decimalCommand.doubleValue();
            sendAction(prefix + value.toString());
        }
    }

    private void handleFanCommands(Command command) throws IOException {
        if (command instanceof OnOffType onOffCommand) {
            if (onOffCommand == OnOffType.ON) {
                sendAction(CMD_FAN_ON);
            } else {
                sendAction(CMD_FAN_OFF);
            }
        }
    }

    private void handleTriggerCommands(Command command, String prefix) throws IOException {
        if (command instanceof OnOffType onOffCommand && onOffCommand == OnOffType.ON) {
            sendAction(prefix);
        }
    }

    private void handleModeCommands(Command command) throws IOException {
        if (command instanceof DecimalType decimalCommand) {
            Double value = decimalCommand.doubleValue();
            // per API there are 7 evaporator modes selected with number 0-6
            if (value % 1 == 0 && value >= 0.0 && value <= 6.0) {
                sendAction(CMD_SET_EVAPORATOR_MODE + value.toString());
            }
        }
    }

    private State getStateErrorValue() {
        Double val = getStateDoubleValue(STATE_ERROR);
        if (val != null && val != 0.0) {
            return getStateDecimalValue(STATE_SAUNA_ERROR);
        }
        return DecimalType.ZERO;
    }
}
