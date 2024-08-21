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
package org.openhab.binding.lcn.internal.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Common definitions and helpers for the PCK protocol.
 *
 * @author Tobias Jüttner - Initial Contribution
 * @author Fabian Wolter - Migration to OH2
 */
@NonNullByDefault
public final class LcnDefs {
    /** Text encoding used by LCN-PCHK. */
    public static final Charset LCN_ENCODING = StandardCharsets.UTF_8;
    /** Number of thresholds registers of an LCN module */
    public static final int THRESHOLD_REGISTER_COUNT = 4;
    /** Number of key tables of an LCN module. */
    public static final int KEY_TABLE_COUNT = 4;
    /** Number of key tables of an LCN module before firmware 0C030C0. */
    public static final int KEY_TABLE_COUNT_UNTIL_0C030C0 = 3;
    /** Number of keys per table of an LCN module */
    public static final int KEY_COUNT = 8;
    /** Number of thresholds before LCN module firmware version 2013 */
    public static final int THRESHOLD_COUNT_BEFORE_2013 = 5;
    /**
     * Default dimmer output ramp when used with roller shutters. Results in a switching delay of 600ms. Value copied
     * from the LCN-PRO motor/shutter command dialog.
     */
    public static final int ROLLER_SHUTTER_RAMP_MS = 4000;
    /** Max. value of a variable, threshold or regulator setpoint */
    public static final int MAX_VARIABLE_VALUE = 32768;
    /** The fixed ramp when output 1+2 are controlled */
    public static final int FIXED_RAMP_MS = 250;
    /** Authentication at LCN-PCHK: Request user name. */
    public static final String AUTH_USERNAME = "Username:";
    /** Authentication at LCN-PCHK: Request password. */
    public static final String AUTH_PASSWORD = "Password:";
    /** LCN-PK/PKU is connected. */
    public static final String LCNCONNSTATE_CONNECTED = "$io:#LCN:connected";
    /** LCN-PK/PKU is disconnected. */
    public static final String LCNCONNSTATE_DISCONNECTED = "$io:#LCN:disconnected";
    /** LCN-PCHK/VISU has not enough licenses to handle this connection. */
    public static final String INSUFFICIENT_LICENSES = "$err:(license?)";

    /**
     * LCN dimming mode.
     * If solely modules with firmware 170206 or newer are present, LCN-PRO automatically programs {@link #NATIVE200}.
     * Otherwise the default is {@link #NATIVE50}.
     * Since LCN-PCHK doesn't know the current mode, it must explicitly be set.
     */
    public enum OutputPortDimMode {
        NATIVE50, // 0..50 dimming steps (all LCN module generations)
        NATIVE200 // 0..200 dimming steps (since 170206)
    }

    /**
     * Tells LCN-PCHK how to format output-port status-messages.
     * {@link #NATIVE} allows to show the status in half-percent steps (e.g. "10.5").
     * {@link #NATIVE} is completely backward compatible and there are no restrictions
     * concerning the LCN module generations. It requires LCN-PCHK 2.3 or higher though.
     */
    public enum OutputPortStatusMode {
        PERCENT, // Default (compatible with all versions of LCN-PCHK)
        NATIVE // 0..200 steps (since LCN-PCHK 2.3)
    }

    /** Possible states for LCN LEDs. */
    public enum LedStatus {
        OFF,
        ON,
        BLINK,
        FLICKER
    }

    /** Possible states for LCN logic-operations. */
    public enum LogicOpStatus {
        NOT,
        OR, // Note: Actually not correct since AND won't be OR also
        AND
    }

    /** Time units used for several LCN commands. */
    public enum TimeUnit {
        SECONDS,
        MINUTES,
        HOURS,
        DAYS
    }

    /** Relay-state modifiers used in LCN commands. */
    public enum RelayStateModifier {
        ON,
        OFF,
        TOGGLE,
        NOCHANGE
    }

    /** Value-reference for relative LCN variable commands. */
    public enum RelVarRef {
        CURRENT,
        PROG // Programmed value (LCN-PRO). Relevant for set-points and thresholds.
    }

    /** Command types used when sending LCN keys. */
    public enum SendKeyCommand {
        DONTSEND(0),
        HIT(1),
        MAKE(2),
        BREAK(3);

        private int id;

        SendKeyCommand(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static SendKeyCommand get(int id) {
            return Arrays.stream(values()).filter(v -> v.getId() == id).findAny()
                    .orElseThrow(NoSuchElementException::new);
        }
    }

    /** Key-lock modifiers used in LCN commands. */
    public enum KeyLockStateModifier {
        ON,
        OFF,
        TOGGLE,
        NOCHANGE
    }

    /** List of key tables of an LCN module */
    public enum KeyTable {
        A,
        B,
        C,
        D
    }

    /**
     * Generates an array of booleans from an input integer (actually a byte).
     *
     * @param inputByte the input byte (0..255)
     * @return the array of 8 booleans
     * @throws IllegalArgumentException if input is out of range (not a byte)
     */
    public static boolean[] getBooleanValue(int inputByte) throws IllegalArgumentException {
        if (inputByte < 0 || inputByte > 255) {
            throw new IllegalArgumentException();
        }
        boolean[] result = new boolean[8];
        for (int i = 0; i < 8; ++i) {
            result[i] = (inputByte & (1 << i)) != 0;
        }
        return result;
    }
}
