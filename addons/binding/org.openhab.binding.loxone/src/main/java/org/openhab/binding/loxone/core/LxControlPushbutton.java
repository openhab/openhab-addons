/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.core;

import java.io.IOException;
import java.util.Map;

/**
 * A pushbutton type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a pushbutton control covers virtual input of type pushbutton
 *
 * @author Pawel Pieczul - initial commit
 *
 */
public class LxControlPushbutton extends LxControlSwitch {
    /**
     * A name by which Miniserver refers to pushbutton controls
     */
    public final static String TYPE_NAME = "pushbutton";
    /**
     * Command string used to set control's state to ON and OFF (tap)
     */
    private final static String CMD_PULSE = "Pulse";

    /**
     * Create pushbutton control object.
     *
     * @param client
     *            communication client used to send commands to the Miniserver
     * @param uuid
     *            switch's UUID
     * @param name
     *            switch's name
     * @param room
     *            room to which switch belongs
     * @param category
     *            category to which switch belongs
     * @param states
     *            switch's states and their names (expecting one object with "active" name)
     */
    LxControlPushbutton(LxWsClient client, LxUuid uuid, String name, LxContainer room, LxCategory category,
            Map<String, LxControlState> states) {
        super(client, uuid, name, room, category, states);
    }

    /**
     * Set pushbutton to ON and to OFF (tap it).
     * <p>
     * Sends a command to operate the pushbutton.
     *
     * @throws IOException
     *             when something went wrong with communication
     */
    public void Pulse() throws IOException {
        socketClient.sendAction(uuid, CMD_PULSE);
    }
}
