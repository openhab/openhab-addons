/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.boschindego.handler;

import static org.openhab.binding.boschindego.BoschIndegoBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.zazaz.iot.bosch.indego.DeviceCommand;
import de.zazaz.iot.bosch.indego.DeviceStateInformation;
import de.zazaz.iot.bosch.indego.DeviceStatus;
import de.zazaz.iot.bosch.indego.IndegoAuthenticationException;
import de.zazaz.iot.bosch.indego.IndegoController;
import de.zazaz.iot.bosch.indego.IndegoException;

/**
 * The {@link BoschIndegoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Fleck - Initial contribution
 */
public class BoschIndegoHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(BoschIndegoHandler.class);
    private int commandToSend;

    public BoschIndegoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(STATE)) {
            if (command instanceof DecimalType) {
                synchronized (this) {
                    commandToSend = ((DecimalType) command).intValue();
                    this.notifyAll();
                }
            } else {
                System.out.println("No decimal type");
            }
            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void updateConfiguration(Configuration configuration) {
        // TODO Auto-generated method stub
        super.updateConfiguration(configuration);

    }

    private synchronized void poll() {
        // Create controller instance
        try {
            IndegoController controller = new IndegoController(getConfig().get("username").toString(),
                    getConfig().get("password").toString());
            // Connect to server
            controller.connect();
            // Query the device state
            DeviceStateInformation state = controller.getState();
            DeviceStatus statusWithMessage = DeviceStatus.decodeStatusCode(state.getState());
            int eshStatus = getEshStatusFromCommand(statusWithMessage.getAssociatedCommand());
            int mowed = state.getMowed();
            updateStatus(ThingStatus.ONLINE);

            if (commandToSend > 0 && commandToSend <= 3 && commandToSend != eshStatus) {
                System.out.println("Sending command");
                updateState(TEXTUAL_STATE, new StringType("Refreshing..."));
                controller.sendCommand(getCommandFromEshStatus(commandToSend));
                commandToSend = 0;
                try {
                    for (int i = 0; i < 30; i++) {
                        DeviceStateInformation stateTmp = controller.getState();
                        if (state.getState() != stateTmp.getState()) {
                            state = stateTmp;
                            statusWithMessage = DeviceStatus.decodeStatusCode(state.getState());
                            eshStatus = getEshStatusFromCommand(statusWithMessage.getAssociatedCommand());
                            mowed = state.getMowed();
                            break;
                        }
                        Thread.sleep(1000);
                    }

                } catch (InterruptedException e) {
                }
            }
            controller.disconnect();
            updateState(MOWED, new PercentType(mowed));
            updateState(STATE, new DecimalType(eshStatus));
            updateState(TEXTUAL_STATE, new StringType(statusWithMessage.getMessage()));

        } catch (IndegoAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } catch (IndegoException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    private DeviceCommand getCommandFromEshStatus(int eshStatus) {
        DeviceCommand command;
        switch (eshStatus) {
            case 1:
                command = DeviceCommand.MOW;
                break;
            case 2:
                command = DeviceCommand.RETURN;
                break;
            case 3:
                command = DeviceCommand.PAUSE;
                break;
            default:
                command = null;
        }
        return command;
    }

    private int getEshStatusFromCommand(DeviceCommand command) {
        int eshStatus;
        switch (command) {
            case MOW:
                eshStatus = 1;
                break;
            case RETURN:
                eshStatus = 2;
                break;
            case PAUSE:
                eshStatus = 3;
                break;
            default:
                eshStatus = 0;
        }
        return eshStatus;
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    Thread.sleep(3000);
                    poll();
                    while (true) {
                        synchronized (BoschIndegoHandler.this) {
                            BoschIndegoHandler.this.wait(((BigDecimal) getConfig().get("refresh")).intValue() * 1000);
                            System.out.println("Polling1");
                            poll();
                        }
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();
        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
