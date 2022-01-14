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
package org.openhab.binding.bmwconnecteddrive.internal.dto.status;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VehicleStatus} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class VehicleStatus {
    public int mileage = Constants.INT_UNDEF;// ": 17273,
    public double remainingFuel = Constants.INT_UNDEF;// ": 4,
    public double remainingRangeElectric = Constants.INT_UNDEF;// ": 148,
    public double remainingRangeElectricMls;// ": 91,
    public double remainingRangeFuel = Constants.INT_UNDEF;// ": 70,"
    public double remainingRangeFuelMls;// ":43,"
    public double maxRangeElectric = Constants.INT_UNDEF;// ":216,"
    public double maxRangeElectricMls;// ":134,"
    public double maxFuel;// ":8.5,
    public double chargingLevelHv;// ":71,
    public String vin;// : "ANONYMOUS",
    public String updateReason;// ": "VEHICLE_SHUTDOWN_SECURED",
    public String updateTime;// ": "2020-08-24 T15:55:32+0000",
    public String doorDriverFront = Constants.UNDEF;// ": "CLOSED",
    public String doorDriverRear = Constants.UNDEF;// ": "CLOSED",
    public String doorPassengerFront = Constants.UNDEF;// ": "CLOSED",
    public String doorPassengerRear = Constants.UNDEF;// ": "CLOSED",
    public String windowDriverFront = Constants.UNDEF;// ": "CLOSED",
    public String windowDriverRear = Constants.UNDEF;// ": "CLOSED",
    public String windowPassengerFront = Constants.UNDEF;// ": "CLOSED",
    public String windowPassengerRear = Constants.UNDEF;// ": "CLOSED",
    public String sunroof = Constants.UNDEF;// ": "CLOSED",
    public String trunk = Constants.UNDEF;// ": "CLOSED",
    public String rearWindow = Constants.UNDEF;// ": "INVALID",
    public String hood = Constants.UNDEF;// ": "CLOSED",
    public String doorLockState;// ": "SECURED",
    public String parkingLight;// ": "OFF",
    public String positionLight;// ": "ON",
    public String connectionStatus;// ": "DISCONNECTED",
    public String chargingStatus;// ": "INVALID","
    public String lastChargingEndReason;// ": "CHARGING_GOAL_REACHED",
    public String lastChargingEndResult;// ": "SUCCESS","
    public Double chargingTimeRemaining;// ": "45",
    public Position position;
    public String internalDataTimeUTC;// ": "2020-08-24 T15:55:32",
    public boolean singleImmediateCharging;// ":false,
    public String chargingConnectionType;// ": "CONDUCTIVE",
    public String chargingInductivePositioning;// ": "NOT_POSITIONED",
    public String vehicleCountry;// ": "DE","+"
    @SerializedName("DCS_CCH_Activation")
    public String dcsCchActivation;// ": "NA",
    @SerializedName("DCS_CCH_Ongoing")
    public boolean dcsCchOngoing;// ":false
    public List<CCMMessage> checkControlMessages = new ArrayList<CCMMessage>();// ":[],
    public List<CBSMessage> cbsData = new ArrayList<CBSMessage>();
}
