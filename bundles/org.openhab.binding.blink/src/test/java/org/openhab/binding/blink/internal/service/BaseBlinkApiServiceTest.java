package org.openhab.binding.blink.internal.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.google.gson.Gson;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@NonNullByDefault
class BaseBlinkApiServiceTest {

    @Mock @NonNullByDefault({}) HttpClient httpClient;
    @Spy Gson gson = new Gson();
    @Mock @NonNullByDefault({}) Request request;
    @Mock @NonNullByDefault({}) ContentResponse response;

    @InjectMocks @NonNullByDefault({}) BaseBlinkApiService apiService;

    @BeforeEach
    void setup() {
        doReturn(request).when(httpClient).newRequest(anyString());
        doReturn(request).when(request).method(anyString());
        doReturn(request).when(request).header(ArgumentMatchers.any(HttpHeader.class), anyString());
    }

    @Test
    void testUrlConstructionAndReturn() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        when(response.getStatus()).thenReturn(200);
        String expected = "resultString";
        when(response.getContentAsString()).thenReturn(expected);
        doReturn(response).when(request).send();
        String result = apiService.request("abc", "/api/v1/hurz", HttpMethod.GET, null, null);
        verify(httpClient).newRequest("https://rest-abc.immedia-semi.com/api/v1/hurz");
        assertThat(result, is(expected));
    }

    @Test
    void testRawReturn() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        when(response.getStatus()).thenReturn(200);
        byte[] expected = "resultString".getBytes(StandardCharsets.UTF_8);
        when(response.getContent()).thenReturn(expected);
        doReturn(response).when(request).send();
        byte[] result = apiService.rawRequest("abc", "/api/v1/hurz", HttpMethod.GET, null, null);
        assertThat(result, is(expected));
    }

    @Test
    void testHeaderAndParamsSet() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        Map<String, String> params = Map.of("p1", "v1", "p2", "v2", "p3", "v3");
        doReturn(request).when(request).header(anyString(), anyString());
        doReturn(request).when(request).param(anyString(), anyString());
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("");
        doReturn(response).when(request).send();
        apiService.request("abc", "/api/v1/hurz", HttpMethod.GET, "mytoken", params);
        verify(request).header(HttpHeader.ACCEPT, "application/json; charset=UTF-8");
        verify(request).header("token-auth", "mytoken");
        verify(request, times(params.size())).param(anyString(), anyString());
        IntStream.range(1, 4).forEach(i -> verify(request).param("p" + i, "v" + i));
    }

    @Test
    @SuppressWarnings("null")
    void testExceptionOnStatusNonEqual200() throws ExecutionException, InterruptedException, TimeoutException {
        when(response.getStatus()).thenReturn(500);
        doReturn(response).when(request).send();
        IOException exception = assertThrows(IOException.class,
                () -> apiService.request("abc", "/api/v1/hurz", HttpMethod.GET, null, null));
        assertThat(exception.getMessage(), is("Blink API Call unsuccessful <Status 500>"));
    }

    @Test
    @SuppressWarnings("null")
    void testExceptionOnErrorInSend() throws ExecutionException, InterruptedException, TimeoutException {
        IOException exception;
        doThrow(InterruptedException.class).when(request).send();
        exception = assertThrows(IOException.class,
                () -> apiService.request("abc", "/api/v1/hurz", HttpMethod.GET, null, null));
        assertThat(exception.getCause(), is(notNullValue()));
        // assertThat(exception.getCause().getClass(), is(InterruptedException.class));
        // would like to check if cause is a InterruptedException.class, but I'm in @NotNull @Nullable hell because of assertThrows
        doThrow(TimeoutException.class).when(request).send();
        exception = assertThrows(IOException.class,
                () -> apiService.request("abc", "/api/v1/hurz", HttpMethod.GET, null, null));
        assertThat(exception.getCause(), is(notNullValue()));
        doThrow(ExecutionException.class).when(request).send();
        exception = assertThrows(IOException.class,
                () -> apiService.request("abc", "/api/v1/hurz", HttpMethod.GET, null, null));
        assertThat(exception.getCause(), is(notNullValue()));
    }

    static class SimpleClass {
        @Nullable public String iam;
        public int age;
    }

    @Test
    void testJsonDeserialization() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        when(response.getStatus()).thenReturn(200);
        String jsonString = "{ 'iam' : 'old', 'age' : 90 }";
        when(response.getContentAsString()).thenReturn(jsonString);
        doReturn(response).when(request).send();
        SimpleClass result = apiService.apiRequest("abc", "api/v1/hurz", HttpMethod.GET, null, null, SimpleClass.class);
        verify(gson).fromJson(jsonString, SimpleClass.class);
        assertThat(result.iam, is("old"));
        assertThat(result.age, is(90));
    }

}