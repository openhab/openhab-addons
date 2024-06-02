/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.salus.internal.rest;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.salus.internal.rest.RestClient.Content;
import org.openhab.binding.salus.internal.rest.RestClient.Header;
import org.openhab.binding.salus.internal.rest.exceptions.HttpSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@SuppressWarnings("DataFlowIssue")
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class RetryHttpClientTest {
    @Mock
    @Nullable
    RestClient restClient;
    String url = "https://example.com";
    Header header = new Header("Authorization", "Bearer token");
    Header[] headers = new Header[] { header };
    String response = "Success";
    Content content = new Content("Request body");

    @Test
    @DisplayName("get method retries calling restClient.get up to maxRetries times until it succeeds")
    void testGetMethodRetriesUntilSucceeds() throws Exception {
        // Given
        var maxRetries = 4;
        var retryHttpClient = new RetryHttpClient(requireNonNull(restClient), maxRetries);

        given(restClient.get(url, headers))//
                .willThrow(new SalusApiException("1")) //
                .willThrow(new HttpSalusApiException(404, "2")) //
                .willThrow(new SalusApiException("3")) //
                .willReturn(response);

        // When
        var result = retryHttpClient.get(url, headers);

        // Then
        assertThat(result).isEqualTo(response);
        verify(restClient, times(4)).get(url, headers);
    }

    @Test
    @DisplayName("post method retries calling restClient.post up to maxRetries times until it succeeds")
    void testPostMethodRetriesUntilSucceeds() throws SalusApiException {
        // Given
        int maxRetries = 4;
        var retryHttpClient = new RetryHttpClient(requireNonNull(restClient), maxRetries);

        given(restClient.post(url, content, headers))//
                .willThrow(new SalusApiException("1")) //
                .willThrow(new HttpSalusApiException(404, "2")) //
                .willThrow(new SalusApiException("3")) //
                .willReturn(response);

        // When
        var result = retryHttpClient.post(url, content, headers);

        // Then
        assertThat(result).isEqualTo(response);
        verify(restClient, times(4)).post(url, content, headers);
    }

    @Test
    @DisplayName("get method logs debug messages when it fails and retries")
    public void testGetMethodLogsDebugMessagesWhenFailsAndRetries() throws SalusApiException {
        // Given
        var maxRetries = 3;
        var retryHttpClient = new RetryHttpClient(requireNonNull(restClient), maxRetries);

        given(restClient.get(url, headers)).willThrow(new RuntimeException("Error"));

        // When
        assertThatThrownBy(() -> retryHttpClient.get(url, headers))//
                .isInstanceOf(RuntimeException.class)//
                .hasMessage("Error");
    }

    @Test
    @DisplayName("post method logs debug messages when it fails and retries")
    public void testPostMethodLogsDebugMessagesWhenFailsAndRetries() throws SalusApiException {
        // Given
        int maxRetries = 3;
        var retryHttpClient = new RetryHttpClient(requireNonNull(restClient), maxRetries);

        given(restClient.post(url, content, headers)).willThrow(new RuntimeException("Error"));

        // When
        assertThatThrownBy(() -> retryHttpClient.post(url, content, headers))//
                .isInstanceOf(RuntimeException.class)//
                .hasMessage("Error");
    }

    @Test
    @DisplayName("RetryHttpClient throws an IllegalArgumentException if maxRetries is less than or equal to 0")
    public void testThrowsIllegalArgumentExceptionIfMaxRetriesLessThanOrEqualTo0() {
        // Given
        RestClient restClient = mock(RestClient.class);
        int maxRetries = 0;

        // When/Then
        assertThatThrownBy(() -> new RetryHttpClient(restClient, maxRetries))//
                .isInstanceOf(IllegalArgumentException.class)//
                .hasMessage("maxRetries cannot be lower or equal to 0, but was " + maxRetries);
    }
}
