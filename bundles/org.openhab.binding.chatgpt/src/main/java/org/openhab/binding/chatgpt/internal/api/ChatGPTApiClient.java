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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Set<String> modelsRequiringReasoningEffort = ConcurrentHashMap.newKeySet();

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
    public ChatResponse sendPrompt(String model, List<Conversation.Message> history, Collection<LLMTool> tools,
            @Nullable String systemMessageStr, @Nullable Double temperature, @Nullable Double topP,
            @Nullable Integer maxTokens, @Nullable Integer timeoutSeconds) throws ChatGPTApiException {
        List<ChatMessage> chatMessages = new ArrayList<>();

        if (systemMessageStr != null && !systemMessageStr.isBlank()) {
            ChatMessage systemMessage = new ChatMessage();
            systemMessage.setRole(ChatMessage.Role.SYSTEM.value());
            systemMessage.setContent(systemMessageStr);
            chatMessages.add(systemMessage);
        }

        // Convert internal conversation history into API-compliant ChatMessage objects.
        // Maintain a queue of pending tool calls so TOOL_RETURN messages can match their assistant tool_calls.
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
                    // Aggregate contiguous TOOL_CALL messages into a single assistant message containing tool_calls.
                    List<ChatToolCalls> toolCallsList = new ArrayList<>();
                    List<PendingToolCall> groupPending = new ArrayList<>();
                    int j = i;
                    while (j < history.size() && history.get(j).role() == ConversationRole.TOOL_CALL) {
                        try {
                            ChatGPTLLMToolCall toolCall = ChatGPTLLMToolCall.fromJson(history.get(j).content());
                            String toolCallId = toolCall.id;
                            if (toolCallId == null || toolCallId.isEmpty()) {
                                // Synthesize a unique, deterministic ID if missing, so tool_calls and TOOL_RETURN share
                                // a valid ID.
                                toolCallId = "tc_" + j;
                            }
                            String name = toolCall.tool.replaceAll("[^a-zA-Z0-9_-]", "_");

                            ChatFunctionCall cfc = new ChatFunctionCall();
                            cfc.setName(name);
                            cfc.setArguments(objectMapper.writeValueAsString(toolCall.params));

                            ChatToolCalls ctc = new ChatToolCalls();
                            ctc.setId(toolCallId);
                            ctc.setType("function");
                            ctc.setFunction(cfc);

                            toolCallsList.add(ctc);
                            groupPending.add(new PendingToolCall(toolCallId, name));
                        } catch (Exception e) {
                            logger.warn("Failed to parse TOOL_CALL message content: {}", e.getMessage(), e);
                        }
                        j++;
                    }
                    i = j - 1;

                    // Count matching TOOL_RETURN messages answering this group.
                    // OpenAI-compatible APIs strictly require every tool_call in an assistant message to be answered by
                    // a tool message.
                    // If a previous turn failed mid-execution, trim any unanswered (orphaned) tool calls.
                    int answered = 0;
                    for (int k = j; k < history.size() && history.get(k).role() == ConversationRole.TOOL_RETURN; k++) {
                        answered++;
                    }
                    if (answered < toolCallsList.size()) {
                        logger.debug("Dropping {} orphaned tool call(s) without a matching TOOL_RETURN",
                                toolCallsList.size() - answered);
                        toolCallsList.subList(answered, toolCallsList.size()).clear();
                        groupPending.subList(answered, groupPending.size()).clear();
                    }

                    // If all tool calls in the group were orphaned, skip emitting an assistant message.
                    if (toolCallsList.isEmpty()) {
                        break;
                    }

                    pendingToolCalls.addAll(groupPending);

                    ChatMessage assistantMsg = new ChatMessage();
                    assistantMsg.setRole(ChatMessage.Role.ASSISTANT.value());
                    assistantMsg.setToolCalls(toolCallsList);
                    chatMessages.add(assistantMsg);
                    break;
                }
                case TOOL_RETURN: {
                    // Match each tool return with its corresponding pending tool call from the previous assistant
                    // message.
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

        // If we know this model requires reasoning_effort for tool requests, use it preemptively
        boolean hasTools = tools != null && !tools.isEmpty();
        String reasoningEffort = hasTools && modelsRequiringReasoningEffort.contains(model) ? "none" : null;
        return executeCompletionRequest(queryJson, model, hasTools, timeoutSeconds, reasoningEffort);
    }

    private ChatResponse executeCompletionRequest(String queryJson, String model, boolean hasTools,
            @Nullable Integer timeoutSeconds, @Nullable String reasoningEffort) throws ChatGPTApiException {
        String finalJson = queryJson;
        if (reasoningEffort != null) {
            try {
                JsonNode jsonNode = objectMapper.readTree(queryJson);
                ((com.fasterxml.jackson.databind.node.ObjectNode) jsonNode).put("reasoning_effort", reasoningEffort);
                finalJson = objectMapper.writeValueAsString(jsonNode);
            } catch (IOException e) {
                logger.debug("Failed to add reasoning_effort to request: {}", e.getMessage());
            }
        }

        Request request = httpClient.newRequest(baseUrl + PATH_CHAT_COMPLETIONS).method(HttpMethod.POST)
                .timeout(timeoutSeconds != null ? timeoutSeconds : 10, TimeUnit.SECONDS)
                .header(HttpHeader.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON.asString())
                .content(new StringContentProvider(finalJson, StandardCharsets.UTF_8));
        if (!apiKey.isBlank()) {
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + apiKey);
        }

        logger.debug("Request to {} (POST): payload size = {} bytes", baseUrl + PATH_CHAT_COMPLETIONS,
                queryJson.getBytes(StandardCharsets.UTF_8).length);
        if (logger.isTraceEnabled()) {
            try {
                String prettyRequest = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(objectMapper.readTree(queryJson));
                logger.trace("Request payload to {} (POST):\n{}", baseUrl + PATH_CHAT_COMPLETIONS, prettyRequest);
            } catch (IOException e) {
                logger.trace("Request payload to {} (POST): {}", baseUrl + PATH_CHAT_COMPLETIONS, queryJson);
            }
        }
        try {
            ContentResponse response = request.send();
            if (response.getStatus() == HttpStatus.OK_200) {
                String body = response.getContentAsString();
                ChatResponse chatResponse = objectMapper.readValue(body, ChatResponse.class);
                ChatResponse.Usage usage = chatResponse.getUsage();
                if (usage != null) {
                    logger.debug(
                            "Response from {} (POST): payload size = {} bytes, prompt tokens = {}, completion tokens = {}, total tokens = {}",
                            baseUrl + PATH_CHAT_COMPLETIONS, body.getBytes(StandardCharsets.UTF_8).length,
                            usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
                } else {
                    logger.debug("Response from {} (POST): payload size = {} bytes", baseUrl + PATH_CHAT_COMPLETIONS,
                            body.getBytes(StandardCharsets.UTF_8).length);
                }
                if (logger.isTraceEnabled()) {
                    try {
                        String prettyResponse = objectMapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(objectMapper.readTree(body));
                        logger.trace("Response payload from {} (POST):\n{}", baseUrl + PATH_CHAT_COMPLETIONS,
                                prettyResponse);
                    } catch (IOException e) {
                        logger.trace("Response payload from {} (POST):\n{}", baseUrl + PATH_CHAT_COMPLETIONS, body);
                    }
                }
                return chatResponse;
            } else {
                String errorBody = response.getContentAsString();
                logger.debug("Error response from {} (POST): HTTP {} {}, payload size = {} bytes",
                        baseUrl + PATH_CHAT_COMPLETIONS, response.getStatus(), response.getReason(),
                        errorBody.getBytes(StandardCharsets.UTF_8).length);

                if (reasoningEffort == null && hasTools && response.getStatus() == HttpStatus.BAD_REQUEST_400
                        && errorBody.contains("reasoning_effort")) {
                    logger.debug("Model {} requires reasoning_effort; caching and retrying", model);
                    modelsRequiringReasoningEffort.add(model);
                    return executeCompletionRequest(queryJson, model, hasTools, timeoutSeconds, "none");
                }
                if (logger.isTraceEnabled()) {
                    try {
                        String prettyError = objectMapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(objectMapper.readTree(errorBody));
                        logger.trace("Error response payload from {} (POST):\n{}", baseUrl + PATH_CHAT_COMPLETIONS,
                                prettyError);
                    } catch (IOException e) {
                        logger.trace("Error response payload from {} (POST):\n{}", baseUrl + PATH_CHAT_COMPLETIONS,
                                errorBody);
                    }
                }
                throw new ChatGPTApiException(
                        "ChatGPT API request failed with HTTP " + response.getStatus() + " " + response.getReason());
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
                .timeout(timeoutSeconds != null ? timeoutSeconds : 10, TimeUnit.SECONDS).method(HttpMethod.GET);
        if (!apiKey.isBlank()) {
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + apiKey);
        }

        logger.debug("Request to {} (GET)", baseUrl + PATH_MODELS);
        try {
            ContentResponse response = request.send();
            if (response.getStatus() == HttpStatus.OK_200) {
                String body = response.getContentAsString();
                logger.debug("Response from {} (GET): payload size = {} bytes", baseUrl + PATH_MODELS,
                        body.getBytes(StandardCharsets.UTF_8).length);
                if (logger.isTraceEnabled()) {
                    logger.trace("Response payload from {} (GET):\n{}", baseUrl + PATH_MODELS, body);
                }
                JsonNode modelsNode = objectMapper.readTree(body);
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
                        "Fetching models failed with HTTP " + response.getStatus() + " " + response.getReason());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChatGPTApiException("Fetching models interrupted: " + e.getMessage(), e);
        } catch (TimeoutException | ExecutionException | IOException e) {
            throw new ChatGPTApiException("Fetching models failed: " + e.getMessage(), e);
        }
    }

    private record PendingToolCall(String id, String name) {
    }
}
