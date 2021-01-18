/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.dto;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants;

/**
 * The {@link PriceOptimiserResult} is a DTO class representing the results of a price optimiser run.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class PriceOptimiserResult {
    // optimiser inputs
    public ZonedDateTime requestedStartTime = OctopusEnergyBindingConstants.UNDEFINED_TIME;
    public ZonedDateTime requestedEndTime = OctopusEnergyBindingConstants.UNDEFINED_TIME;
    public Duration requestedDuration = Duration.ZERO;

    // optimiser outputs
    public ZonedDateTime optimisedStartTime = OctopusEnergyBindingConstants.UNDEFINED_TIME;
    public BigDecimal optimisedAverageUnitCost = BigDecimal.ZERO;

    public ZonedDateTime lastUpdatedTime = OctopusEnergyBindingConstants.UNDEFINED_TIME;

    public PriceOptimiserResult(ZonedDateTime requestedStartTime, ZonedDateTime requestedEndTime,
            Duration requestedDuration, ZonedDateTime optimisedStartTime, BigDecimal optimisedAverageUnitCost,
            ZonedDateTime lastUpdatedTime) {
        super();
        this.requestedStartTime = requestedStartTime;
        this.requestedEndTime = requestedEndTime;
        this.requestedDuration = requestedDuration;
        this.optimisedStartTime = optimisedStartTime;
        this.optimisedAverageUnitCost = optimisedAverageUnitCost;
        this.lastUpdatedTime = lastUpdatedTime;
    }

    @Override
    public String toString() {
        return "PriceOptimizerResult [requestedStartTime=" + requestedStartTime + ", requestedEndTime="
                + requestedEndTime + ", requestedDuration=" + requestedDuration + ", optimisedStartTime="
                + optimisedStartTime + ", optimisedAverageUnitCost=" + optimisedAverageUnitCost + ", lastUpdatedTime="
                + lastUpdatedTime + "]";
    }
}
