/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.message;

import java.io.IOException;
import java.io.InputStream;

import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.utils.Utils;
import org.openhab.binding.insteonplm.internal.utils.Utils.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes data coming from the serial port and turns it
 * into an message. For that, it has to figure out the length of the
 * message from the header, and read enough bytes until it hits the
 * message boundary. The code is tricky, partly because the Insteon protocol is.
 * Most of the time the command code (second byte) is enough to determine the length
 * of the incoming message, but sometimes one has to look deeper into the message
 * to determine if it is a standard or extended message (their lengths differ).
 *
 * @author Bernd Pfrommer
 * @since 1.5.0
 */
public class MessageFactory {
    private static final Logger logger = LoggerFactory.getLogger(MessageFactory.class);
    // no idea what the max msg length could be, but
    // I doubt it'll ever be larger than 4k
    private final static int MAX_MSG_LEN = 4096;
    private byte[] m_buf = new byte[MAX_MSG_LEN];
    private int m_end = 0; // offset of end of buffer

    private final XMLMessageReader m_messages;

    /**
     * Constructor
     *
     * @throws FieldException
     * @throws ParsingException
     * @throws IOException
     */
    public MessageFactory() throws IOException, ParsingException, FieldException {
        // Use xml msg loader to load configs
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("msg_definitions.xml");
        if (stream != null) {
            m_messages = new XMLMessageReader(stream);
        } else {
            logger.error("could not get message definition resource!");
            m_messages = null;
        }
    }

    /**
     * Adds incoming data to the data buffer. First call addData(), then call processData()
     *
     * @param data data to be added
     * @param len length of data to be added
     */
    public void addData(byte[] data, int len) {
        if (len + m_end > MAX_MSG_LEN) {
            logger.error("warn: truncating excessively long message!");
            len = MAX_MSG_LEN - m_end;
        }
        // append the new data to the one we already have
        System.arraycopy(data, 0, m_buf, m_end, len);
        m_end += len;
        // copy the incoming data to the end of the buffer
        logger.trace("read buffer: len {} data: {}", m_end, Utils.getHexString(m_buf, m_end));
    }

    /**
     * After data has been added, this method processes it.
     * processData() needs to be called until it returns null, indicating that no
     * more messages can be formed from the data buffer.
     *
     * @return a valid message, or null if the message is not complete
     * @throws IOException if data was received with unknown command codes
     */
    public Message processData() throws IOException {
        // handle the case where we get a pure nack
        if (m_end > 0 && m_buf[0] == 0x15) {
            logger.trace("got pure nack!");
            removeFromBuffer(1);
            try {
                Message m = makeMessage("PureNACK");
                return m;
            } catch (IOException e) {
                return null;
            }
        }
        // drain the buffer until the first byte is 0x02
        if (m_end > 0 && m_buf[0] != 0x02) {
            bail("incoming message does not start with 0x02");
        }
        // Now see if we have enough data for a complete message.
        // If not, we return null, and expect this method to be called again
        // when more data has come in.
        int msgLen = -1;
        boolean isExtended = false;
        if (m_end > 1) {
            // we have some data, but do we have enough to read the entire header?
            int headerLength = m_messages.getHeaderLength(m_buf[1]);
            isExtended = m_messages.isExtended(m_buf, m_end, headerLength);
            logger.trace("header length expected: {} extended: {}", headerLength, isExtended);
            if (headerLength < 0) {
                removeFromBuffer(1); // get rid of the leading 0x02 so draining works
                bail("got unknown command code " + Utils.getHexByte(m_buf[1]));
            } else if (headerLength >= 2) {
                if (m_end >= headerLength) {
                    // only when the header is complete do we know that isExtended is correct!
                    msgLen = m_messages.getMessageLength(m_buf[1], isExtended);
                    if (msgLen < 0) {
                        // Cannot make sense out of the combined command code & isExtended flag.
                        removeFromBuffer(1);
                        bail("unknown command code/ext flag: " + Utils.getHexByte(m_buf[1]));
                    }
                }
            } else { // should never happen
                logger.error("invalid header length, internal error!");
                msgLen = -1;
            }
        }
        logger.trace("msgLen expected: {}", msgLen);
        Message msg = null;
        if (msgLen > 0 && m_end >= msgLen) {
            msg = m_messages.createMessage(m_buf, msgLen, isExtended);
            removeFromBuffer(msgLen);
        }
        logger.trace("keeping buffer len {} data: {}", m_end, Utils.getHexString(m_buf, m_end));
        return msg;
    }

