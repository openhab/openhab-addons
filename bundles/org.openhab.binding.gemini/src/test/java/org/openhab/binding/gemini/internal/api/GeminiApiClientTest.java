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
package org.openhab.binding.gemini.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.openhab.core.voice.text.conversation.Conversation;
import org.openhab.core.voice.text.conversation.ConversationRole;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for the request reconstruction of {@link GeminiApiClient}, i.e. how a stored conversation
 * history is turned back into the {@code contents} of a Gemini {@code generateContent} request.
 *
 * The parallel function call case is the behaviour that must not regress: all calls of one batch have
 * to be replayed as the parts of a single model turn, followed by a single user turn holding all
 * function responses. Splitting them into separate turns makes Gemini 3.x reject the request with
 * {@code 400 INVALID_ARGUMENT} because only the first call carries a thought signature.
 *
 * @author Christian Heldt - Initial contribution
 */
@NonNullByDefault
public class GeminiApiClientTest {

    private static final String MODEL = "gemini-3.5-flash-lite";
    private static final String PROMPT = "Which lamps in the living room are on?";
    private static final String RESPONSE_JSON = """
            {"candidates":[{"content":{"role":"model","parts":[{"text":"Lamp1 is on."}]}}]}""";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private @NonNullByDefault({}) HttpClient httpClient;
    private @NonNullByDefault({}) Request request;
    private @NonNullByDefault({}) ContentResponse response;
    private @NonNullByDefault({}) GeminiApiClient apiClient;

    private static <T> T typedMock(Class<T> clazz) {
        return Objects.requireNonNull(mock(clazz));
    }

    @BeforeEach
    public void setUp() throws Exception {
        httpClient = typedMock(HttpClient.class);
        request = typedMock(Request.class);
        response = typedMock(ContentResponse.class);

        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.method(any(HttpMethod.class))).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        when(request.header(any(HttpHeader.class), anyString())).thenReturn(request);
        when(request.header(anyString(), anyString())).thenReturn(request);
        when(request.content(any(ContentProvider.class))).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(response.getContentAsString()).thenReturn(RESPONSE_JSON);

