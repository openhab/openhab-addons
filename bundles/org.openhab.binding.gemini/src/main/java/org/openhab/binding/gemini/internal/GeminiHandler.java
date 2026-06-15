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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.gemini.internal.action.GeminiActions;
import org.openhab.binding.gemini.internal.api.GeminiApiClient;
import org.openhab.binding.gemini.internal.api.GeminiApiException;
import org.openhab.binding.gemini.internal.api.dto.response.GeminiModel;
import org.openhab.binding.gemini.internal.api.dto.response.GeminiResponse;
import org.openhab.binding.gemini.internal.hli.GeminiHLIService;
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
    private List<GeminiModel> models = List.of();
    private @Nullable ScheduledFuture<?> initFuture;

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

    public List<GeminiModel> getModels() {
        return models;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel channel = getThing().getChannel(channelUID);
        if (channel != null && CHANNEL_TYPE_UID_CHAT.equals(channel.getChannelTypeUID())
                && command instanceof StringType stringCommand) {
            String lastPrompt = stringCommand.toFullString();

            GeminiConfiguration config = this.config;
            GeminiApiClient client = apiClient;
            if (client != null) {
                int timeout = config != null ? config.requestTimeout : DEFAULT_REQUEST_TIMEOUT;
                String model = (config != null && !config.model.isBlank()) ? config.model : DEFAULT_MODEL;
                double temp = config != null ? config.temperature : DEFAULT_TEMPERATURE;
                double topP = config != null ? config.topP : DEFAULT_TOP_P;
                int maxTokens = config != null ? config.maxOutputTokens : DEFAULT_MAX_OUTPUT_TOKENS;
                String systemMessage = DEFAULT_SYSTEM_MESSAGE;

                GeminiChannelConfiguration channelConfig = channel.getConfiguration()
                        .as(GeminiChannelConfiguration.class);

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

                Integer channelMaxTokens = channelConfig.maxOutputTokens;
                if (channelMaxTokens != null) {
                    maxTokens = channelMaxTokens;
                }

                String channelSystemMessage = channelConfig.systemMessage;
                if (channelSystemMessage != null && !channelSystemMessage.isBlank()) {
                    systemMessage = channelSystemMessage;
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
        String msg = response.getFirstText();
        if (msg != null) {
            updateState(channelUID, new StringType(msg));
        } else {
            logger.warn("Didn't receive any chat response candidates from Gemini - this is unexpected.");
        }
    }

    @Override
    public void initialize() {
        ScheduledFuture<?> future = initFuture;
        if (future != null) {
            future.cancel(true);
            this.initFuture = null;
        }

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

        this.apiClient = new GeminiApiClient(httpClient, apiKey);

        updateStatus(ThingStatus.UNKNOWN);

        this.initFuture = scheduler.schedule(() -> {
            try {
                GeminiApiClient client = this.apiClient;
                GeminiConfiguration currentConfig = this.config;
                if (client == null || currentConfig == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.configuration-error");
                    return;
                }
                List<GeminiModel> apiModels = client.fetchModels(currentConfig.requestTimeout);
                if (!apiModels.isEmpty()) {
                    List<GeminiModel> modelList = new ArrayList<>();
                    Set<String> seenNames = new HashSet<>();
                    for (GeminiModel model : apiModels) {
                        String name = model.name();
                        if (name != null) {
                            List<String> methods = model.supportedGenerationMethods();
                            if (methods != null && methods.contains("generateContent")) {
                                String cleanName = name.replace("models/", "");
                                String lowerName = cleanName.toLowerCase(Locale.ROOT);
                                if (lowerName.startsWith("gemini") || lowerName.startsWith("gemma")) {
                                    if (seenNames.add(cleanName)) {
                                        modelList.add(model);
                                    }
                                }
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
        }, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> future = initFuture;
        if (future != null) {
            future.cancel(true);
            this.initFuture = null;
        }
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(GeminiModelOptionProvider.class, GeminiActions.class, GeminiHLIService.class);
    }

    private boolean invalidTimeout(int timeout) {
        return timeout <= 0;
    }
}
