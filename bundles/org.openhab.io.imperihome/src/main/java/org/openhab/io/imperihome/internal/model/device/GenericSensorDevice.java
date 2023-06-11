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
package org.openhab.io.imperihome.internal.model.device;

import org.openhab.core.items.Item;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
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