        apiClient = new GeminiApiClient(httpClient, "test-api-key");
    }

    @Test
    public void parallelToolCallsAreSerializedAsOneModelTurnAndOneUserTurn() throws Exception {
        // Gemini sends parallel calls as one model turn and puts the thought signature on the first
        // part only. The HLI stores them as interleaved TOOL_CALL/TOOL_RETURN pairs, marking calls
        // 2..N with parallel=true so the original turn structure can be reconstructed here.
        GeminiLLMToolCall call1 = new GeminiLLMToolCall("item-get-state", Map.of("item", "Lamp1"), "call-1",
                "signature-abc");
        GeminiLLMToolCall call2 = new GeminiLLMToolCall("item-get-state", Map.of("item", "Lamp2"), "call-2", null,
                Boolean.TRUE);
        List<Conversation.Message> history = List.of(new Conversation.Message(1, ConversationRole.USER, PROMPT),
                new Conversation.Message(2, ConversationRole.TOOL_CALL, call1.toJson()),
                new Conversation.Message(3, ConversationRole.TOOL_RETURN, "Lamp1 is ON"),
                new Conversation.Message(4, ConversationRole.TOOL_CALL, call2.toJson()),
                new Conversation.Message(5, ConversationRole.TOOL_RETURN, "Lamp2 is OFF"));

        apiClient.sendPrompt(MODEL, history, List.of(), null, null, null, null, null);

        JsonNode contents = captureRequestBody().get("contents");
        // one user turn, one model turn with both calls, one user turn with both responses - a split
        // into per-call turns would yield five contents here
        assertEquals(3, contents.size());

        JsonNode userTurn = contents.get(0);
        assertEquals("user", userTurn.get("role").asText());
        assertEquals(PROMPT, userTurn.get("parts").get(0).get("text").asText());

        JsonNode callTurn = contents.get(1);
        assertEquals("model", callTurn.get("role").asText());
        JsonNode callParts = callTurn.get("parts");
        assertEquals(2, callParts.size());

        JsonNode firstCallPart = callParts.get(0);
        assertFalse(firstCallPart.has("text"));
        assertEquals("signature-abc", firstCallPart.get("thoughtSignature").asText());
        JsonNode firstCall = firstCallPart.get("functionCall");
        assertEquals("item-get-state", firstCall.get("name").asText());
        assertEquals("Lamp1", firstCall.get("args").get("item").asText());
        assertEquals("call-1", firstCall.get("id").asText());

        JsonNode secondCallPart = callParts.get(1);
        assertFalse(secondCallPart.has("text"));
        // Gemini only signs the first part of a batch, so the second one must not carry a signature
        assertFalse(secondCallPart.has("thoughtSignature"));
        JsonNode secondCall = secondCallPart.get("functionCall");
        assertEquals("item-get-state", secondCall.get("name").asText());
        assertEquals("Lamp2", secondCall.get("args").get("item").asText());
        assertEquals("call-2", secondCall.get("id").asText());

        JsonNode returnTurn = contents.get(2);
        assertEquals("user", returnTurn.get("role").asText());
        JsonNode returnParts = returnTurn.get("parts");
        assertEquals(2, returnParts.size());

        // the results are paired with the calls by position, and the call id is echoed so that
        // equally named parallel calls can be told apart
        JsonNode firstResponse = returnParts.get(0).get("functionResponse");
        assertEquals("item-get-state", firstResponse.get("name").asText());
        assertEquals("call-1", firstResponse.get("id").asText());
        assertEquals("Lamp1 is ON", firstResponse.get("response").get("result").asText());

        JsonNode secondResponse = returnParts.get(1).get("functionResponse");
        assertEquals("item-get-state", secondResponse.get("name").asText());
        assertEquals("call-2", secondResponse.get("id").asText());
        assertEquals("Lamp2 is OFF", secondResponse.get("response").get("result").asText());
    }

    @Test
    public void singleToolCallUsesLegacyMessageFormatAndOmitsAbsentIds() throws Exception {
        // conversations stored by earlier versions hold a single call as a JSON object and its result as plain text
        GeminiLLMToolCall call = new GeminiLLMToolCall("item-get-state", Map.of("item", "Lamp1"), null, null);
        List<Conversation.Message> history = List.of(new Conversation.Message(1, ConversationRole.USER, PROMPT),
                new Conversation.Message(2, ConversationRole.TOOL_CALL, call.toJson()),
                new Conversation.Message(3, ConversationRole.TOOL_RETURN, "Lamp1 is ON"));

        apiClient.sendPrompt(MODEL, history, List.of(), null, null, null, null, null);

        JsonNode contents = captureRequestBody().get("contents");
        assertEquals(3, contents.size());

        JsonNode callParts = contents.get(1).get("parts");
        assertEquals("model", contents.get(1).get("role").asText());
        assertEquals(1, callParts.size());
        assertFalse(callParts.get(0).has("thoughtSignature"));
        JsonNode functionCall = callParts.get(0).get("functionCall");
        assertEquals("item-get-state", functionCall.get("name").asText());
        assertEquals("Lamp1", functionCall.get("args").get("item").asText());
        // no id was stored, so none must be sent
        assertFalse(functionCall.has("id"));

        JsonNode returnParts = contents.get(2).get("parts");
        assertEquals("user", contents.get(2).get("role").asText());
        assertEquals(1, returnParts.size());
        JsonNode functionResponse = returnParts.get(0).get("functionResponse");
        assertEquals("item-get-state", functionResponse.get("name").asText());
        assertEquals("Lamp1 is ON", functionResponse.get("response").get("result").asText());
        assertFalse(functionResponse.has("id"));
    }

    @Test
    public void sequentialToolCallsStaySeparateTurns() throws Exception {
        // two loop iterations with one call each: no parallel marker, so no merging may happen -
        // each call keeps its own model turn with its own thought signature
        GeminiLLMToolCall call1 = new GeminiLLMToolCall("item-get-state", Map.of("item", "Lamp1"), "call-1",
                "signature-abc");
        GeminiLLMToolCall call2 = new GeminiLLMToolCall("item-get-state", Map.of("item", "Lamp2"), "call-2",
                "signature-def");
        List<Conversation.Message> history = List.of(new Conversation.Message(1, ConversationRole.USER, PROMPT),
                new Conversation.Message(2, ConversationRole.TOOL_CALL, call1.toJson()),
                new Conversation.Message(3, ConversationRole.TOOL_RETURN, "Lamp1 is ON"),
                new Conversation.Message(4, ConversationRole.TOOL_CALL, call2.toJson()),
                new Conversation.Message(5, ConversationRole.TOOL_RETURN, "Lamp2 is OFF"));

        apiClient.sendPrompt(MODEL, history, List.of(), null, null, null, null, null);

        JsonNode contents = captureRequestBody().get("contents");
        assertEquals(5, contents.size());

        assertEquals("model", contents.get(1).get("role").asText());
        assertEquals(1, contents.get(1).get("parts").size());
        assertEquals("signature-abc", contents.get(1).get("parts").get(0).get("thoughtSignature").asText());
        assertEquals("user", contents.get(2).get("role").asText());
        assertEquals(1, contents.get(2).get("parts").size());
        assertEquals("Lamp1 is ON",
                contents.get(2).get("parts").get(0).get("functionResponse").get("response").get("result").asText());

        assertEquals("model", contents.get(3).get("role").asText());
        assertEquals(1, contents.get(3).get("parts").size());
        assertEquals("signature-def", contents.get(3).get("parts").get(0).get("thoughtSignature").asText());
        assertEquals("call-2", contents.get(3).get("parts").get(0).get("functionCall").get("id").asText());
        assertEquals("user", contents.get(4).get("role").asText());
        assertEquals(1, contents.get(4).get("parts").size());
        assertEquals("Lamp2 is OFF",
                contents.get(4).get("parts").get(0).get("functionResponse").get("response").get("result").asText());
    }

    private JsonNode captureRequestBody() throws Exception {
        ArgumentCaptor<ContentProvider> captor = ArgumentCaptor.forClass(ContentProvider.class);
        verify(request).content(captor.capture());

        StringBuilder body = new StringBuilder();
        for (ByteBuffer buffer : Objects.requireNonNull(captor.getValue())) {
            body.append(StandardCharsets.UTF_8.decode(buffer));
        }
        JsonNode root = objectMapper.readTree(body.toString());
        assertTrue(root.has("contents"), "request payload has no contents");
        return root;
    }
}
