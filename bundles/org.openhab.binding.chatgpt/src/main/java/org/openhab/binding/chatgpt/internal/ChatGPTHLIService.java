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

import static org.openhab.binding.chatgpt.internal.ChatGPTHLIConstants.SERVICE_ID;
import static org.openhab.binding.chatgpt.internal.ChatGPTHLIConstants.SERVICE_PID;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.chatgpt.internal.dto.ChatFunction;
import org.openhab.binding.chatgpt.internal.dto.ChatFunctionCall;
import org.openhab.binding.chatgpt.internal.dto.ChatMessage;
import org.openhab.binding.chatgpt.internal.dto.ChatRequestBody;
import org.openhab.binding.chatgpt.internal.dto.ChatResponse;
import org.openhab.binding.chatgpt.internal.dto.ChatToolCalls;
import org.openhab.binding.chatgpt.internal.dto.ChatTools;
import org.openhab.binding.chatgpt.internal.dto.ToolChoice;
import org.openhab.binding.chatgpt.internal.dto.functions.ItemsControl;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.TypeParser;
import org.openhab.core.voice.text.HumanLanguageInterpreter;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Artur Fedjukevits - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "="
        + SERVICE_PID, service = { ChatGPTHLIService.class, HumanLanguageInterpreter.class })
@ConfigurableService(category = "voice", label = "ChatGPT HLI Service", description_uri = "voice:" + SERVICE_ID)
@NonNullByDefault
public class ChatGPTHLIService implements ThingHandlerService, HumanLanguageInterpreter {

    private @Nullable ThingHandler thingHandler;
    private List<ChatMessage> messages = new ArrayList<>();

    private LocalTime lastMessageTime = LocalTime.now();
    private List<ChatTools> tools = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(ChatGPTHLIService.class);
    private final Map<String, ChatFunction> FUNCTIONS = new HashMap<>();
    private @Nullable ItemRegistry itemRegistry;
    private @Nullable EventPublisher eventPublisher;
    private ChatGPTHLIConfiguration config = new ChatGPTHLIConfiguration();

    @Activate
    public ChatGPTHLIService(@Reference ItemRegistry itemRegistry, @Reference EventPublisher eventPublisher) {
        this.itemRegistry = itemRegistry;
        this.eventPublisher = eventPublisher;

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/json/tools.json");
                InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(reader);

            try {
                this.tools = Arrays.asList(mapper.treeToValue(node, ChatTools[].class));
            } catch (JsonProcessingException e) {
                logger.error("Error processing tools.json", e);
                this.tools = new ArrayList<>();
            }

        } catch (IOException e) {
            logger.error("Error reading tools.json", e);
            this.tools = new ArrayList<>();
        }

        for (ChatTools tool : tools) {
            logger.debug("Loaded tool: {}", tool.getFunction().getName());
        }

        FUNCTIONS.clear();
        FUNCTIONS.putAll(tools.stream().collect(HashMap::new, (map, tool) -> {
            ChatFunction function = tool.getFunction();
            String functionName = function.getName();

            map.put(functionName, function);
        }, HashMap::putAll));

        ChatFunction itemControlFunction = FUNCTIONS.get("items_control");
        if (itemControlFunction != null) {

            itemControlFunction.setParametersClass(ItemsControl.class);

            itemControlFunction.setExecutor(p -> {
                ItemsControl parameters = (ItemsControl) p;

                return sendCommand(parameters.getName(), parameters.getState());
            });
        }

