/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility static class to perform some validations.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public final class ValidationUtils {

    private ValidationUtils() {
        // avoid instantiation.
    }

    public static final String MAC_PATTERN = "^([0-9A-Fa-f]{2}[:-]*){5}([0-9A-Fa-f]{2})$";
    private static final Pattern pattern = Pattern.compile(ValidationUtils.MAC_PATTERN);

    /**
     * Validates if one Mac address is valid.
     *
     * @param mac the mac, with or without :
     * @return true if is valid.
     */
    public static boolean isMacValid(final String mac) {
        Matcher matcher = pattern.matcher(mac);
        return matcher.matches();
    }

    /**
     * Validates if one Mac address is not valid.
     *
     * @param mac the mac, with or without :
     * @return true if is not valid.
     */
    public static boolean isMacNotValid(final String macAddress) {
        return !isMacValid(macAddress);
    }
}
