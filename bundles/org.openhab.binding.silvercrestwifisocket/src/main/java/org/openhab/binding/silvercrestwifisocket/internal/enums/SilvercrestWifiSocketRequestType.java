/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
