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
package org.openhab.binding.tellstick.internal.local.dto;

import org.openhab.binding.tellstick.internal.TellstickRuntimeException;
import org.tellstick.device.TellstickSensorEvent;
import org.tellstick.device.iface.TellstickEvent;
import org.tellstick.enums.DataType;

/**
 * This class is used for events for the telldus live sensors.
 *
 * @author Jan Gustafsson - Initial contribution
 */
public class TellstickLocalSensorEventDTO extends TellstickSensorEvent implements TellstickEvent {

    private LocalDataTypeValueDTO dataType;

    public TellstickLocalSensorEventDTO(int sensorId, String data, LocalDataTypeValueDTO dataValue, String protocol,
            String model, long timeStamp) {
        super(sensorId, data, null, protocol, model, timeStamp);
        this.dataType = dataValue;
    }

    public LocalDataTypeValueDTO getDataTypeValue() {
        return dataType;
    }

    @Override
    public DataType getDataType() {
        throw new TellstickRuntimeException("Should not call this method");
    }
}
