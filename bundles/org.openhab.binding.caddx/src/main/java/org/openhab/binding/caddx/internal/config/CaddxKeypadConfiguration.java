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
package org.openhab.binding.caddx.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration class for the Caddx Keypad Thing.
 *
 * @author Georgios Moutsos - Initial contribution
 */

@NonNullByDefault
public class CaddxKeypadConfiguration {

    // Keypad Thing constants
    public static final String KEYPAD_ADDRESS = "keypadAddress";
    public static final String TERMINAL_MODE_SECONDS = "terminalModeSeconds";

    private int keypadAddress;
    private int terminalModeSeconds;

    public int getKeypadAddress() {
        return keypadAddress;
    }

    public int getTerminalModeSeconds() {
        return terminalModeSeconds;
    }
}
