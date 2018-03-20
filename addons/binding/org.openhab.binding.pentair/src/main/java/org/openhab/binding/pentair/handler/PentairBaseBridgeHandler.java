/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pentair.handler;

import static org.openhab.binding.pentair.PentairBindingConstants.INTELLIFLO_THING_TYPE;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.pentair.internal.PentairPacket;
import org.openhab.binding.pentair.internal.PentairPacketIntellichlor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for all common functions for different bridge implementations. Use as superclass for IPBridge and
 * SerialBridge implementations.
 *
 * - Implements parsing of packets on Pentair bus and dispositions to appropriate Thing
 * - Periodically sends query to any {@link PentairIntelliFloHandler} things
 * - Provides function to write packets
 *
 * @author Jeff James - Initial contribution
 *
 */
public abstract class PentairBaseBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(PentairBaseBridgeHandler.class);

    /** input stream - subclass needs to assign in connect function */
    protected BufferedInputStream reader;
    /** output stream - subclass needs to assing in connect function */
    protected BufferedOutputStream writer;
    /** thread for parser - subclass needs to create/assign connect */
    protected Thread thread;
    /** parser object - subclass needs to create/assign during connect */
    protected Parser parser;
    /** polling job for pump status */
    protected ScheduledFuture<?> pollingjob;
    /** ID to use when sending commands on Pentair bus - subclass needs to assign based on configuration parameter */
    protected int id;
    /** array to keep track of IDs seen on the Pentair bus that do not correlate to a configured Thing object */
    protected ArrayList<Integer> unregistered = new ArrayList<>();

    /**
     * Gets pentair bus id
     *
     * @return id
     */
    public int getId() {
        return id;
    }

    private enum ParserState {
        WAIT_SOC,
        CMD_PENTAIR,
        CMD_INTELLICHLOR
    };

    /**
     * Constructor
     *
     * @param bridge
     */
    PentairBaseBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Bridge received refresh command");
        }
    }

    @Override
    public void initialize() {
        logger.debug("initializing Pentair Bridge handler.");

        connect();

        pollingjob = scheduler.scheduleWithFixedDelay(new PumpStatus(), 10, 120, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        pollingjob.cancel(true);
        disconnect();
    }

    /**
     * Abstract method for creating connection. Must be implemented in subclass.
     */
    protected abstract void connect();

    /**
     * Abstract method for disconnect. Must be implemented in subclass
     */
    protected abstract void disconnect();

    /**
     * Helper function to find a Thing assigned to this bridge with a specific pentair bus id.
     *
     * @param id Pentiar bus id
     * @return Thing object. null if id is not found.
     */
    public Thing findThing(int id) {
        List<Thing> things = getThing().getThings();

        for (Thing t : things) {
            PentairBaseThingHandler handler = (PentairBaseThingHandler) t.getHandler();

            if (handler != null && handler.getPentairID() == id) {
                return t;
            }
        }

        return null;
    }

    /**
     * Class for throwing an End of Buffer exception, used in getByte when read returns a -1. This is used to signal an
     * exit from the parser.
     *
     * @author Jeff James - initial contribution
     *
     */
    public class EOBException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    /**
     * Gets a single byte from reader input stream
     *
     * @param s used during debug to identify proper state transitioning
     * @return next byte from reader
     * @throws EOBException
     * @throws IOException
     */
    private int getByte(ParserState s) throws EOBException, IOException {
        int c = 0;

        c = reader.read();
        if (c == -1) {
            // EOBException is thrown if no more bytes in buffer. This exception is used to exit the parser when full
            // packet is not in buffer
            throw new EOBException();
        }

        return c;
    }

    /**
     * Gets a specific number of bytes from reader input stream
     *
     * @param buf byte buffer to store bytes
     * @param start starting index to store bytes
     * @param n number of bytes to read
     * @return number of bytes read
     * @throws EOBException
     * @throws IOException
     */
    private int getBytes(byte[] buf, int start, int n) throws EOBException, IOException {
        int i;
        int c;

        for (i = 0; i < n; i++) {
            c = reader.read();
            if (c == -1) {
                // EOBException is thrown if no more bytes in buffer. This exception is used to exit the parser when
                // full packet is not in buffer
                throw new EOBException();
            }

            buf[start + i] = (byte) c;
        }

        return i;
    }

    /**
     * Job to send pump query status packages to all Intelliflo Pump things in order to see the status.
     * Note: From the internet is seems some FW versions of EasyTouch controllers send this automatically and this the
     * pump status packets can just be snooped, however my controller version does not do this. No harm in sending.
     *
     * @author Jeff James
     *
     */
    class PumpStatus implements Runnable {
        @Override
        public void run() {
            List<Thing> things = getThing().getThings();

            // FF 00 FF A5 00 60 10 07 00 01 1C
            byte[] packet = { (byte) 0xA5, (byte) 0x00, (byte) 0x00, (byte) id, (byte) 0x07, (byte) 0x00 };

            PentairPacket p = new PentairPacket(packet);

            for (Thing t : things) {
                if (!t.getThingTypeUID().equals(INTELLIFLO_THING_TYPE)) {
                    continue;
                }

                p.setDest(((PentairIntelliFloHandler) t.getHandler()).id);
                writePacket(p);
                try {
                    Thread.sleep(300); // make sure each pump has time to respond
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    };

    /**
     * Implements the thread to read and parse the input stream. Once a packet can be indentified, it locates the
     * representive sending Thing and dispositions the packet so it can be further processed.
     *
     * @author Jeff James - initial implementation
     *
     */
    class Parser implements Runnable {
        @Override
        public void run() {
            logger.debug("parser thread started");
            byte buf[] = new byte[40];
            int c;
            int chksum, i, length;
            Thing thing;
            PentairBaseThingHandler thinghandler;

            ParserState parserstate = ParserState.WAIT_SOC;

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    c = getByte(parserstate);

                    switch (parserstate) {
                        case WAIT_SOC:
                            if (c == 0xFF) { // for CMD_PENTAIR, we need at lease one 0xFF
                                do {
                                    c = getByte(parserstate);
                                } while (c == 0xFF); // consume all 0xFF

                                if (c == 0x00) {
                                    parserstate = ParserState.CMD_PENTAIR;
                                }
                            }

                            if (c == 0x10) {
                                parserstate = ParserState.CMD_INTELLICHLOR;
                            }
                            break;
                        case CMD_PENTAIR:
                            parserstate = ParserState.WAIT_SOC; // any break will go back to WAIT_SOC

                            if (c != 0xFF) {
                                logger.debug("FF00 !FF");
                                break;
                            }

                            if (getBytes(buf, 0, 6) != 6) { // read enough to get the length
                                logger.debug("Unable to read 6 bytes");

                                break;
                            }
                            if (buf[0] != (byte) 0xA5) {
                                logger.debug("FF00FF !A5");
                                break;
                            }

                            length = buf[5];
                            if (length == 0) {
                                logger.debug("Command length of 0");
                            }
                            if (length > 34) {
                                logger.debug("Received packet longer than 34 bytes: {}", length);
                                break;
                            }
                            if (getBytes(buf, 6, length) != length) { // read remaining packet
                                break;
                            }

                            chksum = 0;
                            for (i = 0; i < length + 6; i++) {
                                chksum += buf[i] & 0xFF;
                            }

                            c = getByte(parserstate) << 8;
                            c += getByte(parserstate);

                            if (c != chksum) {
                                logger.debug("Checksum error: {}", PentairPacket.bytesToHex(buf, length + 6));
                                break;
                            }

                            PentairPacket p = new PentairPacket(buf);

                            thing = findThing(p.getSource());
                            if (thing == null) {
                                if ((p.getSource() >> 8) == 0x02) { // control panels are 0x3*, don't treat as an
                                                                    // unregistered device
                                    logger.trace("Command from control panel device ({}): {}", p.getSource(), p);
                                } else if (!unregistered.contains(p.getSource())) { // if not yet seen, print out log
                                                                                    // message once
                                    logger.info("Command from unregistered device ({}): {}", p.getSource(), p);
                                    unregistered.add(p.getSource());
                                } else {
                                    logger.trace("Command from unregistered device ({}): {}", p.getSource(), p);
                                }
                                break;
                            }

                            thinghandler = (PentairBaseThingHandler) thing.getHandler();
                            if (thinghandler == null) {
                                logger.debug("Thing handler = null");
                                break;
                            }

                            logger.trace("Received pentair command: {}", p);

                            thinghandler.processPacketFrom(p);

                            break;
                        case CMD_INTELLICHLOR:
                            parserstate = ParserState.WAIT_SOC;

                            buf[0] = 0x10; // 0x10 is included in checksum
                            if (c != (byte) 0x02) {
                                break;
                            }

                            buf[1] = 0x2;
                            length = 3;
                            // assume 3 byte command, plus 1 checksum, plus 0x10, 0x03
                            if (getBytes(buf, 2, 6) != 6) {
                                break;
                            }

                            // Check to see if this is a 3 or 4 byte command
                            if ((buf[6] != (byte) 0x10 || buf[7] != (byte) 0x03)) {
                                length = 4;

                                buf[8] = (byte) getByte(parserstate);
                                if ((buf[7] != (byte) 0x10) && (buf[8] != (byte) 0x03)) {
                                    logger.debug("Invalid Intellichlor command: {}",
                                            PentairPacket.bytesToHex(buf, length + 6));
                                    break; // invalid command
                                }
                            }

                            chksum = 0;
                            for (i = 0; i < length + 2; i++) {
                                chksum += buf[i] & 0xFF;
                            }

                            c = buf[length + 2] & 0xFF;
                            if (c != (chksum & 0xFF)) { // make sure it matches chksum
                                logger.debug("Invalid Intellichlor checksum: {}",
                                        PentairPacket.bytesToHex(buf, length + 6));
                                break;
                            }

                            PentairPacketIntellichlor pic = new PentairPacketIntellichlor(buf, length);

                            thing = findThing(0);

                            if (thing == null) {
                                if (!unregistered.contains(0)) { // if not yet seen, print out log message
                                    logger.info("Command from unregistered Intelliflow: {}", pic);
                                    unregistered.add(0);
                                } else {
                                    logger.trace("Command from unregistered Intelliflow: {}", pic);
                                }

                                break;
                            }

                            thinghandler = (PentairBaseThingHandler) thing.getHandler();
                            if (thinghandler == null) {
                                logger.debug("Thing handler = null");
                                break;
                            }

                            thinghandler.processPacketFrom(pic);

                            break;
                    }
                }
            } catch (IOException e) {
                logger.trace("I/O error while reading from stream: {}", e.getMessage());
                disconnect();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } catch (EOBException e) {
                // EOB Exception is used to exit the parser loop if full message is not in buffer.
            }

            logger.debug("msg reader thread exited");
        }
    }

    /**
     * Method to write a package on the Pentair bus. Will add preamble and checksum to bytes written
     *
     * @param p {@link PentairPacket} to write
     */
    public void writePacket(PentairPacket p) {
        try { // FF 00 FF A5 00 60 10 07 00 01 1C
            byte[] preamble = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0xFF };
            byte[] buf = new byte[5 + p.getLength() + 8]; // 5 is preamble, 8 is 6 bytes for header and 2 for checksum

            p.setSource(id);

            System.arraycopy(preamble, 0, buf, 0, 5);
            System.arraycopy(p.buf, 0, buf, 5, p.getLength() + 6);
            int checksum = p.calcChecksum();

            buf[p.getLength() + 11] = (byte) ((checksum >> 8) & 0xFF);
            buf[p.getLength() + 12] = (byte) (checksum & 0xFF);

            logger.debug("Writing packet: {}", PentairPacket.bytesToHex(buf));

            writer.write(buf, 0, 5 + p.getLength() + 8);
            writer.flush();
        } catch (IOException e) {
            logger.trace("I/O error while writing stream: {}", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
