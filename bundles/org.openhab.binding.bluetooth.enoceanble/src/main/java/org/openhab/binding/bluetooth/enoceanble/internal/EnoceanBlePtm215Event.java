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
package org.openhab.binding.bluetooth.enoceanble.internal;


public class EnoceanBlePtm215Event {

    private static final byte PRESSED = 0x1;

    private static final byte BUTTON1_DIR1 = 0x10;
    private static final byte BUTTON1_DIR2 = 0x8;
    private static final byte BUTTON2_DIR1 = 0x4;
    private static final byte BUTTON2_DIR2 = 0x2;

    private final byte byteState;

    public EnoceanBlePtm215Event(byte[] manufacturerData) {
        byteState = manufacturerData[4]; //TODO: Currently configured for BlueGiga. For bluez, this has to be manufacturerData[8]. This has to be fixed in Bluez / BlueGiga.
    }

    public boolean isPressed() {
        return checkFlag(PRESSED);
    }

    public boolean isButton1() {
        return checkFlag(BUTTON1_DIR1) || checkFlag(BUTTON1_DIR2);
    }

    public boolean isButton2() {
        return checkFlag(BUTTON2_DIR1) || checkFlag(BUTTON2_DIR2);
    }

    public boolean isDir1() {
        return checkFlag(BUTTON1_DIR1) || checkFlag(BUTTON2_DIR1);
    }

    public boolean isDir2() {
        return checkFlag(BUTTON1_DIR2) || checkFlag(BUTTON2_DIR2);
    }

    private boolean checkFlag(int flag) {
        return (byteState & flag) == flag;
    }

    @Override
    public String toString() {
        return "Button " + (isButton1() ? 1 : 2) + " Dir " + (isDir1() ? 1 : 2) + " " + (isPressed() ? "PRESSED" : "RELEASED");
    }
}
