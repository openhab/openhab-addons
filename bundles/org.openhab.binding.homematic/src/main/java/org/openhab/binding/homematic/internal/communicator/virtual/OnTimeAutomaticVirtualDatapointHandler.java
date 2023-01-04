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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A virtual Number datapoint which adds an automatic ON_TIME datapoint on supported device. This datapoint sets the
 * ON_TIME datapoint every time a STATE or LEVEL datapoint is set, so that the light turns off automatically by the
 * device after the specified time.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class OnTimeAutomaticVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    private final Logger logger = LoggerFactory.getLogger(OnTimeAutomaticVirtualDatapointHandler.class);

    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_ON_TIME_AUTOMATIC;
    }

    @Override
    public void initialize(HmDevice device) {
        for (HmChannel channel : device.getChannels()) {
            HmDatapointInfo dpInfoOnTime = HmDatapointInfo.createValuesInfo(channel, DATAPOINT_NAME_ON_TIME);
            if (channel.hasDatapoint(dpInfoOnTime)) {
                HmDatapointInfo dpInfoLevel = HmDatapointInfo.createValuesInfo(channel, DATAPOINT_NAME_LEVEL);
                HmDatapointInfo dpInfoState = HmDatapointInfo.createValuesInfo(channel, DATAPOINT_NAME_STATE);
                if (channel.hasDatapoint(dpInfoLevel) || channel.hasDatapoint(dpInfoState)) {
                    HmDatapoint dpOnTime = channel.getDatapoint(dpInfoOnTime);
                    HmDatapoint dpOnTimeAutomatic = dpOnTime.clone();
                    dpOnTimeAutomatic.setName(getName());
                    dpOnTimeAutomatic.setDescription(getName());
                    addDatapoint(channel, dpOnTimeAutomatic);
                }
            }
        }
    }

    @Override
    public boolean canHandleCommand(HmDatapoint dp, Object value) {
        boolean isLevel = DATAPOINT_NAME_LEVEL.equals(dp.getName()) && value != null && value instanceof Number
                && ((Number) value).doubleValue() > 0.0;
        boolean isState = DATAPOINT_NAME_STATE.equals(dp.getName()) && MiscUtils.isTrueValue(value);

        return ((isLevel || isState) && getVirtualDatapointValue(dp.getChannel()) > 0.0)
                || getName().equals(dp.getName());
    }

    @Override
    public void handleCommand(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException {
        if (!getName().equals(dp.getName())) {
            HmChannel channel = dp.getChannel();
            HmDatapoint dpOnTime = channel
                    .getDatapoint(HmDatapointInfo.createValuesInfo(channel, DATAPOINT_NAME_ON_TIME));
            if (dpOnTime != null) {
                gateway.sendDatapoint(dpOnTime, new HmDatapointConfig(), getVirtualDatapointValue(channel), null);
            } else {
                logger.warn(
                        "Can't find ON_TIME datapoint in channel '{}' in device '{}', ignoring virtual datapoint '{}'",
                        channel.getNumber(), channel.getDevice().getAddress(), getName());
            }
            gateway.sendDatapointIgnoreVirtual(dp, dpConfig, value);
        } else {
            dp.setValue(value);
        }
    }

    /**
     * Returns the virtual datapoint value or 0 if not specified.
     */
    private Double getVirtualDatapointValue(HmChannel channel) {
        HmDatapoint dpOnTimeAutomatic = getVirtualDatapoint(channel);
        return dpOnTimeAutomatic == null || dpOnTimeAutomatic.getValue() == null
                || dpOnTimeAutomatic.getType() != HmValueType.FLOAT ? 0.0
                        : ((Number) dpOnTimeAutomatic.getValue()).doubleValue();
    }
}
