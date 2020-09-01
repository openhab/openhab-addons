/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedCarConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.CarType;
import org.openhab.binding.bmwconnecteddrive.internal.dto.NetworkError;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.AllTrips;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.AllTripsContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.LastTrip;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.LastTripContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Doors;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Position;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatusContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Windows;
import org.openhab.binding.bmwconnecteddrive.internal.handler.RemoteServiceHandler.ExecutionState;
import org.openhab.binding.bmwconnecteddrive.internal.handler.RemoteServiceHandler.RemoteService;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ConnectedCarHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConnectedCarHandler extends ConnectedCarChannelHandler {
    private final Logger logger = LoggerFactory.getLogger(ConnectedCarHandler.class);

    static final Gson GSON = new Gson();

    private Optional<ConnectedDriveProxy> proxy = Optional.empty();
    private Optional<RemoteServiceHandler> remote = Optional.empty();
    private Optional<ConnectedCarConfiguration> configuration = Optional.empty();

    private boolean imperial = false;
    private boolean hasFuel = false;
    private boolean isElectric = false;
    private boolean isHybrid = false;

    private Position currentPosition = new Position();

    StringResponseCallback vehicleStatusCallback = new VehicleStatusCallback();
    StringResponseCallback lastTripCallback = new LastTripCallback();
    StringResponseCallback allTripsCallback = new AllTripsCallback();
    StringResponseCallback chargeProfileCallback = new ChargeProfilesCallback();
    StringResponseCallback rangeMapCallback = new RangeMapCallback();
    ByteResponseCallback imageCallback = new ImageCallback();

    private Optional<String> vehicleStatusCache = Optional.empty();
    private Optional<String> lastTripCache = Optional.empty();
    private Optional<String> allTripsCache = Optional.empty();
    private Optional<String> chargeProfileCache = Optional.empty();
    private Optional<String> rangeMapCache = Optional.empty();

    private Optional<ConnectedDriveBridgeHandler> bridgeHandler = Optional.empty();
    protected Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public ConnectedCarHandler(Thing thing, HttpClient hc, String type, boolean imperial) {
        super(thing);
        this.imperial = imperial;
        hasFuel = type.equals(CarType.CONVENTIONAL.toString()) || type.equals(CarType.PLUGIN_HYBRID.toString())
                || type.equals(CarType.ELECTRIC_REX.toString());
        isElectric = type.equals(CarType.PLUGIN_HYBRID.toString()) || type.equals(CarType.ELECTRIC_REX.toString())
                || type.equals(CarType.ELECTRIC.toString());
        isHybrid = hasFuel && isElectric;
        logger.debug("DriveTrain {} isElectric {} hasFuel {} Imperial {}", type, isElectric, hasFuel, imperial);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String group = channelUID.getGroupId();

        // Refresh of Channels with cached values
        if (command instanceof RefreshType) {
            if (CHANNEL_GROUP_LAST_TRIP.equals(group)) {
                lastTripCallback.onResponse(lastTripCache);
            } else if (CHANNEL_GROUP_LIFETIME.equals(group)) {
                allTripsCallback.onResponse(allTripsCache);
            } else if (CHANNEL_GROUP_LAST_TRIP.equals(group)) {
                lastTripCallback.onResponse(lastTripCache);
            } else if (CHANNEL_GROUP_LAST_TRIP.equals(group)) {
                lastTripCallback.onResponse(lastTripCache);
            } else if (CHANNEL_GROUP_STATUS.equals(group)) {
                vehicleStatusCallback.onResponse(vehicleStatusCache);
            } else if (CHANNEL_GROUP_CHARGE_PROFILE.equals(group)) {
                vehicleStatusCallback.onResponse(chargeProfileCache);
            } else if (CHANNEL_GROUP_RANGE_MAP.equals(group)) {
                vehicleStatusCallback.onResponse(rangeMapCache);
            }
        } else {
            // Executing Remote Services
            if (CHANNEL_GROUP_REMOTE.equals(channelUID.getGroupId())) {
                logger.info("Remote Command {}", CHANNEL_GROUP_REMOTE);
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        if (remote.isPresent()) {
                            switch (channelUID.getIdWithoutGroup()) {
                                case REMOTE_SERVICE_LIGHT_FLASH:
                                    updateState(remoteLightChannel,
                                            OnOffType.from(remote.get().execute(RemoteService.LIGHT_FLASH)));
                                    break;
                                case REMOTE_SERVICE_AIR_CONDITIONING:
                                    updateState(remoteClimateChannel,
                                            OnOffType.from(remote.get().execute(RemoteService.AIR_CONDITIONING)));
                                    break;
                                case REMOTE_SERVICE_DOOR_LOCK:
                                    updateState(remoteLockChannel,
                                            OnOffType.from(remote.get().execute(RemoteService.DOOR_LOCK)));
                                    break;
                                case REMOTE_SERVICE_DOOR_UNLOCK:
                                    updateState(remoteUnlockChannel,
                                            OnOffType.from(remote.get().execute(RemoteService.DOOR_UNLOCK)));
                                    break;
                                case REMOTE_SERVICE_HORN:
                                    updateState(remoteHornChannel,
                                            OnOffType.from(remote.get().execute(RemoteService.HORN)));
                                    break;
                                case REMOTE_SERVICE_VEHICLE_FINDER:
                                    updateState(remoteFinderChannel,
                                            OnOffType.from(remote.get().execute(RemoteService.VEHICLE_FINDER)));
                                    break;
                            }
                        }
                        updateState(carDataFingerprint, OnOffType.OFF);
                    }
                }
            }

            // Log Troubleshoot data
            if (channelUID.getIdWithoutGroup().equals(CARDATA_FINGERPRINT)) {
                logger.info("Trigger CarData Fingerprint");
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        logger.warn("BMW ConnectedDrive Binding - Car Data Troubleshoot fingerprint - BEGIN");
                        if (vehicleStatusCache.isPresent()) {
                            logger.warn("### Vehicle Status ###");
                            // Anonymous data for VIN and Position
                            VehicleStatusContainer container = GSON.fromJson(vehicleStatusCache.get(),
                                    VehicleStatusContainer.class);
                            VehicleStatus status = container.vehicleStatus;
                            status.vin = ANONYMOUS;
                            if (status.position != null) {
                                status.position.lat = -1;
                                status.position.lon = -1;
                                status.position.heading = -1;
                                logger.warn("{}", GSON.toJson(container));
                            }
                        } else {
                            logger.warn("### Vehicle Status Empty ###");
                        }
                        if (lastTripCache.isPresent()) {
                            logger.warn("### Last Trip ###");
                            logger.warn("{}", lastTripCache.get());
                        } else {
                            logger.warn("### Last Trip Empty ###");
                        }
                        if (allTripsCache.isPresent()) {
                            logger.warn("### All Trips ###");
                            logger.warn("{}", allTripsCache.get());
                        } else {
                            logger.warn("### All Trips Empty ###");
                        }
                        if (isElectric) {
                            if (chargeProfileCache.isPresent()) {
                                logger.warn("### Charge Profile ###");
                                logger.warn("{}", chargeProfileCache.get());
                            } else {
                                logger.warn("### Charge Profile Empty ###");
                            }
                        }
                        if (rangeMapCache.isPresent()) {
                            logger.warn("### Range Map ###");
                            logger.warn("{}", rangeMapCache.get());
                        } else {
                            logger.warn("### Range Map Empty ###");
                        }
                        logger.warn("BMW ConnectedDrive Binding - Car Data Troubleshoot fingerprint - END");
                    }
                    // Switch back to off immediately
                    updateState(channelUID, OnOffType.OFF);
                }
            }
        }
    }

    /**
     * URLs taken from https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/const.py
     *
     * """URLs for different services and error code mapping."""
     *
     * AUTH_URL = 'https://customer.bmwgroup.com/{gcdm_oauth_endpoint}/authenticate'
     * AUTH_URL_LEGACY = 'https://{server}/gcdm/oauth/token'
     * BASE_URL = 'https://{server}/webapi/v1'
     *
     * VEHICLES_URL = BASE_URL + '/user/vehicles'
     * VEHICLE_VIN_URL = VEHICLES_URL + '/{vin}'
     * VEHICLE_STATUS_URL = VEHICLE_VIN_URL + '/status'
     * REMOTE_SERVICE_STATUS_URL = VEHICLE_VIN_URL + '/serviceExecutionStatus?serviceType={service_type}'
     * REMOTE_SERVICE_URL = VEHICLE_VIN_URL + "/executeService"
     * VEHICLE_IMAGE_URL = VEHICLE_VIN_URL + "/image?width={width}&height={height}&view={view}"
     * VEHICLE_POI_URL = VEHICLE_VIN_URL + '/sendpoi'
     *
     * }
     */
    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        configuration = Optional.of(getConfigAs(ConnectedCarConfiguration.class));
        if (configuration.isPresent()) {
            scheduler.execute(() -> {
                Bridge bridge = getBridge();
                if (bridge != null) {
                    BridgeHandler handler = bridge.getHandler();
                    if (handler != null) {
                        bridgeHandler = Optional.of(((ConnectedDriveBridgeHandler) handler));
                        proxy = bridgeHandler.get().getProxy();
                        if (proxy.isPresent()) {
                            remote = Optional.of(proxy.get().getRemoteServiceHandler(this));
                        }
                    } else {
                        logger.warn("Brdige Handler null");
                    }
                } else {
                    logger.warn("Bridge null");
                }

                // Switch all Remote Service Channels Off
                switchRemoteServicesOff();
                updateState(carDataFingerprint, OnOffType.OFF);

                // get Car Image one time at the beginning
                proxy.get().requestImage(configuration.get(), imageCallback);

                // check imperial setting is different to AutoDetect
                if (!UNITS_AUTODETECT.equals(configuration.get().units)) {
                    imperial = UNITS_IMPERIAL.equals(configuration.get().units);
                }

                // start update schedule
                startSchedule(configuration.get().refreshInterval);
            });
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    private void switchRemoteServicesOff() {
        updateState(remoteLightChannel, OnOffType.from(false));
        updateState(remoteFinderChannel, OnOffType.from(false));
        updateState(remoteLockChannel, OnOffType.from(false));
        updateState(remoteUnlockChannel, OnOffType.from(false));
        updateState(remoteHornChannel, OnOffType.from(false));
        updateState(remoteClimateChannel, OnOffType.from(false));
        updateState(remoteStateChannel, OnOffType.from(false));
    }

    private void startSchedule(int interval) {
        if (refreshJob.isPresent()) {
            if (refreshJob.get().isCancelled()) {
                Optional.of(scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES));
            } // else - scheduler is already running!
        } else {
            Optional.of(scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES));
        }
    }

    @Override
    public void dispose() {
        if (refreshJob.isPresent()) {
            refreshJob.get().cancel(true);
        }
    }

    public void getData() {
        if (proxy.isPresent() && configuration.isPresent()) {
            proxy.get().requestVehcileStatus(configuration.get(), vehicleStatusCallback);
            if (isElectric) {
                proxy.get().requestLastTrip(configuration.get(), lastTripCallback);
                proxy.get().requestAllTrips(configuration.get(), allTripsCallback);
                proxy.get().requestChargingProfile(configuration.get(), chargeProfileCallback);
            }
        } else {
            logger.warn("ConnectedDrive Proxy isn't present");
        }
    }

    void requestRangeMap(Position p) {
        // format_string = '%Y-%m-%dT%H:%M:%S'
        // timestamp = datetime.datetime.now().strftime(format_string)
        // params = {
        // 'deviceTime': timestamp,
        // 'dlat': self._vehicle.observer_latitude,
        // 'dlon': self._vehicle.observer_longitude,
        // }
        double diff = Converter.measure(p.lat, p.lon, currentPosition.lat, currentPosition.lon);
        if (diff > 1000) {
            logger.info("Difference between old {} and new Position {} = {}", currentPosition.toString(), p.toString(),
                    diff);
            LocalDateTime ldt = LocalDateTime.now();
            MultiMap<String> dataMap = new MultiMap<String>();
            dataMap.add("deviceTime", ldt.format(Converter.DATE_INPUT_PATTERN));
            dataMap.add("dlat", Float.toString(p.lat));
            dataMap.add("dlon", Float.toString(p.lon));
            if (configuration.isPresent()) {
                proxy.get().requestRangeMap(configuration.get(), dataMap, rangeMapCallback);
            }
        }
        currentPosition = p;
    }

    public void updateRemoteExecutionStatus(String service, String status) {
        updateState(remoteStateChannel, StringType
                .valueOf(Converter.toTitleCase(new StringBuffer(service).append(" ").append(status).toString())));
        if (ExecutionState.EXECUTED.toString().equals(status)) {
            switchRemoteServicesOff();
        }
    }

    public Optional<ConnectedCarConfiguration> getConfiguration() {
        return configuration;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     * Callbacks for ConnectedDrive Portal
     *
     * @author Bernd Weymann
     *
     */
    @NonNullByDefault
    public class ChargeProfilesCallback implements StringResponseCallback {
        @Override
        public void onResponse(Optional<String> content) {
            chargeProfileCache = content;
        }

        /**
         * Store Error Report in cache variable. Via Fingerprint Channel error is logged and Issue can be raised
         */
        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            chargeProfileCache = Optional.of(GSON.toJson(error));
        }
    }

    @NonNullByDefault
    public class RangeMapCallback implements StringResponseCallback {
        @Override
        public void onResponse(Optional<String> content) {
            rangeMapCache = content;
        }

        /**
         * Store Error Report in cache variable. Via Fingerprint Channel error is logged and Issue can be raised
         */
        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            rangeMapCache = Optional.of(GSON.toJson(error));
        }
    }

    @NonNullByDefault
    public class ImageCallback implements ByteResponseCallback {
        @Override
        public void onResponse(Optional<byte[]> content) {
            if (content.isPresent()) {
                String contentType = HttpUtil.guessContentTypeFromData(content.get());
                updateState(imageChannel, new RawType(content.get(), contentType));
            }
        }

        /**
         * Store Error Report in cache variable. Via Fingerprint Channel error is logged and Issue can be raised
         */
        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
        }
    }

    @NonNullByDefault
    public class AllTripsCallback implements StringResponseCallback {
        @Override
        public void onResponse(Optional<String> content) {
            if (content.isPresent()) {
                allTripsCache = content;
                AllTripsContainer at = GSON.fromJson(content.get(), AllTripsContainer.class);
                AllTrips c = at.allTrips;
                if (c == null) {
                    return;
                }
                updateState(lifeTimeCumulatedDrivenDistance, QuantityType
                        .valueOf(Converter.round(c.totalElectricDistance.userTotal), MetricPrefix.KILO(SIUnits.METRE)));
                updateState(lifeTimeSingleLongestDistance, QuantityType
                        .valueOf(Converter.round(c.chargecycleRange.userHigh), MetricPrefix.KILO(SIUnits.METRE)));
                updateState(lifeTimeAverageConsumption, QuantityType
                        .valueOf(Converter.round(c.avgElectricConsumption.userAverage), SmartHomeUnits.KILOWATT_HOUR));
                updateState(lifeTimeAverageRecuperation, QuantityType
                        .valueOf(Converter.round(c.avgRecuperation.userAverage), SmartHomeUnits.KILOWATT_HOUR));
                updateState(tripDistanceSinceCharging, QuantityType.valueOf(
                        Converter.round(c.chargecycleRange.userCurrentChargeCycle), MetricPrefix.KILO(SIUnits.METRE)));
            }
        }

        /**
         * Store Error Report in cache variable. Via Fingerprint Channel error is logged and Issue can be raised
         */
        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            allTripsCache = Optional.of(GSON.toJson(error));
        }
    }

    @NonNullByDefault
    public class LastTripCallback implements StringResponseCallback {
        @Override
        public void onResponse(Optional<String> content) {
            if (content.isPresent()) {
                lastTripCache = content;
                LastTripContainer lt = GSON.fromJson(content.get(), LastTripContainer.class);
                LastTrip trip = lt.lastTrip;
                if (trip == null) {
                    return;
                }
                updateState(tripDistance,
                        QuantityType.valueOf(Converter.round(trip.totalDistance), MetricPrefix.KILO(SIUnits.METRE)));
                // updateState(tripDistanceSinceCharging,
                // QuantityType.valueOf(entry.lastTrip, MetricPrefix.KILO(SIUnits.METRE)));
                updateState(tripAvgConsumption, QuantityType.valueOf(Converter.round(trip.avgElectricConsumption),
                        SmartHomeUnits.KILOWATT_HOUR));
                updateState(tripAvgRecuperation,
                        QuantityType.valueOf(Converter.round(trip.avgRecuperation), SmartHomeUnits.KILOWATT_HOUR));
            }
        }

        /**
         * Store Error Report in cache variable. Via Fingerprint Channel error is logged and Issue can be raised
         */
        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            lastTripCache = Optional.of(GSON.toJson(error));
        }
    }

    /**
     * The VehicleStatus is supported by all Car Types so it's used to reflect the Thing Status
     */
    @NonNullByDefault
    public class VehicleStatusCallback implements StringResponseCallback {
        private ThingStatus thingStatus = ThingStatus.UNKNOWN;

        /**
         * Vehicle Satus is supported by all cars so callback result is used to report Thing Status.
         * If valid content is delivered in onResponse Thing goes online while onError Thing goes offline
         *
         * @param status
         * @param detail
         * @param reason
         */
        private void setThingStatus(ThingStatus status, ThingStatusDetail detail, String reason) {
            if (thingStatus != status) {
                updateStatus(status, detail, reason);
            }
        }

        @Override
        public void onResponse(Optional<String> content) {
            if (content.isPresent()) {
                setThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, Constants.EMPTY);
                vehicleStatusCache = content;
                VehicleStatusContainer status = GSON.fromJson(content.get(), VehicleStatusContainer.class);
                VehicleStatus vStatus = status.vehicleStatus;
                if (vStatus == null) {
                    return;
                }
                updateState(lock, StringType.valueOf(Converter.toTitleCase(vStatus.doorLockState)));
                Doors doorState = GSON.fromJson(GSON.toJson(vStatus), Doors.class);
                updateState(doors, StringType.valueOf(VehicleStatus.checkClosed(doorState)));
                Windows windowState = GSON.fromJson(GSON.toJson(vStatus), Windows.class);
                updateState(windows, StringType.valueOf(VehicleStatus.checkClosed(windowState)));
                updateState(checkControl, StringType.valueOf(vStatus.getCheckControl()));
                updateState(service, StringType.valueOf(vStatus.getNextService(imperial)));
                if (isElectric) {
                    updateState(chargingStatus, StringType.valueOf(Converter.toTitleCase(vStatus.chargingStatus)));
                }

                // Range values
                // based on unit of length decide if range shall be reported in km or miles
                if (!imperial) {
                    updateState(mileage, QuantityType.valueOf(vStatus.mileage, MetricPrefix.KILO(SIUnits.METRE)));
                    float totalRange = 0;
                    if (isElectric) {
                        totalRange += vStatus.remainingRangeElectric;
                        updateState(remainingRangeElectric,
                                QuantityType.valueOf(vStatus.remainingRangeElectric, MetricPrefix.KILO(SIUnits.METRE)));
                    }
                    if (hasFuel) {
                        totalRange += vStatus.remainingRangeFuel;
                        updateState(remainingRangeFuel,
                                QuantityType.valueOf(vStatus.remainingRangeFuel, MetricPrefix.KILO(SIUnits.METRE)));
                    }
                    if (isHybrid) {
                        updateState(remainingRangeHybrid,
                                QuantityType.valueOf(Converter.round(totalRange), MetricPrefix.KILO(SIUnits.METRE)));
                    }
                    updateState(rangeRadius, new DecimalType((totalRange) * 1000));
                } else {
                    updateState(mileage, QuantityType.valueOf(vStatus.mileage, ImperialUnits.MILE));
                    float totalRange = 0;
                    if (isElectric) {
                        totalRange += vStatus.remainingRangeElectricMls;
                        updateState(remainingRangeElectric,
                                QuantityType.valueOf(vStatus.remainingRangeElectricMls, ImperialUnits.MILE));
                    }
                    if (hasFuel) {
                        totalRange += vStatus.remainingRangeFuelMls;
                        updateState(remainingRangeFuel,
                                QuantityType.valueOf(vStatus.remainingRangeFuelMls, ImperialUnits.MILE));
                    }
                    if (isHybrid) {
                        updateState(remainingRangeHybrid,
                                QuantityType.valueOf(Converter.round(totalRange), ImperialUnits.MILE));
                    }
                    updateState(rangeRadius, new DecimalType((totalRange) * Constants.MILES_TO_FEET_FACTOR));
                }
                if (isElectric) {
                    updateState(remainingSoc, QuantityType.valueOf(vStatus.chargingLevelHv, SmartHomeUnits.PERCENT));
                }
                if (hasFuel) {
                    updateState(remainingFuel, QuantityType.valueOf(vStatus.remainingFuel, SmartHomeUnits.LITRE));
                }
                // last update Time
                if (vStatus.internalDataTimeUTC != null) {
                    updateState(lastUpdate, new StringType(Converter.getLocalDateTime(vStatus.internalDataTimeUTC)));
                } else {
                    updateState(lastUpdate, new StringType(Converter.getZonedDateTime(vStatus.updateTime)));
                }

                Position p = vStatus.position;
                updateState(latitude, new DecimalType(p.lat));
                updateState(longitude, new DecimalType(p.lon));
                updateState(latlong, StringType.valueOf(p.toString()));
                updateState(heading, QuantityType.valueOf(p.heading, SmartHomeUnits.DEGREE_ANGLE));
                requestRangeMap(p);
            }
        }

        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            vehicleStatusCache = Optional.of(GSON.toJson(error));
            setThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.reason);
        }
    }
}
