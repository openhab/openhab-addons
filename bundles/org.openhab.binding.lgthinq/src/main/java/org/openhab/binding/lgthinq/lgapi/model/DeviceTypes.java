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
package org.openhab.binding.lgthinq.lgapi.model;

/**
 * The {@link DeviceTypes}
 *
 * @author Nemer Daud - Initial contribution
 */
public enum DeviceTypes {
    AIR_CONDITIONER(401),
    UNKNOWN(-1);

    private final int deviceTypeId;

    public int deviceTypeId() {
        return deviceTypeId;
    }

    public static DeviceTypes fromDeviceTypeId(int deviceTypeId) {
        switch (deviceTypeId) {
            case 401:
                return AIR_CONDITIONER;
            default:
                return UNKNOWN;
        }
    }

    DeviceTypes(int i) {
        this.deviceTypeId = i;
    }
}
