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
package org.openhab.binding.gree.internal;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonSyntaxException;

/**
 * {@link GreeException} implements a binding specific exception class. This allows to unity exception handling on the
 * higher levels, but still carrying the exception, which caused the problem.
 *
 * @author Markus Michels - Initial Contribution
 */
@NonNullByDefault
public class GreeException extends Exception {
    private static final long serialVersionUID = -2337258558995287405L;
    private static final String EX_NONE = "none";

    public GreeException(@Nullable Exception exception) {
        super(exception);
    }

    public GreeException(@Nullable String message) {
        super(message);
    }

    public GreeException(@Nullable String message, @Nullable Exception exception) {
        super(message, exception);
    }

    public String getMessageString() {
        return isEmpty() ? "" : nonNullString(super.getMessage());
    }

    @Override
    public String toString() {
        String message = nonNullString(super.getMessage());
        String cause = getCauseClass().toString();
        if (!isEmpty()) {
            if (isUnknownHost()) {
                String[] string = message.split(": "); // java.net.UnknownHostException: api.rach.io
                message = MessageFormat.format("Unable to connect to {0} (Unknown host / Network down / Low signal)",
                        string[1]);
            } else if (isMalformedURL()) {
                message = "Invalid URL";
            } else if (isTimeout()) {
                message = "Device unreachable or API Timeout";
            } else {
                message = MessageFormat.format("{0} ({1})", message, cause);
            }
        } else {
            message = getMessageString();
        }
        return message;
    }

    public boolean isApiException() {
        return getCauseClass() == GreeException.class;
    }

    public boolean isTimeout() {
        Class<?> extype = !isEmpty() ? getCauseClass() : null;
        return (extype != null) && ((extype == SocketTimeoutException.class) || (extype == TimeoutException.class)
                || (extype == ExecutionException.class) || (extype == InterruptedException.class)
                || getMessageString().toLowerCase().contains("timeout"));
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

    private boolean isEmpty() {
        return nonNullString(super.getMessage()).equals(EX_NONE);
    }

    private static String nonNullString(@Nullable String s) {
        return s != null ? s : "";
    }

    private Class<?> getCauseClass() {
        Throwable cause = getCause();
        if (cause != null) {
            return cause.getClass();
        }
        return GreeException.class;
    }
}
