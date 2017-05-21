/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.core;

import java.util.Map;

/**
 * An InfoOnlyDigital type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, this control covers digital virtual states only. This control does not send
 * any commands to the Miniserver. It can be used to read a formatted representation of a digital virtual state.
 *
 * @author Pawel Pieczul - initial commit
 *
 */
public class LxControlInfoOnlyDigital extends LxControl {
    /**
     * A name by which Miniserver refers to digital virtual state controls
     */
    public static final String TYPE_NAME = "infoonlydigital";
    /**
     * InfoOnlyDigital has one state that can be on/off
     */
    private static final String STATE_ACTIVE = "active";

    private String textOn;
    private String textOff;

    /**
     * Create InfoOnlyDigital control object.
     *
     * @param client
     *            communication client used to send commands to the Miniserver
     * @param uuid
     *            control's UUID
     * @param name
     *            control's name
     * @param room
     *            room to which control belongs
     * @param category
     *            category to which control belongs
     * @param states
     *            control's states and their names
     * @param textOn
     *            string describing what it means when control in in ON state
     * @param textOff
     *            string describing what it means when control in in OFF state
     */
    LxControlInfoOnlyDigital(LxWsClient client, LxUuid uuid, String name, LxContainer room, LxCategory category,
            Map<String, LxControlState> states, String textOn, String textOff) {
        super(client, uuid, name, room, category, states);
        this.textOn = textOn;
        this.textOff = textOff;
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    /**
     * Obtain current value of the virtual state, expressed in a format configured on the Miniserver
     *
     * @return
     *         string for on/off value of the state or null if current value is not compatible with this control
     */
    public String getFormattedValue() {
        LxControlState state = getState(STATE_ACTIVE);
        if (state != null) {
            if (state.getValue() == 0) {
                return textOff;
            } else if (state.getValue() == 1) {
                return textOn;
            }
        }
        return null;
    }
}
