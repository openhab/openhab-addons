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
package org.openhab.binding.evcc.internal.handler;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;
import static org.openhab.core.util.StringUtils.capitalize;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.i18n.TranslationProvider;
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
import org.openhab.core.types.State;
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
    protected String type = "";
    protected @Nullable EvccBridgeHandler bridgeHandler;
    protected boolean isInitialized = false;
    protected String endpoint = "";
    protected String smartCostType = "";
    protected @Nullable StateResolver stateResolver = StateResolver.getInstance();

    public EvccBaseThingHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.channelTypeRegistry = channelTypeRegistry;
    }

    protected void commonInitialize(JsonObject state) {
        List<Channel> newChannels = new ArrayList<>();

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

            @Nullable
            Channel channel = createChannel(thingKey, value);
            if (null != channel) {
                newChannels.add(channel);
            }
        }

        newChannels.sort(Comparator.comparing(channel -> channel.getUID().getId()));
        updateThing(editThing().withChannels(newChannels).build());
        updateStatus(ThingStatus.ONLINE);
        isInitialized = true;
        Optional.ofNullable(bridgeHandler).ifPresentOrElse(handler -> handler.register(this),
                () -> logger.error("No bridgeHandler present when initializing the thing"));
    }

    protected String getPropertyOrConfigValue(String propertyName) {
        Object value = thing.getConfiguration().get(propertyName);
        if (value instanceof String s) {
            return s;
        } else if (value instanceof BigDecimal bd) {
            return bd.toString();
        } else {
            switch (propertyName) {
                case PROPERTY_INDEX:
                    return thing.getProperties().getOrDefault(propertyName, "0");
                default:
                    return "";
            }
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
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> handler.unregister(this));
        isInitialized = false;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String key = Utils.getKeyFromChannelUID(channelUID);
            Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
                JsonObject jsonState = getStateFromCachedState(handler.getCachedEvccState());
                if (!jsonState.isEmpty()) {
                    JsonElement value = jsonState.get(key);
                    Optional.ofNullable(stateResolver).ifPresent(resolver -> {
                        State state = resolver.resolveState(key, value);
                        if (null != state) {
                            updateState(channelUID, state);
                        }
                    });
                }
            });
        }
    }

    private String getItemType(ChannelTypeUID channelTypeUID) {
        ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID);
        if (null != channelType) {
            String itemType = channelType.getItemType();
            return Objects.requireNonNullElse(itemType, "Unknown");
        }
        return "Unknown";
    }

    @Nullable
    protected Channel createChannel(String thingKey, JsonElement value) {
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, thingKey);
        String itemType = getItemType(channelTypeUID);
        if (!"Unknown".equals(itemType)) {
            String label = getChannelLabel(thingKey);
            Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), thingKey)).withLabel(label)
                    .withType(channelTypeUID).withAcceptedItemType(itemType).build();
            if (getThing().getChannels().stream().noneMatch(c -> c.getUID().equals(channel.getUID()))) {
                return channel;
            }
        } else {
            String valString = Objects.requireNonNullElse(value.toString(), "Null");
            logUnknownChannelXmlAsync(thingKey, "Hint for type: " + valString);
        }
        return null;
    }

    private String getChannelLabel(String thingKey) {
        @Nullable
        String tmp = Optional.ofNullable(bridgeHandler).map(handler -> {
            String labelKey = "channel-type.evcc." + thingKey + ".label";
            BundleContext ctx = FrameworkUtil.getBundle(EvccBridgeHandler.class).getBundleContext();
            TranslationProvider tp = handler.getI18nProvider();
            Locale locale = handler.getLocaleProvider().getLocale();
            return tp.getText(ctx.getBundle(), labelKey, thingKey, locale);
        }).orElse(thingKey);
        return null != tmp ? tmp : thingKey;
    }

    protected String getThingKey(String key) {
        if ("batteryGridChargeLimit".equals(key) || "smartCostLimit".equals(key)) {
            if ("co2".equals(smartCostType)) {
                key += "Co2";
            } else {
                key += "Price";
            }
        }
        String type = "heating".equals(this.type) ? "loadpoint" : this.type;
        return (type + "-" + Utils.sanitizeChannelID(key));
    }

    public void updateStatesFromApiResponse(JsonObject jsonState) {
        if (!isInitialized || jsonState.isEmpty()) {
            return;
        }

        for (Map.Entry<@Nullable String, @Nullable JsonElement> entry : jsonState.entrySet()) {
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
                List<Channel> channels = new ArrayList<>(getThing().getChannels());
                builder.withoutChannels(channels);
                @Nullable
                Channel newChannel = createChannel(thingKey, value);
                if (null != newChannel) {
                    channels.add(newChannel);
                    channels.sort(Comparator.comparing(channel -> channel.getUID().getId()));
                    updateThing(builder.withChannels(channels).build());
                }
            }
            resolveAndUpdateState(channelUID, thingKey, value);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    protected void resolveAndUpdateState(ChannelUID channelUID, String key, JsonElement value) {
        Optional.ofNullable(stateResolver).ifPresent(resolver -> {
            State state = resolver.resolveState(key, value);
            if (null != state) {
                updateState(channelUID, state);
            }
        });
    }

    protected boolean sendCommand(String url) {
        AtomicBoolean successful = new AtomicBoolean(false);
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            HttpClient httpClient = handler.getHttpClient();
            try {
                ContentResponse response = httpClient.newRequest(url).timeout(5, TimeUnit.SECONDS)
                        .method(HttpMethod.POST).header(HttpHeader.ACCEPT, "application/json").send();

                if (response.getStatus() == 200) {
                    logger.debug("Sending command was successful");
                    successful.set(true);
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
        return successful.get();
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
                """, key, itemType, capitalize(key));

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
}
