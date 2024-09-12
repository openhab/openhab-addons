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
package org.openhab.binding.plugwiseha.internal.api.model.dto;

import java.util.Map;

/**
 * The {@link Locations} class is an object model class that mirrors the XML
 * structure provided by the Plugwise Home Automation controller for the
 * collection of Plugwise locations/zones. It extends the
 * {@link PlugwiseHACollection} class.
 * 
 * @author B. van Wetten - Initial contribution
 */
public class Locations extends PlugwiseHACollection<Location> {

    @Override
    public void merge(Map<String, Location> locations) {
        if (locations != null) {
            for (Location location : locations.values()) {
                String id = location.getId();
                Location originalLocation = this.get(id);

                Boolean originalLocationIsOlder = false;
                if (originalLocation != null) {
                    originalLocationIsOlder = originalLocation.isOlderThan(location);
                }

                if (originalLocation != null && originalLocationIsOlder) {
                    Logs updatedPointLogs = location.getPointLogs();
                    if (updatedPointLogs != null) {
                        updatedPointLogs.merge(originalLocation.getPointLogs());
                    }

                    ActuatorFunctionalities updatedActuatorFunctionalities = location.getActuatorFunctionalities();
                    if (updatedActuatorFunctionalities != null) {
                        updatedActuatorFunctionalities.merge(originalLocation.getActuatorFunctionalities());
                    }

                    this.put(id, location);
                }
            }
        }
    }
}
