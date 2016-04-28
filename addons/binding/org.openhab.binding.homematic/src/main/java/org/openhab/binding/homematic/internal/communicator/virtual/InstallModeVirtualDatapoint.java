/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.IOException;

import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * A virtual Switch datapoint to start and stop the install mode.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class InstallModeVirtualDatapoint extends AbstractVirtualDatapointHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(HmDevice device) {
        if (device.isGatewayExtras()) {
            addDatapoint(device, 0, VIRTUAL_DATAPOINT_NAME_INSTALL_MODE, HmValueType.BOOL, Boolean.FALSE, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(HmDatapoint dp, Object value) {
        return VIRTUAL_DATAPOINT_NAME_INSTALL_MODE.equals(dp.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException {
        dp.setValue(value);
        boolean enable = MiscUtils.isTrueValue(value);
        int duration = getDuration(dp.getChannel());
        if (enable) {
            gateway.disableDatapoint(dp, duration);
        }
        gateway.getRpcClient().setInstallMode(dp.getChannel().getDevice().getHmInterface(), enable, duration);
    }

    /**
     * Returns the virtual datapoint value for install mode duration.
     */
    private Integer getDuration(HmChannel channel) {
        HmDatapoint dpDuration = channel
                .getDatapoint(HmDatapointInfo.createValuesInfo(channel, VIRTUAL_DATAPOINT_NAME_INSTALL_MODE_DURATION));
        return dpDuration == null || dpDuration.getValue() == null || dpDuration.getType() != HmValueType.INTEGER ? 60
                : ((Number) dpDuration.getValue()).intValue();
    }

}
