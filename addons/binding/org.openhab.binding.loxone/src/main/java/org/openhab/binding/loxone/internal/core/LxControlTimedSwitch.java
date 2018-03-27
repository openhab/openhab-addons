/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

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
public class LxControlTimedSwitch extends LxControlPushbutton {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
            return new LxControlTimedSwitch(client, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

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
     * Get current value of the timed switch'es state.
     *
     * @return
     *         0 - switch off, 1 - switch on
     */
    @Override
    public Double getState() {
        /**
         * 0 = the output is turned off
         * -1 = the output is permanently on
         * otherwise it will count down from deactivationDelayTotal
         **/
        Double value = getStateValue(STATE_DEACTIVATION_DELAY);
        if (value != null) {
            if (value == -1 || value > 0) { // mapping
                return 1d;
            } else if (value == 0) {
                return 0d;
            }
        }
        return null;
    }

    /**
     * Get the time remaining to the switch off, in seconds
     *
     * @return deactivation delay in seconds
     *         Loxone also returns floating point values for the delay e.g. 9.99 seconds
     */
    public Double getDeactivationDelay() {
        return getStateValue(STATE_DEACTIVATION_DELAY);
    }
}
