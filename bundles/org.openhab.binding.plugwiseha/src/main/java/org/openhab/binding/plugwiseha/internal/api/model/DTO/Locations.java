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

package org.openhab.binding.plugwiseha.internal.api.model.DTO;

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
            for (Location updatedLocation : locations.values()) {
                String id = updatedLocation.getId();
                Location originalLocation = this.get(id);

                try {
                    if (originalLocation != null && originalLocation.isOlderThan(updatedLocation)) {
                        Logs updatedPointLogs = updatedLocation.getPointLogs();
                        ActuatorFunctionalities updatedActuatorFunctionalities = updatedLocation
                                .getActuatorFunctionalities();

                        updatedPointLogs.merge(originalLocation.getPointLogs());
                        updatedActuatorFunctionalities.merge(originalLocation.getActuatorFunctionalities());

                        this.put(id, updatedLocation);
                    }
                } catch (NullPointerException e) {
                    e.toString();
                }
            }
        }
    }
}
