/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal.bridge.slip;

import java.io.Closeable;
import java.io.IOException;
import java.text.ParseException;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.bridge.VeluxBridge;
import org.openhab.binding.velux.internal.bridge.common.BridgeAPI;
import org.openhab.binding.velux.internal.bridge.common.BridgeCommunicationProtocol;
import org.openhab.binding.velux.internal.bridge.slip.io.Connection;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.bridge.slip.utils.SlipEncoding;
import org.openhab.binding.velux.internal.bridge.slip.utils.SlipRFC1055;
import org.openhab.binding.velux.internal.development.Threads;
import org.openhab.binding.velux.internal.handler.VeluxBridgeHandler;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SLIP-based 2nd Level I/O interface towards the <B>Velux</B> bridge.
 * <P>
 * It provides methods for pre- and post- communication as well as a common method for the real communication.
 * <P>
 * In addition to the generic {@link VeluxBridge} methods, i.e.
 * <UL>
 * <LI>{@link VeluxBridge#bridgeLogin} for pre-communication,</LI>
 * <LI>{@link VeluxBridge#bridgeLogout} for post-communication,</LI>
 * <LI>{@link VeluxBridge#bridgeCommunicate} as method for the common communication,</LI>
 * <LI>{@link VeluxBridge#bridgeAPI} as interfacing method to all interaction prototypes,</LI>
 * </UL>
 * the following class access methods provides the protocol-specific implementation:
 * <UL>
 * <LI>{@link #bridgeDirectCommunicate} for SLIP-based communication.</LI>
 * <LI>{@link #bridgeAPI} for return all defined protocol-specific implementations which are provided by
 * {@link org.openhab.binding.velux.internal.bridge.slip.SlipBridgeAPI SlipBridgeAPI}.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution.
 * @author Andrew Fiddian-Green - Refactored (simplified) the message processing loop
 */
@NonNullByDefault
public class SlipVeluxBridge extends VeluxBridge implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(SlipVeluxBridge.class);

    /*
     * ***************************
     * ***** Private Objects *****
     */

    /**
     * Timeout for sequence of one request with (optional multiple) responses.
     * <P>
     * This can only happen if there is an unexpected (undocumented) occurrence of events
     * which has to be discussed along the Velux API documentation.
     */
    static final long COMMUNICATION_TIMEOUT_MSECS = 60000L;
    /**
     * Wait interval within sequence after the request and no immediate response.
     * <P>
     * This can happen if the bridge is busy due to a specific command to query and collect information from other
     * devices via the io-homecontrol protocol.
     */
    static final long COMMUNICATION_RETRY_MSECS = 5000L;

    /*
     * ***************************
     * ***** Private Objects *****
     */

    private final byte[] emptyPacket = new byte[0];
    private Connection connection = new Connection();

    /**
     * Handler passing the interface methods to other classes.
     * Can be accessed via method {@link org.openhab.binding.velux.internal.bridge.common.BridgeAPI BridgeAPI}.
     */
    private BridgeAPI bridgeAPI;

    /**
     * Constructor.
     * <P>
     * Inherits the initialization of the binding-wide instance for dealing for common informations and
     * initializes the Velux bridge connection settings.
     *
     * @param bridgeInstance refers to the binding-wide instance for dealing for common informations.
     */
    public SlipVeluxBridge(VeluxBridgeHandler bridgeInstance) {
        super(bridgeInstance);
        logger.trace("SlipVeluxBridge(constructor) called.");
        bridgeAPI = new SlipBridgeAPI(bridgeInstance);
        supportedProtocols = new TreeSet<>();
        supportedProtocols.add("slip");
        logger.trace("SlipVeluxBridge(constructor) done.");
    }

    /**
     * Destructor.
     * <P>
     * De-initializes the binding-wide instance.
     */
    @Override
    public void shutdown() {
        logger.trace("shutdown() called.");
        connection.resetConnection();
        logger.trace("shutdown() done.");
    }

    /**
     * Provides information about the base-level communication method and
     * any kind of available gateway interactions.
     * <P>
     * <B>Note:</B> the implementation within this class {@link SlipVeluxBridge} as inherited from {@link VeluxBridge}
     * will return the protocol-specific class implementations.
     * <P>
     * The information will be initialized by the corresponding API class {@link SlipBridgeAPI}.
     *
     * @return <b>bridgeAPI</b> of type {@link BridgeAPI} contains all possible methods.
     */
    @Override
    public BridgeAPI bridgeAPI() {
        logger.trace("bridgeAPI() called.");
        return bridgeAPI;
    }

    /**
     * <B>Method as implementation of abstract superclass method.</B>
     * <P>
     * Initializes a client/server communication towards <b>Velux</b> veluxBridge
     * based on the Basic I/O interface {@link Connection#io} and parameters
     * passed as arguments (see below).
     *
     * @param communication Structure of interface type {@link SlipBridgeCommunicationProtocol} describing the intended
     *            communication, that is request and response interactions as well as appropriate URL definition.
     * @param useAuthentication boolean flag to decide whether to use authenticated communication.
     * @return <b>success</b> of type boolean which signals the success of the communication.
     *
     */
    @Override
    protected boolean bridgeDirectCommunicate(BridgeCommunicationProtocol communication, boolean useAuthentication) {
        logger.trace("bridgeDirectCommunicate(BCP: {}, {}authenticated) called.", communication.name(),
                useAuthentication ? "" : "un");
        return bridgeDirectCommunicate((SlipBridgeCommunicationProtocol) communication, useAuthentication);
    }

    /**
     * Returns the timestamp in milliseconds since Unix epoch
     * of last (potentially faulty) communication.
     *
     * @return timestamp in milliseconds.
     */
    @Override
    public long lastCommunication() {
        return connection.lastCommunication();
    }

    /**
     * Returns the timestamp in milliseconds since Unix epoch
     * of last successful communication.
     *
     * @return timestamp in milliseconds.
     */
    @Override
    public long lastSuccessfulCommunication() {
        return connection.lastSuccessfulCommunication();
    }

    /**
     * Initializes a client/server communication towards the Velux Bridge based on the Basic I/O interface
     * {@link Connection#io} and parameters passed as arguments (see below).
     *
     * @param communication a structure of interface type {@link SlipBridgeCommunicationProtocol} describing the
     *            intended communication, that is request and response interactions as well as appropriate URL
     *            definition.
     * @param useAuthentication a boolean flag to select whether to use authenticated communication.
     * @return a boolean which in general signals the success of the communication, but in the
     *         special case of receive-only calls, signals if any products were updated during the call
     */
    private synchronized boolean bridgeDirectCommunicate(SlipBridgeCommunicationProtocol communication,
            boolean useAuthentication) {
        logger.trace("bridgeDirectCommunicate() '{}', {}authenticated", communication.name(),
                useAuthentication ? "" : "un");

        // store common parameters as constants for frequent use
        final short txCmd = communication.getRequestCommand().toShort();
        final byte[] txData = communication.getRequestDataAsArrayOfBytes();
        final Command txEnum = Command.get(txCmd);
        final String txName = txEnum.toString();
        final boolean isSequentialEnforced = this.bridgeInstance.veluxBridgeConfiguration().isSequentialEnforced;
        final boolean isProtocolTraceEnabled = this.bridgeInstance.veluxBridgeConfiguration().isProtocolTraceEnabled;
        final long expiryTime = System.currentTimeMillis() + COMMUNICATION_TIMEOUT_MSECS;

        // logger messages
        final String logMsg = "bridgeDirectCommunicate() [{}] {} => {} {} {}";
        final String ipAddr = bridgeInstance.veluxBridgeConfiguration().ipAddress;

        if (isProtocolTraceEnabled) {
            Threads.findDeadlocked();
        }

        logger.debug(logMsg, ipAddr, txName, "started =>", Thread.currentThread(), "");

        boolean looping = false;
        boolean success = false;
        boolean sending = false;
        boolean rcvonly = false;
        byte[] txPacket = emptyPacket;

        // handling of the requests
        switch (txEnum) {
            case GW_OPENHAB_CLOSE:
                logger.trace(logMsg, ipAddr, txName, "shut down command", "=> executing", "");
                connection.resetConnection();
                success = true;
                break;

            case GW_OPENHAB_RECEIVEONLY:
                logger.trace(logMsg, ipAddr, txName, "receive-only mode", "=> checking messages", "");
                if (!connection.isAlive()) {
                    logger.trace(logMsg, ipAddr, txName, "no connection", "=> opening", "");
                    looping = true;
                } else if (connection.isMessageAvailable()) {
                    logger.trace(logMsg, ipAddr, txName, "message(s) waiting", "=> start reading", "");
                    looping = true;
                } else {
                    logger.trace(logMsg, ipAddr, txName, "no waiting messages", "=> done", "");
                }
                rcvonly = true;
                break;

            default:
                logger.trace(logMsg, ipAddr, txName, "send mode", "=> preparing command", "");
                SlipEncoding slipEnc = new SlipEncoding(txCmd, txData);
                if (!slipEnc.isValid()) {
                    logger.debug(logMsg, ipAddr, txName, "slip encoding error", "=> aborting", "");
                    break;
                }
                txPacket = new SlipRFC1055().encode(slipEnc.toMessage());
                logger.trace(logMsg, ipAddr, txName, "command ready", "=> start sending", "");
                looping = sending = true;
        }

        while (looping) {
            // timeout
            if (System.currentTimeMillis() > expiryTime) {
                logger.warn(logMsg, ipAddr, txName, "process loop time out", "=> aborting", "=> PLEASE REPORT !!");
                // abort the processing loop
                break;
            }

            // send command (optionally), and receive response
            byte[] rxPacket;
            try {
                if (sending) {
                    if (isProtocolTraceEnabled) {
                        logger.info("sending command {}", txName);
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace(logMsg, ipAddr, txName, txName, "=> sending data =>", new Packet(txData));
                    } else {
                        logger.debug(logMsg, ipAddr, txName, txName, "=> sending data length =>", txData.length);
                    }
                }
                rxPacket = connection.io(this.bridgeInstance, sending ? txPacket : emptyPacket);
                // message sent, don't send it again
                sending = false;
                if (rxPacket.length == 0) {
                    // only log in send mode (in receive-only mode, no response is ok)
                    if (!rcvonly) {
                        logger.debug(logMsg, ipAddr, txName, "no response", "=> aborting", "");
                    }
                    // abort the processing loop
                    break;
                }
            } catch (IOException e) {
                logger.debug(logMsg, ipAddr, txName, "i/o error =>", e.getMessage(), "=> aborting");
                // abort the processing loop
                break;
            }

            // RFC1055 decode response
            byte[] rfc1055;
            try {
                rfc1055 = new SlipRFC1055().decode(rxPacket);
            } catch (ParseException e) {
                logger.debug(logMsg, ipAddr, txName, "parsing error =>", e.getMessage(), "=> aborting");
                // abort the processing loop
                break;
            }

            // SLIP decode response
            SlipEncoding slipEnc = new SlipEncoding(rfc1055);
            if (!slipEnc.isValid()) {
                logger.debug(logMsg, ipAddr, txName, "slip decode error", "=> aborting", "");
                // abort the processing loop
                break;
            }

            // attributes of the received (rx) response
            final short rxCmd = slipEnc.getCommand();
            final byte[] rxData = slipEnc.getData();
            final Command rxEnum = Command.get(rxCmd);
            final String rxName = rxEnum.toString();

            // logging
            if (logger.isTraceEnabled()) {
                logger.trace(logMsg, ipAddr, txName, rxName, "=> received data =>", new Packet(rxData));
            } else {
                logger.debug(logMsg, ipAddr, txName, rxName, "=> received data length =>", rxData.length);
            }
            if (isProtocolTraceEnabled) {
                logger.info("received message {} => {}", rxName, new Packet(rxData));
            }

            // handling of the responses
            switch (rxEnum) {
                case GW_ERROR_NTF:
                    byte code = rxData[0];
                    switch (code) {
                        case 7: // busy
                            logger.trace(logMsg, ipAddr, txName, rxName, getErrorText(code), "=> retrying");
                            sending = true;
                            break;
                        case 12: // authentication failed
                            logger.debug(logMsg, ipAddr, txName, rxName, getErrorText(code), "=> aborting");
                            resetAuthentication();
                            looping = false;
                            break;
                        default:
                            logger.warn(logMsg, ipAddr, txName, rxName, getErrorText(code), "=> aborting");
                            looping = false;
                    }
                    break;

                case GW_NODE_INFORMATION_CHANGED_NTF:
                case GW_ACTIVATION_LOG_UPDATED_NTF:
                    logger.trace(logMsg, ipAddr, txName, rxName, "=> ignorable command", "=> continuing");
                    break;

                case GW_NODE_STATE_POSITION_CHANGED_NTF:
                    logger.trace(logMsg, ipAddr, txName, rxName, "=> special command", "=> starting");
                    SCgetHouseStatus receiver = new SCgetHouseStatus().setCreatorCommand(txEnum);
                    receiver.setResponse(rxCmd, rxData, isSequentialEnforced);
                    if (receiver.isCommunicationSuccessful()) {
                        bridgeInstance.existingProducts().update(receiver.getProduct());
                        logger.trace(logMsg, ipAddr, txName, rxName, "=> special command", "=> update submitted");
                        if (rcvonly) {
                            // receive-only: return success to confirm that product(s) were updated
                            success = true;
                        }
                    }
                    logger.trace(logMsg, ipAddr, txName, rxName, "=> special command", "=> continuing");
                    break;

                case GW_COMMAND_RUN_STATUS_NTF:
                case GW_COMMAND_REMAINING_TIME_NTF:
                case GW_SESSION_FINISHED_NTF:
                    if (!isSequentialEnforced) {
                        logger.trace(logMsg, ipAddr, txName, rxName, "=> parallelism allowed", "=> continuing");
                        break;
                    }
                    logger.trace(logMsg, ipAddr, txName, rxName, "=> serialism enforced", "=> default processing");
                    // fall through => execute default processing

                default:
                    logger.trace(logMsg, ipAddr, txName, rxName, "=> applying data length =>", rxData.length);
                    communication.setResponse(rxCmd, rxData, isSequentialEnforced);
                    looping = !communication.isCommunicationFinished();
                    success = communication.isCommunicationSuccessful();
            }

        }
        // in receive-only mode 'failure` just means that no products were updated, so don't log it as a failure..
        logger.debug(logMsg, ipAddr, txName, "finished", "=>", ((success || rcvonly) ? "success" : "failure"));
        return success;
    }

    /**
     * Return text description of potential GW_ERROR_NTF error codes, for logging purposes
     *
     * @param errCode is the GW_ERROR_NTF error code
     * @return the description message
     */
    private static String getErrorText(byte errCode) {
        switch (errCode) {
            case 0:
                return "=> (0) not further defined error";
            case 1:
                return "=> (1) unknown command or command is not accepted at this state";
            case 2:
                return "=> (2) error on frame structure";
            case 7:
                return "=> (7) busy, try again later";
            case 8:
                return "=> (8) bad system table index";
            case 12:
                return "=> (12) not authenticated";
        }
        return String.format("=> (%d) unknown error", errCode);
    }

    @Override
    public void close() throws IOException {
        shutdown();
    }
}
