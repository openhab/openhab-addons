/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.openhab.binding.chatgpt.internal.hli.ChatGPTHLIConstants.SERVICE_ID;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.chatgpt.internal.ChatGPTConfiguration;
import org.openhab.binding.chatgpt.internal.ChatGPTHandler;
import org.openhab.binding.chatgpt.internal.dto.ChatFunction;
import org.openhab.binding.chatgpt.internal.dto.ChatFunctionCall;
import org.openhab.binding.chatgpt.internal.dto.ChatMessage;
import org.openhab.binding.chatgpt.internal.dto.ChatRequestBody;
import org.openhab.binding.chatgpt.internal.dto.ChatResponse;
import org.openhab.binding.chatgpt.internal.dto.ChatToolCalls;
import org.openhab.binding.chatgpt.internal.dto.ChatTools;
import org.openhab.binding.chatgpt.internal.dto.ToolChoice;
import org.openhab.binding.chatgpt.internal.dto.functions.CreateIntent;
import org.openhab.binding.chatgpt.internal.dto.functions.ItemsControl;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.model.script.actions.Semantics;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandDescription;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.TypeParser;
import org.openhab.core.voice.text.HumanLanguageInterpreter;
import org.openhab.ui.habot.card.Card;
import org.openhab.ui.habot.card.CardBuilder;
import org.openhab.ui.habot.nlp.ChatReply;
import org.openhab.ui.habot.nlp.Intent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link ChatGPTHLIService} is responsible for handling the human language interpretation using ChatGPT.
 * 
 * @author Artur Fedjukevits - Initial contribution
 */
@Component(service = { ChatGPTHLIService.class, HumanLanguageInterpreter.class })
@NonNullByDefault
public class ChatGPTHLIService implements ThingHandlerService, HumanLanguageInterpreter {

    private @Nullable ThingHandler thingHandler;
    private List<ChatMessage> messages = new ArrayList<>();

    private LocalTime lastMessageTime = LocalTime.now();
    private List<ChatTools> tools = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(ChatGPTHLIService.class);
    private final Map<String, ChatFunction> functions = new HashMap<>();
    private @Nullable ItemRegistry itemRegistry;
    private @Nullable EventPublisher eventPublisher;
    private @Nullable ChatGPTConfiguration config;
    private @Nullable Intent lastCreatedIntent;
    private @Nullable CardBuilder cardBuilder;
    private @Nullable Collection<Item> matchedItemsCache;

    @Activate
    public ChatGPTHLIService(@Reference ItemRegistry itemRegistry, @Reference EventPublisher eventPublisher,
            final @Reference CardBuilder cardBuilder) {
        this.itemRegistry = itemRegistry;
        this.eventPublisher = eventPublisher;
        this.cardBuilder = cardBuilder;

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/json/tools.json");
                InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(reader);

            try {
                this.tools = Arrays.asList(mapper.treeToValue(node, ChatTools[].class));
            } catch (JsonProcessingException e) {
                logger.debug("Error processing tools.json", e);
            }
        } catch (IOException e) {
            logger.error("Error reading tools.json", e);
        }

        for (ChatTools tool : tools) {
            logger.debug("Loaded tool: {}", tool.getFunction().getName());
        }

        functions.clear();
        functions.putAll(tools.stream().collect(HashMap::new, (map, tool) -> {
            ChatFunction function = tool.getFunction();
            String functionName = function.getName();

            map.put(functionName, function);
        }, HashMap::putAll));

