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
package org.openhab.binding.bmwconnecteddrive.internal.dto.statistics;

/**
 * The {@link LastTrip} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class LastTrip {
    public float efficiencyValue;// ": 0.98,
    public float totalDistance;// ": 2,
    public float electricDistance;// ": 2,
    public float avgElectricConsumption;// ": 7,
    public float avgRecuperation;// ": 6,
    public float drivingModeValue;// ": 0.87,
    public float accelerationValue;// ": 0.99,
    public float anticipationValue;// ": 0.99,
    public float totalConsumptionValue;// ": 1.25,
    public float auxiliaryConsumptionValue;// ": 0.78,
    public float avgCombinedConsumption;// ": 0,
    public float electricDistanceRatio;// ": 100,
    public float savedFuel;// ": 0,
    public String date;// ": "2020-08-24T17:55:00+0000",
    public float duration;// ": 5
}
