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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link EvccBaseThingHandler} is responsible for building a base class with common methods for the thing handlers
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public abstract class EvccBaseThingHandler extends BaseThingHandler implements EvccThingLifecycleAware {

    private final Logger logger = LoggerFactory.getLogger(EvccBaseThingHandler.class);
    private final ChannelTypeRegistry channelTypeRegistry;
    private final Gson gson = new Gson();
    protected @Nullable EvccBridgeHandler bridgeHandler;
    protected boolean isInitialized = false;
    protected String endpoint = "";
    protected String smartCostType = "";

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

            createChannel(thingKey, builder, value);
        }

        updateThing(builder.build());
        updateStatus(ThingStatus.ONLINE);
        isInitialized = true;
        Optional.ofNullable(bridgeHandler).ifPresentOrElse(handler -> handler.register(this),
                () -> logger.error("No bridgeHandler present when initializing the thing"));
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
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> handler.unregister(this));
        isInitialized = false;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String key = Utils.getKeyFromChannelUID(channelUID);
            Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
                JsonObject state = getStateFromCachedState(handler.getCachedEvccState());
                if (!state.isEmpty()) {
                    JsonElement value = state.get(key);
                    ItemTypeUnit typeUnit = getItemType(new ChannelTypeUID(BINDING_ID, channelUID.getId()));
                    if (null != value) {
                        setItemValue(typeUnit, channelUID, value);
                    }
                }
            });
        }
    }

    private void createChannel(String thingKey, ThingBuilder builder, JsonElement value) {
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, thingKey);
        ItemTypeUnit typeUnit = getItemType(channelTypeUID);
        String itemType = typeUnit.itemType;

        if (!"Unknown".equals(itemType)) {
            String label = getChannelLabel(thingKey);
            Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), thingKey)).withLabel(label)
                    .withType(channelTypeUID).withAcceptedItemType(itemType).build();
            if (getThing().getChannels().stream().noneMatch(c -> c.getUID().equals(channel.getUID()))) {
                builder.withChannel(channel);
            }
        } else {
            String valString = Objects.requireNonNullElse(value.toString(), "Null");
            logUnknownChannelXmlAsync(thingKey, "Hint for type: " + valString);
        }
    }

    private String getChannelLabel(String thingKey) {
        String returnValue = thingKey;
        @Nullable
        String tmp = Optional.ofNullable(bridgeHandler).map(handler -> {
            String labelKey = "channel-type.evcc." + thingKey + ".label";
            BundleContext ctx = FrameworkUtil.getBundle(EvccBridgeHandler.class).getBundleContext();
            TranslationProvider tp = handler.getI18nProvider();
            Locale locale = handler.getLocaleProvider().getLocale();
            return tp.getText(ctx.getBundle(), labelKey, thingKey, locale);
        }).orElse(thingKey);
        if (null != tmp) {
            returnValue = tmp;
        }
        return returnValue;
    }

    private ItemTypeUnit getItemType(ChannelTypeUID channelTypeUID) {
        ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID);
        if (null != channelType) {
            String itemType = channelType.getItemType();
            if (null != itemType) {
                Unit<?> unit = Utils.getUnitFromChannelType(itemType);
                return new ItemTypeUnit(channelType, unit);
            }
        }
        return new ItemTypeUnit(channelType, Units.ONE);
    }

    private void setItemValue(ItemTypeUnit itemTypeUnit, ChannelUID channelUID, JsonElement value) {
        if (value.isJsonNull() || itemTypeUnit.itemType.isEmpty()) {
            return;
        }
        switch (itemTypeUnit.itemType) {
            case CoreItemFactory.NUMBER:
            case NUMBER_CURRENCY:
            case NUMBER_ENERGY_PRICE:
                updateState(channelUID, new DecimalType(value.getAsDouble()));
                break;
            case NUMBER_DIMENSIONLESS:
            case NUMBER_ELECTRIC_CURRENT:
            case NUMBER_EMISSION_INTENSITY:
            case NUMBER_ENERGY:
            case NUMBER_LENGTH:
            case NUMBER_POWER:
                Double finalValue = "%".equals(itemTypeUnit.unitHint) ? value.getAsDouble() / 100 : value.getAsDouble();
                if (channelUID.getId().contains("capacity") || "km".equals(itemTypeUnit.unitHint)) {
                    updateState(channelUID, new QuantityType<>(finalValue, itemTypeUnit.unit.multiply(1000)));
                } else if ("Wh".equals(itemTypeUnit.unitHint)) {
                    updateState(channelUID, new QuantityType<>(finalValue, itemTypeUnit.unit.divide(1000)));
                } else {
                    updateState(channelUID, new QuantityType<>(finalValue, itemTypeUnit.unit));
                }
                break;
            case NUMBER_TIME:
                updateState(channelUID, QuantityType.valueOf(value.getAsDouble() + " s"));
                break;
            case NUMBER_TEMPERATURE:
                updateState(channelUID, QuantityType.valueOf(value.getAsDouble() + " " + itemTypeUnit.unitHint));
                break;
            case CoreItemFactory.DATETIME:
                updateState(channelUID, new DateTimeType(value.getAsString()));
                break;
            case CoreItemFactory.STRING:
                updateState(channelUID, new StringType(value.getAsString()));
                break;
            case CoreItemFactory.SWITCH:
                updateState(channelUID, value.getAsBoolean() ? OnOffType.ON : OnOffType.OFF);
                break;
            default:
                logUnknownChannelXmlAsync(channelUID.getId(), "Hint for type: " + value.toString());
        }
    }

    protected String getThingKey(String key) {
        Map<String, String> props = getThing().getProperties();
        if ("batteryGridChargeLimit".equals(key) || "smartCostLimit".equals(key)) {
            if ("co2".equals(smartCostType)) {
                key += "Co2";
            } else {
                key += "Price";
            }
        }
        String type = "heating".equals(props.get("type")) ? "loadpoint" : props.get("type");
        return (type + "-" + Utils.sanitizeChannelID(key));
    }

    @Override
    public void updateFromEvccState(JsonObject state) {
        if (!isInitialized || state.isEmpty()) {
            return;
        }

        for (Map.Entry<@Nullable String, @Nullable JsonElement> entry : state.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (null == key || null == value || !value.isJsonPrimitive()) {
                continue;
            }

            String thingKey = getThingKey(key);
            ChannelUID channelUID = channelUID(thingKey);
            Channel existingChannel = getThing().getChannel(channelUID.getId());
            if (existingChannel == null) {
                ThingBuilder builder = editThing();
                createChannel(thingKey, builder, value);
                updateThing(builder.build());
            }
            setItemValue(getItemType(new ChannelTypeUID(BINDING_ID, channelUID.getId())), channelUID, value);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    protected void sendCommand(String url) {
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            HttpClient httpClient = handler.getHttpClient();
            try {
                ContentResponse response = httpClient.newRequest(url).timeout(5, TimeUnit.SECONDS)
                        .method(HttpMethod.POST).header(HttpHeader.ACCEPT, "application/json").send();

                if (response.getStatus() == 200) {
                    logger.debug("Sending command was successful");
                } else {
                    @Nullable
                    JsonObject responseJson = gson.fromJson(response.getContentAsString(), JsonObject.class);
                    Optional.ofNullable(responseJson).ifPresent(json -> {
                        if (json.has("error")) {
                            logger.debug("Sending command was unsuccessful, got this error:\n {}",
                                    json.get("error").getAsString());
                            updateStatus(getThing().getStatus(), ThingStatusDetail.COMMUNICATION_ERROR,
                                    json.get("error").getAsString());
                        } else {
                            updateStatus(getThing().getStatus(), ThingStatusDetail.COMMUNICATION_ERROR);
                            logger.warn("evcc API error: HTTP {}", response.getStatus());
                        }
                    });
                }
            } catch (Exception e) {
                logger.warn("evcc bridge couldn't call the API", e);
            }
        });
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

    public void logUnknownChannelXmlAsync(String key, String itemType) {
        CompletableFuture.runAsync(() -> logUnknownChannelXml(key, itemType));
    }

    protected void logUnknownChannelXml(String key, String itemType) {
        String xmlSnippet = String.format("""
                <channel-type id="%s">
                    <item-type unitHint="">%s</item-type>
                    <label>%s</label>
                    <description>Autogenerated placeholder</description>
                    <state readOnly="true"></state>
                    <autoUpdatePolicy>veto</autoUpdatePolicy>
                </channel-type>
                """, key, itemType, Utils.capitalizeWords(key));

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
            Files.writeString(filePath, xmlSnippet, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            logger.debug("Unknown channel definition written to file: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to write unknown channel definition to file", e);
        }
    }

    private static class ItemTypeUnit {
        private final Unit<?> unit;
        private final String unitHint;
        private final String itemType;

        public ItemTypeUnit(@Nullable ChannelType type, @Nullable Unit<?> unit) {
            if (null == type) {
                unitHint = "";
                itemType = "Unknown";
            } else {
                String tmp = type.getUnitHint();
                unitHint = null != tmp ? tmp : "";
                tmp = type.getItemType();
                itemType = null != tmp ? tmp : "Unknown";
            }
            this.unit = Objects.requireNonNullElse(unit, Units.ONE);
        }
    }
}
