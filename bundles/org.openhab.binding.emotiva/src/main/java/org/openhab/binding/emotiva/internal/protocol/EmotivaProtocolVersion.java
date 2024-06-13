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
package org.openhab.binding.emotiva.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enum for mapping Emotiva Network Protocol versions.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public enum EmotivaProtocolVersion {

    PROTOCOL_V2("2.0"),
    PROTOCOL_V3("3.0");

    private final String protocolVersion;

    EmotivaProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public static EmotivaProtocolVersion protocolFromConfig(String protocolVersion) {
        for (EmotivaProtocolVersion value : values()) {
            if (protocolVersion.equals(value.protocolVersion)) {
                return value;
            }
        }
        return PROTOCOL_V2;
    }

    public String value() {
        return protocolVersion;
    }
}
