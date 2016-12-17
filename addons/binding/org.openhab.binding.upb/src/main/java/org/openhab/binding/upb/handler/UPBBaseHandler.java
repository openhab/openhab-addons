/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upb.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.upb.UPBBindingConstants;
import org.openhab.binding.upb.internal.MessageBuilder;
import org.openhab.binding.upb.internal.UPBMessage;
import org.openhab.binding.upb.internal.converter.StateConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation for handling commands sent to UPB things.
 *
 * @author Chris Van Orman
 * @since 2.0.0
 *
 */
public abstract class UPBBaseHandler extends BaseThingHandler implements UPBMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(UPBBaseHandler.class);

    private StateConverter stateConverter = new StateConverter();
    private UPBMessageSender messageSender;
    private byte id;

    /**
     * Instantiates a new {@link UPBBaseHandler}.
     *
     * @param thing the thing to be handled
     */
    public UPBBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        this.id = ((Number) getConfig().get(UPBBindingConstants.DEVICE_ID)).byteValue();

        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        byte[] commandBytes = getCommandBytes(channelUID, command);

        if (messageSender == null) {
            logger.warn("messageSender is null, command ignored");
            return;
        }

        if (commandBytes == null) {
            logger.warn("Failed to interpret command [{}]", command);
            return;
        }

        MessageBuilder message = MessageBuilder.create().destination(getId()).link(isLink()).command(commandBytes);

        if (command == RefreshType.REFRESH) {
            message.priority(UPBMessage.Priority.LOW);
        }

        messageSender.sendMessage(message);
    }

    @Override
    public void messageReceived(UPBMessage message) {
        if (message.getDestination() != getId() && message.getSource() != getId()) {
            return;
        }

        if (message.getControlWord().isLink() != isLink()) {
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        // parse status
        for (Channel c : getThing().getChannels()) {
            State newState = stateConverter.convert(message, c.getUID().getId());

            if (newState != null) {
                updateState(c.getUID(), newState);
                break;
            }
        }
    }

    /**
     * Converts a {@link Command} into an array of bytes to be passed to the UPB modem.
     * 
     * @param channelUID the channel to get the command for
     * @param command the command to convert to byte form
     * @return an array of bytes to send to the UPB modem, or null if the command could not be converted
     */
    protected byte[] getCommandBytes(ChannelUID channelUID, Command command) {
        byte[] commandByte = null;

        if (command == OnOffType.ON) {
            commandByte = new byte[] { UPBMessage.Command.ACTIVATE.toByte() };
        } else if (command == OnOffType.OFF) {
            commandByte = new byte[] { UPBMessage.Command.DEACTIVATE.toByte() };
        } else if (command instanceof PercentType) {
            commandByte = new byte[] { UPBMessage.Command.GOTO.toByte(), ((PercentType) command).byteValue() };
        } else if (command == RefreshType.REFRESH && !isLink()) {
            commandByte = new byte[] { UPBMessage.Command.REPORT_STATE.toByte() };
        }

        return commandByte;
    }

    @Override
    public void setMessageSender(UPBMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    /**
     * Gets whether this handler represents a link or not.
     *
     * @return true if this handler represents a link; false otherwise.
     */
    protected boolean isLink() {
        return false;
    }

    /**
     * Gets the UPB identifier for the thing being handled by this handler.
     *
     * @return the UPB identifier for the thing being handled by this handler
     */
    protected byte getId() {
        return id;
    }
}
