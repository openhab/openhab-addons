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
 * An InfoOnlyDigital type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, this control covers digital virtual states only. This control does not send
 * any commands to the Miniserver. It can be used to read a formatted representation of a digital virtual state.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlInfoOnlyDigital extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
            return new LxControlInfoOnlyDigital(client, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * A name by which Miniserver refers to digital virtual state controls
     */
    private static final String TYPE_NAME = "infoonlydigital";
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
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            room to which control belongs
     * @param category
     *            category to which control belongs
     */
    LxControlInfoOnlyDigital(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room,
            LxCategory category) {
        super(client, uuid, json, room, category);
    }

    /**
     * Update Miniserver's control in runtime.
     *
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            New room that this control belongs to
     * @param category
     *            New category that this control belongs to
     */
    @Override
    void update(LxJsonControl json, LxContainer room, LxCategory category) {
        super.update(json, room, category);
        if (json.details != null && json.details.text != null) {
            textOn = json.details.text.on;
            textOff = json.details.text.off;
        }
    }

    /**
     * Obtain current value of the virtual state, expressed in a format configured on the Miniserver
     *
     * @return
     *         string for on/off value of the state or null if current value is not available
     */
    public String getFormattedValue() {
        Double value = getStateValue(STATE_ACTIVE);
        if (value != null) {
            if (value == 0) {
                return textOff;
            } else if (value == 1) {
                return textOn;
            }
        }
        return null;
    }

    /**
     * Obtain current value of a digital virtual state
     *
     * @return
     *         1 for ON, 0 for OFF and -1 if current value is not available
     */
    public Double getValue() {
        return getStateValue(STATE_ACTIVE);
    }
}
