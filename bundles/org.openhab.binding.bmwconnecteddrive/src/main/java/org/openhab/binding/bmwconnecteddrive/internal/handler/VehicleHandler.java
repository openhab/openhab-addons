/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwconnecteddrive.internal.VehicleConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.action.BMWConnectedDriveActions;
import org.openhab.binding.bmwconnecteddrive.internal.dto.DestinationContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.NetworkError;
import org.openhab.binding.bmwconnecteddrive.internal.dto.compat.VehicleAttributesContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.navigation.NavigationContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.AllTrips;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.AllTripsContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.LastTrip;
import org.openhab.binding.bmwconnecteddrive.internal.dto.statistics.LastTripContainer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatusContainer;
import org.openhab.binding.bmwconnecteddrive.internal.handler.RemoteServiceHandler.ExecutionState;
import org.openhab.binding.bmwconnecteddrive.internal.handler.RemoteServiceHandler.RemoteService;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileUtils;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileUtils.ChargeKeyDay;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper.ProfileKey;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ImageProperties;
import org.openhab.binding.bmwconnecteddrive.internal.utils.RemoteServiceUtils;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link VehicleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit & send charge profile
 */
@NonNullByDefault
public class VehicleHandler extends VehicleChannelHandler {
    private int legacyMode = Constants.INT_UNDEF; // switch to legacy API in case of 404 Errors

    private Optional<ConnectedDriveProxy> proxy = Optional.empty();
    private Optional<RemoteServiceHandler> remote = Optional.empty();
    private Optional<VehicleConfiguration> configuration = Optional.empty();
    private Optional<ConnectedDriveBridgeHandler> bridgeHandler = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<ScheduledFuture<?>> editTimeout = Optional.empty();
    private Optional<List<ResponseCallback>> callbackCounter = Optional.empty();

    private ImageProperties imageProperties = new ImageProperties();
    VehicleStatusCallback vehicleStatusCallback = new VehicleStatusCallback();
    StringResponseCallback oldVehicleStatusCallback = new LegacyVehicleStatusCallback();
    StringResponseCallback navigationCallback = new NavigationStatusCallback();
    StringResponseCallback lastTripCallback = new LastTripCallback();
    StringResponseCallback allTripsCallback = new AllTripsCallback();
    StringResponseCallback chargeProfileCallback = new ChargeProfilesCallback();
    StringResponseCallback rangeMapCallback = new RangeMapCallback();
    DestinationsCallback destinationCallback = new DestinationsCallback();
    ByteResponseCallback imageCallback = new ImageCallback();

    private Optional<ChargeProfileWrapper> chargeProfileEdit = Optional.empty();
    private Optional<String> chargeProfileSent = Optional.empty();

