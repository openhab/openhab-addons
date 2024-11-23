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
package org.openhab.binding.echonetlite.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public class LangUtil {
    public static byte b(int i) {
        return (byte) (i & 0xFF);
    }

    public static String constantToVariable(CharSequence constant) {
        final StringBuilder sb = new StringBuilder();
        boolean shouldCapitalise = false;
        for (int i = 0, n = constant.length(); i < n; i++) {
            final char c = constant.charAt(i);
            if ('_' == c) {
                shouldCapitalise = true;
            } else if (shouldCapitalise) {
                sb.append(Character.toUpperCase(c));
                shouldCapitalise = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }
}
