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
package org.openhab.binding.hive.internal.client;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;

/**
 * An implementation of {@link HiveApiResponse} based around jetty's
 * {@link org.eclipse.jetty.client.HttpClient}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
final class JettyHiveApiResponse implements HiveApiResponse {
    private final JsonService jsonService;
    private final ContentResponse response;

    public JettyHiveApiResponse(
            final JsonService jsonService,
            final ContentResponse response
    ) {
        Objects.requireNonNull(jsonService);
        Objects.requireNonNull(response);

        this.jsonService = jsonService;
        this.response = response;
    }

    @Override
    public int getStatusCode() {
        return this.response.getStatus();
    }

    @Override
    public <T> T getContent(Class<T> contentType) {
        return this.jsonService.fromJson(this.response.getContentAsString(), contentType);
    }

    @Override
    public String getRawContent() {
        return this.response.getContentAsString();
    }
}
