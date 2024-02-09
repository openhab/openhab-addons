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
package org.openhab.binding.automower.internal.rest.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Base class for the Husqvarna apis
 *
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public abstract class HusqvarnaApi {
    private final HttpClient httpClient;
    protected final Gson gson;

    protected HusqvarnaApi(HttpClient httpClient) {
        this.httpClient = httpClient;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    protected abstract String getBaseUrl();

    protected HttpClient getHttpClient() {
        return httpClient;
    }
}
