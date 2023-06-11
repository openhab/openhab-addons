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
package org.openhab.binding.linuxinput.internal.evdev4j.jnr;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Thomas Weißschuh - Initial contribution
 */
@NonNullByDefault
class Utils {
    private Utils() {
    }

    static String trimEnd(String suffix, String s) {
        if (s.endsWith(suffix)) {
            return s.substring(0, s.length() - suffix.length());
        }
        return s;
    }
}
