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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.LGThinQStateDescriptionProvider;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientService;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientServiceFactory;
import org.openhab.binding.lgthinq.lgservices.LGThinQFridgeApiClientService;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCapability;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinQFridgeHandler} Handle Fridge things
 *
 * @author Nemer Daud - Initial contribution
 * @author Arne Seime - Complementary sensors
 */
@NonNullByDefault
public class LGThinQFridgeHandler extends LGThinQAbstractDeviceHandler<FridgeCapability, FridgeCanonicalSnapshot> {

    private final DeviceChannelGroup dashboard;
    private final DeviceChannelGroup extendedInfo;
    /**
     * Platform-specific channel ID for the smart-saving mode channel (V1 and V2 use different channel names).
     */
    private final String smartSavingChannelId;
    private final Logger logger = LoggerFactory.getLogger(LGThinQFridgeHandler.class);
    private final LGThinQFridgeApiClientService lgThinqFridgeApiClientService;
    private String tempUnit = RE_TEMP_UNIT_CELSIUS;

    public LGThinQFridgeHandler(Thing thing, LGThinQStateDescriptionProvider stateDescriptionProvider,
            ItemChannelLinkRegistry itemChannelLinkRegistry, HttpClientFactory httpClientFactory) {
        super(thing, stateDescriptionProvider, itemChannelLinkRegistry);
        lgThinqFridgeApiClientService = LGThinQApiClientServiceFactory.newFridgeApiClientService(lgPlatformType,
                httpClientFactory);
        dashboard = new DeviceChannelGroup(getThing().getUID(), CHANNEL_DASHBOARD_GRP_ID);
        extendedInfo = new DeviceChannelGroup(getThing().getUID(), CHANNEL_EXTENDED_INFO_GRP_ID);
        smartSavingChannelId = LG_API_PLATFORM_TYPE_V2.equals(lgPlatformType) ? CHANNEL_FR_SMART_SAVING_MODE_V2
                : CHANNEL_FR_SMART_SAVING_SWITCH_V1;
    }

    private Unit<Temperature> getTemperatureUnit(FridgeCanonicalSnapshot shot) {
        if (!(RE_CELSIUS_UNIT_VALUES.contains(shot.getTempUnit())
                || RE_FAHRENHEIT_UNIT_VALUES.contains(shot.getTempUnit()))) {
            logger.warn(
                    "Temperature Unit not recognized (must be Celsius or Fahrenheit). Ignoring and considering Celsius as default");
            return SIUnits.CELSIUS;
        }
        return RE_CELSIUS_UNIT_VALUES.contains(shot.getTempUnit()) ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT;
    }

    @Override
    protected void updateDeviceChannels(FridgeCanonicalSnapshot shot) {
        Unit<Temperature> unTemp = getTemperatureUnit(shot);
        ChannelUID fridgeTempUID = dashboard.channel(CHANNEL_FR_FRIDGE_TEMP_ID);
        ChannelUID freezerTempUID = dashboard.channel(CHANNEL_FR_FREEZER_TEMP_ID);
        if (isLinked(fridgeTempUID)) {
            updateState(fridgeTempUID,
                    new QuantityType<>(decodeTempValue(fridgeTempUID, shot.getFridgeTemp().intValue()), unTemp));
        }
        if (isLinked(freezerTempUID)) {
            updateState(freezerTempUID,
                    new QuantityType<>(decodeTempValue(freezerTempUID, shot.getFreezerTemp().intValue()), unTemp));
        }
        if (isLinked(dashboard.channel(CHANNEL_FR_DOOR_OPEN))) {
            updateState(dashboard.channel(CHANNEL_FR_DOOR_OPEN), parseDoorStatus(shot.getDoorStatus()));
        }
        if (isLinked(dashboard.channel(CHANNEL_FR_EXPRESS_FREEZE_MODE))) {
            updateState(dashboard.channel(CHANNEL_FR_EXPRESS_FREEZE_MODE), new StringType(shot.getExpressMode()));
        }
        if (isLinked(dashboard.channel(CHANNEL_FR_EXPRESS_COOL_MODE))) {
            updateState(dashboard.channel(CHANNEL_FR_EXPRESS_COOL_MODE), OnOffType.from(shot.getExpressCoolMode()));
        }
        if (isLinked(dashboard.channel(CHANNEL_FR_VACATION_MODE))) {
            updateState(dashboard.channel(CHANNEL_FR_VACATION_MODE), OnOffType.from(shot.getEcoFriendlyMode()));
        }
        if (isLinked(extendedInfo.channel(CHANNEL_FR_FRESH_AIR_FILTER))) {
            updateState(extendedInfo.channel(CHANNEL_FR_FRESH_AIR_FILTER),
                    new StringType(shot.getFreshAirFilterState()));
        }
        if (isLinked(extendedInfo.channel(CHANNEL_FR_WATER_FILTER))) {
            updateState(extendedInfo.channel(CHANNEL_FR_WATER_FILTER), new StringType(shot.getWaterFilterUsedMonth()));
        }

        updateState(dashboard.channel(CHANNEL_FR_REF_TEMP_UNIT), new StringType(shot.getTempUnit()));
        if (!tempUnit.equals(shot.getTempUnit())) {
            tempUnit = shot.getTempUnit();
            try {
                // force update states after first snapshot fetched to fit changes in temperature unit
                updateChannelDynStateDescription();
            } catch (Exception ex) {
                logger.warn("Unexpected error updating dynamic state description.", ex);
            }
        }
    }

