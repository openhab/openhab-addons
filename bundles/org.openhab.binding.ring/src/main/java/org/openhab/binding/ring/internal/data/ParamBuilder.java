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
package org.openhab.binding.ring.internal.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Builder for http request or post parameters.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public class ParamBuilder {

    /**
     * When true, URL encode parameter names and values properly.
     */
    private boolean urlEncode;
    private static final String URL_ENCODING = "UTF-8";
    /**
     * The map used to store the parameters.
     */
    private final Map<String, String> parameters;

    /**
     * Create a new ParamBuilder. Specify if it should URL encode it.
     *
     * @param urlEncoded
     */
    public ParamBuilder(boolean urlEncoded) {
        this.urlEncode = urlEncoded;
        this.parameters = new HashMap<>();
    }

    /**
     * Add a name/value pair.
     *
     * @param name
     * @param value
     */
    public void add(String name, String value) {
        parameters.put(name, value);
    }

    /**
     * Helper method to handle encoding.
     *
     * @param input the input String.
     * @return the (possibly encode) result.
     */
    @Nullable
    private String encode(String input) {
        try {
            return urlEncode ? URLEncoder.encode(input, URL_ENCODING) : input;
        } catch (UnsupportedEncodingException e) {
            // Should not happen
            return null;
        }
    }

    /**
     * Get the result string in the format param1=value1&param2=value2, etc.
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (b.length() != 0) {
                b.append("&");
            }
            b.append(encode(entry.getKey())).append("=").append(encode(entry.getValue()));
        }
        return b.toString();
    }
}
