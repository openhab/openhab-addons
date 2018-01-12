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
 * A Text State type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a text state represents a State functional block on the Miniserver
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlTextState extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
            return new LxControlTextState(client, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * A name by which Miniserver refers to text state controls
     */
    private static final String TYPE_NAME = "textstate";

    /**
     * A state which will receive an update of possible Text State values)
     */
    private static final String STATE_TEXT_AND_ICON = "textandicon";

    /**
     * Create text state object.
     *
     * @param client
     *            communication client used to send commands to the Miniserver
     * @param uuid
     *            controller's UUID
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            room to which controller belongs
     * @param category
     *            category to which controller belongs
     */
    LxControlTextState(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
        super(client, uuid, json, room, category);
    }

    /**
     * Return current text value of the state
     *
     * @return
     *         string with current value
     */
    public String getText() {
        return getStateTextValue(STATE_TEXT_AND_ICON);
    }
}
