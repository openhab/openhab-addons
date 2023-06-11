/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.openhab.binding.loxone.internal.types.LxState;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.UnDefType;

/**
 * Loxone Control that controls the Burglar Alarm
 *
 * @author Michael Mattan - Initial contribution
 *
 */
class LxControlAlarm extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlAlarm(uuid);
        }

        @Override
        String getType() {
            return "alarm";
        }
    }

    private static final String CMD_ON = "on";
    private static final String CMD_ON_WITH_MOVEMENT = "on/1";
    private static final String CMD_ON_WITHOUT_MOVEMENT = "on/0";
    private static final String CMD_DELAYED_ON = "delayedon";
    private static final String CMD_DELAYED_ON_WITH_MOVEMENT = "delayedon/1";
    private static final String CMD_DELAYED_ON_WITHOUT_MOVEMENT = "delayedon/0";
    private static final String CMD_OFF = "off";
    private static final String CMD_QUIT = "quit";
    private static final String CMD_DISABLE_MOVEMENT = "dismv/1";
    private static final String CMD_ENABLE_MOVEMENT = "dismv/0";

    /**
     * If the alarm control is armed
     */
    private static final String STATE_ARMED = "armed";

    /**
     * The id of the next alarm level
     */
    private static final String STATE_NEXT_LEVEL = "nextlevel";

    /**
     * The delay of the next level in seconds
     */
    private static final String STATE_NEXT_LEVEL_DELAY = "nextleveldelay";

    /**
     * The total delay of the next level in seconds
     */
    private static final String STATE_NEXT_LEVEL_DELAY_TOTAL = "nextleveldelaytotal";

    /**
     * The id of the current alarm level
     */
    private static final String STATE_LEVEL = "level";

    /**
     * Timestamp when alarm started
     */
    private static final String STATE_START_TIME = "starttime";

    /**
     * The delay of the alarm control being armed
     */
    private static final String STATE_ARMED_DELAY = "armeddelay";

    /**
     * The total delay of the alarm control being armed
     */
    private static final String STATE_ARMED_DELAY_TOTAL = "armeddelaytotal";

    /**
     * A string of sensors separated by a pipe
     */
    private static final String STATE_SENSOR = "sensors";

    /**
     * If the movement is disabled or not
     */
    private static final String STATE_DISABLED_MOVE = "disabledmove";

    private ChannelUID startTimeId;
    private ChannelUID ackChannelId;
    private State startTime = UnDefType.UNDEF;
    private boolean presenceConnected = false;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LxControlAlarm(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        if (details.presenceConnected != null) {
            presenceConnected = details.presenceConnected;
        }

        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH), defaultChannelLabel,
                "Alarm armed", tags, this::handleArmAlarm, () -> getStateOnOffValue(STATE_ARMED));

        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH),
                defaultChannelLabel + " / Arm Delayed", "Arm with delay", tags, this::handleArmDelayedAlarm,
                () -> OnOffType.OFF);

        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Next Level", "ID of the next alarm level", tags, null,
                () -> getStateDecimalValue(STATE_NEXT_LEVEL));

        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Next Level Delay", "Delay of the next level", tags, null,
                () -> getStateDecimalValue(STATE_NEXT_LEVEL_DELAY));

        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Next Level Delay Total", "Total delay of the next level", tags, null,
                () -> getStateDecimalValue(STATE_NEXT_LEVEL_DELAY_TOTAL));

        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Level", "Current alarm level", tags, null,
                () -> getStateDecimalValue(STATE_LEVEL));

        startTimeId = addChannel("DateTime", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_DATETIME),
                defaultChannelLabel + " / Start Time", "Time when alarm started", tags, null, () -> startTime);

        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Armed Delay", "Delay of the alarm being armed", tags, null,
                () -> getStateDecimalValue(STATE_ARMED_DELAY));

        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Armed Total Delay", "Total delay of the alarm being armed", tags, null,
                () -> getStateDecimalValue(STATE_ARMED_DELAY_TOTAL));

        addChannel("String", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_TEXT),
                defaultChannelLabel + " / Sensors", "Alarm sensors", tags, null,
                () -> getStateStringValue(STATE_SENSOR));

        ackChannelId = addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH),
                defaultChannelLabel + " / Acknowledge", "Acknowledge alarm", tags, this::handleQuitAlarm,
                () -> OnOffType.OFF);
        addChannelStateDescriptionFragment(ackChannelId,
                StateDescriptionFragmentBuilder.create().withReadOnly(true).build());

        if (presenceConnected) {
            // this channel has reversed logic - we show state of enabled option, but receive state updates if disabled
            addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH),
                    defaultChannelLabel + " / Motion Sensors", "Motion sensors enabled", tags,
                    this::handleMotionSensors, this::getStateMotionSensors);
        }
    }

    @Override
    public void onStateChange(LxState state) {
        String stateName = state.getName();
        if (STATE_START_TIME.equals(stateName)) {
            startTime = UnDefType.UNDEF;
            Object obj = state.getStateValue();
            if (obj instanceof String && !((String) obj).isEmpty()) {
                try {
                    LocalDateTime ldt = LocalDateTime.parse((String) obj, dateTimeFormatter);
                    ZonedDateTime dt = ldt.atZone(ZoneId.systemDefault());
                    startTime = new DateTimeType(dt);
                } catch (DateTimeParseException e) {
                    startTime = null;
                }
            }
            setChannelState(startTimeId, startTime);
        } else if (STATE_LEVEL.equals(stateName)) {
            Object obj = state.getStateValue();
            addChannelStateDescriptionFragment(ackChannelId, StateDescriptionFragmentBuilder.create()
                    .withReadOnly(obj instanceof Double && ((Double) obj) == 0.0).build());
            super.onStateChange(state);
        } else {
            super.onStateChange(state);
        }
    }

    private void handleArming(Command command, String onAction, String onWithMovementAction,
            String onWithoutMovementAction) throws IOException {
        if (command instanceof OnOffType) {
            if (command == OnOffType.ON) {
                if (presenceConnected) {
                    Double value = getStateDoubleValue(STATE_DISABLED_MOVE);
                    if (value == null || value == 1.0) {
                        sendAction(onWithoutMovementAction);
                    } else {
                        sendAction(onWithMovementAction);
                    }
                } else {
                    sendAction(onAction);
                }
            } else {
                sendAction(CMD_OFF);
            }
        }
    }

    private void handleArmAlarm(Command command) throws IOException {
        handleArming(command, CMD_ON, CMD_ON_WITH_MOVEMENT, CMD_ON_WITHOUT_MOVEMENT);
    }

    private void handleArmDelayedAlarm(Command command) throws IOException {
        handleArming(command, CMD_DELAYED_ON, CMD_DELAYED_ON_WITH_MOVEMENT, CMD_DELAYED_ON_WITHOUT_MOVEMENT);
    }

    private void handleQuitAlarm(Command command) throws IOException {
        if (command instanceof OnOffType && command == OnOffType.ON) {
            sendAction(CMD_QUIT);
        }
    }

    private void handleMotionSensors(Command command) throws IOException {
        if (command instanceof OnOffType) {
            if (command == OnOffType.ON) {
                sendAction(CMD_ENABLE_MOVEMENT);
            } else {
                sendAction(CMD_DISABLE_MOVEMENT);
            }
        }
    }

    private State getStateMotionSensors() {
        Double value = getStateDoubleValue(STATE_DISABLED_MOVE);
        if (value != null) {
            if (value == 1.0) {
                return OnOffType.OFF;
            }
            return OnOffType.ON;
        }
        return UnDefType.UNDEF;
    }
}
