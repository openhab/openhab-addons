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
package org.openhab.binding.gemini.internal.hli;

import static org.openhab.binding.gemini.internal.GeminiBindingConstants.BINDING_ID;
import static org.openhab.binding.gemini.internal.GeminiBindingConstants.DEFAULT_MODEL;
import static org.openhab.binding.gemini.internal.GeminiBindingConstants.DEFAULT_SYSTEM_MESSAGE;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gemini.internal.GeminiConfiguration;
import org.openhab.binding.gemini.internal.GeminiHandler;
import org.openhab.binding.gemini.internal.api.GeminiApiClient;
import org.openhab.binding.gemini.internal.api.GeminiApiException;
import org.openhab.binding.gemini.internal.api.dto.GeminiContent;
import org.openhab.binding.gemini.internal.api.dto.GeminiFunctionCall;
import org.openhab.binding.gemini.internal.api.dto.GeminiPart;
import org.openhab.binding.gemini.internal.api.dto.response.GeminiCandidate;
import org.openhab.binding.gemini.internal.api.dto.response.GeminiResponse;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.voice.text.HumanLanguageInterpreter;
import org.openhab.core.voice.text.InterpretationException;
import org.openhab.core.voice.text.InterpreterContext;
import org.openhab.core.voice.text.conversation.Conversation;
import org.openhab.core.voice.text.conversation.ConversationException;
import org.openhab.core.voice.text.conversation.ConversationRole;
import org.openhab.core.voice.text.interpreter.llm.LLMTool;
import org.openhab.core.voice.text.interpreter.llm.LLMToolCall;
import org.openhab.core.voice.text.interpreter.llm.LLMToolException;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link GeminiHLIService} is a {@link HumanLanguageInterpreter} implementation based on Google Gemini.
 *
 * @author Florian Hotze - Initial contribution
 */
@Component(service = { GeminiHLIService.class, HumanLanguageInterpreter.class })
@NonNullByDefault
public class GeminiHLIService implements ThingHandlerService, HumanLanguageInterpreter {
    private static final String LABEL = "Gemini Human Language Interpreter";

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

    @Override
    public String getId() {
        return BINDING_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return LABEL;
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        return Set.of();
    }

    @Override
    public Set<String> getSupportedGrammarFormats() {
        return Set.of();
    }

    @Override
    public @Nullable String getGrammar(Locale locale, String format) {
        return null;
    }

