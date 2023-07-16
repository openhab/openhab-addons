/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.connectedcar.internal.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpHeader;

/**
 * The {@link ApiHttpMap} helper classs to build header and data maps for HTTP requests
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class ApiHttpMap {
    protected Map<String, String> headers = new LinkedHashMap<>();
    protected Map<String, String> data = new LinkedHashMap<>();
    protected String body = "";

    public ApiHttpMap header(String header, String value) {
        headers.put(header, value);
        return this;
    }

    public ApiHttpMap header(HttpHeader header, String value) {
        return header(header.toString(), value);
    }

    public ApiHttpMap headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public ApiHttpMap headers(ApiHttpMap headers) {
        return headers(headers.getHeaders());
    }

    public ApiHttpMap data(String attribute, String value) {
        data.put(attribute, value);
        return this;
    }

    public ApiHttpMap datas(Map<String, String> data) {
        this.data.putAll(data);
        return this;
    }

    public ApiHttpMap body(String body) {
        this.body = body;
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getData() {
        return data;
    }

    public String getBody() {
        return body;
    }

    public String getRequestData(boolean json) {
        return body.isEmpty() ? ApiHttpClient.buildPostData(data, json) : body;
    }

    public ApiHttpMap clearHeader() {
        headers.clear();
        return this;
    }

    public ApiHttpMap clearData() {
        data.clear();
        return this;
    }
}
