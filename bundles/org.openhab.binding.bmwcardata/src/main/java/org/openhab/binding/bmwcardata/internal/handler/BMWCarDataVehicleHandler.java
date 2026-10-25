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
package org.openhab.binding.bmwcardata.internal.handler;

import static org.openhab.binding.bmwcardata.internal.BMWCarDataBindingConstants.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataApi;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataException;
import org.openhab.binding.bmwcardata.internal.api.dto.BasicData;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.binding.bmwcardata.internal.config.VehicleConfiguration;
import org.openhab.binding.bmwcardata.internal.handler.VehicleChannel.Drive;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
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
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * The {@link BMWCarDataVehicleHandler} publishes the telematic data of a vehicle, which is either pushed by the
 * stream of the account or polled from the REST API.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BMWCarDataVehicleHandler extends BaseThingHandler {

    // descriptors of the destination guidance, which needs a navigation system
    private static final String DESTINATION_PREFIX = "vehicle.cabin.infotainment.navigation.destinationSet.";

    private final Logger logger = LoggerFactory.getLogger(BMWCarDataVehicleHandler.class);

    private String vin = "";
    private boolean dynamicChannels = true;
    // serializes accepting and publishing values: the REST refresh and the stream deliver data concurrently, and a
    // value accepted as newer must not be published after a value that was accepted later; held while publishing, as
    // the order of the state updates is what counts
    private final Object dataLock = new Object();
    private @Nullable ScheduledFuture<?> propertiesJob;
    // states by channel id, to answer REFRESH commands without spending API requests
    private final Map<String, State> states = new ConcurrentHashMap<>();
    // the latest readings by descriptor, to drop values older than already published ones or copies: the stream
    // delivers many messages twice, and a refresh after the stream delivers the same values again; guarded by dataLock
    private final Map<String, Reading> readings = new HashMap<>();
    // once the vehicle reports isPlugged, the plug state is no longer derived from the charging port status
    private volatile boolean pluggedReported;
    // a refresh or stream message of the account may still deliver data while the handler is disposed
    private volatile boolean disposed;
    // guarded by dataLock
    private @Nullable BigDecimal latitude;
    private @Nullable BigDecimal longitude;
    private @Nullable BigDecimal altitude;
    private @Nullable Instant lastUpdate;

    public BMWCarDataVehicleHandler(Thing thing) {
        super(thing);
    }

    public String getVin() {
        return vin;
    }

    @Override
    public void initialize() {
        disposed = false;
        VehicleConfiguration config = getConfigAs(VehicleConfiguration.class);
        vin = config.vin.trim().toUpperCase(Locale.ROOT);
        dynamicChannels = config.dynamicChannels;
        if (!BMWCarDataApi.isValidVin(vin)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.vin");
            return;
        }
        Bridge bridge = getBridge();
        updateBridgeStatus(bridge == null ? ThingStatus.OFFLINE : bridge.getStatus());
    }

    @Override
    public void dispose() {
        disposed = true;
        cancelProperties();
        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (BMWCarDataApi.isValidVin(vin)) {
            updateBridgeStatus(bridgeStatusInfo.getStatus());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // a REFRESH must not publish a state that a concurrent update has just replaced
            synchronized (dataLock) {
                State state = states.get(channelUID.getId());
                if (state != null) {
                    updateState(channelUID, state);
                }
            }
        }
        // all channels are read-only
    }

    /**
     * Publishes the values of the given descriptors.
     */
    public void updateTelematicData(Map<String, TelematicValue> data) {
        if (disposed) {
            return;
        }
        synchronized (dataLock) {
            publishTelematicData(data);
        }
        // data received from the stream proves the vehicle reachable again after a failed poll
        if (getThing().getStatus() == ThingStatus.OFFLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void publishTelematicData(Map<String, TelematicValue> data) {
        BigDecimal newLatitude = null;
        BigDecimal newLongitude = null;
        BigDecimal newAltitude = null;
        Instant newest = null;
        Map<String, TelematicValue> dynamicValues = new LinkedHashMap<>();
        for (Map.Entry<String, TelematicValue> entry : data.entrySet()) {
            String descriptor = entry.getKey();
            TelematicValue value = entry.getValue();
            VehicleChannel channel = VehicleChannel.fromDescriptor(descriptor);
            // the API returns all descriptors of the container, those never reported by the vehicle without value
            if ((channel == null && !dynamicChannels) || VehicleChannel.toPrimitive(value) == null) {
                continue;
            }
            Instant timestamp = parseTimestamp(value.timestamp);
            if (timestamp != null) {
                if (!isNewer(descriptor, new Reading(timestamp, normalize(value.value), value.unit))) {
                    continue;
                }
                if (newest == null || timestamp.isAfter(newest)) {
                    newest = timestamp;
                }
            }
            if (channel == null) {
                dynamicValues.put(descriptor, value);
                continue;
            }
            State state = channel.toState(value);
            if (state == null) {
                logger.trace("Ignoring unsupported value {} of {}", value.value, descriptor);
                continue;
            }
            switch (channel) {
                case LATITUDE -> {
                    if (state instanceof DecimalType decimal) {
                        newLatitude = decimal.toBigDecimal();
                    }
                }
                case LONGITUDE -> {
                    if (state instanceof DecimalType decimal) {
                        newLongitude = decimal.toBigDecimal();
                    }
                }
                default -> {
                    String channelUid = channel.getChannelUid();
                    if (channelUid != null) {
                        publish(channelUid, state);
                    }
                    String detailChannelUid = channel.getDetailChannelUid();
                    State detailState = channel.toDetailState(value);
                    if (detailChannelUid != null && detailState != null) {
                        publish(detailChannelUid, detailState);
                    }
                    if (channel == VehicleChannel.PLUGGED) {
                        pluggedReported = true;
                    } else if (channel == VehicleChannel.PORT && !pluggedReported) {
                        State pluggedState = VehicleChannel.toPluggedState(value);
                        String pluggedChannelUid = VehicleChannel.PLUGGED.getChannelUid();
                        if (pluggedState != null && pluggedChannelUid != null) {
                            publish(pluggedChannelUid, pluggedState);
                        }
                    }
                    if (channel == VehicleChannel.ALTITUDE && state instanceof QuantityType<?> altitudeState) {
                        QuantityType<?> metres = altitudeState.toUnit(SIUnits.METRE);
                        if (metres != null) {
                            newAltitude = metres.toBigDecimal();
                        }
                    }
                }
            }
        }

        if (newAltitude != null) {
            altitude = newAltitude;
        }
        if (newLatitude != null || newLongitude != null) {
            if (newLatitude != null) {
                latitude = newLatitude;
            }
            if (newLongitude != null) {
                longitude = newLongitude;
            }
            BigDecimal lat = latitude;
            BigDecimal lon = longitude;
            BigDecimal alt = altitude;
            if (lat != null && lon != null) {
                publish(CHANNEL_GPS,
                        alt != null ? new PointType(new DecimalType(lat), new DecimalType(lon), new DecimalType(alt))
                                : new PointType(new DecimalType(lat), new DecimalType(lon)));
            }
        }
        Instant previous = lastUpdate;
        if (newest != null && (previous == null || newest.isAfter(previous))) {
            lastUpdate = newest;
            publish(CHANNEL_LAST_UPDATE, new DateTimeType(newest));
        }
        if (!dynamicValues.isEmpty()) {
            updateDynamicChannels(dynamicValues);
        }
    }

    /**
     * Publishes the values of descriptors without predefined channel, creating their channels as needed.
     */
    private void updateDynamicChannels(Map<String, TelematicValue> values) {
        Map<String, State> newStates = new LinkedHashMap<>();
        // runs under dataLock, so the thing updates of concurrent data do not overwrite each other
        ThingBuilder builder = null;
        List<String> newChannelIds = new ArrayList<>();
        for (Map.Entry<String, TelematicValue> entry : values.entrySet()) {
            String descriptor = entry.getKey();
            String channelId = GROUP_OTHER + "#" + DynamicChannel.channelId(descriptor);
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId);
            Channel channel = getThing().getChannel(channelUID);
            if (channel != null) {
                State state = DynamicChannel.toState(entry.getValue(), channel.getAcceptedItemType());
                if (state != null) {
                    newStates.put(channelId, state);
                }
                continue;
            }
            DynamicChannel.Definition definition = DynamicChannel.define(entry.getValue());
            if (definition == null) {
                continue;
            }
            if (builder == null) {
                builder = editThing();
            }
            builder.withChannel(ChannelBuilder.create(channelUID, definition.itemType())
                    .withType(new ChannelTypeUID(BINDING_ID, definition.channelTypeId())).withLabel(descriptor)
                    .build());
            newChannelIds.add(channelId);
            newStates.put(channelId, definition.state());
        }
        if (builder != null) {
            logger.debug("Creating dynamic channels {}", newChannelIds);
            updateThing(builder.build());
        }
        newStates.forEach(this::publish);
    }

    /**
     * The latest value of a descriptor, to drop older values and copies.
     *
     * @param timestamp when the vehicle reported the value
     * @param value the value, normalized by {@link #normalize(JsonElement)}
     * @param unit the unit, if any
     */
    private record Reading(Instant timestamp, @Nullable Object value, @Nullable String unit) {
    }

    /**
     * Records the reading when it is newer than the last one of the descriptor. A reading with the same timestamp,
     * value and unit as the last one is a copy, which the stream delivers for many messages and a refresh delivers
     * after the stream, and is not published again.
     */
    private boolean isNewer(String descriptor, Reading reading) {
        Reading previous = readings.get(descriptor);
        // an older reading or a copy keeps the previous one, so it is not published
        if (previous != null && (reading.timestamp().isBefore(previous.timestamp()) || reading.equals(previous))) {
            return false;
        }
        readings.put(descriptor, reading);
        return true;
    }

    /**
     * Normalizes a value for comparison: the REST API sends numbers and booleans as strings, e.g. {@code "200"}, the
     * stream as JSON numbers and booleans, e.g. {@code 200}.
     *
     * @return a number as {@link BigDecimal} without trailing zeros, another primitive as its text, or the element
     */
    private static @Nullable Object normalize(@Nullable JsonElement value) {
        if (value instanceof JsonPrimitive primitive) {
            String text = primitive.getAsString().strip();
            try {
                return new BigDecimal(text).stripTrailingZeros();
            } catch (NumberFormatException e) {
                return text;
            }
        }
        return value;
    }

    private void publish(String channelUid, State state) {
        states.put(channelUid, state);
        updateState(channelUid, state);
    }

    private void updateBridgeStatus(ThingStatus bridgeStatus) {
        if (bridgeStatus == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            scheduleProperties();
        } else {
            cancelProperties();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Called by the account after the telematic data of this vehicle was refreshed via the REST API.
     */
    void refreshSucceeded() {
        if (!disposed && getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Called by the account if the telematic data of this vehicle could not be refreshed via the REST API.
     */
    void refreshFailed(@Nullable String reason) {
        if (!disposed) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        }
    }

    private synchronized void scheduleProperties() {
        ScheduledFuture<?> job = propertiesJob;
        if ((job == null || job.isDone()) && !getThing().getProperties().containsKey(PROPERTY_MODEL)) {
            propertiesJob = scheduler.schedule(this::retrieveProperties, 0, TimeUnit.SECONDS);
        }
    }

    private synchronized void cancelProperties() {
        ScheduledFuture<?> job = propertiesJob;
        if (job != null) {
            job.cancel(true);
            propertiesJob = null;
        }
    }

    private void retrieveProperties() {
        BMWCarDataAccountHandler account = getAccountHandler();
        if (account == null || getThing().getProperties().containsKey(PROPERTY_MODEL)) {
            return;
        }
        BasicData basicData;
        try {
            basicData = account.fetchBasicData(vin);
        } catch (BMWCarDataException e) {
            logger.debug("Failed to retrieve basic vehicle data: {}", e.getMessage());
            return;
        }
        if (disposed) {
            return;
        }
        Map<String, String> properties = new HashMap<>(editProperties());
        putIfPresent(properties, PROPERTY_BRAND, basicData.brand);
        String model = basicData.modelName != null ? basicData.modelName : basicData.series;
        // the model property marks the properties as retrieved, so it is always set
        properties.put(PROPERTY_MODEL, model != null ? model : "");
        putIfPresent(properties, PROPERTY_SERIES, basicData.series);
        putIfPresent(properties, PROPERTY_DRIVE_TRAIN, basicData.driveTrain);
        putIfPresent(properties, PROPERTY_PROPULSION_TYPE, basicData.propulsionType);
        putIfPresent(properties, PROPERTY_COLOR, basicData.colourDescription);
        putIfPresent(properties, PROPERTY_CONSTRUCTION_DATE, basicData.constructionDate);
        putIfPresent(properties, PROPERTY_HAS_SUNROOF, Objects.toString(basicData.hasSunRoof, null));
        putIfPresent(properties, PROPERTY_HAS_NAVIGATION, Objects.toString(basicData.hasNavi, null));
        updateProperties(properties);
        removeIrrelevantChannels(basicData);
    }

    /**
     * Removes the channels that do not apply to the vehicle: those of a drive it does not have, of the sunroof and of
     * the destination guidance of the navigation system. Equipment is only treated as missing when BMW explicitly
     * reports so. The channels are removed once, together with retrieving the properties; recreate the Thing to get
     * them back.
     */
    private void removeIrrelevantChannels(BasicData basicData) {
        // BMW does not document the values, so that users can report the ones of their vehicles
        logger.debug("Vehicle reports drive train {} and propulsion type {}", basicData.driveTrain,
                basicData.propulsionType);
        List<Channel> channels = irrelevantChannelIds(basicData).stream()
                .map(id -> getThing().getChannel(new ChannelUID(getThing().getUID(), id))).filter(Objects::nonNull)
                .map(Objects::requireNonNull).toList();
        if (!channels.isEmpty() && !disposed) {
            logger.debug("Removing {} channels that do not apply to the vehicle", channels.size());
            // dynamic channels may be created concurrently, under the same lock
            synchronized (dataLock) {
                updateThing(editThing().withoutChannels(channels).build());
            }
        }
    }

    /**
     * @param basicData the basic data of the vehicle
     * @return the ids of the channels that do not apply to the vehicle
     */
    static List<String> irrelevantChannelIds(BasicData basicData) {
        boolean noSunroof = Boolean.FALSE.equals(basicData.hasSunRoof);
        boolean noNavigation = Boolean.FALSE.equals(basicData.hasNavi);
        Set<Drive> drives = drives(basicData);
        List<String> ids = new ArrayList<>();
        for (VehicleChannel channel : VehicleChannel.values()) {
            // the location comes from the telematics unit, which needs it for eCall, not from the navigation system
            boolean irrelevant = (channel.getDrive() != Drive.ANY && !drives.contains(channel.getDrive()))
                    || (noSunroof && channel == VehicleChannel.SUNROOF)
                    || (noNavigation && channel.getDescriptor().startsWith(DESTINATION_PREFIX));
            String channelUid = channel.getChannelUid();
            if (irrelevant && channelUid != null) {
                ids.add(channelUid);
                String detailChannelUid = channel.getDetailChannelUid();
                if (detailChannelUid != null) {
                    ids.add(detailChannelUid);
                }
            }
        }
        return ids;
    }

    /**
     * Derives the drives of the vehicle from its basic data. BMW does not document the values, so only those seen on
     * real vehicles are recognized: the drive trains {@code BEV}, {@code PHEV} and {@code CONV}, and the propulsion
     * type {@code EL} when the drive train is missing. Any other value keeps all channels; the propulsion type of a
     * combustion vehicle is its fuel, e.g. {@code BE}.
     */
    static Set<Drive> drives(BasicData basicData) {
        String driveTrain = normalize(basicData.driveTrain);
        @Nullable
        Set<Drive> drives = switch (driveTrain) {
            case "BEV" -> EnumSet.of(Drive.ELECTRIC);
            case "PHEV" -> EnumSet.of(Drive.ELECTRIC, Drive.COMBUSTION);
            case "CONV" -> EnumSet.of(Drive.COMBUSTION);
            default -> null;
        };
        if (drives == null && driveTrain.isEmpty() && "EL".equals(normalize(basicData.propulsionType))) {
            drives = EnumSet.of(Drive.ELECTRIC);
        }
        return drives != null ? drives : EnumSet.of(Drive.ELECTRIC, Drive.COMBUSTION);
    }

    private static String normalize(@Nullable String value) {
        return value == null ? "" : value.strip().toUpperCase(Locale.ROOT);
    }

    private static void putIfPresent(Map<String, String> properties, String key, @Nullable String value) {
        if (value != null && !value.isBlank()) {
            properties.put(key, value);
        }
    }

    private @Nullable BMWCarDataAccountHandler getAccountHandler() {
        Bridge bridge = getBridge();
        return bridge != null && bridge.getHandler() instanceof BMWCarDataAccountHandler handler ? handler : null;
    }

    private static @Nullable Instant parseTimestamp(@Nullable String timestamp) {
        if (timestamp == null) {
            return null;
        }
        try {
            return Instant.parse(timestamp);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
