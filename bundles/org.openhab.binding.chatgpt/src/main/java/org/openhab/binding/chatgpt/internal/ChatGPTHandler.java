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

import static org.openhab.binding.chatgpt.internal.ChatGPTBindingConstants.MAX_CONTEXT_TOKENS;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.openhab.binding.chatgpt.internal.dto.ChatFunction;
import org.openhab.binding.chatgpt.internal.dto.ChatFunctionCall;
import org.openhab.binding.chatgpt.internal.dto.ChatMessage;
import org.openhab.binding.chatgpt.internal.dto.ChatRequestBody;
import org.openhab.binding.chatgpt.internal.dto.ChatResponse;
import org.openhab.binding.chatgpt.internal.dto.ChatToolCalls;
import org.openhab.binding.chatgpt.internal.dto.ChatTools;
import org.openhab.binding.chatgpt.internal.dto.ToolChoice;
import org.openhab.binding.chatgpt.internal.dto.functions.ItemsControl;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link ChatGPTHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Artur Fedjukevits - upgraded the handler
 */
@NonNullByDefault
public class ChatGPTHandler extends BaseThingHandler {

    private static final int REQUEST_TIMEOUT_MS = 10_000;
    private final Logger logger = LoggerFactory.getLogger(ChatGPTHandler.class);

    private HttpClient httpClient;

    private String apiKey = "";
    private String apiUrl = "";
    private String modelUrl = "";

    private String lastPrompt = "";
    private List<ChatMessage> messages = new ArrayList<>();

    private LocalTime lastMessageTime = LocalTime.now();

    private List<String> models = List.of();

    private final Map<String, ChatFunction> FUNCTIONS = new HashMap<>();

    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;
    private List<ChatTools> tools;

