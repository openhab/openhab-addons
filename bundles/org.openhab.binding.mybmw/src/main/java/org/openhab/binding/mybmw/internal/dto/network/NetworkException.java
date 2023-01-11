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
package org.openhab.binding.mybmw.internal.dto.network;

/**
 * The {@link NetworkException} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - extend Exception 
 */
public class NetworkException extends Exception {

    private String url;
    private int status;
    private String reason;
    private String params;

    public NetworkException() {
    }

    public NetworkException(String url, int status, String reason, String params) {
        this.url = url;
        this.status = status;
        this.reason = reason;
        this.params = params;
    }

    public NetworkException(String url, int status, String reason, String params, Throwable cause) {
        super(cause);
        this.url = url;
        this.status = status;
        this.reason = reason;
        this.params = params;
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

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "NetworkException [url=" + url + ", status=" + status + ", reason=" + reason + ", params=" + params
                + "]";
    }
}
