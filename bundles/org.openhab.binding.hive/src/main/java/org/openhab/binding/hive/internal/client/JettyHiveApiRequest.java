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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.hive.internal.client.exception.HiveClientRequestException;

/**
 * An implementation of {@link HiveApiRequest} based around jetty's
 * {@link org.eclipse.jetty.client.HttpClient}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
final class JettyHiveApiRequest implements HiveApiRequest {
    private final JsonService jsonService;
    private final Request request;

    public JettyHiveApiRequest(
            final JsonService jsonService,
            final Request request
    ) {
        Objects.requireNonNull(jsonService);
        Objects.requireNonNull(request);

        this.jsonService = jsonService;
        this.request = request;
    }

    private void setContent(final Object content) {
        this.request.content(
                new StringContentProvider(this.jsonService.toJson(content)),
                HiveApiConstants.MEDIA_TYPE_JSON
        );
    }

    @Override
    public HiveApiRequest accept(final String mediaType) {
        this.request.accept(mediaType);

        return this;
    }

    @Override
    public HiveApiResponse get() throws HiveClientRequestException {
        this.request.method(HttpMethod.GET);

        return this.sendRequest();
    }

    @Override
    public HiveApiResponse post(final Object requestBody) throws HiveClientRequestException {
        Objects.requireNonNull(requestBody);

        this.request.method(HttpMethod.POST);
        this.setContent(requestBody);

        return this.sendRequest();
    }

    @Override
    public HiveApiResponse put(final Object requestBody) throws HiveClientRequestException {
        Objects.requireNonNull(requestBody);

        this.request.method(HttpMethod.PUT);
        this.setContent(requestBody);

        return this.sendRequest();
    }

    @Override
    public HiveApiResponse delete() throws HiveClientRequestException {
        this.request.method(HttpMethod.DELETE);

        return this.sendRequest();
    }

    private HiveApiResponse sendRequest() throws HiveClientRequestException {
        try {
            final ContentResponse response = this.request.send();

            return new JettyHiveApiResponse(this.jsonService, response);
        } catch (InterruptedException | TimeoutException | ExecutionException ex) {
            throw new HiveClientRequestException(ex);
        }
    }
}
