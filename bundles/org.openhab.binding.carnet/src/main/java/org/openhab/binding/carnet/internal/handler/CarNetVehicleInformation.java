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

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetVehicleDetails;

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
        this.vin = vehicle.carportData.vin;
        this.model = vehicle.carportData.modelYear + " " + vehicle.carportData.brand + " "
                + vehicle.carportData.modelName + " (" + vehicle.carportData.countryCode + "-"
                + vehicle.carportData.modelCode + ")";
        this.color = vehicle.carportData.color;
        this.engine = vehicle.carportData.engine;
        this.transmission = vehicle.carportData.transmission;
        this.mmi = vehicle.carportData.mmi;
    }

    public Map<String, String> getProperties() {
        Map<String, String> properties = new TreeMap<String, String>();
        return properties;
    }

    public String getId() {
        return vin;
    }
}
