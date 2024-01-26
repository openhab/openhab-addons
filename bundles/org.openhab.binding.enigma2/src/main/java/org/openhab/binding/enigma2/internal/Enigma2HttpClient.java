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
package org.openhab.binding.enigma2.internal;

import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.HttpUtil;

/**
 * The {@link Enigma2HttpClient} class is responsible for sending HTTP-Get requests to the Enigma2 device.
 * It is devided from {@link Enigma2Client} for better testing purpose.
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
public class Enigma2HttpClient {
    public static final Pattern PATTERN = Pattern
            .compile("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFF]+");
    private final int timeout;

    public Enigma2HttpClient(int timeout) {
        this.timeout = timeout;
    }

    public String get(String url) throws IOException, IllegalArgumentException {
        String xml = HttpUtil.executeUrl("GET", url, timeout * 1000);
        // remove some unsupported xml-characters
        return PATTERN.matcher(xml).replaceAll("");
    }
}
