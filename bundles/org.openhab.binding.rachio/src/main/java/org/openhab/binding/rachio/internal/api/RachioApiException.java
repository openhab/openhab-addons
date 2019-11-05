/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal.api;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApi.RachioApiResult;

/**
 * The {@link RachioApiException} implements an extension to the standard Exception class. This allows to keep also the
 * result of the last API call (e.g. including the http status code in the message).
 *
 * @author Markus Michels - Initial contribution
 */

@NonNullByDefault
public class RachioApiException extends Exception {
    private static final long serialVersionUID = -2579498702258574787L;
    private RachioApiResult   apiResult        = new RachioApiResult();
    @Nullable
    private Throwable         e                = null;

    public RachioApiException(String message) {
        super(message);
    }

    public RachioApiException(String message, Throwable throwable) {
        super(message, throwable);
        e = throwable;
    }

    public RachioApiException(String message, RachioApiResult result) {
        super(message);
        apiResult = result;
    }

    public RachioApiException(String message, RachioApiResult result, Throwable throwable) {
        super(message);
        apiResult = result;
        e = throwable;
    }

    public RachioApiResult getApiResult() {
        return apiResult;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @SuppressWarnings("null")
    @Override
    public String toString() {
        String message = super.getMessage();
        Validate.notNull(e);
        if (e != null) {
            if (e.getClass() == UnknownHostException.class) {
                String[] string = message.split(": "); // java.net.UnknownHostException: api.rach.io
                message = MessageFormat.format("Unable to connect to {0} (unknown host / internet connection down)",
                        string[1]);
            } else if (e.getClass() == MalformedURLException.class) {
                message = MessageFormat.format("Invalid URL: '{0}'", message);
            } else {
                message = MessageFormat.format("'{0}' ({1}", e.toString(), e.getMessage());
            }
        } else {
            message = MessageFormat.format("'{0}' ({1})", super.getClass().toString(), super.getMessage());
        }

        String url = !apiResult.url.isEmpty() ? MessageFormat.format(", {0} {1}, http code={2}",
                apiResult.requestMethod, apiResult.url, apiResult.responseCode) : "";
        String rateLimit = apiResult.rateLimit > 0
                ? MessageFormat.format(", apiCalls={0}, rateLimit={1} of {2}", apiResult.apiCalls,
                        apiResult.rateRemaining, apiResult.rateLimit)
                : "";
        String resultString = !apiResult.resultString.isEmpty()
                ? MessageFormat.format(", result = '{0}'", apiResult.resultString)
                : "";
        return MessageFormat.format("Exception: {0}{1}{2}{3}", message, url, rateLimit, resultString);
    }
}
