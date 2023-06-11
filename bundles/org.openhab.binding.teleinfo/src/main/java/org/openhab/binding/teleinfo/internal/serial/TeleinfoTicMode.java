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
package org.openhab.binding.teleinfo.internal.serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Define an enum for TIC mode of Linky telemeters
 *
 * @author Olivier MARCEAU - Initial contribution
 */
@NonNullByDefault
public enum TeleinfoTicMode {
    HISTORICAL(1200, "\\s"),
    STANDARD(9600, "\\t");

    private final int bitrate;
    private final String separator;

    TeleinfoTicMode(int bitrate, String separator) {
        this.bitrate = bitrate;
        this.separator = separator;
    }

    public int getBitrate() {
        return bitrate;
    }

    public String getSeparator() {
        return separator;
    }
}
