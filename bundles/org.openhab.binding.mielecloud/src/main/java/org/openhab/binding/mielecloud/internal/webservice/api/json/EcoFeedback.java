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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Immutable POJO representing the amount of water and energy used by the current running program up to the present
 * moment. Queried from the Miele REST API.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class EcoFeedback {
    @Nullable
    private WaterConsumption currentWaterConsumption;
    @Nullable
    private EnergyConsumption currentEnergyConsumption;
    @Nullable
    private Double waterForecast;
    @Nullable
    private Double energyForecast;

    public Optional<WaterConsumption> getCurrentWaterConsumption() {
        return Optional.ofNullable(currentWaterConsumption);
    }

    public Optional<EnergyConsumption> getCurrentEnergyConsumption() {
        return Optional.ofNullable(currentEnergyConsumption);
    }

    /**
     * Gets the relative water usage for the selected program from 0 to 1.
     */
    public Optional<Double> getWaterForecast() {
        return Optional.ofNullable(waterForecast);
    }

    /**
     * Gets the relative energy usage for the selected program from 0 to 1.
     */
    public Optional<Double> getEnergyForecast() {
        return Optional.ofNullable(energyForecast);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentWaterConsumption, currentEnergyConsumption, waterForecast, energyForecast);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EcoFeedback other = (EcoFeedback) obj;
        return Objects.equals(currentWaterConsumption, other.currentWaterConsumption)
                && Objects.equals(currentEnergyConsumption, other.currentEnergyConsumption)
                && Objects.equals(waterForecast, other.waterForecast)
                && Objects.equals(energyForecast, other.energyForecast);
    }

    @Override
    public String toString() {
        return "EcoFeedback [currentWaterConsumption=" + currentWaterConsumption + ", currentEnergyConsumption="
                + currentEnergyConsumption + ", waterForecast=" + waterForecast + ", energyForecast=" + energyForecast
                + "]";
    }
}
