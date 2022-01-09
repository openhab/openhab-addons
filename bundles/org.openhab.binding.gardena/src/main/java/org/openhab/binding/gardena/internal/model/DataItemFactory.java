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
package org.openhab.binding.gardena.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.dto.api.CommonServiceDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.DataItem;
import org.openhab.binding.gardena.internal.model.dto.api.DeviceDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.LocationDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.MowerServiceDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.PowerSocketServiceDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.SensorServiceDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.ValveServiceDataItem;
import org.openhab.binding.gardena.internal.model.dto.api.ValveSetServiceDataItem;

/**
 * Creates the dataItem object based on the device type.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class DataItemFactory {
    public static Class<? extends DataItem<?>> create(String type) throws GardenaException {
        switch (type) {
            case "LOCATION":
                return LocationDataItem.class;
            case "DEVICE":
                return DeviceDataItem.class;
            case "COMMON":
                return CommonServiceDataItem.class;
            case "MOWER":
                return MowerServiceDataItem.class;
            case "POWER_SOCKET":
                return PowerSocketServiceDataItem.class;
            case "VALVE":
                return ValveServiceDataItem.class;
            case "VALVE_SET":
                return ValveSetServiceDataItem.class;
            case "SENSOR":
                return SensorServiceDataItem.class;
            default:
                throw new GardenaException("Unknown DataItem type: " + type);
        }
    }
}
