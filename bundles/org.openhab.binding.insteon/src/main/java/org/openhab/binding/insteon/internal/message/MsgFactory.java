/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.message;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes data coming from the serial port and turns it
 * into a message. For that, it has to figure out the length of the
 * message from the header, and read enough bytes until it hits the
 * message boundary. The code is tricky, partly because the Insteon protocol is.
 * Most of the time the command code (second byte) is enough to determine the length
 * of the incoming message, but sometimes one has to look deeper into the message
 * to determine if it is a standard or extended message (their lengths differ).
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class MsgFactory {
    private final Logger logger = LoggerFactory.getLogger(MsgFactory.class);
    // no idea what the max msg length could be, but
    // I doubt it'll ever be larger than 4k
    private static final int MAX_MSG_LEN = 4096;
    private byte[] buf = new byte[MAX_MSG_LEN];
    private int end = 0; // offset of end of buffer
    private boolean done = true; // done fully processing buffer flag

    /**
     * Constructor
     */
    public MsgFactory() {
    }

    /**
     * Indicates if no more complete message available in the buffer to be processed
     *
     * @return buffer data fully processed flag
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Adds incoming data to the data buffer. First call addData(), then call processData()
     *
     * @param data data to be added
     * @param len length of data to be added
     */
    public void addData(byte[] data, int len) {
        int l = len;
        if (l + end > MAX_MSG_LEN) {
            logger.warn("truncating excessively long message!");
            l = MAX_MSG_LEN - end;
        }
        // indicate new data can be processed if length > 0
        if (l > 0) {
            done = false;
        }
        // append the new data to the one we already have
        System.arraycopy(data, 0, buf, end, l);
        end += l;
        // copy the incoming data to the end of the buffer
        logger.trace("read buffer: len {} data: {}", end, Utils.getHexString(buf, end));
    }

    /**
     * After data has been added, this method processes it.
     * processData() needs to be called until it returns null, indicating that no
     * more messages can be formed from the data buffer.
     *
     * @return a valid message, or null if the message is not complete
     * @throws IOException if data was received with unknown command codes
     */
    public @Nullable Msg processData() throws IOException {
        Msg msg = null;
        // handle the case where we get a pure nack
        if (end > 0 && buf[0] == 0x15) {
            logger.trace("got pure nack!");
            removeFromBuffer(1);
            try {
                msg = Msg.makeMessage("PureNACK");
                return msg;
            } catch (InvalidMessageTypeException e) {
                return null;
            }
        }
        // drain the buffer until the first byte is 0x02
        if (end > 0 && buf[0] != 0x02) {
            bail("incoming message does not start with 0x02");
        }
        // Now see if we have enough data for a complete message.
        // If not, we return null, and expect this method to be called again
        // when more data has come in.
        if (end > 1) {
            // we have some data, but do we have enough to read the entire header?
            int headerLength = Msg.getHeaderLength(buf[1]);
            boolean isExtended = Msg.isExtended(buf, end, headerLength);
            logger.trace("header length expected: {} extended: {}", headerLength, isExtended);
            if (headerLength < 0) {
                removeFromBuffer(1); // get rid of the leading 0x02 so draining works
                bail("got unknown command code " + Utils.getHexByte(buf[0]));
            } else if (headerLength >= 2) {
                if (end >= headerLength) {
                    // only when the header is complete do we know that isExtended is correct!
                    int msgLen = Msg.getMessageLength(buf[1], isExtended);
                    logger.trace("msgLen expected: {}", msgLen);
                    if (msgLen < 0) {
                        // Cannot make sense out of the combined command code & isExtended flag.
                        removeFromBuffer(1);
                        bail("got unknown command code/ext flag " + Utils.getHexByte(buf[0]));
                    } else if (msgLen > 0) {
                        if (end >= msgLen) {
                            msg = Msg.createMessage(buf, msgLen, isExtended);
                            removeFromBuffer(msgLen);
                        }
                    } else { // should never happen
                        logger.warn("invalid message length, internal error!");
                    }
                }
            } else { // should never happen
                logger.warn("invalid header length, internal error!");
            }
        }
        // indicate no more messages available in buffer if empty or undefined message
        if (end == 0 || msg == null) {
            logger.trace("done processing current buffer data");
            done = true;
        }
        logger.trace("keeping buffer len {} data: {}", end, Utils.getHexString(buf, end));
        return msg;
    }

    private void bail(String txt) throws IOException {
        drainBuffer(); // this will drain until end or it finds the next 0x02
        logger.debug("bad data received: {}", txt);
        throw new IOException(txt);
    }

    private void drainBuffer() {
        while (end > 0 && buf[0] != 0x02) {
            removeFromBuffer(1);
        }
    }

    private void removeFromBuffer(int len) {
        int l = len;
        if (l > end) {
            l = end;
        }
        System.arraycopy(buf, l, buf, 0, end + 1 - l);
        end -= l;
    }
}
