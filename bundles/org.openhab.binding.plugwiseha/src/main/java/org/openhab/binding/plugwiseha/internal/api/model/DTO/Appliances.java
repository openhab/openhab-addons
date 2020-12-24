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
 * The {@link Appliances} class is an object model class that mirrors the XML
 * structure provided by the Plugwise Home Automation controller for the
 * collection of appliances. It extends the {@link PlugwiseHACollection} class.
 * 
 * @author B. van Wetten - Initial contribution
 */
public class Appliances extends PlugwiseHACollection<Appliance> {

    @Override
    public void merge(Map<String, Appliance> appliances) {
        if (appliances != null) {
            for (Appliance updatedAppliance : appliances.values()) {
                String id = updatedAppliance.getId();
                Appliance originalAppliance = this.get(id);

                if (originalAppliance != null && originalAppliance.isOlderThan(updatedAppliance)) {
                    Logs updatedPointLogs = updatedAppliance.getPointLogs();
                    ActuatorFunctionalities updatedActuatorFunctionalities = updatedAppliance
                            .getActuatorFunctionalities();

                    updatedPointLogs.merge(originalAppliance.getPointLogs());
                    updatedActuatorFunctionalities.merge(originalAppliance.getActuatorFunctionalities());

                    this.put(id, updatedAppliance);
                }
            }
        }
    }
}