    private void bail(String txt) throws IOException {
        drainBuffer(); // this will drain until end or it finds the next 0x02
        logger.warn(txt);
        throw new IOException(txt);
    }

    private void drainBuffer() {
        while (m_end > 0 && m_buf[0] != 0x02) {
            removeFromBuffer(1);
        }
    }

    private void removeFromBuffer(int len) {
        if (len > m_end) {
            len = m_end;
        }
        System.arraycopy(m_buf, len, m_buf, 0, m_end + 1 - len);
        m_end -= len;
    }

    /**
     * Creates Insteon message (for sending) of a given type
     *
     * @param type the type of message to create, as defined in the xml file
     * @return reference to message created
     * @throws IOException if there is no such message type known
     */
    public Message makeMessage(String type) throws IOException {
        Message m = m_messages.getMessage(type);
        if (m == null) {
            throw new IOException("unknown message type: " + type);
        }
        return new Message(m);
    }

    /**
     * Helper method to make standard message
     *
     * @param flags
     * @param cmd1
     * @param cmd2
     * @return standard message
     * @throws FieldException
     * @throws IOException
     */
    public Message makeStandardMessage(InsteonFlags flags, StandardInsteonMessages cmd1, byte cmd2,
            InsteonAddress address) throws FieldException, IOException {
        return (makeStandardMessage(flags, cmd1, cmd2, -1, address));
    }

    /**
     * Helper method to make standard message, possibly with group
     *
     * @param flags
     * @param cmd1
     * @param cmd2
     * @param group (-1 if not a group message)
     * @return standard message
     * @throws FieldException
     * @throws IOException
     */
    public Message makeStandardMessage(InsteonFlags flags, StandardInsteonMessages cmd1, byte cmd2, int group,
            InsteonAddress address) throws FieldException, IOException {
        Message m = makeMessage("SendStandardMessage");
        InsteonAddress addr = null;
        flags.setExtended(false);
        if (group != -1) {
            flags.setGroup(true);
            ; // mark message as group message
            flags.setAcknowledge(true);
            // and stash the group number into the address
            addr = new InsteonAddress((byte) 0, (byte) 0, (byte) (group & 0xff));
        } else {
            addr = address;
        }
        m.setAddress("toAddress", addr);
        m.setByte("messageFlags", flags.getByte());
        m.setByte("command1", cmd1.getCmd());
        m.setByte("command2", cmd2);
        return m;
    }

    public Message makeX10Message(byte rawX10, byte X10Flag) throws FieldException, IOException {
        Message m = makeMessage("SendX10Message");
        m.setByte("rawX10", rawX10);
        m.setByte("X10Flag", X10Flag);
        m.setQuietTime(300L);
        return m;
    }

    /**
     * Helper method to make extended message
     *
     * @param flags
     * @param cmd1
     * @param cmd2
     * @return extended message
     * @throws FieldException
     * @throws IOException
     */
    public Message makeExtendedMessage(InsteonFlags flags, byte cmd1, byte cmd2, InsteonAddress address)
            throws FieldException, IOException {
        return makeExtendedMessage(flags, cmd1, cmd2, new byte[] {}, address);
    }

    /**
     * Helper method to make extended message
     *
     * @param flags
     * @param cmd1
     * @param cmd2
     * @param data array with userdata
     * @return extended message
     * @throws FieldException
     * @throws IOException
     */
    public Message makeExtendedMessage(InsteonFlags flags, byte cmd1, byte cmd2, byte[] data, InsteonAddress address)
            throws FieldException, IOException {
        flags.setExtended(true);
        Message m = makeMessage("SendExtendedMessage");
        m.setAddress("toAddress", address);
        m.setByte("messageFlags", flags.getByte());
        m.setByte("command1", cmd1);
        m.setByte("command2", cmd2);
        m.setUserData(data);
        m.setCRC();
        return m;
    }

    /**
     * Helper method to make extended message, but with different CRC calculation
     *
     * @param flags
     * @param cmd1
     * @param cmd2
     * @param data array with user data
     * @return extended message
     * @throws FieldException
     * @throws IOException
     */
    public Message makeExtendedMessageCRC2(byte flags, byte cmd1, byte cmd2, byte[] data, InsteonAddress address)
            throws FieldException, IOException {
        Message m = makeMessage("SendExtendedMessage");
        m.setAddress("toAddress", address);
        m.setByte("messageFlags", (byte) (((flags & 0xff) | 0x10) & 0xff));
        m.setByte("command1", cmd1);
        m.setByte("command2", cmd2);
        m.setUserData(data);
        m.setCRC2();
        return m;
    }
}
