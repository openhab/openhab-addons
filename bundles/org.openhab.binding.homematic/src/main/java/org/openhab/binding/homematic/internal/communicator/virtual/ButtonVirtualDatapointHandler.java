/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.VIRTUAL_DATAPOINT_NAME_BUTTON;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A virtual String datapoint which adds a BUTTON datapoint. It will forward key events to the
 * system channel {@link DefaultSystemChannelTypeProvider#SYSTEM_BUTTON}.
 *
 * @author Michael Reitler - Initial contribution
 */
public class ButtonVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    private final Logger logger = LoggerFactory.getLogger(ButtonVirtualDatapointHandler.class);

    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_BUTTON;
    }

    @Override
    public void initialize(HmDevice device) {
        for (HmChannel channel : device.getChannels()) {
            if (channel.hasPressDatapoint()) {
                HmDatapoint dp = addDatapoint(device, channel.getNumber(), getName(), HmValueType.STRING, null, false);
                dp.setTrigger(true);
                dp.setOptions(new String[] { CommonTriggerEvents.SHORT_PRESSED, CommonTriggerEvents.LONG_PRESSED,
                        CommonTriggerEvents.DOUBLE_PRESSED });
            }
        }
    }

    @Override
    public boolean canHandleEvent(HmDatapoint dp) {
        return dp.isPressDatapoint();
    }

    @Override
    public void handleEvent(VirtualGateway gateway, HmDatapoint dp) {
        HmDatapoint vdp = getVirtualDatapoint(dp.getChannel());
        if (MiscUtils.isTrueValue(dp.getValue())) {
            String pressType = StringUtils.substringAfter(dp.getName(), "_");
            switch (pressType) {
                case "SHORT":
                    if (dp.getValue() == null || !dp.getValue().equals(dp.getPreviousValue())) {
                        vdp.setValue(CommonTriggerEvents.SHORT_PRESSED);
                    } else {
                        // two (or more) PRESS_SHORT events were received
                        // within AbstractHomematicGateway#DEFAULT_DISABLE_DELAY seconds
                        vdp.setValue(CommonTriggerEvents.DOUBLE_PRESSED);
                    }
                    break;
                case "LONG":
                    vdp.setValue(CommonTriggerEvents.LONG_PRESSED);
                    break;
                case "LONG_RELEASE":
                case "CONT":
                    vdp.setValue(null);
                    break;
                default:
                    vdp.setValue(null);
                    logger.warn("Unexpected vaule '{}' for PRESS virtual datapoint", pressType);
            }
        } else {
            vdp.setValue(null);
        }
    }
}
