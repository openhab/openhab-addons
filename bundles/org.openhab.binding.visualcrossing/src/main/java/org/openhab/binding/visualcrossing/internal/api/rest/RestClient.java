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
package org.openhab.binding.visualcrossing.internal.api.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingApiException;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingAuthException;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingRateException;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public interface RestClient {
    /**
     * GET request to server
     * 
     * @param url to send request
     * @param headers to send
     * @return response from server
     */
    @Nullable
    String get(String url, @Nullable Header... headers)
            throws VisualCrossingApiException, VisualCrossingAuthException, VisualCrossingRateException;

    /**
     * POST request to server
     * 
     * @param url to send request
     * @param headers to send
     * @param content to send
     * @return response from server
     */
    @Nullable
    String post(String url, Content content, @Nullable Header... headers)
            throws VisualCrossingApiException, VisualCrossingAuthException, VisualCrossingRateException;

    /**
     * Represents content with a body and a type.
     */
    record Content(String body, String type) {
        /**
         * Creates a Content instance with the given body and default type ("application/json").
         *
         * @param body The content body.
         */
        public Content(String body) {
            this(body, APPLICATION_JSON);
        }
    }

    /**
     * Represents an HTTP header with a name and a list of values.
     */
    record Header(String name, List<String> values) {
        /**
         * Creates a Header instance with the given name and a single value.
         *
         * @param name The header name.
         * @param value The header value.
         */
        public Header(String name, String value) {
            this(name, List.of(value));
        }
    }
}
