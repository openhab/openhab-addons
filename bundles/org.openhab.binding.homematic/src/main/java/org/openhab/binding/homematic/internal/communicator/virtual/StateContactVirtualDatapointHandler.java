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
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * A virtual datapoint that converts the ENUM state of the HMIP-SWDO device to a contact.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class StateContactVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_STATE_CONTACT;
    }

    @Override
    public void initialize(HmDevice device) {
        if (isApplicable(device)) {
            HmChannel channelOne = device.getChannel(1);
            if (channelOne != null) {
                HmDatapointInfo dpStateInfo = HmDatapointInfo.createValuesInfo(channelOne, DATAPOINT_NAME_STATE);
                HmDatapoint dpState = channelOne.getDatapoint(dpStateInfo);
                if (dpState != null) {
                    addDatapoint(device, 1, getName(), HmValueType.BOOL, convertState(dpState.getValue()), true);
                }
            }
        }
    }

    @Override
    public boolean canHandleEvent(HmDatapoint dp) {
        return isApplicable(dp.getChannel().getDevice()) && DATAPOINT_NAME_STATE.equals(dp.getName());
    }

    @Override
    public void handleEvent(VirtualGateway gateway, HmDatapoint dp) {
        Object value = convertState(dp.getValue());
        HmDatapoint vdp = getVirtualDatapoint(dp.getChannel());
        vdp.setValue(value);
    }

    private boolean isApplicable(HmDevice device) {
        return device.getType().toUpperCase().startsWith("HMIP-SWDO");
    }

    private Boolean convertState(Object value) {
        if (!(value instanceof Integer)) {
            return null;
        }
        if ((int) value == 0) {
            return true;
        } else if ((int) value == 1) {
            return false;
        } else {
            return null;
        }
    }
}
