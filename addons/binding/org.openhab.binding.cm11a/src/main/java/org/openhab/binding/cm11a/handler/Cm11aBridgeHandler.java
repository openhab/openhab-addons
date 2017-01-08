/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cm11a.handler;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.cm11a.config.Cm11aConfig;
import org.openhab.binding.cm11a.internal.X10Interface;
import org.openhab.binding.cm11a.internal.X10ReceivedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NoSuchPortException;

/**
 * The {@link CM11AHandler} is the Bridge (see
 * https://www.eclipse.org/smarthome/documentation/development/bindings/bridge-handler.html
 * for a description of OpenHAB bridges. This gets called first and is responsible for setting up the handler. Mostly it
 * loads the
 * 10Interface class which does all of the heavy lifting.
 *
 * @author bob raker - Initial contribution
 */
public class Cm11aBridgeHandler extends BaseBridgeHandler implements ReceivedDataListener {

    private Cm11aConfig cm11aConfig = null;

    private X10Interface x10Interface = null;

    private Bridge bridge;

    private Logger logger = LoggerFactory.getLogger(Cm11aBridgeHandler.class);

    public Cm11aBridgeHandler(Bridge bridge) {
        super(bridge);
        this.bridge = bridge;
    }

    public Cm11aConfig getCm11AConfig() {
        return this.cm11aConfig;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Commands are handled by the "Things" which include Cm11aSwitchHandler and Cm11aLampHandler
    }

    @Override
    public void initialize() {

        // Get serial port number from config
        cm11aConfig = getThing().getConfiguration().as(Cm11aConfig.class);
        logger.trace("********* cm11a initialize started *********");

        // Initialize the X10 interface
        if (validateConfig(this.cm11aConfig)) {
            try {
                x10Interface = new X10Interface(cm11aConfig.serialPort);
                x10Interface.setDaemon(true);
                x10Interface.start();
                x10Interface.addReceivedDataListener(this);
                logger.info("Initialized CM11A X10 interface on: " + cm11aConfig.serialPort);
            } catch (NoSuchPortException e) {
                x10Interface = null;
                logger.error("No such port exists on this machine: " + cm11aConfig.serialPort);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "No such port exists on this machine: " + cm11aConfig.serialPort);
                return;
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        // Close the serial port
        x10Interface.disconnect();
    }

    /**
     * Validate the configuration is valid
     *
     * @param cm11aConfig2
     * @return
     */
    private boolean validateConfig(Cm11aConfig config) {
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "cm11a configuration missing");
            return false;
        }

        String port = config.serialPort;
        if (port == null || port.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "cm11a serialPort not specified");
            return false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.cm11a.handler.ReceivedDataListener#receivedX10Data(org.openhab.binding.cm11a.internal.
     * X10ReceivedData)
     */
    @Override
    public void receivedX10Data(X10ReceivedData rd) {

        logger.debug("Cm11aReceivedDataManager recieved the following data: " + rd.toString());

        List<Thing> things = bridge.getThings();

        // This block goes through the Things attached to this bridge and find the HouseUnitCode that matches what came
        // from the serial port. Then it looks at the channels in that thing and looks for a channel that ends with
        // "switchstatus" or "lightlevel"
        // which is the one that should be updated.
        synchronized (rd) {
            for (Thing thing : things) {
                String houseUnitCode = (String) thing.getConfiguration().get("HouseUnitCode");
                if (houseUnitCode != null) {
                    for (String huc : rd.getAddr()) {
                        if (houseUnitCode.equals(huc)) {
                            List<Channel> channels = thing.getChannels();
                            // The channel we want should end in "switchstatus" or "lightlevel". In reality there is
                            // probably only one
                            // channel since these things only define one channel
                            ChannelUID desiredChannelUid = null;
                            for (Channel ch : channels) {
                                if (ch.getUID().toString().endsWith("switchstatus")
                                        || ch.getUID().toString().endsWith("lightlevel")) {
                                    desiredChannelUid = ch.getUID();
                                    break;
                                }
                            }
                            if (desiredChannelUid != null) {
                                if (rd.getCmd() == X10ReceivedData.X10COMMAND.ON) {
                                    handleUpdate(desiredChannelUid, OnOffType.ON);
                                    ((Cm11aAbstractHandler) thing.getHandler()).setCurrentState(OnOffType.ON); // update
                                                                                                               // state
                                                                                                               // in the
                                                                                                               // thing
                                                                                                               // handler
                                } else if (rd.getCmd() == X10ReceivedData.X10COMMAND.OFF) {
                                    handleUpdate(desiredChannelUid, OnOffType.OFF);
                                    ((Cm11aAbstractHandler) thing.getHandler()).setCurrentState(OnOffType.OFF); // update
                                                                                                                // state
                                                                                                                // in
                                                                                                                // the
                                                                                                                // thing
                                                                                                                // handler
                                } else if (rd.getCmd() == X10ReceivedData.X10COMMAND.DIM) {
                                    int dims = rd.getDims();
                                    State newState = ((Cm11aAbstractHandler) thing.getHandler())
                                            .addDimsToCurrentState(dims);
                                    handleUpdate(desiredChannelUid, newState);
                                    ((Cm11aAbstractHandler) thing.getHandler()).setCurrentState(newState);
                                    logger.debug("Current state set to: " + ((Cm11aAbstractHandler) thing.getHandler())
                                            .getCurrentState().toFullString());
                                } else if (rd.getCmd() == X10ReceivedData.X10COMMAND.BRIGHT) {
                                    State newState = ((Cm11aAbstractHandler) thing.getHandler())
                                            .addBrightsToCurrentState(rd.getDims());
                                    handleUpdate(desiredChannelUid, newState);
                                    ((Cm11aAbstractHandler) thing.getHandler()).setCurrentState(newState);
                                    logger.debug("Current state set to: " + ((Cm11aAbstractHandler) thing.getHandler())
                                            .getCurrentState().toFullString());
                                } else {
                                    logger.error("Received unknown command from cm11a: " + rd.getCmd());
                                }
                            }
                            logger.debug("Got channels: " + Arrays.toString(channels.toArray(new Channel[0])));
                        }
                    }
                }
            }
        }
    }

    /**
     * The default implementation of this method doesn't do anything. We need it to update the ui as done by the
     * updateState function.
     * And, update state can not be called directly because it is protected
     */
    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        updateState(channelUID, newState);
    }

    /**
     * Get the X10Interface
     *
     * @return the X10Interface
     */
    public X10Interface getX10Interface() {
        return x10Interface;
    }
}
