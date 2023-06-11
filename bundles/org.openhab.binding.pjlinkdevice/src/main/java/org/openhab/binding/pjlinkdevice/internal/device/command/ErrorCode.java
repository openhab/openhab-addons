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
package org.openhab.binding.pjlinkdevice.internal.device.command;

import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Error codes as specified in <a href="https://pjlink.jbmia.or.jp/english/data_cl2/PJLink_5-1.pdf">[PJLinkSpec]</a>
 * chapters
 * 2.3. Set commands
 * 2.4. Get commands
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public enum ErrorCode {
    UNDEFINED_COMMAND("Undefined command", "ERR1"),
    OUT_OF_PARAMETER("Out of parameter", "ERR2"),
    UNAVAILABLE_TIME("Unavailable time", "ERR3"),
    DEVICE_FAILURE("Projector/Display failure", "ERR4");

    private String text;
    private String code;

    private ErrorCode(String text, String code) {
        this.text = text;
        this.code = code;
    }

    public static @Nullable ErrorCode getValueForCode(String code) {
        for (ErrorCode result : ErrorCode.values()) {
            if (result.code.equalsIgnoreCase(code)) {
                return result;
            }
        }

        return null;
    }

    public String getText() {
        return this.text;
    }

    /**
     * Checks if a the given code is an error code. Optionally, restrictCodesTo can restrict the allowed error codes
     * (based on PJLink specification).
     *
     * @param code string to be checked for error codes
     * @param restrictCodesTo list of expected error codes according to PJLink specification, can be null if all error
     *            codes (ERR1-ERR4) can be expected
     * @throws ResponseException
     */
    public static void checkForErrorStatus(String code, @Nullable Set<ErrorCode> restrictCodesTo)
            throws ResponseException {
        ErrorCode parsed = getValueForCode(code);
        if (parsed != null && (restrictCodesTo == null || restrictCodesTo.contains(parsed))) {
            throw new ResponseException(MessageFormat.format("Got error status {0} ({1})", parsed.getText(), code));
        }
    }
}
