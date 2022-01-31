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
 * The {@link AllTrips} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class AllTrips {
    public CommunityPowerEntry avgElectricConsumption;
    public CommunityPowerEntry avgRecuperation;
    public CommunityChargeCycleEntry chargecycleRange;
    public CommunityEletricDistanceEntry totalElectricDistance;
    public CommunityPowerEntry avgCombinedConsumption;
    public float savedCO2;// ":461.083,"
    public float savedCO2greenEnergy;// ":2712.255,"
    public float totalSavedFuel;// ":0,"
    public String resetDate;// ":"2020-08-24T14:40:40+0000","
    public int batterySizeMax;// ":33200
}
