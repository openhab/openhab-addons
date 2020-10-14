/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.clearone.internal;

import static org.openhab.binding.clearone.internal.ClearOneBindingConstants.XAP_CMD_UID;

/**
 * Class that parses messages from the Stack.
 *
 * @author Garry Mitchell - Initial contribution
 */
public class Message {

    private String message = "";
    public String typeId = "";
    public int zoneId;
    public String unitId = "";
    public String commandId = "";
    public String data;

    String start = "";

    /**
     * Constructor.
     *
     * @param message
     *            - the message received
     */
    public Message(String message) {
        this.message = message;
        zoneId = -1;
        processMessage();
    }

    /**
     * Processes the incoming message and extracts the information.
     */
    private void processMessage() {

        // Messages start with "OK> #"

        if (message.length() <= 4) {
            return;
        }
        start = message.substring(0, 5);
        if (!start.equals("OK> #")) {
            return;
        }

        typeId = message.substring(5, 6);
        unitId = message.substring(6, 7);
        commandId = message.substring(8, message.indexOf(" ", 8));
        data = message.substring(message.indexOf(" ", 8) + 1, message.length());
        switch (commandId) {
            case XAP_CMD_UID:
                break;
            default:
                break;
        }
    }
}
