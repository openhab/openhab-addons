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
package org.openhab.binding.lgthinq.internal.handler;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.LGThinQStateDescriptionProvider;
import org.openhab.binding.lgthinq.lgservices.LGThinQACApiClientService;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientService;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientServiceFactory;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACTargetTmp;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ExtendedDeviceInfo;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link LGThinQAirConditionerHandler} Handle Air Conditioner and HeatPump Things
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQAirConditionerHandler extends LGThinQAbstractDeviceHandler<ACCapability, ACCanonicalSnapshot> {

    private final DeviceChannelGroup dashboard;
    private final DeviceChannelGroup extendedInfo;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(LGThinQAirConditionerHandler.class);
    private final LGThinQACApiClientService lgThinqACApiClientService;
    private double minTempConstraint = 16, maxTempConstraint = 30;

    public LGThinQAirConditionerHandler(Thing thing, LGThinQStateDescriptionProvider stateDescriptionProvider,
            ItemChannelLinkRegistry itemChannelLinkRegistry, HttpClientFactory httpClientFactory) {
        super(thing, stateDescriptionProvider, itemChannelLinkRegistry);
        lgThinqACApiClientService = LGThinQApiClientServiceFactory.newACApiClientService(lgPlatformType,
                httpClientFactory);
        dashboard = new DeviceChannelGroup(getThing().getUID(), CHANNEL_DASHBOARD_GRP_ID);
        extendedInfo = new DeviceChannelGroup(getThing().getUID(), CHANNEL_EXTENDED_INFO_GRP_ID);
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            ACCapability cap = getCapabilities();
            if (!isExtraInfoCollectorSupported()) {
                ThingBuilder builder = editThing()
                        .withoutChannels(this.getThing().getChannelsOfGroup(extendedInfo.uid().getId()));
                updateThing(builder.build());
            } else if (!cap.isEnergyMonitorAvailable()) {
                ThingBuilder builder = editThing().withoutChannel(extendedInfo.channel(CHANNEL_AC_CURRENT_ENERGY_ID));
                updateThing(builder.build());
            } else if (!cap.isFilterMonitorAvailable()) {
                ThingBuilder builder = editThing().withoutChannel(extendedInfo.channel(CHANNEL_AC_REMAINING_FILTER_ID));
                updateThing(builder.build());
            }
        } catch (LGThinqApiException e) {
            logger.warn("Error getting capability of the device: {}", getDeviceId());
        }
    }

    @Override
    protected void updateDeviceChannels(ACCanonicalSnapshot shot) {
        updateState(dashboard.channel(CHANNEL_AC_POWER_ID),
                OnOffType.from(DevicePowerState.DV_POWER_ON == shot.getPowerStatus()));
        updateState(dashboard.channel(CHANNEL_AC_MOD_OP_ID), new DecimalType(shot.getOperationMode()));
        if (DeviceTypes.HEAT_PUMP.equals(getDeviceType())) {
            updateState(dashboard.channel(CHANNEL_AC_AIR_WATER_SWITCH_ID),
                    new DecimalType(shot.getHpAirWaterTempSwitch()));
        }
        updateState(dashboard.channel(CHANNEL_AC_FAN_SPEED_ID), new DecimalType(shot.getAirWindStrength()));
        updateState(dashboard.channel(CHANNEL_AC_CURRENT_TEMP_ID), new DecimalType(shot.getCurrentTemperature()));
        updateState(dashboard.channel(CHANNEL_AC_TARGET_TEMP_ID), new DecimalType(shot.getTargetTemperature()));
        try {
            ACCapability acCap = getCapabilities();
            if (getThing().getChannel(dashboard.channel(CHANNEL_AC_STEP_UP_DOWN_ID)) != null) {
                updateState(dashboard.channel(CHANNEL_AC_STEP_UP_DOWN_ID),
                        new DecimalType((int) shot.getStepUpDownMode()));
            }
            if (getThing().getChannel(dashboard.channel(CHANNEL_AC_STEP_LEFT_RIGHT_ID)) != null) {
                updateState(dashboard.channel(CHANNEL_AC_STEP_LEFT_RIGHT_ID),
                        new DecimalType((int) shot.getStepLeftRightMode()));
            }
            if (getThing().getChannel(dashboard.channel(CHANNEL_AC_COOL_JET_ID)) != null) {
                Double commandCoolJetOn = Double.valueOf(acCap.getCoolJetModeCommandOn());
                updateState(dashboard.channel(CHANNEL_AC_COOL_JET_ID),
                        OnOffType.from(commandCoolJetOn.equals(shot.getCoolJetMode())));
            }
            if (getThing().getChannel(dashboard.channel(CHANNEL_AC_AIR_CLEAN_ID)) != null) {
                Double commandAirCleanOn = Double.valueOf(acCap.getAirCleanModeCommandOn());
                updateState(dashboard.channel(CHANNEL_AC_AIR_CLEAN_ID),
                        OnOffType.from(commandAirCleanOn.equals(shot.getAirCleanMode())));
            }
            if (getThing().getChannel(dashboard.channel(CHANNEL_AC_ENERGY_SAVING_ID)) != null) {
                Double energySavingOn = Double.valueOf(acCap.getEnergySavingModeCommandOn());
                updateState(dashboard.channel(CHANNEL_AC_ENERGY_SAVING_ID),
                        OnOffType.from(energySavingOn.equals(shot.getEnergySavingMode())));
            }
            if (getThing().getChannel(dashboard.channel(CHANNEL_AC_AUTO_DRY_ID)) != null) {
                Double autoDryOn = Double.valueOf(acCap.getAutoDryModeCommandOn());
                updateState(dashboard.channel(CHANNEL_AC_AUTO_DRY_ID),
                        OnOffType.from(autoDryOn.equals(shot.getAutoDryMode())));
            }
            if (DeviceTypes.HEAT_PUMP.equals(getDeviceType())) {
                // HP has different combination of min and max target temperature depending on the switch mode and
                // operation mode
                String opModeValue = Objects
                        .requireNonNullElse(acCap.getOpMode().get(getLastShot().getOperationMode().toString()), "");
                if (CAP_HP_AIR_SWITCH.equals(shot.getHpAirWaterTempSwitch())) {
                    if (opModeValue.equals(CAP_ACHP_OP_MODE_COOL_KEY)) {
                        minTempConstraint = shot.getHpAirTempCoolMin();
                        maxTempConstraint = shot.getHpAirTempCoolMax();
                    } else if (opModeValue.equals(CAP_ACHP_OP_MODE_HEAT_KEY)) {
                        minTempConstraint = shot.getHpAirTempHeatMin();
                        maxTempConstraint = shot.getHpAirTempHeatMax();
                    }
                } else if (CAP_HP_WATER_SWITCH.equals(shot.getHpAirWaterTempSwitch())) {
                    if (opModeValue.equals(CAP_ACHP_OP_MODE_COOL_KEY)) {
                        minTempConstraint = shot.getHpWaterTempCoolMin();
                        maxTempConstraint = shot.getHpWaterTempCoolMax();
                    } else if (opModeValue.equals(CAP_ACHP_OP_MODE_HEAT_KEY)) {
                        minTempConstraint = shot.getHpWaterTempHeatMin();
                        maxTempConstraint = shot.getHpWaterTempHeatMax();
                    }
                } else {
                    logger.warn("Invalid value received by HP snapshot for the air/water switch property: {}",
                            shot.getHpAirWaterTempSwitch());
                }
                updateState(dashboard.channel(CHANNEL_AC_MIN_TEMP_ID),
                        new DecimalType(BigDecimal.valueOf(minTempConstraint)));
                updateState(dashboard.channel(CHANNEL_AC_MAX_TEMP_ID),
                        new DecimalType(BigDecimal.valueOf(maxTempConstraint)));
            }
        } catch (LGThinqApiException e) {
            logger.error("Unexpected Error getting ACCapability Capabilities", e);
        } catch (NumberFormatException e) {
            logger.warn("command value for capability is not numeric.", e);
        }
    }

    @Override
    public void updateChannelDynStateDescription() throws LGThinqApiException {
        ACCapability acCap = getCapabilities();
        manageDynChannel(dashboard.channel(CHANNEL_AC_COOL_JET_ID), CHANNEL_AC_COOL_JET_ID, CoreItemFactory.SWITCH,
                acCap.isJetModeAvailable());
        manageDynChannel(dashboard.channel(CHANNEL_AC_AUTO_DRY_ID), CHANNEL_AC_AUTO_DRY_ID, CoreItemFactory.SWITCH,
                acCap.isAutoDryModeAvailable());
        manageDynChannel(dashboard.channel(CHANNEL_AC_AIR_CLEAN_ID), CHANNEL_AC_AIR_CLEAN_ID, CoreItemFactory.SWITCH,
                acCap.isAirCleanAvailable());
        manageDynChannel(dashboard.channel(CHANNEL_AC_ENERGY_SAVING_ID), CHANNEL_AC_ENERGY_SAVING_ID,
                CoreItemFactory.SWITCH, acCap.isEnergySavingAvailable());
        manageDynChannel(dashboard.channel(CHANNEL_AC_STEP_UP_DOWN_ID), CHANNEL_AC_STEP_UP_DOWN_ID,
                CoreItemFactory.SWITCH, acCap.isStepUpDownAvailable());
        manageDynChannel(dashboard.channel(CHANNEL_AC_STEP_LEFT_RIGHT_ID), CHANNEL_AC_STEP_LEFT_RIGHT_ID,
                CoreItemFactory.SWITCH, acCap.isStepLeftRightAvailable());

        if (!acCap.getFanSpeed().isEmpty()) {
            StateDescriptionBuilder.setStateOptions(stateDescriptionProvider,
                    dashboard.channel(CHANNEL_AC_FAN_SPEED_ID), acCap.getFanSpeed(),
                    e -> Objects.requireNonNullElse(CAP_AC_FAN_SPEED.get(e.getValue()), ""));
        }
        if (!acCap.getOpMode().isEmpty()) {
            StateDescriptionBuilder.setStateOptions(stateDescriptionProvider, dashboard.channel(CHANNEL_AC_MOD_OP_ID),
                    acCap.getOpMode(), e -> Objects.requireNonNullElse(CAP_AC_OP_MODE.get(e.getValue()), ""));
        }
        if (!acCap.getStepLeftRight().isEmpty()) {
            StateDescriptionBuilder.setStateOptions(stateDescriptionProvider,
                    dashboard.channel(CHANNEL_AC_STEP_LEFT_RIGHT_ID), acCap.getStepLeftRight(),
                    e -> Objects.requireNonNullElse(CAP_AC_STEP_LEFT_RIGHT_MODE.get(e.getValue()), ""));
        }
        if (!acCap.getStepUpDown().isEmpty()) {
            StateDescriptionBuilder.setStateOptions(stateDescriptionProvider,
                    dashboard.channel(CHANNEL_AC_STEP_UP_DOWN_ID), acCap.getStepUpDown(),
                    e -> Objects.requireNonNullElse(CAP_AC_STEP_UP_DOWN_MODE.get(e.getValue()), ""));
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
    public String getDeviceAlias() {
        return emptyIfNull(getThing().getProperties().get(PROP_INFO_DEVICE_ALIAS));
    }

    @Override
    public String getDeviceUriJsonConfig() {
        return emptyIfNull(getThing().getProperties().get(PROP_INFO_MODEL_URL_INFO));
    }

    @Override
    public void onDeviceRemoved() {
    }

    @Override
    public void onDeviceDisconnected() {
    }

    protected void resetExtraInfoChannels() {
        updateState(extendedInfo.channel(CHANNEL_AC_CURRENT_ENERGY_ID), UnDefType.UNDEF);
        if (!isExtraInfoCollectorEnabled()) { // if collector is enabled we can keep the current value
            updateState(extendedInfo.channel(CHANNEL_AC_REMAINING_FILTER_ID), UnDefType.UNDEF);
        }
    }

    protected void processCommand(AsyncCommandParams params) throws LGThinqApiException {
        Command command = params.command();
        switch (getSimpleChannelUID(params.channelUID())) {
            case CHANNEL_AC_MOD_OP_ID: {
                if (params.command() instanceof DecimalType dtCmd) {
                    lgThinqACApiClientService.changeOperationMode(getBridgeId(), getDeviceId(), dtCmd.intValue());
                } else {
                    logger.warn("Received command different of Numeric in Mod Operation. Ignoring");
                }
                break;
            }
            case CHANNEL_AC_FAN_SPEED_ID: {
                if (command instanceof DecimalType dtCmd) {
                    lgThinqACApiClientService.changeFanSpeed(getBridgeId(), getDeviceId(), dtCmd.intValue());
                } else {
                    logger.warn("Received command different of Numeric in FanSpeed Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_AC_STEP_UP_DOWN_ID: {
                if (command instanceof DecimalType dtCmd) {
                    lgThinqACApiClientService.changeStepUpDown(getBridgeId(), getDeviceId(), getLastShot(),
                            dtCmd.intValue());
                } else {
                    logger.warn("Received command different of Numeric in Step Up/Down Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_AC_STEP_LEFT_RIGHT_ID: {
                if (command instanceof DecimalType dtCmd) {
                    lgThinqACApiClientService.changeStepLeftRight(getBridgeId(), getDeviceId(), getLastShot(),
                            dtCmd.intValue());
                } else {
                    logger.warn("Received command different of Numeric in Step Left/Right Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_AC_POWER_ID: {
                if (command instanceof OnOffType ooCmd) {
                    lgThinqACApiClientService.turnDevicePower(getBridgeId(), getDeviceId(),
                            ooCmd == OnOffType.ON ? DevicePowerState.DV_POWER_ON : DevicePowerState.DV_POWER_OFF);
                } else {
                    logger.warn("Received command different of OnOffType in Power Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_AC_COOL_JET_ID: {
                if (command instanceof OnOffType ooCmd) {
                    lgThinqACApiClientService.turnCoolJetMode(getBridgeId(), getDeviceId(),
                            ooCmd == OnOffType.ON ? getCapabilities().getCoolJetModeCommandOn()
                                    : getCapabilities().getCoolJetModeCommandOff());
                } else {
                    logger.warn("Received command different of OnOffType in CoolJet Mode Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_AC_AIR_CLEAN_ID: {
                if (command instanceof OnOffType ooCmd) {
                    lgThinqACApiClientService.turnAirCleanMode(getBridgeId(), getDeviceId(),
                            ooCmd == OnOffType.ON ? getCapabilities().getAirCleanModeCommandOn()
                                    : getCapabilities().getAirCleanModeCommandOff());
                } else {
                    logger.warn("Received command different of OnOffType in AirClean Mode Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_AC_AUTO_DRY_ID: {
                if (command instanceof OnOffType ooCmd) {
                    lgThinqACApiClientService.turnAutoDryMode(getBridgeId(), getDeviceId(),
                            ooCmd == OnOffType.ON ? getCapabilities().getAutoDryModeCommandOn()
                                    : getCapabilities().getAutoDryModeCommandOff());
                } else {
                    logger.warn("Received command different of OnOffType in AutoDry Mode Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_AC_ENERGY_SAVING_ID: {
                if (command instanceof OnOffType ooCmd) {
                    lgThinqACApiClientService.turnEnergySavingMode(getBridgeId(), getDeviceId(),
                            ooCmd == OnOffType.ON ? getCapabilities().getEnergySavingModeCommandOn()
                                    : getCapabilities().getEnergySavingModeCommandOff());
                } else {
                    logger.warn("Received command different of OnOffType in EvergySaving Mode Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_AC_TARGET_TEMP_ID: {
                double targetTemp;
                if (command instanceof DecimalType dtCmd) {
                    targetTemp = dtCmd.doubleValue();
                } else if (command instanceof QuantityType<?> qtCmd) {
                    targetTemp = qtCmd.doubleValue();
                } else {
                    logger.warn("Received command different of Numeric in TargetTemp Channel. Ignoring");
                    break;
                }
                if (targetTemp > maxTempConstraint || targetTemp < minTempConstraint) {
                    logger.warn("Target Temperature: {} is out of range: {} - {}. Ignoring command", targetTemp,
                            minTempConstraint, maxTempConstraint);
                    break;
                }
                lgThinqACApiClientService.changeTargetTemperature(getBridgeId(), getDeviceId(),
                        ACTargetTmp.statusOf(targetTemp));
                break;
            }
            case CHANNEL_EXTENDED_INFO_COLLECTOR_ID: {
                break;
            }
            default: {
                logger.warn("Command {} to the channel {} not supported. Ignored.", command, params.channelUID());
            }
        }
    }

    // =========== Energy Collector Implementation =============

    @Override
    protected boolean isExtraInfoCollectorSupported() {
        try {
            return getCapabilities().isEnergyMonitorAvailable() || getCapabilities().isFilterMonitorAvailable();
        } catch (LGThinqApiException e) {
            logger.warn("Can't get capabilities of the device: {}", getDeviceId());
        }
        return false;
    }

    @Override
    protected boolean isExtraInfoCollectorEnabled() {
        String value = getItemLinkedValue(extendedInfo.channel(CHANNEL_EXTENDED_INFO_COLLECTOR_ID));
        return value != null && OnOffType.from(value) == OnOffType.ON;
    }

    @Override
    protected Map<String, Object> collectExtraInfoState() throws LGThinqException {
        ExtendedDeviceInfo info = lgThinqACApiClientService.getExtendedDeviceInfo(getBridgeId(), getDeviceId());
        Map<String, Object> result = mapper.convertValue(info, new TypeReference<>() {
        });
        return result == null ? Collections.emptyMap() : result;
    }

    @Override
    protected void updateExtraInfoStateChannels(Map<String, Object> energyStateAttributes) {
        logger.debug("Calling updateExtraInfoStateChannels for device: {}", getDeviceId());
        String instantEnergyConsumption = (String) energyStateAttributes.get(CAP_EXTRA_ATTR_INSTANT_POWER);
        String filterUsed = (String) energyStateAttributes.get(CAP_EXTRA_ATTR_FILTER_USED_TIME);
        String filterLifetime = (String) energyStateAttributes.get(CAP_EXTRA_ATTR_FILTER_MAX_TIME_TO_USE);
        if (instantEnergyConsumption == null) {
            updateState(extendedInfo.channel(CHANNEL_AC_CURRENT_ENERGY_ID), UnDefType.NULL);
        } else {
            try {
                double ip = Double.parseDouble(instantEnergyConsumption);
                updateState(extendedInfo.channel(CHANNEL_AC_CURRENT_ENERGY_ID),
                        new QuantityType<>(ip, Units.WATT_HOUR));
            } catch (NumberFormatException e) {
                updateState(extendedInfo.channel(CHANNEL_AC_CURRENT_ENERGY_ID), UnDefType.UNDEF);
            }
        }

        if (filterLifetime == null || filterUsed == null) {
            updateState(extendedInfo.channel(CHANNEL_AC_REMAINING_FILTER_ID), UnDefType.NULL);
        } else {
            try {
                double used = Double.parseDouble(filterUsed);
                double max = Double.parseDouble(filterLifetime);
                double perc = (1 - (used / max)) * 100;
                updateState(extendedInfo.channel(CHANNEL_AC_REMAINING_FILTER_ID),
                        new QuantityType<>(perc, Units.PERCENT));
            } catch (NumberFormatException ex) {
                updateState(extendedInfo.channel(CHANNEL_AC_REMAINING_FILTER_ID), UnDefType.UNDEF);
            }
        }
    }
}
