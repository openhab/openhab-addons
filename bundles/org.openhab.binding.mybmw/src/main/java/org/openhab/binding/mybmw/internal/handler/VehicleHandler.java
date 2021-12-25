/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.handler;

import static org.openhab.binding.mybmw.internal.MyBMWConstants.*;

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
import org.openhab.binding.mybmw.internal.VehicleConfiguration;
import org.openhab.binding.mybmw.internal.action.MyBMWActions;
import org.openhab.binding.mybmw.internal.dto.network.NetworkError;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.handler.RemoteServiceHandler.ExecutionState;
import org.openhab.binding.mybmw.internal.handler.RemoteServiceHandler.RemoteService;
import org.openhab.binding.mybmw.internal.utils.ChargeProfileUtils;
import org.openhab.binding.mybmw.internal.utils.ChargeProfileUtils.ChargeKeyDay;
import org.openhab.binding.mybmw.internal.utils.ChargeProfileWrapper;
import org.openhab.binding.mybmw.internal.utils.ChargeProfileWrapper.ProfileKey;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.binding.mybmw.internal.utils.ImageProperties;
import org.openhab.binding.mybmw.internal.utils.RemoteServiceUtils;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
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
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link VehicleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit & send charge profile
 */
@NonNullByDefault
public class VehicleHandler extends VehicleChannelHandler {
    private Optional<MyBMWProxy> proxy = Optional.empty();
    private Optional<RemoteServiceHandler> remote = Optional.empty();
    public Optional<VehicleConfiguration> configuration = Optional.empty();
    private Optional<MyBMWBridgeHandler> bridgeHandler = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<ScheduledFuture<?>> editTimeout = Optional.empty();
    private Optional<List<ResponseCallback>> callbackCounter = Optional.empty();

    private ImageProperties imageProperties = new ImageProperties();
    VehicleStatusCallback vehicleStatusCallback = new VehicleStatusCallback();
    ByteResponseCallback imageCallback = new ImageCallback();

    private Optional<ChargeProfileWrapper> chargeProfileEdit = Optional.empty();
    private Optional<String> chargeProfileSent = Optional.empty();

    public VehicleHandler(Thing thing, MyBMWOptionProvider op, String driveTrain, String language) {
        super(thing, op, driveTrain, language);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String group = channelUID.getGroupId();

        // Refresh of Channels with cached values
        if (command instanceof RefreshType) {
            if (CHANNEL_GROUP_STATUS.equals(group)) {
                vehicleStatusCache.ifPresent(vehicleStatus -> vehicleStatusCallback.onResponse(vehicleStatus));
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
                bridgeHandler = Optional.of(((MyBMWBridgeHandler) handler));
                proxy = ((MyBMWBridgeHandler) handler).getProxy();
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
                prox.requestVehicles(config.brand, vehicleStatusCallback);
                synchronized (imageProperties) {
                    if (!imageCache.isPresent() && !imageProperties.failLimitReached()) {
                        prox.requestImage(config, imageProperties, imageCallback);
                    }
                }
            }, () -> {
                logger.warn("MyBMW Vehicle Configuration isn't present");
            });
        }, () -> {
            logger.warn("MyBMWProxy isn't present");
        });
    }

    private void logFingerPrint() {
        // tbd [todo]
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
        return false;
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
        }
    }

    /**
     * The VehicleStatus is supported by all Vehicle Types so it's used to reflect the Thing Status
     */
    public class VehicleStatusCallback implements StringResponseCallback {
        @Override
        public void onResponse(@Nullable String content) {
            if (content != null) {
                if (getConfiguration().isPresent()) {
                    Vehicle v = Converter.getVehicle(configuration.get().vin, content);
                    if (v.valid) {
                        logger.info("Send update");
                        updateStatus(ThingStatus.ONLINE);
                        updateVehicle(v);
                    } else {
                        logger.info("Vehicle not valid");
                    }
                } else {
                    logger.info("configuration not present");
                }
            } else {
                logger.info("Content not valid");
            }
        }

        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            vehicleStatusCache = Optional.of(Converter.getGson().toJson(error));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.reason);
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
                    // chargeProfileCache.ifPresent(this::updateChargeProfileFromContent);
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
            // chargeProfileCache = Optional.of(sent);
            chargeProfileSent = Optional.empty();
            chargeProfileEdit = Optional.empty();
            // chargeProfileCache.ifPresent(this::updateChargeProfileFromContent);
        });
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MyBMWActions.class);
    }

    public Optional<ChargeProfileWrapper> getChargeProfileWrapper() {
        // return chargeProfileCache.flatMap(cache -> {
        // return ChargeProfileWrapper.fromJson(cache).map(wrapper -> {
        // return wrapper;
        // }).or(() -> {
        // logger.debug("cannot parse charging profile: {}", cache);
        // return Optional.empty();
        // });
        // }).or(() -> {
        // logger.debug("No ChargeProfile recieved so far - cannot start editing");
        // return Optional.empty();
        // });
        return Optional.empty();
    }

    public void sendChargeProfile(Optional<ChargeProfileWrapper> profile) {
        profile.map(profil -> profil.getJson()).ifPresent(json -> {
            logger.debug("sending charging profile: {}", json);
            chargeProfileSent = Optional.of(json);
            remote.ifPresent(rem -> rem.execute(RemoteService.CHARGING_CONTROL, json));
        });
    }
}
