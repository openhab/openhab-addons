/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.cm11a.internal.handler;

import java.util.List;

import org.openhab.binding.cm11a.internal.X10Interface;
import org.openhab.binding.cm11a.internal.X10ReceivedData;
import org.openhab.binding.cm11a.internal.X10ReceivedData.X10COMMAND;
import org.openhab.binding.cm11a.internal.config.Cm11aConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NoSuchPortException;

/**
 * The {@link Cm11aBridgeHandler} is the Bridge (see
 * https://openhab.org/documentation/development/bindings/bridge-handler.html
 * for a description of OpenHAB bridges. This gets called first and is responsible for setting up the handler. Mostly it
 * loads the 10Interface class which does all of the heavy lifting.
 *
 * @author Bob Raker - Initial contribution
 */
public class Cm11aBridgeHandler extends BaseBridgeHandler implements ReceivedDataListener {

    private Cm11aConfig cm11aConfig;

    private X10Interface x10Interface;

    private Bridge bridge;

    private final Logger logger = LoggerFactory.getLogger(Cm11aBridgeHandler.class);

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

        // Verify the configuration is valid
        if (!validateConfig(this.cm11aConfig)) {
            return;
        }

        // Initialize the X10 interface
        try {
            x10Interface = new X10Interface(cm11aConfig.serialPort, this);
            x10Interface.setDaemon(true);
            x10Interface.start();
            x10Interface.addReceivedDataListener(this);
            logger.info("Initialized CM11A X10 interface on: {}", cm11aConfig.serialPort);
        } catch (NoSuchPortException e) {
            x10Interface = null;
            logger.error("No such port exists on this machine: {}", cm11aConfig.serialPort);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "No such port exists on this machine: " + cm11aConfig.serialPort);
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        // Close the serial port
        if (x10Interface != null) {
            x10Interface.disconnect();
            x10Interface = null;
        }
        logger.debug("Cm11aBridgeHandler is being removed.");
    }

    /**
     * Validate that the configuration is valid
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
        logger.debug("Cm11aReceivedDataManager received the following data: {}", rd);

        List<Thing> things = bridge.getThings();

        // This block goes through the Things attached to this bridge and find the HouseUnitCode that matches what came
        // from the serial port. Then it looks at the channels in that thing and looks for a channel that ends with
        // "switchstatus" or "lightlevel"
        // which is the one that should be updated.
        synchronized (rd) {
            for (Thing thing : things) {
                String houseUnitCode = (String) thing.getConfiguration().get("houseUnitCode");
                for (String messageHouseUnitCode : rd.getAddr()) {
                    if (messageHouseUnitCode.equals(houseUnitCode)) {
                        // The channel we want should end in "switchstatus" or "lightlevel". In reality there is
                        // probably only one channel since these things only define one channel
                        ChannelUID desiredChannelUid = findX10Channel(thing.getChannels());
                        if (desiredChannelUid == null) {
                            return;
                        }
                        X10COMMAND cmd = rd.getCmd();
                        int dims = rd.getDims();
                        updateX10State(thing, desiredChannelUid, cmd, dims);
                    }
                }
            }
        }
    }

    /**
     * Find the X10 channel.
     *
     * @param channels
     * @return
     */
    private ChannelUID findX10Channel(List<Channel> channels) {
        ChannelUID desiredChannelUid = null;
        for (Channel channel : channels) {
            if (channel.getUID().toString().endsWith("switchstatus")
                    || channel.getUID().toString().endsWith("lightlevel")) {
                desiredChannelUid = channel.getUID();
                break;
            }
        }
        return desiredChannelUid;
    }

    /**
     * Update the X10 state
     *
     * @param thing
     * @param channelUid
     * @param cmd
     * @param dims
     */
    private void updateX10State(Thing thing, ChannelUID channelUid, X10COMMAND cmd, int dims) {
        if (thing == null) {
            logger.debug("Unable to update X10 state: thing is null");
            return;
        }

        Cm11aAbstractHandler handler = (Cm11aAbstractHandler) thing.getHandler();
        if (handler == null) {
            logger.debug("Unable to update X10 state: handler is null");
            return;
        }

        // Perform appropriate update based on X10Command received
        // Handle ON/OFF commands
        if (cmd == X10ReceivedData.X10COMMAND.ON) {
            updateState(channelUid, OnOffType.ON);
            handler.setCurrentState(OnOffType.ON);
        } else if (cmd == X10ReceivedData.X10COMMAND.OFF) {
            updateState(channelUid, OnOffType.OFF);
            handler.setCurrentState(OnOffType.OFF);
            // Handle DIM/Bright commands
        } else if (cmd == X10ReceivedData.X10COMMAND.DIM) {
            State newState = handler.addDimsToCurrentState(dims);
            updateState(channelUid, newState);
            handler.setCurrentState(newState);
            logger.debug("Current state set to: {}", handler.getCurrentState().toFullString());
        } else if (cmd == X10ReceivedData.X10COMMAND.BRIGHT) {
            State newState = handler.addBrightsToCurrentState(dims);
            updateState(channelUid, newState);
            handler.setCurrentState(newState);
            logger.debug("Current state set to: {}", handler.getCurrentState().toFullString());
        } else {
            logger.warn("Received unknown command from cm11a: {}", cmd);
        }
    }

    /**
     * Get the X10Interface
     *
     * @return the X10Interface
     */
    public X10Interface getX10Interface() {
        return x10Interface;
    }

    public void changeBridgeStatusToUp() {
        if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.ONLINE);
            logger.debug("Changed the Bridge status to online because the serial interface is working again.");
        }
    }

    public void changeBridgeStatusToDown(String message) {
        if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
            // Bridge was online but the serial interface is now down
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
            logger.debug("Changed the Bridge status to offline because {}.", message);
        }
    }
}
