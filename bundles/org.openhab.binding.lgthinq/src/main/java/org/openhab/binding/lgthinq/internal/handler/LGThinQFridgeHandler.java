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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.LGThinQStateDescriptionProvider;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientService;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientServiceFactory;
import org.openhab.binding.lgthinq.lgservices.LGThinQFridgeApiClientService;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCapability;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelGroupUID;
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
 * The {@link LGThinQFridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nemer Daud - Initial contribution
 * @author Arne Seime - Complementary sensors
 */
@NonNullByDefault
public class LGThinQFridgeHandler extends LGThinQAbstractDeviceHandler<FridgeCapability, FridgeCanonicalSnapshot> {
    public final ChannelGroupUID channelGroupExtendedInfoUID;
    public final ChannelGroupUID channelGroupDashboardUID;
    private final ChannelUID fridgeTempChannelUID;
    private final ChannelUID freezerTempChannelUID;
    private final ChannelUID doorChannelUID;
    private final ChannelUID smartSavingModeChannelUID;
    private final ChannelUID activeSavingChannelUID;
    private final ChannelUID icePlusChannelUID;
    private final ChannelUID expressModeChannelUID;
    private final ChannelUID freshAirFilterChannelUID;
    private final ChannelUID waterFilterChannelUID;
    private final ChannelUID tempUnitUID;
    private String tempUnit = TEMP_UNIT_CELSIUS;
    private final Logger logger = LoggerFactory.getLogger(LGThinQFridgeHandler.class);
    @NonNullByDefault
    private final LGThinQFridgeApiClientService lgThinqFridgeApiClientService;

    public LGThinQFridgeHandler(Thing thing, LGThinQStateDescriptionProvider stateDescriptionProvider,
            ItemChannelLinkRegistry itemChannelLinkRegistry, HttpClientFactory httpClientFactory) {
        super(thing, stateDescriptionProvider, itemChannelLinkRegistry);
        lgThinqFridgeApiClientService = LGThinQApiClientServiceFactory.newFridgeApiClientService(lgPlatformType,
                httpClientFactory);
        channelGroupDashboardUID = new ChannelGroupUID(getThing().getUID(), CHANNEL_DASHBOARD_GRP_ID);
        channelGroupExtendedInfoUID = new ChannelGroupUID(getThing().getUID(), CHANNEL_EXTENDED_INFO_GRP_ID);
        fridgeTempChannelUID = new ChannelUID(channelGroupDashboardUID, FR_CHANNEL_FRIDGE_TEMP_ID);
        freezerTempChannelUID = new ChannelUID(channelGroupDashboardUID, FR_CHANNEL_FREEZER_TEMP_ID);
        doorChannelUID = new ChannelUID(channelGroupDashboardUID, FR_CHANNEL_DOOR_ID);
        tempUnitUID = new ChannelUID(channelGroupDashboardUID, FR_CHANNEL_REF_TEMP_UNIT);
        icePlusChannelUID = new ChannelUID(channelGroupDashboardUID, FR_CHANNEL_ICE_PLUS);
        expressModeChannelUID = new ChannelUID(channelGroupDashboardUID, FR_CHANNEL_EXPRESS_MODE);
        smartSavingModeChannelUID = new ChannelUID(channelGroupDashboardUID,
                PLATFORM_TYPE_V2.equals(lgPlatformType) ? FR_CHANNEL_SMART_SAVING_MODE_V2
                        : FR_CHANNEL_SMART_SAVING_SWITCH_V1);
        activeSavingChannelUID = new ChannelUID(channelGroupDashboardUID, FR_CHANNEL_ACTIVE_SAVING);
        freshAirFilterChannelUID = new ChannelUID(channelGroupExtendedInfoUID, FR_CHANNEL_FRESH_AIR_FILTER);
        waterFilterChannelUID = new ChannelUID(channelGroupExtendedInfoUID, FR_CHANNEL_WATER_FILTER);
    }

    private Unit<Temperature> getTemperatureUnit(FridgeCanonicalSnapshot shot) {
        if (!(CELSIUS_UNIT_VALUES.contains(shot.getTempUnit())
                || FAHRENHEIT_UNIT_VALUES.contains(shot.getTempUnit()))) {
            logger.warn(
                    "Temperature Unit not recognized (must be Celsius or Fahrenheit). Ignoring and considering Celsius as default");
            return SIUnits.CELSIUS;
        }
        return CELSIUS_UNIT_VALUES.contains(shot.getTempUnit()) ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT;
    }

    @Override
    protected void updateDeviceChannels(FridgeCanonicalSnapshot shot) {
        Unit<Temperature> unTemp = getTemperatureUnit(shot);
        if (isLinked(fridgeTempChannelUID)) {
            updateState(fridgeTempChannelUID,
                    new QuantityType<>(decodeTempValue(fridgeTempChannelUID, shot.getFridgeTemp().intValue()), unTemp));
        }
        if (isLinked(freezerTempChannelUID)) {
            updateState(freezerTempChannelUID, new QuantityType<>(
                    decodeTempValue(freezerTempChannelUID, shot.getFreezerTemp().intValue()), unTemp));
        }
        if (isLinked(doorChannelUID)) {
            updateState(doorChannelUID, parseDoorStatus(shot.getDoorStatus()));
        }
        if (isLinked(expressModeChannelUID)) {
            updateState(expressModeChannelUID, new StringType(shot.getExpressMode()));
        }
        if (isLinked(freshAirFilterChannelUID)) {
            updateState(freshAirFilterChannelUID, new StringType(shot.getFreshAirFilterState()));
        }
        if (isLinked(waterFilterChannelUID)) {
            updateState(waterFilterChannelUID, new StringType(shot.getWaterFilterUsedMonth()));
        }

        updateState(tempUnitUID, new StringType(shot.getTempUnit()));
        if (!tempUnit.equals(shot.getTempUnit())) {
            tempUnit = shot.getTempUnit();
            try {
                // force update states after first snapshot fetched to fit changes in temperature unit
                updateChannelDynStateDescription();
            } catch (Exception ex) {
                logger.error("Error updating dynamic state description", ex);
            }
        }
    }

    private State parseDoorStatus(String doorStatus) {
        if (DOOR_CLOSE_FR_VALUES.contains(doorStatus)) {
            return OpenClosedType.CLOSED;
        } else if (DOOR_OPEN_FR_VALUES.contains(doorStatus)) {
            return OpenClosedType.OPEN;
        } else {
            return UnDefType.UNDEF;
        }
    }

    protected Integer decodeTempValue(ChannelUID ch, Integer value) {
        FridgeCapability refCap = null;
        try {
            refCap = getCapabilities();
        } catch (LGThinqApiException e) {
            logger.error("Error getting capability of the device. It's mostly like a bug", e);
            return 0;
        }
        // temperature channels are little different. First we need to get the tempUnit in the first snapshot,
        Map<String, String> convertionMap;
        if (fridgeTempChannelUID.equals(ch)) {
            convertionMap = TEMP_UNIT_FAHRENHEIT.equals(tempUnit) ? refCap.getFridgeTempFMap()
                    : refCap.getFridgeTempCMap();
        } else if (freezerTempChannelUID.equals(ch)) {
            convertionMap = TEMP_UNIT_FAHRENHEIT.equals(tempUnit) ? refCap.getFreezerTempFMap()
                    : refCap.getFreezerTempCMap();
        } else {
            throw new IllegalStateException("Conversion Map Channel temperature not mapped. It's most likely a bug");
        }
        String strValue = convertionMap.get(value.toString());
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

    protected Integer encodeTempValue(ChannelUID ch, Integer value) {
        FridgeCapability refCap = null;
        try {
            refCap = getCapabilities();
        } catch (LGThinqApiException e) {
            logger.error("Error getting capability of the device. It's mostly like a bug", e);
            return 0;
        }
        // temperature channels are little different. First we need to get the tempUnit in the first snapshot,
        final Map<String, String> convertionMap, invertedMap;
        if (fridgeTempChannelUID.equals(ch)) {
            convertionMap = TEMP_UNIT_FAHRENHEIT.equals(tempUnit) ? refCap.getFridgeTempFMap()
                    : refCap.getFridgeTempCMap();
        } else if (freezerTempChannelUID.equals(ch)) {
            convertionMap = TEMP_UNIT_FAHRENHEIT.equals(tempUnit) ? refCap.getFreezerTempFMap()
                    : refCap.getFreezerTempCMap();
        } else {
            throw new IllegalStateException("Conversion Map Channel temperature not mapped. It's most likely a bug");
        }
        invertedMap = new HashMap<>();
        convertionMap.forEach((k, v) -> {
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

    @Override
    public void updateChannelDynStateDescription() throws LGThinqApiException {
        FridgeCapability cap = getCapabilities();
        if (!cap.getIcePlusMap().isEmpty() && getThing().getChannel(icePlusChannelUID) == null) {
            createDynChannel(FR_CHANNEL_ICE_PLUS, icePlusChannelUID, "Switch");
        }
        if (!cap.getExpressModeMap().isEmpty() && getThing().getChannel(expressModeChannelUID) == null) {
            createDynChannel(FR_CHANNEL_EXPRESS_MODE, expressModeChannelUID, "String");
        }
        Unit<Temperature> unTemp = getTemperatureUnit(getLastShot());
        if (SIUnits.CELSIUS.equals(unTemp)) {
            loadChannelTempStateOption(cap.getFridgeTempCMap(), fridgeTempChannelUID, unTemp);
            loadChannelTempStateOption(cap.getFreezerTempCMap(), freezerTempChannelUID, unTemp);
        } else {
            loadChannelTempStateOption(cap.getFridgeTempFMap(), fridgeTempChannelUID, unTemp);
            loadChannelTempStateOption(cap.getFreezerTempFMap(), freezerTempChannelUID, unTemp);
        }
        loadChannelStateOption(cap.getActiveSavingMap(), activeSavingChannelUID);

        loadChannelStateOption(cap.getExpressModeMap(), expressModeChannelUID, CAP_FR_EXPRESS_MODES);

        loadChannelStateOption(cap.getActiveSavingMap(), activeSavingChannelUID);

        loadChannelStateOption(cap.getSmartSavingMap(), smartSavingModeChannelUID);

        loadChannelStateOption(cap.getTempUnitMap(), tempUnitUID);

        loadChannelStateOption(CAP_FR_FRESH_AIR_FILTER_MAP, freshAirFilterChannelUID);

        loadChannelStateOption(CAP_FR_WATER_FILTER, waterFilterChannelUID);
    }

    private void loadChannelStateOption(Map<String, String> cap, ChannelUID channelUID) {
        loadChannelStateOption(cap, channelUID, null);
    }

    private void loadChannelTempStateOption(Map<String, String> cap, ChannelUID channelUID, Unit<Temperature> unTemp) {
        final List<StateOption> faOptions = new ArrayList<>();
        cap.forEach((k, v) -> {
            try {
                Integer vInt = Integer.valueOf(v);
                QuantityType<Temperature> t = new QuantityType<>(Integer.valueOf(v), unTemp);
                faOptions.add(new StateOption(t.toString(), t.toString()));
            } catch (NumberFormatException ex) {
                logger.debug("Error converting invalid temperature number: {}. This can be safely ignored", v);
            }
        });
        stateDescriptionProvider.setStateOptions(channelUID, faOptions);
    }

    private void loadChannelStateOption(Map<String, String> cap, ChannelUID channelUID,
            @Nullable Map<String, String> decodeMap) {
        final List<StateOption> faOptions = new ArrayList<>();
        cap.forEach((k, v) -> faOptions.add(new StateOption(k, decodeMap == null ? v : decodeMap.get(v))));
        stateDescriptionProvider.setStateOptions(channelUID, faOptions);
    }

    @Override
    protected void processCommand(AsyncCommandParams params) throws LGThinqApiException {
        FridgeCanonicalSnapshot lastShot = getLastShot();
        Map<String, Object> cmdSnap = lastShot.getRawData();
        Command command = params.command;
        String simpleChannelUID;
        simpleChannelUID = getSimpleChannelUID(params.channelUID);
        switch (simpleChannelUID) {
            case FR_CHANNEL_FREEZER_TEMP_ID:
            case FR_CHANNEL_FRIDGE_TEMP_ID: {
                int targetTemp;
                if (command instanceof DecimalType) {
                    targetTemp = ((DecimalType) command).intValue();
                } else if (command instanceof QuantityType) {
                    targetTemp = ((QuantityType<?>) command).intValue();
                } else {
                    logger.warn("Received command different of Numeric in TargetTemp Channel. Ignoring");
                    break;
                }

                if (FR_CHANNEL_FRIDGE_TEMP_ID.equals(simpleChannelUID)) {
                    targetTemp = encodeTempValue(fridgeTempChannelUID, targetTemp);
                    lgThinqFridgeApiClientService.setFridgeTemperature(getBridgeId(), getDeviceId(), getCapabilities(),
                            targetTemp, lastShot.getTempUnit(), cmdSnap);
                } else {
                    targetTemp = encodeTempValue(freezerTempChannelUID, targetTemp);
                    lgThinqFridgeApiClientService.setFreezerTemperature(getBridgeId(), getDeviceId(), getCapabilities(),
                            targetTemp, lastShot.getTempUnit(), cmdSnap);
                }
                break;
            }
            default: {
                logger.error("Command {} to the channel {} not supported. Ignored.", command, params.channelUID);
            }
        }
    }
}
