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
package org.openhab.binding.shelly.internal.api;

import static org.openhab.binding.shelly.internal.util.ShellyUtils.getString;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link ShellyApiException} implements an extension to the standard Exception class. This allows to keep also the
 * result of the last API call (e.g. including the http status code in the message).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyApiException extends Exception {
    private static final long serialVersionUID = -5809459454769761821L;

    private ShellyApiResult apiResult = new ShellyApiResult();
    private static final String NONE = "none";

    public ShellyApiException(Exception exception) {
        super(exception);
    }

    public ShellyApiException(String message) {
        super(message);
    }

    public ShellyApiException(ShellyApiResult res) {
        super(NONE);
        apiResult = res;
    }

    public ShellyApiException(String message, Exception exception) {
        super(message, exception);
    }

    public ShellyApiException(ShellyApiResult result, Exception exception) {
        super(exception);
        apiResult = result;
    }

    @Override
    public @Nullable String getMessage() {
        return isEmpty() ? "" : nonNullString(super.getMessage());
    }

    @Override
    public String toString() {
        String message = nonNullString(super.getMessage()).replace("java.util.concurrent.ExecutionException: ", "")
                .replace("java.net.", "");
        String cause = getCauseClass().toString();
        String url = apiResult.getUrl();
        if (!isEmpty()) {
            if (isUnknownHost()) {
                String[] string = message.split(": "); // java.net.UnknownHostException: api.rach.io
                message = MessageFormat.format("Unable to connect to {0} (Unknown host / Network down / Low signal)",
                        string[1]);
            } else if (isMalformedURL()) {
                message = "Invalid URL: " + url;
            } else if (isJsonError()) {
                message = getString(getMessage());
            } else if (isTimeout()) {
                message = "API Timeout for " + url;
            } else if (!isConnectionError()) {
                message = message + "(" + cause + ")";
            }
        } else {
            message = apiResult.toString();
        }
        return message;
    }

    public boolean isJsonError() {
        return getString(getMessage()).startsWith("Unable to create object of type");
    }

    public boolean isApiException() {
        return getCauseClass() == ShellyApiException.class;
    }

    public boolean isTimeout() {
        Class<?> extype = !isEmpty() ? getCauseClass() : null;
        return (extype != null) && ((extype == TimeoutException.class) || extype == InterruptedException.class
                || extype == SocketTimeoutException.class
                || nonNullString(getMessage()).toLowerCase().contains("timeout"));
    }

    public boolean isConnectionError() {
        Class<?> exType = getCauseClass();
        return isUnknownHost() || isMalformedURL() || exType == ConnectException.class
                || exType == SocketException.class || exType == PortUnreachableException.class
                || exType == NoRouteToHostException.class;
    }

    public boolean isNoRouteToHost() {
        return getCauseClass() == NoRouteToHostException.class;
    }

    public boolean isUnknownHost() {
        return getCauseClass() == UnknownHostException.class;
    }

    public boolean isMalformedURL() {
        return getCauseClass() == MalformedURLException.class;
    }

    public boolean isHttpAccessUnauthorized() {
        return apiResult.isHttpAccessUnauthorized();
    }

    public boolean isJSONException() {
        return getCauseClass() == JsonSyntaxException.class;
    }

    public ShellyApiResult getApiResult() {
        return apiResult;
    }

    private boolean isEmpty() {
        return NONE.equals(nonNullString(super.getMessage()));
    }

    private static String nonNullString(@Nullable String s) {
        return s != null ? s : "";
    }

    private Class<?> getCauseClass() {
        Throwable cause = getCause();
        if (cause != null && cause.getClass() == ExecutionException.class) {
            cause = cause.getCause();
        }
        if (cause != null) {
            return cause.getClass();
        }
        return ShellyApiException.class;
    }
}
