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
package org.openhab.binding.chatgpt.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.chatgpt.internal.api.dto.ChatResponse;

/**
 * Unit tests for {@link ChatGPTApiClient}.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class ChatGPTApiClientTest {

    private static final String UNRECOGNIZED_REASONING_EFFORT_RESPONSE = """
            {
              "error" : {
                "message" : "Unrecognized request argument supplied: reasoning_effort",
                "type" : "invalid_request_error",
                "param" : null,
                "code" : null
              }
            }
            """;

    private static final String SUCCESS_RESPONSE = """
            {
              "id": "chatcmpl-123",
              "object": "chat.completion",
              "created": 1677652288,
              "model": "gpt-4o-mini",
              "choices": [{
                "index": 0,
                "message": {
                  "role": "assistant",
                  "content": "Hello! How can I help you today?"
                },
                "finish_reason": "stop"
              }],
              "usage": {
                "prompt_tokens": 9,
                "completion_tokens": 12,
                "total_tokens": 21
              }
            }
            """;

    private HttpClient httpClient = mock(HttpClient.class);
    private Request request = mock(Request.class);
    private ContentResponse response400 = mock(ContentResponse.class);
    private ContentResponse response200 = mock(ContentResponse.class);

    private @NonNullByDefault({}) ChatGPTApiClient client;

    @BeforeEach
    public void setUp() {
        httpClient = mock(HttpClient.class);
        request = mock(Request.class);
        response400 = mock(ContentResponse.class);
        response200 = mock(ContentResponse.class);

        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.method(any(HttpMethod.class))).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        when(request.header(any(HttpHeader.class), anyString())).thenReturn(request);
        when(request.header(anyString(), anyString())).thenReturn(request);
        when(request.content(any())).thenReturn(request);

        when(response400.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400);
        when(response400.getContentAsString()).thenReturn(UNRECOGNIZED_REASONING_EFFORT_RESPONSE);

        when(response200.getStatus()).thenReturn(HttpStatus.OK_200);
        when(response200.getContentAsString()).thenReturn(SUCCESS_RESPONSE);

        client = new ChatGPTApiClient(httpClient, "test-api-key", "https://api.openai.com/v1");
    }

    @Test
    public void testRetryWithoutReasoningEffortOn400BadRequest() throws Exception {
        when(request.send()).thenReturn(response400, response200);

        ChatResponse response = client.sendPrompt("gpt-4o-mini", "Hello", null, null, null, null, "medium", 10);

        assertNotNull(response);
        assertEquals(1, response.getChoices().size());
        assertEquals("Hello! How can I help you today?",
                response.getChoices().getFirst().getChatMessage().getContent());

        ArgumentCaptor<ContentProvider> contentCaptor = ArgumentCaptor.forClass(ContentProvider.class);
        verify(request, times(2)).content(contentCaptor.capture());

        List<ContentProvider> capturedProviders = contentCaptor.getAllValues();
        assertEquals(2, capturedProviders.size());

        String firstPayload = extractPayload(capturedProviders.get(0));
        String secondPayload = extractPayload(capturedProviders.get(1));

        assertTrue(firstPayload.contains("\"reasoning_effort\":\"medium\""));
        assertFalse(secondPayload.contains("reasoning_effort"));
    }

    @Test
    public void testCachedModelDoesNotIncludeReasoningEffortOnSubsequentRequest() throws Exception {
        when(request.send()).thenReturn(response400, response200);

        // First call triggers 400 error and retries, populating modelsNotSupportingReasoningEffort cache
        client.sendPrompt("gpt-4o-mini", "Hello", null, null, null, null, "medium", 10);

        // Second call with same model should not include reasoning_effort from the start
        Request secondRequest = mock(Request.class);
        when(httpClient.newRequest(anyString())).thenReturn(secondRequest);
        when(secondRequest.method(any(HttpMethod.class))).thenReturn(secondRequest);
        when(secondRequest.timeout(anyLong(), any(TimeUnit.class))).thenReturn(secondRequest);
        when(secondRequest.header(any(HttpHeader.class), anyString())).thenReturn(secondRequest);
        when(secondRequest.header(anyString(), anyString())).thenReturn(secondRequest);
        when(secondRequest.content(any())).thenReturn(secondRequest);
        when(secondRequest.send()).thenReturn(response200);

        ChatResponse response = client.sendPrompt("gpt-4o-mini", "Hello again", null, null, null, null, "medium", 10);

        assertNotNull(response);
        verify(secondRequest, times(1)).send();

        ArgumentCaptor<ContentProvider> contentCaptor = ArgumentCaptor.forClass(ContentProvider.class);
        verify(secondRequest, times(1)).content(contentCaptor.capture());

        String payload = extractPayload(contentCaptor.getValue());
        assertFalse(payload.contains("reasoning_effort"));
    }

    @Test
    public void testNoRetryForOther400BadRequest() throws Exception {
        ContentResponse other400Response = mock(ContentResponse.class);
        when(other400Response.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400);
        when(other400Response.getContentAsString()).thenReturn("""
                {
                  "error": {
                    "message": "Invalid model specified",
                    "type": "invalid_request_error"
                  }
                }
                """);

        when(request.send()).thenReturn(other400Response);

        assertThrows(ChatGPTApiException.class, () -> {
            client.sendPrompt("invalid-model", "Hello", null, null, null, null, "medium", 10);
        });

        verify(request, times(1)).send();
    }

    private String extractPayload(ContentProvider provider) {
        StringBuilder sb = new StringBuilder();
        for (ByteBuffer buffer : provider) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            sb.append(new String(bytes, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
