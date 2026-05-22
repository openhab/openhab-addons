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
package org.openhab.binding.mielecloud.internal.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.util.MockUtil;
import org.openhab.binding.mielecloud.internal.webservice.language.LanguageProvider;
import org.openhab.binding.mielecloud.internal.webservice.request.RequestFactoryImpl;
import org.openhab.core.io.net.http.HttpClientFactory;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class RequestFactoryImplTest {
    private static final String URL = "https://www.openhab.org/";
    private static final String ACCESS_TOKEN = "DE_0123456789abcdef0123456789abcdef";
    private static final String JSON_CONTENT = "{ \"update\": 1 }";

    private static final String LANGUAGE = "de";

    private static final long REQUEST_TIMEOUT = 5;
    private static final long EXTENDED_REQUEST_TIMEOUT = 10;
    private static final TimeUnit REQUEST_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final LanguageProvider defaultLanguageProvider = new LanguageProvider() {
        @Override
        public Optional<String> getLanguage() {
            return Optional.of(LANGUAGE);
        }
    };
    private final LanguageProvider emptyStringLanguageProvider = new LanguageProvider() {
        @Override
        public Optional<String> getLanguage() {
            return Optional.of("");
        }
    };

    private Request getRequestMock() {
        Request requestMock = mock(Request.class);
        when(requestMock.headers(any())).thenReturn(requestMock);
        when(requestMock.timeout(anyLong(), any())).thenReturn(requestMock);
        when(requestMock.method(any(HttpMethod.class))).thenReturn(requestMock);
        when(requestMock.param(anyString(), anyString())).thenReturn(requestMock);
        when(requestMock.body(any())).thenReturn(requestMock);
        return requestMock;
    }

    private RequestFactoryImpl createRequestFactoryImpl(Request requestMock, LanguageProvider languageProvider) {
        HttpClient httpClient = MockUtil.mockHttpClient(URL, requestMock);

        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        when(httpClientFactory.createHttpClient(anyString())).thenReturn(httpClient);

        return new RequestFactoryImpl(httpClientFactory, languageProvider);
    }

    @Test
    public void testCreateGetRequestReturnsRequestWithExpectedHeaders() {
        // given:
        Request requestMock = getRequestMock();
        RequestFactoryImpl requestFactory = createRequestFactoryImpl(requestMock, defaultLanguageProvider);

        // when:
        Request request = requestFactory.createGetRequest(URL, ACCESS_TOKEN);

        // then:
        assertEquals(requestMock, request);
        verify(request, times(3)).headers(any());
        verify(request).timeout(REQUEST_TIMEOUT, REQUEST_TIMEOUT_UNIT);
        verify(request).method(HttpMethod.GET);
        verify(request).param("language", LANGUAGE);
        verifyNoMoreInteractions(request);
    }

    @Test
    public void testCreatePutRequestReturnsRequestWithExpectedHeadersAndContent() {
        Request requestMock = getRequestMock();
        RequestFactoryImpl requestFactory = createRequestFactoryImpl(requestMock, defaultLanguageProvider);

        // when:
        Request request = requestFactory.createPutRequest(URL, ACCESS_TOKEN, JSON_CONTENT);

        // then:
        assertEquals(requestMock, request);
        verify(request, times(3)).headers(any());
        verify(request).timeout(EXTENDED_REQUEST_TIMEOUT, REQUEST_TIMEOUT_UNIT);
        verify(request).method(HttpMethod.PUT);
        verify(request).body(any());
        verify(request).param("language", LANGUAGE);
        verifyNoMoreInteractions(request);
    }

    @Test
    public void testCreatePostRequestReturnsRequestWithExpectedHeaders() {
        Request requestMock = getRequestMock();
        RequestFactoryImpl requestFactory = createRequestFactoryImpl(requestMock, defaultLanguageProvider);

        // when:
        Request request = requestFactory.createPostRequest(URL, ACCESS_TOKEN);

        // then:
        assertEquals(requestMock, request);
        verify(request, times(3)).headers(any());
        verify(request).timeout(REQUEST_TIMEOUT, REQUEST_TIMEOUT_UNIT);
        verify(request).method(HttpMethod.POST);
        verify(request).param("language", LANGUAGE);
        verifyNoMoreInteractions(request);
    }

    @Test
    public void testCreateRequestWithoutSuppliedLangugeCreatesNoLanguageParameter() {
        // given:
        Request requestMock = getRequestMock();
        RequestFactoryImpl requestFactory = createRequestFactoryImpl(requestMock, new LanguageProvider() {
            @Override
            public Optional<String> getLanguage() {
                return Optional.empty();
            }
        });

        // when:
        Request request = requestFactory.createGetRequest(URL, ACCESS_TOKEN);

        // then:
        assertEquals(requestMock, request);
        verify(request, times(3)).headers(any());
        verify(request).timeout(REQUEST_TIMEOUT, REQUEST_TIMEOUT_UNIT);
        verify(request).method(HttpMethod.GET);
        verifyNoMoreInteractions(request);
    }

    @Test
    public void testCreateRequestWithEmptyLanguageCreatesNoLanguageParameter() {
        // given:
        Request requestMock = getRequestMock();
        RequestFactoryImpl requestFactory = createRequestFactoryImpl(requestMock, emptyStringLanguageProvider);

        // when:
        Request request = requestFactory.createGetRequest(URL, ACCESS_TOKEN);

        // then:
        assertEquals(requestMock, request);
        verify(request, times(3)).headers(any());
        verify(request).timeout(REQUEST_TIMEOUT, REQUEST_TIMEOUT_UNIT);
        verify(request).method(HttpMethod.GET);
        verifyNoMoreInteractions(request);
    }

    @Test
    public void whenAnSseRequestIsCreatedWithoutLanguageThenTheRequiredParametersAreSet() {
        Request requestMock = getRequestMock();
        RequestFactoryImpl requestFactory = createRequestFactoryImpl(requestMock, emptyStringLanguageProvider);

        // when:
        Request request = requestFactory.createSseRequest(URL, ACCESS_TOKEN);

        // then:
        assertEquals(requestMock, request);
        verify(request, times(3)).headers(any());
        verifyNoMoreInteractions(request);
    }

    @Test
    public void whenAnSseRequestIsCreatedWithLanguageThenTheAcceptLanguageHeaderIsSet() {
        Request requestMock = getRequestMock();
        RequestFactoryImpl requestFactory = createRequestFactoryImpl(requestMock, defaultLanguageProvider);

        // when:
        Request request = requestFactory.createSseRequest(URL, ACCESS_TOKEN);

        // then:
        assertEquals(requestMock, request);
        verify(request, times(4)).headers(any());
        verifyNoMoreInteractions(request);
    }
}
