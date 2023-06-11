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
package org.openhab.binding.linuxinput.internal.evdev4j;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

import jnr.constants.Constant;

/**
 * @author Thomas Weißschuh - Initial contribution
 */
@NonNullByDefault
public class Utils {
    private Utils() {
    }

    @SafeVarargs
    static <T extends Constant> int combineFlags(Class<T> klazz, T... flags) {
        if (klazz == Constant.class) {
            throw new IllegalArgumentException();
        }
        int result = 0;
        for (Constant c : flags) {
            result |= c.intValue();
        }
        return result;
    }

    public static <T extends Constant> Optional<T> constantFromInt(T[] cs, int i) {
        for (T c : cs) {
            if (c.intValue() == i) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }
}
