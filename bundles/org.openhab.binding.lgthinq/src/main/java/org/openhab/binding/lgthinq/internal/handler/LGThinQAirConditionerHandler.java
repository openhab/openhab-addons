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
package org.openhab.binding.lgthinq.internal.handler;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.LGThinQDeviceDynStateDescriptionProvider;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.LGThinQACApiClientService;
import org.openhab.binding.lgthinq.lgservices.LGThinQACApiV1ClientServiceImpl;
import org.openhab.binding.lgthinq.lgservices.LGThinQACApiV2ClientServiceImpl;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientService;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACTargetTmp;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinQAirConditionerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQAirConditionerHandler extends LGThinQAbstractDeviceHandler<ACCapability, ACCanonicalSnapshot> {

    private final ChannelUID opModeChannelUID;
    private final ChannelUID fanSpeedChannelUID;
    private final ChannelUID jetModeChannelUID;
    private final ChannelUID airCleanChannelUID;
    private final ChannelUID autoDryChannelUID;
    private final ChannelUID energySavingChannelUID;

    private final Logger logger = LoggerFactory.getLogger(LGThinQAirConditionerHandler.class);
    @NonNullByDefault
    private final LGThinQACApiClientService lgThinqACApiClientService;
    private @Nullable ScheduledFuture<?> thingStatePollingJob;

    public LGThinQAirConditionerHandler(Thing thing,
            LGThinQDeviceDynStateDescriptionProvider stateDescriptionProvider) {
        super(thing, stateDescriptionProvider);
        lgThinqACApiClientService = lgPlatformType.equals(PLATFORM_TYPE_V1)
                ? LGThinQACApiV1ClientServiceImpl.getInstance()
                : LGThinQACApiV2ClientServiceImpl.getInstance();
        opModeChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_MOD_OP_ID);
        fanSpeedChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_FAN_SPEED_ID);
        jetModeChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_COOL_JET_ID);
        airCleanChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_AIR_CLEAN_ID);
        autoDryChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_AUTO_DRY_ID);
        energySavingChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_ENERGY_SAVING_ID);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Thinq thing.");
        Bridge bridge = getBridge();
        initializeThing((bridge == null) ? null : bridge.getStatus());
    }

    @Override
    protected void updateDeviceChannels(ACCanonicalSnapshot shot) {

        updateState(CHANNEL_POWER_ID,
                DevicePowerState.DV_POWER_ON.equals(shot.getPowerStatus()) ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_MOD_OP_ID, new DecimalType(BigDecimal.valueOf(shot.getOperationMode())));
        updateState(CHANNEL_FAN_SPEED_ID, new DecimalType(BigDecimal.valueOf(shot.getAirWindStrength())));
        updateState(CHANNEL_CURRENT_TEMP_ID, new DecimalType(BigDecimal.valueOf(shot.getCurrentTemperature())));
        updateState(CHANNEL_TARGET_TEMP_ID, new DecimalType(BigDecimal.valueOf(shot.getTargetTemperature())));
        try {
            ACCapability acCap = getCapabilities();
            if (getThing().getChannel(jetModeChannelUID) != null) {
                Double commandCoolJetOn = Double.valueOf(acCap.getCoolJetModeCommandOn());
                updateState(CHANNEL_COOL_JET_ID,
                        commandCoolJetOn.equals(shot.getCoolJetMode()) ? OnOffType.ON : OnOffType.OFF);
            }
            if (getThing().getChannel(airCleanChannelUID) != null) {
                Double commandAirCleanOn = Double.valueOf(acCap.getAirCleanModeCommandOn());
                updateState(CHANNEL_AIR_CLEAN_ID,
                        commandAirCleanOn.equals(shot.getAirCleanMode()) ? OnOffType.ON : OnOffType.OFF);
            }
            if (getThing().getChannel(energySavingChannelUID) != null) {
                Double energySavingOn = Double.valueOf(acCap.getEnergySavingModeCommandOn());
                updateState(CHANNEL_ENERGY_SAVING_ID,
                        energySavingOn.equals(shot.getEnergySavingMode()) ? OnOffType.ON : OnOffType.OFF);
            }
            if (getThing().getChannel(autoDryChannelUID) != null) {
                Double autoDryOn = Double.valueOf(acCap.getCoolJetModeCommandOn());
                updateState(CHANNEL_AUTO_DRY_ID,
                        autoDryOn.equals(shot.getAutoDryMode()) ? OnOffType.ON : OnOffType.OFF);
            }

        } catch (LGThinqApiException e) {
            logger.error("Unexpected Error gettinf ACCapability Capabilities", e);
        } catch (NumberFormatException e) {
            logger.warn("command value for capability is not numeric.", e);
        }
    }

    @Override
    public void updateChannelDynStateDescription() throws LGThinqApiException {
        ACCapability acCap = getCapabilities();
        if (getThing().getChannel(jetModeChannelUID) == null && acCap.isJetModeAvailable()) {
            createDynSwitchChannel(CHANNEL_COOL_JET_ID, jetModeChannelUID);
        }
        if (getThing().getChannel(autoDryChannelUID) == null && acCap.isAutoDryModeAvailable()) {
            createDynSwitchChannel(CHANNEL_AUTO_DRY_ID, autoDryChannelUID);
        }
        if (getThing().getChannel(airCleanChannelUID) == null && acCap.isAirCleanAvailable()) {
            createDynSwitchChannel(CHANNEL_AIR_CLEAN_ID, airCleanChannelUID);
        }
        if (getThing().getChannel(energySavingChannelUID) == null && acCap.isEnergySavingAvailable()) {
            createDynSwitchChannel(CHANNEL_ENERGY_SAVING_ID, energySavingChannelUID);
        }
        if (getThing().getChannel(fanSpeedChannelUID) == null && !acCap.getFanSpeed().isEmpty()) {
            List<StateOption> options = new ArrayList<>();
            acCap.getFanSpeed()
                    .forEach((k, v) -> options.add(new StateOption(k, emptyIfNull(CAP_AC_FAN_SPEED.get(v)))));
            stateDescriptionProvider.setStateOptions(fanSpeedChannelUID, options);
        }
        if (isLinked(opModeChannelUID)) {
            List<StateOption> options = new ArrayList<>();
            acCap.getOpMode().forEach((k, v) -> options.add(new StateOption(k, emptyIfNull(CAP_AC_OP_MODE.get(v)))));
            stateDescriptionProvider.setStateOptions(opModeChannelUID, options);
        }
    }

    @Override
    public LGThinQApiClientService<ACCapability, ACCanonicalSnapshot> getLgThinQAPIClientService() {
        return lgThinqACApiClientService;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    protected void stopThingStatePolling() {
        if (thingStatePollingJob != null && !thingStatePollingJob.isDone()) {
            logger.debug("Stopping LG thinq polling for device/alias: {}/{}", getDeviceId(), getDeviceAlias());
            thingStatePollingJob.cancel(true);
        }
    }

    protected DeviceTypes getDeviceType() {
        if (THING_TYPE_HEAT_PUMP.equals(getThing().getThingTypeUID())) {
            return DeviceTypes.HEAT_PUMP;
        } else if (THING_TYPE_AIR_CONDITIONER.equals(getThing().getThingTypeUID())) {
            return DeviceTypes.AIR_CONDITIONER;
        } else {
            throw new IllegalArgumentException(
                    "DeviceTypeUuid [" + getThing().getThingTypeUID() + "] not expected for AirConditioner/HeatPump");
        }
    }

    @Override
    public void onDeviceAdded(LGDevice device) {
        // TODO - handle it. Think if it's needed
    }

    @Override
    public String getDeviceAlias() {
        return emptyIfNull(getThing().getProperties().get(DEVICE_ALIAS));
    }

    @Override
    public String getDeviceUriJsonConfig() {
        return emptyIfNull(getThing().getProperties().get(MODEL_URL_INFO));
    }

    @Override
    public void onDeviceRemoved() {
        // TODO - HANDLE IT, Think if it's needed
    }

    @Override
    public void onDeviceDisconnected() {
        // TODO - HANDLE IT, Think if it's needed
    }

    protected void processCommand(AsyncCommandParams params) throws LGThinqApiException {
        Command command = params.command;
        switch (params.channelUID) {
            case CHANNEL_MOD_OP_ID: {
                if (params.command instanceof DecimalType) {
                    lgThinqACApiClientService.changeOperationMode(getBridgeId(), getDeviceId(),
                            ((DecimalType) command).intValue());
                } else {
                    logger.warn("Received command different of Numeric in Mod Operation. Ignoring");
                }
                break;
            }
            case CHANNEL_FAN_SPEED_ID: {
                if (command instanceof DecimalType) {
                    lgThinqACApiClientService.changeFanSpeed(getBridgeId(), getDeviceId(),
                            ((DecimalType) command).intValue());
                } else {
                    logger.warn("Received command different of Numeric in FanSpeed Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_POWER_ID: {
                if (command instanceof OnOffType) {
                    lgThinqACApiClientService.turnDevicePower(getBridgeId(), getDeviceId(),
                            command == OnOffType.ON ? DevicePowerState.DV_POWER_ON : DevicePowerState.DV_POWER_OFF);
                } else {
                    logger.warn("Received command different of OnOffType in Power Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_COOL_JET_ID: {
                if (command instanceof OnOffType) {
                    lgThinqACApiClientService.turnCoolJetMode(getBridgeId(), getDeviceId(),
                            command == OnOffType.ON ? getCapabilities().getCoolJetModeCommandOn()
                                    : getCapabilities().getCoolJetModeCommandOff());
                } else {
                    logger.warn("Received command different of OnOffType in CoolJet Mode Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_AIR_CLEAN_ID: {
                if (command instanceof OnOffType) {
                    lgThinqACApiClientService.turnAirCleanMode(getBridgeId(), getDeviceId(),
                            command == OnOffType.ON ? getCapabilities().getAirCleanModeCommandOn()
                                    : getCapabilities().getAirCleanModeCommandOff());
                } else {
                    logger.warn("Received command different of OnOffType in AirClean Mode Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_AUTO_DRY_ID: {
                if (command instanceof OnOffType) {
                    lgThinqACApiClientService.turnAutoDryMode(getBridgeId(), getDeviceId(),
                            command == OnOffType.ON ? getCapabilities().getAutoDryModeCommandOn()
                                    : getCapabilities().getAutoDryModeCommandOff());
                } else {
                    logger.warn("Received command different of OnOffType in AutoDry Mode Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_ENERGY_SAVING_ID: {
                if (command instanceof OnOffType) {
                    lgThinqACApiClientService.turnEnergySavingMode(getBridgeId(), getDeviceId(),
                            command == OnOffType.ON ? getCapabilities().getEnergySavingModeCommandOn()
                                    : getCapabilities().getEnergySavingModeCommandOff());
                } else {
                    logger.warn("Received command different of OnOffType in EvergySaving Mode Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_TARGET_TEMP_ID: {
                double targetTemp;
                if (command instanceof DecimalType) {
                    targetTemp = ((DecimalType) command).doubleValue();
                } else if (command instanceof QuantityType) {
                    targetTemp = ((QuantityType<?>) command).doubleValue();
                } else {
                    logger.warn("Received command different of Numeric in TargetTemp Channel. Ignoring");
                    break;
                }
                lgThinqACApiClientService.changeTargetTemperature(getBridgeId(), getDeviceId(),
                        ACTargetTmp.statusOf(targetTemp));
                break;
            }
            default: {
                logger.error("Command {} to the channel {} not supported. Ignored.", command, params.channelUID);
            }
        }
    }
}
