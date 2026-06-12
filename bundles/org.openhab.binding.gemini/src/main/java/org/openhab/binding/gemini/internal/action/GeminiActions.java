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
import static org.openhab.binding.gemini.internal.GeminiBindingConstants.DEFAULT_SYSTEM_MESSAGE;
import static org.openhab.binding.gemini.internal.GeminiBindingConstants.DEFAULT_TEMPERATURE;
import static org.openhab.binding.gemini.internal.GeminiBindingConstants.DEFAULT_TOP_P;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gemini.internal.GeminiHandler;
import org.openhab.binding.gemini.internal.api.GeminiApiClient;
import org.openhab.binding.gemini.internal.api.GeminiApiException;
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

    public @Nullable String sendMessage(String prompt) {
        return sendMessage(prompt, null);
    }

    public @Nullable String sendMessage(String prompt, @Nullable String model) {
        return sendMessage(prompt, model, null, null, null, null, null);
    }

    @RuleAction(label = "@text/action.sendMessage.label", description = "@text/action.sendMessage.description")
    public @Nullable @ActionOutput(label = "@text/action.sendMessage.result.label", description = "@text/action.sendMessage.result.description", type = "java.lang.String") String sendMessage(
            @ActionInput(name = "prompt", label = "@text/action.sendMessage.prompt.label", description = "@text/action.sendMessage.prompt.description", type = "java.lang.String", required = true) String prompt,
            @ActionInput(name = "model", label = "@text/action.sendMessage.model.label", description = "@text/action.sendMessage.model.description", type = "java.lang.String", defaultValue = DEFAULT_MODEL) @Nullable String model,
            @ActionInput(name = "systemMessage", label = "@text/action.sendMessage.systemMessage.label", description = "@text/action.sendMessage.systemMessage.description", type = "java.lang.String", defaultValue = DEFAULT_SYSTEM_MESSAGE) @Nullable String systemMessage,
            @ActionInput(name = "temperature", label = "@text/action.sendMessage.temperature.label", description = "@text/action.sendMessage.temperature.description", type = "java.lang.Double", defaultValue = DEFAULT_TEMPERATURE_STR) @Nullable Double temperature,
            @ActionInput(name = "topP", label = "@text/action.sendMessage.topP.label", description = "@text/action.sendMessage.topP.description", type = "java.lang.Double", defaultValue = DEFAULT_TOP_P_STR) @Nullable Double topP,
            @ActionInput(name = "maxOutputTokens", label = "@text/action.sendMessage.maxOutputTokens.label", description = "@text/action.sendMessage.maxOutputTokens.description", type = "java.lang.Integer", defaultValue = MAX_OUTPUT_TOKENS_STR) @Nullable Integer maxOutputTokens,
            @ActionInput(name = "requestTimeout", label = "@text/action.sendMessage.requestTimeout.label", description = "@text/action.sendMessage.requestTimeout.description", type = "java.lang.Integer") @Nullable Integer requestTimeout) {
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

        try {
            GeminiResponse response = apiClient.sendPrompt(Objects.requireNonNullElse(model, DEFAULT_MODEL),
                    Objects.requireNonNull(prompt, "prompt must not be null"),
                    Objects.requireNonNullElse(systemMessage, DEFAULT_SYSTEM_MESSAGE),
                    Objects.requireNonNullElse(temperature, DEFAULT_TEMPERATURE),
                    Objects.requireNonNullElse(topP, DEFAULT_TOP_P),
                    Objects.requireNonNullElse(maxOutputTokens, DEFAULT_MAX_OUTPUT_TOKENS), requestTimeout);
            return response.getFirstText();
        } catch (GeminiApiException e) {
            logger.debug("Request to Gemini via action failed: {}", e.getMessage(), e);
        }
        return null;
    }

    public static @Nullable String sendMessage(ThingActions actions, @Nullable String prompt) {
        if (!(actions instanceof GeminiActions geminiActions)) {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of GeminiActions");
        }
        return geminiActions.sendMessage(Objects.requireNonNull(prompt, "prompt must not be null"));
    }

    public static @Nullable String sendMessage(ThingActions actions, @Nullable String prompt, @Nullable String model) {
        if (!(actions instanceof GeminiActions geminiActions)) {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of GeminiActions");
        }
        return geminiActions.sendMessage(Objects.requireNonNull(prompt, "prompt must not be null"), model);
    }

    public static @Nullable String sendMessage(ThingActions actions, @Nullable String prompt, @Nullable String model,
            @Nullable String systemMessage, @Nullable Double temperature, @Nullable Double topP,
            @Nullable Integer maxOutputTokens, @Nullable Integer requestTimeout) {
        if (!(actions instanceof GeminiActions geminiActions)) {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of GeminiActions");
        }

        return geminiActions.sendMessage(Objects.requireNonNull(prompt, "prompt must not be null"), model,
                systemMessage, temperature, topP, maxOutputTokens, requestTimeout);
    }
}
