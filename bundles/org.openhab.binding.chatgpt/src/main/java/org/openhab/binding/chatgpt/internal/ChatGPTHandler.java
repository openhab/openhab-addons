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
package org.openhab.binding.chatgpt.internal;

import static org.openhab.binding.chatgpt.internal.ChatGPTBindingConstants.DEFAULT_MAX_TOKENS;
import static org.openhab.binding.chatgpt.internal.ChatGPTBindingConstants.DEFAULT_MODEL;
import static org.openhab.binding.chatgpt.internal.ChatGPTBindingConstants.DEFAULT_SYSTEM_MESSAGE;
import static org.openhab.binding.chatgpt.internal.ChatGPTBindingConstants.DEFAULT_TEMPERATURE;
import static org.openhab.binding.chatgpt.internal.ChatGPTBindingConstants.DEFAULT_TOP_P;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.chatgpt.internal.api.ChatGPTApiClient;
import org.openhab.binding.chatgpt.internal.api.ChatGPTApiException;
import org.openhab.binding.chatgpt.internal.api.dto.ChatMessage;
import org.openhab.binding.chatgpt.internal.api.dto.ChatResponse;
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

/**
 * The {@link ChatGPTHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Artur Fedjukevits - Replaced gson with jackson
 */
@NonNullByDefault
public class ChatGPTHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(ChatGPTHandler.class);

    private final HttpClient httpClient;
    private @Nullable ChatGPTConfiguration config;
    private @Nullable ChatGPTApiClient apiClient;
    private String lastPrompt = "";
    private List<String> models = List.of();

    public ChatGPTHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    public @Nullable ChatGPTApiClient getApiClient() {
        return apiClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof StringType stringCommand) {
            lastPrompt = stringCommand.toFullString();

            Channel channel = getThing().getChannel(channelUID);
            if (channel == null) {
                logger.error("Channel with UID '{}' cannot be found on Thing '{}'.", channelUID, getThing().getUID());
                return;
            }

            ChatGPTApiClient client = apiClient;

            if (client != null) {
                final var timeout = resolveTimeout(channelUID);
                String model = (config != null && !config.model.isBlank()) ? config.model : DEFAULT_MODEL;
                double temp = config != null ? config.temperature : DEFAULT_TEMPERATURE;
                double topP = config != null ? config.topP : DEFAULT_TOP_P;
                int maxTokens = config != null ? config.maxTokens : DEFAULT_MAX_TOKENS;
                String systemMessage = DEFAULT_SYSTEM_MESSAGE;

                ChatGPTChannelConfiguration channelConfig = channel.getConfiguration()
                        .as(ChatGPTChannelConfiguration.class);

                String channelModel = channelConfig.model;
                if (channelModel != null && !channelModel.isBlank()) {
                    model = channelModel;
                }

                Double channelTemp = channelConfig.temperature;
                if (channelTemp != null) {
                    temp = channelTemp;
                }

                Double channelTopP = channelConfig.topP;
                if (channelTopP != null) {
                    topP = channelTopP;
                }

                Integer channelMaxTokens = channelConfig.maxTokens;
                if (channelMaxTokens != null) {
                    maxTokens = channelMaxTokens;
                }

                String channelSystemMessage = channelConfig.systemMessage;
                if (channelSystemMessage != null && !channelSystemMessage.isBlank()) {
                    systemMessage = channelSystemMessage;
                }

                try {
                    ChatResponse response = client.sendPrompt(model, lastPrompt, systemMessage, temp, topP, maxTokens,
                            timeout);
                    processChatResponse(channelUID, response);
                    updateStatus(ThingStatus.ONLINE);
                } catch (ChatGPTApiException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Could not connect to OpenAI API: " + e.getMessage());
                    logger.debug("Request to OpenAI failed: {}", e.getMessage(), e);
                }
            }
        }
    }

    private void processChatResponse(ChannelUID channelUID, ChatResponse chatResponse) {
        if (chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
            String finishReason = chatResponse.getChoices().get(0).getFinishReason();

            if ("length".equals(finishReason)) {
                logger.warn("Token length exceeded. Increase maximum token limit to avoid the issue.");
                return;
            }

            @Nullable
            ChatMessage chatResponseMessage = chatResponse.getChoices().getFirst().getChatMessage();
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

    public @Nullable ChatGPTConfiguration getConfigAs() {
        return this.config;
    }

    private @Nullable Integer resolveTimeout(ChannelUID channelUID) {
        Channel channel = getThing().getChannel(channelUID);
        if (channel != null) {
            ChatGPTChannelConfiguration channelConfig = channel.getConfiguration()
                    .as(ChatGPTChannelConfiguration.class);
            if (channelConfig.requestTimeout != null) {
                return channelConfig.requestTimeout;
            }
        }
        ChatGPTConfiguration c = config;
        return c != null ? c.requestTimeout : null;
    }

    @Override
    public void initialize() {
        final ChatGPTConfiguration c = getConfigAs(ChatGPTConfiguration.class);
        this.config = c;

        String apiKey = c.apiKey;

        if (apiKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error");
            return;
        }

        if (!isValidTimeout(c.requestTimeout)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/requestTimeout.configuration-error");
            return;
        }
        if (thing.getChannels().stream()
                .map(channel -> channel.getConfiguration().as(ChatGPTChannelConfiguration.class))
                .anyMatch(config -> !isValidTimeout(config.requestTimeout))) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/requestTimeout.configuration-error");
            return;
        }

        this.apiClient = new ChatGPTApiClient(httpClient, apiKey, c.baseUrl);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            ChatGPTApiClient client = this.apiClient;
            if (client == null) {
                return;
            }
            try {
                List<String> apiModels = client.fetchModels(c.requestTimeout);
                if (!apiModels.isEmpty()) {
                    updateStatus(ThingStatus.ONLINE);
                    this.models = List.copyOf(apiModels);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.communication-error");
                }
            } catch (ChatGPTApiException e) {
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

    boolean isValidTimeout(@Nullable Integer timeout) {
        return timeout == null || timeout > 0;
    }
}
