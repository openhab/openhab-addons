/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is a workaround class for dealing with localization.
 *
 * It provides the following methods:
 * <ul>
 * <li>{@link #getText} returns the localized message.</li>
 * </ul>
 * <p>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class Localization {

    /*
     * ************************
     * ***** Constructors *****
     */

    /**
     * Suppress default constructor for creating a non-instantiable class.
     */
    private Localization() {
        throw new AssertionError();
    }

    /**
     * Converts a given message into an equivalent localized message.
     *
     * @param key the message of type {@link String} to be converted,
     * @param arguments (optional) arguments being referenced within the messageString.
     * @return <B>localizedMessageString</B> the resulted message of type {@link String}.
     */
    public static String getText(String key, Object... arguments) {
        // ToDo: a well-working solution still to be found
        String text = String.format(key, arguments);
        return text;
    }

}
