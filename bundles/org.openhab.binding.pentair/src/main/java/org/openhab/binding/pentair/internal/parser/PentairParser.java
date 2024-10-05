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
package org.openhab.binding.pentair.internal.parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairParser } class implements the thread to read and parse the input stream. Once a packet can be
 * identified, it locates the
 * representative sending Thing and dispositions the packet so it can be further processed.
 *
 * @author Jeff James - Initial contribution
 *
 */
@NonNullByDefault
public class PentairParser implements Runnable {
    public static final int MAX_PACKET_SIZE = 50;

    private final Logger logger = LoggerFactory.getLogger(PentairParser.class);

    private enum ParserState {
        WAIT_STARTOFPACKET,
        CMD_PENTAIR,
        CMD_INTELLICHLOR
    };

    private @Nullable InputStream reader;

    public void setInputStream(InputStream reader) {
        this.reader = reader;
    }

    // Callback interface when a packet is received
    public interface CallbackPentairParser {
        public void onPentairPacket(PentairStandardPacket p);

        public void onIntelliChlorPacket(PentairIntelliChlorPacket p);

        public void parserFailureCallback();
    };

    @Nullable
    private CallbackPentairParser callback;

    public void setCallback(CallbackPentairParser cb) {
        callback = cb;
    }

    private int getByte() throws IOException {
        InputStream reader = Objects.requireNonNull(this.reader, "Reader has not been initialized.");

        return reader.read();
    }

    private int getBytes(ByteBuffer buf, int n) throws IOException {
        for (int i = 0; i < n; i++) {
            buf.put((byte) getByte());
        }

        return n;
    }

    private int calcChecksum(ByteBuffer buf) {
        int chksum = 0, i;

        for (i = 0; i < buf.limit(); i++) {
            chksum += (buf.get() & 0xFF);
        }

        return chksum;
    }

    @Override
    public void run() {
        ByteBuffer buf = ByteBuffer.allocate(MAX_PACKET_SIZE + 10);
        int c, c2;
        int checksumInPacket, checksumCalc;
        int length;

        ParserState parserstate = ParserState.WAIT_STARTOFPACKET;

        Objects.requireNonNull(this.reader, "Reader stream has not been set.");

        while (!Thread.interrupted()) {
            try {
                c = getByte();

                switch (parserstate) {
                    case WAIT_STARTOFPACKET: // will parse FF FF FF ... 00
                        if (c == 0xFF) { // for CMD_PENTAIR, we need at lease one 0xFF
                            do {
                                c = getByte();
                            } while (c == 0xFF); // consume all 0xFF

                            if (c == 0x00) {
                                parserstate = ParserState.CMD_PENTAIR;
                            }
                        }

                        if (c == 0x10) {
                            parserstate = ParserState.CMD_INTELLICHLOR;
                        }
                        break;
                    case CMD_PENTAIR: {
                        parserstate = ParserState.WAIT_STARTOFPACKET; // any break caused by invalid packet will go
                                                                      // back to waiting for a new start of packet

                        if (c != 0xFF) {
                            logger.trace("parser: FF00 !FF");
                            break;
                        }

                        buf.clear();

                        if (getBytes(buf, 6) != 6) { // read enough to get the length
                            logger.trace("Unable to read 6 bytes");

                            break;
                        }

                        if (buf.get(0) != (byte) 0xA5) {
                            logger.trace("parser: FF00FF !A5");
                            break;
                        }

                        length = (buf.get(5) & 0xFF);
                        if (length > MAX_PACKET_SIZE) {
                            logger.trace("Received packet longer than {} bytes: {}", MAX_PACKET_SIZE, length);
                            break;
                        }

                        // buf should contain A5 00 0F 10 02 1D (A5 00 D S A L)
                        if (getBytes(buf, length) != length) { // read remaining packet
                            break;
                        }

                        checksumInPacket = (getByte() << 8) & 0xFF00;
                        checksumInPacket += (getByte() & 0xFF);

                        buf.flip();

                        checksumCalc = calcChecksum(buf.duplicate());

                        if (checksumInPacket != checksumCalc) {
                            logger.trace("Checksum error: {}!={}-{}", checksumInPacket, checksumCalc,
                                    PentairBasePacket.toHexString(buf));
                            break;
                        }

                        PentairStandardPacket p = new PentairStandardPacket(buf.array(), buf.limit());

                        logger.trace("[{}] PentairPacket: {}", p.getSource(), p.toString());
                        CallbackPentairParser callback = this.callback;
                        if (callback != null) {
                            callback.onPentairPacket(p);
                        }

                        break;
                    }
                    case CMD_INTELLICHLOR: { // 10 02 00 12 89 90 xx 10 03
                        parserstate = ParserState.WAIT_STARTOFPACKET; // any break caused by invalid packet will go back
                                                                      // to waiting on a new packet frame

                        buf.clear();
                        buf.put((byte) 0x10); // need to add back in the initial start of packet since that is included
                                              // in checksum

                        if ((byte) c != (byte) 0x02) {
                            break;
                        }
                        buf.put((byte) c);
                        buf.put((byte) getByte()); // Destination

                        c = (byte) getByte();
                        buf.put((byte) c); // Command

                        length = PentairIntelliChlorPacket.getPacketDataLength(c);
                        int dest = buf.get(2);
                        if (length == -1) {
                            logger.debug("[{}] IntelliChlor Packet unseen: command - {}", dest, c & 0xFF);
                            break;
                        }

                        // data bytes + 1 checksum + 0x10, 0x03
                        if (getBytes(buf, length) != length) {
                            break;
                        }

                        checksumInPacket = getByte();

                        c = getByte(); // 0x10
                        c2 = getByte(); // 0x03
                        // Check to see if closing command is 0x10 and and 0x03
                        if ((byte) c != (byte) 0x10 || (byte) c2 != (byte) 0x03) {
                            logger.trace("[{}]Invalid Intellichlor command: {}", dest,
                                    PentairBasePacket.toHexString(buf));
                            break; // invalid command
                        }

                        buf.flip();
                        checksumCalc = calcChecksum(buf.duplicate());
                        if ((byte) checksumCalc != (byte) checksumInPacket) {
                            logger.trace("[{}] Invalid Intellichlor checksum: {}", dest,
                                    PentairBasePacket.toHexString(buf));
                            break;
                        }

                        PentairIntelliChlorPacket pic = new PentairIntelliChlorPacket(buf.array(), buf.limit());

                        logger.trace("[{}] IntelliChlor Packet: {}", dest, pic.toString());
                        CallbackPentairParser callback = this.callback;
                        if (callback != null) {
                            callback.onIntelliChlorPacket(pic);
                        }

                        break;
                    }
                }
            } catch (IOException e) {
                logger.debug("I/O error while reading from stream: {}", e.getMessage());
                Thread.currentThread().interrupt();
                CallbackPentairParser callback = this.callback;
                if (callback != null) {
                    callback.parserFailureCallback();
                }
                break; // exit while loop
                // PentairBaseBridgeHandler will monitor this thread and restart if it exits unexpectedly
            }
        }

        logger.trace("msg reader thread exited");
    }
}
