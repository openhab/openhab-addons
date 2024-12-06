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
package org.openhab.binding.insteon.internal.transport.message;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.utils.HexUtils;
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
        if (logger.isTraceEnabled()) {
            logger.trace("read buffer: len {} data: {}", end, HexUtils.getHexString(buf, end, false));
        }
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
                return Msg.makeMessage("PureNACK");
            } catch (InvalidMessageTypeException e) {
                return null;
            }
        }
        // drain the buffer until the first byte is 0x02
        if (end > 0 && buf[0] != 0x02) {
            logger.debug("incoming message does not start with 0x02");
            bail();
        }
        // Now see if we have enough data for a complete message.
        // If not, we return null, and expect this method to be called again
        // when more data has come in.
        if (end > 1) {
            try {
                int headerLength = Msg.getHeaderLength(buf[1]);
                logger.trace("header length expected: {}", headerLength);
                if (end >= headerLength) {
                    boolean isExtended = Msg.isExtended(buf, headerLength);
                    int msgLen = Msg.getMessageLength(buf[1], isExtended);
                    logger.trace("msgLen expected: {} extended: {}", msgLen, isExtended);
                    if (end >= msgLen) {
                        msg = Msg.createMessage(buf, msgLen, isExtended);
                        removeFromBuffer(msgLen);
                    }
                }
            } catch (InvalidMessageTypeException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("got unknown command code: {}", HexUtils.getHexString(buf[1]));
                }
                bail();
            }
        }
        // indicate no more messages available in buffer if empty or undefined message
        if (end == 0 || msg == null) {
            logger.trace("done processing current buffer data");
            done = true;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("keeping buffer len {} data: {}", end, HexUtils.getHexString(buf, end, false));
        }
        return msg;
    }

    private void bail() throws IOException {
        // drain buffer until end or the next message start
        do {
            removeFromBuffer(1);
        } while (end > 0 && buf[0] != 0x02);
        throw new IOException("bad data received");
    }

    private void removeFromBuffer(int len) {
        int l = len;
        if (l > end) {
            l = end;
        }
        if (l > 0) {
            System.arraycopy(buf, l, buf, 0, end + 1 - l);
            end -= l;
        }
    }
}
