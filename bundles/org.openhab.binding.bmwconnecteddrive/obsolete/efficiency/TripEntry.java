/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto.efficiency;

/**
 * The {@link TripEntry} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class TripEntry {
    public final static String LASTTRIP_DELTA_KM = "LASTTRIP_DELTA_KM";
    public final static String ACTUAL_DISTANCE_WITHOUT_CHARGING = "ACTUAL_DISTANCE_WITHOUT_CHARGING";
    public final static String AVERAGE_ELECTRIC_CONSUMPTION = "AVERAGE_ELECTRIC_CONSUMPTION";
    public final static String AVERAGE_RECUPERATED_ENERGY_PER_100_KM = "AVERAGE_RECUPERATED_ENERGY_PER_100_KM";
    public final static String CUMULATED_ELECTRIC_DRIVEN_DISTANCE = "CUMULATED_ELECTRIC_DRIVEN_DISTANCE";

    public String name;
    public String unit;
    public float lastTrip;
    // "lastTripList": [
    // {
    // "name": "LASTTRIP_DELTA_KM",
    // "unit": "KM",
    // "lastTrip": "2.0"
    // },
    // {
    // "name": "ACTUAL_DISTANCE_WITHOUT_CHARGING",
    // "unit": "KM",
    // "lastTrip": "31.0"
    // },
    // {
    // "name": "AVERAGE_ELECTRIC_CONSUMPTION",
    // "unit": "KWH_PER_100KM",
    // "lastTrip": "14.5"
    // },
    // {
    // "name": "AVERAGE_RECUPERATED_ENERGY_PER_100_KM",
    // "unit": "KWH_PER_100KM",
    // "lastTrip": "8.0"
    // },
    // {
    // "name": "CUMULATED_ELECTRIC_DRIVEN_DISTANCE",
    // "unit": "KM",
    // "lastTrip": "16592.4"
    // }
    // ],
}
