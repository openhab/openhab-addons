/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;

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
        return result.replaceAll("[^a-zA-Z0-9_]", "-").toLowerCase(Locale.ROOT);
    }

    /**
     * This method creates a stable ID string based on the provided list of values (cut down to first 10 hex chars of
     * SHA-256)
     *
     * @param values list of strings to create the ID from
     * @return a stable ID string
     */
    public static String createIdString(List<String> values) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(String.join("", values).getBytes(StandardCharsets.UTF_8));
            return HexUtils.bytesToHex(digest).substring(0, 10);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
