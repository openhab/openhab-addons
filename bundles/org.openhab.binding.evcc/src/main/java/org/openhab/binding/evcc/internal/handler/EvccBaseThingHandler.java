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

import static org.openhab.binding.evcc.internal.EvccBindingConstants.BINDING_ID;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link EvccBaseThingHandler} is responsible for creating the bridge and thing
 * handlers.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public abstract class EvccBaseThingHandler extends BaseThingHandler implements EvccJsonAwareHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccBaseThingHandler.class);
    private final Gson gson = new Gson();
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
            if ("Unknown".equals(itemType)) {
                logUnknownChannelXml(thingKey, value.getClass().toString());
                continue;
            }

            Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), thingKey))
                    .withType(channelTypeUID).withAcceptedItemType(itemType).build();

            ChannelUID channelUID = channel.getUID();

            setItemValue(itemType, channelUID, value);

            if (!getThing().getChannels().stream().anyMatch(c -> c.getUID().equals(channel.getUID()))) {
                builder.withChannel(channel);
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
        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }
        bridgeHandler = bridge.getHandler() instanceof EvccBridgeHandler ? (EvccBridgeHandler) bridge.getHandler()
                : null;
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }
    }

    @Override
    public void dispose() {
        if (bridgeHandler != null) {
            bridgeHandler.unregister(this);
        }
        isInitialized = false;
    }

    private String getItemType(ChannelTypeUID channelTypeUID) {
        ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID);
        if (null != channelType) {
            String itemType = channelType.getItemType();
            if (null != itemType) {
                return itemType;
            }
        }
        return "Unknown";
    }

    private boolean setItemValue(String itemType, ChannelUID channelUID, JsonElement value) {
        switch (itemType) {
            case "Number:Energy":
            case "Number:Power":
            case "Number:Dimensionless":
            case "Number:ElectricCurrent":
            case "Number", "Number:Currency", "Number:Time":
                updateState(channelUID, new DecimalType(value.getAsDouble()));
                break;
            case "Number:Length":
                updateState(channelUID, new QuantityType<>(value.getAsDouble(), MetricPrefix.KILO(SIUnits.METRE)));
                break;
            case "String":
                updateState(channelUID, new StringType(value.getAsString()));
                break;
            case "Switch":
                updateState(channelUID, value.getAsBoolean() ? OnOffType.ON : OnOffType.OFF);
                break;
            default:
                logUnknownChannelXml(channelUID.getId(), itemType);
                return false;
        }
        return true;
    }

    public static String capitalizeWords(String input) {
        return Arrays.stream(input.split("(?=[A-Z])")) // split before uppercase
                .map((String s) -> s.substring(0, 1).toUpperCase() + s.substring(1)) // capitalize each
                .collect(Collectors.joining(" "));
    }

    private String getThingKey(String key) {
        Map<String, String> props = getThing().getProperties();

        switch (props.get("type")) {
            case "site":
                return key;
            case null:
                logger.warn("Property type was not found for {}", getThing().getUID().getAsString());
                return key;
            default:
                return props.get("type") + key.substring(0, 1).toUpperCase() + key.substring(1);
        }
    }

    @Override
    public void updateFromEvccState(JsonObject root) {
        if (!isInitialized) {
            return;
        }
        ThingBuilder builder = editThing();
        boolean channelAdded = false;

        for (Map.Entry<@Nullable String, @Nullable JsonElement> entry : root.entrySet()) {
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
                ChannelTypeUID typeUID = new ChannelTypeUID(BINDING_ID, thingKey);
                Channel newChannel = ChannelBuilder.create(channelUID).withType(typeUID).build();
                builder.withChannel(newChannel);
                channelAdded = true;
                logger.debug("Dynamisch neuen Channel hinzugefügt: {}", thingKey);
            }

            if (!setItemValue(getItemType(new ChannelTypeUID(BINDING_ID, channelUID.getId())), channelUID, value)) {
                continue;
            }
        }

        if (channelAdded) {
            updateThing(builder.build());
        }

        updateStatus(ThingStatus.ONLINE);
    }

    protected void sendCommand(String url) {
        if (bridgeHandler != null) {
            HttpClient httpClient = bridgeHandler.getHttpClient();
            try {
                ContentResponse response = httpClient.newRequest(url).timeout(5, TimeUnit.SECONDS)
                        .method(HttpMethod.POST).header(HttpHeader.ACCEPT, "application/json").send();

                if (response.getStatus() == 200) {
                    @Nullable
                    JsonObject returnValue = gson.fromJson(response.getContentAsString(), JsonObject.class);
                    if (returnValue != null) {
                        ; // TODO: check for error in response correct
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    logger.warn("EVCC API-Fehler: HTTP {}", response.getStatus());
                }

            } catch (Exception e) {
                logger.error("EVCC Bridge konnte API nicht abrufen", e);
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
                dispose();
                break;
            case UNINITIALIZED:
                updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED);
                dispose();
                break;
            case ONLINE:
                if (!isInitialized) {
                    Bridge bridge = getBridge();
                    if (bridge == null) {
                        break;
                    }
                    logger.debug("Bridge {} ist wieder ONLINE – initialisiere EVCC-Site-Handler neu…", bridge.getUID());
                    initialize(); // explizit neue Initialisierung starten
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }
                break;
            default:
                break;
        }
    }

    private void logUnknownChannelXml(String key, String itemType) {
        String xmlSnippet = String.format(
                "<channel-type id=\"%s\">\n" + "    <item-type>%s</item-type>\n" + "    <label>%s</label>\n"
                        + "    <description>Autogenerated placeholder</description>\n" + "</channel-type>",
                key, itemType, capitalizeWords(key));
        logger.debug("Unbekannter Channel erkannt – eventuell fehlt die Definition im {}.xml:\n{}",
                thing.getProperties().get("type"), xmlSnippet);
    }
}