        ChatFunction itemControlFunction = functions.get("items_control");
        if (itemControlFunction != null) {
            itemControlFunction.setParametersClass(ItemsControl.class);

            itemControlFunction.setExecutor(p -> {
                ItemsControl parameters = (ItemsControl) p;
                return sendCommand(parameters.getName(), parameters.getState(), parameters.getAnswer());
            });
        }
        ChatFunction createIntentFunction = functions.get("create_intent");
        if (createIntentFunction != null) {
            createIntentFunction.setParametersClass(CreateIntent.class);
            createIntentFunction.setExecutor(p -> {
                CreateIntent params = (CreateIntent) p;
                return createIntentFromParams(params);
            });
        }
        logger.debug("ChatGPTHLIService activated");
    }

    private String createIntentFromParams(CreateIntent params) {

        Intent intent = new Intent(params.getName());

        if (params.getEntities() != null) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = mapper.convertValue(params.getEntities(),
                    new TypeReference<Map<String, String>>() {
                    });
            intent.setEntities(map);
        }

        this.lastCreatedIntent = intent;

        if (params.getMatchedItems() != null && !params.getMatchedItems().isEmpty()) {
            Collection<Item> matchedItems = params.getMatchedItems().stream().flatMap(name -> {
                try {
                    return Stream.of(itemRegistry.getItem(name));
                } catch (ItemNotFoundException e) {
                    logger.warn("Item '{}' from OpenAI not found in registry", name);
                    return Stream.empty();
                }
            }).collect(Collectors.toList());

            if (!matchedItems.isEmpty()) {
                this.matchedItemsCache = matchedItems;
                logger.debug("Matched items from OpenAI: {}", matchedItems);
            } else {
                logger.warn("No valid items resolved from OpenAI intent: {}", params.getMatchedItems());
            }
        }

        logger.debug("Intent created: {} with entities {} and items {}", params.getName(), params.getEntities(),
                params.getMatchedItems());

        return params.getAnswer();
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

    public ChatReply reply(Locale locale, String text) {

        ChatReply reply = new ChatReply(locale, text);

        this.lastCreatedIntent = null;
        this.matchedItemsCache = null;

        String requestBody = prepareRequestBody(text, true);
        if (requestBody == null) {
            reply.setAnswer("Failed to prepare request body");
            return reply;
        }

        if (thingHandler instanceof ChatGPTHandler chatGPTHandler) {
            String response = chatGPTHandler.sendPrompt(requestBody);
            String answer = processChatResponse(response);

            if (!answer.isEmpty()) {
                reply.setAnswer(answer);
            }

            if (this.lastCreatedIntent != null) {
                Intent intent = this.lastCreatedIntent;
                reply.setIntent(intent);

                if (this.matchedItemsCache != null) {
                    Collection<Item> matchedItems = this.matchedItemsCache;

                    String[] itemStrings = matchedItems.stream().map(Item::getName).toArray(String[]::new);
                    reply.setMatchedItems(itemStrings);

                    Card card;
                    if ("show-chart".equalsIgnoreCase(intent.getName())) {
                        String period = intent.getEntities().getOrDefault("period", "D");
                        card = cardBuilder.buildChartCard(intent, matchedItems, period);
                    } else {
                        card = cardBuilder.buildCard(intent, matchedItems);
                    }
                    reply.setCard(card);
                }
            }

        } else {
            reply.setAnswer("Failed to interpret text");
        }

        return reply;
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
        logger.info("ChatGPTHLIService bound to ThingHandler: {}", handler);
        this.thingHandler = handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return thingHandler;
    }

    @Override
    public void activate() {
    }

    @Override
    public void initialize() {
        if (thingHandler instanceof ChatGPTHandler chatGPTHandler) {
            this.config = chatGPTHandler.getConfigAs();
            logger.info("ChatGPT configuration initialized: {}", config);
        } else {
            logger.warn("ThingHandler not ChatGPTHandler — unable to initialize config");
        }
    }

    private String processChatResponse(@Nullable String response) {
        if (response == null || response.isEmpty()) {
            return "";
        }

        if (this.config == null) {
            logger.warn("ChatGPT configuration is still null");
            return "";
        }

        logger.trace("Received response: {}", response);

        ObjectMapper objectMapper = new ObjectMapper();
        ChatResponse chatResponse;
        try {
            chatResponse = objectMapper.readValue(response, ChatResponse.class);
        } catch (JsonProcessingException e) {
            logger.debug("Failed to parse ChatGPT response: {}", e.getMessage(), e);
            return "";
        }

        this.lastMessageTime = LocalTime.now();

        if (chatResponse.getUsage().getTotalTokens() > this.config.contextThreshold) {
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

        if ("length".equals(finishReason)) {
            logger.warn("Token length exceeded. Increase the maximum token limit to avoid the issue.");
            return "";
        }

        @Nullable
        ChatMessage chatResponseMessage = chatResponse.getChoices().get(0).getChatMessage();

        if (chatResponseMessage == null) {
            logger.debug("ChatGPT response does not contain a message.");
            return "";
        }

        this.messages.add(chatResponseMessage);

        if ("tool_calls".equals(finishReason)) {
            return executeToolCalls(chatResponseMessage.getToolCalls());
        }

        return (chatResponseMessage.getContent() == null) ? "" : chatResponseMessage.getContent();
    }

    private String executeToolCalls(List<ChatToolCalls> toolCalls) {

        StringBuilder combinedAnswer = new StringBuilder();

        toolCalls.forEach(tool -> {
            if (tool.getType().equals("function")) {
                ChatFunctionCall functionCall = tool.getFunction();
                if (functionCall != null) {
                    String functionName = functionCall.getName();
                    ChatFunction function = functions.get(functionName);
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
                            logger.debug("Failed to parse arguments: {}", e.getMessage(), e);
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

                        if (!resultString.isEmpty()) {
                            combinedAnswer.append(resultString).append(" ");
                        }
                    } else {
                        logger.debug("Function '{}' not found", functionName);
                    }
                }
            }
        });

        return combinedAnswer.toString().trim();
    }

    private @Nullable String prepareRequestBody(String message) {
        return prepareRequestBody(message, false);
    }

    private @Nullable String prepareRequestBody(String message, boolean habot) {

        if (this.config == null) {
            if (thingHandler == null) {
                logger.error("ThingHandler is null in ChatGPTHLIService, cannot get configuration!");
                return null;
            } else if (thingHandler instanceof ChatGPTHandler chatGPTHandler) {
                this.config = chatGPTHandler.getConfigAs();
                logger.debug("Loaded ChatGPT config: {}", this.config);
            } else {
                logger.error("ThingHandler is not ChatGPTHandler: {}", thingHandler.getClass());
            }
        }
        if (this.config == null) {
            logger.error("ChatGPT configuration is still null, aborting request body preparation.");
            return null;
        }

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

        if (habot) {
            ChatMessage habotSystemMessage = new ChatMessage();
            habotSystemMessage.setRole(ChatMessage.Role.SYSTEM.value());
            habotSystemMessage.setContent("You are operating in HABot chat mode.\n"
                    + "- If the user asks to display or query information about items, or to show charts, use the `create_intent` function.\n"
                    + "- If the user only wants to control an item (e.g. turn on/off, set dimmer value, send command), use the `items_control` function instead.\n"
                    + "Do not mix them: `create_intent` is only for displaying cards, `items_control` is for commands.");
            this.messages.add(habotSystemMessage);
        }

        ChatRequestBody chatRequestBody = new ChatRequestBody();
        chatRequestBody.setModel(this.config.model);
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
            logger.debug("Failed to serialize ChatGPT request: {}", e.getMessage(), e);
            return null;
        }
    }

    private ChatMessage generateSystemMessage() {
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setRole(ChatMessage.Role.SYSTEM.value());
        StringBuilder content = new StringBuilder();
        content.append(this.config.systemMessage);

        Collection<Item> openaiItems = itemRegistry.getItemsByTag("ChatGPT");

        if (!openaiItems.isEmpty()) {
            openaiItems.forEach(item -> {
                String location = "";
                String itemType = item.getType();
                CommandDescription description = item.getCommandDescription();
                List<CommandOption> options = new ArrayList<>();

                if (description != null) {
                    options = description.getCommandOptions();
                }

                content.append("name: \"").append(item.getName()).append("\", type: \"").append(itemType);

                if (config.useSemanticModel) {
                    Item locationItem = Semantics.getLocation(item);
                    if (locationItem != null) {
                        location = locationItem.getName();
                    }
                } else {
                    String[] nameParts = item.getName().split("_");
                    location = nameParts[0];
                }

                if (!location.isEmpty()) {
                    content.append("\", location: \"").append(location);
                }

                if (!options.isEmpty()) {
                    content.append("\", accepted commands: \"");
                    options.forEach(option -> {
                        content.append(option.getCommand()).append(", ");
                    });
                    content.delete(content.length() - 2, content.length());
                    content.append("\"").append(System.lineSeparator());
                }

                content.append("\", description: \"").append(item.getLabel()).append("\", state: \"")
                        .append(item.getState().toString()).append("\"").append(System.lineSeparator());
            });
        }

        systemMessage.setContent(content.toString());
        return systemMessage;
    }

    public String sendCommand(String itemName, String commandString, String answer) {
        try {
            Item item = itemRegistry.getItem(itemName);
            Command command = null;
            if ("toggle".equalsIgnoreCase(commandString)
                    && (item instanceof SwitchItem || item instanceof RollershutterItem)) {
                if (OnOffType.ON.equals(item.getStateAs(OnOffType.class))) {
                    command = OnOffType.OFF;
                } else if (OnOffType.OFF.equals(item.getStateAs(OnOffType.class))) {
                    command = OnOffType.ON;
                } else if (UpDownType.UP.equals(item.getStateAs(UpDownType.class))) {
                    command = UpDownType.DOWN;
                } else if (UpDownType.DOWN.equals(item.getStateAs(UpDownType.class))) {
                    command = UpDownType.UP;
                }
            } else {
                command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), commandString);
            }
            if (command != null) {
                logger.debug("Received command '{}' for item '{}'", commandString, itemName);
                eventPublisher.post(ItemEventFactory.createCommandEvent(itemName, command));
                return answer;
            } else {
                return "Invalid command";
            }
        } catch (ItemNotFoundException e) {
            logger.warn("Received command '{}' for a non-existent item '{}'", commandString, itemName);
            return "Item not found";
        }
    }
}
