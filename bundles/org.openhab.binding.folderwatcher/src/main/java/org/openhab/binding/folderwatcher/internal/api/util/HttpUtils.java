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
package org.openhab.binding.folderwatcher.internal.api.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HttpUtils} class contains metohdos related to HTTP(S).
 * <p>
 * Based on offical AWS example {@see https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-examples-using-sdks.html}
 * 
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class HttpUtils {
    public static String urlEncode(String url, boolean keepPathSlash) {
        String encoded;
        try {
            encoded = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding is not supported.", e);
        }
        if (keepPathSlash) {
            encoded = encoded.replace("%2F", "/");
        }
        return encoded;
    }
}
