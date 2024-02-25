package org.openhab.binding.salus.internal.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.salus.internal.rest.RestClient.Content;
import org.openhab.binding.salus.internal.rest.RestClient.Header;
import org.openhab.binding.salus.internal.rest.RestClient.Response;

@ExtendWith(MockitoExtension.class)
class RetryHttpClientTest {
    @Mock
    RestClient restClient;
    String url = "https://example.com";
    Header header = new Header("Authorization", "Bearer token");
    Header[] headers = new Header[] { header };
    Response<String> response = new Response<>(200, "Success");
    Content content = new Content("Request body");

    @Test
    @DisplayName("get method retries calling restClient.get up to maxRetries times until it succeeds")
    void testGetMethodRetriesUntilSucceeds() throws Exception {
        // Given
        var maxRetries = 4;
        var retryHttpClient = new RetryHttpClient(restClient, maxRetries);

        given(restClient.get(url, headers))//
                .willThrow(new RuntimeException())//
                .willThrow(new ExecutionException(new RuntimeException()))//
                .willThrow(new InterruptedException())//
                .willReturn(response);

        // When
        var result = retryHttpClient.get(url, headers);

        // Then
        assertThat(result).isEqualTo(response);
        verify(restClient, times(4)).get(url, headers);
    }

    @Test
    @DisplayName("post method retries calling restClient.post up to maxRetries times until it succeeds")
    void testPostMethodRetriesUntilSucceeds() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        int maxRetries = 4;
        var retryHttpClient = new RetryHttpClient(restClient, maxRetries);

        given(restClient.post(url, content, headers))//
                .willThrow(new RuntimeException()) //
                .willThrow(new ExecutionException(new RuntimeException())) //
                .willThrow(new InterruptedException()) //
                .willReturn(response);

        // When
        var result = retryHttpClient.post(url, content, headers);

        // Then
        assertThat(result).isEqualTo(response);
        verify(restClient, times(4)).post(url, content, headers);
    }

    @Test
    @DisplayName("get method logs debug messages when it fails and retries")
    public void testGetMethodLogsDebugMessagesWhenFailsAndRetries()
            throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        var maxRetries = 3;
        var retryHttpClient = new RetryHttpClient(restClient, maxRetries);

        given(restClient.get(url, headers)).willThrow(new RuntimeException("Error"));

        // When
        assertThatThrownBy(() -> retryHttpClient.get(url, headers))//
                .isInstanceOf(RuntimeException.class)//
                .hasMessage("Error");
    }

    @Test
    @DisplayName("post method logs debug messages when it fails and retries")
    public void testPostMethodLogsDebugMessagesWhenFailsAndRetries()
            throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        int maxRetries = 3;
        var retryHttpClient = new RetryHttpClient(restClient, maxRetries);

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
