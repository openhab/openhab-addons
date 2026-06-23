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

import static org.openhab.binding.gemini.internal.GeminiBindingConstants.DEFAULT_REQUEST_TIMEOUT;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.gemini.internal.api.dto.GeminiContent;
import org.openhab.binding.gemini.internal.api.dto.GeminiFunctionCall;
import org.openhab.binding.gemini.internal.api.dto.GeminiPart;
import org.openhab.binding.gemini.internal.api.dto.request.GeminiFunctionDeclaration;
import org.openhab.binding.gemini.internal.api.dto.request.GeminiFunctionResponse;
import org.openhab.binding.gemini.internal.api.dto.request.GeminiGenerationConfig;
import org.openhab.binding.gemini.internal.api.dto.request.GeminiRequest;
import org.openhab.binding.gemini.internal.api.dto.request.GeminiSchema;
import org.openhab.binding.gemini.internal.api.dto.request.GeminiTool;
import org.openhab.binding.gemini.internal.api.dto.response.GeminiModel;
import org.openhab.binding.gemini.internal.api.dto.response.GeminiModelsResponse;
import org.openhab.binding.gemini.internal.api.dto.response.GeminiResponse;
import org.openhab.core.voice.text.conversation.Conversation;
import org.openhab.core.voice.text.interpreter.llm.LLMTool;
import org.openhab.core.voice.text.interpreter.llm.LLMToolCall;
import org.openhab.core.voice.text.interpreter.llm.LLMToolParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link GeminiApiClient} class encapsulates all HTTP/REST API communications with the Google Gemini API.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class GeminiApiClient {
    private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final String HEADER_API_KEY = "x-goog-api-key";
    private static final String ROLE_USER = "user";
    private static final String ROLE_MODEL = "model";

    private final Logger logger = LoggerFactory.getLogger(GeminiApiClient.class);

    private final HttpClient httpClient;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public GeminiApiClient(HttpClient httpClient, String apiKey) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setDefaultPropertyInclusion(
                com.fasterxml.jackson.annotation.JsonInclude.Value.construct(Include.NON_NULL, Include.ALWAYS));
    }

    /**
     * Sends a single prompt to the Gemini API and returns the response DTO.
     *
     * @param model the Gemini model to use
     * @param prompt the prompt text
     * @param systemMessage the system instruction text
     * @param temperature the temperature config parameter
     * @param topP the topP config parameter
     * @param maxOutputTokens the maxOutputTokens config parameter
     * @param timeoutSeconds request timeout in seconds
     * @return the deserialized GeminiResponse
     * @throws GeminiApiException if a communication error, timeout, or parsing error occurs
     */
    public GeminiResponse sendPrompt(String model, String prompt, @Nullable String systemMessage,
            @Nullable Double temperature, @Nullable Double topP, @Nullable Integer maxOutputTokens,
            @Nullable Integer timeoutSeconds) throws GeminiApiException {
        GeminiContent systemInstruction = createSystemInstruction(systemMessage);

        // Contents
        GeminiPart userPart = new GeminiPart(prompt, null, null, null);
        GeminiContent userContent = new GeminiContent(ROLE_USER, List.of(userPart));

        // Config
        GeminiGenerationConfig genConfig = new GeminiGenerationConfig(maxOutputTokens, temperature, topP, null);

        GeminiRequest request = new GeminiRequest(List.of(userContent), systemInstruction, genConfig, null);

        return executeGenerateContentRequest(model, request, timeoutSeconds);
    }

    /**
     * Sends a conversation history and tools to the Gemini API and returns the response DTO.
     *
     * @param model the Gemini model to use
     * @param history the conversation message history
     * @param tools the available tools
     * @param systemMessage the system instruction text
     * @param temperature the temperature config parameter
     * @param topP the topP config parameter
     * @param maxOutputTokens the maxOutputTokens config parameter
     * @param timeoutSeconds request timeout in seconds
     * @return the deserialized GeminiResponse
     * @throws GeminiApiException if a communication error, timeout, or parsing error occurs
     */
    public GeminiResponse sendPrompt(String model, List<Conversation.Message> history, List<LLMTool> tools,
            @Nullable String systemMessage, @Nullable Double temperature, @Nullable Double topP,
            @Nullable Integer maxOutputTokens, @Nullable Integer timeoutSeconds) throws GeminiApiException {
        GeminiContent systemInstruction = createSystemInstruction(systemMessage);

        List<GeminiContent> contents = new ArrayList<>();
        Queue<String> pendingToolCallNames = new LinkedList<>();

        for (Conversation.Message msg : history) {
            switch (msg.role()) {
                case USER: {
                    GeminiPart part = new GeminiPart(msg.content(), null, null, null);
                    contents.add(new GeminiContent(ROLE_USER, List.of(part)));
                    break;
                }
                case OPENHAB: {
                    GeminiPart part = new GeminiPart(msg.content(), null, null, null);
                    contents.add(new GeminiContent(ROLE_MODEL, List.of(part)));
                    break;
                }
                case TOOL_CALL: {
                    LLMToolCall toolCall = LLMToolCall.fromJson(msg.content());
                    String name = toolCall.tool().replaceAll("[^a-zA-Z0-9_-]", "_");
                    pendingToolCallNames.add(name);
                    GeminiFunctionCall fc = new GeminiFunctionCall(name, toolCall.params());
                    GeminiPart part = new GeminiPart(null, fc, null, null);
                    contents.add(new GeminiContent(ROLE_MODEL, List.of(part)));
                    break;
                }
                case TOOL_RETURN: {
                    String name = pendingToolCallNames.poll();
                    if (name == null) {
                        logger.trace("skipping orphaned TOOL_RETURN");
                        break; // TOOL_RETURN without preceding TOOL_CALL - ignore
                    }
                    GeminiFunctionResponse fr = new GeminiFunctionResponse(name, Map.of("result", msg.content()));
                    GeminiPart part = new GeminiPart(null, null, fr, null);
                    contents.add(new GeminiContent(ROLE_USER, List.of(part)));
                    break;
                }
                case THINKING:
                    break;
            }
        }

        while (!contents.isEmpty()) {
            GeminiContent first = contents.getFirst();
            List<GeminiPart> firstParts = first.parts();
            boolean isValidFirst = ROLE_USER.equals(first.role()) && firstParts != null
                    && firstParts.stream().anyMatch(p -> p.text() != null);
            if (isValidFirst) {
                break;
            }
            logger.trace("removing leading invalid content entry with role '{}'", first.role());
            contents.removeFirst();
        }

        List<GeminiTool> geminiTools = null;
        if (!tools.isEmpty()) {
            List<GeminiFunctionDeclaration> functions = new ArrayList<>();
            for (LLMTool t : tools) {
                Map<String, GeminiSchema> properties = new HashMap<>();
                List<String> required = new ArrayList<>();

                for (LLMToolParam p : t.getParamDescriptions(null)) {
                    List<String> enumValues = p.options().isEmpty() ? null : p.options();
                    String typeStr = p.type().name().toLowerCase(Locale.ROOT);
                    GeminiSchema itemsSchema = null;
                    if ("array".equals(typeStr)) {
                        itemsSchema = new GeminiSchema("string");
                    }
                    GeminiSchema prop = new GeminiSchema(typeStr, p.description(), null, null, enumValues, itemsSchema);
                    properties.put(p.name(), prop);
                    if (p.required()) {
                        required.add(p.name());
                    }
                }

                GeminiSchema params = new GeminiSchema("object", null, properties, required.isEmpty() ? null : required,
                        null, null);

                String fName = t.getUID().replaceAll("[^a-zA-Z0-9_-]", "_");
                String fDesc = t.getDescription(null);
                functions.add(new GeminiFunctionDeclaration(fName, fDesc, params));
            }
            geminiTools = List.of(new GeminiTool(functions));
        }

        GeminiGenerationConfig genConfig = new GeminiGenerationConfig(maxOutputTokens, temperature, topP, null);

        GeminiRequest request = new GeminiRequest(contents, systemInstruction, genConfig, geminiTools);

        return executeGenerateContentRequest(model, request, timeoutSeconds);
    }

    /**
     * Executes an HTTP POST request to the <code>generateContent</code> endpoint of a Gemini model.
     *
     * <p>
     * If the request fails with 503 Service Unavailable, up to two retries are performed with with an increasing delay.
     *
     * @param model the Gemini model to use
     * @param requestPayload the payload to send
     * @param timeoutSeconds request timeout in seconds
     * @return the deserialized GeminiResponse
     * @throws GeminiApiException if a communication error, timeout, or parsing error occurs
     */
    private GeminiResponse executeGenerateContentRequest(String model, GeminiRequest requestPayload,
            @Nullable Integer timeoutSeconds) throws GeminiApiException {
        String url = GEMINI_API_BASE_URL + "/models/" + model + ":generateContent";

        String queryJson;
        try {
            queryJson = objectMapper.writeValueAsString(requestPayload);
        } catch (JsonProcessingException e) {
            throw new GeminiApiException("Failed to serialize Gemini request: " + e.getMessage(), e);
        }

        int attemptCount = 1;
        while (true) {
            Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                    .timeout((Objects.requireNonNullElse(timeoutSeconds, DEFAULT_REQUEST_TIMEOUT)), TimeUnit.SECONDS)
                    .header(HttpHeader.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON.asString())
                    .header(HEADER_API_KEY, apiKey)
                    .content(new StringContentProvider(queryJson, StandardCharsets.UTF_8));
            if (logger.isDebugEnabled()) {
                try {
                    String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(requestPayload);
                    logger.debug("Request to {} (attempt {}): \n{}", url, attemptCount, prettyJson);
                } catch (JsonProcessingException e) {
                    logger.debug("Request to {} (attempt {}): {}", url, attemptCount, queryJson);
                }
            }

            try {
                ContentResponse response = request.send();
                if (response.getStatus() == HttpStatus.OK_200) {
                    String responseBody = response.getContentAsString();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Response from {}: {}", url, responseBody);
                    }
                    try {
                        @Nullable
                        GeminiResponse geminiResponse = readNullableValue(responseBody, GeminiResponse.class);
                        if (geminiResponse == null) {
                            throw new GeminiApiException("Failed to parse Gemini response: response was null");
                        }
                        return geminiResponse;
                    } catch (JsonProcessingException e) {
                        throw new GeminiApiException("Failed to parse Gemini response: " + e.getMessage(), e);
                    }
                } else if (response.getStatus() == HttpStatus.SERVICE_UNAVAILABLE_503 && attemptCount <= 3) {
                    logger.debug("Gemini request failed with 503 Service Unavailable on attempt #{}/3", attemptCount);
                    try {
                        Thread.sleep(1000 * attemptCount);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new GeminiApiException(
                                "Interrupted while waiting to retry Gemini API request: " + e.getMessage(), e);
                    }
                    attemptCount++;
                } else {
                    logger.debug("Gemini request failed on the final attempt with HTTP {} {}: {}", response.getStatus(),
                            response.getReason(), response.getContentAsString());
                    throw new GeminiApiException("Gemini generateContent request resulted failed with  HTTP "
                            + response.getStatus() + " " + response.getReason());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new GeminiApiException("Could not connect to Gemini API: " + e.getMessage(), e);
            } catch (TimeoutException | ExecutionException e) {
                throw new GeminiApiException("Could not connect to Gemini API: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Fetches the list of models supported by the Gemini API key.
     *
     * @param timeoutSeconds request timeout in seconds
     * @return a list of GeminiModel objects
     * @throws GeminiApiException if a communication error, timeout, or parsing error occurs
     */
    public List<GeminiModel> fetchModels(@Nullable Integer timeoutSeconds) throws GeminiApiException {
        String modelsUrl = GEMINI_API_BASE_URL + "/models";
        Request request = httpClient.newRequest(modelsUrl)
                .timeout(Objects.requireNonNullElse(timeoutSeconds, DEFAULT_REQUEST_TIMEOUT), TimeUnit.SECONDS)
                .method(HttpMethod.GET).header(HEADER_API_KEY, apiKey);
        logger.debug("Request to {}: (GET)", modelsUrl);

        try {
            ContentResponse response = request.send();
            if (response.getStatus() == HttpStatus.OK_200) {
                String responseBody = response.getContentAsString();
                if (logger.isDebugEnabled()) {
                    logger.debug("Response from {}: {}", modelsUrl, responseBody);
                }
                try {
                    @Nullable
                    GeminiModelsResponse modelsResponse = readNullableValue(responseBody, GeminiModelsResponse.class);
                    if (modelsResponse == null) {
                        throw new GeminiApiException("Failed to parse models response DTO: response was null");
                    }
                    List<GeminiModel> models = modelsResponse.models();
                    if (models != null) {
                        return models;
                    }
                } catch (JsonProcessingException e) {
                    throw new GeminiApiException("Failed to parse models response DTO: " + e.getMessage(), e);
                }
            } else {
                throw new GeminiApiException(
                        "Gemini request for models resulted in HTTP status " + response.getStatus());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GeminiApiException("Could not retrieve models from Gemini API: " + e.getMessage(), e);
        } catch (TimeoutException | ExecutionException e) {
            throw new GeminiApiException("Could not retrieve models from Gemini API: " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private @Nullable GeminiContent createSystemInstruction(@Nullable String systemMessage) {
        if (systemMessage != null && !systemMessage.isBlank()) {
            GeminiPart sysPart = new GeminiPart(systemMessage, null, null, null);
            return new GeminiContent(null, List.of(sysPart));
        }
        return null;
    }

    private <T> @Nullable T readNullableValue(String content, Class<T> valueType) throws JsonProcessingException {
        return objectMapper.readValue(content, valueType);
    }
}
