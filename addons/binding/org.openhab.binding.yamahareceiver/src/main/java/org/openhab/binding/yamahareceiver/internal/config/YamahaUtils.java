/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.config;

/**
 * Utilities used by the Yamaha binding
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class YamahaUtils {

    /**
     * Tries to parse a string into Enum, if unsuccessful returns null.
     * @param c
     * @param string
     * @param <T>
     * @return Enum value or null if unsuccessful
     */
    public static <T extends Enum<T>> T tryParseEnum(Class<T> c, String string) {
        if (string != null) {
            try {
                return Enum.valueOf(c, string);
            } catch (IllegalArgumentException ex) {
            }
        }
        return null;
    }
}
