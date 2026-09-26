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
import static org.openhab.binding.evcc.internal.handler.Utils.getKeyFromChannelUID;
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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
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
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The {@link EvccBaseThingHandler} is responsible for building a base class with common methods for the thing handlers
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public abstract class EvccBaseThingHandler extends BaseThingHandler implements EvccThingLifecycleAware {

    protected final Logger logger = LoggerFactory.getLogger(EvccBaseThingHandler.class);
    private final ChannelTypeRegistry channelTypeRegistry;
    private final Gson gson = new Gson();
    protected String type = "";
    protected @Nullable EvccBridgeHandler bridgeHandler;
    protected String endpoint = "";
    protected String smartCostType = "";
    // Guarded together with dispose() by synchronizing on `this`; see isDisposed() javadoc
    // in EvccThingLifecycleAware for why this must be checked-and-acted-upon atomically by
    // any caller that dispatches updates to this handler from another thread.
    private volatile boolean disposed = false;

    public EvccBaseThingHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.channelTypeRegistry = channelTypeRegistry;
    }

    /**
     * Get a property value from the Thing configuration or properties.
     *
     * Attempts to retrieve the value from the Thing's configuration first.
     * For property index and vehicle ID, falls back to Thing properties with defaults.
     *
     * @param propertyName The name of the property to retrieve
     * @return The property value as a string, or empty string if not found
     */
    protected String getPropertyOrConfigValue(String propertyName) {
        Object value = thing.getConfiguration().get(propertyName);
        if (value instanceof String s) {
            return s;
        } else if (value instanceof BigDecimal bd) {
            return bd.toString();
        } else {
            return switch (propertyName) {
                case PROPERTY_INDEX -> thing.getProperties().getOrDefault(propertyName, "0");
                case PROPERTY_VEHICLE_ID -> thing.getProperties().getOrDefault(propertyName, "");
                default -> "";
            };
        }
    }

    /**
     * Initialize the Thing handler.
     *
     * Sets status to UNKNOWN and attempts to locate and store the bridge handler.
     * If bridge is not available or not an EvccBridgeHandler, sets status to OFFLINE.
     */
    @Override
    public void initialize() {
        disposed = false;
        updateStatus(ThingStatus.UNKNOWN);
        if (getBridge() instanceof Bridge bridge && bridge.getHandler() instanceof EvccBridgeHandler handler) {
            bridgeHandler = handler;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    /**
     * Dispose of the Thing handler.
     *
     * Unregisters this handler from the bridge handler to stop receiving updates.
     * Called when the handler is being removed or deactivated.
     *
     * Synchronizes on {@code this} - the same monitor used by callers dispatching updates to
     * this handler (see {@link EvccThingLifecycleAware#isDisposed()}) - so that disposal is
     * atomic with respect to any update currently in flight or about to be dispatched.
     */
    @Override
    public void dispose() {
        synchronized (this) {
            disposed = true;
            Optional.ofNullable(bridgeHandler).ifPresent(handler -> handler.unregister(this));
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * Handle incoming commands from the openHAB framework.
     *
     * Supports REFRESH commands which re-query the cached state and update the channel.
     * Other commands are ignored by the base implementation.
     *
     * @param channelUID The channel that received the command
     * @param command The command to handle (typically RefreshType.REFRESH)
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String key = getKeyFromChannelUID(channelUID);
            Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
                JsonObject jsonState = getStateFromCachedState(handler.getCachedEvccState());
                if (!jsonState.isEmpty()) {
                    JsonElement value = jsonState.get(key);
                    if (value == null) {
                        return;
                    }
                    State state = StateResolver.getInstance().resolveState(key, value);
                    if (null != state) {
                        updateState(channelUID, state);
                    }
                }
            });
        }
    }

    /**
     * Handle partial updates from the websocket.
     *
     * Base implementation processes primitives, objects, and arrays:
     * - Primitives update a single channel
     * - Objects iterate entries and update individual channels
     * - Arrays iterate elements and update numbered channels
     *
     * Handlers should override this method to provide specialized handling for their message structure.
     * Updates Thing status to ONLINE on successful processing.
     *
     * @param key The update key (channel identifier)
     * @param value The update value (primitive, object, or array)
     */
    @Override
    public void handleUpdate(String key, JsonElement value) {
        if (value.isJsonPrimitive()) {
            String thingKey = getThingKey(key);
            ChannelUID channelUID = channelUID(thingKey);
            resolveAndUpdateState(channelUID, key, value);
        } else {
            if (value instanceof JsonObject object) {
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    String subkey = entry.getKey();
                    JsonElement val = entry.getValue();
                    String thingKey = getThingKey(key + Utils.capitalizeFirstLetter(subkey));
                    ChannelUID channelUID = channelUID(thingKey);
                    resolveAndUpdateState(channelUID, key + Utils.capitalizeFirstLetter(subkey), val);
                }
            } else if (value instanceof JsonArray array) {
                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i) instanceof JsonPrimitive primitive) {
                        String thingKey = getThingKey(key + (i + 1));
                        ChannelUID channelUID = channelUID(thingKey);
                        resolveAndUpdateState(channelUID, key + (i + 1), primitive);
                    }
                }
            }
        }
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Apply an already-normalized update. The router calls this for routes that carry a state transformer;
     * the members already map directly to channel keys, so they are applied without prefixing.
     *
     * @param normalized The normalized update whose members map directly to channel keys
     */
    @Override
    public void applyNormalizedUpdate(JsonObject normalized) {
        updateOnlyPresentChannels(normalized);
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Get the handler type identifier.
     *
     * @return The type string (e.g., "battery", "pv", "loadpoint")
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Get the item type for a channel from the ChannelTypeRegistry.
     *
     * Queries the registry for the channel type definition and returns the item type.
     *
     * @param channelTypeUID The channel type to look up
     * @return The item type (e.g., "Number", "String") or "Unknown" if not found
     */
    private String getItemType(ChannelTypeUID channelTypeUID) {
        ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID);
        if (null != channelType) {
            String itemType = channelType.getItemType();
            return Objects.requireNonNullElse(itemType, "Unknown");
        }
        return "Unknown";
    }

    /**
     * Create a Channel for the specified Thing key and value.
     *
     * Looks up the channel type definition and constructs a Channel with proper configuration.
     * Logs unknown channel types asynchronously for debugging.
     *
     * @param thingKey The channel identifier (e.g., "power", "soc")
     * @param value The sample value (used for unknown channel logging)
     * @return The created Channel, or null if channel type is unknown or already exists
     */
    @Nullable
    protected Channel createChannel(String thingKey, JsonElement value) {
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, thingKey);
        String itemType = getItemType(channelTypeUID);
        if (!"Unknown".equals(itemType)) {
            String label = getChannelLabel(thingKey);
            Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), thingKey)).withLabel(label)
                    .withType(channelTypeUID).withAcceptedItemType(itemType).build();
            if (getThing().getChannel(channel.getUID()) == null) {
                return channel;
            }
        } else {
            String valString = Objects.requireNonNullElse(value.toString(), "Null");
            logUnknownChannelXmlAsync(thingKey, "Hint for type: " + valString);
        }
        return null;
    }

    protected String getChannelLabel(String thingKey) {
        @Nullable
        String tmp = Optional.ofNullable(bridgeHandler).map(handler -> {
            String labelKey = "channel-type.evcc." + thingKey + ".label";
            @Nullable
            Bundle bundle = FrameworkUtil.getBundle(EvccBridgeHandler.class);
            if (bundle == null || bundle.getBundleContext() == null) {
                return thingKey;
            }
            BundleContext ctx = bundle.getBundleContext();
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

    public void createChannelsAndSetStatesFromApiResponse(JsonObject jsonState) {
        updateStatesFromApiResponse(jsonState);
    }

    public void updateStatesFromApiResponse(JsonObject jsonState) {
        if (jsonState.isEmpty()) {
            return;
        }
        Set<String> validChannelIds = extractValidChannelIds(jsonState);
        List<Channel> channels = new ArrayList<>(getThing().getChannels());
        boolean channelsChanged = syncThingChannels(channels, jsonState, validChannelIds);
        if (channelsChanged) {
            updateThing(editThing().withChannels(channels).build());
        }
        updateChannelStates(getThing().getChannels(), jsonState, validChannelIds);
    }

    private Set<String> extractValidChannelIds(JsonObject jsonState) {
        return jsonState.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null && e.getValue().isJsonPrimitive())
                .map(e -> getThingKey(e.getKey())).collect(Collectors.toSet());
    }

    private boolean syncThingChannels(List<Channel> channels, JsonObject jsonState, Set<String> validChannelIds) {
        boolean channelsChanged = addNonExistingChannels(channels, jsonState);

        // Never remove channels on partial updates - they may reappear in future messages
        // or be unlinked channels that should be preserved
        return channelsChanged;
    }

    private boolean addNonExistingChannels(List<Channel> channels, JsonObject jsonState) {
        boolean channelsAdded = false;
        for (Map.Entry<@Nullable String, @Nullable JsonElement> entry : jsonState.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (null == key || null == value || !value.isJsonPrimitive()) {
                continue;
            }

            String thingKey = getThingKey(key);
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), thingKey);
            Channel existingChannel = getThing().getChannel(channelUID);
            if (existingChannel == null) {
                @Nullable
                Channel newChannel = createChannel(thingKey, value);
                if (null != newChannel) {
                    channels.add(newChannel);
                    channelsAdded = true;
                }
            }
        }
        if (channelsAdded) {
            channels.sort(Comparator.comparing(c -> c.getUID().getId()));
        }
        return channelsAdded;
    }

    private void updateChannelStates(List<Channel> channels, JsonObject jsonState, Set<String> validChannelIds) {
        Set<String> excludedFromReset = getChannelIdsExcludedFromReset();
        for (Channel channel : channels) {
            ChannelUID uid = channel.getUID();
            String id = uid.getId();

            if (validChannelIds.contains(id)) {
                // If channel is valid -> resolve state and set channel state
                @Nullable
                JsonElement value = jsonState.get(getKeyFromChannelUID(uid));
                if (value != null) {
                    resolveAndUpdateState(uid, id, value);
                }
            } else if (!excludedFromReset.contains(id)) {
                // else set channel state to UNDEF if channel is linked
                if (isLinked(uid)) {
                    updateState(uid, UnDefType.UNDEF);
                }
            }
        }
    }

    /**
     * Channel IDs that must not be reset to {@link UnDefType#UNDEF} by {@link #updateChannelStates}
     * even though they are absent from the JSON object passed to {@link #updateStatesFromApiResponse}.
     * <p>
     * This is needed for channels whose state is derived and published separately (e.g. from a
     * {@link org.openhab.core.types.TimeSeries}) rather than resolved directly from a matching JSON key.
     *
     * @return the set of channel IDs to exclude from the UNDEF reset, empty by default
     */
    protected Set<String> getChannelIdsExcludedFromReset() {
        return Set.of();
    }

    protected void resolveAndUpdateState(ChannelUID channelUID, String key, JsonElement value) {
        State state = StateResolver.getInstance().resolveState(key, value);
        if (null != state) {
            updateState(channelUID, state);
        }
    }

    /**
     * Updates only the channels present in the provided JSON state without setting UNDEF for missing channels.
     * This method is intended for partial updates from websocket messages where missing fields should preserve
     * their current values rather than being reset to UNDEF.
     * 
     * Also creates channel definitions for any new fields encountered that don't yet have channels.
     *
     * @param partialState The partial JSON state containing only the fields to update
     */
    protected void updateOnlyPresentChannels(JsonObject partialState) {
        logger.trace("updateOnlyPresentChannels called with {} entries", partialState.size());

        // First, sync any new channels that may not exist yet
        List<Channel> channels = new ArrayList<>(getThing().getChannels());
        boolean channelsChanged = false;

        for (Map.Entry<String, JsonElement> entry : partialState.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive()) {
                String thingKey = getThingKey(key);
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), thingKey);
                Channel existingChannel = getThing().getChannel(channelUID);
                if (existingChannel == null) {
                    @Nullable
                    Channel newChannel = createChannel(thingKey, value);
                    if (null != newChannel) {
                        logger.debug("Created new channel: {}", thingKey);
                        channels.add(newChannel);
                        channelsChanged = true;
                    }
                }
            }
        }

        if (channelsChanged) {
            logger.debug("Updating thing with {} new channels", channels.size());
            channels.sort(Comparator.comparing(c -> c.getUID().getId()));
            updateThing(editThing().withChannels(channels).build());
        }

        // Then update all present channels
        for (Map.Entry<String, JsonElement> entry : partialState.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            String thingKey = getThingKey(key);
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), thingKey);
            logger.trace("Processing update for key '{}' (channel '{}'), checking if linked...", key, thingKey);
            try {
                if (isLinked(channelUID)) {
                    logger.trace("Updating linked channel {}", thingKey);
                    resolveAndUpdateState(channelUID, key, value);
                } else {
                    logger.trace("Channel {} not linked, skipping update", thingKey);
                }
            } catch (IllegalStateException e) {
                logger.debug("Handler disposed while processing channel {}", thingKey);
            } catch (Exception e) {
                logger.error("Unexpected error updating channel {}: {}", thingKey, e.getMessage(), e);
            }
        }
    }

    /**
     * Safely extracts an element from an indexed array with bounds checking.
     *
     * @param array The array to extract from
     * @param index The index to extract
     * @return The element at index, or empty JsonObject if out of bounds or not an object
     */
    @Nullable
    protected JsonObject extractFromIndexedArray(JsonArray array, int index) {
        if (index >= 0 && index < array.size() && array.get(index).isJsonObject()) {
            return array.get(index).getAsJsonObject();
        }
        return null;
    }

    /**
     * Safely extracts an object from a map with key existence check.
     *
     * @param map The map to extract from
     * @param key The key to look up
     * @return The object at key, or null if not found or not an object
     */
    @Nullable
    protected JsonObject extractFromObjectMap(JsonObject map, String key) {
        if (map.has(key) && map.get(key).isJsonObject()) {
            return map.get(key).getAsJsonObject();
        }
        return null;
    }

    protected void performApiRequest(String url, String method, JsonElement payload) {
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            HttpMethod httpMethod = HttpMethod.valueOf(method);
            handler.requestQueue.enqueueRequest(url, httpMethod, payload, this::checkResponse,
                    error -> updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR));
        });
    }

    protected @Nullable JsonElement performApiReadRequest(String url) {
        EvccBridgeHandler currentBridgeHandler = bridgeHandler;
        if (currentBridgeHandler == null) {
            return null;
        }

        CountDownLatch completion = new CountDownLatch(1);
        AtomicReference<@Nullable JsonElement> responseBody = new AtomicReference<>();
        AtomicReference<@Nullable Exception> errorRef = new AtomicReference<>();

        currentBridgeHandler.requestQueue.enqueueRequest(url, HttpMethod.GET, JsonNull.INSTANCE, response -> {
            try {
                responseBody.set(gson.fromJson(response.getContentAsString(), JsonElement.class));
            } catch (Exception e) {
                errorRef.set(e);
            } finally {
                completion.countDown();
            }
        }, error -> {
            errorRef.set(error);
            completion.countDown();
        });

        try {
            if (!completion.await(6, TimeUnit.SECONDS)) {
                logger.debug("Timed out reading evcc API response from {}", url);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted while reading evcc API response from {}", url, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return null;
        }

        Exception error = errorRef.get();
        if (error != null) {
            logger.debug("Failed to read evcc API response from {}", url, error);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return null;
        }
        return responseBody.get();
    }

    private void checkResponse(ContentResponse response) {
        if (response.getStatus() == 200) {
            logger.debug("Sending command was successful");
        } else {
            try {
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
            } catch (Exception e) {
                logger.warn("evcc bridge couldn't parse the API error response", e);
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
                Bridge bridge = getBridge();
                if (bridge == null) {
                    break;
                }
                logger.debug("Bridge {} is ONLINE again, initialize evcc {} again...", bridge.getUID(),
                        getThing().getUID().getId());
                initialize();
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
