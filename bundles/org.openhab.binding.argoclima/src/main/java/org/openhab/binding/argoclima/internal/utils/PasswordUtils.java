/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.argoclima.internal.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@code PasswordUtils} class provides password manipulation utilities for use in Argo API
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public final class PasswordUtils {
    /**
     * Get MD5 hash of the configured password (for Basic auth)
     *
     * @return MD5 hash of password
     * @throws NoSuchAlgorithmException In case MD5 is not available in the security provider
     *             (an impossible condition, hence not handling extra)
     */
    public static String md5HashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toLowerCase();
    }

    /**
     * Get the masked password used in authenticating to Argo server (for logging)
     *
     * @implNote Password length is preserved (which may be considered a security weakness, but is useful for
     *           troubleshooting and given state of Argo API's security... likely is an overkill already :)
     * @return {@code ***}-masked string instead of the same length as configured password
     */
    public static String maskPassword(String password) {
        return password.replaceAll(".", "*");
    }
}
