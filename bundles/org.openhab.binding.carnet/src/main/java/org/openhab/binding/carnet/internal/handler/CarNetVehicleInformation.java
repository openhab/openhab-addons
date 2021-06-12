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
package org.openhab.binding.carnet.internal.handler;

import static org.openhab.binding.carnet.internal.CarUtils.getString;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNVehicleDetails.CarNetVehicleDetails;

/**
 * {@link CarNetVehicleInformation} define vehicle related information passed to the listener.
 *
 * @author Markus Michels - Initial Contribution
 */
@NonNullByDefault
public class CarNetVehicleInformation {
    public String vin = "";
    public String model = "";
    public String color;
    public String engine;
    public String mmi;
    public String transmission;

    public CarNetVehicleInformation(CarNetVehicleDetails vehicle) {
        this.vin = getString(vehicle.vin);
        if (vehicle.carportData.modelName != null) {
            this.model = getString(vehicle.carportData.modelYear) + " " + getString(vehicle.brand) + " "
                    + getString(vehicle.carportData.modelName) + " (" + getString(vehicle.carportData.countryCode) + "-"
                    + getString(vehicle.carportData.modelCode) + ")";
        } else {
            this.model = getString(vehicle.brand);
        }
        this.color = getString(vehicle.carportData.color);
        this.engine = getString(vehicle.carportData.engine);
        this.transmission = getString(vehicle.carportData.transmission);
        this.mmi = getString(vehicle.carportData.mmi);
    }

    public Map<String, String> getProperties() {
        Map<String, String> properties = new TreeMap<String, String>();
        return properties;
    }

    public String getId() {
        return vin;
    }
}
