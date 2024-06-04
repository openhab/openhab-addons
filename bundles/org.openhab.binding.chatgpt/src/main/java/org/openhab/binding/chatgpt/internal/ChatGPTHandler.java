/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.chatgpt.internal.ChatGPTBindingConstants.*;

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
import org.openhab.binding.chatgpt.internal.dto.ChatResponse;
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
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link ChatGPTHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class ChatGPTHandler extends BaseThingHandler {

    private static final int REQUEST_TIMEOUT_MS = 10_000;
    private final Logger logger = LoggerFactory.getLogger(ChatGPTHandler.class);

    private HttpClient httpClient;
    private Gson gson = new Gson();

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
        if (command instanceof RefreshType && !"".equals(lastPrompt)) {
            String response = sendPrompt(channelUID, lastPrompt);
            processChatResponse(channelUID, response);
        }

        if (command instanceof StringType stringCommand) {
            lastPrompt = stringCommand.toFullString();
            String response = sendPrompt(channelUID, lastPrompt);
            processChatResponse(channelUID, response);
        }
    }

    private void processChatResponse(ChannelUID channelUID, @Nullable String response) {
        if (response != null) {
            ChatResponse chatResponse = gson.fromJson(response, ChatResponse.class);
            if (chatResponse != null) {
                String msg = chatResponse.getChoices().get(0).getMessage().getContent();
                updateState(channelUID, new StringType(msg));
            } else {
                logger.warn("Didn't receive any response from ChatGPT - this is unexpected.");
            }
        }
    }

    private @Nullable String sendPrompt(ChannelUID channelUID, String prompt) {
        Channel channel = getThing().getChannel(channelUID);
        if (channel == null) {
            logger.error("Channel with UID '{}' cannot be found on Thing '{}'.", channelUID, getThing().getUID());
            return null;
        }
        ChatGPTChannelConfiguration channelConfig = channel.getConfiguration().as(ChatGPTChannelConfiguration.class);

        JsonObject root = new JsonObject();
        root.addProperty("temperature", channelConfig.temperature);
        root.addProperty("model", channelConfig.model);
        root.addProperty("max_tokens", channelConfig.maxTokens);

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", channelConfig.systemMessage);
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        JsonArray messages = new JsonArray(2);
        messages.add(systemMessage);
        messages.add(userMessage);
        root.add("messages", messages);

        String queryJson = gson.toJson(root);
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

    @Override
    public void initialize() {
        ChatGPTConfiguration config = getConfigAs(ChatGPTConfiguration.class);

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
                    JsonObject jsonObject = gson.fromJson(response.getContentAsString(), JsonObject.class);
                    if (jsonObject != null) {
                        JsonArray data = jsonObject.getAsJsonArray("data");

                        List<String> modelIds = new ArrayList<>();
                        for (JsonElement element : data) {
                            JsonObject model = element.getAsJsonObject();
                            String id = model.get("id").getAsString();
                            modelIds.add(id);
                        }
                        this.models = List.copyOf(modelIds);
                    } else {
                        logger.warn("Did not receive a valid JSON response from the models endpoint.");
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
        return List.of(ChatGPTModelOptionProvider.class);
    }
}