        logger.debug("ChatGPTHLIService activated");
    }

    @Activate
    protected void activate(Map<String, Object> config) {
        this.config = new Configuration(config).as(ChatGPTHLIConfiguration.class);
        logger.debug("ChatGPTHLIService configuration completed");
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            logger.debug("Config: {} -> {}", entry.getKey(), entry.getValue());
        }
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        this.config = new Configuration(config).as(ChatGPTHLIConfiguration.class);
        logger.debug("ChatGPTHLIService configuration modified");
    }

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public Set<String> getSupportedGrammarFormats() {
        return Set.of();
    }

    @Override
    public String interpret(Locale locale, String text) {

        String requestBody = prepareRequestBody(text);
        if (requestBody == null) {
            return "Failed to prepare request body";
        }

        if (thingHandler instanceof ChatGPTHandler chatGPTHandler) {

            String response = chatGPTHandler.sendPrompt(requestBody);
            return processChatResponse(response);

        }

        return "Failed to interpret text";
    }

    @Override
    public Set<Locale> getSupportedLocales() {

        return Set.of();
    }

    @Override
    public String getLabel(@Nullable Locale locale) {

        return "ChatGPT Human Language Interpreter";
    }

    @Override
    @Nullable
    public String getGrammar(Locale locale, String format) {

        return "null";
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.thingHandler = handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return thingHandler;
    }

    @Override
    public void activate() {
    }

    private String processChatResponse(@Nullable String response) {

        if (response == null || response.isEmpty()) {
            return "";
        }

        logger.debug("Received response: {}", response);

        ObjectMapper objectMapper = new ObjectMapper();
        ChatResponse chatResponse;
        try {
            chatResponse = objectMapper.readValue(response, ChatResponse.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse ChatGPT response: {}", e.getMessage(), e);
            return "";
        }

        if (chatResponse == null) {
            logger.warn("Didn't receive any response from ChatGPT - this is unexpected.");
            return "";
        }

        this.lastMessageTime = LocalTime.now();

        if (chatResponse.getUsage().getTotalTokens() > this.config.contextTreshold) {

            Integer lastUserMessageIndex = null;
            for (int i = messages.size() - 1; i >= 0; i--) {
                if (messages.get(i).getRole().equals(ChatMessage.Role.USER.value())) {
                    lastUserMessageIndex = i;
                    break;
                }
            }

            if (lastUserMessageIndex != null) {
                messages.subList(1, lastUserMessageIndex).clear();
                messages.set(0, generateSystemMessage());
            }

        }

        String finishReason = chatResponse.getChoices().get(0).getFinishReason();

        if (finishReason.equals("length")) {
            logger.error("token length exceeded. Increase maximum token to avoid the issue.");
            return "";
        }

        @Nullable
        ChatMessage chatResponseMessage = chatResponse.getChoices().get(0).getChatMessage();

        if (chatResponseMessage == null) {
            logger.error("ChatGPT response does not contain a message.");
            return "";
        }

        this.messages.add(chatResponseMessage);

        if (finishReason.equals("tool_calls")) {
            executeToolCalls(chatResponseMessage.getToolCalls());
            return "";
        } else {
            return (chatResponseMessage.getContent() == null) ? "" : chatResponseMessage.getContent();
        }
    }

    private void executeToolCalls(@Nullable List<ChatToolCalls> toolCalls) {
        toolCalls.forEach(tool -> {
            if (tool.getType().equals("function")) {
                ChatFunctionCall functionCall = tool.getFunction();
                if (functionCall != null) {

                    String functionName = functionCall.getName();
                    ChatFunction function = FUNCTIONS.get(functionName);
                    if (function != null) {

                        ObjectMapper objectMapper = new ObjectMapper();
                        String arguments = functionCall.getArguments();
                        Object argumentsObject;

                        logger.debug("Function '{}' with arguments: {}", functionName, arguments);

                        JsonNode argumentsNode;
                        try {
                            argumentsNode = objectMapper.readTree(arguments);
                            Class<?> parametersClass = function.getParametersClass();
                            argumentsObject = objectMapper.treeToValue(argumentsNode, parametersClass);
                        } catch (JsonProcessingException e) {
                            logger.error("Failed to parse arguments: {}", e.getMessage(), e);
                            return;
                        }

                        Object result = function.getExecutor().apply(argumentsObject);
                        String resultString = String.valueOf(result);

                        ChatMessage message = new ChatMessage();
                        message.setRole(ChatMessage.Role.TOOL.value());
                        message.setName(functionName);
                        message.setToolCallId(tool.getId());
                        message.setContent(resultString);
                        messages.add(message);

                    } else {
                        logger.error("Function '{}' not found", functionName);
                    }
                }
            }
        });
    }

    private @Nullable String prepareRequestBody(String message) {

        LocalTime currentTime = LocalTime.now();

        if (currentTime.isAfter(this.lastMessageTime.plusMinutes(config.keepContext))) {
            this.messages.clear();
        }
        if (this.messages.isEmpty()) {
            ChatMessage systemMessage = generateSystemMessage();
            this.messages.add(systemMessage);
        }

        this.lastMessageTime = currentTime;

        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole(ChatMessage.Role.USER.value());
        userMessage.setContent(message);
        this.messages.add(userMessage);

        ChatRequestBody chatRequestBody = new ChatRequestBody();

        if (this.config.chatGPTModel == null || this.config.chatGPTModel.isEmpty()) {
            logger.error("Model is not set");
            return null;
        }

        chatRequestBody.setModel(this.config.chatGPTModel);
        chatRequestBody.setTemperature(this.config.temperature);
        chatRequestBody.setMaxTokens(this.config.maxTokens);
        chatRequestBody.setTopP(this.config.topP);

        chatRequestBody.setToolChoice(ToolChoice.AUTO.value());
        chatRequestBody.setTools(this.tools);

        chatRequestBody.setMessages(this.messages);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);

        try {
            return objectMapper.writeValueAsString(chatRequestBody);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize ChatGPT request: {}", e.getMessage(), e);
            return null;
        }
    }

    private ChatMessage generateSystemMessage() {

        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setRole(ChatMessage.Role.SYSTEM.value());
        StringBuilder content = new StringBuilder();
        content.append(this.config.systemMessage);

        Collection<Item> openaiItems = itemRegistry.getItemsByTag("OpenAI");

        if (!openaiItems.isEmpty()) {

            openaiItems.forEach(item -> {
                String[] nameParts = item.getName().split("_");
                content.append("name: \"").append(item.getName()).append("\", location: \"").append(nameParts[0])
                        .append("\", description: \"").append(item.getLabel()).append("\", state: \"")
                        .append(item.getState().toString()).append("\", type: \"").append(item.getType().toString())
                        .append("\"").append(System.lineSeparator());
            });
        }

        systemMessage.setContent(content.toString());
        return systemMessage;
    }

    public String sendCommand(String itemName, String commandString) {
        try {
            Item item = itemRegistry.getItem(itemName);
            Command command = null;
            if ("toggle".equalsIgnoreCase(commandString)
                    && (item instanceof SwitchItem || item instanceof RollershutterItem)) {
                if (OnOffType.ON.equals(item.getStateAs(OnOffType.class))) {
                    command = OnOffType.OFF;
                }
                if (OnOffType.OFF.equals(item.getStateAs(OnOffType.class))) {
                    command = OnOffType.ON;
                }
                if (UpDownType.UP.equals(item.getStateAs(UpDownType.class))) {
                    command = UpDownType.DOWN;
                }
                if (UpDownType.DOWN.equals(item.getStateAs(UpDownType.class))) {
                    command = UpDownType.UP;
                }
            } else {
                command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), commandString);
            }
            if (command != null) {
                logger.debug("Received command '{}' for item '{}'", commandString, itemName);
                eventPublisher.post(ItemEventFactory.createCommandEvent(itemName, command));

                return "Done";

            } else {

                return "Invalid command";

            }
        } catch (ItemNotFoundException e) {
            logger.warn("Received command '{}' for a non-existent item '{}'", commandString, itemName);

            return "Item not found";
        }
    }
}
