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
package org.openhab.binding.plugwise.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility class for sharing string utility methods.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public final class StringUtils {

    public static String lowerCamelToUpperUnderscore(String text) {
        return text.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
    }
}
