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
package org.openhab.binding.evcc.internal.handler;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link EvccBaseThingHandler} is responsible for building a base class with common methods for the thing handlers
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public abstract class EvccBaseThingHandler extends BaseThingHandler implements EvccJsonAwareHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccBaseThingHandler.class);
    private final ChannelTypeRegistry channelTypeRegistry;
    protected @Nullable EvccBridgeHandler bridgeHandler;
    protected boolean isInitialized = false;
    protected String endpoint = "";

    public EvccBaseThingHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.channelTypeRegistry = channelTypeRegistry;
    }

    protected void commonInitialize(JsonObject state) {
        ThingBuilder builder = editThing();

        for (Map.Entry<@Nullable String, @Nullable JsonElement> entry : state.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (null == key || value == null) {
                continue;
            }
            String thingKey = getThingKey(key);

            // Skip non-primitive values
            if (!value.isJsonPrimitive()) {
                continue;
            }

            ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, thingKey);
            String itemType = getItemType(channelTypeUID);

            if (!"Unknown".equals(itemType)) {
                Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), thingKey))
                        .withType(channelTypeUID).withAcceptedItemType(itemType).build();
                if (!getThing().getChannels().stream().anyMatch(c -> c.getUID().equals(channel.getUID()))) {
                    builder.withChannel(channel);
                }
            } else {
                logUnknownChannelXml(thingKey, "Hint for type: " + value.toString());
            }
        }

        updateThing(builder.build());
        updateStatus(ThingStatus.ONLINE);
        isInitialized = true;
        if (null != bridgeHandler) {
            bridgeHandler.register(this);
        } else {
            logger.error("No bridgeHandler present when initializing the thing");
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        if (getBridge() instanceof Bridge bridge && bridge.getHandler() instanceof EvccBridgeHandler handler) {
            bridgeHandler = handler;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    @Override
    public void dispose() {
        if (bridgeHandler != null) {
            bridgeHandler.unregister(this);
        }
        isInitialized = false;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String key = Utils.getKeyFromChannelUID(channelUID);
            if (bridgeHandler != null) {
                JsonObject state = getStatefromCachedState(bridgeHandler.getCachedEvccState());
                if (!state.isEmpty()) {
                    JsonElement value = state.get(key);
                    String itemType = getItemType(new ChannelTypeUID(BINDING_ID, channelUID.getId()));
                    setItemValue(itemType, channelUID, value);
                }
            }
        }
    }

    protected String getItemType(ChannelTypeUID channelTypeUID) {
        ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID);
        if (null != channelType) {
            String itemType = channelType.getItemType();
            if (null != itemType) {
                return itemType;
            }
        }
        return "Unknown";
    }

    private void setItemValue(String itemType, ChannelUID channelUID, JsonElement value) {
        if (value.isJsonNull()) {
            return;
        }
        switch (itemType) {
            case CoreItemFactory.NUMBER:
            case NUMBER_CURRENCY:
            case NUMBER_DIMENSIONLESS:
            case NUMBER_ENERGY_PRICE:
            case NUMBER_ELECTRIC_CURRENT:
            case NUMBER_EMISSION_INTENSITY:
            case NUMBER_ENERGY:
            case NUMBER_POWER:
            case NUMBER_TIME:
                updateState(channelUID, new DecimalType(value.getAsDouble()));
                break;
            case NUMBER_LENGTH:
                updateState(channelUID, new QuantityType<>(value.getAsDouble(), MetricPrefix.KILO(SIUnits.METRE)));
                break;
            case CoreItemFactory.STRING:
                updateState(channelUID, new StringType(value.getAsString()));
                break;
            case CoreItemFactory.SWITCH:
                updateState(channelUID, value.getAsBoolean() ? OnOffType.ON : OnOffType.OFF);
                break;
            default:
                logUnknownChannelXml(channelUID.getId(), "Hint for type: " + value.toString());
        }
    }

    protected String getThingKey(String key) {
        Map<String, String> props = getThing().getProperties();
        return (props.get("type") + "-" + Utils.sanatizeChannelID(key));
    }

    @Override
    public void updateFromEvccState(JsonObject state) {
        if (!isInitialized) {
            return;
        }

        for (Map.Entry<@Nullable String, @Nullable JsonElement> entry : state.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (null == key || value == null) {
                continue;
            }
            String thingKey = getThingKey(key);

            if (!value.isJsonPrimitive()) {
                continue;
            }

            ChannelUID channelUID = channelUID(thingKey);
            Channel existingChannel = getThing().getChannel(channelUID.getId());
            if (existingChannel == null) {
                logUnknownChannelXml(channelUID.getId(), "Hint for type: " + value.toString());
                continue;
            }

            setItemValue(getItemType(new ChannelTypeUID(BINDING_ID, channelUID.getId())), channelUID, value);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    protected void sendCommand(String url) {
        if (bridgeHandler != null) {
            HttpClient httpClient = bridgeHandler.getHttpClient();
            try {
                ContentResponse response = httpClient.newRequest(url).timeout(5, TimeUnit.SECONDS)
                        .method(HttpMethod.POST).header(HttpHeader.ACCEPT, "application/json").send();

                if (response.getStatus() != 200) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    logger.warn("evcc API error: HTTP {}", response.getStatus());
                }
            } catch (Exception e) {
                logger.warn("evcc bridge couldn't call the API", e);
            }
        }
    }

    private ChannelUID channelUID(String id) {
        return new ChannelUID(getThing().getUID(), id);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo statusInfo) {
        switch (statusInfo.getStatus()) {
            case OFFLINE:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                break;
            case UNINITIALIZED:
                updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED);
                break;
            case ONLINE:
                if (!isInitialized) {
                    Bridge bridge = getBridge();
                    if (bridge == null) {
                        break;
                    }
                    logger.debug("Bridge {} is ONLINE again, initialize evcc {} again...", bridge.getUID(),
                            getThing().getUID().getId());
                    initialize();
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }
                break;
            default:
                break;
        }
    }

    protected void logUnknownChannelXml(String key, String itemType) {
        String xmlSnippet = String.format(
                "<channel-type id=\"%s\">\n" + "    <item-type>%s</item-type>\n" + "    <label>%s</label>\n"
                        + "    <description>Autogenerated placeholder</description>\n" + "</channel-type>\n",
                key, itemType, Utils.capitalizeWords(key));
        logger.trace("Unbekannter Channel erkannt eventuell fehlt die Definition im {}.xml:\n{}",
                getThing().getProperties().get("type"), xmlSnippet);

        Path filePath = Paths.get(System.getProperty("user.dir"), "evcc", "unknown-channels.xml");

        try {
            // Ensure parent directory exists
            if (!Files.exists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }

            // Check if file exists and contains the ID
            if (Files.exists(filePath)) {
                String content = Files.readString(filePath, StandardCharsets.UTF_8);
                String idPattern = String.format("id=\"%s\"", key);
                if (content.contains(idPattern)) {
                    logger.trace("Channel ID '{}' already exists in file.", key);
                    return;
                }
            }

            // Append the snippet if it's not already present
            Files.write(filePath, xmlSnippet.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
            logger.debug("Unknown channel definition written to file: {}", filePath.toAbsolutePath());

        } catch (IOException e) {
            logger.error("Failed to write unknown channel definition to file", e);
        }
    }
}
