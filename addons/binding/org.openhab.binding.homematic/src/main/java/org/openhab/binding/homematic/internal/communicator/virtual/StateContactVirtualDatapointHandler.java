/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import org.openhab.binding.homematic.internal.misc.HomematicClientException;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_STATE_CONTACT;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandleEvent(HmDatapoint dp) {
        return isApplicable(dp.getChannel().getDevice()) && DATAPOINT_NAME_STATE.equals(dp.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(VirtualGateway gateway, HmDatapoint dp) throws HomematicClientException {
        Object value = convertState(dp.getValue());
        HmDatapoint vdp = getVirtualDatapoint(dp.getChannel());
        vdp.setValue(value);
    }

    private boolean isApplicable(HmDevice device) {
        return "HMIP-SWDO".equals(device.getType());
    }

    private Boolean convertState(Object value) {
        if (value == null) {
            return null;
        }
        if ("CLOSED".equals(value)) {
            return true;
        } else if ("OPEN".equals(value)) {
            return false;
        } else {
            return null;
        }
    }
}
