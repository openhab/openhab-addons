/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.plugwise.internal.protocol.field;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumerates Plugwise devices.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public enum DeviceType {

    STICK("Stick", false, false),
    CIRCLE("Circle", true, false),
    CIRCLE_PLUS("Circle+", true, false),
    SCAN("Scan", false, true),
    SENSE("Sense", false, true),
    STEALTH("Stealth", true, false),
    SWITCH("Switch", false, true),
    UNKNOWN("Unknown", false, false);

    private final String string;
    private final boolean relayDevice;
    private final boolean sleepingEndDevice;

    DeviceType(String string, boolean relayDevice, boolean sleepingEndDevice) {
        this.string = string;
        this.relayDevice = relayDevice;
        this.sleepingEndDevice = sleepingEndDevice;
    }

    public boolean isRelayDevice() {
        return relayDevice;
    }

    public boolean isSleepingEndDevice() {
        return sleepingEndDevice;
    }

    @Override
    public String toString() {
        return string;
    }
}