    @Override
    public String interpret(Locale locale, String text) throws InterpretationException {
        GeminiHandler geminiHandler = handler;
        if (geminiHandler == null) {
            throw new InterpretationException("GeminiHandler is not initialized");
        }
        GeminiApiClient apiClient = geminiHandler.getApiClient();
        if (apiClient == null) {
            throw new InterpretationException("Gemini API client is not available");
        }
        GeminiConfiguration config = geminiHandler.getGeminiConfiguration();
        if (config == null) {
            throw new InterpretationException("Gemini HLI configuration is not available");
        }
        String model = config.model.isBlank() ? DEFAULT_MODEL : config.model;
        try {
            GeminiResponse geminiResponse = apiClient.sendPrompt(model, text, DEFAULT_SYSTEM_MESSAGE,
                    config.temperature, config.topP, config.maxOutputTokens, config.requestTimeout);
            String response = geminiResponse.getFirstText();
            if (response != null) {
                return response;
            }
        } catch (GeminiApiException e) {
            var ex = new InterpretationException("Failed to get response from Gemini: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
        throw new InterpretationException("No valid response received");
    }

    @Override
    public String interpret(Locale locale, InterpreterContext interpreterContext) throws InterpretationException {
        GeminiHandler geminiHandler = handler;
        if (geminiHandler == null) {
            throw new InterpretationException("GeminiHandler is not initialized");
        }
        GeminiApiClient apiClient = geminiHandler.getApiClient();
        if (apiClient == null) {
            throw new InterpretationException("Gemini API client is not available");
        }

        GeminiConfiguration config = geminiHandler.getGeminiConfiguration();
        if (config == null) {
            throw new InterpretationException("Gemini HLI configuration is not available");
        }

        Conversation conversation = interpreterContext.conversation();
        List<LLMTool> tools = interpreterContext.tools();

        String systemMessage = interpreterContext.systemPrompt();
        if (systemMessage == null || systemMessage.isBlank()) {
            throw new InterpretationException("System prompt is missing or empty");
        }
        String locationItem = interpreterContext.locationItem();
        if (locationItem != null && !locationItem.isBlank()) {
            systemMessage = systemMessage.trim() + "\n\n" + "Your location (item name): " + locationItem;
        }
        if (!tools.isEmpty()) {
            String toolGuidance = """
                    You have tools available to interact with the environment. Use them when appropriate.
                    However, if the user's request cannot be fulfilled by any tool, or if they ask a general question,
                    answer them directly using your general knowledge.
                    Do not try to force a tool call. Do not state that you can only perform actions supported by the tools.
                    When using a tool, always provide a clear and concise natural language response.
                    """;
            systemMessage = systemMessage.trim() + "\n\n" + toolGuidance;
        }

        int loopCount = 0;
        final int maxLoops = 10;
        while (true) {
            if (loopCount >= maxLoops) {
                throw new InterpretationException(
                        "Tool execution loop limit exceeded (max " + maxLoops + " iterations)");
            }
            loopCount++;
            String model = config.model.isBlank() ? DEFAULT_MODEL : config.model;
            try {
                GeminiResponse geminiResponse = apiClient.sendPrompt(model, conversation.getMessages(), tools,
                        systemMessage, config.temperature, config.topP, config.maxOutputTokens, config.requestTimeout);

                List<GeminiCandidate> candidates = geminiResponse.candidates();
                if (candidates == null || candidates.isEmpty()) {
                    throw new InterpretationException("No valid response received");
                }
                GeminiContent responseContent = candidates.getFirst().content();
                if (responseContent == null) {
                    throw new InterpretationException("No valid response received");
                }
                List<GeminiPart> parts = responseContent.parts();
                if (parts == null) {
                    throw new InterpretationException("No valid response received");
                }

                boolean hasToolCall = false;
                StringBuilder textBuilder = new StringBuilder();

                for (GeminiPart part : parts) {
                    GeminiFunctionCall fc = part.functionCall();
                    if (fc != null) {
                        hasToolCall = true;
                        String toolName = fc.name();
                        Map<String, Object> args = fc.args();

                        LLMToolCall llmToolCall = new LLMToolCall(toolName != null ? toolName : "",
                                args != null ? args : new HashMap<>());
                        try {
                            conversation.addMessage(ConversationRole.TOOL_CALL, llmToolCall.toJson());
                        } catch (ConversationException e) {
                            var ex = new InterpretationException("Failed to add TOOL_CALL to conversation");
                            ex.initCause(e);
                            throw ex;
                        }

                        String result = executeTool(tools, toolName, args, locale);

                        try {
                            conversation.addMessage(ConversationRole.TOOL_RETURN, result);
                        } catch (ConversationException e) {
                            var ex = new InterpretationException("Failed to add TOOL_RETURN to conversation");
                            ex.initCause(e);
                            throw ex;
                        }
                    } else if (part.text() != null) {
                        textBuilder.append(part.text());
                    }
                }

                if (!hasToolCall) {
                    String finalResponse = textBuilder.toString();
                    try {
                        conversation.addMessage(ConversationRole.OPENHAB, finalResponse);
                    } catch (ConversationException e) {
                        var ex = new InterpretationException("Failed to add OPENHAB message to conversation");
                        ex.initCause(e);
                        throw ex;
                    }
                    return finalResponse;
                }
            } catch (GeminiApiException e) {
                InterpretationException ex = new InterpretationException(
                        "Failed to communicate with Gemini: " + e.getMessage());
                ex.initCause(e);
                throw ex;
            }
        }
    }

    private String executeTool(List<LLMTool> tools, @Nullable String toolName, @Nullable Map<String, Object> args,
            Locale locale) {
        if (toolName == null) {
            return "Error: Tool name is null";
        }
        LLMTool tool = tools.stream().filter(t -> t.getUID().replaceAll("[^a-zA-Z0-9_-]", "_").equals(toolName))
                .findFirst().orElse(null);
        if (tool == null) {
            return "Error: Tool " + toolName + " not found";
        }
        try {
            return tool.call(args != null ? args : new HashMap<>(), locale);
        } catch (LLMToolException e) {
            return "Error: " + e.getMessage();
        }
    }
}
