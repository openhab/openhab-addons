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
package org.openhab.binding.freebox.internal.api;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link APIAction} class implements behavior to build and
 * send proper requests to the Freebox API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class APIAction {
    protected final String requestUrl;
    protected final @NonNullByDefault({}) RequestAnnotation annotation;
    protected final @Nullable Object payload;
    protected int maxRetry;

    public APIAction() {
        this(null, null);
    }

    public APIAction(String requestUrl) {
        this(requestUrl, null);
    }

    public APIAction(Object payload) {
        this(null, payload);
    }

    public APIAction(@Nullable String requestUrl, @Nullable Object payload) {
        Annotation[] annotations = getClass().getAnnotations();
        this.annotation = Arrays.stream(annotations).filter(a -> a instanceof RequestAnnotation)
                .map(a -> (RequestAnnotation) a).findFirst().get();
        this.payload = payload;
        this.maxRetry = annotation.maxRetries();

        StringBuilder url = new StringBuilder(annotation.relativeUrl());
        if (requestUrl != null) {
            try {
                url.append(URLEncoder.encode(requestUrl, StandardCharsets.UTF_8.name()));
                if (annotation.endsWithSlash()) {
                    url.append("/");
                }
            } catch (UnsupportedEncodingException ignore) {
            }
        }
        this.requestUrl = url.toString();
    }

    public String getUrl() {
        return requestUrl;
    }

    public String getMethod() {
        return annotation.method();
    }

    @SuppressWarnings("unchecked")
    public Class<? extends FreeboxResponse<?>> getResponseClass() {
        return (Class<? extends FreeboxResponse<?>>) annotation.responseClass();
    }

    public boolean retriesLeft() {
        return maxRetry-- > 0;
    }

    public @Nullable Object getPayload() {
        return payload;
    }
}
