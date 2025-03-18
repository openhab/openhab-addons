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
package org.openhab.binding.mybmw.internal.handler.backend;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link NetworkException} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - extend Exception
 */
@NonNullByDefault
public class NetworkException extends Exception {

    private static final long serialVersionUID = 123L;

    private String url = "";
    private int status = -1;
    private String reason = "";
    private String body = "";

    public NetworkException() {
    }

    public NetworkException(String url, int status, @Nullable String reason, @Nullable String body) {
        super(reason);
        this.url = url;
        this.status = status;
        this.reason = reason != null ? reason : "";
        this.body = body != null ? body : "";
    }

    public NetworkException(String url, int status, @Nullable String reason, @Nullable String body, Throwable cause) {
        super(cause);
        this.url = url;
        this.status = status;
        this.reason = reason != null ? reason : "";
        this.body = body != null ? body : "";
    }

    public NetworkException(String message) {
        super(message);
        this.reason = message;
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
        this.reason = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NetworkException [url=" + url + ", status=" + status + ", reason=" + reason + ", body=" + body + "]";
    }
}
