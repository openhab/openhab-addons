/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.antiferencematrix.handler;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.antiferencematrix.AntiferenceMatrixBindingConstants;
import org.openhab.binding.antiferencematrix.internal.model.InputPortDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AntiferenceMatrixInputHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Neil Renaud - Initial contribution
 */
public class AntiferenceMatrixInputHandler extends AntiferenceMatrixBasePortHandler {

    private Logger logger = LoggerFactory.getLogger(AntiferenceMatrixInputHandler.class);

    private String statusMessage;

    public AntiferenceMatrixInputHandler(Thing thing) {
        super(thing);
    }

    @Override
    void doRefresh(AntiferenceMatrixBridgeHandler bridge) {
        InputPortDetails inputPortDetails = bridge.getInputPortDetails(getInputId());
        if (!inputPortDetails.getResult()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    inputPortDetails.getErrorMessage());
        }
        refresh(inputPortDetails);
    }

    @Override
    void handleOtherCommand(ChannelUID channelUID, Command command, AntiferenceMatrixBridgeHandler bridge) {
        // We don't handle any other commands on Input Ports
    }

    public int getInputId() {
        String inputId = getThing().getProperties().get(AntiferenceMatrixBindingConstants.PROPERTY_INPUT_ID);
        return Integer.valueOf(inputId);
    }

    public void refresh(InputPortDetails inputPortDetails) {
        // if (statusMessage == null || !statusMessage.equals(inputPortDetails.getStatusMessage())) {
        statusMessage = inputPortDetails.getStatusMessage();
        updateState(new ChannelUID(getThing().getUID(), AntiferenceMatrixBindingConstants.PORT_STATUS_MESSAGE_CHANNEL),
                new StringType(statusMessage));
        // }
        updateStatusIfRequired(ThingStatus.ONLINE);
    }

    @Override
    Logger getLogger() {
        return logger;
    }

}
