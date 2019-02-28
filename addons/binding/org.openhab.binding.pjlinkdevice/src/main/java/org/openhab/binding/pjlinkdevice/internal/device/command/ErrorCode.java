/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

/**
 * @author Nils Schnabel - Initial contribution
 */
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

    public static ErrorCode getValueForCode(String code) throws ResponseException {
        for (ErrorCode result : ErrorCode.values()) {
            if (result.code.equals(code)) {
                return result;
            }
        }

        return null;
    }

    public String getText() throws ResponseException {
        return this.text;
    }

    public static void checkForErrorStatus(String code, Set<ErrorCode> restrictCodesTo) throws ResponseException {
        ErrorCode parsed = getValueForCode(code);
        if (parsed != null && (restrictCodesTo == null || restrictCodesTo.contains(parsed))) {
            throw new ResponseException(MessageFormat.format("Got error status {0} ({1})", parsed.getText(), code));
        }
    }
}
