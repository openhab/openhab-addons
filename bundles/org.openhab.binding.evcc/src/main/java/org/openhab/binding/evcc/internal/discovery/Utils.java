/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Utils} provides utility functions
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class Utils {
    /**
     * This method will check if the string for regex:[a-zA-Z0-9_-]+ and will replace those with - as well as german
     * "umlaut"s
     * It returns the original string if the regex doesn't match, otherwise the revised string
     */
    public static String sanatizeName(String name) {
        if (!name.matches("[a-zA-Z0-9_-]+")) {
            return name.replaceAll("ß", "ss").replaceAll("ä", "ae").replaceAll("ü", "ue").replaceAll("ö", "oe")
                    .replaceAll("[^a-zA-Z0-9_-]", "-");
        } else {
            return name;
        }
    }
}
