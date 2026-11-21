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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myvaillant.internal.api.JsonUtil;
import org.openhab.binding.myvaillant.internal.api.MyVaillantApi;
import org.openhab.binding.myvaillant.internal.api.MyVaillantException;
import org.openhab.binding.myvaillant.internal.api.SystemData;
import org.openhab.binding.myvaillant.internal.config.ComponentConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

import com.google.gson.JsonObject;

/**
 * A heating circuit with its flow temperature and heating curve.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public class CircuitHandler extends ComponentHandler {

    private int index = -1;

    public CircuitHandler(Thing thing) {
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
    protected boolean isPresent(SystemData systemData) {
        return systemData.getCircuits().containsKey(index);
    }

    @Override
    protected void updateChannels(SystemData systemData) {
        JsonObject circuit = systemData.getCircuits().get(index);
        if (circuit == null) {
            return;
        }
        updateState(CHANNEL_CIRCUIT_STATE, States.string(JsonUtil.getString(circuit, "circuit_state")));
        updateState(CHANNEL_CURRENT_FLOW_TEMPERATURE,
                States.temperature(JsonUtil.getDouble(circuit, "current_circuit_flow_temperature")));
        updateState(CHANNEL_FLOW_TEMPERATURE_SETPOINT,
                States.temperature(JsonUtil.getDouble(circuit, "heating_circuit_flow_setpoint")));
        updateState(CHANNEL_HEATING_CURVE, States.decimal(JsonUtil.getDouble(circuit, "heating_curve")));
        updateState(CHANNEL_HEAT_DEMAND_LIMIT,
                States.temperature(JsonUtil.getDouble(circuit, "heat_demand_limited_by_outside_temperature")));
        updateState(CHANNEL_MIN_FLOW_TEMPERATURE_SETPOINT,
                States.temperature(JsonUtil.getDouble(circuit, "heating_flow_temperature_minimum_setpoint")));
        updateState(CHANNEL_MAX_FLOW_TEMPERATURE_SETPOINT,
                States.temperature(JsonUtil.getDouble(circuit, "heating_flow_temperature_maximum_setpoint")));
        updateState(CHANNEL_ROOM_TEMPERATURE_CONTROL_MODE,
                States.string(JsonUtil.getString(circuit, "room_temperature_control_mode")));
        updateState(CHANNEL_ENERGY_MANAGER_STATE_CIRCUIT,
                States.string(JsonUtil.getString(circuit, "calculated_energy_manager_state")));
        updateState(CHANNEL_COOLING_ALLOWED, States.onOff(JsonUtil.getBoolean(circuit, "is_cooling_allowed")));
    }

    @Override
    protected boolean sendCommand(String channelId, Command command, MyVaillantApi api, SystemData systemData,
            SystemHandler systemHandler) throws MyVaillantException {
        String systemId = systemData.getSystemId();
        switch (channelId) {
            case CHANNEL_HEATING_CURVE -> {
                Double heatingCurve = States.toDouble(command, null);
                if (heatingCurve == null) {
                    return false;
                }
                api.setCircuitHeatingCurve(systemId, index, heatingCurve);
                updateState(channelId, new DecimalType(heatingCurve));
            }
            case CHANNEL_HEAT_DEMAND_LIMIT, CHANNEL_MIN_FLOW_TEMPERATURE_SETPOINT -> {
                Double temperature = States.toCelsius(command);
                if (temperature == null) {
                    return false;
                }
                if (CHANNEL_HEAT_DEMAND_LIMIT.equals(channelId)) {
                    api.setCircuitHeatDemandLimitedByOutsideTemperature(systemId, index, temperature);
                } else {
                    api.setCircuitMinFlowTemperatureSetpoint(systemId, index, temperature);
                }
                updateState(channelId, new QuantityType<>(temperature, SIUnits.CELSIUS));
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
