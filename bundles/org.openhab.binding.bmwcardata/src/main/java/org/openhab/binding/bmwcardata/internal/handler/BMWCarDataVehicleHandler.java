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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataApi;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataAuthException;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataException;
import org.openhab.binding.bmwcardata.internal.api.dto.BasicData;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.binding.bmwcardata.internal.config.VehicleConfiguration;
import org.openhab.binding.bmwcardata.internal.handler.VehicleChannel.Drive;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * The {@link BMWCarDataVehicleHandler} publishes the telematic data of a vehicle, which is either pushed by the
 * stream of the account or polled from the REST API.
 *
 * @author Michael Weger - Initial contribution
 * @author Mark Herwege - Filtering of copies and removal of channels that do not apply
 */
@NonNullByDefault
public class BMWCarDataVehicleHandler extends BaseThingHandler {

    private static final long RETRY_SECONDS = 15 * 60;
    // their group types are named <group>-group
    private static final List<String> PREDEFINED_GROUPS = List.of(GROUP_STATUS, GROUP_RANGE, GROUP_CHARGE,
            GROUP_BATTERY, GROUP_CLIMATE, GROUP_NAVIGATION, GROUP_TRIP, GROUP_TYRES, GROUP_SECURITY, GROUP_SERVICE,
            GROUP_STATISTICS);
    private static final List<String> BASIC_DATA_PROPERTIES = List.of(PROPERTY_VIN, PROPERTY_BRAND, PROPERTY_MODEL,
            PROPERTY_SERIES, PROPERTY_DRIVE_TRAIN, PROPERTY_PROPULSION_TYPE, PROPERTY_COLOR, PROPERTY_CONSTRUCTION_DATE,
            PROPERTY_HAS_SUNROOF, PROPERTY_HAS_NAVIGATION, PROPERTY_TELEMATICS_CAPABLE, PROPERTY_MODEL_KEY,
            PROPERTY_SERIES_DEVT, PROPERTY_BODY_TYPE, PROPERTY_NUMBER_OF_DOORS, PROPERTY_STEERING, PROPERTY_ENGINE,
            PROPERTY_HEAD_UNIT, PROPERTY_PU_STEP, PROPERTY_CHARGING_MODES, PROPERTY_HVS_MAX_ENERGY,
            PROPERTY_REESS_CAPACITY, PROPERTY_SIM_STATUS, PROPERTY_COUNTRY_CODE);

    private final Logger logger = LoggerFactory.getLogger(BMWCarDataVehicleHandler.class);

    // compared by the jobs to detect a change of the VIN while they run
    private volatile String vin = "";
    private boolean dynamicChannels = true;
    // held while publishing, so that a value accepted as newer by the REST refresh or the stream is not published
    // after a value accepted later
    private final Object dataLock = new Object();
    private final ScheduledExecutorService jobScheduler;
    private @Nullable ScheduledFuture<?> propertiesJob;
    private @Nullable ScheduledFuture<?> imageJob;
    // answers REFRESH commands without spending API requests
    private final Map<String, State> states = new ConcurrentHashMap<>();
    // the stream delivers many messages twice, and a refresh after the stream delivers the same values again; guarded
    // by dataLock
    private final Map<String, Reading> readings = new HashMap<>();
    // once the vehicle reports isPlugged, the plug state is no longer derived from the charging port status
    private volatile boolean pluggedReported;
    private volatile boolean disposed;
    // the basic data arriving later must not replace the status of a failed refresh
    private volatile boolean refreshFailing;
    // guarded by dataLock
    private @Nullable BigDecimal latitude;
    private @Nullable BigDecimal longitude;
    private @Nullable BigDecimal altitude;
    private @Nullable Instant lastUpdate;
    private @Nullable Instant doorLockTimestamp;
    // their fallback data points no longer update the channel; guarded by dataLock
    private final Set<VehicleChannel> reportedChannels = EnumSet.noneOf(VehicleChannel.class);

    public BMWCarDataVehicleHandler(Thing thing) {
        this(thing, null);
    }

    /**
     * @param jobScheduler runs the jobs of the handler, {@code null} for the scheduler of the base class
     */
    BMWCarDataVehicleHandler(Thing thing, @Nullable ScheduledExecutorService jobScheduler) {
        super(thing);
        this.jobScheduler = jobScheduler != null ? jobScheduler : scheduler;
    }

