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
package org.openhab.binding.gemini.internal.action;

import static org.openhab.binding.gemini.internal.GeminiBindingConstants.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gemini.internal.GeminiConfiguration;
import org.openhab.binding.gemini.internal.GeminiHandler;
import org.openhab.binding.gemini.internal.api.GeminiApiClient;
import org.openhab.binding.gemini.internal.api.GeminiApiException;
import org.openhab.binding.gemini.internal.api.dto.request.GeminiThinkingLevel;
import org.openhab.binding.gemini.internal.api.dto.response.GeminiResponse;
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
 * The {@link GeminiActions} class provides Thing Actions for the Gemini binding.
 *
 * @author Florian Hotze - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = GeminiActions.class)
@ThingActionsScope(name = "gemini")
@NonNullByDefault
public class GeminiActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(GeminiActions.class);

    private @Nullable GeminiHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof GeminiHandler geminiHandler) {
            this.handler = geminiHandler;
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

    public @Nullable String sendMessage(@Nullable String prompt, @Nullable String model, @Nullable String systemMessage,
            @Nullable Double temperature, @Nullable Double topP, @Nullable Integer maxOutputTokens,
            @Nullable Integer requestTimeout) {
        return sendMessage(prompt, model, systemMessage, temperature, topP, maxOutputTokens, null, requestTimeout);
    }

    @RuleAction(label = "@text/action.sendMessage.label", description = "@text/action.sendMessage.description")
    public @Nullable @ActionOutput(label = "@text/action.sendMessage.result.label", description = "@text/action.sendMessage.result.description", type = "java.lang.String") String sendMessage(
            @ActionInput(name = "prompt", label = "@text/action.sendMessage.prompt.label", description = "@text/action.sendMessage.prompt.description", type = "java.lang.String", required = true) @Nullable String prompt,
            @ActionInput(name = "model", label = "@text/action.sendMessage.model.label", description = "@text/action.sendMessage.model.description", type = "java.lang.String") @Nullable String model,
            @ActionInput(name = "systemMessage", label = "@text/action.sendMessage.systemMessage.label", description = "@text/action.sendMessage.systemMessage.description", type = "java.lang.String") @Nullable String systemMessage,
            @ActionInput(name = "temperature", label = "@text/action.sendMessage.temperature.label", description = "@text/action.sendMessage.temperature.description", type = "java.lang.Double") @Nullable Double temperature,
            @ActionInput(name = "topP", label = "@text/action.sendMessage.topP.label", description = "@text/action.sendMessage.topP.description", type = "java.lang.Double") @Nullable Double topP,
            @ActionInput(name = "maxOutputTokens", label = "@text/action.sendMessage.maxOutputTokens.label", description = "@text/action.sendMessage.maxOutputTokens.description", type = "java.lang.Integer") @Nullable Integer maxOutputTokens,
            @ActionInput(name = "thinkingLevel", label = "@text/action.sendMessage.thinkingLevel.label", description = "@text/action.sendMessage.thinkingLevel.description", type = "java.lang.String") @Nullable String thinkingLevel,
            @ActionInput(name = "requestTimeout", label = "@text/action.sendMessage.requestTimeout.label", description = "@text/action.sendMessage.requestTimeout.description", type = "java.lang.Integer") @Nullable Integer requestTimeout) {
        if (prompt == null || prompt.isBlank()) {
            logger.warn("Cannot send message: prompt is null or blank.");
            return null;
        }

        GeminiHandler geminiHandler = handler;
        if (geminiHandler == null) {
            logger.warn("Cannot send message: Gemini handler is not initialized.");
            return null;
        }
        GeminiApiClient apiClient = geminiHandler.getApiClient();
        if (apiClient == null) {
            logger.warn("Cannot send message: Gemini API client is not initialized.");
            return null;
        }

        GeminiConfiguration config = geminiHandler.getGeminiConfiguration();

        String resolvedModel = (model != null && !model.isBlank()) ? model
                : ((config != null && !config.model.isBlank()) ? config.model : DEFAULT_MODEL);
        String resolvedSystemMessage = Objects.requireNonNullElse(systemMessage, DEFAULT_SYSTEM_MESSAGE);
        double resolvedTemperature = Objects.requireNonNullElse(temperature,
                config != null ? config.temperature : DEFAULT_TEMPERATURE);
        double resolvedTopP = Objects.requireNonNullElse(topP, config != null ? config.topP : DEFAULT_TOP_P);
        int resolvedMaxOutputTokens = Objects.requireNonNullElse(maxOutputTokens,
                config != null ? config.maxOutputTokens : DEFAULT_MAX_OUTPUT_TOKENS);

        // Attempt to parse the passed-in thinking level
        GeminiThinkingLevel resolvedThinkingLevel = GeminiThinkingLevel.fromString(thinkingLevel);
        if (resolvedThinkingLevel == null && thinkingLevel != null && !thinkingLevel.isBlank()) {
            logger.warn("Invalid thinking level: {} (falling back to Thing default)", thinkingLevel);
        }
        // Fallback to the thing's configured thinking level
        if (resolvedThinkingLevel == null && config != null && !config.thinkingLevel.isBlank()) {
            resolvedThinkingLevel = GeminiThinkingLevel.fromString(config.thinkingLevel);
        }
        // Fallback to the default thinking level
        if (resolvedThinkingLevel == null) {
            resolvedThinkingLevel = DEFAULT_THINKING_LEVEL;
        }

        int resolvedRequestTimeout = Objects.requireNonNullElse(requestTimeout,
                config != null ? config.requestTimeout : DEFAULT_REQUEST_TIMEOUT);
        if (!GeminiHandler.isValidTimeout(resolvedRequestTimeout)) {
            resolvedRequestTimeout = DEFAULT_REQUEST_TIMEOUT;
        }

        try {
            GeminiResponse response = apiClient.sendPrompt(resolvedModel, prompt, resolvedSystemMessage,
                    resolvedTemperature, resolvedTopP, resolvedMaxOutputTokens, resolvedThinkingLevel,
                    resolvedRequestTimeout);
            String text = response.getFirstText();
            if (text != null) {
                return text;
            }
            logger.warn("Didn't receive any chat response candidates from Gemini - this is unexpected.");
        } catch (GeminiApiException e) {
            logger.debug("Request to Gemini via action failed: {}", e.getMessage(), e);
        }
        return null;
    }

    public static @Nullable String sendMessage(ThingActions actions, @Nullable String prompt) {
        if (!(actions instanceof GeminiActions geminiActions)) {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of GeminiActions");
        }
        return geminiActions.sendMessage(prompt);
    }

    public static @Nullable String sendMessage(ThingActions actions, @Nullable String prompt, @Nullable String model) {
        if (!(actions instanceof GeminiActions geminiActions)) {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of GeminiActions");
        }
        return geminiActions.sendMessage(prompt, model);
    }

    public static @Nullable String sendMessage(ThingActions actions, @Nullable String prompt, @Nullable String model,
            @Nullable String systemMessage, @Nullable Double temperature, @Nullable Double topP,
            @Nullable Integer maxOutputTokens, @Nullable Integer requestTimeout) {
        return sendMessage(actions, prompt, model, systemMessage, temperature, topP, maxOutputTokens, null,
                requestTimeout);
    }

    public static @Nullable String sendMessage(ThingActions actions, @Nullable String prompt, @Nullable String model,
            @Nullable String systemMessage, @Nullable Double temperature, @Nullable Double topP,
            @Nullable Integer maxOutputTokens, @Nullable String thinkingLevel, @Nullable Integer requestTimeout) {
        if (!(actions instanceof GeminiActions geminiActions)) {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of GeminiActions");
        }

        return geminiActions.sendMessage(prompt, model, systemMessage, temperature, topP, maxOutputTokens,
                thinkingLevel, requestTimeout);
    }
}
