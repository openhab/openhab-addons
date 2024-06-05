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

import static org.openhab.binding.chatgpt.internal.ChatGPTBindingConstants.THING_TYPE_ACCOUNT;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.chatgpt.internal.dto.ChatTools;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link ChatGPTHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.chatgpt", service = ThingHandlerFactory.class)
public class ChatGPTHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT);
    private HttpClientFactory httpClientFactory;
    protected final ItemRegistry itemRegistry;
    protected final EventPublisher eventPublisher;
    private List<ChatTools> tools;
    private final Logger logger = LoggerFactory.getLogger(ChatGPTHandlerFactory.class);

    @Activate
    public ChatGPTHandlerFactory(@Reference HttpClientFactory httpClientFactory, @Reference ItemRegistry itemRegistry,
            @Reference EventPublisher eventPublisher) {
        this.httpClientFactory = httpClientFactory;
        this.itemRegistry = itemRegistry;
        this.eventPublisher = eventPublisher;

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/json/tools.json");
                InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(reader);

            logger.info("Node: {}", mapper.writeValueAsString(node));

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
            logger.info("Loaded tool: {}", tool.getFunction().getName());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            return new ChatGPTHandler(thing, httpClientFactory, itemRegistry, eventPublisher, tools);
        }

        return null;
    }
}
