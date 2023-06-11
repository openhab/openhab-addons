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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The possible outcomes of a command which can only be acknowledged or fail.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public enum AcknowledgeResponseValue {
    OK("Success", "OK");

    private String text;
    private String code;

    private AcknowledgeResponseValue(String text, String code) {
        this.text = text;
        this.code = code;
    }

    public String getText() {
        return this.text;
    }

    public static AcknowledgeResponseValue getValueForCode(String code) throws ResponseException {
        for (AcknowledgeResponseValue result : AcknowledgeResponseValue.values()) {
            if (result.code.equalsIgnoreCase(code)) {
                return result;
            }
        }

        throw new ResponseException("Cannot understand acknowledgement status: " + code);
    }
}
