/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.silvercrestwifisocket.internal.enums;

/**
 * This enum represents the available Wifi Socket request types.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public enum SilvercrestWifiSocketRequestType {
    /** Request ON. */
    ON("010000FFFF04040404"),
    /** Request OFF. */
    OFF("01000000FF04040404"),
    /** Request Status. */
    GPIO_STATUS("020000000004040404"),
    /** Discover socket. The command has one placeholder for the mac address. */
    DISCOVERY("23%s0202");

    private String command;

    private SilvercrestWifiSocketRequestType(final String command) {
        this.command = command;
    }

    /**
     * Gets the hexadecimal command/format for include in request messages.
     *
     * @return the hexadecimal command/format
     */
    public String getCommand() {
        return this.command;
    }
}
