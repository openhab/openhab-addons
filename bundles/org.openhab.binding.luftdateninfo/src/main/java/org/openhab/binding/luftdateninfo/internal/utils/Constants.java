/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.luftdateninfo.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Constants} Constants used in this binding
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Constants {
    public static final String EMPTY = "";
    public static final String P1 = "P1";
    public static final String P2 = "P2";

    public static final String TEMPERATURE = "temperature";
    public static final String HUMIDITY = "humidity";
    public static final String PRESSURE = "pressure";
    public static final String PRESSURE_SEALEVEL = "pressure_at_sealevel";

    public static final String NOISE_EQ = "noise_LAeq";
    public static final String NOISE_MIN = "noise_LA_min";
    public static final String NOISE_MAX = "noise_LA_max";
    public static final int UNDEF = -1;
}
