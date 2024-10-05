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
package org.openhab.binding.linktap.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Firmware} class defines the firmware version.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class Firmware {
    String raw;
    int buildVer;
    int hwVer;

    public Firmware(final @Nullable String fwVersion) {
        raw = "S00000000";
        hwVer = 0;
        buildVer = 0;

        if (fwVersion == null || fwVersion.length() < 7) {
            return;
        } else {
            raw = fwVersion;
            buildVer = Integer.parseInt(raw.substring(1, 7));
        }

        switch (fwVersion.charAt(0)) {
            case 'G':
                hwVer = 2;
                break;
            case 'S':
                hwVer = 1;
                break;
            default:
                break;
        }
    }

    public boolean supportsLocalConfig() {
        return buildVer >= 60883;
    }

    public boolean supportsMDNS() {
        return buildVer >= 60880;
    }

    public String generateTestedRevisionForHw(final int versionNo) {
        return String.format("%c%05d", raw.charAt(0), versionNo);
    }

    public String getRecommendedMinVer() {
        return generateTestedRevisionForHw(60883);
    }
}
