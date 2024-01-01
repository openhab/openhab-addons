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
package org.openhab.binding.mybmw.internal.dto.vehicle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.handler.backend.JsonStringDeserializer;
import org.openhab.binding.mybmw.internal.util.FileReader;

/**
 * 
 * checks the transformation of the capabilities to string lists
 * 
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactoring
 */
public class VehicleCapabilitiesTest {
    @Test
    void testGetCapabilitiesAsString() {
        String content = FileReader.fileToString("responses/BEV/vehicles_state.json");
        VehicleStateContainer vehicleStateContainer = JsonStringDeserializer.getVehicleState(content);

        String servicesSupportedReference = "BmwCharging;ChargingHistory;ChargingPlan;CustomerEsim;DCSContractManagement;RemoteHistory;ScanAndCharge";
        String servicesUnsupportedReference = "CarSharing;ChargeNowForBusiness;ClimateTimer;EvGoCharging;MiniCharging;RemoteEngineStart;RemoteHistoryDeletion;RemoteParking;Sustainability;WifiHotspotService";
        String servicesEnabledReference = "ChargingHospitality;ChargingLoudness;ChargingPowerLimit;ChargingSettings;ChargingTargetSoc";
        String servicesDisabledReference = "DataPrivacy;EasyCharge;NonLscFeature;SustainabilityAccumulatedView";
        assertEquals(servicesSupportedReference, vehicleStateContainer.getCapabilities()
                .getCapabilitiesAsString(VehicleCapabilities.SUPPORTED_SUFFIX, true), "Services supported");
        assertEquals(servicesUnsupportedReference, vehicleStateContainer.getCapabilities()
                .getCapabilitiesAsString(VehicleCapabilities.SUPPORTED_SUFFIX, false), "Services unsupported");
        assertEquals(servicesEnabledReference, vehicleStateContainer.getCapabilities()
                .getCapabilitiesAsString(VehicleCapabilities.ENABLED_SUFFIX, true), "Services enabled");
        assertEquals(servicesDisabledReference, vehicleStateContainer.getCapabilities()
                .getCapabilitiesAsString(VehicleCapabilities.ENABLED_SUFFIX, false), "Services disabled");
    }
}
