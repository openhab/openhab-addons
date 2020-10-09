/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.text.ParseException;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.bridge.VeluxBridge;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeInstance;
import org.openhab.binding.velux.internal.bridge.common.BridgeAPI;
import org.openhab.binding.velux.internal.bridge.common.BridgeCommunicationProtocol;
import org.openhab.binding.velux.internal.bridge.slip.io.Connection;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.bridge.slip.utils.SlipEncoding;
import org.openhab.binding.velux.internal.bridge.slip.utils.SlipRFC1055;
import org.openhab.binding.velux.internal.development.Threads;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SLIP-based 2nd Level I/O interface towards the <B>Velux</B> bridge.
 * <P>
 * It provides methods for pre- and postcommunication
 * as well as a common method for the real communication.
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
 */
@NonNullByDefault
public class SlipVeluxBridge extends VeluxBridge {
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
    public SlipVeluxBridge(VeluxBridgeInstance bridgeInstance) {
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
        logger.trace("bridgeDirectCommunicate(BCP: {},{}authenticated) called.", communication.name(),
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
     * Initializes a client/server communication towards <b>Velux</b> veluxBridge
     * based on the Basic I/O interface {@link Connection#io} and parameters
     * passed as arguments (see below).
     *
     * @param communication Structure of interface type {@link SlipBridgeCommunicationProtocol} describing the
     *            intended communication, that is request and response interactions as well as appropriate URL
     *            definition.
     * @param useAuthentication boolean flag to decide whether to use authenticated communication.
     * @return <b>success</b> of type boolean which signals the success of the communication.
     */
    private synchronized boolean bridgeDirectCommunicate(SlipBridgeCommunicationProtocol communication,
            boolean useAuthentication) {
        String host = this.bridgeInstance.veluxBridgeConfiguration().ipAddress;
        logger.trace("bridgeDirectCommunicate({}, {}authenticated) on {} called.", communication.name(),
                useAuthentication ? "" : "un", host);

        assert this.bridgeInstance.veluxBridgeConfiguration().protocol.contentEquals("slip");

        long communicationStartInMSecs = System.currentTimeMillis();

        boolean isSequentialEnforced = this.bridgeInstance.veluxBridgeConfiguration().isSequentialEnforced;
        boolean isProtocolTraceEnabled = this.bridgeInstance.veluxBridgeConfiguration().isProtocolTraceEnabled;

        // From parameters
        short command = communication.getRequestCommand().toShort();
        byte[] data = communication.getRequestDataAsArrayOfBytes();
        // For further use at different logging statements
        String commandString = Command.get(command).toString();

        if (isProtocolTraceEnabled) {
            Threads.findDeadlocked();
        }

        logger.debug("bridgeDirectCommunicate({}, {}authenticated) on {} initiated by {}.", commandString,
                useAuthentication ? "" : "un", host, Thread.currentThread());
        boolean success = false;

        communication: do {
            if (communicationStartInMSecs + COMMUNICATION_TIMEOUT_MSECS < System.currentTimeMillis()) {
                logger.warn(
                        "{} bridgeDirectCommunicate({}) on {}: communication handshake failed (unexpected sequence of requests/responses).",
                        VeluxBindingConstants.BINDING_VALUES_SEPARATOR, communication.name(), host);
                break;
            }

            // Special handling
            if (Command.get(command) == Command.GW_OPENHAB_CLOSE) {
                logger.trace("bridgeDirectCommunicate() on {}: special command: shutting down connection.", host);
                connection.resetConnection();
                success = true;
                continue;
            }

            // Normal processing
            logger.trace("bridgeDirectCommunicate() on {}: working on request {} with {} bytes of data.", host,
                    commandString, data.length);
            byte[] sendBytes = emptyPacket;
            if (Command.get(command) == Command.GW_OPENHAB_RECEIVEONLY) {
                logger.trace(
                        "bridgeDirectCommunicate() on {}: special command: determine whether there is any message waiting.",
                        host);
                logger.trace("bridgeDirectCommunicate(): check for a waiting message.");
                if (!connection.isMessageAvailable()) {
                    logger.trace("bridgeDirectCommunicate() on {}: no message waiting, aborting.", host);
                    break communication;
                }
                logger.trace("bridgeDirectCommunicate() on {}: there is a message waiting.", host);
            } else {
                SlipEncoding t = new SlipEncoding(command, data);
                if (!t.isValid()) {
                    logger.warn("bridgeDirectCommunicate() on {}: SlipEncoding() failed, aborting.", host);
                    break;
                }
                logger.trace("bridgeDirectCommunicate() on {}: transportEncoding={}.", host, t.toString());
                sendBytes = new SlipRFC1055().encode(t.toMessage());
            }
            do {
                if (communicationStartInMSecs + COMMUNICATION_TIMEOUT_MSECS < System.currentTimeMillis()) {
                    logger.warn("bridgeDirectCommunicate() on {}: receive takes too long. Please report to maintainer.",
                            host);
                    break communication;
                }
                byte[] receivedPacket;
                try {
                    if (sendBytes.length > 0) {
                        logger.trace("bridgeDirectCommunicate() on {}: sending {} bytes.", host, sendBytes.length);
                        if (isProtocolTraceEnabled) {
                            logger.info("Sending command {}.", commandString);
                        }
                    } else {
                        logger.trace("bridgeDirectCommunicate() on {}: initiating receive-only.", host);
                    }
                    // (Optionally) Send and receive packet.
                    receivedPacket = connection.io(this.bridgeInstance, sendBytes);
                    // Once being sent, it should never be sent again
                    sendBytes = emptyPacket;
                } catch (Exception e) {
                    logger.warn("bridgeDirectCommunicate() on {}: connection.io returns {}", host, e.getMessage());
                    break communication;
                }
                logger.trace("bridgeDirectCommunicate() on {}: received packet {}.", host,
                        new Packet(receivedPacket).toString());
                byte[] response;
                try {
                    response = new SlipRFC1055().decode(receivedPacket);
                } catch (ParseException e) {
                    logger.warn("bridgeDirectCommunicate() on {}: method SlipRFC1055() raised a decoding error: {}.",
                            host, e.getMessage());
                    break communication;
                }
                SlipEncoding tr = new SlipEncoding(response);
                if (!tr.isValid()) {
                    logger.warn("bridgeDirectCommunicate() on {}: method SlipEncoding() raised a decoding error.",
                            host);
                    break communication;
                }
                short responseCommand = tr.getCommand();
                byte[] responseData = tr.getData();
                logger.debug("bridgeDirectCommunicate() on {}: working on response {} with {} bytes of data.", host,
                        Command.get(responseCommand).toString(), responseData.length);
                if (isProtocolTraceEnabled) {
                    logger.info("Received answer {}.", Command.get(responseCommand).toString());
                }
                // Handle some common (unexpected) answers
                switch (Command.get(responseCommand)) {
                    case GW_NODE_INFORMATION_CHANGED_NTF:
                        logger.trace("bridgeDirectCommunicate() on {}: received GW_NODE_INFORMATION_CHANGED_NTF.",
                                host);
                        logger.trace("bridgeDirectCommunicate() on {}: continue with receiving.", host);
                        continue;
                    case GW_NODE_STATE_POSITION_CHANGED_NTF:
                        logger.trace(
                                "bridgeDirectCommunicate() on {}: received GW_NODE_STATE_POSITION_CHANGED_NTF, special processing of this packet.",
                                host);
                        SCgetHouseStatus receiver = new SCgetHouseStatus();
                        receiver.setResponse(responseCommand, responseData, isSequentialEnforced);
                        if (receiver.isCommunicationSuccessful()) {
                            logger.trace("bridgeDirectCommunicate() on {}: existingProducts().update() called.", host);
                            bridgeInstance.existingProducts().update(new ProductBridgeIndex(receiver.getNtfNodeID()),
                                    receiver.getNtfState(), receiver.getNtfCurrentPosition(), receiver.getNtfTarget());
                        }
                        logger.trace("bridgeDirectCommunicate() on {}: continue with receiving.", host);
                        continue;
                    case GW_ERROR_NTF:
                        switch (responseData[0]) {
                            case 0:
                                logger.warn(
                                        "bridgeDirectCommunicate() on {}: received GW_ERROR_NTF on {} (Not further defined error), aborting.",
                                        host, commandString);
                                break communication;
                            case 1:
                                logger.warn(
                                        "bridgeDirectCommunicate() on {}: received GW_ERROR_NTF (Unknown Command or command is not accepted at this state) on {}, aborting.",
                                        host, commandString);
                                break communication;
                            case 2:
                                logger.warn(
                                        "bridgeDirectCommunicate() on {}: received GW_ERROR_NTF (ERROR on Frame Structure) on {}, aborting.",
                                        host, commandString);
                                break communication;
                            case 7:
                                logger.trace(
                                        "bridgeDirectCommunicate() on {}: received GW_ERROR_NTF (Busy. Try again later) on {}, retrying.",
                                        host, commandString);
                                sendBytes = emptyPacket;
                                continue;
                            case 8:
                                logger.warn(
                                        "bridgeDirectCommunicate() on {}: received GW_ERROR_NTF (Bad system table index) on {}, aborting.",
                                        host, commandString);
                                break communication;
                            case 12:
                                logger.warn(
                                        "bridgeDirectCommunicate() on {}: received GW_ERROR_NTF (Not authenticated) on {}, aborting.",
                                        host, commandString);
                                resetAuthentication();
                                break communication;
                            default:
                                logger.warn(
                                        "bridgeDirectCommunicate() on {}: received GW_ERROR_NTF ({}) on {}, aborting.",
                                        host, responseData[0], commandString);
                                break communication;
                        }
                    case GW_ACTIVATION_LOG_UPDATED_NTF:
                        logger.info("bridgeDirectCommunicate() on {}: received GW_ACTIVATION_LOG_UPDATED_NTF.", host);
                        logger.trace("bridgeDirectCommunicate() on {}: continue with receiving.", host);
                        continue;

                    case GW_COMMAND_RUN_STATUS_NTF:
                    case GW_COMMAND_REMAINING_TIME_NTF:
                    case GW_SESSION_FINISHED_NTF:
                        if (!isSequentialEnforced) {
                            logger.trace(
                                    "bridgeDirectCommunicate() on {}: response ignored due to activated parallelism, continue with receiving.",
                                    host);
                            continue;
                        }

                    default:
                }
                logger.trace("bridgeDirectCommunicate() on {}: passes back command {} and data {}.", host,
                        new CommandNumber(responseCommand).toString(), new Packet(responseData).toString());
                communication.setResponse(responseCommand, responseData, isSequentialEnforced);
            } while (!communication.isCommunicationFinished());
            success = communication.isCommunicationSuccessful();
        } while (false); // communication
        logger.debug("bridgeDirectCommunicate({}) on {}: returns {}.", commandString, host,
                success ? "success" : "failure");
        return success;
    }
}
