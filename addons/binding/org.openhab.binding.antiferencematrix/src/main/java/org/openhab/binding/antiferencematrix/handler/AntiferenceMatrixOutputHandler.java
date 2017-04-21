/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.antiferencematrix.handler;

import static org.openhab.binding.antiferencematrix.AntiferenceMatrixBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.antiferencematrix.AntiferenceMatrixBindingConstants;
import org.openhab.binding.antiferencematrix.internal.model.InputPortDetails;
import org.openhab.binding.antiferencematrix.internal.model.OutputPortDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AntiferenceMatrixOutputHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Neil Renaud - Initial contribution
 */
public class AntiferenceMatrixOutputHandler extends AntiferenceMatrixBasePortHandler {

    private Logger logger = LoggerFactory.getLogger(AntiferenceMatrixOutputHandler.class);

    public AntiferenceMatrixOutputHandler(Thing thing) {
        super(thing);
    }

    @Override
    void handleOtherCommand(ChannelUID channelUID, Command command, AntiferenceMatrixBridgeHandler bridge) {
        int outputId = getOutputId();
        if (channelUID.getId().equals(POWER_CHANNEL) && command instanceof OnOffType) {
            bridge.changePower(outputId, (OnOffType) command);
        }
        if (channelUID.getId().equals(SOURCE_CHANNEL) && command instanceof DecimalType) {
            bridge.changeSource(outputId, (DecimalType) command);
        }
    }

    @Override
    void doRefresh(AntiferenceMatrixBridgeHandler bridge) {
        OutputPortDetails outputPortDetails = bridge.getOutputPortDetails(getOutputId());
        if (!outputPortDetails.getResult()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    outputPortDetails.getErrorMessage());
        }
        refresh(outputPortDetails);
    }

    public int getOutputId() {
        String outputId = getThing().getProperties().get(AntiferenceMatrixBindingConstants.PROPERTY_OUTPUT_ID);
        return Integer.valueOf(outputId);
    }

    public void refresh(OutputPortDetails outputPortDetails) {
        // if (statusMessage == null || !statusMessage.equals(outputPortDetails.getStatusMessage())) {
        // statusMessage = outputPortDetails.getStatusMessage();
        updateState(new ChannelUID(getThing().getUID(), AntiferenceMatrixBindingConstants.PORT_STATUS_MESSAGE_CHANNEL),
                new StringType(outputPortDetails.getStatusMessage()));
        // }

        OnOffType power;
        if (outputPortDetails.getSinkPowerStatus() > 0) {
            power = OnOffType.ON;
        } else {
            power = OnOffType.OFF;
        }

        // if (this.power != power) {
        updateState(new ChannelUID(getThing().getUID(), AntiferenceMatrixBindingConstants.POWER_CHANNEL), power);
        // this.power = power;
        // }

        updateStatusIfRequired(ThingStatus.ONLINE);
    }

    public void refresh(InputPortDetails inputPortDetails) {
        int[] nodes = inputPortDetails.getTransmissionNodes();
        int outputId = getOutputId();
        for (int node : nodes) {
            if (node == outputId) {
                // if (this.source != inputPortDetails.getBay()) {
                updateState(new ChannelUID(getThing().getUID(), AntiferenceMatrixBindingConstants.SOURCE_CHANNEL),
                        new DecimalType(inputPortDetails.getBay()));
                updateStatusIfRequired(ThingStatus.ONLINE);
                // this.source = inputPortDetails.getBay();
                // }

            }
        }

    }

    @Override
    Logger getLogger() {
        return logger;
    }

}
