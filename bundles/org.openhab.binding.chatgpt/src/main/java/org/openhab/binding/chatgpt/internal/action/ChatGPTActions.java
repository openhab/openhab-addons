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
package org.openhab.binding.chatgpt.internal.action;

import static org.openhab.binding.chatgpt.internal.ChatGPTBindingConstants.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.chatgpt.internal.ChatGPTConfiguration;
import org.openhab.binding.chatgpt.internal.ChatGPTHandler;
import org.openhab.binding.chatgpt.internal.api.ChatGPTApiClient;
import org.openhab.binding.chatgpt.internal.api.ChatGPTApiException;
import org.openhab.binding.chatgpt.internal.api.dto.ChatMessage;
import org.openhab.binding.chatgpt.internal.api.dto.ChatResponse;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChatGPTActions} class provides Thing Actions for the ChatGPT binding.
 *
 * @author Florian Hotze - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ChatGPTActions.class)
@ThingActionsScope(name = "chatgpt")
@NonNullByDefault
public class ChatGPTActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(ChatGPTActions.class);

    private @Nullable ChatGPTHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ChatGPTHandler chatGPTHandler) {
            this.handler = chatGPTHandler;
        } else {
            this.handler = null;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    public @Nullable String sendMessage(@Nullable String prompt) {
        return sendMessage(prompt, null);
    }

    public @Nullable String sendMessage(@Nullable String prompt, @Nullable String model) {
        return sendMessage(prompt, model, null, null, null, null, null, null);
    }

    @RuleAction(label = "@text/action.sendMessage.label", description = "@text/action.sendMessage.description")
    public @Nullable @ActionOutput(label = "@text/action.sendMessage.result.label", description = "@text/action.sendMessage.result.description", type = "java.lang.String") String sendMessage(
            @ActionInput(name = "prompt", label = "@text/action.sendMessage.prompt.label", description = "@text/action.sendMessage.prompt.description", type = "java.lang.String", required = true) @Nullable String prompt,
            @ActionInput(name = "model", label = "@text/action.sendMessage.model.label", description = "@text/action.sendMessage.model.description", type = "java.lang.String") @Nullable String model,
            @ActionInput(name = "systemMessage", label = "@text/action.sendMessage.systemMessage.label", description = "@text/action.sendMessage.systemMessage.description", type = "java.lang.String") @Nullable String systemMessage,
            @ActionInput(name = "temperature", label = "@text/action.sendMessage.temperature.label", description = "@text/action.sendMessage.temperature.description", type = "java.lang.Double") @Nullable Double temperature,
            @ActionInput(name = "topP", label = "@text/action.sendMessage.topP.label", description = "@text/action.sendMessage.topP.description", type = "java.lang.Double") @Nullable Double topP,
            @ActionInput(name = "maxTokens", label = "@text/action.sendMessage.maxTokens.label", description = "@text/action.sendMessage.maxTokens.description", type = "java.lang.Integer") @Nullable Integer maxTokens,
            @ActionInput(name = "reasoningEffort", label = "@text/action.sendMessage.reasoningEffort.label", description = "@text/action.sendMessage.reasoningEffort.description", type = "java.lang.String") @Nullable String reasoningEffort,
            @ActionInput(name = "requestTimeout", label = "@text/action.sendMessage.requestTimeout.label", description = "@text/action.sendMessage.requestTimeout.description", type = "java.lang.Integer") @Nullable Integer requestTimeout) {
        if (prompt == null || prompt.isBlank()) {
            logger.warn("Cannot send message: prompt is null or blank.");
            return null;
        }

        ChatGPTHandler chatGPTHandler = handler;
        if (chatGPTHandler == null) {
            logger.warn("Cannot send message: ChatGPT handler is not initialized.");
            return null;
        }
        ChatGPTApiClient apiClient = chatGPTHandler.getApiClient();
        if (apiClient == null) {
            logger.warn("Cannot send message: ChatGPT API client is not initialized.");
            return null;
        }

        ChatGPTConfiguration config = chatGPTHandler.getConfigAs();

        String resolvedModel = (model != null && !model.isBlank()) ? model
                : ((config != null && !config.model.isBlank()) ? config.model : DEFAULT_MODEL);
        String resolvedSystemMessage = Objects.requireNonNullElse(systemMessage, DEFAULT_SYSTEM_MESSAGE);
        double resolvedTemperature = Objects.requireNonNullElse(temperature,
                config != null ? config.temperature : DEFAULT_TEMPERATURE);
        double resolvedTopP = Objects.requireNonNullElse(topP, config != null ? config.topP : DEFAULT_TOP_P);
        int resolvedMaxTokens = Objects.requireNonNullElse(maxTokens,
                config != null ? config.maxTokens : DEFAULT_MAX_TOKENS);
        String resolvedReasoningEffort = (reasoningEffort != null && !reasoningEffort.isBlank()) ? reasoningEffort
                : ((config != null && !config.reasoningEffort.isBlank()) ? config.reasoningEffort
                        : DEFAULT_REASONING_EFFORT);
        int resolvedRequestTimeout = Objects.requireNonNullElse(requestTimeout,
                config != null ? config.requestTimeout : DEFAULT_REQUEST_TIMEOUT);

        try {
            ChatResponse response = apiClient.sendPrompt(resolvedModel, prompt, resolvedSystemMessage,
                    resolvedTemperature, resolvedTopP, resolvedMaxTokens, resolvedReasoningEffort,
                    resolvedRequestTimeout);
            if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                ChatMessage chatResponseMessage = response.getChoices().getFirst().getChatMessage();
                if (chatResponseMessage != null && chatResponseMessage.getContent() != null) {
                    return chatResponseMessage.getContent();
                }
            }
            logger.warn("Didn't receive any chat response choices from ChatGPT - this is unexpected.");
        } catch (ChatGPTApiException e) {
            logger.debug("Request to OpenAI via action failed: {}", e.getMessage(), e);
        }
        return null;
    }

    public static @Nullable String sendMessage(ThingActions actions, @Nullable String prompt) {
        if (!(actions instanceof ChatGPTActions chatGPTActions)) {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ChatGPTActions");
        }
        return chatGPTActions.sendMessage(prompt);
    }

    public static @Nullable String sendMessage(ThingActions actions, @Nullable String prompt, @Nullable String model) {
        if (!(actions instanceof ChatGPTActions chatGPTActions)) {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ChatGPTActions");
        }
        return chatGPTActions.sendMessage(prompt, model);
    }

    public static @Nullable String sendMessage(ThingActions actions, @Nullable String prompt, @Nullable String model,
            @Nullable String systemMessage, @Nullable Double temperature, @Nullable Double topP,
            @Nullable Integer maxTokens, @Nullable String reasoningEffort, @Nullable Integer requestTimeout) {
        if (!(actions instanceof ChatGPTActions chatGPTActions)) {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of ChatGPTActions");
        }

        return chatGPTActions.sendMessage(prompt, model, systemMessage, temperature, topP, maxTokens, reasoningEffort,
                requestTimeout);
    }
}
