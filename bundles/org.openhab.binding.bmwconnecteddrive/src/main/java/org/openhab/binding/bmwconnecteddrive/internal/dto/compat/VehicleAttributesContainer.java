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
package org.openhab.binding.bmwconnecteddrive.internal.dto.compat;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.bmwconnecteddrive.internal.dto.status.CBSMessage;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.CCMMessage;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.Position;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.status.VehicleStatusContainer;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;

/**
 * The {@link VehicleAttributesContainer} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class VehicleAttributesContainer {
    public VehicleAttributes attributesMap;
    public VehicleMessages vehicleMessages;

    public String transform() {
        // create target objects
        VehicleStatusContainer vsc = new VehicleStatusContainer();
        VehicleStatus vs = new VehicleStatus();
        vsc.vehicleStatus = vs;

        vs.mileage = attributesMap.mileage;
        vs.doorLockState = attributesMap.doorLockState;

        vs.doorDriverFront = attributesMap.doorDriverFront;
        vs.doorDriverRear = attributesMap.doorDriverRear;
        vs.doorPassengerFront = attributesMap.doorPassengerFront;
        vs.doorPassengerRear = attributesMap.doorPassengerRear;
        vs.hood = attributesMap.hoodState;
        vs.trunk = attributesMap.trunkState;

        vs.windowDriverFront = attributesMap.winDriverFront;
        vs.windowDriverRear = attributesMap.winDriverRear;
        vs.windowPassengerFront = attributesMap.winPassengerFront;
        vs.windowPassengerRear = attributesMap.winPassengerRear;
        vs.sunroof = attributesMap.sunroofState;

        vs.remainingFuel = attributesMap.remainingFuel;
        vs.remainingRangeElectric = attributesMap.beRemainingRangeElectricKm;
        vs.remainingRangeElectricMls = attributesMap.beRemainingRangeElectricMile;
        vs.remainingRangeFuel = attributesMap.beRemainingRangeFuelKm;
        vs.remainingRangeFuelMls = attributesMap.beRemainingRangeFuelMile;
        vs.remainingFuel = attributesMap.remainingFuel;
        vs.chargingLevelHv = attributesMap.chargingLevelHv;
        vs.chargingStatus = attributesMap.chargingHVStatus;
        vs.lastChargingEndReason = attributesMap.lastChargingEndReason;

        vs.updateTime = attributesMap.updateTimeConverted;
        // vs.internalDataTimeUTC = attributesMap.updateTime;

        Position p = new Position();
        p.lat = attributesMap.gpsLat;
        p.lon = attributesMap.gpsLon;
        p.heading = attributesMap.heading;
        vs.position = p;

        final List<CCMMessage> ccml = new ArrayList<CCMMessage>();
        if (vehicleMessages != null) {
            if (vehicleMessages.ccmMessages != null) {
                vehicleMessages.ccmMessages.forEach(entry -> {
                    CCMMessage ccmM = new CCMMessage();
                    ccmM.ccmDescriptionShort = entry.text;
                    ccmM.ccmMileage = entry.unitOfLengthRemaining;
                    ccml.add(ccmM);
                });
            }
        }
        vs.checkControlMessages = ccml;

        final List<CBSMessage> cbsl = new ArrayList<CBSMessage>();
        if (vehicleMessages != null) {
            if (vehicleMessages.cbsMessages != null) {
                vehicleMessages.cbsMessages.forEach(entry -> {
                    CBSMessage cbsm = new CBSMessage();
                    cbsm.cbsType = entry.text;
                    cbsm.cbsDueDate = entry.date;
                    cbsm.cbsRemainingMileage = entry.unitOfLengthRemaining;
                    cbsl.add(cbsm);
                });
            }
        }
        vs.cbsData = cbsl;

        return Converter.getGson().toJson(vsc);
    }
}
