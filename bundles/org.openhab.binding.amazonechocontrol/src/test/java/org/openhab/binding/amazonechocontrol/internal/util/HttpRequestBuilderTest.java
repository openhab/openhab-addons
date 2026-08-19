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
package org.openhab.binding.amazonechocontrol.internal.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.amazonechocontrol.internal.util.HttpRequestBuilder.buildFailureReason;
import static org.openhab.binding.amazonechocontrol.internal.util.HttpRequestBuilder.isThrottled;

import java.net.CookieManager;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amazonechocontrol.internal.util.HttpRequestBuilder.FailMode;
import org.openhab.binding.amazonechocontrol.internal.util.HttpRequestBuilder.HttpResponse;
import org.openhab.binding.amazonechocontrol.internal.util.HttpRequestBuilder.RequestParams;

import com.google.gson.Gson;

/**
 * The {@link HttpRequestBuilderTest} contains tests for the failure handling of the {@link HttpRequestBuilder}
 * class
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class HttpRequestBuilderTest {
    private static final String THROTTLING_ERROR_TYPE = "ThrottlingException:"
            + "http://internal.amazon.com/coral/com.amazon.alexa.exceptions/";
    private static final URI REQUEST_URI = URI.create("https://alexa.amazon.de/api/notifications");

    @Test
    public void testMissingErrorTypeHeaderIsNotThrottled() {
        assertThat(isThrottled(400, null), is(false));
        assertThat(buildFailureReason("Bad Request", null), is("Bad Request"));
    }

    @Test
    public void testBlankErrorTypeHeaderIsNotThrottled() {
        assertThat(isThrottled(400, ""), is(false));
        assertThat(buildFailureReason("Bad Request", ""), is("Bad Request"));
    }

    @Test
    public void testThrottlingErrorTypeIsThrottledAndAppendedToTheReason() {
        assertThat(isThrottled(400, THROTTLING_ERROR_TYPE), is(true));
        assertThat(buildFailureReason("Bad Request", THROTTLING_ERROR_TYPE),
                is("Bad Request (x-amzn-ErrorType: " + THROTTLING_ERROR_TYPE + ")"));
    }

    @Test
    public void testTooManyRequestsIsThrottledWithoutTheHeader() {
        assertThat(isThrottled(429, null), is(true));
    }

    @Test
    public void testOtherErrorTypeIsNotThrottledButStillAppended() {
        assertThat(isThrottled(400, "ValidationException"), is(false));
        assertThat(buildFailureReason("Bad Request", "ValidationException"),
                is("Bad Request (x-amzn-ErrorType: ValidationException)"));
    }

    @Test
    public void testMissingReasonPhraseIsReplacedByAPlaceholder() {
        assertThat(buildFailureReason(null, null), is("no reason given"));
        assertThat(buildFailureReason("", null), is("no reason given"));
        assertThat(buildFailureReason(null, THROTTLING_ERROR_TYPE),
                is("no reason given (x-amzn-ErrorType: " + THROTTLING_ERROR_TYPE + ")"));
    }

    @Test
    public void testThrottledResponseFailsFastInsteadOfRetrying() {
        HttpClient httpClient = mock(HttpClient.class);
        // stubbed so that a regression retries instead of failing with a NullPointerException
        when(httpClient.newRequest(any(URI.class))).thenReturn(mock(Request.class, RETURNS_SELF));
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder(httpClient, new CookieManager(), new Gson());

        HttpFields headers = new HttpFields();
        headers.add("x-amzn-ErrorType", THROTTLING_ERROR_TYPE);

        CompletableFuture<HttpResponse> httpResponse = new CompletableFuture<>();
        RequestParams params = new RequestParams(HttpMethod.GET, null, false, Map.of());
        requestBuilder.new HttpResponseListener(httpResponse, params, false, FailMode.RETRY)
                .onComplete(throttledResult(headers));

        verify(httpClient, never()).newRequest(any(URI.class));
        assertThat(httpResponse.isCompletedExceptionally(), is(true));
        ExecutionException failure = assertThrows(ExecutionException.class, httpResponse::get);
        assertThat(failure.getCause().getMessage(), containsString("ThrottlingException"));
    }

    private Result throttledResult(HttpFields headers) {
        Request request = mock(Request.class);
        when(request.getURI()).thenReturn(REQUEST_URI);
        Response response = mock(Response.class);
        when(response.getRequest()).thenReturn(request);
        when(response.getStatus()).thenReturn(400);
        when(response.getHeaders()).thenReturn(headers);
        when(response.getReason()).thenReturn("Bad Request");
        Result result = mock(Result.class);
        when(result.getResponse()).thenReturn(response);
        return result;
    }
}
