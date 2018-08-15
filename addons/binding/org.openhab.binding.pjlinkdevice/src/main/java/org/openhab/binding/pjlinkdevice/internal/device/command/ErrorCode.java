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
import java.util.HashMap;
import java.util.Set;

/**
 * @author Nils Schnabel - Initial contribution
 */
public enum ErrorCode {
    UNDEFINED_COMMAND,
    OUT_OF_PARAMETER,
    UNAVAILABLE_TIME,
    DEVICE_FAILURE;

    public static ErrorCode getValueForCode(String code) throws ResponseException {
        final HashMap<String, ErrorCode> codes = new HashMap<String, ErrorCode>();
        codes.put("ERR1", UNDEFINED_COMMAND);
        codes.put("ERR2", OUT_OF_PARAMETER);
        codes.put("ERR3", UNAVAILABLE_TIME);
        codes.put("ERR4", DEVICE_FAILURE);
        return codes.get(code);
    }

    public String getText() throws ResponseException {
        final HashMap<ErrorCode, String> texts = new HashMap<ErrorCode, String>();
        texts.put(UNDEFINED_COMMAND, "Undefined command");
        texts.put(OUT_OF_PARAMETER, "Out of parameter");
        texts.put(UNAVAILABLE_TIME, "Unavailable time");
        texts.put(DEVICE_FAILURE, "Projector/Display failure");
        return texts.get(this);
    }

    public static void checkForErrorStatus(String code, Set<ErrorCode> restrictCodesTo) throws ResponseException {
        ErrorCode parsed = getValueForCode(code);
        if (parsed != null && (restrictCodesTo == null || restrictCodesTo.contains(parsed))) {
            throw new ResponseException(MessageFormat.format("Got error status {0} ({1})", parsed.getText(), code));
        }
    }
}
