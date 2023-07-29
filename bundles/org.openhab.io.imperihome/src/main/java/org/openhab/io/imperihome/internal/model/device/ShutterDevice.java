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
package org.openhab.io.imperihome.internal.model.device;

import org.openhab.core.items.Item;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.io.imperihome.internal.model.param.DeviceParam;
import org.openhab.io.imperihome.internal.model.param.ParamType;

/**
 * Shutter device, containing level. Stoppable and pulseable attributes currently hardcoded to 0 (false).
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class ShutterDevice extends AbstractEnergyLinkDevice {

    public ShutterDevice(Item item) {
        super(DeviceType.SHUTTER, item);
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        super.stateUpdated(item, newState);

        int level = 0;

        PercentType percentState = (PercentType) item.getStateAs(PercentType.class);
        if (percentState != null) {
            level = percentState.intValue();
        }

        addParam(new DeviceParam(ParamType.PULSEABLE, "0"));
        addParam(new DeviceParam(ParamType.STOPPABLE, getLinks().containsKey("stopper") ? "1" : "0"));
        addParam(new DeviceParam(ParamType.LEVEL, String.valueOf(level)));
    }
}
