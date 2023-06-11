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

import java.io.IOException;

import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * A virtual Switch datapoint to start and stop the install mode.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class InstallModeVirtualDatapoint extends AbstractVirtualDatapointHandler {
    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_INSTALL_MODE;
    }

    @Override
    public void initialize(HmDevice device) {
        if (device.isGatewayExtras()) {
            addDatapoint(device, 0, getName(), HmValueType.BOOL, Boolean.FALSE, false);
        }
    }

    @Override
    public boolean canHandleCommand(HmDatapoint dp, Object value) {
        return getName().equals(dp.getName());
    }

    @Override
    public void handleCommand(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException {
        dp.setValue(value);
        boolean enable = MiscUtils.isTrueValue(value);
        int duration = getDuration(dp.getChannel());
        if (enable) {
            gateway.disableDatapoint(dp, duration);
        }
        HmInterface hmInterface = dp.getChannel().getDevice().getHmInterface();
        gateway.getRpcClient(hmInterface).setInstallMode(hmInterface, enable, duration);
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
