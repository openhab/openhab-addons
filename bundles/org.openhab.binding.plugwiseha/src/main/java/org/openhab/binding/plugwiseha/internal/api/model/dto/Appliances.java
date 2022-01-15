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
package org.openhab.binding.plugwiseha.internal.api.model.dto;

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
    public void merge(Map<String, Appliance> appliancesToMerge) {
        if (appliancesToMerge != null) {
            for (Appliance applianceToMerge : appliancesToMerge.values()) {
                String id = applianceToMerge.getId();
                Appliance originalAppliance = this.get(id);

                Boolean originalApplianceIsOlder = false;
                if (originalAppliance != null) {
                    originalApplianceIsOlder = originalAppliance.isOlderThan(applianceToMerge);
                }

                if (originalAppliance != null && originalApplianceIsOlder) {
                    Logs updatedPointLogs = applianceToMerge.getPointLogs();
                    if (updatedPointLogs != null) {
                        updatedPointLogs.merge(originalAppliance.getPointLogs());
                    }

                    ActuatorFunctionalities updatedActuatorFunctionalities = applianceToMerge
                            .getActuatorFunctionalities();
                    if (updatedActuatorFunctionalities != null) {
                        updatedActuatorFunctionalities.merge(originalAppliance.getActuatorFunctionalities());
                    }

                    this.put(id, applianceToMerge);
                }
            }
        }
    }
}
