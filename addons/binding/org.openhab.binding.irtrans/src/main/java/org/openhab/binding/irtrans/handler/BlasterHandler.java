/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.irtrans.handler;

import static org.openhab.binding.irtrans.IRtransBindingConstants.CHANNEL_IO;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.irtrans.IRcommand;
import org.openhab.binding.irtrans.IRtransBindingConstants.Led;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BlasterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 * @since 2.1.0
 *
 */
public class BlasterHandler extends BaseThingHandler implements TransceiverStatusListener {

    // List of Configuration constants
    public static final String COMMAND = "command";
    public static final String LED = "led";
    public static final String REMOTE = "remote";

    private Logger logger = LoggerFactory.getLogger(BlasterHandler.class);

    public BlasterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        ((EthernetBridgeHandler) getBridge().getHandler()).registerTransceiverStatusListener(this);
    }

    @Override
    public void handleRemoval() {
        ((EthernetBridgeHandler) getBridge().getHandler()).unregisterTransceiverStatusListener(this);
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        EthernetBridgeHandler ethernetBridge = (EthernetBridgeHandler) getBridge().getHandler();

        if (ethernetBridge == null) {
            logger.warn("IRtrans Ethernet bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        if (!(command instanceof RefreshType)) {
            if (channelUID.getId().equals(CHANNEL_IO)) {
                if (command instanceof StringType) {
                    String remoteName = StringUtils.substringBefore(command.toString(), ",");
                    String irCommandName = StringUtils.substringAfter(command.toString(), ",");

                    IRcommand ircommand = new IRcommand();
                    ircommand.remote = remoteName;
                    ircommand.command = irCommandName;

                    IRcommand thingCompatibleCommand = new IRcommand();
                    thingCompatibleCommand.remote = (String) getConfig().get(REMOTE);
                    thingCompatibleCommand.command = (String) getConfig().get(COMMAND);

                    if (ircommand.matches(thingCompatibleCommand)) {
                        if (!ethernetBridge.sendIRcommand(ircommand, Led.get((String) getConfig().get(LED)))) {
                            logger.warn("An error occured whilst sending the infrared command '{}' for Channel '{}'",
                                    ircommand, channelUID);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onCommandReceived(EthernetBridgeHandler bridge, IRcommand command) {

        logger.debug("Received command {},{} for thing {}", command.remote, command.command, this.getThing().getUID());

        IRcommand thingCompatibleCommand = new IRcommand();
        thingCompatibleCommand.remote = (String) getConfig().get(REMOTE);
        thingCompatibleCommand.command = (String) getConfig().get(COMMAND);

        if (command.matches(thingCompatibleCommand)) {
            StringType stringType = new StringType(command.remote + "," + command.command);
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_IO), stringType);
        }

    }

    @Override
    public void onBridgeDisconnected(EthernetBridgeHandler bridge) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
    }

    @Override
    public void onBridgeConnected(EthernetBridgeHandler bridge) {
        updateStatus(ThingStatus.ONLINE);
    }
}
