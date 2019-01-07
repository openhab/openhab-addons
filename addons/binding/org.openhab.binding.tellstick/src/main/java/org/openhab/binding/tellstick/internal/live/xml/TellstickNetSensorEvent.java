/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
