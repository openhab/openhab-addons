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
package org.openhab.binding.insteon.internal.device;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class that represents Insteon engine
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public enum InsteonEngine {
    I1(0x00, false),
    I2(0x01, false),
    I2CS(0x02, true),
    UNKNOWN(0xFF, false);

    private static Map<Integer, InsteonEngine> map = new HashMap<>();
    static {
        for (InsteonEngine engine : InsteonEngine.values()) {
            map.put(engine.version, engine);
        }
    }

    private int version;
    private boolean checksum;

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
        return map.getOrDefault(version, InsteonEngine.UNKNOWN);
    }
}
