/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api.model;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.RequestAnnotation;

@NonNullByDefault
public class APIAction {
    protected String requestUrl;
    protected @NonNullByDefault({}) RequestAnnotation annotation;

    public APIAction() {
        this(null);
    }

    public APIAction(@Nullable String requestUrl) {
        Annotation[] annotations = getClass().getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof RequestAnnotation) {
                this.annotation = (RequestAnnotation) annotation;
            }
        }
        try {
            this.requestUrl = this.annotation.relativeUrl() + (requestUrl != null ? encodeUrl(requestUrl) + "/" : "");
        } catch (FreeboxException e) {
            // Error encoding URL
            this.requestUrl = this.annotation.relativeUrl();
        }
    }

    public String getUrl() throws FreeboxException {
        return requestUrl;
    }

    private String encodeUrl(String url) throws FreeboxException {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new FreeboxException("Encoding the URL \"" + url + "\" in UTF-8 failed", e);
        }
    }

    public String getMethod() {
        return annotation.method();
    }

    @SuppressWarnings("unchecked")
    public Class<? extends FreeboxResponse<?>> getResponseClass() {
        return (Class<? extends FreeboxResponse<?>>) annotation.responseClass();
    }

    public boolean getRetryAuth() {
        return annotation.retryAuth();
    }
}
