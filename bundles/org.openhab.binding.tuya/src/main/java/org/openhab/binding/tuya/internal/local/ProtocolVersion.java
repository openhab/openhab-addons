/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tuya.internal.local;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ProtocolVersion} maps the protocol version String to a an enum value
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public enum ProtocolVersion {
    V3_1("3.1"),
    V3_3("3.3"),
    V3_4("3.4"),
    V3_5("3.5");

    private final String versionString;

    ProtocolVersion(String versionString) {
        this.versionString = versionString;
    }

    public byte[] getBytes() {
        return versionString.getBytes(StandardCharsets.UTF_8);
    }

    public String getString() {
        return versionString;
    }

    public static ProtocolVersion fromString(String version) {
        return Arrays.stream(values()).filter(t -> t.versionString.equals(version)).findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown version " + version));
    }
}
