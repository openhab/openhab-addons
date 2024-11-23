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
package org.openhab.binding.lutron.internal.radiora.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feedback that gives the state of all zones
 * <p>
 * <b>Syntax:</b>
 *
 * <pre>
 * {@code
 * ZMP,<Zone States>
 * }
 * </pre>
 *
 * <b>Example:</b>
 * <p>
 * Zones 2 and 9 are ON, all others are OFF, and Zones 31 and 32 are unassigned.
 * In a bridged system, a system parameter S1 or S2 will be appended.
 *
 * <pre>
 * ZMP,010000001000000000000000000000XX
 * ZMP,00100000010000000000000000000000,S2
 * </pre>
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class ZoneMapFeedback extends RadioRAFeedback {
    private final Logger logger = LoggerFactory.getLogger(ZoneMapFeedback.class);

    private String zoneStates; // 32 bit String of (0,1,X)
    private int system; // 1 or 2, or 0 for none

    public ZoneMapFeedback(String msg) {
        String[] params = parse(msg, 1);

        zoneStates = params[1];

        system = 0;
        if (params.length > 2) {
            String sysParam = params[2].trim().toUpperCase();
            if ("S1".equals(sysParam)) {
                system = 1;
            } else if ("S2".equals(sysParam)) {
                system = 2;
            } else {
                logger.debug("Invalid 2nd parameter {} in ZMP message. Should be S1 or S2.", sysParam);
            }
        }
    }

    public String getZoneStates() {
        return zoneStates;
    }

    public char getZoneValue(int zone) {
        if (zone < 1 || zone > zoneStates.length()) {
            return 'X';
        }

        return zoneStates.charAt(zone - 1);
    }

    public int getSystem() {
        return system;
    }
}