    public String getVin() {
        return vin;
    }

    boolean isDisposed() {
        return disposed;
    }

    @Override
    public void initialize() {
        disposed = false;
        refreshFailing = false;
        VehicleConfiguration config = getConfigAs(VehicleConfiguration.class);
        String previousVin = vin;
        vin = config.vin.trim().toUpperCase(Locale.ROOT);
        if (!vin.equals(previousVin)) {
            // a configuration change initializes the same handler again
            clearVehicleData();
        }
        dynamicChannels = config.dynamicChannels;
        if (!BMWCarDataApi.isValidVin(vin)) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.vin");
            return;
        }
        Bridge bridge = getBridge();
        updateBridgeStatus(bridge == null ? ThingStatus.OFFLINE : bridge.getStatus());
    }

    @Override
    public void dispose() {
        disposed = true;
        cancelProperties();
        cancelImage();
        BMWCarDataAccountHandler account = getAccountHandler();
        if (account != null) {
            account.vehicleDisposed(this);
        }
        super.dispose();
    }

    private void clearVehicleData() {
        synchronized (dataLock) {
            states.clear();
            readings.clear();
            reportedChannels.clear();
            latitude = null;
            longitude = null;
            altitude = null;
            lastUpdate = null;
            doorLockTimestamp = null;
            pluggedReported = false;
        }
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
                    return;
                }
            }
            // also sent when an item is linked to the channel
            if (CHANNEL_IMAGE.equals(channelUID.getId())) {
                scheduleImage();
            }
        }
        // all channels are read-only
    }

    /**
     * Publishes the values of the given descriptors.
     *
     * @param requestedVin the VIN the data was received for, as the handler may have been initialized again with
     *            another VIN while it was requested
     */
    public void updateTelematicData(String requestedVin, Map<String, TelematicValue> data) {
        if (disposed) {
            return;
        }
        synchronized (dataLock) {
            // checked under the lock, so that the data of another vehicle is cleared after it has been published
            if (!requestedVin.equals(vin)) {
                return;
            }
            publishTelematicData(data);
        }
        // data received from the stream proves the vehicle reachable again after a failed poll
        if (getThing().getStatus() == ThingStatus.OFFLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR
                && hasBasicData()) {
            refreshFailing = false;
            goOnline();
        }
    }

    private void publishTelematicData(Map<String, TelematicValue> data) {
        BigDecimal newLatitude = null;
        BigDecimal newLongitude = null;
        BigDecimal newAltitude = null;
        // a coordinate reported as unavailable, e.g. INVALID, unlike one the update does not contain
        boolean latitudeUndefined = false;
        boolean longitudeUndefined = false;
        boolean altitudeUndefined = false;
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
            VehicleChannel primary = channel.getPrimary();
            if (primary != null && reportedChannels.contains(primary)) {
                continue;
            }
            // an undefined value, e.g. INVALID, does not make a data point reported, so its fallback still applies
            if (state != UnDefType.UNDEF) {
                reportedChannels.add(channel);
            }
            if (channel == VehicleChannel.DOOR_LOCK || channel == VehicleChannel.DOOR_STATUS) {
                // both descriptors update the door lock: an older value of one must not replace a newer of the other
                Instant previousDoorLock = doorLockTimestamp;
                if (timestamp != null && previousDoorLock != null && timestamp.isBefore(previousDoorLock)) {
                    continue;
                }
                if (timestamp != null) {
                    doorLockTimestamp = timestamp;
                }
            }
            switch (channel) {
                case LATITUDE -> {
                    if (state instanceof DecimalType decimal) {
                        newLatitude = decimal.toBigDecimal();
                    } else {
                        latitudeUndefined = true;
                    }
                }
                case LONGITUDE -> {
                    if (state instanceof DecimalType decimal) {
                        newLongitude = decimal.toBigDecimal();
                    } else {
                        longitudeUndefined = true;
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
                    if (channel == VehicleChannel.ALTITUDE) {
                        QuantityType<?> metres = state instanceof QuantityType<?> altitudeState
                                ? altitudeState.toUnit(SIUnits.METRE)
                                : null;
                        if (metres != null) {
                            newAltitude = metres.toBigDecimal();
                        } else {
                            altitudeUndefined = true;
                        }
                    }
                }
            }
        }

        if (newAltitude != null) {
            altitude = newAltitude;
        } else if (altitudeUndefined) {
            altitude = null;
        }
        // an unavailable coordinate is forgotten, so that the other one is not combined with an outdated value
        if (newLatitude != null || newLongitude != null || latitudeUndefined || longitudeUndefined
                || altitudeUndefined) {
            if (newLatitude != null || latitudeUndefined) {
                latitude = newLatitude;
            }
            if (newLongitude != null || longitudeUndefined) {
                longitude = newLongitude;
            }
            BigDecimal lat = latitude;
            BigDecimal lon = longitude;
            BigDecimal alt = altitude;
            if (lat != null && lon != null) {
                publish(CHANNEL_GPS,
                        alt != null ? new PointType(new DecimalType(lat), new DecimalType(lon), new DecimalType(alt))
                                : new PointType(new DecimalType(lat), new DecimalType(lon)));
            } else if (latitudeUndefined || longitudeUndefined) {
                publish(CHANNEL_GPS, UnDefType.UNDEF);
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
     * Records the reading unless it is older than the last one of the descriptor or a copy of it.
     */
    private boolean isNewer(String descriptor, Reading reading) {
        Reading previous = readings.get(descriptor);
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

    /**
     * Sets the vehicle ONLINE, once its channels are final, so that the account reads its current values.
     */
    private void goOnline() {
        updateStatus(ThingStatus.ONLINE);
        BMWCarDataAccountHandler account = getAccountHandler();
        if (account != null) {
            account.vehicleOnline(this);
        }
    }

    private void updateBridgeStatus(ThingStatus bridgeStatus) {
        if (bridgeStatus == ThingStatus.ONLINE) {
            if (hasBasicData()) {
                // e.g. after an update of the binding with other channels
                BMWCarDataAccountHandler account = getAccountHandler();
                BasicData basicData = account != null ? account.getCachedBasicData(vin) : null;
                if (basicData != null) {
                    reconcileChannels(basicData);
                }
                goOnline();
            } else {
                updateStatus(ThingStatus.UNKNOWN);
                scheduleProperties(0);
            }
            scheduleImage();
        } else {
            cancelProperties();
            cancelImage();
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Called by the account after the telematic data of this vehicle was refreshed via the REST API.
     *
     * @param requestedVin the VIN that was refreshed
     */
    void refreshSucceeded(String requestedVin) {
        if (!requestedVin.equals(vin)) {
            return;
        }
        refreshFailing = false;
        if (!disposed && getThing().getStatus() != ThingStatus.ONLINE && hasBasicData()) {
            goOnline();
        }
    }

    /**
     * Called by the account if the telematic data of this vehicle could not be refreshed via the REST API.
     *
     * @param requestedVin the VIN that was refreshed
     */
    void refreshFailed(String requestedVin, BMWCarDataException e) {
        if (disposed || !requestedVin.equals(vin)) {
            return;
        }
        refreshFailing = true;
        if (e.isVinNotPermitted()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.vin-not-permitted");
        } else if (BMWCarDataException.ERROR_CONTAINER_LIMIT.equals(e.getErrorId())) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.comm-error.container-limit");
        } else {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.refresh-failed [\"" + e.getShortDescription() + "\"]");
        }
    }

    /**
     * @return whether the basic data of the configured vehicle has been retrieved; the model property marks it, and
     *         the VIN property tells whose it is, so that it is retrieved again after the VIN was changed, as well as
     *         for Things of binding versions that did not set the VIN property
     */
    private boolean hasBasicData() {
        Map<String, String> properties = getThing().getProperties();
        return properties.containsKey(PROPERTY_MODEL) && vin.equals(properties.get(PROPERTY_VIN));
    }

    private synchronized void scheduleProperties(long delaySeconds) {
        ScheduledFuture<?> job = propertiesJob;
        if ((job == null || job.isDone()) && !hasBasicData()) {
            propertiesJob = jobScheduler.schedule(this::retrieveProperties, delaySeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * Retrieves the basic data again later; called by the running job, which is therefore replaced.
     */
    private synchronized void schedulePropertiesRetry() {
        if (!disposed) {
            propertiesJob = jobScheduler.schedule(this::retrieveProperties, RETRY_SECONDS, TimeUnit.SECONDS);
        }
    }

    private synchronized void cancelProperties() {
        ScheduledFuture<?> job = propertiesJob;
        if (job != null) {
            job.cancel(true);
            propertiesJob = null;
        }
    }

    /**
     * Retrieves the image of the vehicle, only if an item is linked to its channel, as it costs a request.
     */
    private synchronized void scheduleImage() {
        ScheduledFuture<?> job = imageJob;
        if ((job == null || job.isDone()) && !states.containsKey(CHANNEL_IMAGE) && isLinked(CHANNEL_IMAGE)) {
            imageJob = jobScheduler.schedule(this::retrieveImage, 0, TimeUnit.SECONDS);
        }
    }

    private synchronized void cancelImage() {
        ScheduledFuture<?> job = imageJob;
        if (job != null) {
            job.cancel(true);
            imageJob = null;
        }
    }

    /**
     * Retrieves the image again later; called by the running job, which is therefore replaced.
     */
    private synchronized void scheduleImageRetry() {
        if (!disposed) {
            imageJob = jobScheduler.schedule(this::retrieveImage, RETRY_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void retrieveImage() {
        BMWCarDataAccountHandler account = getAccountHandler();
        // the handler may be initialized again with another VIN while the image is retrieved
        String requestedVin = vin;
        if (account == null || states.containsKey(CHANNEL_IMAGE) || !isLinked(CHANNEL_IMAGE)) {
            return;
        }
        RawType image;
        try {
            image = account.fetchVehicleImage(requestedVin);
        } catch (BMWCarDataAuthException e) {
            logger.debug("Failed to retrieve the vehicle image: {}", e.getMessage());
            if (requestedVin.equals(vin)) {
                // the account renews the rejected token
                scheduleImageRetry();
            }
            return;
        } catch (BMWCarDataException e) {
            logger.debug("Failed to retrieve the vehicle image: {}", e.getMessage());
            // each retry costs a request, e.g. a VIN without permission (CU-104) or image (CU-126) never succeeds
            if ((e.isTransient() || e.isRateLimited()) && requestedVin.equals(vin)) {
                scheduleImageRetry();
            }
            return;
        }
        if (disposed) {
            return;
        }
        synchronized (dataLock) {
            if (requestedVin.equals(vin)) {
                publish(CHANNEL_IMAGE, image);
            }
        }
    }

    private void retrieveProperties() {
        BMWCarDataAccountHandler account = getAccountHandler();
        // the handler may be initialized again with another VIN while the basic data is retrieved
        String requestedVin = vin;
        if (account == null || hasBasicData()) {
            return;
        }
        BasicData basicData;
        try {
            basicData = account.fetchBasicData(requestedVin);
        } catch (BMWCarDataException e) {
            logger.debug("Failed to retrieve basic vehicle data: {}", e.getMessage());
            if (disposed || !requestedVin.equals(vin)) {
                return;
            }
            if (e.isVinNotPermitted()) {
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.vin-not-permitted");
                return;
            }
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                    e.isRateLimited() ? "@text/offline.comm-error.basic-data-budget"
                            : "@text/offline.comm-error.basic-data-failed [\"" + e.getShortDescription() + "\"]");
            schedulePropertiesRetry();
            return;
        }
        if (disposed || !requestedVin.equals(vin)) {
            return;
        }
        Map<String, String> properties = new HashMap<>(editProperties());
        // those of the previous vehicle, if the VIN was changed
        BASIC_DATA_PROPERTIES.forEach(properties::remove);
        properties.put(PROPERTY_VIN, requestedVin);
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
        putIfPresent(properties, PROPERTY_TELEMATICS_CAPABLE, Objects.toString(basicData.isTelematicsCapable, null));
        putIfPresent(properties, PROPERTY_MODEL_KEY, basicData.modelKey);
        putIfPresent(properties, PROPERTY_SERIES_DEVT, basicData.seriesDevt);
        putIfPresent(properties, PROPERTY_BODY_TYPE, basicData.bodyType);
        putIfPresent(properties, PROPERTY_NUMBER_OF_DOORS, Objects.toString(basicData.numberOfDoors, null));
        putIfPresent(properties, PROPERTY_STEERING, basicData.steering);
        putIfPresent(properties, PROPERTY_ENGINE, basicData.engine);
        putIfPresent(properties, PROPERTY_HEAD_UNIT, basicData.headUnit);
        putIfPresent(properties, PROPERTY_PU_STEP, basicData.puStep);
        List<String> chargingModes = basicData.chargingModes;
        putIfPresent(properties, PROPERTY_CHARGING_MODES,
                chargingModes != null ? String.join(", ", chargingModes) : null);
        putIfPresent(properties, PROPERTY_HVS_MAX_ENERGY, basicData.hvsMaxEnergyAbsolute);
        putIfPresent(properties, PROPERTY_REESS_CAPACITY, basicData.reessNominalCapacityGross);
        putIfPresent(properties, PROPERTY_SIM_STATUS, basicData.simStatus);
        putIfPresent(properties, PROPERTY_COUNTRY_CODE, basicData.countryCode);
        updateProperties(properties);
        reconcileChannels(basicData);
        Bridge bridge = getBridge();
        // a refresh that failed meanwhile keeps its status until a refresh or stream data succeeds
        if (!disposed && !refreshFailing && bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            goOnline();
        }
    }

    /**
     * Removes the predefined channels that do not apply to the vehicle and adds those that apply but are missing,
     * e.g. after the VIN was changed to another vehicle. The dynamic channels are left alone.
     */
    private void reconcileChannels(BasicData basicData) {
        // BMW does not document the values, so that users can report the ones of their vehicles
        logger.debug("Vehicle reports drive train {} and propulsion type {}", basicData.driveTrain,
                basicData.propulsionType);
        ThingHandlerCallback callback = getCallback();
        if (callback == null) {
            return;
        }
        Set<String> irrelevant = new HashSet<>(irrelevantChannelIds(basicData));
        ThingUID thingUID = getThing().getUID();
        List<Channel> defined = new ArrayList<>();
        for (String group : PREDEFINED_GROUPS) {
            for (ChannelBuilder builder : callback.createChannelBuilders(new ChannelGroupUID(thingUID, group),
                    new ChannelGroupTypeUID(BINDING_ID, group + "-group"))) {
                defined.add(builder.build());
            }
        }
        // dynamic channels may be created concurrently, under the same lock
        synchronized (dataLock) {
            if (disposed) {
                return;
            }
            Thing thing = getThing();
            List<Channel> unsupported = thing.getChannels().stream()
                    .filter(channel -> irrelevant.contains(channel.getUID().getId())).toList();
            List<Channel> missing = defined.stream().filter(channel -> !irrelevant.contains(channel.getUID().getId())
                    && thing.getChannel(channel.getUID()) == null).toList();
            if (unsupported.isEmpty() && missing.isEmpty()) {
                return;
            }
            logger.debug("Removing {} channels that do not apply to the vehicle and adding {} that do",
                    unsupported.size(), missing.size());
            ThingBuilder builder = editThing().withoutChannels(unsupported);
            missing.forEach(builder::withChannel);
            updateThing(builder.build());
        }
    }

    /**
     * @param basicData the basic data of the vehicle
     * @return the ids of the channels that do not apply to the vehicle
     */
    static List<String> irrelevantChannelIds(BasicData basicData) {
        boolean noSunroof = Boolean.FALSE.equals(basicData.hasSunRoof);
        // hasNavi is not used: vehicles with a navigation system were seen reporting false
        Set<Drive> drives = drives(basicData);
        List<String> ids = new ArrayList<>();
        for (VehicleChannel channel : VehicleChannel.values()) {
            boolean irrelevant = !channel.getDrive().appliesTo(drives)
                    || (noSunroof && channel == VehicleChannel.SUNROOF);
            String channelUid = channel.getChannelUid();
            // several data points can update the same channel
            if (irrelevant && channelUid != null && !ids.contains(channelUid)) {
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
     * real vehicles are recognized; any other value keeps all channels. The propulsion type of a combustion vehicle is
     * its fuel, e.g. {@code BE}.
     */
    static Set<Drive> drives(BasicData basicData) {
        return switch (normalize(basicData.driveTrain)) {
            case "BEV" -> EnumSet.of(Drive.ELECTRIC);
            case "PHEV" -> EnumSet.of(Drive.ELECTRIC, Drive.COMBUSTION);
            case "CONV" -> EnumSet.of(Drive.COMBUSTION);
            case "" -> "EL".equals(normalize(basicData.propulsionType)) ? EnumSet.of(Drive.ELECTRIC)
                    : EnumSet.of(Drive.ELECTRIC, Drive.COMBUSTION);
            default -> EnumSet.of(Drive.ELECTRIC, Drive.COMBUSTION);
        };
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
