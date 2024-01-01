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
package org.openhab.binding.tellstick.internal.live.xml;

import org.openhab.binding.tellstick.internal.TellstickRuntimeException;
import org.tellstick.device.TellstickSensorEvent;
import org.tellstick.device.iface.TellstickEvent;
import org.tellstick.enums.DataType;

/**
 * This class is used for events for the telldus live sensors.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class TellstickNetSensorEvent extends TellstickSensorEvent implements TellstickEvent {

    private DataTypeValue dataType;

    public TellstickNetSensorEvent(int sensorId, String data, DataTypeValue dataValue, String protocol, String model,
            long timeStamp) {
        super(sensorId, data, null, protocol, model, timeStamp);
        this.dataType = dataValue;
    }

    public DataTypeValue getDataTypeValue() {
        return dataType;
    }

    @Override
    public DataType getDataType() {
        throw new TellstickRuntimeException("Should not call this method");
    }
}
