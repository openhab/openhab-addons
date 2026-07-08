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
package org.openhab.binding.chatgpt.internal.hli;

import static org.openhab.binding.chatgpt.internal.ChatGPTBindingConstants.DEFAULT_SYSTEM_MESSAGE;
import static org.openhab.binding.chatgpt.internal.hli.ChatGPTHLIConstants.SERVICE_ID;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.chatgpt.internal.ChatGPTConfiguration;
import org.openhab.binding.chatgpt.internal.ChatGPTHandler;
import org.openhab.binding.chatgpt.internal.api.ChatGPTApiClient;
import org.openhab.binding.chatgpt.internal.api.ChatGPTApiException;
import org.openhab.binding.chatgpt.internal.api.ChatGPTLLMToolCall;
import org.openhab.binding.chatgpt.internal.api.dto.ChatFunctionCall;
import org.openhab.binding.chatgpt.internal.api.dto.ChatMessage;
import org.openhab.binding.chatgpt.internal.api.dto.ChatResponse;
import org.openhab.binding.chatgpt.internal.api.dto.ChatToolCalls;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.voice.text.HumanLanguageInterpreter;
import org.openhab.core.voice.text.InterpretationException;
import org.openhab.core.voice.text.InterpreterContext;
import org.openhab.core.voice.text.conversation.Conversation;
import org.openhab.core.voice.text.conversation.ConversationException;
import org.openhab.core.voice.text.conversation.ConversationRole;
import org.openhab.core.voice.text.interpreter.llm.LLMTool;
import org.openhab.core.voice.text.interpreter.llm.LLMToolException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link ChatGPTHLIService} is responsible for handling the human language interpretation using ChatGPT.
 *
 * @author Artur Fedjukevits - Initial contribution
 * @author Florian Hotze - Simplified using ChatGPTApiClient & added localized messages
 */
@Component(service = { ChatGPTHLIService.class, HumanLanguageInterpreter.class })
@NonNullByDefault
public class ChatGPTHLIService implements ThingHandlerService, HumanLanguageInterpreter {
    private static final String ERROR_KEY_MISSING_CONFIG = "hli.error.missing-configuration";
    private static final String ERROR_KEY_TECHNICAL_PROBLEM = "hli.error.technical-problem";
    private static final String DEFAULT_ERROR_MISSING_CONFIG = "Cannot interpret due to missing configuration.";
    private static final String DEFAULT_ERROR_TECHNICAL_PROBLEM = "Cannot interpret due to a technical problem.";

    private final Logger logger = LoggerFactory.getLogger(ChatGPTHLIService.class);
    private final TranslationProvider i18nProvider;
    private final Bundle bundle;

    private @Nullable ChatGPTHandler handler;

    @Activate
    public ChatGPTHLIService(final @Reference TranslationProvider i18nProvider, BundleContext context) {
        this.i18nProvider = i18nProvider;
        this.bundle = context.getBundle();
        logger.debug("ChatGPTHLIService activated");
    }

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

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return "ChatGPT Human Language Interpreter";
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

    private String getLocalizedMessage(String key, String defaultText, Locale locale) {
        String message = i18nProvider.getText(bundle, key, defaultText, locale);
        return message != null ? message : defaultText;
    }