    private State parseDoorStatus(String doorStatus) {
        if (RE_DOOR_CLOSE_VALUES.contains(doorStatus)) {
            return OpenClosedType.CLOSED;
        } else if (RE_DOOR_OPEN_VALUES.contains(doorStatus)) {
            return OpenClosedType.OPEN;
        } else {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Decodes a raw temperature value from the device into a human-readable integer using the capability's conversion
     * map.
     *
     * @param ch the channel UID (fridge or freezer) to select the correct conversion map
     * @param value raw temperature value from the device
     * @return decoded temperature integer, or 0 on error
     */
    protected Integer decodeTempValue(ChannelUID ch, Integer value) {
        FridgeCapability refCap;
        try {
            refCap = getCapabilities();
        } catch (LGThinqApiException e) {
            logger.error("Error getting capability of the device. It's mostly like a bug", e);
            return 0;
        }
        Map<String, String> conversionMap = getConversionMap(ch, refCap);
        String strValue = conversionMap.get(value.toString());
        if (strValue == null) {
            logger.error(
                    "Temperature value informed [{}] can't be converted based on the cap file. It mostly like a bug",
                    value);
            return 0;
        }
        try {
            return Integer.valueOf(strValue);
        } catch (Exception ex) {
            logger.error("Temperature value informed [{}] can't be parsed to number. It mostly like a bug", value, ex);
            return 0;
        }
    }

    /**
     * Encodes a human-readable temperature integer back to the raw device value using the inverted conversion map.
     *
     * @param ch the channel UID (fridge or freezer) to select the correct conversion map
     * @param value human-readable temperature integer
     * @return encoded raw device value, or 0 on error
     */
    protected Integer encodeTempValue(ChannelUID ch, Integer value) {
        FridgeCapability refCap;
        try {
            refCap = getCapabilities();
        } catch (LGThinqApiException e) {
            logger.error("Error getting capability of the device. It's mostly like a bug", e);
            return 0;
        }
        final Map<String, String> conversionMap = getConversionMap(ch, refCap);
        final Map<String, String> invertedMap = new HashMap<>();
        conversionMap.forEach((k, v) -> {
            invertedMap.put(v, k);
        });

        String strValue = invertedMap.get(value.toString());
        if (strValue == null) {
            logger.error("Temperature value informed can't be converted based on the cap file. It mostly like a bug");
            return 0;
        }
        try {
            return Integer.valueOf(strValue);
        } catch (Exception ex) {
            logger.error("Temperature value converted can't be cast to Integer. It mostly like a bug", ex);
            return 0;
        }
    }

    private Map<String, String> getConversionMap(ChannelUID ch, FridgeCapability refCap) {
        Map<String, String> conversionMap;
        if (dashboard.channel(CHANNEL_FR_FRIDGE_TEMP_ID).equals(ch)) {
            conversionMap = RE_TEMP_UNIT_FAHRENHEIT.equals(tempUnit) ? refCap.getFridgeTempFMap()
                    : refCap.getFridgeTempCMap();
        } else if (dashboard.channel(CHANNEL_FR_FREEZER_TEMP_ID).equals(ch)) {
            conversionMap = RE_TEMP_UNIT_FAHRENHEIT.equals(tempUnit) ? refCap.getFreezerTempFMap()
                    : refCap.getFreezerTempCMap();
        } else {
            throw new IllegalStateException("Conversion Map Channel temperature not mapped. It's most likely a bug");
        }
        return conversionMap;
    }

    @Override
    public LGThinQApiClientService<FridgeCapability, FridgeCanonicalSnapshot> getLgThinQAPIClientService() {
        return lgThinqFridgeApiClientService;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    protected DeviceTypes getDeviceType() {
        return DeviceTypes.AIR_CONDITIONER;
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

    @Override
    public void updateChannelDynStateDescription() throws LGThinqApiException {
        FridgeCapability cap = getCapabilities();
        manageDynChannel(dashboard.channel(CHANNEL_FR_ICE_PLUS), CHANNEL_FR_ICE_PLUS, CoreItemFactory.SWITCH,
                !cap.getIcePlusMap().isEmpty());
        manageDynChannel(dashboard.channel(CHANNEL_FR_EXPRESS_FREEZE_MODE), CHANNEL_FR_EXPRESS_FREEZE_MODE,
                CoreItemFactory.STRING, !cap.getExpressFreezeModeMap().isEmpty());
        manageDynChannel(dashboard.channel(CHANNEL_FR_EXPRESS_COOL_MODE), CHANNEL_FR_EXPRESS_COOL_MODE,
                CoreItemFactory.SWITCH, cap.isExpressCoolModePresent());
        manageDynChannel(dashboard.channel(CHANNEL_FR_VACATION_MODE), CHANNEL_FR_VACATION_MODE, CoreItemFactory.SWITCH,
                cap.isEcoFriendlyModePresent());

        Unit<Temperature> unTemp = getTemperatureUnit(getLastShot());
        if (SIUnits.CELSIUS.equals(unTemp)) {
            loadChannelTempStateOption(cap.getFridgeTempCMap(), dashboard.channel(CHANNEL_FR_FRIDGE_TEMP_ID), unTemp);
            loadChannelTempStateOption(cap.getFreezerTempCMap(), dashboard.channel(CHANNEL_FR_FREEZER_TEMP_ID), unTemp);
        } else {
            loadChannelTempStateOption(cap.getFridgeTempFMap(), dashboard.channel(CHANNEL_FR_FRIDGE_TEMP_ID), unTemp);
            loadChannelTempStateOption(cap.getFreezerTempFMap(), dashboard.channel(CHANNEL_FR_FREEZER_TEMP_ID), unTemp);
        }
        loadChannelStateOption(cap.getActiveSavingMap(), dashboard.channel(CHANNEL_FR_ACTIVE_SAVING));
        loadChannelStateOption(cap.getExpressFreezeModeMap(), dashboard.channel(CHANNEL_FR_EXPRESS_FREEZE_MODE));
        loadChannelStateOption(cap.getActiveSavingMap(), dashboard.channel(CHANNEL_FR_ACTIVE_SAVING));
        loadChannelStateOption(cap.getSmartSavingMap(), dashboard.channel(smartSavingChannelId));
        loadChannelStateOption(cap.getTempUnitMap(), dashboard.channel(CHANNEL_FR_REF_TEMP_UNIT));
        loadChannelStateOption(CAP_RE_FRESH_AIR_FILTER_MAP, extendedInfo.channel(CHANNEL_FR_FRESH_AIR_FILTER));
        loadChannelStateOption(CAP_RE_WATER_FILTER, extendedInfo.channel(CHANNEL_FR_WATER_FILTER));
    }

    private void loadChannelStateOption(Map<String, String> cap, ChannelUID channelUID) {
        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider, channelUID, cap);
    }

    private void loadChannelTempStateOption(Map<String, String> cap, ChannelUID channelUID, Unit<Temperature> unTemp) {
        final List<StateOption> faOptions = new ArrayList<>();
        cap.forEach((k, v) -> {
            try {
                QuantityType<Temperature> t = new QuantityType<>(Integer.valueOf(v), unTemp);
                faOptions.add(new StateOption(t.toString(), t.toString()));
            } catch (NumberFormatException ex) {
                logger.debug("Error converting invalid temperature number: {}. This can be safely ignored", v);
            }
        });
        stateDescriptionProvider.setStateOptions(channelUID, faOptions);
    }

    @Override
    protected void processCommand(AsyncCommandParams params) throws LGThinqApiException {
        FridgeCanonicalSnapshot lastShot = getLastShot();
        Map<String, Object> cmdSnap = lastShot.getRawData();
        Command command = params.command();
        String simpleChannelUID = getSimpleChannelUID(params.channelUID());
        switch (simpleChannelUID) {
            case CHANNEL_FR_FREEZER_TEMP_ID:
            case CHANNEL_FR_FRIDGE_TEMP_ID: {
                int targetTemp;
                if (command instanceof DecimalType dCmd) {
                    targetTemp = dCmd.intValue();
                } else if (command instanceof QuantityType<?> qCmd) {
                    targetTemp = qCmd.intValue();
                } else {
                    logger.warn("Received command different of Numeric in TargetTemp Channel. Ignoring");
                    break;
                }

                if (CHANNEL_FR_FRIDGE_TEMP_ID.equals(simpleChannelUID)) {
                    targetTemp = encodeTempValue(dashboard.channel(CHANNEL_FR_FRIDGE_TEMP_ID), targetTemp);
                    lgThinqFridgeApiClientService.setFridgeTemperature(getBridgeId(), getDeviceId(), getCapabilities(),
                            targetTemp, lastShot.getTempUnit(), cmdSnap);
                } else {
                    targetTemp = encodeTempValue(dashboard.channel(CHANNEL_FR_FREEZER_TEMP_ID), targetTemp);
                    lgThinqFridgeApiClientService.setFreezerTemperature(getBridgeId(), getDeviceId(), getCapabilities(),
                            targetTemp, lastShot.getTempUnit(), cmdSnap);
                }
                break;
            }
            case CHANNEL_FR_ICE_PLUS: {
                if (command instanceof OnOffType ooCmd) {
                    lgThinqFridgeApiClientService.setIcePlus(getBridgeId(), getDeviceId(), getCapabilities(),
                            ooCmd == OnOffType.ON, cmdSnap);
                } else {
                    logger.warn("Received command different of OnOff in IcePlus Channel. It's mostly like a bug");
                }
                break;
            }
            case CHANNEL_FR_EXPRESS_FREEZE_MODE: {
                String targetExpressMode;
                if (command instanceof StringType stCmd) {
                    targetExpressMode = stCmd.toString();
                } else {
                    logger.warn("Received command different of String in ExpressMode Channel. It's mostly like a bug");
                    break;
                }

                lgThinqFridgeApiClientService.setExpressMode(getBridgeId(), getDeviceId(), targetExpressMode);
                break;
            }
            case CHANNEL_FR_EXPRESS_COOL_MODE: {
                if (command instanceof OnOffType ooCmd) {
                    lgThinqFridgeApiClientService.setExpressCoolMode(getBridgeId(), getDeviceId(),
                            ooCmd == OnOffType.ON);
                } else {
                    logger.warn(
                            "Received command different of OnOffType in ExpressCoolMode Channel. It's mostly like a bug");
                }
                break;
            }
            case CHANNEL_FR_VACATION_MODE: {
                if (command instanceof OnOffType ooCmd) {
                    lgThinqFridgeApiClientService.setEcoFriendlyMode(getBridgeId(), getDeviceId(),
                            ooCmd == OnOffType.ON);
                } else {
                    logger.warn(
                            "Received command different of OnOffType in VacationMode Channel. It's most likely a bug");
                }
                break;
            }
            default: {
                logger.warn("Command {} to the channel {} not supported. Ignored.", command, params.channelUID());
            }
        }
    }
}
