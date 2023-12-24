/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Optional;

/**
 * The {@link ActuatorFunctionalities} class is an object model class that
 * mirrors the XML structure provided by the Plugwise Home Automation controller
 * for the collection of actuator functionalities. (e.g. 'offset', 'relay', et
 * cetera). It extends the {@link PlugwiseHACollection} class.
 * 
 * @author B. van Wetten - Initial contribution
 */

public class ActuatorFunctionalities extends PlugwiseHACollection<ActuatorFunctionality> {

    private static final String THERMOSTAT_FUNCTIONALITY = "thermostat";
    private static final String OFFSETTEMPERATURE_FUNCTIONALITY = "temperature_offset";
    private static final String RELAY_FUNCTIONALITY = "relay";

    public Optional<Boolean> getRelayLockState() {
        return this.getFunctionalityRelay().flatMap(ActuatorFunctionality::getRelayLockState)
                .map(Boolean::parseBoolean);
    }

    public String getRegulationControl() {
        ActuatorFunctionality functionality = this.getFunctionalityThermostat().orElse(null);
        if (functionality != null) {
            return functionality.getRegulationControl();
        }
        return null;
    }

    public Optional<Boolean> getCoolingAllowed() {
        return this.getFunctionalityThermostat().flatMap(ActuatorFunctionality::getCoolingAllowed)
                .map(Boolean::parseBoolean);
    }

    public Optional<Boolean> getPreHeatState() {
        return this.getFunctionalityThermostat().flatMap(ActuatorFunctionality::getPreHeatState)
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

                Boolean originalIsOlder = false;
                if (originalActuatorFunctionality != null) {
                    originalIsOlder = originalActuatorFunctionality.isOlderThan(actuatorFunctionality);
                }

                if (originalActuatorFunctionality == null || originalIsOlder) {
                    this.put(type, actuatorFunctionality);
                }
            }
        }
    }
}
