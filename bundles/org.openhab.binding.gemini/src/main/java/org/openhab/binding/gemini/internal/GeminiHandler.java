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
package org.openhab.binding.gemini.internal;

import static org.openhab.binding.gemini.internal.GeminiBindingConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.gemini.internal.api.GeminiApiClient;
import org.openhab.binding.gemini.internal.api.GeminiApiException;
import org.openhab.binding.gemini.internal.api.dto.GeminiContent;
import org.openhab.binding.gemini.internal.api.dto.GeminiPart;
import org.openhab.binding.gemini.internal.api.dto.response.GeminiCandidate;
import org.openhab.binding.gemini.internal.api.dto.response.GeminiModel;
import org.openhab.binding.gemini.internal.api.dto.response.GeminiResponse;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GeminiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class GeminiHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GeminiHandler.class);

    private final HttpClient httpClient;
    private @Nullable GeminiConfiguration config;
    private @Nullable GeminiApiClient apiClient;
    private List<String> models = List.of();

    public GeminiHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    public @Nullable GeminiConfiguration getGeminiConfiguration() {
        return this.config;
    }

    public @Nullable GeminiApiClient getApiClient() {
        return apiClient;
    }

    public List<String> getModels() {
        return models;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_CHAT.equals(channelUID.getId()) && command instanceof StringType stringCommand) {
            String lastPrompt = stringCommand.toFullString();

            GeminiApiClient client = apiClient;
            if (client != null) {
                Channel channel = getThing().getChannel(channelUID);
                int timeout = DEFAULT_REQUEST_TIMEOUT;
                String model = DEFAULT_MODEL;
                double temp = DEFAULT_TEMPERATURE;
                double topP = DEFAULT_TOP_P;
                int maxTokens = 8092;
                String systemMessage = DEFAULT_SYSTEM_MESSAGE;

                if (channel != null) {
                    GeminiChannelConfiguration channelConfig = channel.getConfiguration()
                            .as(GeminiChannelConfiguration.class);
                    Integer channelTimeout = channelConfig.requestTimeout;
                    if (channelTimeout != null) {
                        timeout = channelTimeout;
                    }

                    String channelModel = channelConfig.model;
                    if (channelModel != null && !channelModel.isBlank()) {
                        model = channelModel;
                    } else {
                        model = DEFAULT_MODEL;
                    }

                    Double channelTemp = channelConfig.temperature;
                    if (channelTemp != null) {
                        temp = channelTemp;
                    }

                    Double channelTopP = channelConfig.topP;
                    if (channelTopP != null) {
                        topP = channelTopP;
                    }

                    Integer channelMaxTokens = channelConfig.maxOutputTokens;
                    if (channelMaxTokens != null) {
                        maxTokens = channelMaxTokens;
                    }

                    String channelSystemMessage = channelConfig.systemMessage;
                    if (channelSystemMessage != null && !channelSystemMessage.isBlank()) {
                        systemMessage = channelSystemMessage;
                    }
                }

                try {
                    GeminiResponse response = client.sendPrompt(model, lastPrompt, systemMessage, temp, topP, maxTokens,
                            timeout);
                    processChatResponse(channelUID, response);
                    updateStatus(ThingStatus.ONLINE);
                } catch (GeminiApiException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Could not connect to Gemini API: " + e.getMessage());
                    logger.debug("Request to Gemini failed: {}", e.getMessage(), e);
                }
            }
        }
    }

    private void processChatResponse(ChannelUID channelUID, GeminiResponse response) {
        List<GeminiCandidate> candidates = response.candidates();
        if (candidates != null && !candidates.isEmpty()) {
            GeminiCandidate candidate = candidates.getFirst();
            GeminiContent content = candidate.content();
            if (content != null) {
                List<GeminiPart> parts = content.parts();
                if (parts != null && !parts.isEmpty()) {
                    @Nullable
                    String msg = parts.getFirst().text();
                    if (msg != null) {
                        updateState(channelUID, new StringType(msg));
                    }
                }
            }
        } else {
            logger.warn("Didn't receive any chat response candidates from Gemini - this is unexpected.");
        }
    }

    @Override
    public void initialize() {
        final GeminiConfiguration c = getConfigAs(GeminiConfiguration.class);
        this.config = c;

        String apiKey = c.apiKey;

        if (apiKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error");
            return;
        }

        if (invalidTimeout(c.requestTimeout)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/requestTimeout.configuration-error");
            return;
        }
        if (thing.getChannels().stream().map(channel -> channel.getConfiguration().as(GeminiChannelConfiguration.class))
                .anyMatch(channelConfig -> invalidTimeout(channelConfig.requestTimeout))) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/requestTimeout.configuration-error");
            return;
        }

        this.apiClient = new GeminiApiClient(httpClient, apiKey);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                GeminiApiClient client = this.apiClient;
                if (client == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.configuration-error");
                    return;
                }
                List<GeminiModel> apiModels = client.fetchModels();
                if (!apiModels.isEmpty()) {
                    List<String> modelList = new ArrayList<>();
                    for (GeminiModel model : apiModels) {
                        String name = model.name();
                        if (name != null) {
                            List<String> methods = model.supportedGenerationMethods();
                            if (methods != null && methods.contains("generateContent")) {
                                modelList.add(name.replace("models/", ""));
                            }
                        }
                    }
                    this.models = List.copyOf(modelList);
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.communication-error");
                }
            } catch (GeminiApiException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    private boolean invalidTimeout(@Nullable Integer timeout) {
        return timeout != null && timeout <= 0;
    }
}
