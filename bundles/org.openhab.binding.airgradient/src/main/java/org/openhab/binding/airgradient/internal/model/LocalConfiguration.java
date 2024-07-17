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

    @Nullable
    public String country; // ALPHA-2 Country code

    @Nullable
    public String pmStandard; // usaqi/ugm3

    @Nullable
    public String ledBarMode; // off, pm, co2

    @Nullable
    public Long abcDays; // Co2 calibration automatic baseline calibration days ( 0-200)

    @Nullable
    public Long tvocLearningOffset; // Time constant of long-term estimator for offset. Past events will be forgotten
                                    // after about twice the learning time. Range 1..1000 [hours]

    @Nullable
    public Long noxLearningOffset; // Time constant of long-term estimator for offset. Past events will be forgotten
                                   // after about twice the learning time. Range 1..1000 [hours]

    @Nullable
    public String mqttBrokerUrl;

    @Nullable
    public String temperatureUnit; // c/f

    @Nullable
    public String configurationControl; // local, cloud, both

    @Nullable
    public Boolean postDataToAirGradient;

    @Nullable
    public Long ledBarBrightness; // 0 - 100

    @Nullable
    public Long displayBrightness; // 0 - 100

    @Nullable
    public Boolean offlineMode; // Don't connect to wifi

    @Nullable
    public String model;

    @Nullable
    public Boolean co2CalibrationRequested; // TRIGGER: Calibration of Co2 sensor

    @Nullable
    public Boolean ledBarTestRequested; // TRIGGER: LEDs will run test sequence
}
