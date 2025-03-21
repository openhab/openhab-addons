/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An enumeration representing various device types along with their unique identifiers, acronyms, submodels, and thing
 * type IDs.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public enum DeviceTypes {
    AIR_CONDITIONER(401, "AC", "", "air-conditioner-401"),
    HEAT_PUMP(401, "AC", "AWHP", "heatpump-401HP"),
    WASHERDRYER_MACHINE(201, "WM", "", "washer-201"),
    WASHER_TOWER(221, "WM", "", "washer-tower-221"),
    DRYER(202, "DR", "Dryer", "dryer-202"),
    DRYER_TOWER(222, "DR", "Dryer", "dryer-tower-222"),
    FRIDGE(101, "REF", "Fridge", "fridge-101"),
    DISH_WASHER(204, "DW", "DishWasher", "dishwasher-204"),
    UNKNOWN(-1, "", "", "");

    private final int deviceTypeId;
    private final String deviceTypeAcron;
    private final String deviceSubModel;
    private final String thingTypeId;

    DeviceTypes(int i, String n, String submodel, String thingTypeId) {
        this.deviceTypeId = i;
        this.deviceTypeAcron = n;
        this.deviceSubModel = submodel;
        this.thingTypeId = thingTypeId;
    }

    /**
     * Returns the DeviceTypes enum based on the given device type ID and device code.
     *
     * @param deviceTypeId the device type ID to determine the corresponding DeviceTypes enum
     * @param deviceCode the code of the device
     * @return the DeviceTypes enum associated with the given device type ID and device code
     */
    public static DeviceTypes fromDeviceTypeId(int deviceTypeId, String deviceCode) {
        switch (deviceTypeId) {
            case 401:
                if ("AI05".equals(deviceCode)) {
                    return HEAT_PUMP;
                }
                return AIR_CONDITIONER;
            case 201:
                return WASHERDRYER_MACHINE;
            case 221:
                return WASHER_TOWER;
            case 202:
                return DRYER;
            case 204:
                return DISH_WASHER;
            case 222:
                return DRYER_TOWER;
            case 101:
                return FRIDGE;
            default:
                return UNKNOWN;
        }
    }

    /**
     * Converts the device type acronym and model type to a corresponding DeviceTypes enum value.
     *
     * @param deviceTypeAcron The device type acronym.
     * @param modelType The model type of the device.
     * @return The DeviceTypes enum value corresponding to the device type acronym and model type.
     */
    public static DeviceTypes fromDeviceTypeAcron(String deviceTypeAcron, String modelType) {
        return switch (deviceTypeAcron) {
            case "AC" -> {
                if ("AWHP".equals(modelType)) {
                    yield HEAT_PUMP;
                }
                yield AIR_CONDITIONER;
            }
            case "WM" -> {
                if ("Dryer".equals(modelType)) {
                    yield DRYER;
                }
                yield WASHERDRYER_MACHINE;
            }
            case "REF" -> FRIDGE;
            case "DW" -> DISH_WASHER;
            default -> UNKNOWN;
        };
    }

    public String deviceTypeAcron() {
        return deviceTypeAcron;
    }

    public int deviceTypeId() {
        return deviceTypeId;
    }

    public String deviceSubModel() {
        return deviceSubModel;
    }

    public String thingTypeId() {
        return thingTypeId;
    }
}
