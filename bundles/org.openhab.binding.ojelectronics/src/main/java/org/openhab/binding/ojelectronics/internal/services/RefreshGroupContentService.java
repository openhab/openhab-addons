/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.ojelectronics.internal.ThermostatHandler;
import org.openhab.binding.ojelectronics.internal.models.groups.GroupContent;
import org.openhab.binding.ojelectronics.internal.models.groups.Thermostat;

/**
 * Refreshes values of {@link ThermostatHandler}
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
public class RefreshGroupContentService {

    private final List<GroupContent> groupContentList;
    private List<Thing> things;

    /**
     * Creates a new instance of {@link RefreshGroupContentService}
     *
     * @param groupContents {@link GroupContent}
     * @param things Things
     */
    public RefreshGroupContentService(List<GroupContent> groupContents, List<Thing> things) {
        this.groupContentList = groupContents;
        this.things = things;
    }

    /**
     * Handles the changes to all things.
     */
    public void handle() {
        groupContentList.stream().flatMap(entry -> entry.thermostats.stream()).forEach(this::handleThermostat);
    }

    private void handleThermostat(Thermostat thermostat) {
        things.stream().filter(thing -> thing.getHandler() instanceof ThermostatHandler)
                .map(thing -> (ThermostatHandler) thing.getHandler())
                .filter(thingHandler -> thingHandler.getSerialNumber().equals(thermostat.serialNumber))
                .forEach(thingHandler -> thingHandler.handleThermostatRefresh(thermostat));
    }
}