    public VehicleHandler(Thing thing, BMWConnectedDriveOptionProvider op, String type, boolean imperial) {
        super(thing, op, type, imperial);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String group = channelUID.getGroupId();

        // Refresh of Channels with cached values
        if (command instanceof RefreshType) {
            if (CHANNEL_GROUP_LAST_TRIP.equals(group)) {
                lastTripCache.ifPresent(lastTrip -> lastTripCallback.onResponse(lastTrip));
            } else if (CHANNEL_GROUP_LIFETIME.equals(group)) {
                allTripsCache.ifPresent(allTrips -> allTripsCallback.onResponse(allTrips));
            } else if (CHANNEL_GROUP_DESTINATION.equals(group)) {
                destinationCache.ifPresent(destination -> destinationCallback.onResponse(destination));
            } else if (CHANNEL_GROUP_STATUS.equals(group)) {
                vehicleStatusCache.ifPresent(vehicleStatus -> vehicleStatusCallback.onResponse(vehicleStatus));
            } else if (CHANNEL_GROUP_CHARGE.equals(group)) {
                chargeProfileEdit.ifPresentOrElse(this::updateChargeProfile,
                        () -> chargeProfileCache.ifPresent(this::updateChargeProfileFromContent));
            } else if (CHANNEL_GROUP_VEHICLE_IMAGE.equals(group)) {
                imageCache.ifPresent(image -> imageCallback.onResponse(image));
            }
            // Check for Channel Group and corresponding Actions
        } else if (CHANNEL_GROUP_REMOTE.equals(group)) {
            // Executing Remote Services
            if (command instanceof StringType) {
                String serviceCommand = ((StringType) command).toFullString();
                remote.ifPresent(remot -> {
                    switch (serviceCommand) {
                        case REMOTE_SERVICE_LIGHT_FLASH:
                        case REMOTE_SERVICE_AIR_CONDITIONING:
                        case REMOTE_SERVICE_DOOR_LOCK:
                        case REMOTE_SERVICE_DOOR_UNLOCK:
                        case REMOTE_SERVICE_HORN:
                        case REMOTE_SERVICE_VEHICLE_FINDER:
                        case REMOTE_SERVICE_CHARGE_NOW:
                            RemoteServiceUtils.getRemoteService(serviceCommand)
                                    .ifPresentOrElse(service -> remot.execute(service), () -> {
                                        logger.debug("Remote service execution {} unknown", serviceCommand);
                                    });
                            break;
                        case REMOTE_SERVICE_CHARGING_CONTROL:
                            sendChargeProfile(chargeProfileEdit);
                            break;
                        default:
                            logger.debug("Remote service execution {} unknown", serviceCommand);
                            break;
                    }
                });
            }
        } else if (CHANNEL_GROUP_VEHICLE_IMAGE.equals(group)) {
            // Image Change
            configuration.ifPresent(config -> {
                if (command instanceof StringType) {
                    if (channelUID.getIdWithoutGroup().equals(IMAGE_VIEWPORT)) {
                        String newViewport = command.toString();
                        synchronized (imageProperties) {
                            if (!imageProperties.viewport.equals(newViewport)) {
                                imageProperties = new ImageProperties(newViewport, imageProperties.size);
                                imageCache = Optional.empty();
                                proxy.ifPresent(prox -> prox.requestImage(config, imageProperties, imageCallback));
                            }
                        }
                        updateChannel(CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_VIEWPORT, StringType.valueOf(newViewport));
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
                                    proxy.ifPresent(prox -> prox.requestImage(config, imageProperties, imageCallback));
                                }
                            }
                        }
                        updateChannel(CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_SIZE, new DecimalType(newImageSize));
                    }
                }
            });
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
        final VehicleConfiguration config = getConfigAs(VehicleConfiguration.class);
        configuration = Optional.of(config);
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                bridgeHandler = Optional.of(((ConnectedDriveBridgeHandler) handler));
                proxy = ((ConnectedDriveBridgeHandler) handler).getProxy();
                remote = proxy.map(prox -> prox.getRemoteServiceHandler(this));
            } else {
                logger.debug("Bridge Handler null");
            }
        } else {
            logger.debug("Bridge null");
        }

        // get Image after init with config values
        synchronized (imageProperties) {
            imageProperties = new ImageProperties(config.imageViewport, config.imageSize);
        }
        updateChannel(CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_VIEWPORT, StringType.valueOf((config.imageViewport)));
        updateChannel(CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_SIZE, new DecimalType((config.imageSize)));

        // check imperial setting is different to AutoDetect
        if (!UNITS_AUTODETECT.equals(config.units)) {
            imperial = UNITS_IMPERIAL.equals(config.units);
        }

        // start update schedule
        startSchedule(config.refreshInterval);
    }

    private void startSchedule(int interval) {
        refreshJob.ifPresentOrElse(job -> {
            if (job.isCancelled()) {
                refreshJob = Optional
                        .of(scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES));
            } // else - scheduler is already running!
        }, () -> {
            refreshJob = Optional.of(scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES));
        });
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
        editTimeout.ifPresent(job -> job.cancel(true));
        remote.ifPresent(RemoteServiceHandler::cancel);
    }

    public void getData() {
        proxy.ifPresentOrElse(prox -> {
            configuration.ifPresentOrElse(config -> {
                if (legacyMode == 1) {
                    prox.requestLegacyVehcileStatus(config, oldVehicleStatusCallback);
                } else {
                    prox.requestVehcileStatus(config, vehicleStatusCallback);
                }
                addCallback(vehicleStatusCallback);
                prox.requestLNavigation(config, navigationCallback);
                addCallback(navigationCallback);
                if (isSupported(Constants.STATISTICS)) {
                    prox.requestLastTrip(config, lastTripCallback);
                    prox.requestAllTrips(config, allTripsCallback);
                    addCallback(lastTripCallback);
                    addCallback(allTripsCallback);
                }
                if (isSupported(Constants.LAST_DESTINATIONS)) {
                    prox.requestDestinations(config, destinationCallback);
                    addCallback(destinationCallback);
                }
                if (isElectric) {
                    prox.requestChargingProfile(config, chargeProfileCallback);
                    addCallback(chargeProfileCallback);
                }
                synchronized (imageProperties) {
                    if (!imageCache.isPresent() && !imageProperties.failLimitReached()) {
                        prox.requestImage(config, imageProperties, imageCallback);
                        addCallback(imageCallback);
                    }
                }
            }, () -> {
                logger.warn("ConnectedDrive Configuration isn't present");
            });
        }, () -> {
            logger.warn("ConnectedDrive Proxy isn't present");
        });
    }

    private synchronized void addCallback(ResponseCallback rc) {
        callbackCounter.ifPresent(counter -> counter.add(rc));
    }

    private synchronized void removeCallback(ResponseCallback rc) {
        callbackCounter.ifPresent(counter -> {
            counter.remove(rc);
            // all necessary callbacks received => print and set to empty
            if (counter.isEmpty()) {
                logFingerPrint();
                callbackCounter = Optional.empty();
            }
        });
    }

    private void logFingerPrint() {
        final String vin = configuration.map(config -> config.vin).orElse("");
        logger.debug("###### Vehicle Troubleshoot Fingerprint Data - BEGIN ######");
        logger.debug("### Discovery Result ###");
        bridgeHandler.ifPresent(handler -> {
            logger.debug("{}", handler.getDiscoveryFingerprint());
        });
        vehicleStatusCache.ifPresentOrElse(vehicleStatus -> {
            logger.debug("### Vehicle Status ###");

            // Anonymous data for VIN and Position
            try {
                VehicleStatusContainer container = Converter.getGson().fromJson(vehicleStatus,
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
            } catch (JsonSyntaxException jse) {
                logger.debug("{}", jse.getMessage());
            }
        }, () -> {
            logger.debug("### Vehicle Status Empty ###");
        });
        lastTripCache.ifPresentOrElse(lastTrip -> {
            logger.debug("### Last Trip ###");
            logger.debug("{}", lastTrip.replaceAll(vin, Constants.ANONYMOUS));
        }, () -> {
            logger.debug("### Last Trip Empty ###");
        });
        allTripsCache.ifPresentOrElse(allTrips -> {
            logger.debug("### All Trips ###");
            logger.debug("{}", allTrips.replaceAll(vin, Constants.ANONYMOUS));
        }, () -> {
            logger.debug("### All Trips Empty ###");
        });
        if (isElectric) {
            chargeProfileCache.ifPresentOrElse(chargeProfile -> {
                logger.debug("### Charge Profile ###");
                logger.debug("{}", chargeProfile.replaceAll(vin, Constants.ANONYMOUS));
            }, () -> {
                logger.debug("### Charge Profile Empty ###");
            });
        }
        destinationCache.ifPresentOrElse(destination -> {
            logger.debug("### Charge Profile ###");
            try {
                DestinationContainer container = Converter.getGson().fromJson(destination, DestinationContainer.class);
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
            } catch (JsonSyntaxException jse) {
                logger.debug("{}", jse.getMessage());
            }
        }, () -> {
            logger.debug("### Charge Profile Empty ###");
        });
        rangeMapCache.ifPresentOrElse(rangeMap -> {
            logger.debug("### Range Map ###");
            logger.debug("{}", rangeMap.replaceAll(vin, Constants.ANONYMOUS));
        }, () -> {
            logger.debug("### Range Map Empty ###");
        });
        logger.debug("###### Vehicle Troubleshoot Fingerprint Data - END ######");
    }

    /**
     * Don't stress ConnectedDrive with unnecessary requests. One call at the beginning is done to check the response.
     * After cache has e.g. a proper error response it will be shown in the fingerprint
     *
     * @return
     */
    private boolean isSupported(String service) {
        final String services = thing.getProperties().get(Constants.SERVICES_SUPPORTED);
        if (services != null) {
            if (services.contains(service)) {
                return true;
            }
        }
        // if cache is empty give it a try one time to collected Troubleshoot data
        return lastTripCache.isEmpty() || allTripsCache.isEmpty() || destinationCache.isEmpty();
    }

    public void updateRemoteExecutionStatus(@Nullable String service, @Nullable String status) {
        if (RemoteService.CHARGING_CONTROL.toString().equals(service)
                && ExecutionState.EXECUTED.name().equals(status)) {
            saveChargeProfileSent();
        }
        updateChannel(CHANNEL_GROUP_REMOTE, REMOTE_STATE, StringType
                .valueOf(Converter.toTitleCase((service == null ? "-" : service) + Constants.SPACE + status)));
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
    public class ChargeProfilesCallback implements StringResponseCallback {
        @Override
        public void onResponse(@Nullable String content) {
            if (content != null) {
                chargeProfileCache = Optional.of(content);
                if (chargeProfileEdit.isEmpty()) {
                    updateChargeProfileFromContent(content);
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

    public class RangeMapCallback implements StringResponseCallback {
        @Override
        public void onResponse(@Nullable String content) {
            rangeMapCache = Optional.ofNullable(content);
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

    public class DestinationsCallback implements StringResponseCallback {

        @Override
        public void onResponse(@Nullable String content) {
            destinationCache = Optional.ofNullable(content);
            if (content != null) {
                try {
                    DestinationContainer dc = Converter.getGson().fromJson(content, DestinationContainer.class);
                    if (dc != null && dc.destinations != null) {
                        updateDestinations(dc.destinations);
                    }
                } catch (JsonSyntaxException jse) {
                    logger.debug("{}", jse.getMessage());
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

    public class ImageCallback implements ByteResponseCallback {
        @Override
        public void onResponse(byte[] content) {
            if (content.length > 0) {
                imageCache = Optional.of(content);
                String contentType = HttpUtil.guessContentTypeFromData(content);
                updateChannel(CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_FORMAT, new RawType(content, contentType));
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

    public class AllTripsCallback implements StringResponseCallback {
        @Override
        public void onResponse(@Nullable String content) {
            if (content != null) {
                allTripsCache = Optional.of(content);
                try {
                    AllTripsContainer atc = Converter.getGson().fromJson(content, AllTripsContainer.class);
                    if (atc != null) {
                        AllTrips at = atc.allTrips;
                        if (at != null) {
                            updateAllTrips(at);
                        }
                    }
                } catch (JsonSyntaxException jse) {
                    logger.debug("{}", jse.getMessage());
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

    public class LastTripCallback implements StringResponseCallback {
        @Override
        public void onResponse(@Nullable String content) {
            if (content != null) {
                lastTripCache = Optional.of(content);
                try {
                    LastTripContainer lt = Converter.getGson().fromJson(content, LastTripContainer.class);
                    if (lt != null) {
                        LastTrip trip = lt.lastTrip;
                        if (trip != null) {
                            updateLastTrip(trip);
                        }
                    }
                } catch (JsonSyntaxException jse) {
                    logger.debug("{}", jse.getMessage());
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
    public class VehicleStatusCallback implements StringResponseCallback {
        @Override
        public void onResponse(@Nullable String content) {
            if (content != null) {
                // switch to non legacy mode
                legacyMode = 0;
                updateStatus(ThingStatus.ONLINE);
                vehicleStatusCache = Optional.of(content);
                try {
                    VehicleStatusContainer status = Converter.getGson().fromJson(content, VehicleStatusContainer.class);
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
                } catch (JsonSyntaxException jse) {
                    logger.debug("{}", jse.getMessage());
                }
            }
            removeCallback(this);
        }

        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            // only if legacyMode isn't set yet try legacy API
            if (error.status != 200 && legacyMode == Constants.INT_UNDEF) {
                logger.debug("VehicleStatus not found - try legacy API");
                proxy.get().requestLegacyVehcileStatus(configuration.get(), oldVehicleStatusCallback);
            }
            vehicleStatusCache = Optional.of(Converter.getGson().toJson(error));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.reason);
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
    public class LegacyVehicleStatusCallback implements StringResponseCallback {
        @Override
        public void onResponse(@Nullable String content) {
            if (content != null) {
                try {
                    VehicleAttributesContainer vac = Converter.getGson().fromJson(content,
                            VehicleAttributesContainer.class);
                    vehicleStatusCallback.onResponse(Converter.transformLegacyStatus(vac));
                    legacyMode = 1;
                    logger.debug("VehicleStatus switched to legacy mode");
                } catch (JsonSyntaxException jse) {
                    logger.debug("{}", jse.getMessage());
                }
            }
        }

        @Override
        public void onError(NetworkError error) {
            vehicleStatusCallback.onError(error);
        }
    }

    public class NavigationStatusCallback implements StringResponseCallback {
        @Override
        public void onResponse(@Nullable String content) {
            if (content != null) {
                try {
                    NavigationContainer nav = Converter.getGson().fromJson(content, NavigationContainer.class);
                    updateChannel(CHANNEL_GROUP_RANGE, SOC_MAX, QuantityType.valueOf(nav.socmax, Units.KILOWATT_HOUR));
                } catch (JsonSyntaxException jse) {
                    logger.debug("{}", jse.getMessage());
                }
            }
            removeCallback(this);
        }

        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            removeCallback(this);
        }
    }

    private void handleChargeProfileCommand(ChannelUID channelUID, Command command) {
        if (chargeProfileEdit.isEmpty()) {
            chargeProfileEdit = getChargeProfileWrapper();
        }

        chargeProfileEdit.ifPresent(profile -> {

            boolean processed = false;

            final String id = channelUID.getIdWithoutGroup();

            if (command instanceof StringType) {
                final String stringCommand = ((StringType) command).toFullString();
                switch (id) {
                    case CHARGE_PROFILE_PREFERENCE:
                        profile.setPreference(stringCommand);
                        updateChannel(CHANNEL_GROUP_CHARGE, CHARGE_PROFILE_PREFERENCE,
                                StringType.valueOf(Converter.toTitleCase(profile.getPreference())));
                        processed = true;
                        break;
                    case CHARGE_PROFILE_MODE:
                        profile.setMode(stringCommand);
                        updateChannel(CHANNEL_GROUP_CHARGE, CHARGE_PROFILE_MODE,
                                StringType.valueOf(Converter.toTitleCase(profile.getMode())));
                        processed = true;
                        break;
                    default:
                        break;
                }
            } else if (command instanceof OnOffType) {
                final ProfileKey enableKey = ChargeProfileUtils.getEnableKey(id);
                if (enableKey != null) {
                    profile.setEnabled(enableKey, OnOffType.ON.equals(command));
                    updateTimedState(profile, enableKey);
                    processed = true;
                } else {
                    final ChargeKeyDay chargeKeyDay = ChargeProfileUtils.getKeyDay(id);
                    if (chargeKeyDay != null) {
                        profile.setDayEnabled(chargeKeyDay.key, chargeKeyDay.day, OnOffType.ON.equals(command));
                        updateTimedState(profile, chargeKeyDay.key);
                        processed = true;
                    }
                }
            } else if (command instanceof DateTimeType) {
                DateTimeType dtt = (DateTimeType) command;
                logger.debug("Accept {} for ID {}", dtt.toFullString(), id);
                final ProfileKey key = ChargeProfileUtils.getTimeKey(id);
                if (key != null) {
                    profile.setTime(key, dtt.getZonedDateTime().toLocalTime());
                    updateTimedState(profile, key);
                    processed = true;
                }
            }

            if (processed) {
                // cancel current timer and add another 5 mins - valid for each edit
                editTimeout.ifPresent(timeout -> timeout.cancel(true));
                // start edit timer with 5 min timeout
                editTimeout = Optional.of(scheduler.schedule(() -> {
                    editTimeout = Optional.empty();
                    chargeProfileEdit = Optional.empty();
                    chargeProfileCache.ifPresent(this::updateChargeProfileFromContent);
                }, 5, TimeUnit.MINUTES));
            } else {
                logger.debug("unexpected command {} not processed", command.toFullString());
            }
        });
    }

    private void saveChargeProfileSent() {
        editTimeout.ifPresent(timeout -> {
            timeout.cancel(true);
            editTimeout = Optional.empty();
        });
        chargeProfileSent.ifPresent(sent -> {
            chargeProfileCache = Optional.of(sent);
            chargeProfileSent = Optional.empty();
            chargeProfileEdit = Optional.empty();
            chargeProfileCache.ifPresent(this::updateChargeProfileFromContent);
        });
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(BMWConnectedDriveActions.class);
    }

    public Optional<ChargeProfileWrapper> getChargeProfileWrapper() {
        return chargeProfileCache.flatMap(cache -> {
            return ChargeProfileWrapper.fromJson(cache).map(wrapper -> {
                return wrapper;
            }).or(() -> {
                logger.debug("cannot parse charging profile: {}", cache);
                return Optional.empty();
            });
        }).or(() -> {
            logger.debug("No ChargeProfile recieved so far - cannot start editing");
            return Optional.empty();
        });
    }

    public void sendChargeProfile(Optional<ChargeProfileWrapper> profile) {
        profile.map(profil -> profil.getJson()).ifPresent(json -> {
            logger.debug("sending charging profile: {}", json);
            chargeProfileSent = Optional.of(json);
            remote.ifPresent(rem -> rem.execute(RemoteService.CHARGING_CONTROL, json));
        });
    }
}
