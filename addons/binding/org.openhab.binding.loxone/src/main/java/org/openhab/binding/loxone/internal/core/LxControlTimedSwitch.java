/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
 * A timed switch type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a switch control is:
 * <ul>
 * <li>a virtual input of switch type
 * <li>a push button function block
 * </ul>
 *
 * @author Stephan Brunner
 *
 */
public class LxControlTimedSwitch extends LxControl {

    /**
     * A name by which Miniserver refers to timed switch controls
     */
    private static final String TYPE_NAME = "timedswitch";

    /**
     * deactivationDelay - countdown until the output is deactivated.
     * 0 = the output is turned off
     * -1 = the output is permanently on
     * otherwise it will count down from deactivationDelayTotal
     */
    private static final String STATE_DEACTIVATION_DELAY = "deactivationdelay";

    /**
     * Command string used to set timed switch to ON without deactivation delay
     */
    private static final String CMD_ON = "On";

    /**
     * Command string used to set timed switch to ON with deactivation delay
     */
    private static final String CMD_PULSE = "pulse";

    /**
     * Command string used to set timed switch to OFF
     */
    private static final String CMD_OFF = "Off";

    /**
     * Create timed switch control object.
     *
     * @param client
     *            communication client used to send commands to the Miniserver
     * @param uuid
     *            switch's UUID
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            room to which switch belongs
     * @param category
     *            category to which switch belongs
     */
    LxControlTimedSwitch(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
        super(client, uuid, json, room, category);
    }

    /**
     * Check if control accepts provided type name from the Miniserver
     *
     * @param type
     *            name of the type received from Miniserver
     * @return
     *         true if this control is suitable for this type
     */
    public static boolean accepts(String type) {
        return type.equalsIgnoreCase(TYPE_NAME);
    }

    /**
     * Set timed switch to ON without deactivation delay.
     * <p>
     * Sends a command to operate the timed switch.
     *
     * @throws IOException
     *             when something went wrong with communication
     */
    public void on() throws IOException {
        socketClient.sendAction(uuid, CMD_ON);
    }

    /**
     * Set timed switch to ON with deactivation delay.
     * <p>
     * Sends a command to operate the timed switch.
     *
     * @throws IOException
     *             when something went wrong with communication
     */
    public void pulse() throws IOException {
        socketClient.sendAction(uuid, CMD_PULSE);
    }

    /**
     * Set timed switch to OFF.
     * <p>
     * Sends a command to operate the timed switch.
     *
     * @throws IOException
     *             when something went wrong with communication
     */
    public void off() throws IOException {
        socketClient.sendAction(uuid, CMD_OFF);
    }

    /**
     * Get current value of the timed switch'es state.
     *
     * @return
     *         0 - switch off, 1 - switch on
     */
    public Double getState() {

        /**
         * 0 = the output is turned off
         * -1 = the output is permanently on
         * otherwise it will count down from deactivationDelayTotal
         **/
        LxControlState state = getState(STATE_DEACTIVATION_DELAY);
        if (state != null && state.getValue() != null) {
            if (state.getValue() == -1 || state.getValue() > 0) { // mapping
                return 1d;
            } else if (state.getValue() == 0) {
                return 0d;
            }
        }
        return null;
    }

    /**
     *
     * @return deactivation delay in seconds
     *         Loxone also returns floating point values for the delay e.g. 9.99 seconds
     */
    public Double getDeactivationDelay() {
        LxControlState state = getState(STATE_DEACTIVATION_DELAY);
        if (state != null) {
            return state.getValue();
        }
        return null;
    }
}
