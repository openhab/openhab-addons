/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ecobee.internal.handler;

import static org.openhab.binding.ecobee.internal.EcobeeBindingConstants.*;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecobee.internal.action.EcobeeActions;
import org.openhab.binding.ecobee.internal.api.EcobeeApi;
import org.openhab.binding.ecobee.internal.config.EcobeeThermostatConfiguration;
import org.openhab.binding.ecobee.internal.dto.SelectionDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.AlertDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.ClimateDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.EventDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.HouseDetailsDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.LocationDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.ManagementDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.ProgramDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.RemoteSensorDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.RuntimeDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.SettingsDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.TechnicianDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.ThermostatDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.ThermostatUpdateRequestDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.VersionDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.WeatherDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.WeatherForecastDTO;
import org.openhab.binding.ecobee.internal.function.AbstractFunction;
import org.openhab.binding.ecobee.internal.function.FunctionRequest;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EcobeeThermostatBridgeHandler} is the handler for an Ecobee thermostat.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class EcobeeThermostatBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EcobeeThermostatBridgeHandler.class);

    private TimeZoneProvider timeZoneProvider;
    private ChannelTypeRegistry channelTypeRegistry;

    private @NonNullByDefault({}) String thermostatId;

    private final Map<String, EcobeeSensorThingHandler> sensorHandlers = new ConcurrentHashMap<>();

    private @Nullable ThermostatDTO savedThermostat;
    private @Nullable List<RemoteSensorDTO> savedSensors;
    private List<String> validClimateRefs = new CopyOnWriteArrayList<>();
    private Map<String, State> stateCache = new ConcurrentHashMap<>();
    private Map<ChannelUID, Boolean> channelReadOnlyMap = new HashMap<>();
    private Map<Integer, String> symbolMap = new HashMap<>();
    private Map<Integer, String> skyMap = new HashMap<>();

    public EcobeeThermostatBridgeHandler(Bridge bridge, TimeZoneProvider timeZoneProvider,
            ChannelTypeRegistry channelTypeRegistry) {
        super(bridge);
        this.timeZoneProvider = timeZoneProvider;
        this.channelTypeRegistry = channelTypeRegistry;
    }

    @Override
    public void initialize() {
        thermostatId = getConfigAs(EcobeeThermostatConfiguration.class).thermostatId;
        logger.debug("ThermostatBridge: Initializing thermostat '{}'", thermostatId);
        initializeWeatherMaps();
        initializeReadOnlyChannels();
        clearSavedState();
        updateStatus(EcobeeUtils.isBridgeOnline(getBridge()) ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
    }

    @Override
    public void dispose() {
        logger.debug("ThermostatBridge: Disposing thermostat '{}'", thermostatId);
    }

    @Override
    public void childHandlerInitialized(ThingHandler sensorHandler, Thing sensorThing) {
        String sensorId = (String) sensorThing.getConfiguration().get(CONFIG_SENSOR_ID);
        sensorHandlers.put(sensorId, (EcobeeSensorThingHandler) sensorHandler);
        logger.debug("ThermostatBridge: Saving sensor handler for {} with id {}", sensorThing.getUID(), sensorId);
    }

    @Override
    public void childHandlerDisposed(ThingHandler sensorHandler, Thing sensorThing) {
        String sensorId = (String) sensorThing.getConfiguration().get(CONFIG_SENSOR_ID);
        sensorHandlers.remove(sensorId);
        logger.debug("ThermostatBridge: Removing sensor handler for {} with id {}", sensorThing.getUID(), sensorId);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            State state = stateCache.get(channelUID.getId());
            if (state != null) {
                updateState(channelUID.getId(), state);
            }
            return;
        }
        if (isChannelReadOnly(channelUID)) {
            logger.debug("Can't apply command '{}' to '{}' because channel is readonly", command, channelUID.getId());
            return;
        }
        scheduler.execute(() -> {
            handleThermostatCommand(channelUID, command);
        });
    }

    /**
     * Called by the AccountBridgeHandler to create a Selection that
     * includes only the Ecobee objects for which there's at least one
     * item linked to one of that object's channels.
     *
     * @return Selection
     */
    public SelectionDTO getSelection() {
        final SelectionDTO selection = new SelectionDTO();
        for (String group : CHANNEL_GROUPS) {
            for (Channel channel : thing.getChannelsOfGroup(group)) {
                if (isLinked(channel.getUID())) {
                    try {
                        Field field = selection.getClass()
                                .getField("include" + StringUtils.capitalizeByWhitespace(group));
                        logger.trace("ThermostatBridge: Thermostat thing '{}' including object '{}' in selection",
                                thing.getUID(), field.getName());
                        field.set(selection, Boolean.TRUE);
                        break;
                    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                            | SecurityException e) {
                        logger.debug("ThermostatBridge: Exception setting selection for group '{}'", group, e);
                    }
                }
            }
        }
        return selection;
    }

    public List<RemoteSensorDTO> getSensors() {
        List<RemoteSensorDTO> localSavedSensors = savedSensors;
        return localSavedSensors == null ? EMPTY_SENSORS : localSavedSensors;
    }

    public @Nullable String getAlerts() {
        ThermostatDTO thermostat = savedThermostat;
        if (thermostat != null && thermostat.alerts != null) {
            return EcobeeApi.getGson().toJson(thermostat.alerts);
        }
        return null;
    }

    public @Nullable String getEvents() {
        ThermostatDTO thermostat = savedThermostat;
        if (thermostat != null && thermostat.events != null) {
            return EcobeeApi.getGson().toJson(thermostat.events);
        }
        return null;
    }

    public @Nullable String getClimates() {
        ThermostatDTO thermostat = savedThermostat;
        if (thermostat != null && thermostat.program != null && thermostat.program.climates != null) {
            return EcobeeApi.getGson().toJson(thermostat.program.climates);
        }
        return null;
    }

    public boolean isValidClimateRef(String climateRef) {
        return validClimateRefs.contains(climateRef);
    }

    public String getThermostatId() {
        return thermostatId;
    }

    /*
     * Called by EcobeeActions to perform a thermostat function
     */
    public boolean actionPerformFunction(AbstractFunction function) {
        logger.debug("ThermostatBridge: Perform function '{}' on thermostat {}", function.type, thermostatId);
        SelectionDTO selection = new SelectionDTO();
        selection.setThermostats(Set.of(thermostatId));
        FunctionRequest request = new FunctionRequest(selection);
        request.functions = List.of(function);
        EcobeeAccountBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            return handler.performThermostatFunction(request);
        }
        return false;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(EcobeeActions.class);
    }

    public void updateChannels(ThermostatDTO thermostat) {
        logger.debug("ThermostatBridge: Updating channels for thermostat id {}", thermostat.identifier);
        savedThermostat = thermostat;
        updateAlert(thermostat.alerts);
        updateHouseDetails(thermostat.houseDetails);
        updateInfo(thermostat);
        updateEquipmentStatus(thermostat);
        updateLocation(thermostat.location);
        updateManagement(thermostat.management);
        updateProgram(thermostat.program);
        updateEvent(thermostat.events);
        updateRemoteSensors(thermostat.remoteSensors);
        updateRuntime(thermostat.runtime);
        updateSettings(thermostat.settings);
        updateTechnician(thermostat.technician);
        updateVersion(thermostat.version);
        updateWeather(thermostat.weather);
        savedSensors = thermostat.remoteSensors;
    }

    private void handleThermostatCommand(ChannelUID channelUID, Command command) {
        logger.debug("Got command '{}' for channel '{}' of thing '{}'", command, channelUID, getThing().getUID());
        String channelId = channelUID.getIdWithoutGroup();
        String groupId = channelUID.getGroupId();
        if (groupId == null) {
            logger.info("Can't handle command '{}' because channel's groupId is null", command);
            return;
        }
        ThermostatDTO thermostat = new ThermostatDTO();
        Field field;
        try {
            switch (groupId) {
                case CHGRP_INFO:
                    field = thermostat.getClass().getField(channelId);
                    setField(field, thermostat, command);
                    break;
                case CHGRP_SETTINGS:
                    SettingsDTO settings = new SettingsDTO();
                    field = settings.getClass().getField(channelId);
                    setField(field, settings, command);
                    thermostat.settings = settings;
                    break;
                case CHGRP_LOCATION:
                    LocationDTO location = new LocationDTO();
                    field = location.getClass().getField(channelId);
                    setField(field, location, command);
                    thermostat.location = location;
                    break;
                case CHGRP_HOUSE_DETAILS:
                    HouseDetailsDTO houseDetails = new HouseDetailsDTO();
                    field = houseDetails.getClass().getField(channelId);
                    setField(field, houseDetails, command);
                    thermostat.houseDetails = houseDetails;
                    break;
                default:
                    // All other groups contain only read-only fields
                    return;
            }
            performThermostatUpdate(thermostat);
        } catch (NoSuchFieldException | SecurityException e) {
            logger.info("Unable to get field for '{}.{}'", groupId, channelId);
        }
    }

    private void setField(Field field, Object object, Command command) {
        logger.debug("Setting field '{}.{}' to value '{}'", object.getClass().getSimpleName().toLowerCase(),
                field.getName(), command);
        Class<?> fieldClass = field.getType();
        try {
            boolean success = false;
            if (String.class.isAssignableFrom(fieldClass)) {
                if (command instanceof StringType) {
                    logger.debug("Set field of type String to value of StringType");
                    field.set(object, command.toString());
                    success = true;
                }
            } else if (Integer.class.isAssignableFrom(fieldClass)) {
                if (command instanceof DecimalType decimalCommand) {
                    logger.debug("Set field of type Integer to value of DecimalType");
                    field.set(object, Integer.valueOf(decimalCommand.intValue()));
                    success = true;
                } else if (command instanceof QuantityType quantityCommand) {
                    Unit<?> unit = quantityCommand.getUnit();
                    logger.debug("Set field of type Integer to value of QuantityType with unit {}", unit);
                    if (unit.equals(ImperialUnits.FAHRENHEIT) || unit.equals(SIUnits.CELSIUS)) {
                        QuantityType<?> quantity = quantityCommand.toUnit(ImperialUnits.FAHRENHEIT);
                        if (quantity != null) {
                            field.set(object, quantity.intValue() * 10);
                            success = true;
                        }
                    }
                }
            } else if (Boolean.class.isAssignableFrom(fieldClass)) {
                if (command instanceof OnOffType) {
                    logger.debug("Set field of type Boolean to value of OnOffType");
                    field.set(object, command == OnOffType.ON);
                    success = true;
                }
            }
            if (!success) {
                logger.info("Don't know how to convert command of type '{}' to {}.{}",
                        command.getClass().getSimpleName(), object.getClass().getSimpleName(), field.getName());
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            logger.info("Unable to set field '{}.{}' to value '{}'", object.getClass().getSimpleName(), field.getName(),
                    command, e);
        }
    }

    private void updateInfo(ThermostatDTO thermostat) {
        final String grp = CHGRP_INFO + "#";
        updateChannel(grp + CH_IDENTIFIER, EcobeeUtils.undefOrString(thermostat.identifier));
        updateChannel(grp + CH_NAME, EcobeeUtils.undefOrString(thermostat.name));
        updateChannel(grp + CH_THERMOSTAT_REV, EcobeeUtils.undefOrString(thermostat.thermostatRev));
        updateChannel(grp + CH_IS_REGISTERED, EcobeeUtils.undefOrOnOff(thermostat.isRegistered));
        updateChannel(grp + CH_MODEL_NUMBER, EcobeeUtils.undefOrString(thermostat.modelNumber));
        updateChannel(grp + CH_BRAND, EcobeeUtils.undefOrString(thermostat.brand));
        updateChannel(grp + CH_FEATURES, EcobeeUtils.undefOrString(thermostat.features));
        updateChannel(grp + CH_LAST_MODIFIED, EcobeeUtils.undefOrDate(thermostat.lastModified, timeZoneProvider));
        updateChannel(grp + CH_THERMOSTAT_TIME, EcobeeUtils.undefOrDate(thermostat.thermostatTime, timeZoneProvider));
    }

    private void updateEquipmentStatus(ThermostatDTO thermostat) {
        final String grp = CHGRP_EQUIPMENT_STATUS + "#";
        updateChannel(grp + CH_EQUIPMENT_STATUS, EcobeeUtils.undefOrString(thermostat.equipmentStatus));
    }

    private void updateRuntime(@Nullable RuntimeDTO runtime) {
        if (runtime == null) {
            return;
        }
        final String grp = CHGRP_RUNTIME + "#";
        updateChannel(grp + CH_RUNTIME_REV, EcobeeUtils.undefOrString(runtime.runtimeRev));
        updateChannel(grp + CH_CONNECTED, EcobeeUtils.undefOrOnOff(runtime.connected));
        updateChannel(grp + CH_FIRST_CONNECTED, EcobeeUtils.undefOrDate(runtime.firstConnected, timeZoneProvider));
        updateChannel(grp + CH_CONNECT_DATE_TIME, EcobeeUtils.undefOrDate(runtime.connectDateTime, timeZoneProvider));
        updateChannel(grp + CH_DISCONNECT_DATE_TIME,
                EcobeeUtils.undefOrDate(runtime.disconnectDateTime, timeZoneProvider));
        updateChannel(grp + CH_RT_LAST_MODIFIED, EcobeeUtils.undefOrDate(runtime.lastModified, timeZoneProvider));
        updateChannel(grp + CH_RT_LAST_STATUS_MODIFIED,
                EcobeeUtils.undefOrDate(runtime.lastStatusModified, timeZoneProvider));
        updateChannel(grp + CH_RUNTIME_DATE, EcobeeUtils.undefOrString(runtime.runtimeDate));
        updateChannel(grp + CH_RUNTIME_INTERVAL, EcobeeUtils.undefOrDecimal(runtime.runtimeInterval));
        updateChannel(grp + CH_ACTUAL_TEMPERATURE, EcobeeUtils.undefOrTemperature(runtime.actualTemperature));
        updateChannel(grp + CH_ACTUAL_HUMIDITY, EcobeeUtils.undefOrQuantity(runtime.actualHumidity, Units.PERCENT));
        updateChannel(grp + CH_RAW_TEMPERATURE, EcobeeUtils.undefOrTemperature(runtime.rawTemperature));
        updateChannel(grp + CH_SHOW_ICON_MODE, EcobeeUtils.undefOrDecimal(runtime.showIconMode));
        updateChannel(grp + CH_DESIRED_HEAT, EcobeeUtils.undefOrTemperature(runtime.desiredHeat));
        updateChannel(grp + CH_DESIRED_COOL, EcobeeUtils.undefOrTemperature(runtime.desiredCool));
        updateChannel(grp + CH_DESIRED_HUMIDITY, EcobeeUtils.undefOrQuantity(runtime.desiredHumidity, Units.PERCENT));
        updateChannel(grp + CH_DESIRED_DEHUMIDITY,
                EcobeeUtils.undefOrQuantity(runtime.desiredDehumidity, Units.PERCENT));
        updateChannel(grp + CH_DESIRED_FAN_MODE, EcobeeUtils.undefOrString(runtime.desiredFanMode));
        if (runtime.desiredHeatRange != null && runtime.desiredHeatRange.size() == 2) {
            updateChannel(grp + CH_DESIRED_HEAT_RANGE_LOW,
                    EcobeeUtils.undefOrTemperature(runtime.desiredHeatRange.get(0)));
            updateChannel(grp + CH_DESIRED_HEAT_RANGE_HIGH,
                    EcobeeUtils.undefOrTemperature(runtime.desiredHeatRange.get(1)));
        }
        if (runtime.desiredCoolRange != null && runtime.desiredCoolRange.size() == 2) {
            updateChannel(grp + CH_DESIRED_COOL_RANGE_LOW,
                    EcobeeUtils.undefOrTemperature(runtime.desiredCoolRange.get(0)));
            updateChannel(grp + CH_DESIRED_COOL_RANGE_HIGH,
                    EcobeeUtils.undefOrTemperature(runtime.desiredCoolRange.get(1)));
        }
        updateChannel(grp + CH_ACTUAL_AQ_ACCURACY, EcobeeUtils.undefOrLong(runtime.actualAQAccuracy));
        updateChannel(grp + CH_ACTUAL_AQ_SCORE, EcobeeUtils.undefOrLong(runtime.actualAQScore));
        updateChannel(grp + CH_ACTUAL_CO2, EcobeeUtils.undefOrQuantity(runtime.actualCO2, Units.PARTS_PER_MILLION));
        updateChannel(grp + CH_ACTUAL_VOC, EcobeeUtils.undefOrQuantity(runtime.actualVOC, Units.PARTS_PER_BILLION));
    }

    private void updateSettings(@Nullable SettingsDTO settings) {
        if (settings == null) {
            return;
        }
        final String grp = CHGRP_SETTINGS + "#";
        updateChannel(grp + CH_HVAC_MODE, EcobeeUtils.undefOrString(settings.hvacMode));
        updateChannel(grp + CH_LAST_SERVICE_DATE, EcobeeUtils.undefOrString(settings.lastServiceDate));
        updateChannel(grp + CH_SERVICE_REMIND_ME, EcobeeUtils.undefOrOnOff(settings.serviceRemindMe));
        updateChannel(grp + CH_MONTHS_BETWEEN_SERVICE, EcobeeUtils.undefOrDecimal(settings.monthsBetweenService));
        updateChannel(grp + CH_REMIND_ME_DATE, EcobeeUtils.undefOrString(settings.remindMeDate));
        updateChannel(grp + CH_VENT, EcobeeUtils.undefOrString(settings.vent));
        updateChannel(grp + CH_VENTILATOR_MIN_ON_TIME, EcobeeUtils.undefOrDecimal(settings.ventilatorMinOnTime));
        updateChannel(grp + CH_SERVICE_REMIND_TECHNICIAN, EcobeeUtils.undefOrOnOff(settings.serviceRemindTechnician));
        updateChannel(grp + CH_EI_LOCATION, EcobeeUtils.undefOrString(settings.eiLocation));
        updateChannel(grp + CH_COLD_TEMP_ALERT, EcobeeUtils.undefOrTemperature(settings.coldTempAlert));
        updateChannel(grp + CH_COLD_TEMP_ALERT_ENABLED, EcobeeUtils.undefOrOnOff(settings.coldTempAlertEnabled));
        updateChannel(grp + CH_HOT_TEMP_ALERT, EcobeeUtils.undefOrTemperature(settings.hotTempAlert));
        updateChannel(grp + CH_HOT_TEMP_ALERT_ENABLED, EcobeeUtils.undefOrOnOff(settings.hotTempAlertEnabled));
        updateChannel(grp + CH_COOL_STAGES, EcobeeUtils.undefOrDecimal(settings.coolStages));
        updateChannel(grp + CH_HEAT_STAGES, EcobeeUtils.undefOrDecimal(settings.heatStages));
        updateChannel(grp + CH_MAX_SET_BACK, EcobeeUtils.undefOrDecimal(settings.maxSetBack));
        updateChannel(grp + CH_MAX_SET_FORWARD, EcobeeUtils.undefOrDecimal(settings.maxSetForward));
        updateChannel(grp + CH_QUICK_SAVE_SET_BACK, EcobeeUtils.undefOrDecimal(settings.quickSaveSetBack));
        updateChannel(grp + CH_QUICK_SAVE_SET_FORWARD, EcobeeUtils.undefOrDecimal(settings.quickSaveSetForward));
        updateChannel(grp + CH_HAS_HEAT_PUMP, EcobeeUtils.undefOrOnOff(settings.hasHeatPump));
        updateChannel(grp + CH_HAS_FORCED_AIR, EcobeeUtils.undefOrOnOff(settings.hasForcedAir));
        updateChannel(grp + CH_HAS_BOILER, EcobeeUtils.undefOrOnOff(settings.hasBoiler));
        updateChannel(grp + CH_HAS_HUMIDIFIER, EcobeeUtils.undefOrOnOff(settings.hasHumidifier));
        updateChannel(grp + CH_HAS_ERV, EcobeeUtils.undefOrOnOff(settings.hasErv));
        updateChannel(grp + CH_HAS_HRV, EcobeeUtils.undefOrOnOff(settings.hasHrv));
        updateChannel(grp + CH_CONDENSATION_AVOID, EcobeeUtils.undefOrOnOff(settings.condensationAvoid));
        updateChannel(grp + CH_USE_CELSIUS, EcobeeUtils.undefOrOnOff(settings.useCelsius));
        updateChannel(grp + CH_USE_TIME_FORMAT_12, EcobeeUtils.undefOrOnOff(settings.useTimeFormat12));
        updateChannel(grp + CH_LOCALE, EcobeeUtils.undefOrString(settings.locale));
        updateChannel(grp + CH_HUMIDITY, EcobeeUtils.undefOrString(settings.humidity));
        updateChannel(grp + CH_HUMIDIFIER_MODE, EcobeeUtils.undefOrString(settings.humidifierMode));
        updateChannel(grp + CH_BACKLIGHT_ON_INTENSITY, EcobeeUtils.undefOrDecimal(settings.backlightOnIntensity));
        updateChannel(grp + CH_BACKLIGHT_SLEEP_INTENSITY, EcobeeUtils.undefOrDecimal(settings.backlightSleepIntensity));
        updateChannel(grp + CH_BACKLIGHT_OFF_TIME, EcobeeUtils.undefOrDecimal(settings.backlightOffTime));
        updateChannel(grp + CH_SOUND_TICK_VOLUME, EcobeeUtils.undefOrDecimal(settings.soundTickVolume));
        updateChannel(grp + CH_SOUND_ALERT_VOLUME, EcobeeUtils.undefOrDecimal(settings.soundAlertVolume));
        updateChannel(grp + CH_COMPRESSOR_PROTECTION_MIN_TIME,
                EcobeeUtils.undefOrDecimal(settings.compressorProtectionMinTime));
        updateChannel(grp + CH_COMPRESSOR_PROTECTION_MIN_TEMP,
                EcobeeUtils.undefOrTemperature(settings.compressorProtectionMinTemp));
        updateChannel(grp + CH_STAGE1_HEATING_DIFFERENTIAL_TEMP,
                EcobeeUtils.undefOrDecimal(settings.stage1HeatingDifferentialTemp));
        updateChannel(grp + CH_STAGE1_COOLING_DIFFERENTIAL_TEMP,
                EcobeeUtils.undefOrDecimal(settings.stage1CoolingDifferentialTemp));
        updateChannel(grp + CH_STAGE1_HEATING_DISSIPATION_TIME,
                EcobeeUtils.undefOrDecimal(settings.stage1HeatingDissipationTime));
        updateChannel(grp + CH_STAGE1_COOLING_DISSIPATION_TIME,
                EcobeeUtils.undefOrDecimal(settings.stage1CoolingDissipationTime));
        updateChannel(grp + CH_HEAT_PUMP_REVERSAL_ON_COOL, EcobeeUtils.undefOrOnOff(settings.heatPumpReversalOnCool));
        updateChannel(grp + CH_FAN_CONTROLLER_REQUIRED, EcobeeUtils.undefOrOnOff(settings.fanControlRequired));
        updateChannel(grp + CH_FAN_MIN_ON_TIME, EcobeeUtils.undefOrDecimal(settings.fanMinOnTime));
        updateChannel(grp + CH_HEAT_COOL_MIN_DELTA, EcobeeUtils.undefOrDecimal(settings.heatCoolMinDelta));
        updateChannel(grp + CH_TEMP_CORRECTION, EcobeeUtils.undefOrDecimal(settings.tempCorrection));
        updateChannel(grp + CH_HOLD_ACTION, EcobeeUtils.undefOrString(settings.holdAction));
        updateChannel(grp + CH_HEAT_PUMP_GROUND_WATER, EcobeeUtils.undefOrOnOff(settings.heatPumpGroundWater));
        updateChannel(grp + CH_HAS_ELECTRIC, EcobeeUtils.undefOrOnOff(settings.hasElectric));
        updateChannel(grp + CH_HAS_DEHUMIDIFIER, EcobeeUtils.undefOrOnOff(settings.hasDehumidifier));
        updateChannel(grp + CH_DEHUMIDIFIER_MODE, EcobeeUtils.undefOrString(settings.dehumidifierMode));
        updateChannel(grp + CH_DEHUMIDIFIER_LEVEL, EcobeeUtils.undefOrDecimal(settings.dehumidifierLevel));
        updateChannel(grp + CH_DEHUMIDIFY_WITH_AC, EcobeeUtils.undefOrOnOff(settings.dehumidifyWithAC));
        updateChannel(grp + CH_DEHUMIDIFY_OVERCOOL_OFFSET,
                EcobeeUtils.undefOrDecimal(settings.dehumidifyOvercoolOffset));
        updateChannel(grp + CH_AUTO_HEAT_COOL_FEATURE_ENABLED,
                EcobeeUtils.undefOrOnOff(settings.autoHeatCoolFeatureEnabled));
        updateChannel(grp + CH_WIFI_OFFLINE_ALERT, EcobeeUtils.undefOrOnOff(settings.wifiOfflineAlert));
        updateChannel(grp + CH_HEAT_MIN_TEMP, EcobeeUtils.undefOrTemperature(settings.heatMinTemp));
        updateChannel(grp + CH_HEAT_MAX_TEMP, EcobeeUtils.undefOrTemperature(settings.heatMaxTemp));
        updateChannel(grp + CH_COOL_MIN_TEMP, EcobeeUtils.undefOrTemperature(settings.coolMinTemp));
        updateChannel(grp + CH_COOL_MAX_TEMP, EcobeeUtils.undefOrTemperature(settings.coolMaxTemp));
        updateChannel(grp + CH_HEAT_RANGE_HIGH, EcobeeUtils.undefOrTemperature(settings.heatRangeHigh));
        updateChannel(grp + CH_HEAT_RANGE_LOW, EcobeeUtils.undefOrTemperature(settings.heatRangeLow));
        updateChannel(grp + CH_COOL_RANGE_HIGH, EcobeeUtils.undefOrTemperature(settings.coolRangeHigh));
        updateChannel(grp + CH_COOL_RANGE_LOW, EcobeeUtils.undefOrTemperature(settings.coolRangeLow));
        updateChannel(grp + CH_USER_ACCESS_CODE, EcobeeUtils.undefOrString(settings.userAccessCode));
        updateChannel(grp + CH_USER_ACCESS_SETTING, EcobeeUtils.undefOrDecimal(settings.userAccessSetting));
        updateChannel(grp + CH_AUX_RUNTIME_ALERT, EcobeeUtils.undefOrDecimal(settings.auxRuntimeAlert));
        updateChannel(grp + CH_AUX_OUTDOOR_TEMP_ALERT, EcobeeUtils.undefOrTemperature(settings.auxOutdoorTempAlert));
        updateChannel(grp + CH_AUX_MAX_OUTDOOR_TEMP, EcobeeUtils.undefOrTemperature(settings.auxMaxOutdoorTemp));
        updateChannel(grp + CH_AUX_RUNTIME_ALERT_NOTIFY, EcobeeUtils.undefOrOnOff(settings.auxRuntimeAlertNotify));
        updateChannel(grp + CH_AUX_OUTDOOR_TEMP_ALERT_NOTIFY,
                EcobeeUtils.undefOrOnOff(settings.auxOutdoorTempAlertNotify));
        updateChannel(grp + CH_AUX_RUNTIME_ALERT_NOTIFY_TECHNICIAN,
                EcobeeUtils.undefOrOnOff(settings.auxRuntimeAlertNotifyTechnician));
        updateChannel(grp + CH_AUX_OUTDOOR_TEMP_ALERT_NOTIFY_TECHNICIAN,
                EcobeeUtils.undefOrOnOff(settings.auxOutdoorTempAlertNotifyTechnician));
        updateChannel(grp + CH_DISABLE_PREHEATING, EcobeeUtils.undefOrOnOff(settings.disablePreHeating));
        updateChannel(grp + CH_DISABLE_PRECOOLING, EcobeeUtils.undefOrOnOff(settings.disablePreCooling));
        updateChannel(grp + CH_INSTALLER_CODE_REQUIRED, EcobeeUtils.undefOrOnOff(settings.installerCodeRequired));
        updateChannel(grp + CH_DR_ACCEPT, EcobeeUtils.undefOrString(settings.drAccept));
        updateChannel(grp + CH_IS_RENTAL_PROPERTY, EcobeeUtils.undefOrOnOff(settings.isRentalProperty));
        updateChannel(grp + CH_USE_ZONE_CONTROLLER, EcobeeUtils.undefOrOnOff(settings.useZoneController));
        updateChannel(grp + CH_RANDOM_START_DELAY_COOL, EcobeeUtils.undefOrDecimal(settings.randomStartDelayCool));
        updateChannel(grp + CH_RANDOM_START_DELAY_HEAT, EcobeeUtils.undefOrDecimal(settings.randomStartDelayHeat));
        updateChannel(grp + CH_HUMIDITY_HIGH_ALERT,
                EcobeeUtils.undefOrQuantity(settings.humidityHighAlert, Units.PERCENT));
        updateChannel(grp + CH_HUMIDITY_LOW_ALERT,
                EcobeeUtils.undefOrQuantity(settings.humidityLowAlert, Units.PERCENT));
        updateChannel(grp + CH_DISABLE_HEAT_PUMP_ALERTS, EcobeeUtils.undefOrOnOff(settings.disableHeatPumpAlerts));
        updateChannel(grp + CH_DISABLE_ALERTS_ON_IDT, EcobeeUtils.undefOrOnOff(settings.disableAlertsOnIdt));
        updateChannel(grp + CH_HUMIDITY_ALERT_NOTIFY, EcobeeUtils.undefOrOnOff(settings.humidityAlertNotify));
        updateChannel(grp + CH_HUMIDITY_ALERT_NOTIFY_TECHNICIAN,
                EcobeeUtils.undefOrOnOff(settings.humidityAlertNotifyTechnician));
        updateChannel(grp + CH_TEMP_ALERT_NOTIFY, EcobeeUtils.undefOrOnOff(settings.tempAlertNotify));
        updateChannel(grp + CH_TEMP_ALERT_NOTIFY_TECHNICIAN,
                EcobeeUtils.undefOrOnOff(settings.tempAlertNotifyTechnician));
        updateChannel(grp + CH_MONTHLY_ELECTRICITY_BILL_LIMIT,
                EcobeeUtils.undefOrDecimal(settings.monthlyElectricityBillLimit));
        updateChannel(grp + CH_ENABLE_ELECTRICITY_BILL_ALERT,
                EcobeeUtils.undefOrOnOff(settings.enableElectricityBillAlert));
        updateChannel(grp + CH_ENABLE_PROJECTED_ELECTRICITY_BILL_ALERT,
                EcobeeUtils.undefOrOnOff(settings.enableProjectedElectricityBillAlert));
        updateChannel(grp + CH_ELECTRICITY_BILLING_DAY_OF_MONTH,
                EcobeeUtils.undefOrDecimal(settings.electricityBillingDayOfMonth));
        updateChannel(grp + CH_ELECTRICITY_BILL_CYCLE_MONTHS,
                EcobeeUtils.undefOrDecimal(settings.electricityBillCycleMonths));
        updateChannel(grp + CH_ELECTRICITY_BILL_START_MONTH,
                EcobeeUtils.undefOrDecimal(settings.electricityBillStartMonth));
        updateChannel(grp + CH_VENTILATOR_MIN_ON_TIME_HOME,
                EcobeeUtils.undefOrDecimal(settings.ventilatorMinOnTimeHome));
        updateChannel(grp + CH_VENTILATOR_MIN_ON_TIME_AWAY,
                EcobeeUtils.undefOrDecimal(settings.ventilatorMinOnTimeAway));
        updateChannel(grp + CH_BACKLIGHT_OFF_DURING_SLEEP, EcobeeUtils.undefOrOnOff(settings.backlightOffDuringSleep));
        updateChannel(grp + CH_AUTO_AWAY, EcobeeUtils.undefOrOnOff(settings.autoAway));
        updateChannel(grp + CH_SMART_CIRCULATION, EcobeeUtils.undefOrOnOff(settings.smartCirculation));
        updateChannel(grp + CH_FOLLOW_ME_COMFORT, EcobeeUtils.undefOrOnOff(settings.followMeComfort));
        updateChannel(grp + CH_VENTILATOR_TYPE, EcobeeUtils.undefOrString(settings.ventilatorType));
        updateChannel(grp + CH_IS_VENTILATOR_TIMER_ON, EcobeeUtils.undefOrOnOff(settings.isVentilatorTimerOn));
        updateChannel(grp + CH_VENTILATOR_OFF_DATE_TIME, EcobeeUtils.undefOrString(settings.ventilatorOffDateTime));
        updateChannel(grp + CH_HAS_UV_FILTER, EcobeeUtils.undefOrOnOff(settings.hasUVFilter));
        updateChannel(grp + CH_COOLING_LOCKOUT, EcobeeUtils.undefOrOnOff(settings.coolingLockout));
        updateChannel(grp + CH_VENTILATOR_FREE_COOLING, EcobeeUtils.undefOrOnOff(settings.ventilatorFreeCooling));
        updateChannel(grp + CH_DEHUMIDIFY_WHEN_HEATING, EcobeeUtils.undefOrOnOff(settings.dehumidifyWhenHeating));
        updateChannel(grp + CH_VENTILATOR_DEHUMIDIFY, EcobeeUtils.undefOrOnOff(settings.ventilatorDehumidify));
        updateChannel(grp + CH_GROUP_REF, EcobeeUtils.undefOrString(settings.groupRef));
        updateChannel(grp + CH_GROUP_NAME, EcobeeUtils.undefOrString(settings.groupName));
        updateChannel(grp + CH_GROUP_SETTING, EcobeeUtils.undefOrDecimal(settings.groupSetting));
    }

    private void updateProgram(@Nullable ProgramDTO program) {
        if (program == null) {
            return;
        }
        final String grp = CHGRP_PROGRAM + "#";
        updateChannel(grp + CH_PROGRAM_CURRENT_CLIMATE_REF, EcobeeUtils.undefOrString(program.currentClimateRef));
        if (program.climates != null) {
            saveValidClimateRefs(program.climates);
        }
    }

    private void saveValidClimateRefs(List<ClimateDTO> climates) {
        validClimateRefs.clear();
        for (ClimateDTO climate : climates) {
            validClimateRefs.add(climate.climateRef);
        }
    }

    private void updateAlert(@Nullable List<AlertDTO> alerts) {
        AlertDTO firstAlert;
        if (alerts == null || alerts.isEmpty()) {
            firstAlert = EMPTY_ALERT;
        } else {
            firstAlert = alerts.get(0);
        }
        final String grp = CHGRP_ALERT + "#";
        updateChannel(grp + CH_ALERT_ACKNOWLEDGE_REF, EcobeeUtils.undefOrString(firstAlert.acknowledgeRef));
        updateChannel(grp + CH_ALERT_DATE, EcobeeUtils.undefOrString(firstAlert.date));
        updateChannel(grp + CH_ALERT_TIME, EcobeeUtils.undefOrString(firstAlert.time));
        updateChannel(grp + CH_ALERT_SEVERITY, EcobeeUtils.undefOrString(firstAlert.severity));
        updateChannel(grp + CH_ALERT_TEXT, EcobeeUtils.undefOrString(firstAlert.text));
        updateChannel(grp + CH_ALERT_ALERT_NUMBER, EcobeeUtils.undefOrDecimal(firstAlert.alertNumber));
        updateChannel(grp + CH_ALERT_ALERT_TYPE, EcobeeUtils.undefOrString(firstAlert.alertType));
        updateChannel(grp + CH_ALERT_IS_OPERATOR_ALERT, EcobeeUtils.undefOrOnOff(firstAlert.isOperatorAlert));
        updateChannel(grp + CH_ALERT_REMINDER, EcobeeUtils.undefOrString(firstAlert.reminder));
        updateChannel(grp + CH_ALERT_SHOW_IDT, EcobeeUtils.undefOrOnOff(firstAlert.showIdt));
        updateChannel(grp + CH_ALERT_SHOW_WEB, EcobeeUtils.undefOrOnOff(firstAlert.showWeb));
        updateChannel(grp + CH_ALERT_SEND_EMAIL, EcobeeUtils.undefOrOnOff(firstAlert.sendEmail));
        updateChannel(grp + CH_ALERT_ACKNOWLEDGEMENT, EcobeeUtils.undefOrString(firstAlert.acknowledgement));
        updateChannel(grp + CH_ALERT_REMIND_ME_LATER, EcobeeUtils.undefOrOnOff(firstAlert.remindMeLater));
        updateChannel(grp + CH_ALERT_THERMOSTAT_IDENTIFIER, EcobeeUtils.undefOrString(firstAlert.thermostatIdentifier));
        updateChannel(grp + CH_ALERT_NOTIFICATION_TYPE, EcobeeUtils.undefOrString(firstAlert.notificationType));
    }

    private void updateEvent(@Nullable List<EventDTO> events) {
        EventDTO runningEvent = EMPTY_EVENT;
        if (events != null && !events.isEmpty()) {
            for (EventDTO event : events) {
                if (event.running) {
                    runningEvent = event;
                    break;
                }
            }
        }
        final String grp = CHGRP_EVENT + "#";
        updateChannel(grp + CH_EVENT_NAME, EcobeeUtils.undefOrString(runningEvent.name));
        updateChannel(grp + CH_EVENT_TYPE, EcobeeUtils.undefOrString(runningEvent.type));
        updateChannel(grp + CH_EVENT_RUNNING, EcobeeUtils.undefOrOnOff(runningEvent.running));
        updateChannel(grp + CH_EVENT_START_DATE, EcobeeUtils.undefOrString(runningEvent.startDate));
        updateChannel(grp + CH_EVENT_START_TIME, EcobeeUtils.undefOrString(runningEvent.startTime));
        updateChannel(grp + CH_EVENT_END_DATE, EcobeeUtils.undefOrString(runningEvent.endDate));
        updateChannel(grp + CH_EVENT_END_TIME, EcobeeUtils.undefOrString(runningEvent.endTime));
        updateChannel(grp + CH_EVENT_IS_OCCUPIED, EcobeeUtils.undefOrOnOff(runningEvent.isOccupied));
        updateChannel(grp + CH_EVENT_IS_COOL_OFF, EcobeeUtils.undefOrOnOff(runningEvent.isCoolOff));
        updateChannel(grp + CH_EVENT_IS_HEAT_OFF, EcobeeUtils.undefOrOnOff(runningEvent.isHeatOff));
        updateChannel(grp + CH_EVENT_COOL_HOLD_TEMP, EcobeeUtils.undefOrTemperature(runningEvent.coolHoldTemp));
        updateChannel(grp + CH_EVENT_HEAT_HOLD_TEMP, EcobeeUtils.undefOrTemperature(runningEvent.heatHoldTemp));
        updateChannel(grp + CH_EVENT_FAN, EcobeeUtils.undefOrString(runningEvent.fan));
        updateChannel(grp + CH_EVENT_VENT, EcobeeUtils.undefOrString(runningEvent.vent));
        updateChannel(grp + CH_EVENT_VENTILATOR_MIN_ON_TIME,
                EcobeeUtils.undefOrDecimal(runningEvent.ventilatorMinOnTime));
        updateChannel(grp + CH_EVENT_IS_OPTIONAL, EcobeeUtils.undefOrOnOff(runningEvent.isOptional));
        updateChannel(grp + CH_EVENT_IS_TEMPERATURE_RELATIVE,
                EcobeeUtils.undefOrOnOff(runningEvent.isTemperatureRelative));
        updateChannel(grp + CH_EVENT_COOL_RELATIVE_TEMP, EcobeeUtils.undefOrDecimal(runningEvent.coolRelativeTemp));
        updateChannel(grp + CH_EVENT_HEAT_RELATIVE_TEMP, EcobeeUtils.undefOrDecimal(runningEvent.heatRelativeTemp));
        updateChannel(grp + CH_EVENT_IS_TEMPERATURE_ABSOLUTE,
                EcobeeUtils.undefOrOnOff(runningEvent.isTemperatureAbsolute));
        updateChannel(grp + CH_EVENT_DUTY_CYCLE_PERCENTAGE,
                EcobeeUtils.undefOrDecimal(runningEvent.dutyCyclePercentage));
        updateChannel(grp + CH_EVENT_FAN_MIN_ON_TIME, EcobeeUtils.undefOrDecimal(runningEvent.fanMinOnTime));
        updateChannel(grp + CH_EVENT_OCCUPIED_SENSOR_ACTIVE,
                EcobeeUtils.undefOrOnOff(runningEvent.occupiedSensorActive));
        updateChannel(grp + CH_EVENT_UNOCCUPIED_SENSOR_ACTIVE,
                EcobeeUtils.undefOrOnOff(runningEvent.unoccupiedSensorActive));
        updateChannel(grp + CH_EVENT_DR_RAMP_UP_TEMP, EcobeeUtils.undefOrDecimal(runningEvent.drRampUpTemp));
        updateChannel(grp + CH_EVENT_DR_RAMP_UP_TIME, EcobeeUtils.undefOrDecimal(runningEvent.drRampUpTime));
        updateChannel(grp + CH_EVENT_LINK_REF, EcobeeUtils.undefOrString(runningEvent.linkRef));
        updateChannel(grp + CH_EVENT_HOLD_CLIMATE_REF, EcobeeUtils.undefOrString(runningEvent.holdClimateRef));
    }

    private void updateWeather(@Nullable WeatherDTO weather) {
        if (weather == null || weather.forecasts == null) {
            return;
        }
        final String weatherGrp = CHGRP_WEATHER + "#";

        updateChannel(weatherGrp + CH_WEATHER_TIMESTAMP, EcobeeUtils.undefOrDate(weather.timestamp, timeZoneProvider));
        updateChannel(weatherGrp + CH_WEATHER_WEATHER_STATION, EcobeeUtils.undefOrString(weather.weatherStation));

        for (int index = 0; index < weather.forecasts.size(); index++) {
            final String grp = CHGRP_FORECAST + String.format("%d", index) + "#";
            WeatherForecastDTO forecast = weather.forecasts.get(index);
            if (forecast != null) {
                updateChannel(grp + CH_FORECAST_WEATHER_SYMBOL, EcobeeUtils.undefOrDecimal(forecast.weatherSymbol));
                updateChannel(grp + CH_FORECAST_WEATHER_SYMBOL_TEXT,
                        EcobeeUtils.undefOrString(symbolMap.get(forecast.weatherSymbol)));
                updateChannel(grp + CH_FORECAST_DATE_TIME,
                        EcobeeUtils.undefOrDate(forecast.dateTime, timeZoneProvider));
                updateChannel(grp + CH_FORECAST_CONDITION, EcobeeUtils.undefOrString(forecast.condition));
                updateChannel(grp + CH_FORECAST_TEMPERATURE, EcobeeUtils.undefOrTemperature(forecast.temperature));
                updateChannel(grp + CH_FORECAST_PRESSURE,
                        EcobeeUtils.undefOrQuantity(forecast.pressure, Units.MILLIBAR));
                updateChannel(grp + CH_FORECAST_RELATIVE_HUMIDITY,
                        EcobeeUtils.undefOrQuantity(forecast.relativeHumidity, Units.PERCENT));
                updateChannel(grp + CH_FORECAST_DEWPOINT, EcobeeUtils.undefOrTemperature(forecast.dewpoint));
                updateChannel(grp + CH_FORECAST_VISIBILITY,
                        EcobeeUtils.undefOrQuantity(forecast.visibility, SIUnits.METRE));
                updateChannel(grp + CH_FORECAST_WIND_SPEED,
                        EcobeeUtils.undefOrQuantity(forecast.windSpeed, ImperialUnits.MILES_PER_HOUR));
                updateChannel(grp + CH_FORECAST_WIND_GUST,
                        EcobeeUtils.undefOrQuantity(forecast.windGust, ImperialUnits.MILES_PER_HOUR));
                updateChannel(grp + CH_FORECAST_WIND_DIRECTION, EcobeeUtils.undefOrString(forecast.windDirection));
                updateChannel(grp + CH_FORECAST_WIND_BEARING,
                        EcobeeUtils.undefOrQuantity(forecast.windBearing, Units.DEGREE_ANGLE));
                updateChannel(grp + CH_FORECAST_POP, EcobeeUtils.undefOrQuantity(forecast.pop, Units.PERCENT));
                updateChannel(grp + CH_FORECAST_TEMP_HIGH, EcobeeUtils.undefOrTemperature(forecast.tempHigh));
                updateChannel(grp + CH_FORECAST_TEMP_LOW, EcobeeUtils.undefOrTemperature(forecast.tempLow));
                updateChannel(grp + CH_FORECAST_SKY, EcobeeUtils.undefOrDecimal(forecast.sky));
                updateChannel(grp + CH_FORECAST_SKY_TEXT, EcobeeUtils.undefOrString(skyMap.get(forecast.sky)));
            }
        }
    }

    private void updateVersion(@Nullable VersionDTO version) {
        if (version == null) {
            return;
        }
        final String grp = CHGRP_VERSION + "#";
        updateChannel(grp + CH_THERMOSTAT_FIRMWARE_VERSION,
                EcobeeUtils.undefOrString(version.thermostatFirmwareVersion));
    }

    private void updateLocation(@Nullable LocationDTO loc) {
        LocationDTO location = EMPTY_LOCATION;
        if (loc != null) {
            location = loc;
        }
        final String grp = CHGRP_LOCATION + "#";
        updateChannel(grp + CH_TIME_ZONE_OFFSET_MINUTES, EcobeeUtils.undefOrDecimal(location.timeZoneOffsetMinutes));
        updateChannel(grp + CH_TIME_ZONE, EcobeeUtils.undefOrString(location.timeZone));
        updateChannel(grp + CH_IS_DAYLIGHT_SAVING, EcobeeUtils.undefOrOnOff(location.isDaylightSaving));
        updateChannel(grp + CH_STREET_ADDRESS, EcobeeUtils.undefOrString(location.streetAddress));
        updateChannel(grp + CH_CITY, EcobeeUtils.undefOrString(location.city));
        updateChannel(grp + CH_PROVINCE_STATE, EcobeeUtils.undefOrString(location.provinceState));
        updateChannel(grp + CH_COUNTRY, EcobeeUtils.undefOrString(location.country));
        updateChannel(grp + CH_POSTAL_CODE, EcobeeUtils.undefOrString(location.postalCode));
        updateChannel(grp + CH_PHONE_NUMBER, EcobeeUtils.undefOrString(location.phoneNumber));
        updateChannel(grp + CH_MAP_COORDINATES, EcobeeUtils.undefOrPoint(location.mapCoordinates));
    }

    private void updateHouseDetails(@Nullable HouseDetailsDTO hd) {
        HouseDetailsDTO houseDetails = EMPTY_HOUSEDETAILS;
        if (hd != null) {
            houseDetails = hd;
        }
        final String grp = CHGRP_HOUSE_DETAILS + "#";
        updateChannel(grp + CH_HOUSEDETAILS_STYLE, EcobeeUtils.undefOrString(houseDetails.style));
        updateChannel(grp + CH_HOUSEDETAILS_SIZE, EcobeeUtils.undefOrDecimal(houseDetails.size));
        updateChannel(grp + CH_HOUSEDETAILS_NUMBER_OF_FLOORS, EcobeeUtils.undefOrDecimal(houseDetails.numberOfFloors));
        updateChannel(grp + CH_HOUSEDETAILS_NUMBER_OF_ROOMS, EcobeeUtils.undefOrDecimal(houseDetails.numberOfRooms));
        updateChannel(grp + CH_HOUSEDETAILS_NUMBER_OF_OCCUPANTS,
                EcobeeUtils.undefOrDecimal(houseDetails.numberOfOccupants));
        updateChannel(grp + CH_HOUSEDETAILS_AGE, EcobeeUtils.undefOrDecimal(houseDetails.age));
        updateChannel(grp + CH_HOUSEDETAILS_WINDOW_EFFICIENCY,
                EcobeeUtils.undefOrDecimal(houseDetails.windowEfficiency));
    }

    private void updateManagement(@Nullable ManagementDTO mgmt) {
        ManagementDTO management = EMPTY_MANAGEMENT;
        if (mgmt != null) {
            management = mgmt;
        }
        final String grp = CHGRP_MANAGEMENT + "#";
        updateChannel(grp + CH_MANAGEMENT_ADMIN_CONTACT, EcobeeUtils.undefOrString(management.administrativeContact));
        updateChannel(grp + CH_MANAGEMENT_BILLING_CONTACT, EcobeeUtils.undefOrString(management.billingContact));
        updateChannel(grp + CH_MANAGEMENT_NAME, EcobeeUtils.undefOrString(management.name));
        updateChannel(grp + CH_MANAGEMENT_PHONE, EcobeeUtils.undefOrString(management.phone));
        updateChannel(grp + CH_MANAGEMENT_EMAIL, EcobeeUtils.undefOrString(management.email));
        updateChannel(grp + CH_MANAGEMENT_WEB, EcobeeUtils.undefOrString(management.web));
        updateChannel(grp + CH_MANAGEMENT_SHOW_ALERT_IDT, EcobeeUtils.undefOrOnOff(management.showAlertIdt));
        updateChannel(grp + CH_MANAGEMENT_SHOW_ALERT_WEB, EcobeeUtils.undefOrOnOff(management.showAlertWeb));
    }

    private void updateTechnician(@Nullable TechnicianDTO tech) {
        TechnicianDTO technician = EMPTY_TECHNICIAN;
        if (tech != null) {
            technician = tech;
        }
        final String grp = CHGRP_TECHNICIAN + "#";
        updateChannel(grp + CH_TECHNICIAN_CONTRACTOR_REF, EcobeeUtils.undefOrString(technician.contractorRef));
        updateChannel(grp + CH_TECHNICIAN_NAME, EcobeeUtils.undefOrString(technician.name));
        updateChannel(grp + CH_TECHNICIAN_PHONE, EcobeeUtils.undefOrString(technician.phone));
        updateChannel(grp + CH_TECHNICIAN_STREET_ADDRESS, EcobeeUtils.undefOrString(technician.streetAddress));
        updateChannel(grp + CH_TECHNICIAN_CITY, EcobeeUtils.undefOrString(technician.city));
        updateChannel(grp + CH_TECHNICIAN_PROVINCE_STATE, EcobeeUtils.undefOrString(technician.provinceState));
        updateChannel(grp + CH_TECHNICIAN_COUNTRY, EcobeeUtils.undefOrString(technician.country));
        updateChannel(grp + CH_TECHNICIAN_POSTAL_CODE, EcobeeUtils.undefOrString(technician.postalCode));
        updateChannel(grp + CH_TECHNICIAN_EMAIL, EcobeeUtils.undefOrString(technician.email));
        updateChannel(grp + CH_TECHNICIAN_WEB, EcobeeUtils.undefOrString(technician.web));
    }

    private void updateChannel(String channelId, State state) {
        updateState(channelId, state);
        stateCache.put(channelId, state);
    }

    @SuppressWarnings("null")
    private void updateRemoteSensors(@Nullable List<RemoteSensorDTO> remoteSensors) {
        if (remoteSensors == null) {
            return;
        }
        logger.debug("ThermostatBridge: Thermostat '{}' has {} remote sensors", thermostatId, remoteSensors.size());
        for (RemoteSensorDTO sensor : remoteSensors) {
            EcobeeSensorThingHandler handler = sensorHandlers.get(sensor.id);
            if (handler != null) {
                logger.debug("ThermostatBridge: Sending data to sensor handler '{}({})' of type '{}'", sensor.id,
                        sensor.name, sensor.type);
                handler.updateChannels(sensor);
            }
        }
    }

    private void performThermostatUpdate(ThermostatDTO thermostat) {
        SelectionDTO selection = new SelectionDTO();
        selection.setThermostats(Set.of(thermostatId));
        ThermostatUpdateRequestDTO request = new ThermostatUpdateRequestDTO(selection);
        request.thermostat = thermostat;
        EcobeeAccountBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            handler.performThermostatUpdate(request);
        }
    }

    private @Nullable EcobeeAccountBridgeHandler getBridgeHandler() {
        EcobeeAccountBridgeHandler handler = null;
        Bridge bridge = getBridge();
        if (bridge != null) {
            handler = (EcobeeAccountBridgeHandler) bridge.getHandler();
        }
        return handler;
    }

    @SuppressWarnings("null")
    private boolean isChannelReadOnly(ChannelUID channelUID) {
        Boolean isReadOnly = channelReadOnlyMap.get(channelUID);
        return isReadOnly != null ? isReadOnly : true;
    }

    private void clearSavedState() {
        savedThermostat = null;
        savedSensors = null;
        stateCache.clear();
    }

    private void initializeReadOnlyChannels() {
        channelReadOnlyMap.clear();
        for (Channel channel : thing.getChannels()) {
            ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID != null) {
                ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID, null);
                if (channelType != null) {
                    channelReadOnlyMap.putIfAbsent(channel.getUID(), channelType.getState().isReadOnly());
                }
            }
        }
    }

    private void initializeWeatherMaps() {
        initializeSymbolMap();
        initializeSkyMap();
    }

    private void initializeSymbolMap() {
        symbolMap.clear();
        symbolMap.put(-2, "NO SYMBOL");
        symbolMap.put(0, "SUNNY");
        symbolMap.put(1, "FEW CLOUDS");
        symbolMap.put(2, "PARTLY CLOUDY");
        symbolMap.put(3, "MOSTLY CLOUDY");
        symbolMap.put(4, "OVERCAST");
        symbolMap.put(5, "DRIZZLE");
        symbolMap.put(6, "RAIN");
        symbolMap.put(7, "FREEZING RAIN");
        symbolMap.put(8, "SHOWERS");
        symbolMap.put(9, "HAIL");
        symbolMap.put(10, "SNOW");
        symbolMap.put(11, "FLURRIES");
        symbolMap.put(12, "FREEZING SNOW");
        symbolMap.put(13, "BLIZZARD");
        symbolMap.put(14, "PELLETS");
        symbolMap.put(15, "THUNDERSTORM");
        symbolMap.put(16, "WINDY");
        symbolMap.put(17, "TORNADO");
        symbolMap.put(18, "FOG");
        symbolMap.put(19, "HAZE");
        symbolMap.put(20, "SMOKE");
        symbolMap.put(21, "DUST");
    }

    private void initializeSkyMap() {
        skyMap.clear();
        skyMap.put(1, "SUNNY");
        skyMap.put(2, "CLEAR");
        skyMap.put(3, "MOSTLY SUNNY");
        skyMap.put(4, "MOSTLY CLEAR");
        skyMap.put(5, "HAZY SUNSHINE");
        skyMap.put(6, "HAZE");
        skyMap.put(7, "PASSING CLOUDS");
        skyMap.put(8, "MORE SUN THAN CLOUDS");
        skyMap.put(9, "SCATTERED CLOUDS");
        skyMap.put(10, "PARTLY CLOUDY");
        skyMap.put(11, "A MIXTURE OF SUN AND CLOUDS");
        skyMap.put(12, "HIGH LEVEL CLOUDS");
        skyMap.put(13, "MORE CLOUDS THAN SUN");
        skyMap.put(14, "PARTLY SUNNY");
        skyMap.put(15, "BROKEN CLOUDS");
        skyMap.put(16, "MOSTLY CLOUDY");
        skyMap.put(17, "CLOUDY");
        skyMap.put(18, "OVERCAST");
        skyMap.put(19, "LOW CLOUDS");
        skyMap.put(20, "LIGHT FOG");
        skyMap.put(21, "FOG");
        skyMap.put(22, "DENSE FOG");
        skyMap.put(23, "ICE FOG");
        skyMap.put(24, "SANDSTORM");
        skyMap.put(25, "DUSTSTORM");
        skyMap.put(26, "INCREASING CLOUDINESS");
        skyMap.put(27, "DECREASING CLOUDINESS");
        skyMap.put(28, "CLEARING SKIES");
        skyMap.put(29, "BREAKS OF SUN LATE");
        skyMap.put(30, "EARLY FOG FOLLOWED BY SUNNY SKIES");
        skyMap.put(31, "AFTERNOON CLOUDS");
        skyMap.put(32, "MORNING CLOUDS");
        skyMap.put(33, "SMOKE");
        skyMap.put(34, "LOW LEVEL HAZE");
    }
}
