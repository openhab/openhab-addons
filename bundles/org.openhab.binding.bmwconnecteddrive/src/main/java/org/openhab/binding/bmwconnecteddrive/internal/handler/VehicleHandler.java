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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bmwconnecteddrive.internal.VehicleConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.dto.DestinationContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.NetworkError;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.ChargeProfile;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.WeeklyPlanner;
import org.openhab.binding.bmwconnecteddrive.internal.dto.compat.VehicleAttributesContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.AllTrips;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.AllTripsContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.LastTrip;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.LastTripContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatusContainer;
import org.openhab.binding.bmwconnecteddrive.internal.handler.RemoteServiceHandler.ExecutionState;
import org.openhab.binding.bmwconnecteddrive.internal.handler.RemoteServiceHandler.RemoteService;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ImageProperties;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VehicleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - contributor
 */
@NonNullByDefault
public class VehicleHandler extends VehicleChannelHandler {
    private boolean legacyMode = false; // switch to legacy API in case of 404 Errors

    private Optional<ConnectedDriveProxy> proxy = Optional.empty();
    private Optional<RemoteServiceHandler> remote = Optional.empty();
    private Optional<VehicleConfiguration> configuration = Optional.empty();
    private Optional<ConnectedDriveBridgeHandler> bridgeHandler = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<List<ResponseCallback>> callbackCounter = Optional.empty();

    private ImageProperties imageProperties = new ImageProperties();
    VehicleStatusCallback vehicleStatusCallback = new VehicleStatusCallback();
    StringResponseCallback oldVehicleStatusCallback = new LegacyVehicleStatusCallback();
    StringResponseCallback lastTripCallback = new LastTripCallback();
    StringResponseCallback allTripsCallback = new AllTripsCallback();
    StringResponseCallback chargeProfileCallback = new ChargeProfilesCallback();
    StringResponseCallback rangeMapCallback = new RangeMapCallback();
    DestinationsCallback destinationCallback = new DestinationsCallback();
    ByteResponseCallback imageCallback = new ImageCallback();

    private Optional<ChargeProfile> chargeProfileEdit = Optional.empty();

