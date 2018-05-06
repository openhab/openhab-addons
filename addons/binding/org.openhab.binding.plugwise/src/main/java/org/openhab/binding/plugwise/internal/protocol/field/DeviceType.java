/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol.field;

/**
 * Enumerates Plugwise devices.
 *
 * @author Wouter Born - Initial contribution
 */
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
