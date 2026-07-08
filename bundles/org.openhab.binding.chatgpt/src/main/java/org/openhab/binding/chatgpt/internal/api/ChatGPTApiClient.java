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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.openhab.binding.chatgpt.internal.api.dto.ChatFunction;
import org.openhab.binding.chatgpt.internal.api.dto.ChatFunctionCall;
import org.openhab.binding.chatgpt.internal.api.dto.ChatMessage;
import org.openhab.binding.chatgpt.internal.api.dto.ChatRequestBody;
import org.openhab.binding.chatgpt.internal.api.dto.ChatResponse;
import org.openhab.binding.chatgpt.internal.api.dto.ChatToolCalls;
import org.openhab.binding.chatgpt.internal.api.dto.ChatTools;
import org.openhab.binding.chatgpt.internal.api.dto.Parameters;
import org.openhab.binding.chatgpt.internal.api.dto.ToolChoice;
import org.openhab.core.voice.text.conversation.Conversation;
import org.openhab.core.voice.text.conversation.ConversationRole;
import org.openhab.core.voice.text.interpreter.llm.LLMTool;
import org.openhab.core.voice.text.interpreter.llm.LLMToolParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link ChatGPTApiClient} handles JSON mapping and HTTP REST request logic for OpenAI Chat Completions API.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class ChatGPTApiClient {
    private static final String PATH_CHAT_COMPLETIONS = "/chat/completions";
    private static final String PATH_MODELS = "/models";

    private final Logger logger = LoggerFactory.getLogger(ChatGPTApiClient.class);
    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public ChatGPTApiClient(HttpClient httpClient, String apiKey, String baseUrl) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setDefaultPropertyInclusion(
                com.fasterxml.jackson.annotation.JsonInclude.Value.construct(Include.NON_NULL, Include.ALWAYS));
    }

    /**
     * Sends a simple message prompt to the ChatGPT completions API.
     *
     * @param model the model name
     * @param prompt the prompt message
     * @param systemMessageStr system prompt
     * @param temperature temperature
     * @param topP top_p
     * @param maxTokens max completion tokens
     * @param timeoutSeconds request timeout
     * @return deserialized ChatResponse
     * @throws ChatGPTApiException if an error occurs
     */
    public ChatResponse sendPrompt(String model, String prompt, @Nullable String systemMessageStr,
            @Nullable Double temperature, @Nullable Double topP, @Nullable Integer maxTokens,
            @Nullable Integer timeoutSeconds) throws ChatGPTApiException {
        List<ChatMessage> messages = new ArrayList<>();

        if (systemMessageStr != null && !systemMessageStr.isBlank()) {
            ChatMessage systemMessage = new ChatMessage();
            systemMessage.setRole(ChatMessage.Role.SYSTEM.value());
            systemMessage.setContent(systemMessageStr);
            messages.add(systemMessage);
        }

        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole(ChatMessage.Role.USER.value());
        userMessage.setContent(prompt);
        messages.add(userMessage);

        return sendPrompt(model, messages, temperature, topP, maxTokens, null, timeoutSeconds);
    }

    /**
     * Sends conversation history and core framework tools to the ChatGPT completions API.
     *
     * @param model the model name
     * @param history conversation messages
     * @param tools available tools
     * @param systemMessageStr system prompt
     * @param temperature temperature
     * @param topP top_p
     * @param maxTokens max completion tokens
     * @param timeoutSeconds request timeout
     * @return deserialized ChatResponse
     * @throws ChatGPTApiException if an error occurs
     */
    public ChatResponse sendPrompt(String model, List<Conversation.Message> history, List<LLMTool> tools,
            @Nullable String systemMessageStr, @Nullable Double temperature, @Nullable Double topP,
            @Nullable Integer maxTokens, @Nullable Integer timeoutSeconds) throws ChatGPTApiException {
        List<ChatMessage> chatMessages = new ArrayList<>();

        if (systemMessageStr != null && !systemMessageStr.isBlank()) {
            ChatMessage systemMessage = new ChatMessage();
            systemMessage.setRole(ChatMessage.Role.SYSTEM.value());
            systemMessage.setContent(systemMessageStr);
            chatMessages.add(systemMessage);
        }

        Queue<PendingToolCall> pendingToolCalls = new LinkedList<>();

        for (int i = 0; i < history.size(); i++) {
            Conversation.Message msg = history.get(i);
            switch (msg.role()) {
                case USER: {
                    ChatMessage chatMsg = new ChatMessage();
                    chatMsg.setRole(ChatMessage.Role.USER.value());
                    chatMsg.setContent(msg.content());
                    chatMessages.add(chatMsg);
                    break;
                }
                case OPENHAB: {
                    ChatMessage chatMsg = new ChatMessage();
                    chatMsg.setRole(ChatMessage.Role.ASSISTANT.value());
                    chatMsg.setContent(msg.content());
                    chatMessages.add(chatMsg);
                    break;
                }
                case TOOL_CALL: {
                    List<ChatToolCalls> toolCallsList = new ArrayList<>();
                    int j = i;
                    while (j < history.size() && history.get(j).role() == ConversationRole.TOOL_CALL) {
                        try {
                            ChatGPTLLMToolCall toolCall = ChatGPTLLMToolCall.fromJson(history.get(j).content());
                            String toolCallId = toolCall.id;
                            if (toolCallId == null) {
                                toolCallId = "";
                            }
                            String name = toolCall.tool.replaceAll("[^a-zA-Z0-9_-]", "_");
                            pendingToolCalls.add(new PendingToolCall(toolCallId, name));

                            ChatFunctionCall cfc = new ChatFunctionCall();
                            cfc.setName(name);
                            cfc.setArguments(objectMapper.writeValueAsString(toolCall.params));

                            ChatToolCalls ctc = new ChatToolCalls();
                            ctc.setId(toolCallId);
                            ctc.setType("function");
                            ctc.setFunction(cfc);

                            toolCallsList.add(ctc);
                        } catch (Exception e) {
                            logger.warn("Failed to parse TOOL_CALL message content: {}", e.getMessage(), e);
                        }
                        j++;
                    }
                    i = j - 1;

                    ChatMessage assistantMsg = new ChatMessage();
                    assistantMsg.setRole(ChatMessage.Role.ASSISTANT.value());
                    assistantMsg.setToolCalls(toolCallsList);
                    chatMessages.add(assistantMsg);
                    break;
                }
                case TOOL_RETURN: {
                    PendingToolCall pending = pendingToolCalls.poll();
                    if (pending == null) {
                        logger.trace("Skipping orphaned TOOL_RETURN");
                        break;
                    }
                    ChatMessage toolMsg = new ChatMessage();
                    toolMsg.setRole(ChatMessage.Role.TOOL.value());
                    toolMsg.setToolCallId(pending.id);
                    toolMsg.setName(pending.name);
                    toolMsg.setContent(msg.content());
                    chatMessages.add(toolMsg);
                    break;
                }
                case THINKING:
                    break;
            }
        }

        List<ChatTools> chatToolsList = new ArrayList<>();
        for (LLMTool tool : tools) {
            ChatFunction function = new ChatFunction();
            function.setName(tool.getUID().replaceAll("[^a-zA-Z0-9_-]", "_"));
            function.setDescription(tool.getDescription(null));

            Parameters parameters = new Parameters();
            parameters.setType("object");
            Map<String, Parameters.Property> properties = new HashMap<>();
            List<String> required = new ArrayList<>();

            for (LLMToolParam param : tool.getParamDescriptions(null)) {
                Parameters.Property prop = new Parameters.Property();
                prop.setDescription(param.description());
                prop.setType(param.type().name().toLowerCase(Locale.ROOT));
                if (!param.options().isEmpty()) {
                    prop.setEnumValues(param.options());
                }
                if ("array".equals(prop.getType())) {
                    Parameters.Property itemProp = new Parameters.Property();
                    itemProp.setType("string");
                    prop.setItems(itemProp);
                }
                properties.put(param.name(), prop);
                if (param.required()) {
                    required.add(param.name());
                }
            }

            parameters.setProperties(properties);
            if (!required.isEmpty()) {
                parameters.setRequired(required);
            }
            function.setParameters(parameters);

            ChatTools chatTool = new ChatTools();
            chatTool.setType("function");
            chatTool.setFunction(function);
            chatToolsList.add(chatTool);
        }

        return sendPrompt(model, chatMessages, temperature, topP, maxTokens, chatToolsList, timeoutSeconds);
    }

    private ChatResponse sendPrompt(String model, List<ChatMessage> messages, @Nullable Double temperature,
            @Nullable Double topP, @Nullable Integer maxTokens, @Nullable List<ChatTools> tools,
            @Nullable Integer timeoutSeconds) throws ChatGPTApiException {
        ChatRequestBody chatRequestBody = new ChatRequestBody();
        chatRequestBody.setModel(model);
        chatRequestBody.setTemperature(temperature);
        chatRequestBody.setTopP(topP);
        chatRequestBody.setMaxTokens(maxTokens);
        chatRequestBody.setMessages(messages);
        if (tools != null && !tools.isEmpty()) {
            chatRequestBody.setTools(tools);
            chatRequestBody.setToolChoice(ToolChoice.AUTO.value());
        }

        String queryJson;
        try {
            queryJson = objectMapper.writeValueAsString(chatRequestBody);
        } catch (JsonProcessingException e) {
            throw new ChatGPTApiException("Failed to serialize request body: " + e.getMessage(), e);
        }

        return executeCompletionRequest(queryJson, timeoutSeconds);
    }

    private ChatResponse executeCompletionRequest(String queryJson, @Nullable Integer timeoutSeconds)
            throws ChatGPTApiException {
        Request request = httpClient.newRequest(baseUrl + PATH_CHAT_COMPLETIONS).method(HttpMethod.POST)
                .timeout(timeoutSeconds != null ? timeoutSeconds : 10, TimeUnit.SECONDS)
                .header(HttpHeader.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON.asString())
                .header(HttpHeader.AUTHORIZATION, "Bearer " + apiKey)
                .content(new StringContentProvider(queryJson, StandardCharsets.UTF_8));

        logger.trace("Query '{}'", queryJson);
        try {
            ContentResponse response = request.send();
            if (response.getStatus() == HttpStatus.OK_200) {
                String body = response.getContentAsString();
                return objectMapper.readValue(body, ChatResponse.class);
            } else {
                throw new ChatGPTApiException("ChatGPT API request failed with HTTP status " + response.getStatus()
                        + " " + response.getReason() + ": " + response.getContentAsString());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChatGPTApiException("API request interrupted: " + e.getMessage(), e);
        } catch (TimeoutException | ExecutionException | IOException e) {
            throw new ChatGPTApiException("API request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches the supported models list from OpenAI API models endpoint.
     *
     * @param timeoutSeconds request timeout
     * @return list of model IDs
     * @throws ChatGPTApiException if an error occurs
     */
    public List<String> fetchModels(@Nullable Integer timeoutSeconds) throws ChatGPTApiException {
        Request request = httpClient.newRequest(baseUrl + PATH_MODELS)
                .timeout(timeoutSeconds != null ? timeoutSeconds : 10, TimeUnit.SECONDS).method(HttpMethod.GET)
                .header(HttpHeader.AUTHORIZATION, "Bearer " + apiKey);
        try {
            ContentResponse response = request.send();
            if (response.getStatus() == HttpStatus.OK_200) {
                JsonNode modelsNode = objectMapper.readTree(response.getContentAsString());
                JsonNode data = modelsNode.get("data");
                List<String> modelList = new ArrayList<>();
                if (data != null) {
                    data.forEach(model -> {
                        JsonNode id = model.get("id");
                        if (id != null) {
                            modelList.add(id.asText());
                        }
                    });
                }
                return modelList;
            } else {
                throw new ChatGPTApiException(
                        "Fetching models failed with HTTP status " + response.getStatus() + " " + response.getReason());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChatGPTApiException("Fetching models interrupted: " + e.getMessage(), e);
        } catch (TimeoutException | ExecutionException | IOException e) {
            throw new ChatGPTApiException("Fetching models failed: " + e.getMessage(), e);
        }
    }

    private static class PendingToolCall {
        final String id;
        final String name;

        PendingToolCall(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
