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

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.io.Content;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

    /**
     * Collects the headers set on the request. Jetty 12 no longer takes the header name and value as arguments, but a
     * consumer modifying the header fields, so the captured consumers have to be applied to be able to assert on the
     * resulting headers.
     *
     * @param request the mocked request
     * @param expectedCount the number of expected calls setting headers
     * @return the header fields as they would be sent
     */
    @SuppressWarnings("unchecked")
    private HttpFields capturedHeaders(Request request, int expectedCount) {
        ArgumentCaptor<Consumer<HttpFields.Mutable>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(request, times(expectedCount)).headers(captor.capture());

        HttpFields.Mutable fields = HttpFields.build();
        captor.getAllValues().forEach(consumer -> consumer.accept(fields));
        return fields;
    }

    private void assertDefaultHeaders(HttpFields headers, String accept) {
        assertEquals("application/json", headers.get("Content-type"));
        assertEquals(accept, headers.get("Accept"));
        assertEquals("Bearer " + ACCESS_TOKEN, headers.get("Authorization"));
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
        assertDefaultHeaders(capturedHeaders(request, 3), "*/*");
        verify(request).timeout(REQUEST_TIMEOUT, REQUEST_TIMEOUT_UNIT);
        verify(request).method(HttpMethod.GET);
        verify(request).param("language", LANGUAGE);
        verifyNoMoreInteractions(request);
    }

    @Test
    public void testCreatePutRequestReturnsRequestWithExpectedHeadersAndContent() throws Exception {
        Request requestMock = getRequestMock();
        RequestFactoryImpl requestFactory = createRequestFactoryImpl(requestMock, defaultLanguageProvider);

        // when:
        Request request = requestFactory.createPutRequest(URL, ACCESS_TOKEN, JSON_CONTENT);

        // then:
        assertEquals(requestMock, request);
        assertDefaultHeaders(capturedHeaders(request, 3), "*/*");
        verify(request).timeout(EXTENDED_REQUEST_TIMEOUT, REQUEST_TIMEOUT_UNIT);
        verify(request).method(HttpMethod.PUT);
        ArgumentCaptor<Request.Content> bodyCaptor = ArgumentCaptor.forClass(Request.Content.class);
        verify(request).body(bodyCaptor.capture());
        assertEquals("application/json", bodyCaptor.getValue().getContentType());
        assertEquals(JSON_CONTENT, Content.Source.asString(bodyCaptor.getValue(), StandardCharsets.UTF_8));
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
        assertDefaultHeaders(capturedHeaders(request, 3), "*/*");
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
        assertDefaultHeaders(capturedHeaders(request, 3), "*/*");
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
        assertDefaultHeaders(capturedHeaders(request, 3), "*/*");
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
        assertDefaultHeaders(capturedHeaders(request, 3), "text/event-stream");
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
        HttpFields headers = capturedHeaders(request, 4);
        assertDefaultHeaders(headers, "text/event-stream");
        assertEquals(LANGUAGE, headers.get("Accept-Language"));
        verifyNoMoreInteractions(request);
    }
}
