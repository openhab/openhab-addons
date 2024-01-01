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
package org.openhab.binding.irtrans.internal.handler;

import static org.openhab.binding.irtrans.internal.IRtransBindingConstants.CHANNEL_IO;

import org.openhab.binding.irtrans.internal.IRtransBindingConstants.Led;
import org.openhab.binding.irtrans.internal.IrCommand;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BlasterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
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
                    String[] remoteCommand = command.toString().split(",", 2);
                    if (remoteCommand.length < 2) {
                        logger.warn("Ignoring invalid command '{}'", command);
                        return;
                    }

                    IrCommand ircommand = new IrCommand();
                    ircommand.setRemote(remoteCommand[0]);
                    ircommand.setCommand(remoteCommand[1]);

                    IrCommand thingCompatibleCommand = new IrCommand();
                    thingCompatibleCommand.setRemote((String) getConfig().get(REMOTE));
                    thingCompatibleCommand.setCommand((String) getConfig().get(COMMAND));

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
    public void onCommandReceived(EthernetBridgeHandler bridge, IrCommand command) {
        logger.debug("Received command {},{} for thing {}", command.getRemote(), command.getCommand(),
                this.getThing().getUID());

        IrCommand thingCompatibleCommand = new IrCommand();
        thingCompatibleCommand.setRemote((String) getConfig().get(REMOTE));
        thingCompatibleCommand.setCommand((String) getConfig().get(COMMAND));

        if (command.matches(thingCompatibleCommand)) {
            StringType stringType = new StringType(command.getRemote() + "," + command.getCommand());
            updateState(CHANNEL_IO, stringType);
        }
    }
}