    @Override
    public String interpret(Locale locale, String text) throws InterpretationException {
        ChatGPTHandler handler = null;
        if (handler == null) {
            logger.warn("Cannot interpret: ChatGPTHandler is not initialized");
            throw new InterpretationException(
                    getLocalizedMessage(ERROR_KEY_MISSING_CONFIG, DEFAULT_ERROR_MISSING_CONFIG, locale));
        }
        ChatGPTApiClient client = handler.getApiClient();
        ChatGPTConfiguration config = handler.getConfigAs();
        if (client == null || config == null) {
            throw new InterpretationException(
                    getLocalizedMessage(ERROR_KEY_MISSING_CONFIG, DEFAULT_ERROR_MISSING_CONFIG, locale));
        }
        try {
            ChatResponse response = client.sendPrompt(config.model, text, DEFAULT_SYSTEM_MESSAGE, config.temperature,
                    config.topP, config.maxTokens, config.requestTimeout);
            if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                ChatMessage responseMessage = response.getChoices().getFirst().getChatMessage();
                if (responseMessage != null && responseMessage.getContent() != null) {
                    return responseMessage.getContent();
                }
            }
        } catch (ChatGPTApiException e) {
            logger.debug("Request to OpenAI failed: {}", e.getMessage(), e);
            throw new InterpretationException(
                    getLocalizedMessage(ERROR_KEY_TECHNICAL_PROBLEM, DEFAULT_ERROR_TECHNICAL_PROBLEM, locale));
        }
        throw new InterpretationException(
                getLocalizedMessage(ERROR_KEY_TECHNICAL_PROBLEM, DEFAULT_ERROR_TECHNICAL_PROBLEM, locale));
    }

    @Override
    public String interpret(Locale locale, InterpreterContext interpreterContext) throws InterpretationException {
        ChatGPTHandler chatGPTHandler = null;
        if (chatGPTHandler == null) {
            logger.warn("Cannot interpret: ChatGPTHandler is not initialized");
            throw new InterpretationException(
                    getLocalizedMessage(ERROR_KEY_MISSING_CONFIG, DEFAULT_ERROR_MISSING_CONFIG, locale));
        }
        ChatGPTConfiguration config = chatGPTHandler.getConfigAs();
        ChatGPTApiClient client = chatGPTHandler.getApiClient();
        if (config == null || client == null) {
            logger.warn("Cannot interpret: ChatGPT configuration or API client is not available");
            throw new InterpretationException(
                    getLocalizedMessage(ERROR_KEY_MISSING_CONFIG, DEFAULT_ERROR_MISSING_CONFIG, locale));
        }

        Conversation conversation = interpreterContext.conversation();
        List<LLMTool> tools = interpreterContext.tools();

        String systemMessage = interpreterContext.systemPrompt();
        if (systemMessage == null || systemMessage.isBlank()) {
            logger.warn("Cannot interpret: System prompt is missing or empty");
            throw new InterpretationException(
                    getLocalizedMessage(ERROR_KEY_MISSING_CONFIG, DEFAULT_ERROR_MISSING_CONFIG, locale));
        }
        String locationItem = interpreterContext.locationItem();
        if (locationItem != null && !locationItem.isBlank()) {
            systemMessage = systemMessage.trim() + "\n\n" + "Your location (item name): " + locationItem;
        }

        int loopCount = 0;
        int maxLoops = config.maxToolCalls;
        while (true) {
            if (loopCount >= maxLoops) {
                logger.warn("Cannot interpret: Tool execution loop limit exceeded (max {} iterations)", maxLoops);
                throw new InterpretationException(
                        getLocalizedMessage(ERROR_KEY_TECHNICAL_PROBLEM, DEFAULT_ERROR_TECHNICAL_PROBLEM, locale));
            }
            loopCount++;

            ChatResponse chatResponse;
            try {
                chatResponse = client.sendPrompt(config.model, conversation.getMessages(), tools, systemMessage,
                        config.temperature, config.topP, config.maxTokens, config.requestTimeout);
            } catch (ChatGPTApiException e) {
                logger.warn("Request to OpenAI failed: {}", e.getMessage(), e);
                var ex = new InterpretationException(
                        getLocalizedMessage(ERROR_KEY_TECHNICAL_PROBLEM, DEFAULT_ERROR_TECHNICAL_PROBLEM, locale));
                ex.initCause(e);
                throw ex;
            }

            if (chatResponse.getChoices() == null || chatResponse.getChoices().isEmpty()) {
                logger.warn("Cannot interpret: No valid response received");
                throw new InterpretationException(
                        getLocalizedMessage(ERROR_KEY_TECHNICAL_PROBLEM, DEFAULT_ERROR_TECHNICAL_PROBLEM, locale));
            }

            String finishReason = chatResponse.getChoices().get(0).getFinishReason();
            if ("length".equals(finishReason)) {
                logger.warn("Token length exceeded. Increase maximum token limit to avoid the issue.");
                throw new InterpretationException(
                        getLocalizedMessage(ERROR_KEY_TECHNICAL_PROBLEM, DEFAULT_ERROR_TECHNICAL_PROBLEM, locale));
            }

            @Nullable
            ChatMessage chatResponseMessage = chatResponse.getChoices().get(0).getChatMessage();
            if (chatResponseMessage == null) {
                logger.warn("ChatGPT response does not contain a message.");
                throw new InterpretationException(
                        getLocalizedMessage(ERROR_KEY_TECHNICAL_PROBLEM, DEFAULT_ERROR_TECHNICAL_PROBLEM, locale));
            }

            if ("tool_calls".equals(finishReason)) {
                List<ChatToolCalls> toolCalls = chatResponseMessage.getToolCalls();
                if (toolCalls == null || toolCalls.isEmpty()) {
                    logger.warn("Finish reason is tool_calls but no tool calls found");
                    throw new InterpretationException(
                            getLocalizedMessage(ERROR_KEY_TECHNICAL_PROBLEM, DEFAULT_ERROR_TECHNICAL_PROBLEM, locale));
                }

                ObjectMapper objectMapper = new ObjectMapper();
                for (ChatToolCalls toolCall : toolCalls) {
                    if ("function".equals(toolCall.getType())) {
                        ChatFunctionCall fc = toolCall.getFunction();
                        if (fc != null) {
                            String toolName = fc.getName();
                            String arguments = fc.getArguments();
                            Map<String, Object> args = new HashMap<>();
                            if (arguments != null && !arguments.isEmpty()) {
                                try {
                                    args = objectMapper.readValue(arguments, new TypeReference<Map<String, Object>>() {
                                    });
                                } catch (JsonProcessingException e) {
                                    logger.warn("Failed to parse tool call arguments: {}", e.getMessage(), e);
                                }
                            }

                            ChatGPTLLMToolCall llmToolCall = new ChatGPTLLMToolCall(toolName != null ? toolName : "",
                                    args, toolCall.getId());

                            try {
                                conversation.addMessage(ConversationRole.TOOL_CALL, llmToolCall.toJson());
                            } catch (ConversationException e) {
                                logger.warn("Cannot interpret: Failed to add TOOL_CALL to conversation", e);
                                var ex = new InterpretationException(getLocalizedMessage(ERROR_KEY_TECHNICAL_PROBLEM,
                                        DEFAULT_ERROR_TECHNICAL_PROBLEM, locale));
                                ex.initCause(e);
                                throw ex;
                            }

                            String result = executeTool(tools, toolName, args, locale);

                            try {
                                conversation.addMessage(ConversationRole.TOOL_RETURN, result);
                            } catch (ConversationException e) {
                                logger.warn("Cannot interpret: Failed to add TOOL_RETURN to conversation", e);
                                var ex = new InterpretationException(getLocalizedMessage(ERROR_KEY_TECHNICAL_PROBLEM,
                                        DEFAULT_ERROR_TECHNICAL_PROBLEM, locale));
                                ex.initCause(e);
                                throw ex;
                            }
                        }
                    }
                }
            } else {
                String finalResponse = chatResponseMessage.getContent() != null ? chatResponseMessage.getContent() : "";
                try {
                    conversation.addMessage(ConversationRole.OPENHAB, finalResponse);
                } catch (ConversationException e) {
                    logger.warn("Cannot interpret: Failed to add OPENHAB message to conversation", e);
                    var ex = new InterpretationException(
                            getLocalizedMessage(ERROR_KEY_TECHNICAL_PROBLEM, DEFAULT_ERROR_TECHNICAL_PROBLEM, locale));
                    ex.initCause(e);
                    throw ex;
                }
                return finalResponse;
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
