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
package org.openhab.binding.bmwconnecteddrive.internal.dto.status;

import java.util.List;

/**
 * The {@link VehicleStatus} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class VehicleStatus {
    public int mileage;// ": 17273,
    public float remainingFuel;// ": 4,
    public float remainingRangeElectric;// ": 148,
    public float remainingRangeElectricMls;// ": 91,
    public float remainingRangeFuel;// ": 70,"
    public float remainingRangeFuelMls;// ":43,"
    public float maxRangeElectric;// ":216,"
    public float maxRangeElectricMls;// ":134,"
    public float maxFuel;// ":8.5,
    public float chargingLevelHv;// ":71,
    public String vin;// : "WBY1Z81040V905639",
    public String updateReason;// ": "VEHICLE_SHUTDOWN_SECURED",
    public String updateTime;// ": "2020-08-24 T15:55:32+0000",
    public String doorDriverFront;// ": "CLOSED",
    public String doorDriverRear;// ": "CLOSED",
    public String doorPassengerFront;// ": "CLOSED",
    public String doorPassengerRear;// ": "CLOSED",
    public String windowDriverFront;// ": "CLOSED",
    public String windowDriverRear;// ": "CLOSED",
    public String windowPassengerFront;// ": "CLOSED",
    public String windowPassengerRear;// ": "CLOSED",
    public String sunroof;// ": "CLOSED",
    public String trunk;// ": "CLOSED",
    public String rearWindow;// ": "INVALID",
    public String hood;// ": "CLOSED",
    public String doorLockState;// ": "SECURED",
    public String parkingLight;// ": "OFF",
    public String positionLight;// ": "ON",
    public String connectionStatus;// ": "DISCONNECTED",
    public String chargingStatus;// ": "INVALID","
    public String lastChargingEndReason;// ": "CHARGING_GOAL_REACHED",
    public String lastChargingEndResult;// ": "SUCCESS","
    public Position position;
    public String internalDataTimeUTC;// ": "2020-08-24 T15:55:32",
    public boolean singleImmediateCharging;// ":false,
    public String chargingConnectionType;// ": "CONDUCTIVE",
    public String chargingInductivePositioning;// ": "NOT_POSITIONED",
    public String vehicleCountry;// ": "DE","+"
    public String DCS_CCH_Activation;// ": "NA",
    public boolean DCS_CCH_Ongoing;// ":false
    public List<CCMMessage> checkControlMessages;// ":[],
    public List<CBSMessage> cbsData;
}
