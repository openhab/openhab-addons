/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.chatgpt.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.chatgpt.internal.dto.ChatMessage;
import org.openhab.binding.chatgpt.internal.dto.ChatRequestBody;
import org.openhab.binding.chatgpt.internal.dto.ChatResponse;
import org.openhab.binding.chatgpt.internal.hli.ChatGPTHLIService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link ChatGPTHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Artur Fedjukevits - Replaced gson with jackson
 */
@NonNullByDefault
public class ChatGPTHandler extends BaseThingHandler {

    private static final int REQUEST_TIMEOUT_MS = 10_000;
    private final Logger logger = LoggerFactory.getLogger(ChatGPTHandler.class);

    private HttpClient httpClient;
    private @Nullable ChatGPTConfiguration config;
    private String apiKey = "";
    private String apiUrl = "";
    private String modelUrl = "";
    private String lastPrompt = "";
    private List<String> models = List.of();

    public ChatGPTHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof StringType stringCommand) {
            lastPrompt = stringCommand.toFullString();

            String queryJson = prepareRequestBody(channelUID);

            if (queryJson != null) {
                String response = sendPrompt(queryJson);
                processChatResponse(channelUID, response);
            }
        }
    }

    private void processChatResponse(ChannelUID channelUID, @Nullable String response) {
        if (response != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            ChatResponse chatResponse;
            try {
                chatResponse = objectMapper.readValue(response, ChatResponse.class);
            } catch (JsonProcessingException e) {
                logger.error("Failed to parse ChatGPT response: {}", e.getMessage(), e);
                return;
            }

            if (chatResponse != null) {
                String finishReason = chatResponse.getChoices().get(0).getFinishReason();

                if ("length".equals(finishReason)) {
                    logger.warn("Token length exceeded. Increase maximum token limit to avoid the issue.");
                    return;
                }

                @Nullable
                ChatMessage chatResponseMessage = chatResponse.getChoices().get(0).getChatMessage();

                if (chatResponseMessage == null) {
                    logger.error("ChatGPT response does not contain a message.");
                    return;
                }

                @Nullable
                String msg = chatResponseMessage.getContent();
                if (msg != null) {
                    updateState(channelUID, new StringType(msg));
                }

            } else {
                logger.warn("Didn't receive any response from ChatGPT - this is unexpected.");
            }
        }
    }

    private @Nullable String prepareRequestBody(ChannelUID channelUID) {
        Channel channel = getThing().getChannel(channelUID);
        if (channel == null) {
            logger.error("Channel with UID '{}' cannot be found on Thing '{}'.", channelUID, getThing().getUID());
            return null;
        }

        ChatGPTChannelConfiguration channelConfig = channel.getConfiguration().as(ChatGPTChannelConfiguration.class);

        List<ChatMessage> messages = new ArrayList<>();

        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setRole(ChatMessage.Role.SYSTEM.value());
        systemMessage.setContent(channelConfig.systemMessage);
        messages.add(systemMessage);

        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole(ChatMessage.Role.USER.value());
        userMessage.setContent(lastPrompt);
        messages.add(userMessage);

        ChatRequestBody chatRequestBody = new ChatRequestBody();

        chatRequestBody.setModel(channelConfig.model);
        chatRequestBody.setTemperature(channelConfig.temperature);
        chatRequestBody.setMaxTokens(channelConfig.maxTokens);
        chatRequestBody.setTopP(channelConfig.topP);
        chatRequestBody.setMessages(messages);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);

        try {
            return objectMapper.writeValueAsString(chatRequestBody);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize ChatGPT request: {}", e.getMessage(), e);
            return null;
        }
    }

    public @Nullable String sendPrompt(String queryJson) {
        Request request = httpClient.newRequest(apiUrl).method(HttpMethod.POST)
                .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey).content(new StringContentProvider(queryJson));
        logger.trace("Query '{}'", queryJson);
        try {
            ContentResponse response = request.send();
            updateStatus(ThingStatus.ONLINE);
            if (response.getStatus() == HttpStatus.OK_200) {
                return response.getContentAsString();
            } else {
                logger.error("ChatGPT request resulted in HTTP {} with message: {}", response.getStatus(),
                        response.getReason());
                return null;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not connect to OpenAI API: " + e.getMessage());
            logger.debug("Request to OpenAI failed: {}", e.getMessage(), e);
            return null;
        }
    }

    public @Nullable ChatGPTConfiguration getConfigAs() {
        return this.config;
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(ChatGPTConfiguration.class);

        String apiKey = config.apiKey;

        if (apiKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error");
            return;
        }

        this.apiKey = apiKey;
        this.apiUrl = config.apiUrl;
        this.modelUrl = config.modelUrl;

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                Request request = httpClient.newRequest(modelUrl).timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        .method(HttpMethod.GET).header("Authorization", "Bearer " + apiKey);
                ContentResponse response = request.send();
                if (response.getStatus() == 200) {
                    updateStatus(ThingStatus.ONLINE);
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        JsonNode models = objectMapper.readTree(response.getContentAsString());
                        JsonNode data = models.get("data");

                        if (data != null) {
                            logger.debug("Models: {}", data.toString());
                            List<String> modelList = new ArrayList<>();
                            data.forEach(model -> {
                                JsonNode id = model.get("id");
                                if (id != null) {
                                    modelList.add(id.asText());
                                }
                            });

                            this.models = List.copyOf(modelList);
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "@text/offline.communication-error");
                        }
                    } catch (JsonProcessingException e) {
                        logger.warn("Failed to parse models: {}", e.getMessage(), e);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.communication-error");
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    List<String> getModels() {
        return models;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(ChatGPTModelOptionProvider.class, ChatGPTHLIService.class);
    }
}
