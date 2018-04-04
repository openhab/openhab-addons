/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.model.device;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.imperihome.internal.model.param.DeviceParam;
import org.openhab.io.imperihome.internal.model.param.ParamType;

/**
 * Abstraction of devices that are trippable, i.e. DevDoor, DevFlood, DevMotion, DevSmoke, DevCO2Alert.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class TrippableDevice extends AbstractDevice {

    public TrippableDevice(DeviceType type, Item item) {
        super(type, item);

        addParam(new DeviceParam(ParamType.ARMABLE, "0"));
        addParam(new DeviceParam(ParamType.ARMED, "1"));
        addParam(new DeviceParam(ParamType.ACKABLE, "0"));
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        super.stateUpdated(item, newState);

        boolean tripped = false;

        if (item.getStateAs(OpenClosedType.class) != null) {
            OpenClosedType state = (OpenClosedType) item.getStateAs(OpenClosedType.class);
            tripped = state == OpenClosedType.CLOSED;
        } else if (item.getStateAs(OnOffType.class) != null) {
            OnOffType state = (OnOffType) item.getStateAs(OnOffType.class);
            tripped = state == OnOffType.ON;
        } else if (item.getStateAs(DecimalType.class) != null) {
            DecimalType state = (DecimalType) item.getStateAs(DecimalType.class);
            tripped = state.intValue() != 0;
        } else if (item.getStateAs(StringType.class) != null) {
            StringType state = (StringType) item.getStateAs(StringType.class);
            tripped = StringUtils.isNotBlank(state.toString()) && !state.toString().trim().equals("ok");
        } else {
            logger.debug("Can't interpret state {} as tripped status", item.getState());
        }

        addParam(new DeviceParam(ParamType.TRIPPED, tripped ^ isInverted() ? "1" : "0"));

        if (tripped) {
            addParam(new DeviceParam(ParamType.LAST_TRIP, String.valueOf(System.currentTimeMillis())));
        }
    }

}
