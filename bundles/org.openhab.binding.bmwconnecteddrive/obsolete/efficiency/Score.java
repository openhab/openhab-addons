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
 * The {@link Score} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Score {
    public final static String AVERAGE_ELECTRIC_CONSUMPTION = "AVERAGE_ELECTRIC_CONSUMPTION";
    public final static String AVERAGE_RECUPERATED_ENERGY_PER_100_KM = "AVERAGE_RECUPERATED_ENERGY_PER_100_KM";
    public final static String CUMULATED_ELECTRIC_DRIVEN_DISTANCE = "CUMULATED_ELECTRIC_DRIVEN_DISTANCE";
    public final static String LONGEST_DISTANCE_WITHOUT_CHARGING = "LONGEST_DISTANCE_WITHOUT_CHARGING";

    public String attrName;
    public String attrUnit;
    public float minValue;
    public float maxValue;
    public float lifeTime;
    // {
    // "attrName": "AVERAGE_ELECTRIC_CONSUMPTION",
    // "attrUnit": "KWH_PER_100KM",
    // "minValue": 0.0,
    // "maxValue": 40.0,
    // "lifeTime": 16.5
    // },
    // {
    // "attrName": "AVERAGE_RECUPERATED_ENERGY_PER_100_KM",
    // "attrUnit": "KWH_PER_100KM",
    // "minValue": 0.0,
    // "maxValue": 20.0,
    // "lifeTime": 4.5
    // },
    // {
    // "attrName": "CUMULATED_ELECTRIC_DRIVEN_DISTANCE",
    // "attrUnit": "KM",
    // "minValue": 0.0,
    // "maxValue": 16593.4,
    // "lifeTime": 16592.4
    // },
    // {
    // "attrName": "LONGEST_DISTANCE_WITHOUT_CHARGING",
    // "attrUnit": "KM",
    // "minValue": 0.0,
    // "maxValue": 270.0,
    // "lifeTime": 185.5
    // }],
}
