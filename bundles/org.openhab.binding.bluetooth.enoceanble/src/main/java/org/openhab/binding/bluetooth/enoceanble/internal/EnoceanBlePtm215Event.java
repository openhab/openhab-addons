/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EnoceanBlePtm215Event} class is parsing the BLE manufacturer data into an event object.
 *
 * @author Patrick Fink - Initial contribution
 */
@NonNullByDefault
public class EnoceanBlePtm215Event {

    private static final byte PRESSED = 0x1;

    private static final byte BUTTON1_DIR1 = 0x10;
    private static final byte BUTTON1_DIR2 = 0x8;
    private static final byte BUTTON2_DIR1 = 0x4;
    private static final byte BUTTON2_DIR2 = 0x2;

    private final byte byteState;
    private final int sequence;

    public EnoceanBlePtm215Event(byte[] manufacturerData) {
        byteState = manufacturerData[6];

        byte[] sequenceBytes = new byte[] { manufacturerData[5], manufacturerData[4], manufacturerData[3],
                manufacturerData[2] };
        ByteBuffer sequenceBytesBuffered = ByteBuffer.wrap(sequenceBytes); // big-endian by default
        sequence = sequenceBytesBuffered.getInt();
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

    public int getSequence() {
        return sequence;
    }

    @Override
    public String toString() {
        return "Button " + (isButton1() ? 1 : 2) + " Dir " + (isDir1() ? 1 : 2) + " "
                + (isPressed() ? "PRESSED" : "RELEASED") + " (seq. " + this.sequence + ")";
    }
}
