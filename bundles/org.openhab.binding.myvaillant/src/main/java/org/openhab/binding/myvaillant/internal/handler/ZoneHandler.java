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
package org.openhab.binding.myvaillant.internal.handler;

import static org.openhab.binding.myvaillant.internal.MyVaillantBindingConstants.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myvaillant.internal.action.ZoneActions;
import org.openhab.binding.myvaillant.internal.api.JsonUtil;
import org.openhab.binding.myvaillant.internal.api.MyVaillantException;
import org.openhab.binding.myvaillant.internal.api.SystemData;
import org.openhab.binding.myvaillant.internal.config.ComponentConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * A zone (rooms controlled together, like the climate entity of the Home Assistant integration).
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public class ZoneHandler extends ComponentHandler {

    public static final Set<String> OPERATION_MODES = Set.of("MANUAL", "TIME_CONTROLLED", "OFF");
    private static final String QUICK_VETO = "QUICK_VETO";

    private final Logger logger = LoggerFactory.getLogger(ZoneHandler.class);
    private int index = -1;

    public ZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected @Nullable String readConfiguration() {
        index = getConfigAs(ComponentConfiguration.class).index;
        return index < 0 ? "@text/offline.conf-error.no-index" : null;
    }

    @Override
    protected @Nullable String getComponentId(String systemId) {
        return componentId(systemId, index);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(ZoneActions.class);
    }

    @Override
    protected boolean isPresent(SystemData systemData) {
        return systemData.getZones().containsKey(index);
    }

    /** The zone as of the last update of the system, {@code null} before the first update or if it does not exist */
    public @Nullable JsonObject getZone() {
        SystemData systemData = getData();
        return systemData == null ? null : systemData.getZones().get(index);
    }

    /** Index of the zone in its system */
    public int getIndex() {
        return index;
    }

    @Override
    protected void updateChannels(SystemData systemData) {
        JsonObject zone = systemData.getZones().get(index);
        if (zone == null) {
            return;
        }
        JsonObject heating = JsonUtil.getObject(zone, "heating");
        JsonObject cooling = JsonUtil.getObject(zone, "cooling");
        updateState(CHANNEL_CURRENT_ROOM_TEMPERATURE,
                States.temperature(JsonUtil.getDouble(zone, "current_room_temperature")));
        updateState(CHANNEL_CURRENT_ROOM_HUMIDITY,
                States.quantity(JsonUtil.getDouble(zone, "current_room_humidity"), Units.PERCENT));
        updateState(CHANNEL_DESIRED_ROOM_TEMPERATURE,
                States.temperature(JsonUtil.getDouble(zone, "desired_room_temperature_setpoint")));
        updateState(CHANNEL_DESIRED_ROOM_TEMPERATURE_HEATING,
                States.temperature(JsonUtil.getDouble(zone, "desired_room_temperature_setpoint_heating")));
        updateState(CHANNEL_DESIRED_ROOM_TEMPERATURE_COOLING,
                States.temperature(JsonUtil.getDouble(zone, "desired_room_temperature_setpoint_cooling")));
        updateState(CHANNEL_OPERATION_MODE_HEATING,
                States.string(JsonUtil.getString(heating, "operation_mode_heating")));
        updateState(CHANNEL_OPERATION_MODE_COOLING,
                States.string(JsonUtil.getString(cooling, "operation_mode_cooling")));
        updateState(CHANNEL_MANUAL_MODE_SETPOINT_HEATING,
                States.temperature(JsonUtil.getDouble(heating, "manual_mode_setpoint_heating")));
        updateState(CHANNEL_SETPOINT_COOLING, States.temperature(JsonUtil.getDouble(cooling, "setpoint_cooling")));
        updateState(CHANNEL_SET_BACK_TEMPERATURE,
                States.temperature(JsonUtil.getDouble(heating, "set_back_temperature")));
        updateState(CHANNEL_HEATING_STATE, States.string(JsonUtil.getString(zone, "heating_state")));
        String specialFunction = JsonUtil.getString(zone, "current_special_function");
        updateState(CHANNEL_CURRENT_SPECIAL_FUNCTION, States.string(specialFunction));
        updateState(CHANNEL_HVAC_ACTION, States.string(getHvacAction(systemData, zone)));
        updateState(CHANNEL_MANUAL_COOLING_ACTIVE, States.onOff(JsonUtil.getBoolean(zone, "is_manual_cooling_active")));

        boolean quickVeto = QUICK_VETO.equals(specialFunction);
        Instant quickVetoEnd = quickVeto ? JsonUtil.getInstant(zone, "quick_veto_end_date_time") : null;
        updateState(CHANNEL_QUICK_VETO, OnOffType.from(quickVeto));
        updateState(CHANNEL_QUICK_VETO_END, States.dateTime(quickVetoEnd, systemData.getTimeZone()));
        if (quickVetoEnd != null) {
            double remainingHours = Math.max(0, Duration.between(Instant.now(), quickVetoEnd).toMinutes()) / 60.0;
            updateState(CHANNEL_QUICK_VETO_DURATION,
                    new QuantityType<>(Math.round(remainingHours * 10) / 10.0, Units.HOUR));
        } else {
            updateState(CHANNEL_QUICK_VETO_DURATION, UnDefType.UNDEF);
        }
    }

    /** Like the climate entity of the Home Assistant integration: the state of the zone's circuit */
    private static @Nullable String getHvacAction(SystemData systemData, JsonObject zone) {
        Integer circuitIndex = JsonUtil.getInteger(zone, "associated_circuit_index");
        JsonObject circuit = circuitIndex == null ? null : systemData.getCircuits().get(circuitIndex);
        String circuitState = JsonUtil.getString(circuit, "circuit_state");
        if (circuitState == null) {
            return null;
        }
        return switch (circuitState) {
            case "STANDBY" -> "idle";
            case "HEATING" -> "heating";
            case "COOLING" -> "cooling";
            default -> circuitState.toLowerCase(Locale.ROOT);
        };
    }

    @Override
    protected boolean sendCommand(String channelId, Command command, SystemData systemData, SystemHandler systemHandler)
            throws MyVaillantException, InterruptedException {
        JsonObject zone = systemData.getZones().get(index);
        double quickVetoDuration = systemHandler.getAccountConfiguration().quickVetoDurationInHours;
        switch (channelId) {
            case CHANNEL_DESIRED_ROOM_TEMPERATURE -> {
                Double temperature = States.toCelsius(command);
                if (temperature == null) {
                    return false;
                }
                setTemperature(systemHandler, zone, temperature, quickVetoDuration);
                updateState(channelId, new QuantityType<>(temperature, SIUnits.CELSIUS));
            }
            case CHANNEL_OPERATION_MODE_HEATING, CHANNEL_OPERATION_MODE_COOLING -> {
                String mode = command.toString().toUpperCase(Locale.ROOT);
                if (!OPERATION_MODES.contains(mode)) {
                    logger.warn("Unknown operation mode {}, use one of {}", command, OPERATION_MODES);
                    return false;
                }
                if (CHANNEL_OPERATION_MODE_HEATING.equals(channelId)) {
                    systemHandler.setZoneHeatingOperationMode(index, mode);
                } else {
                    systemHandler.setZoneCoolingOperationMode(index, mode);
                }
                updateState(channelId, new StringType(mode));
            }
            case CHANNEL_MANUAL_MODE_SETPOINT_HEATING, CHANNEL_SETPOINT_COOLING, CHANNEL_SET_BACK_TEMPERATURE -> {
                Double temperature = States.toCelsius(command);
                if (temperature == null) {
                    return false;
                }
                if (CHANNEL_MANUAL_MODE_SETPOINT_HEATING.equals(channelId)) {
                    systemHandler.setZoneManualModeSetpoint(index, temperature, "HEATING");
                } else if (CHANNEL_SETPOINT_COOLING.equals(channelId)) {
                    systemHandler.setZoneCoolingSetpoint(index, temperature);
                } else {
                    systemHandler.setZoneSetBackTemperature(index, temperature);
                }
                updateState(channelId, new QuantityType<>(temperature, SIUnits.CELSIUS));
            }
            case CHANNEL_QUICK_VETO -> {
                if (command == OnOffType.ON) {
                    // Like the boost preset in Home Assistant: the manual mode setpoint for the default duration
                    Double temperature = JsonUtil.getDouble(JsonUtil.getObject(zone, "heating"),
                            "manual_mode_setpoint_heating");
                    if (temperature == null) {
                        temperature = JsonUtil.getDouble(zone, "desired_room_temperature_setpoint");
                    }
                    if (temperature == null) {
                        logger.warn("Zone {} has no temperature for a quick veto", index);
                        return false;
                    }
                    systemHandler.startZoneQuickVeto(index, temperature, quickVetoDuration);
                } else if (command == OnOffType.OFF) {
                    systemHandler.cancelZoneQuickVeto(index);
                } else {
                    return false;
                }
                updateState(channelId, (OnOffType) command);
            }
            case CHANNEL_QUICK_VETO_DURATION -> {
                Double hours = States.toDouble(command, Units.HOUR);
                if (hours == null) {
                    return false;
                }
                if (!QUICK_VETO.equals(JsonUtil.getString(zone, "current_special_function"))) {
                    logger.debug("Zone {} has no quick veto, its duration cannot be changed", index);
                    return false;
                }
                systemHandler.updateZoneQuickVeto(index, null, hours);
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Like setting the temperature of the climate entity in Home Assistant: in manual mode the manual mode setpoint
     * changes, otherwise a quick veto is started or changed.
     */
    void setTemperature(SystemHandler systemHandler, @Nullable JsonObject zone, double temperature,
            double quickVetoDurationHours) throws MyVaillantException, InterruptedException {
        String mode = JsonUtil.getString(JsonUtil.getObject(zone, "heating"), "operation_mode_heating");
        if ("MANUAL".equals(mode)) {
            systemHandler.setZoneManualModeSetpoint(index, temperature, "HEATING");
        } else if (QUICK_VETO.equals(JsonUtil.getString(zone, "current_special_function"))) {
            systemHandler.updateZoneQuickVeto(index, temperature, null);
        } else {
            systemHandler.startZoneQuickVeto(index, temperature, quickVetoDurationHours);
        }
    }
}
