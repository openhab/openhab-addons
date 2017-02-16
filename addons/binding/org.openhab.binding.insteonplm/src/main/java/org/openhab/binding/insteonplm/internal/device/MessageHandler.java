/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.device;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants.ExtendedData;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.GroupMessageStateMachine.GroupMessage;
import org.openhab.binding.insteonplm.internal.device.messages.MessageHandlerData;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.openhab.binding.insteonplm.internal.message.MessageType;
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
    public abstract void handleMessage(InsteonThingHandler handler, int group, byte cmd1, Message msg, Channel channel);

    /**
     * Method to send an extended insteon message for querying a device
     *
     * @param f DeviceFeature that is being currently handled
     * @param aCmd1 cmd1 for message to be sent
     * @param aCmd2 cmd2 for message to be sent
     */
    public void sendExtendedQuery(InsteonThingHandler handler, DeviceFeature f, byte aCmd1, byte aCmd2) {
        try {
            Message m = handler.getMessageFactory().makeExtendedMessage((byte) 0x1f, aCmd1, aCmd2,
                    handler.getAddress());
            m.setQuietTime(500L);
            handler.enqueueMessage(m, f);
        } catch (IOException e) {
            logger.warn("i/o problem sending query message to device {}", handler.getAddress());
        } catch (FieldException e) {
            logger.warn("field exception sending query message to device {}", handler.getAddress());
        }
    }

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
    protected boolean isMybutton(Message msg) {
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
     * @param msg message to be tested against
     * @return true if message matches
     */
    public boolean matches(Message msg) {
        try {
            if (data.extended != ExtendedData.extendedNone) {
                if ((msg.isExtended() && data.extended != ExtendedData.extendedCrc1)
                        || (!msg.isExtended() && data.extended != ExtendedData.extendedNone)) {
                    return (false);
                }
                byte value = msg.getByte("command1");
                if (data.cmd1 != -1 && value != data.cmd1) {
                    return (false);
                }
            }
            byte value = msg.getByte("command2");
            if (data.cmd2 != -1 && value != data.cmd2) {
                return (false);
            }
            value = msg.getByte("userData1");
            if (data.data1 != -1 && value != data.data1) {
                return (false);
            }
            value = msg.getByte("userData2");
            if (data.data2 != -1 && value != data.data2) {
                return (false);
            }
            value = msg.getByte("userData3");
            if (data.data3 != -1 && value != data.data3) {
                return (false);
            }
        } catch (FieldException e) {
            logger.error("error matching message: {}", msg, e);
            return (false);
        }
        return (true);
    }

    /**
     * Determines is an incoming ALL LINK message is a duplicate
     *
     * @param msg the received ALL LINK message
     * @return true if this message is a duplicate
     */
    protected boolean isDuplicate(Message msg) {
        boolean isDuplicate = false;
        try {
            MessageType t = MessageType.s_fromValue(msg.getByte("messageFlags"));
            int hops = msg.getHopsLeft();
            if (t == MessageType.ALL_LINK_BROADCAST) {
                int group = msg.getAddress("toAddress").getLowByte() & 0xff;
                byte cmd1 = msg.getByte("command1");
                // if the command is 0x06, then it's success message
                // from the original broadcaster, with which the device
                // confirms that it got all cleanup replies successfully.
                GroupMessage gm = (cmd1 == 0x06) ? GroupMessage.SUCCESS : GroupMessage.BCAST;
                isDuplicate = !updateGroupState(group, hops, gm);
            } else if (t == MessageType.ALL_LINK_CLEANUP) {
                // the cleanup messages are direct messages, so the
                // group # is not in the toAddress, but in cmd2
                int group = msg.getByte("command2") & 0xff;
                isDuplicate = !updateGroupState(group, hops, GroupMessage.CLEAN);
            }
        } catch (IllegalArgumentException e) {
            logger.error("cannot parse msg: {}", msg, e);
        } catch (FieldException e) {
            logger.error("cannot parse msg: {}", msg, e);
        }
        return (isDuplicate);
    }

    /**
     * Advance the state of the state machine that suppresses duplicates
     *
     * @param group the insteon group of the broadcast message
     * @param hops number of hops left
     * @param a what type of group message came in (action etc)
     * @return true if this is message is NOT a duplicate
     */
    private boolean updateGroupState(int group, int hops, GroupMessage a) {
        GroupMessageStateMachine m = data.groupState.get(new Integer(group));
        if (m == null) {
            m = new GroupMessageStateMachine();
            data.groupState.put(new Integer(group), m);
        }
        logger.trace("updating group state for {} to {}", group, a);
        return (m.action(a, hops));
    }

    /**
     * Extract button information from message
     *
     * @param msg the message to extract from
     * @param the device feature (needed for debug printing)
     * @return the button number or -1 if no button found
     */
    protected int getButtonInfo(Message msg) {
        // the cleanup messages have the button number in the command2 field
        // the broadcast messages have it as the lsb of the toAddress
        try {
            int bclean = msg.getByte("command2") & 0xff;
            int bbcast = msg.getAddress("toAddress").getLowByte() & 0xff;
            int button = msg.isCleanup() ? bclean : bbcast;
            logger.trace("{} button: {} bclean: {} bbcast: {}", msg.getAddress("fromAddress"), button, bclean, bbcast);
            return button;
        } catch (FieldException e) {
            logger.error("field exception while parsing msg {}: ", msg, e);
        }
        return -1;
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
