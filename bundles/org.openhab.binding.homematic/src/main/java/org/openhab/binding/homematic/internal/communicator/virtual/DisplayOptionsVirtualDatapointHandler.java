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

import org.openhab.binding.homematic.internal.communicator.parser.DisplayOptionsParser;
import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * A virtual String datapoint to control the display of a 19 button remote control. You can send a text and/or show
 * symbols, turn on the backlight and let the remote control beep.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DisplayOptionsVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_DISPLAY_OPTIONS;
    }

    @Override
    public void initialize(HmDevice device) {
        if (device.getType().startsWith(DEVICE_TYPE_19_REMOTE_CONTROL) && device.getHmInterface() != HmInterface.CUXD) {
            addDatapoint(device, 18, getName(), HmValueType.STRING, null, false);
        }
    }

    @Override
    public boolean canHandleCommand(HmDatapoint dp, Object value) {
        return getName().equals(dp.getName());
    }

    @Override
    public void handleCommand(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException {
        HmChannel channel = dp.getChannel();

        DisplayOptionsParser rcOptionsParser = new DisplayOptionsParser(channel);
        rcOptionsParser.parse(value);

        String dpNameText = rcOptionsParser.getText();
        if (dpNameText != null && !dpNameText.isBlank()) {
            sendDatapoint(gateway, channel, DATAPOINT_NAME_TEXT, dpNameText);
        }

        sendDatapoint(gateway, channel, DATAPOINT_NAME_BEEP, rcOptionsParser.getBeep());
        sendDatapoint(gateway, channel, DATAPOINT_NAME_UNIT, rcOptionsParser.getUnit());
        sendDatapoint(gateway, channel, DATAPOINT_NAME_BACKLIGHT, rcOptionsParser.getBacklight());

        for (String symbol : rcOptionsParser.getSymbols()) {
            sendDatapoint(gateway, channel, symbol, Boolean.TRUE);
        }

        sendDatapoint(gateway, channel, DATAPOINT_NAME_SUBMIT, Boolean.TRUE);
        dp.setValue(value);
    }

    private void sendDatapoint(VirtualGateway gateway, HmChannel channel, String dpName, Object newValue)
            throws IOException, HomematicClientException {
        HmDatapointInfo dpInfo = HmDatapointInfo.createValuesInfo(channel, dpName);
        HmDatapoint dp = gateway.getDatapoint(dpInfo);
        gateway.sendDatapoint(dp, new HmDatapointConfig(), newValue, null);
    }
}
