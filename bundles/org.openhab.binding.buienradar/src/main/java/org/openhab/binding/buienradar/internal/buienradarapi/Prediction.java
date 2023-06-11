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
package org.openhab.binding.buienradar.internal.buienradarapi;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Prediction} interface contains a prediction of rain at a specific time.
 *
 * @author Edwin de Jong - Initial contribution
 */
@NonNullByDefault
public interface Prediction {
    /**
     * Intensity of rain in mm/hour
     */
    BigDecimal getIntensity();

    /**
     * Date-time of prediction.
     */
    ZonedDateTime getDateTimeOfPrediction();

    /**
     * Date-time of when the prediction was made.
     */
    ZonedDateTime getActualDateTime();
}
