/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.device;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants.ExtendedData;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.messages.MessageHandlerData;
import org.openhab.binding.insteonplm.internal.message.InsteonFlags.MessageType;
import org.openhab.binding.insteonplm.internal.message.modem.StandardMessageReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message handler processes incoming Insteon messages and reacts by publishing
 * corresponding messages on the openhab bus, updating device state etc.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 * @since 1.5.0
 */

public abstract class MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    MessageHandlerData data = new MessageHandlerData();

    /**
     * Constructor
     *
     * @param p state publishing object for dissemination of state changes
     */
    protected MessageHandler(DeviceFeature p) {
        data.feature = p;
    }

    /**
     * Method that processes incoming message. The cmd1 parameter
     * has been extracted earlier already (to make a decision which message handler to call),
     * and is passed in as an argument so cmd1 does not have to be extracted from the message again.
     *
     * @param group all-link group or -1 if not specified
     * @param cmd1 the insteon cmd1 field
     * @param msg the received insteon message
     * @param channel the DeviceFeature to which this message handler is attached
     */
    public abstract void handleMessage(InsteonThingHandler handler, int group, StandardMessageReceived message,
            Channel channel);

    /**
     * Method to send an extended insteon message for querying a device
     *
     * @param f DeviceFeature that is being currently handled
     * @param aCmd1 cmd1 for message to be sent
     * @param aCmd2 cmd2 for message to be sent
     *            public void sendExtendedQuery(InsteonThingHandler handler, DeviceFeature f, byte aCmd1, byte aCmd2) {
     *            try {
     *            Message m = handler.getMessageFactory().makeExtendedMessage((byte) 0x1f, aCmd1, aCmd2,
     *            handler.getAddress());
     *            m.setQuietTime(500L);
     *            handler.enqueueMessage(m);
     *            } catch (IOException e) {
     *            logger.warn("i/o problem sending query message to device {}", handler.getAddress());
     *            } catch (FieldException e) {
     *            logger.warn("field exception sending query message to device {}", handler.getAddress());
     *            }
     *            }
     */

    /**
     * Check if group matches
     *
     * @param group group to test for
     * @return true if group matches or no group is specified
     */
    public boolean matchesGroup(int group) {
        return (this.data.group == -1 || this.data.group == group);
    }

    /**
     * Retrieve group parameter or -1 if no group is specified
     *
     * @return group parameter
     */
    public int getGroup() {
        return data.group;
    }

    /**
     * The feature associated with this message.
     */
    public DeviceFeature getFeature() {
        return data.feature;
    }

    /**
     * Set the group value for this message handler.
     */
    public void setButton(String str) {
        data.button = Integer.valueOf(str);
    }

    /**
     * Retrieve group parameter or -1 if no group is specified
     *
     * @return group parameter
     */
    public int getButton() {
        return data.button;
    }

    /**
     * Set the group value for this message handler.
     */
    public void setGroup(String str) {
        data.group = Integer.valueOf(str);
    }

    public void setExtended(String val) {
        data.extended = ExtendedData.valueOf(val);
    }

    public void setData1(String val) {
        try {
            data.data1 = Byte.valueOf(val);
        } catch (NumberFormatException e) {
            logger.error("Unable to read {}", e, val);
        }
    }

    public void setData2(String val) {
        try {
            data.data2 = Byte.valueOf(val);
        } catch (NumberFormatException e) {
            logger.error("Unable to read {}", e, val);
        }
    }

    public void setData3(String val) {
        try {
            data.data3 = Byte.valueOf(val);
        } catch (NumberFormatException e) {
            logger.error("Unable to read {}", e, val);
        }
    }

    public void setCmd1(String factor) {
        try {
            data.cmd1 = Byte.valueOf(factor);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse {}", e, factor);
        }

    }

    public void setCmd2(String factor) {
        try {
            data.cmd2 = Byte.valueOf(factor);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse {}", e, factor);
        }

    }

    /**
     * Test if message refers to the button configured for given feature
     *
     * @param msg received message
     * @param f device feature to test
     * @return true if we have no button configured or the message is for this button
     */
    protected boolean isMybutton(StandardMessageReceived msg) {
        // if there is no button configured for this handler
        // the message is assumed to refer to this feature
        // no matter what button is addressed in the message
        if (this.data.button == -1) {
            return true;
        }

        int buttonToCheck = getButtonInfo(msg);
        return this.data.button != -1 && this.data.button == buttonToCheck;
    }

    /**
     * Test if message matches the filter parameters
     *
     * @param message message to be tested against
     * @return true if message matches
     */
    public boolean matches(StandardMessageReceived message) {
        if (data.extended != ExtendedData.extendedNone) {
            if ((message.getFlags().isExtended() && data.extended != ExtendedData.extendedCrc1)
                    || (!message.getFlags().isExtended() && data.extended != ExtendedData.extendedNone)) {
                return (false);
            }
            if (data.cmd1 != -1 && message.getCmd1().getCmd1() != data.cmd1) {
                return (false);
            }
        }
        byte value = message.getCmd2();
        if (data.cmd2 != -1 && value != data.cmd2) {
            return (false);
        }
        byte[] userData = message.getData();
        if (data.data1 != -1 && userData[0] != data.data1) {
            return (false);
        }
        if (data.data2 != -1 && userData[1] != data.data2) {
            return (false);
        }
        if (data.data3 != -1 && userData[2] != data.data3) {
            return (false);
        }
        return (true);
    }

    /**
     * Extract button information from message
     *
     * @param msg the message to extract from
     * @param the device feature (needed for debug printing)
     * @return the button number or -1 if no button found
     */
    protected int getButtonInfo(StandardMessageReceived msg) {
        // the cleanup messages have the button number in the command2 field
        // the broadcast messages have it as the lsb of the toAddress
        logger.trace("{} button: {} bclean: {} type: {}", msg.getFromAddress(),
                msg.getFlags().getMessageType().toString(), msg.getCmd2(), msg.getToAddress().getLowByte());
        if (msg.getFlags().getMessageType() == MessageType.BroadcastMessage) {
            return msg.getToAddress().getLowByte();
        } else {
            return msg.getCmd2();
        }
    }

    /**
     * Shorthand to return class name for logging purposes
     *
     * @return name of the class
     */
    protected String nm() {
        return (this.getClass().getSimpleName());
    }
}
