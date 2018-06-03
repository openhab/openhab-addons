/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.io.IOException;

import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;

/**
 * A jalousie type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a jalousie control covers:
 * <ul>
 * <li>Blinds
 * <li>Automatic blinds
 * <li>Automatic blinds integrated
 * </ul>
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlJalousie extends LxControl implements LxControlStateListener {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
            return new LxControlJalousie(client, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * A name by which Miniserver refers to jalousie controls
     */
    private static final String TYPE_NAME = "jalousie";

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
     * The shade position of the Jalousie (blinds), a number from 0 to 1
     * Blinds are not shaded = 0
     * Blinds are shaded = 1
     */
    @SuppressWarnings("unused")
    private static final String STATE_SHADE_POSITION = "shadeposition";
    /**
     * Only used by ones with Autopilot, this represents the safety shutdown
     */
    @SuppressWarnings("unused")
    private static final String STATE_SAFETY_ACTIVE = "safetyactive";
    /**
     * Only used by ones with Autopilot
     */
    @SuppressWarnings("unused")
    private static final String STATE_AUTO_ALLOWED = "autoallowed";
    /**
     * Only used by ones with Autopilot
     */
    @SuppressWarnings("unused")
    private static final String STATE_AUTO_ACTIVE = "autoactive";
    /**
     * Only used by ones with Autopilot, this represents the output QI in Loxone Config
     */
    @SuppressWarnings("unused")
    private static final String STATE_LOCKED = "locked";

    /**
     * Command string used to set control's state to Down
     */
    @SuppressWarnings("unused")
    private static final String CMD_DOWN = "Down";
    /**
     * Command string used to set control's state to Up
     */
    @SuppressWarnings("unused")
    private static final String CMD_UP = "Up";
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

    private Double targetPosition;

    /**
     * Create jalousie control object.
     *
     * @param client
     *            communication client used to send commands to the Miniserver
     * @param uuid
     *            jalousie's UUID
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            room to which jalousie belongs
     * @param category
     *            category to which jalousie belongs
     */
    LxControlJalousie(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
        super(client, uuid, json, room, category);
        addStateListener(STATE_POSITION, this);
    }

    /**
     * Set rollershutter (jalousie) to full up position.
     * <p>
     * Sends a command to operate the rollershutter.
     *
     * @throws IOException
     *             when something went wrong with communication
     */
    public void fullUp() throws IOException {
        socketClient.sendAction(uuid, CMD_FULL_UP);
    }

    /**
     * Set rollershutter (jalousie) to full down position.
     * <p>
     * Sends a command to operate the rollershutter.
     *
     * @throws IOException
     *             when something went wrong with communication
     */
    public void fullDown() throws IOException {
        socketClient.sendAction(uuid, CMD_FULL_DOWN);
    }

    /**
     * Stop movement of the rollershutter (jalousie)
     * <p>
     * Sends a command to operate the rollershutter.
     *
     * @throws IOException
     *             when something went wrong with communication
     */
    public void stop() throws IOException {
        socketClient.sendAction(uuid, CMD_STOP);
    }

    /**
     * Move the rollershutter (jalousie) to a desired position.
     * <p>
     * The jalousie will start moving in the desired direction based on the current position. It will stop moving once
     * there is a state update event received with value above/below (depending on direction) or equal to the set
     * position.
     *
     * @param position
     *            end position to move jalousie to, floating point number from 0..1 (0-fully closed to 1-fully open)
     * @throws IOException
     *             when something went wrong with communication
     */
    public void moveToPosition(Double position) throws IOException {
        Double currentPosition = getPosition();
        if (currentPosition != null && currentPosition >= 0 && currentPosition <= 1) {
            if (currentPosition > position) {
                logger.debug("Moving jalousie up from {} to {}", currentPosition, position);
                targetPosition = position;
                fullUp();
            } else if (currentPosition < position) {
                logger.debug("Moving jalousie down from {} to {}", currentPosition, position);
                targetPosition = position;
                fullDown();
            }
        }
    }

    /**
     * Get current position of the rollershutter (jalousie)
     *
     * @return
     *         a floating point number from range 0-fully closed to 1-fully open or null if position not available
     */
    public Double getPosition() {
        return getStateValue(STATE_POSITION);
    }

    /**
     * Monitor jalousie position against desired target position and stop it if target position is reached.
     */
    @Override
    public void onStateChange(LxControlState state) {
        // check position changes
        if (STATE_POSITION.equals(state.getName()) && targetPosition != null && targetPosition > 0
                && targetPosition < 1) {
            // see in which direction jalousie is moving
            Double currentPosition = state.getValue();
            Double upValue = getStateValue(STATE_UP);
            Double downValue = getStateValue(STATE_DOWN);
            if (currentPosition != null && upValue != null && downValue != null) {
                if (((upValue == 1) && (currentPosition <= targetPosition))
                        || ((downValue == 1) && (currentPosition >= targetPosition))) {
                    targetPosition = null;
                    try {
                        stop();
                    } catch (IOException e) {
                        logger.debug("Error stopping jalousie when meeting target position.");
                    }
                }
            }
        }
    }
}
