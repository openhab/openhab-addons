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
package org.openhab.binding.thekeys.internal.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.HttpUtil;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * HTTP client to query the gateway
 *
 * @author Jordan Martin - Initial contribution
 */
@NonNullByDefault
public class TheKeysHttpClient {

    private final Gson gson;

    public TheKeysHttpClient() {
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    public <T> T get(String url, int timeoutMs, Class<T> responseType) throws IOException {
        String json = HttpUtil.executeUrl("GET", url, timeoutMs);
        return gson.fromJson(json, responseType);
    }

    public <T> T post(String url, String body, int timeout, Class<T> responseType) throws IOException {
        ByteArrayInputStream bodyInputStream = new ByteArrayInputStream(body.getBytes());
        String json = HttpUtil.executeUrl("POST", url, bodyInputStream, "application/x-www-form-urlencoded", timeout);
        return gson.fromJson(json, responseType);
    }
}
