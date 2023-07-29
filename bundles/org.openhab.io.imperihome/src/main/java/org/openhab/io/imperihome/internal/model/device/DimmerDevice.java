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
 * Dimmer device, containing on/off status and dim level.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class DimmerDevice extends AbstractEnergyLinkDevice {

    public DimmerDevice(Item item) {
        super(DeviceType.DIMMER, item);
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        super.stateUpdated(item, newState);

        int level = 0;

        PercentType percentState = (PercentType) item.getStateAs(PercentType.class);
        if (percentState != null) {
            level = percentState.intValue();
        }

        addParam(new DeviceParam(ParamType.LEVEL, String.valueOf(level)));
        addParam(new DeviceParam(ParamType.STATUS, (level > 0) ^ isInverted() ? "1" : "0"));
    }
}