    public ChatGPTHandler(Thing thing, HttpClientFactory httpClientFactory, ItemRegistry itemRegistry,
            EventPublisher eventPublisher, List<ChatTools> tools) {
        super(thing);
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.itemRegistry = itemRegistry;
        this.eventPublisher = eventPublisher;
        this.tools = tools;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof StringType stringCommand) {
            lastPrompt = stringCommand.toFullString();

            String response = sendPrompt(channelUID);
            processChatResponse(channelUID, response);
        }
    }

    private void processChatResponse(ChannelUID channelUID, @Nullable String response) {
        if (response != null) {

            logger.info("Received response: {}", response);

            ObjectMapper objectMapper = new ObjectMapper();
            ChatResponse chatResponse;
            try {
                chatResponse = objectMapper.readValue(response, ChatResponse.class);
            } catch (JsonProcessingException e) {
                logger.error("Failed to parse ChatGPT response: {}", e.getMessage(), e);
                return;
            }

            if (chatResponse != null) {

                this.lastMessageTime = LocalTime.now();

                if (chatResponse.getUsage().getTotalTokens() > MAX_CONTEXT_TOKENS) {

                    Channel channel = getThing().getChannel(channelUID);
                    if (channel == null) {
                        logger.error("Channel with UID '{}' cannot be found on Thing '{}'.", channelUID,
                                getThing().getUID());
                        return;
                    }

                    ChatGPTChannelConfiguration channelConfig = channel.getConfiguration()
                            .as(ChatGPTChannelConfiguration.class);

                    Integer lastUserMessageIndex = null;
                    for (int i = messages.size() - 1; i >= 0; i--) {
                        if (messages.get(i).getRole().equals(ChatMessage.Role.USER.value())) {
                            lastUserMessageIndex = i;
                            break;
                        }
                    }

                    if (lastUserMessageIndex != null) {
                        messages.subList(1, lastUserMessageIndex).clear();
                        messages.set(0, generateSystemMessage(channelConfig.systemMessage, true));
                    }

                }

                String finishReason = chatResponse.getChoices().get(0).getFinishReason();

                if (finishReason.equals("length")) {
                    logger.error("token length exceeded. Increase maximum token to avoid the issue.");
                    return;
                }

                @Nullable
                ChatMessage chatResponseMessage = chatResponse.getChoices().get(0).getChatMessage();

                if (chatResponseMessage == null) {
                    logger.error("ChatGPT response does not contain a message.");
                    return;
                }

                if (finishReason.equals("tool_calls")) {
                    executeToolCalls(channelUID, chatResponseMessage.getToolCalls());
                }

                @Nullable
                String msg = chatResponseMessage.getContent();
                if (msg != null) {
                    this.messages.add(chatResponseMessage);
                    updateState(channelUID, new StringType(msg));
                }

            } else {
                logger.warn("Didn't receive any response from ChatGPT - this is unexpected.");
            }
        }
    }

    private void executeToolCalls(ChannelUID channelUID, @Nullable List<ChatToolCalls> toolCalls) {
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

                        if (!resultString.equals("Done")) {
                            updateState(channelUID, new StringType(resultString));
                        }

                    } else {
                        logger.error("Function '{}' not found", functionName);
                    }
                }
            }
        });
    }

    private @Nullable String sendPrompt(ChannelUID channelUID) {
        Channel channel = getThing().getChannel(channelUID);
        if (channel == null) {
            logger.error("Channel with UID '{}' cannot be found on Thing '{}'.", channelUID, getThing().getUID());
            return null;
        }

        ChatGPTChannelConfiguration channelConfig = channel.getConfiguration().as(ChatGPTChannelConfiguration.class);

        LocalTime currentTime = LocalTime.now();
        Boolean isChat = channelConfig.type.equals("chat");

        if (!isChat || currentTime.isAfter(this.lastMessageTime.plusMinutes(2))) {
            this.messages.clear();
        }
        if (this.messages.isEmpty()) {
            ChatMessage systemMessage = generateSystemMessage(channelConfig.systemMessage, isChat);
            this.messages.add(systemMessage);
        }

        this.lastMessageTime = currentTime;

        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole(ChatMessage.Role.USER.value());
        userMessage.setContent(lastPrompt);
        this.messages.add(userMessage);

        ChatRequestBody chatRequestBody = new ChatRequestBody();

        chatRequestBody.setModel(channelConfig.model);
        chatRequestBody.setTemperature(channelConfig.temperature);
        chatRequestBody.setMaxTokens(channelConfig.maxTokens);
        chatRequestBody.setUser("artur");
        chatRequestBody.setTopP(channelConfig.topP);

        if (isChat) {
            chatRequestBody.setToolChoice(ToolChoice.AUTO.value());
            chatRequestBody.setTools(this.tools);
        }

        chatRequestBody.setMessages(this.messages);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);

        String queryJson;
        try {
            queryJson = objectMapper.writeValueAsString(chatRequestBody);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize ChatGPT request: {}", e.getMessage(), e);
            return null;
        }

        Request request = httpClient.newRequest(apiUrl).method(HttpMethod.POST)
                .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey).content(new StringContentProvider(queryJson));
        logger.debug("Query '{}'", queryJson);
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

    private ChatMessage generateSystemMessage(String message, Boolean isChat) {

        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setRole(ChatMessage.Role.SYSTEM.value());
        StringBuilder content = new StringBuilder();
        content.append(message);

        if (isChat) {

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
        }

        systemMessage.setContent(content.toString());
        return systemMessage;
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
        this.messages.clear();

        updateStatus(ThingStatus.UNKNOWN);

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

        scheduler.execute(() -> {
            try {
                Request request = httpClient.newRequest(modelUrl).timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        .method(HttpMethod.GET).header("Authorization", "Bearer " + apiKey);
                ContentResponse response = request.send();
                if (response.getStatus() == 200) {
                    updateStatus(ThingStatus.ONLINE);
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        JsonNode models = objectMapper.readTree(response.getContentAsString());
                        JsonNode data = models.get("data");

                        if (data != null) {
                            logger.debug("Models: {}", data.toString());

                            List<String> modelList = new ArrayList<>();

                            data.forEach(model -> {
                                JsonNode id = model.get("id");

                                if (id != null) {
                                    modelList.add(id.asText());
                                }

                            });

                            this.models = List.copyOf(modelList);

                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "@text/offline.communication-error");
                        }

                    } catch (JsonProcessingException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        logger.error("Failed to parse models: {}", e.getMessage(), e);
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

    List<String> getModels() {
        return models;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(ChatGPTModelOptionProvider.class);
    }
}
