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
package org.openhab.binding.ojelectronics.internal.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ojelectronics.internal.ThermostatHandler;
import org.openhab.binding.ojelectronics.internal.models.thermostat.ThermostatModel;
import org.openhab.binding.ojelectronics.internal.models.thermostat.ThermostatModelBase;
import org.openhab.binding.ojelectronics.internal.models.thermostat.ThermostatRealTimeValuesModel;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Refreshes values of {@link ThermostatHandler}
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
public class RefreshThermostatsService {

    private final List<ThermostatModel> thermostats;
    private final Logger logger = LoggerFactory.getLogger(RefreshThermostatsService.class);
    private final List<Thing> things;
    private final List<ThermostatRealTimeValuesModel> realTimeValues;

    /**
     * Creates a new instance of {@link RefreshThermostatsService}
     *
     * @param thermostats {@link ThermostatModel}
     * @param things Things
     */
    public RefreshThermostatsService(List<ThermostatModel> thermostats, List<Thing> things) {
        this(thermostats, new ArrayList<>(), things);
    }

    /**
     * Creates a new instance of {@link RefreshThermostatsService}
     *
     * @param thermostats {@link ThermostatModel}
     * @param realTimeValues {@link ThermostatRealTimeValuesModel}
     * @param things Things
     */
    public RefreshThermostatsService(List<ThermostatModel> thermostats,
            List<ThermostatRealTimeValuesModel> realTimeValues, List<Thing> things) {
        this.thermostats = thermostats;
        this.things = things;
        this.realTimeValues = realTimeValues;
        if (this.things.isEmpty()) {
            logger.warn("Bridge contains no thermostats.");
        }
    }

    /**
     * Handles the changes to all things.
     */
    public synchronized void handle() {
        thermostats.forEach(thermostat -> handleThermostat(thermostat, this::handleThermostatRefresh));
        realTimeValues.forEach(thermostat -> handleThermostat(thermostat, this::handleThermostatRealTimeValueRefresh));
    }

    private <T extends ThermostatModelBase> void handleThermostat(T thermostat,
            BiConsumer<ThermostatHandler, T> refreshHandler) {
        things.stream().filter(thing -> thing.getHandler() instanceof ThermostatHandler)
                .map(thing -> (ThermostatHandler) thing.getHandler())
                .filter(thingHandler -> thingHandler.getSerialNumber().equals(thermostat.serialNumber))
                .forEach(thingHandler -> {
                    try {
                        refreshHandler.accept(Objects.requireNonNull(thingHandler), thermostat);
                    } catch (Exception e) {
                        logger.info("Error Handling Refresh of thermostat {}", thermostat, e);
                    }
                });
    }

    private void handleThermostatRefresh(ThermostatHandler thingHandler, ThermostatModel thermostat) {
        thingHandler.handleThermostatRefresh(thermostat);
    }

    private void handleThermostatRealTimeValueRefresh(ThermostatHandler thingHandler,
            ThermostatRealTimeValuesModel thermostat) {
        thingHandler.handleThermostatRefresh(thermostat);
    }
}
