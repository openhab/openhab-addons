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
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.io.imperihome.internal.model.param.DeviceParam;
import org.openhab.io.imperihome.internal.model.param.ParamType;

/**
 * Simple on/off switch device.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class SwitchDevice extends AbstractEnergyLinkDevice {

    public SwitchDevice(Item item) {
        super(DeviceType.SWITCH, item);
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        super.stateUpdated(item, newState);

        if (getParams().get(ParamType.STATUS) == null) {
            PercentType percentState = (PercentType) item.getStateAs(PercentType.class);
            if (percentState != null) {
                boolean value = percentState.intValue() > 0;
                DeviceParam param = new DeviceParam(ParamType.STATUS, value ^ isInverted() ? "1" : "0");
                addParam(param);
            }
        }
    }
}
