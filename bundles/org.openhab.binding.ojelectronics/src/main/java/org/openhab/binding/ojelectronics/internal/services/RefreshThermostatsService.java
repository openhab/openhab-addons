/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.ojelectronics.internal.models.thermostat.Thermostat;
import org.openhab.binding.ojelectronics.internal.models.thermostat.ThermostatBase;
import org.openhab.binding.ojelectronics.internal.models.thermostat.ThermostatRealTimeValues;
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

    private final ArrayList<Thermostat> thermostats;
    private final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(RefreshThermostatsService.class));
    private final List<Thing> things;
    private final ArrayList<ThermostatRealTimeValues> realTimeValues;

    /**
     * Creates a new instance of {@link RefreshThermostatsService}
     *
     * @param thermostats {@link Thermostat}
     * @param things Things
     */
    public RefreshThermostatsService(ArrayList<Thermostat> thermostats, List<Thing> things) {
        this(thermostats, new ArrayList<>(), things);
    }

    /**
     * Creates a new instance of {@link RefreshThermostatsService}
     *
     * @param thermostats {@link Thermostat}
     * @param realTimeValues {@link ThermostatRealTimeValues}
     * @param things Things
     */
    public RefreshThermostatsService(ArrayList<Thermostat> thermostats,
            ArrayList<ThermostatRealTimeValues> realTimeValues, List<Thing> things) {
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

    private <T extends ThermostatBase> void handleThermostat(T thermostat,
            BiConsumer<ThermostatHandler, T> refreshHandler) {
        things.stream().filter(thing -> thing.getHandler() instanceof ThermostatHandler)
                .map(thing -> (ThermostatHandler) thing.getHandler())
                .filter(thingHandler -> thingHandler.getSerialNumber().equals(thermostat.serialNumber))
                .forEach(thingHandler -> {
                    try {
                        refreshHandler.accept(Objects.requireNonNull(thingHandler), thermostat);
                    } catch (Exception e) {
                        logger.error("Error Handling Refresh of thermostat {} {}", thermostat, e);
                    }
                });
    }

    private void handleThermostatRefresh(ThermostatHandler thingHandler, Thermostat thermostat) {
        thingHandler.handleThermostatRefresh(thermostat);
    }

    private void handleThermostatRealTimeValueRefresh(ThermostatHandler thingHandler,
            ThermostatRealTimeValues thermostat) {
        thingHandler.handleThermostatRefresh(thermostat);
    }
}
