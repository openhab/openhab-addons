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
package org.openhab.binding.volvooncall.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VehicleConfiguration} is the class used to match the
 * Vehicle thing configuration.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VehicleConfiguration {
    public static String VIN = "vin";

    public String vin = "";
    public int refresh = 10;
}
