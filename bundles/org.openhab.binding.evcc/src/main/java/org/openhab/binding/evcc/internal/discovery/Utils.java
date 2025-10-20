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

import java.text.Normalizer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Utils} provides utility functions
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class Utils {

    /**
     * This method removes specific local characters (like ä, ö, ü), so we get a sanitized string
     * 
     * @param name that will be sanitized
     * @return a sanitized name that has replaced any invalid char
     */
    public static String sanitizeName(String name) {
        String result = name;
        if (!Normalizer.isNormalized(name, Normalizer.Form.NFKD)) {
            result = Normalizer.normalize(name, Normalizer.Form.NFKD);
            result = result.replaceAll("\\p{M}", "");
        }
        return result.replaceAll("[^a-zA-Z0-9_]", "-").toLowerCase();
    }
}
