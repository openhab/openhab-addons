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
import java.util.HashSet;
import java.util.Set;

import org.openhab.binding.loxone.internal.types.LxState;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A jalousie type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a jalousie control covers:
 * <ul>
 * <li>Blinds</li>
 * <li>Automatic blinds</li>
 * <li>Automatic blinds integrated</li>
 * </ul>
 * <p>
 * Jalousie control has three channels:
 * <ul>
 * <li>0 (default) - rollershutter position</li>
 * <li>1 - shading command (always off switch, sending on triggers shading)</li>
 * <li>2 - automatic shading (on/off switch)</li>
 * </ul>
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlJalousie extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlJalousie(uuid);
        }

        @Override
        String getType() {
            return "jalousie";
        }
    }

    /**
     * Jalousie is moving up
     */
    private static final String STATE_UP = "up";
    /**
     * Jalousie is moving down
     */
    private static final String STATE_DOWN = "down";
    /**
     * The position of the Jalousie, a number from 0 to 1
     * Jalousie upper position = 0
     * Jalousie lower position = 1
     */
    private static final String STATE_POSITION = "position";
    /**
     * Only used by ones with Autopilot
     */
    private static final String STATE_AUTO_ACTIVE = "autoactive";
    /**
     * Command string used to set control's state to Full Down
     */
    private static final String CMD_FULL_DOWN = "FullDown";
    /**
     * Command string used to set control's state to Full Up
     */
    private static final String CMD_FULL_UP = "FullUp";
    /**
     * Command string used to stop rollershutter
     */
    private static final String CMD_STOP = "Stop";
    /**
     * Command to shade the jalousie
     */
    private static final String CMD_SHADE = "shade";
    /**
     * Command to enable automatic shading
     */
    private static final String CMD_AUTO = "auto";
    /**
     * Command to disable automatic shading
     */
    private static final String CMD_NO_AUTO = "NoAuto";

    private final Logger logger = LoggerFactory.getLogger(LxControlJalousie.class);
    private Double targetPosition;

    private LxControlJalousie(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        Set<String> blindsTags = new HashSet<>(tags);
        Set<String> switchTags = new HashSet<>(tags);
        blindsTags.add("Blinds");
        switchTags.add("Switchable");
        addChannel("Rollershutter", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_ROLLERSHUTTER),
                defaultChannelLabel, "Rollershutter", blindsTags, this::handleOperateCommands, this::getOperateState);
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH),
                defaultChannelLabel + " / Shade", "Rollershutter shading", switchTags, this::handleShadeCommands,
                () -> OnOffType.OFF);
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH),
                defaultChannelLabel + " / Auto Shade", "Rollershutter automatic shading", switchTags,
                this::handleAutoShadeCommands, this::getAutoShadeState);
    }

    private void handleOperateCommands(Command command) throws IOException {
        logger.debug("Command input {}", command);
        if (command instanceof PercentType) {
            if (PercentType.ZERO.equals(command)) {
                sendAction(CMD_FULL_UP);
            } else if (PercentType.HUNDRED.equals(command)) {
                sendAction(CMD_FULL_DOWN);
            } else {
                moveToPosition(((PercentType) command).doubleValue() / 100);
            }
        } else if (command instanceof UpDownType) {
            if ((UpDownType) command == UpDownType.UP) {
                sendAction(CMD_FULL_UP);
            } else {
                sendAction(CMD_FULL_DOWN);
            }
        } else if (command instanceof StopMoveType) {
            if ((StopMoveType) command == StopMoveType.STOP) {
                sendAction(CMD_STOP);
            }
        }
    }

    private PercentType getOperateState() {
        Double value = getStateDoubleValue(STATE_POSITION);
        if (value != null && value >= 0 && value <= 1) {
            // state UP or DOWN from Loxone indicates blinds are moving up or down
            // state UP in openHAB means blinds are fully up (0%) and DOWN means fully down (100%)
            // so we will update only position and not up or down states
            // a basic calculation like (value * 100.0) will give significant errors for some fractions, e.g.
            // 0.29 * 100 = 28.xxxxx which in turn if converted to integer will cause the position to take same value
            // for two different positions and later jump skipping one value (29 in this example)
            // for that reason we need to round the results of multiplication to avoid this skipping of percentages
            return new PercentType((int) Math.round(value * 100.0));
        }
        return null;
    }

    private void handleShadeCommands(Command command) throws IOException {
        if (command instanceof OnOffType) {
            if ((OnOffType) command == OnOffType.ON) {
                sendAction(CMD_SHADE);
            }
        }
    }

    private void handleAutoShadeCommands(Command command) throws IOException {
        if (command instanceof OnOffType) {
            if ((OnOffType) command == OnOffType.ON) {
                sendAction(CMD_AUTO);
            } else {
                sendAction(CMD_NO_AUTO);
            }
        }
    }

    private OnOffType getAutoShadeState() {
        Double value = getStateDoubleValue(STATE_AUTO_ACTIVE);
        if (value != null) {
            return value == 1.0 ? OnOffType.ON : OnOffType.OFF;
        }
        return null;
    }

    /**
     * Monitor jalousie position against desired target position and stop it if target position is reached.
     */
    @Override
    public void onStateChange(LxState state) {
        // check position changes
        if (STATE_POSITION.equals(state.getName()) && targetPosition != null && targetPosition >= 0
                && targetPosition <= 1) {
            // see in which direction jalousie is moving
            Object value = state.getStateValue();
            if (value instanceof Double) {
                Double currentPosition = (Double) value;
                Double upValue = getStateDoubleValue(STATE_UP);
                Double downValue = getStateDoubleValue(STATE_DOWN);
                if (upValue != null && downValue != null) {
                    if (((upValue == 1) && (currentPosition <= targetPosition))
                            || ((downValue == 1) && (currentPosition >= targetPosition))) {
                        targetPosition = null;
                        try {
                            sendAction(CMD_STOP);
                        } catch (IOException e) {
                            logger.debug("Error stopping jalousie when meeting target position.");
                        }
                    }
                }
            }
        } else {
            super.onStateChange(state);
        }
    }

    /**
     * Move the rollershutter (jalousie) to a desired position.
     * <p>
     * The jalousie will start moving in the desired direction based on the current position. It will stop moving once
     * there is a state update event received with value above/below (depending on direction) or equal to the set
     * position.
     *
     * @param position end position to move jalousie to, floating point number from 0..1 (0-fully closed to 1-fully
     *            open)
     * @throws IOException when something went wrong with communication
     */
    private void moveToPosition(Double position) throws IOException {
        Double currentPosition = getStateDoubleValue(STATE_POSITION);
        if (currentPosition != null && currentPosition >= 0 && currentPosition <= 1) {
            if (currentPosition > position) {
                logger.debug("Moving jalousie up from {} to {}", currentPosition, position);
                targetPosition = position;
                sendAction(CMD_FULL_UP);
            } else if (currentPosition < position) {
                logger.debug("Moving jalousie down from {} to {}", currentPosition, position);
                targetPosition = position;
                sendAction(CMD_FULL_DOWN);
            }
        }
    }
}
