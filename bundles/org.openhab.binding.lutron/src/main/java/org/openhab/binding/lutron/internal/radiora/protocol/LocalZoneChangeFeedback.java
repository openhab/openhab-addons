/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * Feedback for when a device was changed locally (not through Master Control)
 * <p>
 * <b>Syntax:</b>
 *
 * <pre>
 * {@code
 * LZC,<Zone Number>,<State>
 * }
 * </pre>
 *
 * <b>Examples:</b>
 * <p>
 * Dimmer 1 changed from 100% to 50%
 *
 * <pre>
 * LZC,01,CHG
 * </pre>
 *
 * Dimmer 4 changed from OFF to 25%
 *
 * <pre>
 * LZC,04,ON
 * </pre>
 *
 * In a bridged system, a system parameter S1 or S2 will be appended.
 *
 * <pre>
 * LZC,04,ON,S2
 * </pre>
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class LocalZoneChangeFeedback extends RadioRAFeedback {
    private final Logger logger = LoggerFactory.getLogger(LocalZoneChangeFeedback.class);

    private int zoneNumber; // 1 to 32
    private State state; // ON, OFF, CHG
    private int system; // 1 or 2, or 0 for none

    public enum State {
        ON,
        OFF,
        CHG
    }

    public LocalZoneChangeFeedback(String msg) {
        String[] params = parse(msg, 2);

        zoneNumber = Integer.parseInt(params[1].trim());
        state = State.valueOf(params[2].trim().toUpperCase());

        system = 0;
        if (params.length > 3) {
            String sysParam = params[3].trim().toUpperCase();
            if ("S1".equals(sysParam)) {
                system = 1;
            } else if ("S2".equals(sysParam)) {
                system = 2;
            } else {
                logger.debug("Invalid 3rd parameter {} in LZC message. Should be S1 or S2.", sysParam);
            }
        }
    }

    public State getState() {
        return state;
    }

    public int getZoneNumber() {
        return zoneNumber;
    }

    public int getSystem() {
        return system;
    }
}
