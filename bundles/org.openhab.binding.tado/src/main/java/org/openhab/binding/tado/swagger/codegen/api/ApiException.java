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
package org.openhab.binding.tado.swagger.codegen.api;

import org.eclipse.jetty.client.api.ContentResponse;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class ApiException extends Exception {

    private int code = 0;

    private String responseBody = null;

    public ApiException() {
    }

    public ApiException(Throwable throwable) {
        super(throwable);
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ApiException(ContentResponse response, String message, Throwable throwable) {
        super(message, throwable);
        this.code = response.getStatus();
        this.responseBody = response.getContentAsString();
    }

    public ApiException(ContentResponse response, String message) {
        this(response, message, null);
    }

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