    public VehicleHandler(Thing thing, BMWConnectedDriveOptionProvider op, String type, boolean imperial) {
        super(thing, op, type, imperial);
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
            } else if (CHANNEL_GROUP_CHARGE.equals(group)) {
                if (chargeProfileEdit.isEmpty()) {
                    chargeProfileCallback.onResponse(chargeProfileCache);
                    updateChargeProfileVersion(ChargeProfileVersion.REMOTE);
                } else {
                    updateChargeProfile(chargeProfileEdit.get());
                    updateChargeProfileVersion(ChargeProfileVersion.LOCAL);
                }
            } else if (CHANNEL_GROUP_VEHICLE_IMAGE.equals(group)) {
                imageCallback.onResponse(imageCache);
            }
        }

        // Check for Channel Group and corresponding Actions
        if (CHANNEL_GROUP_REMOTE.equals(group)) {
            // Executing Remote Services
            if (command instanceof StringType) {
                String serviceCommand = ((StringType) command).toFullString();
                if (remote.isPresent()) {
                    switch (serviceCommand) {
                        case REMOTE_SERVICE_LIGHT_FLASH:
                            remote.get().execute(RemoteService.LIGHT_FLASH);
                            break;
                        case REMOTE_SERVICE_AIR_CONDITIONING:
                            remote.get().execute(RemoteService.AIR_CONDITIONING);
                            break;
                        case REMOTE_SERVICE_DOOR_LOCK:
                            remote.get().execute(RemoteService.DOOR_LOCK);
                            break;
                        case REMOTE_SERVICE_DOOR_UNLOCK:
                            remote.get().execute(RemoteService.DOOR_UNLOCK);
                            break;
                        case REMOTE_SERVICE_HORN:
                            remote.get().execute(RemoteService.HORN);
                            break;
                        case REMOTE_SERVICE_VEHICLE_FINDER:
                            remote.get().execute(RemoteService.VEHICLE_FINDER);
                            break;
                        default:
                            logger.info("Remote service execution {} unknown", serviceCommand);
                            break;
                    }
                }
            }
        } else if (CHANNEL_GROUP_VEHICLE_IMAGE.equals(group)) {
            // Image Change
            if (configuration.isPresent()) {
                if (command instanceof StringType) {
                    if (channelUID.getIdWithoutGroup().equals(IMAGE_VIEWPORT)) {
                        String newViewport = command.toString();
                        synchronized (imageProperties) {
                            if (!imageProperties.viewport.equals(newViewport)) {
                                imageProperties = new ImageProperties(newViewport, imageProperties.size);
                                imageCache = Optional.empty();
                                proxy.get().requestImage(configuration.get(), imageProperties, imageCallback);
                            }
                        }
                        updateState(imageViewportChannel, StringType.valueOf(newViewport));
                    }
                }
                if (command instanceof DecimalType) {
                    if (command instanceof DecimalType) {
                        int newImageSize = ((DecimalType) command).intValue();
                        if (channelUID.getIdWithoutGroup().equals(IMAGE_SIZE)) {
                            synchronized (imageProperties) {
                                if (imageProperties.size != newImageSize) {
                                    imageProperties = new ImageProperties(imageProperties.viewport, newImageSize);
                                    imageCache = Optional.empty();
                                    proxy.get().requestImage(configuration.get(), imageProperties, imageCallback);
                                }
                            }
                        }
                        updateState(imageSizeChannel, new DecimalType(newImageSize));
                    }
                }
            }
        } else if (CHANNEL_GROUP_DESTINATION.equals(group)) {
            if (command instanceof StringType) {
                int index = Converter.getIndex(command.toFullString());
                if (index != -1) {
                    selectDestination(index);
                } else {
                    logger.debug("Cannot select Destination index {}", command.toFullString());
                }
            }
        } else if (CHANNEL_GROUP_SERVICE.equals(group)) {
            if (command instanceof StringType) {
                int index = Converter.getIndex(command.toFullString());
                if (index != -1) {
                    selectService(index);
                } else {
                    logger.debug("Cannot select Service index {}", command.toFullString());
                }
            }
        } else if (CHANNEL_GROUP_CHECK_CONTROL.equals(group)) {
            if (command instanceof StringType) {
                int index = Converter.getIndex(command.toFullString());
                if (index != -1) {
                    selectCheckControl(index);
                } else {
                    logger.debug("Cannot select CheckControl index {}", command.toFullString());
                }
            }
        } else if (CHANNEL_GROUP_CHARGE.equals(group)) {
            handleChargeProfileCommand(channelUID, command);
        }
    }

    @Override
    public void initialize() {
        callbackCounter = Optional.of(new ArrayList<ResponseCallback>());
        updateStatus(ThingStatus.UNKNOWN);
        configuration = Optional.of(getConfigAs(VehicleConfiguration.class));
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
                        logger.debug("Brdige Handler null");
                    }
                } else {
                    logger.debug("Bridge null");
                }

                // get Image after init with config values
                synchronized (imageProperties) {
                    imageProperties = new ImageProperties(configuration.get().imageViewport,
                            configuration.get().imageSize);
                }
                updateState(imageViewportChannel, StringType.valueOf((configuration.get().imageViewport)));
                updateState(imageSizeChannel, new DecimalType((configuration.get().imageSize)));

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

    private void startSchedule(int interval) {
        if (refreshJob.isPresent()) {
            if (refreshJob.get().isCancelled()) {
                refreshJob = Optional
                        .of(scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES));
            } // else - scheduler is already running!
        } else {
            refreshJob = Optional.of(scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES));
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
            if (!legacyMode) {
                proxy.get().requestVehcileStatus(configuration.get(), vehicleStatusCallback);
            } else {
                proxy.get().requestLegacyVehcileStatus(configuration.get(), oldVehicleStatusCallback);
            }
            addCallback(vehicleStatusCallback);
            if (isSupported(Constants.STATISTICS)) {
                proxy.get().requestLastTrip(configuration.get(), lastTripCallback);
                proxy.get().requestAllTrips(configuration.get(), allTripsCallback);
                addCallback(lastTripCallback);
                addCallback(allTripsCallback);
            }
            if (isSupported(Constants.LAST_DESTINATIONS)) {
                proxy.get().requestDestinations(configuration.get(), destinationCallback);
                addCallback(destinationCallback);
            }
            if (isElectric) {
                proxy.get().requestChargingProfile(configuration.get(), chargeProfileCallback);
                addCallback(chargeProfileCallback);
            }
            synchronized (imageProperties) {
                if (!imageCache.isPresent() && !imageProperties.failLimitReached()) {
                    proxy.get().requestImage(configuration.get(), imageProperties, imageCallback);
                    addCallback(imageCallback);
                }
            }
        } else {
            logger.warn("ConnectedDrive Proxy isn't present");
        }
    }

    private synchronized void addCallback(ResponseCallback rc) {
        if (callbackCounter.isPresent()) {
            callbackCounter.get().add(rc);
        }
    }

    private synchronized void removeCallback(ResponseCallback rc) {
        if (callbackCounter.isPresent()) {
            callbackCounter.get().remove(rc);
            // all necessary callbacks received => print and set to empty
            if (callbackCounter.get().isEmpty()) {
                logFingerPrint();
                callbackCounter = Optional.empty();
            }
        }
    }

    private void logFingerPrint() {
        logger.debug("###### BMW ConnectedDrive Binding - Vehicle Troubleshoot Fingerprint Data - BEGIN ######");
        logger.debug("### Discovery Result ###");
        logger.debug("{}", bridgeHandler.get().getDiscoveryFingerprint());
        if (vehicleStatusCache.isPresent()) {
            logger.debug("### Vehicle Status ###");

            // Anonymous data for VIN and Position
            VehicleStatusContainer container = Converter.getGson().fromJson(vehicleStatusCache.get(),
                    VehicleStatusContainer.class);
            if (container != null) {
                VehicleStatus status = container.vehicleStatus;
                if (status != null) {
                    status.vin = Constants.ANONYMOUS;
                    if (status.position != null) {
                        status.position.lat = -1;
                        status.position.lon = -1;
                        status.position.heading = -1;
                    }
                }
            }
            logger.debug("{}", Converter.getGson().toJson(container));
        } else {
            logger.debug("### Vehicle Status Empty ###");
        }
        if (lastTripCache.isPresent()) {
            logger.debug("### Last Trip ###");
            logger.debug("{}", lastTripCache.get().replaceAll(configuration.get().vin, Constants.ANONYMOUS));
        } else {
            logger.debug("### Last Trip Empty ###");
        }
        if (allTripsCache.isPresent()) {
            logger.debug("### All Trips ###");
            logger.debug("{}", allTripsCache.get().replaceAll(configuration.get().vin, Constants.ANONYMOUS));
        } else {
            logger.debug("### All Trips Empty ###");
        }
        if (isElectric) {
            if (chargeProfileCache.isPresent()) {
                logger.debug("### Charge Profile ###");
                logger.debug("{}", chargeProfileCache.get().replaceAll(configuration.get().vin, Constants.ANONYMOUS));
            } else {
                logger.debug("### Charge Profile Empty ###");
            }
        }
        if (destinationCache.isPresent()) {
            logger.debug("### Charge Profile ###");
            DestinationContainer container = Converter.getGson().fromJson(destinationCache.get(),
                    DestinationContainer.class);
            if (container != null) {
                if (container.destinations != null) {
                    container.destinations.forEach(entry -> {
                        entry.lat = 0;
                        entry.lon = 0;
                        entry.city = Constants.ANONYMOUS;
                        entry.street = Constants.ANONYMOUS;
                        entry.streetNumber = Constants.ANONYMOUS;
                        entry.country = Constants.ANONYMOUS;
                    });
                    logger.debug("{}", Converter.getGson().toJson(container));
                }
            } else {
                logger.debug("### Destinations Empty ###");
            }
        } else {
            logger.debug("### Charge Profile Empty ###");
        }
        if (rangeMapCache.isPresent()) {
            logger.debug("### Range Map ###");
            logger.debug("{}", rangeMapCache.get().replaceAll(configuration.get().vin, Constants.ANONYMOUS));
        } else {
            logger.debug("### Range Map Empty ###");
        }
        logger.debug("###### BMW ConnectedDrive Binding - Vehicle Troubleshoot Fingerprint Data - END ######");
    }

    /**
     * Don't stress ConnectedDrive with unnecessary requests. One call at the beginning is done to check the response.
     * After cache has e.g. a proper error response it will be shown in the fingerprint
     *
     * @return
     */
    private boolean isSupported(String service) {
        String services = thing.getProperties().get(Constants.SERVICES_SUPPORTED);
        if (services != null) {
            if (services.contains(service)) {
                return true;
            }
        }
        // if cache is empty give it a try one time to collected Troubleshoot data
        if (!lastTripCache.isPresent() || !allTripsCache.isPresent() || !destinationCache.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    public void updateRemoteExecutionStatus(String service, String status) {
        if (service.equals(RemoteService.CHARGING_CONTROL.toString())
                && status.equals(ExecutionState.EXECUTED.toString())) {
            saveChargeProfileEdit();
        }
        updateState(remoteStateChannel, StringType
                .valueOf(Converter.toTitleCase(new StringBuilder(service).append(" ").append(status).toString())));
    }

    public Optional<VehicleConfiguration> getConfiguration() {
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
    @NonNullByDefault({})
    public class ChargeProfilesCallback implements StringResponseCallback {
        @Override
        public void onResponse(Optional<String> content) {
            chargeProfileCache = content;
            if (content.isPresent() && chargeProfileEdit.isEmpty()) {
                ChargeProfile cp = Converter.getGson().fromJson(content.get(), ChargeProfile.class);
                if (cp != null) {
                    updateChargeProfile(cp);
                }
            }
            removeCallback(this);
        }

        /**
         * Store Error Report in cache variable. Via Fingerprint Channel error is logged and Issue can be raised
         */
        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            chargeProfileCache = Optional.of(Converter.getGson().toJson(error));
            removeCallback(this);
        }
    }

    @NonNullByDefault({})
    public class RangeMapCallback implements StringResponseCallback {
        @Override
        public void onResponse(Optional<String> content) {
            rangeMapCache = content;
            removeCallback(this);
        }

        /**
         * Store Error Report in cache variable. Via Fingerprint Channel error is logged and Issue can be raised
         */
        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            rangeMapCache = Optional.of(Converter.getGson().toJson(error));
            removeCallback(this);
        }
    }

    @NonNullByDefault({})
    public class DestinationsCallback implements StringResponseCallback {

        @Override
        public void onResponse(Optional<String> content) {
            destinationCache = content;
            if (content.isPresent()) {
                DestinationContainer dc = Converter.getGson().fromJson(content.get(), DestinationContainer.class);
                if (dc != null) {
                    if (dc.destinations != null) {
                        updateDestinations(dc.destinations);
                    }
                }
            }
            removeCallback(this);
        }

        /**
         * Store Error Report in cache variable. Via Fingerprint Channel error is logged and Issue can be raised
         */
        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            destinationCache = Optional.of(Converter.getGson().toJson(error));
            removeCallback(this);
        }
    }

    @NonNullByDefault({})
    public class ImageCallback implements ByteResponseCallback {
        @Override
        public void onResponse(Optional<byte[]> content) {
            imageCache = content;
            if (content.isPresent()) {
                String contentType = HttpUtil.guessContentTypeFromData(content.get());
                updateState(imageChannel, new RawType(content.get(), contentType));
            } else {
                synchronized (imageProperties) {
                    imageProperties.failed();
                }
            }
            removeCallback(this);
        }

        /**
         * Store Error Report in cache variable. Via Fingerprint Channel error is logged and Issue can be raised
         */
        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            synchronized (imageProperties) {
                imageProperties.failed();
            }
            removeCallback(this);
        }
    }

    @NonNullByDefault({})
    public class AllTripsCallback implements StringResponseCallback {
        @Override
        public void onResponse(Optional<String> content) {
            if (content.isPresent()) {
                allTripsCache = content;
                AllTripsContainer atc = Converter.getGson().fromJson(content.get(), AllTripsContainer.class);
                if (atc != null) {
                    AllTrips at = atc.allTrips;
                    if (at != null) {
                        updateAllTrips(at);
                    }
                }
            }
            removeCallback(this);
        }

        /**
         * Store Error Report in cache variable. Via Fingerprint Channel error is logged and Issue can be raised
         */
        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            allTripsCache = Optional.of(Converter.getGson().toJson(error));
            removeCallback(this);
        }
    }

    @NonNullByDefault({})
    public class LastTripCallback implements StringResponseCallback {
        @Override
        public void onResponse(Optional<String> content) {
            if (content.isPresent()) {
                lastTripCache = content;
                LastTripContainer lt = Converter.getGson().fromJson(content.get(), LastTripContainer.class);
                if (lt != null) {
                    LastTrip trip = lt.lastTrip;
                    if (trip != null) {
                        updateLastTrip(trip);
                    }
                }
            }
            removeCallback(this);
        }

        /**
         * Store Error Report in cache variable. Via Fingerprint Channel error is logged and Issue can be raised
         */
        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            lastTripCache = Optional.of(Converter.getGson().toJson(error));
            removeCallback(this);
        }
    }

    /**
     * The VehicleStatus is supported by all Vehicle Types so it's used to reflect the Thing Status
     */
    @NonNullByDefault({})
    public class VehicleStatusCallback implements StringResponseCallback {
        private ThingStatus thingStatus = ThingStatus.UNKNOWN;

        /**
         * Vehicle Status is supported by all Vehicles so callback result is used to report Thing Status.
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
                VehicleStatusContainer status = Converter.getGson().fromJson(content.get(),
                        VehicleStatusContainer.class);
                if (status != null) {
                    VehicleStatus vStatus = status.vehicleStatus;
                    if (vStatus == null) {
                        return;
                    }
                    updateVehicleStatus(vStatus);
                    updateCheckControls(vStatus.checkControlMessages);
                    updateServices(vStatus.cbsData);
                    updatePosition(vStatus.position);
                }
            }
            removeCallback(this);
        }

        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            if (error.status == 404) {
                legacyMode = true;
                logger.debug("VehicleStatus not found - switch to legacy API");
                proxy.get().requestLegacyVehcileStatus(configuration.get(), oldVehicleStatusCallback);
            }
            vehicleStatusCache = Optional.of(Converter.getGson().toJson(error));
            setThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.reason);
            removeCallback(this);
        }
    }

    /**
     * Fallback API if origin isn't supported.
     * This comes from the Community Discussion where a Vehicle from 2015 answered with "404"
     * https://community.openhab.org/t/bmw-connecteddrive-binding/105124
     *
     * Selection of API was discussed here
     * https://community.openhab.org/t/bmw-connecteddrive-bmw-i3/103876
     *
     * I figured out that only one API was working for this Vehicle. So this backward compatible Callback is introduced.
     * The delivered data is converted into the origin dto object so no changes in previous functional code needed
     */
    @NonNullByDefault({})
    public class LegacyVehicleStatusCallback implements StringResponseCallback {
        @Override
        public void onResponse(Optional<String> content) {
            if (content.isPresent()) {
                VehicleAttributesContainer vac = Converter.getGson().fromJson(content.get(),
                        VehicleAttributesContainer.class);
                if (vac != null) {
                    vehicleStatusCallback.onResponse(Optional.of(vac.transform()));
                }
            }
        }

        @Override
        public void onError(NetworkError error) {
            vehicleStatusCallback.onError(error);
        }
    }

    private void handleChargeProfileCommand(ChannelUID channelUID, Command command) {

        final String id = channelUID.getIdWithoutGroup();

        if (CHARGE_CONTROL_COMMAND.equals(id) && command instanceof StringType) {
            if (chargeProfileEdit.isPresent()) {
                switch (((StringType) command).toFullString()) {
                    case CHARGE_CONTROL_CANCEL:
                        cancelChargeProfileEdit();
                        break;
                    case CHARGE_CONTROL_SEND:
                        if (remote.isPresent()) {
                            remote.get().execute(RemoteService.CHARGING_CONTROL,
                                    Converter.getGson().toJson(chargeProfileEdit.get()));
                        }
                        break;
                }
            }
        } else {
            startChargeProfileEdit();

            final WeeklyPlanner wp = chargeProfileEdit.get().weeklyPlanner;

            if (command instanceof StringType) {
                switch (id) {
                    case CHARGE_PROFILE_MODE:
                        wp.chargingMode = ((StringType) command).toFullString();
                        break;
                    default:
                        cancelChargeProfileEdit();
                        break;
                }
            } else if (command instanceof OnOffType) {
                switch (id) {
                    case CHARGE_PROFILE_CLIMATE:
                        wp.climatizationEnabled = OnOffType.ON.equals(command) ? true : false;
                        break;
                    case CHARGE_TIMER1_ENABLED:
                        wp.timer1.timerEnabled = OnOffType.ON.equals(command) ? true : false;
                        break;
                    case CHARGE_TIMER1_DAYS_MON:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer1.dayOn(Day.MONDAY);
                        } else {
                            wp.timer1.dayOff(Day.MONDAY);
                        }
                        updateState(timer1Days, StringType.valueOf(wp.timer1.getDays()));
                        break;
                    case CHARGE_TIMER1_DAYS_TUE:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer1.dayOn(Day.TUESDAY);
                        } else {
                            wp.timer1.dayOff(Day.TUESDAY);
                        }
                        updateState(timer1Days, StringType.valueOf(wp.timer1.getDays()));
                        break;
                    case CHARGE_TIMER1_DAYS_WED:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer1.dayOn(Day.WEDNESDAY);
                        } else {
                            wp.timer1.dayOff(Day.WEDNESDAY);
                        }
                        updateState(timer1Days, StringType.valueOf(wp.timer1.getDays()));
                        break;
                    case CHARGE_TIMER1_DAYS_THU:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer1.dayOn(Day.THURSDAY);
                        } else {
                            wp.timer1.dayOff(Day.THURSDAY);
                        }
                        updateState(timer1Days, StringType.valueOf(wp.timer1.getDays()));
                        break;
                    case CHARGE_TIMER1_DAYS_FRI:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer1.dayOn(Day.FRIDAY);
                        } else {
                            wp.timer1.dayOff(Day.FRIDAY);
                        }
                        updateState(timer1Days, StringType.valueOf(wp.timer1.getDays()));
                        break;
                    case CHARGE_TIMER1_DAYS_SAT:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer1.dayOn(Day.SATURDAY);
                        } else {
                            wp.timer1.dayOff(Day.SATURDAY);
                        }
                        updateState(timer1Days, StringType.valueOf(wp.timer1.getDays()));
                        break;
                    case CHARGE_TIMER1_DAYS_SUN:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer1.dayOn(Day.SUNDAY);
                        } else {
                            wp.timer1.dayOff(Day.SUNDAY);
                        }
                        updateState(timer1Days, StringType.valueOf(wp.timer1.getDays()));
                        break;
                    case CHARGE_TIMER2_ENABLED:
                        wp.timer2.timerEnabled = OnOffType.ON.equals(command) ? true : false;
                        break;
                    case CHARGE_TIMER2_DAYS_MON:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer2.dayOn(Day.MONDAY);
                        } else {
                            wp.timer2.dayOff(Day.MONDAY);
                        }
                        updateState(timer2Days, StringType.valueOf(wp.timer2.getDays()));
                        break;
                    case CHARGE_TIMER2_DAYS_TUE:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer2.dayOn(Day.TUESDAY);
                        } else {
                            wp.timer2.dayOff(Day.TUESDAY);
                        }
                        updateState(timer2Days, StringType.valueOf(wp.timer2.getDays()));
                        break;
                    case CHARGE_TIMER2_DAYS_WED:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer2.dayOn(Day.WEDNESDAY);
                        } else {
                            wp.timer2.dayOff(Day.WEDNESDAY);
                        }
                        updateState(timer2Days, StringType.valueOf(wp.timer2.getDays()));
                        break;
                    case CHARGE_TIMER2_DAYS_THU:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer2.dayOn(Day.THURSDAY);
                        } else {
                            wp.timer2.dayOff(Day.THURSDAY);
                        }
                        updateState(timer2Days, StringType.valueOf(wp.timer2.getDays()));
                        break;
                    case CHARGE_TIMER2_DAYS_FRI:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer2.dayOn(Day.FRIDAY);
                        } else {
                            wp.timer2.dayOff(Day.FRIDAY);
                        }
                        updateState(timer2Days, StringType.valueOf(wp.timer2.getDays()));
                        break;
                    case CHARGE_TIMER2_DAYS_SAT:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer2.dayOn(Day.SATURDAY);
                        } else {
                            wp.timer2.dayOff(Day.SATURDAY);
                        }
                        updateState(timer2Days, StringType.valueOf(wp.timer2.getDays()));
                        break;
                    case CHARGE_TIMER2_DAYS_SUN:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer2.dayOn(Day.SUNDAY);
                        } else {
                            wp.timer2.dayOff(Day.SUNDAY);
                        }
                        updateState(timer2Days, StringType.valueOf(wp.timer2.getDays()));
                        break;
                    case CHARGE_TIMER3_ENABLED:
                        wp.timer3.timerEnabled = OnOffType.ON.equals(command) ? true : false;
                        break;
                    case CHARGE_TIMER3_DAYS_MON:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer3.dayOn(Day.MONDAY);
                        } else {
                            wp.timer3.dayOff(Day.MONDAY);
                        }
                        updateState(timer3Days, StringType.valueOf(wp.timer3.getDays()));
                        break;
                    case CHARGE_TIMER3_DAYS_TUE:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer3.dayOn(Day.TUESDAY);
                        } else {
                            wp.timer3.dayOff(Day.TUESDAY);
                        }
                        updateState(timer3Days, StringType.valueOf(wp.timer3.getDays()));
                        break;
                    case CHARGE_TIMER3_DAYS_WED:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer3.dayOn(Day.WEDNESDAY);
                        } else {
                            wp.timer3.dayOff(Day.WEDNESDAY);
                        }
                        updateState(timer3Days, StringType.valueOf(wp.timer3.getDays()));
                        break;
                    case CHARGE_TIMER3_DAYS_THU:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer3.dayOn(Day.THURSDAY);
                        } else {
                            wp.timer3.dayOff(Day.THURSDAY);
                        }
                        updateState(timer3Days, StringType.valueOf(wp.timer3.getDays()));
                        break;
                    case CHARGE_TIMER3_DAYS_FRI:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer3.dayOn(Day.FRIDAY);
                        } else {
                            wp.timer3.dayOff(Day.FRIDAY);
                        }
                        updateState(timer3Days, StringType.valueOf(wp.timer3.getDays()));
                        break;
                    case CHARGE_TIMER3_DAYS_SAT:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer3.dayOn(Day.SATURDAY);
                        } else {
                            wp.timer3.dayOff(Day.SATURDAY);
                        }
                        updateState(timer3Days, StringType.valueOf(wp.timer3.getDays()));
                        break;
                    case CHARGE_TIMER3_DAYS_SUN:
                        if (OnOffType.ON.equals(command)) {
                            wp.timer3.dayOn(Day.SUNDAY);
                        } else {
                            wp.timer3.dayOff(Day.SUNDAY);
                        }
                        updateState(timer3Days, StringType.valueOf(wp.timer3.getDays()));
                        break;
                    default:
                        this.cancelChargeProfileEdit();
                        break;
                }
            } else if (command instanceof DecimalType) {
                final int numberCommand = ((DecimalType) command).intValue();
                switch (id) {
                    case CHARGE_WINDOW_START_HOUR:
                        wp.preferredChargingWindow.setStartHour(numberCommand);
                        updateState(chargeWindowStart, StringType.valueOf(wp.preferredChargingWindow.startTime));
                        break;
                    case CHARGE_WINDOW_START_MINUTE:
                        wp.preferredChargingWindow.setStartMinute(numberCommand);
                        updateState(chargeWindowStart, StringType.valueOf(wp.preferredChargingWindow.startTime));
                        break;
                    case CHARGE_WINDOW_END_HOUR:
                        wp.preferredChargingWindow.setEndHour(numberCommand);
                        updateState(chargeWindowEnd, StringType.valueOf(wp.preferredChargingWindow.endTime));
                        break;
                    case CHARGE_WINDOW_END_MINUTE:
                        wp.preferredChargingWindow.setEndMinute(numberCommand);
                        updateState(chargeWindowEnd, StringType.valueOf(wp.preferredChargingWindow.endTime));
                        break;
                    case CHARGE_TIMER1_DEPARTURE_HOUR:
                        wp.timer1.setDepartureHour(numberCommand);
                        updateState(timer1Departure, StringType.valueOf(wp.timer1.departureTime));
                        break;
                    case CHARGE_TIMER1_DEPARTURE_MINUTE:
                        wp.timer1.setDepartureMinute(numberCommand);
                        updateState(timer1Departure, StringType.valueOf(wp.timer1.departureTime));
                        break;
                    case CHARGE_TIMER2_DEPARTURE_HOUR:
                        wp.timer2.setDepartureHour(numberCommand);
                        updateState(timer2Departure, StringType.valueOf(wp.timer2.departureTime));
                        break;
                    case CHARGE_TIMER2_DEPARTURE_MINUTE:
                        wp.timer2.setDepartureMinute(numberCommand);
                        updateState(timer2Departure, StringType.valueOf(wp.timer2.departureTime));
                        break;
                    case CHARGE_TIMER3_DEPARTURE_HOUR:
                        wp.timer3.setDepartureHour(numberCommand);
                        updateState(timer3Departure, StringType.valueOf(wp.timer3.departureTime));
                        break;
                    case CHARGE_TIMER3_DEPARTURE_MINUTE:
                        wp.timer3.setDepartureMinute(numberCommand);
                        updateState(timer3Departure, StringType.valueOf(wp.timer3.departureTime));
                        break;
                    default:
                        this.cancelChargeProfileEdit();
                        break;
                }
            } else {
                this.cancelChargeProfileEdit();
            }
        }
    }

    private void cancelChargeProfileEdit() {
        chargeProfileEdit = Optional.empty();
        final ChargeProfile cp = Converter.getGson().fromJson(chargeProfileCache.get(), ChargeProfile.class);
        if (cp != null) {
            updateChargeProfile(cp);
        }
        updateChargeProfileVersion(ChargeProfileVersion.REMOTE);
    }

    private void startChargeProfileEdit() {
        if (chargeProfileEdit.isEmpty()) {
            final ChargeProfile cp = Converter.getGson().fromJson(chargeProfileCache.get(), ChargeProfile.class);
            if (cp == null) {
                chargeProfileEdit = Optional.of(ChargeProfile.defaultChargeProfile());
            } else {
                chargeProfileEdit = Optional.of(cp.completeChargeProfile());
            }
            updateChargeProfileVersion(ChargeProfileVersion.LOCAL);
        }
    }

    private void saveChargeProfileEdit() {
        chargeProfileCache = Optional.of(Converter.getGson().toJson(chargeProfileEdit.get()));
        chargeProfileEdit = Optional.empty();
        updateChargeProfileVersion(ChargeProfileVersion.REMOTE);
    }
}
