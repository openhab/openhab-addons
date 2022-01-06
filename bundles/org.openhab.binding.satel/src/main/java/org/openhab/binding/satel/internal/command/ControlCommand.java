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
package org.openhab.binding.satel.internal.command;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.protocol.SatelMessage;

/**
 * Base class for all commands that return result code in the response.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public abstract class ControlCommand extends SatelCommandBase {

    /**
     * Creates new command class instance.
     *
     * @param commandCode command code
     * @param payload command bytes
     */
    public ControlCommand(byte commandCode, byte[] payload) {
        super(commandCode, payload);
    }

    /**
     * Creates new command class instance.
     *
     * @param commandCode command code
     * @param payload command bytes
     * @param userCode user code
     */
    public ControlCommand(byte commandCode, byte[] payload, String userCode) {
        super(commandCode, appendUserCode(payload, userCode));
    }

    @Override
    protected boolean isResponseValid(SatelMessage response) {
        return true;
    }

    protected static byte[] appendUserCode(byte[] payload, String userCode) {
        byte[] userCodeBytes = userCodeToBytes(userCode);
        byte[] result = new byte[userCodeBytes.length + payload.length];
        System.arraycopy(userCodeBytes, 0, result, 0, userCodeBytes.length);
        System.arraycopy(payload, 0, result, userCodeBytes.length, payload.length);
        return result;
    }

    protected static byte[] userCodeToBytes(String userCode) {
        if (userCode.isEmpty()) {
            throw new IllegalArgumentException("User code is empty");
        }
        if (userCode.length() > 8) {
            throw new IllegalArgumentException("User code too long");
        }
        byte[] bytes = new byte[8];
        int digitsNbr = 2 * bytes.length;
        for (int i = 0; i < digitsNbr; ++i) {
            if (i < userCode.length()) {
                char digit = userCode.charAt(i);
                if (!Character.isDigit(digit)) {
                    throw new IllegalArgumentException("User code must contain digits only");
                }
                if (i % 2 == 0) {
                    bytes[i / 2] = (byte) ((digit - '0') << 4);
                } else {
                    bytes[i / 2] |= (byte) (digit - '0');
                }
            } else if (i % 2 == 0) {
                bytes[i / 2] = (byte) 0xff;
            } else if (i == userCode.length()) {
                bytes[i / 2] |= 0x0f;
            }
        }

        return bytes;
    }
}
