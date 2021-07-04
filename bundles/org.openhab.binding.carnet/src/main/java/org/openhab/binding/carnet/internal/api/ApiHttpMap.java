/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpHeader;

/**
 * The {@link ApiHttpMap} helper classs to build header and data maps for http requests
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ApiHttpMap {
    private Map<String, String> headers = new LinkedHashMap<>();
    private Map<String, String> data = new LinkedHashMap<>();

    public ApiHttpMap header(String header, String value) {
        headers.put(header, value);
        return this;
    }

    public ApiHttpMap header(HttpHeader header, String value) {
        return header(header.toString(), value);
    }

    public void clearHeader() {
        headers.clear();
    }

    public ApiHttpMap data(String attribute, String value) {
        data.put(attribute, value);
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void clearData() {
        data.clear();
    }
}
