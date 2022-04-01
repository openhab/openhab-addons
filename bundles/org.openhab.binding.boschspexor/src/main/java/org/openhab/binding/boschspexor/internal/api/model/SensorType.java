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
package org.openhab.binding.boschspexor.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Sensor Types
 *
 * @author Marc Fischer - Initial contribution *
 */
@NonNullByDefault
public enum SensorType {
    AirQuality,
    AirQualityLevel,
    Temperature,
    Pressure,
    Acceleration,
    Light,
    Gas,
    Humidity,
    Microphone,
    PassiveInfrared,
    Fire
}
