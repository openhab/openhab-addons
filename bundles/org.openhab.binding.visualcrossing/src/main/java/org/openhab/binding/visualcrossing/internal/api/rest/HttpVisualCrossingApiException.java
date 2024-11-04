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

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpResponseException;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingApiException;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class HttpVisualCrossingApiException extends VisualCrossingApiException {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int code;
    private final String msg;

    public HttpVisualCrossingApiException(int code, String msg, HttpResponseException e) {
        super("HTTP Error %s: %s".formatted(code, msg), e);
        this.code = code;
        this.msg = msg;
    }

    public HttpVisualCrossingApiException(int code, String msg) {
        super("HTTP Error %s: %s".formatted(code, msg));
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
