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
package org.openhab.binding.elkm1.internal.elk;

/**
 * The basic default elk message that everything else will use as a base.
 *
 * @author David Bennett - Initial Contribution
 */

public abstract class ElkMessage {
    // Has the two char mesage type in it.
    private final ElkCommand command;

    public ElkMessage(ElkCommand command) {
        this.command = command;
    }

    public ElkCommand getElkCommand() {
        return command;
    }

    protected abstract String getData();

    // Format of the packet is:
    // 2 chars length (in hex)
    // 2 chars message type (ie: AC)
    // 1 char sub message type
    // data D... (in hex format)
    // Two reserved elements (always 00)
    // Two chars for checksum.

    // This is the whole message without the checksum.
    private String getFullBodyText() {
        int length = getData().length() + 6;
        String len = String.format("%02X", length);
        return len + command.getValue() + getData() + "00";
    }

    private int checksum(String str) {
        int checksum = 0;
        for (char input : getFullBodyText().toCharArray()) {
            checksum += input;
        }
        return ~checksum + 1;
    }

    /**
     * The full text of the message to send.
     */
    public String getSendableMessage() {
        String str = getFullBodyText();
        return str + String.format("%02X", checksum(str) & 0xff);
    }
}
