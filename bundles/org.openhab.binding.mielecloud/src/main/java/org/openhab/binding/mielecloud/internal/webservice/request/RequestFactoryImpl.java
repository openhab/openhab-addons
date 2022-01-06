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
package org.openhab.binding.mielecloud.internal.webservice.request;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceInitializationException;
import org.openhab.binding.mielecloud.internal.webservice.language.LanguageProvider;
import org.openhab.core.io.net.http.HttpClientFactory;

/**
 * Default implementation of {@link RequestFactory}.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class RequestFactoryImpl implements RequestFactory {
    private static final long REQUEST_TIMEOUT = 5;
    private static final long EXTENDED_REQUEST_TIMEOUT = 10;
    private static final TimeUnit REQUEST_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final HttpClient httpClient;
    private final LanguageProvider languageProvider;

    /**
     * Creates a new {@link RequestFactoryImpl}.
     *
     * @param httpClientFactory Factory for obtaining a {@link HttpClient}.
     * @param languageProvider Provider for the language to use for new requests.
     * @throws MieleWebserviceInitializationException if creating and starting a new {@link HttpClient} fails.
     */
    public RequestFactoryImpl(HttpClientFactory httpClientFactory, LanguageProvider languageProvider) {
        this.httpClient = httpClientFactory.createHttpClient("mielecloud");
        try {
            this.httpClient.start();
        } catch (Exception e) {
            throw new MieleWebserviceInitializationException("Failed to start HttpClient", e);
        }
        this.languageProvider = languageProvider;
    }

    private Request createRequestWithDefaultHeaders(String url, String accessToken) {
        return httpClient.newRequest(url).header("Content-type", "application/json").header("Authorization",
                "Bearer " + accessToken);
    }

    private Request decorateWithLanguageParameter(Request request) {
        Optional<String> language = languageProvider.getLanguage();
        if (language.isPresent() && !language.get().isEmpty()) {
            return request.param("language", language.get());
        } else {
            return request;
        }
    }

    private Request decorateWithAcceptLanguageHeader(Request request) {
        Optional<String> language = languageProvider.getLanguage();
        if (language.isPresent() && !language.get().isEmpty()) {
            return request.header("Accept-Language", language.get());
        } else {
            return request;
        }
    }

    private Request createDefaultHttpRequest(String url, String accessToken, long timeout) {
        return decorateWithLanguageParameter(createRequestWithDefaultHeaders(url, accessToken)).header("Accept", "*/*")
                .timeout(timeout, REQUEST_TIMEOUT_UNIT);
    }

    @Override
    public Request createGetRequest(String url, String accessToken) {
        return createDefaultHttpRequest(url, accessToken, REQUEST_TIMEOUT).method(HttpMethod.GET);
    }

    @Override
    public Request createPutRequest(String url, String accessToken, String jsonContent) {
        return createDefaultHttpRequest(url, accessToken, EXTENDED_REQUEST_TIMEOUT).method(HttpMethod.PUT)
                .content(new StringContentProvider("application/json", jsonContent, StandardCharsets.UTF_8));
    }

    @Override
    public Request createPostRequest(String url, String accessToken) {
        return createDefaultHttpRequest(url, accessToken, REQUEST_TIMEOUT).method(HttpMethod.POST);
    }

    @Override
    public Request createSseRequest(String url, String accessToken) {
        return decorateWithAcceptLanguageHeader(createRequestWithDefaultHeaders(url, accessToken)).header("Accept",
                "text/event-stream");
    }

    @Override
    public void close() throws Exception {
        httpClient.stop();
    }
}
