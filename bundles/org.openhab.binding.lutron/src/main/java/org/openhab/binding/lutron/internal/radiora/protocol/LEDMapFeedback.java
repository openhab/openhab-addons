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
package org.openhab.binding.lutron.internal.radiora.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Feedback (LMP) that gives the state of all phantom LEDs
 * <p>
 * <b>Syntax:</b>
 *
 * <pre>
 * {@code
 * LMP,<LED States>
 * }
 * </pre>
 *
 * <b>Example:</b>
 * <p>
 * Phantom LEDs 1 and 5 are ON, all others are OFF
 *
 * <pre>
 * LMP,100010000000000
 * </pre>
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class LEDMapFeedback extends RadioRAFeedback {

    private String bitmap; // 15 bit String of (0,1). 1 is ON, 0 is OFF

    public LEDMapFeedback(String msg) {
        String[] params = parse(msg, 1);

        bitmap = params[1];
    }

    public String getBitmap() {
        return bitmap;
    }

    public char getZoneValue(int zone) {
        if (zone < 1 || zone > bitmap.length()) {
            return '0';
        }

        return bitmap.charAt(zone - 1);
    }
}
