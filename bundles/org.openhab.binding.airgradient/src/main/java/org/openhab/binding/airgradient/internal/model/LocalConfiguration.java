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
package org.openhab.binding.airgradient.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Data model class for configuration from a local sensor.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class LocalConfiguration {

    public @Nullable String country; // ALPHA-2 Country code

    public @Nullable String pmStandard; // usaqi/ugm3

    public @Nullable String ledBarMode; // off, pm, co2

    public @Nullable Long abcDays; // Co2 calibration automatic baseline calibration days ( 0-200)

    public @Nullable Long tvocLearningOffset; // Time constant of long-term estimator for offset. Past events will be
                                              // forgotten
    // after about twice the learning time. Range 1..1000 [hours]

    public @Nullable Long noxLearningOffset; // Time constant of long-term estimator for offset. Past events will be
                                             // forgotten
    // after about twice the learning time. Range 1..1000 [hours]

    public @Nullable String mqttBrokerUrl;

    public @Nullable String temperatureUnit; // c/f

    public @Nullable String configurationControl; // local, cloud, both

    public @Nullable Boolean postDataToAirGradient;

    public @Nullable Long ledBarBrightness; // 0 - 100

    public @Nullable Long displayBrightness; // 0 - 100

    public @Nullable Boolean offlineMode; // Don't connect to wifi

    public @Nullable String model;

    public @Nullable Boolean co2CalibrationRequested; // TRIGGER: Calibration of Co2 sensor

    public @Nullable Boolean ledBarTestRequested; // TRIGGER: LEDs will run test sequence
}
