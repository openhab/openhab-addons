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
 * A pushbutton type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a pushbutton control covers virtual input of type pushbutton
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlPushbutton extends LxControlSwitch {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
            return new LxControlPushbutton(client, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * A name by which Miniserver refers to pushbutton controls
     */
    private static final String TYPE_NAME = "pushbutton";
    /**
     * Command string used to set control's state to ON and OFF (tap)
     */
    private static final String CMD_PULSE = "Pulse";

    /**
     * Create pushbutton control object.
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
    LxControlPushbutton(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
        super(client, uuid, json, room, category);
    }

    /**
     * Set pushbutton to ON and to OFF (tap it).
     * <p>
     * Sends a command to operate the pushbutton.
     *
     * @throws IOException
     *             when something went wrong with communication
     */
    public void pulse() throws IOException {
        socketClient.sendAction(uuid, CMD_PULSE);
    }
}
