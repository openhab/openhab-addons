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
 * A switch type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a switch control is:
 * <ul>
 * <li>a virtual input of switch type
 * <li>a push button function block
 * </ul>
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlDimmer extends LxControl {

    /**
     * A name by which Miniserver refers to switch controls
     */
    private static final String TYPE_NAME = "dimmer";

    /**
     * Switch has one state that can be on/off
     */
    private static final String STATE_POSITION = "position";
    private static final String STATE_MIN = "min";
    private static final String STATE_MAX = "max";
    private static final String STATE_STEP = "step";

    /**
     * Command string used to set control's state to ON
     */
    private static final String CMD_ON = "On";
    /**
     * Command string used to set control's state to OFF
     */
    private static final String CMD_OFF = "Off";

    /**
     * Create switch control object.
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
    LxControlDimmer(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
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

    public void setPosition(Double position) throws IOException {
        socketClient.sendAction(uuid, position.toString()); // find out loxone call
    }

    public void on() throws IOException {
        socketClient.sendAction(uuid, CMD_ON);
    }

    public void off() throws IOException {
        socketClient.sendAction(uuid, CMD_OFF);
    }

    /**
     * Get current value of the dimmer state.
     *
     * @return
     *         0 - full off, 100 - full on
     */
    public Double getPosition() {
        LxControlState state = getState(STATE_POSITION);
        if (state != null) {
            return state.getValue();
        }
        return null;
    }

    public Double getMax() {
        LxControlState state = getState(STATE_MAX);
        if (state != null) {
            return state.getValue();
        }
        return null;
    }

    public Double getMin() {
        LxControlState state = getState(STATE_MIN);
        if (state != null) {
            return state.getValue();
        }
        return null;
    }

}
