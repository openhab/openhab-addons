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
package org.openhab.binding.carnet.internal;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.carnet.internal.api.CarNetApiResult;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link CarNetException} implements an extension to the standard Exception class. This allows to keep also the
 * result of the last API call (e.g. including the http status code in the message).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetException extends Exception {
    private static final long serialVersionUID = -5809459454769761821L;
    private static String NONE = "none";

    private CarNetApiResult apiResult = new CarNetApiResult();

    public CarNetException(String message) {
        super(message);
    }

    public CarNetException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public CarNetException(String message, CarNetApiResult result) {
        super(message);
        apiResult = result;
    }

    public CarNetException(String message, CarNetApiResult result, Throwable throwable) {
        super(message, throwable);
        apiResult = result;
    }

    @Override
    public @Nullable String getMessage() {
        return super.getMessage();
    }

    @Override
    public String toString() {
        String message = nonNullString(super.getMessage());
        Throwable cause = super.getCause();
        if (cause != null) {
            Class<?> clazz = cause.getClass();
            if (clazz == UnknownHostException.class) {
                String[] string = message.split(": "); // java.net.UnknownHostException: api.rach.io
                message = MessageFormat.format("Unable to connect to {0} (unknown host / internet connection down)",
                        string[1]);
            } else if (clazz == MalformedURLException.class) {
                message = MessageFormat.format("Invalid URL: '{0}'", message);
            } else {
                message = nonNullString(cause.toString()) + "(" + nonNullString(cause.getMessage()) + ")";
            }
        } else {
            if (super.getClass() != CarNetException.class) {
                message = MessageFormat.format("{0} {1}", super.getClass().toString(), super.getMessage());
            }
        }

        String url = !apiResult.url.isEmpty() ? MessageFormat.format("{0} {1} (HTTP {2} {3})", apiResult.method,
                apiResult.url, apiResult.httpCode, apiResult.httpReason) : "";
        String resultString = !apiResult.response.isEmpty() ? MessageFormat.format(", result = {0}", apiResult.response)
                : "";
        return MessageFormat.format("{0} {1}{2}", message, url, resultString);
    }

    public CarNetApiResult getApiResult() {
        return apiResult;
    }

    public boolean isSecurityException() {
        return (getCauseClass() == CarNetSecurityException.class) || (apiResult.httpCode == HttpStatus.FORBIDDEN_403);
    }

    public boolean isHttpAccessUnauthorized() {
        return apiResult.isHttpUnauthorized();
    }

    public boolean isHttpServerError() {
        return apiResult.isHttpServerError();
    }

    public boolean isHttpNotModified() {
        return apiResult.isHttpNotModified();
    }

    public boolean isTooManyRequests() {
        return apiResult.isHttpTooManyRequests() || apiResult.getApiError().isApiThrottle();
    }

    public boolean isUnknownHost() {
        return getCauseClass() == MalformedURLException.class;
    }

    public boolean isMalformedURL() {
        return getCauseClass() == UnknownHostException.class;
    }

    public boolean isJSONException() {
        return getCauseClass() == JsonSyntaxException.class;
    }

    public boolean isTimeout() {
        Class<?> extype = !isEmpty() ? getCauseClass() : null;
        return (extype != null) && ((extype == TimeoutException.class) || (extype == ExecutionException.class)
                || (extype == InterruptedException.class)
                || nonNullString(getMessage()).toLowerCase().contains("timeout"));
    }

    private Class<?> getCauseClass() {
        Throwable cause = getCause();
        if (cause != null) {
            return cause.getClass();
        }
        return CarNetException.class;
    }

    private boolean isEmpty() {
        return NONE.equals(nonNullString(super.getMessage()));
    }

    private static String nonNullString(@Nullable String s) {
        return s != null ? s : "";
    }
}
