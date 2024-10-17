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
package org.openhab.binding.insteon.internal.device;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InsteonEngine} represents an Insteon engine version
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public enum InsteonEngine {
    I1(0x00, false),
    I2(0x01, false),
    I2CS(0x02, true),
    UNKNOWN(0xFF, true);

    private static final Map<Integer, InsteonEngine> VERSION_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(engine -> engine.version, Function.identity()));

    private final int version;
    private final boolean checksum;

    private InsteonEngine(int version, boolean checksum) {
        this.version = version;
        this.checksum = checksum;
    }

    public boolean supportsChecksum() {
        return checksum;
    }

    /**
     * Factory method for getting a InsteonEngine from an Insteon engine version
     *
     * @param version the Insteon engine version
     * @return the Insteon engine object
     */
    public static InsteonEngine valueOf(int version) {
        return VERSION_MAP.getOrDefault(version, InsteonEngine.UNKNOWN);
    }
}
