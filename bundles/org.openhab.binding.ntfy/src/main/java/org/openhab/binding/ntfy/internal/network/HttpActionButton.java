/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ntfy.internal.network;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Action button that represents an HTTP action attached to a notification.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class HttpActionButton extends ActionButtonBase {

    private URL url;
    private @Nullable String body;
    private @Nullable String headers;
    private @Nullable String method;

    /**
     * Creates an HTTP action button descriptor.
     *
     * @param label the label shown for the action
     * @param clearNotification whether executing the action should clear the notification
     * @param url the URL to invoke when the action is executed
     * @param method optional HTTP method (may be null)
     * @param headers optional headers to include (may be null)
     * @param body optional request body (may be null)
     * @throws MalformedURLException when the provided URL is not a valid URL
     */
    public HttpActionButton(String label, Boolean clearNotification, String url, @Nullable String method,
            @Nullable String headers, @Nullable String body) throws MalformedURLException {
        super(label, clearNotification);

        this.url = URI.create(url).toURL();
        this.method = method;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public String getHeader() {
        return "http, " + label + ", " + url.toString()
                + (method != null && !method.isBlank() ? ", method=" + method : "")
                + (headers != null && !headers.isBlank() ? ", headers=" + headers : "")
                + (body != null && !body.isBlank() ? ", body=" + body : "") + (clearNotification ? ", clear=true" : "");
    }
}
