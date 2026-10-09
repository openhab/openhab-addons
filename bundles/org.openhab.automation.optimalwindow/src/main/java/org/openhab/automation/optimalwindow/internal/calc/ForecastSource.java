/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.automation.optimalwindow.internal.calc;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.SortedMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Provides the forecast values of an item.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public interface ForecastSource {
    /**
     * Get the forecast values of an item between two points in time.
     *
     * @param itemName the name of the item
     * @param serviceId the persistence service to use, or {@code null} for the default service
     * @param begin the begin of the period
     * @param end the end of the period
     *
     * @return the values by their timestamp
     * 
     * @throws IllegalStateException if the values cannot be retrieved
     */
    SortedMap<Instant, Double> getValues(String itemName, @Nullable String serviceId, ZonedDateTime begin,
            ZonedDateTime end) throws IllegalStateException;
}
