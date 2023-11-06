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
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * Adds a STATE, VALUE and CALIBRATION datapoint to the HMW-IO-12-Sw14-DR device.
 * This device can change its metadata depending on the configuration. This virtual datapoint ensures, that always all
 * datapoints are available.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HmwIoModuleVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public void initialize(HmDevice device) {
        if (device.getType().startsWith(DEVICE_TYPE_WIRED_IO_MODULE)) {
            for (HmChannel channel : device.getChannels()) {
                if (channel.getNumber() >= 7) {
                    HmDatapointInfo dpInfoState = HmDatapointInfo.createValuesInfo(channel, DATAPOINT_NAME_STATE);
                    HmDatapointInfo dpInfoValue = HmDatapointInfo.createValuesInfo(channel, DATAPOINT_NAME_VALUE);
                    boolean hasStateDatapoint = channel.hasDatapoint(dpInfoState);
                    boolean hasValueDatapoint = channel.hasDatapoint(dpInfoValue);

                    if (hasStateDatapoint && !hasValueDatapoint) {
                        HmDatapoint dp = addDatapoint(channel.getDevice(), channel.getNumber(), DATAPOINT_NAME_VALUE,
                                HmValueType.FLOAT, 0.0, false);
                        dp.setMinValue(0.0);
                        dp.setMaxValue(1000.0);
                        dp.setVirtual(false);
                    } else if (hasValueDatapoint && !hasStateDatapoint) {
                        HmDatapoint dp = addDatapoint(channel.getDevice(), channel.getNumber(), DATAPOINT_NAME_STATE,
                                HmValueType.BOOL, false, false);
                        dp.setVirtual(false);
                    }
                }
                if (channel.getNumber() >= 21) {
                    HmDatapointInfo dpInfoCalibration = new HmDatapointInfo(HmParamsetType.MASTER, channel,
                            DATAPOINT_NAME_CALIBRATION);
                    if (!channel.hasDatapoint(dpInfoCalibration)) {
                        HmDatapoint dp = new HmDatapoint(DATAPOINT_NAME_CALIBRATION, DATAPOINT_NAME_CALIBRATION,
                                HmValueType.INTEGER, 0, false, HmParamsetType.MASTER);
                        dp.setMinValue(-127);
                        dp.setMaxValue(127);
                        addDatapoint(channel, dp);
                        dp.setVirtual(false);
                    }
                }
            }
        }
    }
}
