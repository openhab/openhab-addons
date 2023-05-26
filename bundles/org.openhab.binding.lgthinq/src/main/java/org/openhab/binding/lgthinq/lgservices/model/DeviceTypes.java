/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

/**
 * The {@link DeviceTypes}
 *
 * @author Nemer Daud - Initial contribution
 */
public enum DeviceTypes {
    AIR_CONDITIONER(401, "AC", ""),

    HEAT_PUMP(401, "AC", "AWHP"),
    WASHERDRYER_MACHINE(201, "WM", ""),

    WASHING_TOWER(221, "WM", ""),
    DRYER(202, "DR", "Dryer"),
    DRYER_TOWER(222, "DR", "Dryer"),
    REFRIGERATOR(101, "REF", "Fridge"),
    UNKNOWN(-1, "", "");

    private final int deviceTypeId;
    private final String deviceTypeAcron;
    private final String deviceSubModel;

    public String deviceTypeAcron() {
        return deviceTypeAcron;
    }

    public int deviceTypeId() {
        return deviceTypeId;
    }

    public String deviceSubModel() {
        return deviceSubModel;
    }

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
                return WASHING_TOWER;
            case 202:
                return DRYER;
            case 222:
                return DRYER_TOWER;
            case 101:
                return REFRIGERATOR;
            default:
                return UNKNOWN;
        }
    }

    public static DeviceTypes fromDeviceTypeAcron(String deviceTypeAcron, String modelType) {
        switch (deviceTypeAcron) {
            case "AC":
                if ("AWHP".equals(modelType)) {
                    return HEAT_PUMP;
                }
                return AIR_CONDITIONER;
            case "WM":
                if ("Dryer".equals(modelType)) {
                    return DRYER;
                }
                return WASHERDRYER_MACHINE;
            case "REF":
                return REFRIGERATOR;
            default:
                return UNKNOWN;
        }
    }

    DeviceTypes(int i, String n, String submodel) {
        this.deviceTypeId = i;
        this.deviceTypeAcron = n;
        this.deviceSubModel = submodel;
    }
}
