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
package org.openhab.binding.yamahareceiver.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utilities used by the Yamaha binding
 *
 * @author Tomasz Maruszak - Initial contribution
 */
@NonNullByDefault
public class YamahaUtils {

    /**
     * Tries to parse a string into Enum, if unsuccessful returns null.
     *
     * @param c
     * @param string
     * @param <T>
     * @return Enum value or null if unsuccessful
     */
    public static @Nullable <T extends Enum<T>> T tryParseEnum(Class<T> c, @Nullable String string) {
        if (string != null) {
            try {
                return Enum.valueOf(c, string);
            } catch (IllegalArgumentException ex) {
            }
        }
        return null;
    }
}
