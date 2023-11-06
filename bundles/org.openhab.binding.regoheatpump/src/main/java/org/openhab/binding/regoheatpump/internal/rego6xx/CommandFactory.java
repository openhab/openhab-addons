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
package org.openhab.binding.regoheatpump.internal.rego6xx;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CommandFactory} is responsible for creating different commands that can
 * be send to a rego 6xx unit.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class CommandFactory {
    private static final byte DEVICE_ADDRESS = (byte) 0x81;

    public static byte[] createReadRegoVersionCommand() {
        return createReadCommand((byte) 0x7f, (short) 0);
    }

    public static byte[] createReadFromSystemRegisterCommand(short address) {
        return createReadCommand((byte) 0x02, address);
    }

    public static byte[] createWriteToSystemRegisterCommand(short address, short data) {
        return createCommand((byte) 0x03, address, data);
    }

    public static byte[] createReadFromDisplayCommand(short displayLine) {
        return createReadCommand((byte) 0x20, displayLine);
    }

    public static byte[] createReadLastErrorCommand() {
        return createReadCommand((byte) 0x40, (short) 0);
    }

    public static byte[] createReadFromFrontPanelCommand(short address) {
        return createReadCommand((byte) 0x00, address);
    }

    private static byte[] createReadCommand(byte source, short address) {
        return createCommand(source, address, (short) 0);
    }

    private static byte[] createCommand(byte source, short address, short data) {
        byte[] addressBytes = ValueConverter.shortToSevenBitFormat(address);
        byte[] dataBytes = ValueConverter.shortToSevenBitFormat(data);
        return new byte[] { DEVICE_ADDRESS, source, addressBytes[0], addressBytes[1], addressBytes[2], dataBytes[0],
                dataBytes[1], dataBytes[2], Checksum.calculate(addressBytes, dataBytes) };
    }
}
