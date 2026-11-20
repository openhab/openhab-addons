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

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myvaillant.internal.action.DhwActions;
import org.openhab.binding.myvaillant.internal.api.JsonUtil;
import org.openhab.binding.myvaillant.internal.api.MyVaillantApi;
import org.openhab.binding.myvaillant.internal.api.MyVaillantException;
import org.openhab.binding.myvaillant.internal.api.SystemData;
import org.openhab.binding.myvaillant.internal.config.ComponentConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Domestic hot water (the water heater entity of the Home Assistant integration).
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public class DhwHandler extends ComponentHandler {

    private static final Set<String> OPERATION_MODES = Set.of("MANUAL", "TIME_CONTROLLED", "OFF");
    private static final String CYLINDER_BOOST = "CYLINDER_BOOST";

    private final Logger logger = LoggerFactory.getLogger(DhwHandler.class);
    private int index = -1;

    public DhwHandler(Thing thing) {
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
        return Set.of(DhwActions.class);
    }

    @Override
    protected boolean isPresent(SystemData systemData) {
        return systemData.getDomesticHotWater().containsKey(index);
    }

    /** The hot water as of the last update of the system, {@code null} before the first update or if it is gone */
    public @Nullable JsonObject getDomesticHotWater() {
        SystemData systemData = getData();
        return systemData == null ? null : systemData.getDomesticHotWater().get(index);
    }

    /** Index of the hot water in its system */
    public int getIndex() {
        return index;
    }

    @Override
    protected void updateChannels(SystemData systemData) {
        JsonObject dhw = systemData.getDomesticHotWater().get(index);
        if (dhw == null) {
            return;
        }
        updateState(CHANNEL_CURRENT_DHW_TEMPERATURE,
                States.temperature(JsonUtil.getDouble(dhw, "current_dhw_temperature")));
        updateState(CHANNEL_TAPPING_SETPOINT, States.temperature(JsonUtil.getDouble(dhw, "tapping_setpoint")));
        updateState(CHANNEL_OPERATION_MODE, States.string(JsonUtil.getString(dhw, "operation_mode_dhw")));
        String specialFunction = JsonUtil.getString(dhw, "current_special_function");
        updateState(CHANNEL_CURRENT_SPECIAL_FUNCTION, States.string(specialFunction));
        updateState(CHANNEL_BOOST, OnOffType.from(CYLINDER_BOOST.equals(specialFunction)));

        Map<String, String> properties = new HashMap<>(editProperties());
        Double minSetpoint = JsonUtil.getDouble(dhw, "min_setpoint");
        Double maxSetpoint = JsonUtil.getDouble(dhw, "max_setpoint");
        if (minSetpoint != null) {
            properties.put("minSetpoint", minSetpoint.toString());
        }
        if (maxSetpoint != null) {
            properties.put("maxSetpoint", maxSetpoint.toString());
        }
        if (!properties.equals(editProperties())) {
            updateProperties(properties);
        }
    }

    @Override
    protected boolean sendCommand(String channelId, Command command, MyVaillantApi api, SystemData systemData,
            SystemHandler systemHandler) throws MyVaillantException {
        String systemId = systemData.getSystemId();
        switch (channelId) {
            case CHANNEL_TAPPING_SETPOINT -> {
                Double temperature = States.toCelsius(command);
                if (temperature == null) {
                    return false;
                }
                int setpoint = (int) Math.round(temperature);
                api.setDhwTemperature(systemId, index, setpoint);
                updateState(channelId, new QuantityType<>(setpoint, SIUnits.CELSIUS));
            }
            case CHANNEL_OPERATION_MODE -> {
                String mode = command.toString().toUpperCase(Locale.ROOT);
                if (!OPERATION_MODES.contains(mode)) {
                    logger.warn("Unknown operation mode {}, use one of {}", command, OPERATION_MODES);
                    return false;
                }
                api.setDhwOperationMode(systemId, index, mode);
                updateState(channelId, new StringType(mode));
            }
            case CHANNEL_BOOST -> {
                if (command == OnOffType.ON) {
                    api.startDhwBoost(systemId, index);
                } else if (command == OnOffType.OFF) {
                    api.cancelDhwBoost(systemId, index);
                } else {
                    return false;
                }
                updateState(channelId, (OnOffType) command);
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
