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
package org.openhab.binding.energidataservice.internal.provider.listener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.energidataservice.internal.provider.subscription.Co2EmissionSubscription;

/**
 * {@link Co2EmissionListener} provides an interface for receiving
 * CO2 emission data.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public interface Co2EmissionListener extends SubscriptionListener {
    /**
     * Current realtime emission or prognosis has been updated.
     *
     * @param type The type (either {@link Co2EmissionSubscription.Type#Realtime} or
     *            {@link Co2EmissionSubscription.Type#Prognosis})
     * @param emission Emission in g/kWh
     */
    void onCurrentEmission(Co2EmissionSubscription.Type type, BigDecimal emission);

    /**
     * Realtime emissions or prognosis have changed.
     * Can be used to update time series.
     *
     * @param type The type (either {@link Co2EmissionSubscription.Type#Realtime} or
     *            {@link Co2EmissionSubscription.Type#Prognosis})
     * @param emissions Emissions in g/kWh
     */
    void onEmissions(Co2EmissionSubscription.Type type, Map<Instant, BigDecimal> emissions);
}
