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
import java.util.Optional;

/**
 * The {@link ActuatorFunctionalities} class is an object model class that
 * mirrors the XML structure provided by the Plugwise Home Automation controller
 * for the collection of actuator functionalities. (e.g. 'offset', 'relay', et
 * cetera). It extends the {@link CustomCollection} class.
 * 
 * @author B. van Wetten - Initial contribution
 */

public class ActuatorFunctionalities extends PlugwiseHACollection<ActuatorFunctionality> {

    private final String THERMOSTAT_FUNCTIONALITY = "thermostat";
    private final String OFFSETTEMPERATURE_FUNCTIONALITY = "temperature_offset";
    private final String RELAY_FUNCTIONALITY = "relay";

    public Optional<Boolean> getRelayLockState() {
        return this.getFunctionalityRelay().flatMap(ActuatorFunctionality::getRelayLockState)
                .map(Boolean::parseBoolean);
    }

    public Optional<ActuatorFunctionality> getFunctionalityThermostat() {
        return Optional.ofNullable(this.get(THERMOSTAT_FUNCTIONALITY));
    }

    public Optional<ActuatorFunctionality> getFunctionalityOffsetTemperature() {
        return Optional.ofNullable(this.get(OFFSETTEMPERATURE_FUNCTIONALITY));
    }

    public Optional<ActuatorFunctionality> getFunctionalityRelay() {
        return Optional.ofNullable(this.get(RELAY_FUNCTIONALITY));
    }

    @Override
    public void merge(Map<String, ActuatorFunctionality> actuatorFunctionalities) {
        if (actuatorFunctionalities != null) {
            for (ActuatorFunctionality actuatorFunctionality : actuatorFunctionalities.values()) {
                String type = actuatorFunctionality.getType();
                ActuatorFunctionality originalActuatorFunctionality = this.get(type);

                if (originalActuatorFunctionality == null
                        || originalActuatorFunctionality.isOlderThan(actuatorFunctionality)) {
                    this.put(type, actuatorFunctionality);
                }
            }
        }
    }
}
