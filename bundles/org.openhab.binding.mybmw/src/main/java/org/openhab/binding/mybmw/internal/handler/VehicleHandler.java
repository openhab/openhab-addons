/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.VehicleConfiguration;
import org.openhab.binding.mybmw.internal.dto.charge.ChargeSessionsContainer;
import org.openhab.binding.mybmw.internal.dto.charge.ChargeStatisticsContainer;
import org.openhab.binding.mybmw.internal.dto.network.NetworkError;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
import org.openhab.binding.mybmw.internal.utils.ImageProperties;
import org.openhab.binding.mybmw.internal.utils.RemoteServiceUtils;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.io.net.http.HttpUtil;
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

import com.google.gson.JsonSyntaxException;

/**
 * The {@link VehicleHandler} handles responses from BMW API
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit & send charge profile
 */
@NonNullByDefault
public class VehicleHandler extends VehicleChannelHandler {
    private Optional<MyBMWProxy> proxy = Optional.empty();
    private Optional<RemoteServiceHandler> remote = Optional.empty();
    public Optional<VehicleConfiguration> configuration = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<ScheduledFuture<?>> editTimeout = Optional.empty();

    private ImageProperties imageProperties = new ImageProperties();
    VehicleStatusCallback vehicleStatusCallback = new VehicleStatusCallback();
    ChargeStatisticsCallback chargeStatisticsCallback = new ChargeStatisticsCallback();
    ChargeSessionsCallback chargeSessionCallback = new ChargeSessionsCallback();
    ByteResponseCallback imageCallback = new ImageCallback();

    public VehicleHandler(Thing thing, MyBMWCommandOptionProvider cop, LocationProvider lp, String driveTrain) {
        super(thing, cop, lp, driveTrain);
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
            if (command instanceof StringType str) {
                String serviceCommand = str.toFullString();
                remote.ifPresent(remot -> {
                    switch (serviceCommand) {
                        case REMOTE_SERVICE_LIGHT_FLASH:
                        case REMOTE_SERVICE_DOOR_LOCK:
                        case REMOTE_SERVICE_DOOR_UNLOCK:
                        case REMOTE_SERVICE_HORN:
                        case REMOTE_SERVICE_VEHICLE_FINDER:
                            RemoteServiceUtils.getRemoteService(serviceCommand)
                                    .ifPresentOrElse(service -> remot.execute(service), () -> {
                                        logger.debug("Remote service execution {} unknown", serviceCommand);
                                    });
                            break;
                        case REMOTE_SERVICE_AIR_CONDITIONING_START:
                            RemoteServiceUtils.getRemoteService(serviceCommand)
                                    .ifPresentOrElse(service -> remot.execute(service), () -> {
                                        logger.debug("Remote service execution {} unknown", serviceCommand);
                                    });
                            break;
                        case REMOTE_SERVICE_AIR_CONDITIONING_STOP:
                            RemoteServiceUtils.getRemoteService(serviceCommand)
                                    .ifPresentOrElse(service -> remot.execute(service), () -> {
                                        logger.debug("Remote service execution {} unknown", serviceCommand);
                                    });
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
                                imageProperties = new ImageProperties(newViewport);
                                imageCache = Optional.empty();
                                proxy.ifPresent(prox -> prox.requestImage(config, imageProperties, imageCallback));
                            }
                        }
                        updateChannel(CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_VIEWPORT, StringType.valueOf(newViewport));
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
        } else if (CHANNEL_GROUP_CHARGE_SESSION.equals(group)) {
            if (command instanceof StringType) {
                int index = Converter.getIndex(command.toFullString());
                if (index != -1) {
                    selectSession(index);
                } else {
                    logger.debug("Cannot select Session index {}", command.toFullString());
                }
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        final VehicleConfiguration config = getConfigAs(VehicleConfiguration.class);
        configuration = Optional.of(config);
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                proxy = ((MyBMWBridgeHandler) handler).getProxy();
                remote = proxy.map(prox -> prox.getRemoteServiceHandler(this));
            } else {
                logger.debug("Bridge Handler null");
            }
        } else {
            logger.debug("Bridge null");
        }

        imageProperties = new ImageProperties();
        updateChannel(CHANNEL_GROUP_VEHICLE_IMAGE, IMAGE_VIEWPORT, StringType.valueOf(imageProperties.viewport));

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
                prox.requestVehicles(config.vehicleBrand, vehicleStatusCallback);
                if (isElectric) {
                    prox.requestChargeStatistics(config, chargeStatisticsCallback);
                    prox.requestChargeSessions(config, chargeSessionCallback);
                }
                if (imageCache.isEmpty() && !imageProperties.failLimitReached()) {
                    prox.requestImage(config, imageProperties, imageCallback);
                }
            }, () -> {
                logger.warn("MyBMW Vehicle Configuration isn't present");
            });
        }, () -> {
            logger.warn("MyBMWProxy isn't present");
        });
    }

    public void updateRemoteExecutionStatus(@Nullable String service, String status) {
        updateChannel(CHANNEL_GROUP_REMOTE, REMOTE_STATE,
                StringType.valueOf((service == null ? "-" : service) + Constants.SPACE + status.toLowerCase()));
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
                        vehicleStatusCache = Optional.of(content);
                        updateStatus(ThingStatus.ONLINE);
                        updateChannel(CHANNEL_GROUP_STATUS, RAW,
                                StringType.valueOf(Converter.getRawVehicleContent(configuration.get().vin, content)));
                        updateVehicle(v);
                        if (isElectric) {
                            updateChargeProfile(v.status.chargingProfile);
                        }
                    } else {
                        logger.debug("Vehicle {} not valid", configuration.get().vin);
                    }
                } else {
                    logger.debug("configuration not present");
                }
            } else {
                updateChannel(CHANNEL_GROUP_STATUS, RAW, StringType.valueOf(Constants.EMPTY_JSON));
                logger.debug("Content not valid");
            }
        }

        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
            vehicleStatusCache = Optional.of(Converter.getGson().toJson(error));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.reason);
        }
    }

    public class ChargeStatisticsCallback implements StringResponseCallback {
        @Override
        public void onResponse(@Nullable String content) {
            if (content != null) {
                try {
                    ChargeStatisticsContainer csc = Converter.getGson().fromJson(content,
                            ChargeStatisticsContainer.class);
                    if (csc != null) {
                        updateChargeStatistics(csc);
                    }
                } catch (JsonSyntaxException jse) {
                    logger.warn("{}", jse.getLocalizedMessage());
                }
            } else {
                logger.debug("Content not valid");
            }
        }

        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
        }
    }

    public class ChargeSessionsCallback implements StringResponseCallback {
        @Override
        public void onResponse(@Nullable String content) {
            if (content != null) {
                try {
                    ChargeSessionsContainer csc = Converter.getGson().fromJson(content, ChargeSessionsContainer.class);
                    if (csc != null) {
                        if (csc.chargingSessions != null) {
                            updateSessions(csc.chargingSessions.sessions);
                        }
                    }
                } catch (JsonSyntaxException jse) {
                    logger.warn("{}", jse.getLocalizedMessage());
                }
            } else {
                logger.debug("Content not valid");
            }
        }

        @Override
        public void onError(NetworkError error) {
            logger.debug("{}", error.toString());
        }
    }
}
