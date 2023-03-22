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
package org.openhab.binding.mielecloud.internal.util;

import java.util.Locale;
import java.util.MissingResourceException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility for validating locales.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class LocaleValidator {
    private LocaleValidator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks whether the given string is a valid two letter language code.
     *
     * @param language The string to check.
     * @return Whether it is a valid language.
     */
    public static boolean isValidLanguage(String language) {
        try {
            String iso3Language = new Locale(language).getISO3Language();
            return iso3Language != null && !iso3Language.isEmpty();
        } catch (MissingResourceException e) {
            return false;
        }
    }
}
