/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.model.device;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.imperihome.internal.model.param.DeviceParam;
import org.openhab.io.imperihome.internal.model.param.NumericValueParam;
import org.openhab.io.imperihome.internal.model.param.ParamType;

/**
 * Generic sensor device.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class GenericSensorDevice extends AbstractNumericValueDevice {

    public GenericSensorDevice(Item item) {
        super(DeviceType.GENERIC_SENSOR, item, null);
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        super.stateUpdated(item, newState);

        DecimalType value = (DecimalType) item.getStateAs(DecimalType.class);
        if (value != null) {
            addParam(new NumericValueParam(ParamType.GENERIC_VALUE, getUnit(), value));
        } else {
            State state = item.getState();
            String strVal = (state == null) ? null : state.toFullString();
            addParam(new DeviceParam(ParamType.GENERIC_VALUE, strVal));
        }
    }

}
