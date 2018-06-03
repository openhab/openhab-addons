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
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.imperihome.internal.model.param.DeviceParam;
import org.openhab.io.imperihome.internal.model.param.ParamType;

/**
 * Lock device.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class LockDevice extends AbstractDevice {

    public LockDevice(Item item) {
        super(DeviceType.LOCK, item);
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        super.stateUpdated(item, newState);

        if (getParams().get(ParamType.STATUS) == null) {
            OpenClosedType openClosedState = (OpenClosedType) item.getStateAs(OpenClosedType.class);
            if (openClosedState != null) {
                boolean value = openClosedState == OpenClosedType.OPEN;
                DeviceParam param = new DeviceParam(ParamType.STATUS, value ^ isInverted() ? "1" : "0");
                addParam(param);
            }
        }
    }

}
