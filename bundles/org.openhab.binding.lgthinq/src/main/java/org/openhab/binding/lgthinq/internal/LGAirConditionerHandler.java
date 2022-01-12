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
package org.openhab.binding.lgthinq.internal;

import static org.openhab.binding.lgthinq.internal.LGThinqBindingConstants.*;
import static org.openhab.core.library.types.OnOffType.ON;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.errors.LGThinqException;
import org.openhab.binding.lgthinq.handler.LGBridgeHandler;
import org.openhab.binding.lgthinq.lgapi.LGApiClientServiceImpl;
import org.openhab.binding.lgthinq.lgapi.LGApiV2ClientService;
import org.openhab.binding.lgthinq.lgapi.model.*;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGAirConditionerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGAirConditionerHandler extends BaseThingHandler implements LGDeviceThing {
    public static final ThingTypeUID THING_TYPE_AIR_CONDITIONER = new ThingTypeUID(BINDING_ID,
            "" + DeviceTypes.AIR_CONDITIONER.deviceTypeId()); // deviceType from AirConditioner

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_AIR_CONDITIONER);

    private final Logger logger = LoggerFactory.getLogger(LGAirConditionerHandler.class);
    private final LGApiV2ClientService lgApiClientService;
    private @Nullable LGThinqConfiguration config;
    private ThingStatus lastThingStatus = ThingStatus.UNKNOWN;
    // Bridges status that this thing must top scanning for state change
    private static final Set<ThingStatusDetail> BRIDGE_STATUS_DETAIL_ERROR = Set.of(ThingStatusDetail.BRIDGE_OFFLINE,
            ThingStatusDetail.BRIDGE_UNINITIALIZED, ThingStatusDetail.COMMUNICATION_ERROR,
            ThingStatusDetail.CONFIGURATION_ERROR);
    private @Nullable ScheduledFuture<?> thingStatePoolingJob;

    public LGAirConditionerHandler(Thing thing) {
        super(thing);
        lgApiClientService = LGApiClientServiceImpl.getInstance();
    }

    class UpdateThingStatusRunnable implements Runnable {

        @Override
        public void run() {
            final String deviceId = getDeviceId();
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return super.getServices();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue light handler.");
        Bridge bridge = getBridge();
        initializeThing((bridge == null) ? null : bridge.getStatus());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        initializeThing(bridgeStatusInfo.getStatus());
    }

    private void initializeThing(@Nullable ThingStatus bridgeStatus) {
        logger.debug("initializeThing LQ Thinq {}. Bridge status {}", getThing().getUID(), bridgeStatus);
        String deviceId = getThing().getUID().getId();
        Bridge bridge = getBridge();
        if (!deviceId.isBlank()) {
            if (bridge != null) {
                LGBridgeHandler handler = (LGBridgeHandler) bridge.getHandler();
                // registry this thing to the bridge
                if (handler == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
                } else {
                    handler.registryListenerThing(this);
                    if (bridgeStatus == ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-device-id");
        }
    }

    protected void startThingStatePooling() {
        if (thingStatePoolingJob == null || thingStatePoolingJob.isDone()) {
            thingStatePoolingJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    ACSnapShot shot = lgApiClientService.getAcDeviceData(getDeviceId());
                    if (shot.getAcOpMode() != null) {
                        updateState(CHANNEL_MOD_OP_ID, new DecimalType(shot.getOperationMode()));
                    }
                    if (shot.getAcPowerStatus() != null) {
                        updateState(CHANNEL_POWER_ID,
                                OnOffType.from(shot.getAcPowerStatus() == DevicePowerState.DV_POWER_ON));
                        // TODO - validate if is needed to change the status of the thing from OFFLINE to ONLINE (as
                        // soon as LG WebOs do)
                    }
                    if (shot.getAcFanSpeed() != null) {
                        updateState(CHANNEL_FAN_SPEED_ID, new DecimalType(shot.getAirWindStrength()));
                    }
                    if (shot.getCurrentTemperature() != null) {
                        updateState(CHANNEL_CURRENT_TEMP_ID, new DecimalType(shot.getCurrentTemperature()));
                    }
                    if (shot.getTargetTemperature() != null) {
                        updateState(CHANNEL_TARGET_TEMP_ID, new DecimalType(shot.getTargetTemperature()));
                    }
                    updateStatus(ThingStatus.ONLINE);
                } catch (LGThinqException e) {
                    logger.error("Error updating thing {}/{} from LG API. Thing goes OFFLINE until next retry.",
                            getDeviceAlias(), getDeviceId());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }, 10, DEFAULT_STATE_POOLING_UPDATE_DELAY, TimeUnit.SECONDS);
        }
    }

    protected void stopThingStatePooling() {
        if (thingStatePoolingJob != null && !thingStatePoolingJob.isDone()) {
            logger.debug("Stopping LG thinq pooling for device/alias: {}/{}", getDeviceId(), getDeviceAlias());
            thingStatePoolingJob.cancel(true);
        }
    }

    private void handleStatusChanged(ThingStatus newStatus, ThingStatusDetail statusDetail) {
        if (lastThingStatus != ThingStatus.ONLINE && newStatus == ThingStatus.ONLINE) {
            // start the thing pooling
            startThingStatePooling();
        } else if (lastThingStatus == ThingStatus.ONLINE && newStatus == ThingStatus.OFFLINE
                && BRIDGE_STATUS_DETAIL_ERROR.contains(statusDetail)) {
            // comunication error is not a specific Bridge error, then we must analise it to give
            // this thinq the change to recovery from communication errors
            if (statusDetail != ThingStatusDetail.COMMUNICATION_ERROR
                    || (getBridge() != null && getBridge().getStatus() != ThingStatus.ONLINE)) {
                // in case of status offline, I only stop the pooling if is not an COMMUNICATION_ERROR or if
                // the bridge is out
                stopThingStatePooling();
            }

        }
        lastThingStatus = newStatus;
    }

    @Override
    protected void updateStatus(ThingStatus newStatus, ThingStatusDetail statusDetail, @Nullable String description) {
        handleStatusChanged(newStatus, statusDetail);
        super.updateStatus(newStatus, statusDetail, description);
    }

    @Override
    protected void updateStatus(ThingStatus newStatus, ThingStatusDetail statusDetail) {
        handleStatusChanged(newStatus, statusDetail);
        super.updateStatus(newStatus, statusDetail);
    }

    @Override
    protected void updateStatus(ThingStatus newStatus) {
        // Probably just turned off.
        handleStatusChanged(newStatus, ThingStatusDetail.NONE);
        super.updateStatus(newStatus);
    }

    @Override
    public void onDeviceAdded(LGDevice device) {
        // TODO - handle it
    }

    @Override
    public String getDeviceId() {
        return getThing().getUID().getId();
    }

    @Override
    public String getDeviceAlias() {
        return "" + getThing().getProperties().get(DEVICE_ALIAS);
    }

    @Override
    public String getDeviceModelName() {
        return "" + getThing().getProperties().get(MODEL_NAME);
    }

    @Override
    public boolean onDeviceStateChanged() {
        // TODO - HANDLE IT
        return false;
    }

    @Override
    public void onDeviceRemoved() {
        // TODO - HANDLE IT
    }

    @Override
    public void onDeviceGone() {
        // TODO - HANDLE IT
    }

    @Override
    public void dispose() {
        if (thingStatePoolingJob != null) {
            thingStatePoolingJob.cancel(true);
            thingStatePoolingJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // TODO - implement refresh channels
        } else {
            try {
                switch (channelUID.getId()) {
                    case CHANNEL_MOD_OP_ID: {
                        if (command instanceof DecimalType) {
                            lgApiClientService.changeOperationMode(getDeviceId(),
                                    ACOpMode.statusOf(((DecimalType) command).doubleValue()));
                        } else {
                            logger.warn("Received command different of Numeric in Mod Operation. Ignoring");
                        }
                        break;
                    }
                    case CHANNEL_FAN_SPEED_ID: {
                         if (command instanceof DecimalType) {
                             lgApiClientService.changeFanSpeed(getDeviceId(),
                                     ACFanSpeed.statusOf(((DecimalType) command).doubleValue()));
                         } else {
                             logger.warn("Received command different of Numeric in FanSpeed Channel. Ignoring");
                         }
                         break;
                     }
                    case CHANNEL_POWER_ID: {
                        if (command instanceof OnOffType) {
                            lgApiClientService.turnDevicePower(getDeviceId(),
                                    command == ON ? DevicePowerState.DV_POWER_ON : DevicePowerState.DV_POWER_OFF);
                        } else {
                            logger.warn("Received command different of OnOffType in Power Channel. Ignoring");
                        }
                        break;
                    }
                     case CHANNEL_TARGET_TEMP_ID: {
                         if (command instanceof DecimalType) {
                             lgApiClientService.changeTargetTemperature(getDeviceId(),
                                     ACTargetTmp.statusOf(((DecimalType) command).doubleValue()));
                         } else {
                             logger.warn("Received command different of Numeric in TargetTemp Channel. Ignoring");
                         }
                         break;
                     }
                    default: {
                        logger.error("Command {} to the channel {} not supported. Ignored.", command.toString(),
                                channelUID.getId());
                    }
                }
            } catch (LGThinqException e) {
                logger.error("Error executing Command {} to the channel {}. Thing goes offline until retry",
                        command, channelUID.getId());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }
}
