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
package org.openhab.binding.velux.internal.bridge.slip;

import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.bridge.common.BridgeCommunicationProtocol;
import org.openhab.binding.velux.internal.bridge.common.GetStatus;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Retrieve Product Status</B>
 * <P>
 * Common Message semantic: Communication from the bridge and storing returned information within the class itself.
 * <P>
 * As 3rd level class it defines informations how to receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the interface {@link org.openhab.binding.velux.internal.bridge.slip.SlipBridgeCommunicationProtocol
 * SlipBridgeCommunicationProtocol}.
 * <P>
 *
 * @see BridgeCommunicationProtocol
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
class SCgetStatus extends GetStatus implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCgetStatus.class);

    private static final String DESCRIPTION = "Retrieve Product Status";
    private static final Command COMMAND = Command.GW_STATUS_REQUEST_REQ;

    /*
     * ===========================================================
     * Message Content Parameters
     */

    private int reqSessionID = 0; // The session id
    private final int reqIndexArrayCount = 1; // One node will be addressed
    private int reqNodeId = 1; // This is the node id
    private final int reqStatusType = 1; // The current value
    private final int reqFPI1 = 0xF0; // Functional Parameter Indicator 1 bit map.
    private final int reqFPI2 = 0; // Functional Parameter Indicator 2 bit map.
    private final FunctionalParameters reqFunctionalParameters = new FunctionalParameters();

    /*
     * ===========================================================
     * Message Objects
     */

    private byte[] requestData = new byte[0];

    /*
     * ===========================================================
     * Result Objects
     */

    private boolean success = false;
    private boolean finished = false;

    private int ntfNodeId;
    private int ntfCurrentPosition;
    private int ntfState;
    private final FunctionalParameters ntfFunctionalParameters = new FunctionalParameters();

    public SCgetStatus() {
        logger.debug("SCgetStatus(Constructor) called.");
        Random rand = new Random();
        reqSessionID = rand.nextInt(0x0fff);
        logger.debug("SCgetStatus(): starting session with the random number {}.", reqSessionID);
    }

    @Override
    public String name() {
        return DESCRIPTION;
    }

    @Override
    public CommandNumber getRequestCommand() {
        success = false;
        finished = false;
        logger.debug("getRequestCommand() returns {} ({}).", COMMAND.name(), COMMAND.getCommand());
        return COMMAND.getCommand();
    }

    @Override
    public byte[] getRequestDataAsArrayOfBytes() {
        logger.trace("getRequestDataAsArrayOfBytes() returns data for retrieving node with id {}.", reqNodeId);
        reqSessionID = (reqSessionID + 1) & 0xffff;
        Packet request = new Packet(new byte[26]);
        request.setTwoByteValue(0, reqSessionID);
        request.setOneByteValue(2, reqIndexArrayCount);
        request.setTwoByteValue(3, reqNodeId);
        request.setOneByteValue(23, reqStatusType);
        request.setOneByteValue(24, reqFPI1);
        request.setOneByteValue(25, reqFPI2);
        requestData = request.toByteArray();
        return requestData;
    }

    @Override
    public void setResponse(short responseCommand, byte[] thisResponseData, boolean isSequentialEnforced) {
        KLF200Response.introLogging(logger, responseCommand, thisResponseData);
        success = false;
        finished = false;
        Packet responseData = new Packet(thisResponseData);
        switch (Command.get(responseCommand)) {
            case GW_STATUS_REQUEST_CFM:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 3)) {
                    finished = true;
                    break;
                }
                int cfmSessionID = responseData.getTwoByteValue(0);
                int cfmStatus = responseData.getOneByteValue(2);
                switch (cfmStatus) {
                    case 0:
                        logger.info("setResponse(): returned status: Error – Command rejected.");
                        finished = true;
                        break;
                    case 1:
                        logger.debug("setResponse(): returned status: OK - Command is accepted.");
                        if (!KLF200Response.check4matchingSessionID(logger, cfmSessionID, reqSessionID)) {
                            finished = true;
                        }
                        break;
                    default:
                        logger.warn("setResponse(): returned status={} (not defined).", cfmStatus);
                        finished = true;
                        break;
                }
                break;

            case GW_STATUS_REQUEST_NTF:
                finished = true;
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 59)) {
                    break;
                }

                // Extracting information items
                int ntfSessionID = responseData.getTwoByteValue(0);
                int ntfStatusID = responseData.getOneByteValue(2);
                ntfNodeId = responseData.getOneByteValue(3);
                int ntfRunStatus = responseData.getOneByteValue(4);
                int ntfStatusReply = responseData.getOneByteValue(5);
                int ntfStatusType = responseData.getOneByteValue(6);
                int ntfStatusCount = responseData.getOneByteValue(7);
                ntfCurrentPosition = responseData.getTwoByteValue(8);
                ntfFunctionalParameters.read(responseData, 10);

                if (logger.isTraceEnabled()) {
                    logger.trace("setResponse(): ntfSessionID={}.", ntfSessionID);
                    logger.trace("setResponse(): ntfStatusID={}.", ntfStatusID);
                    logger.trace("setResponse(): ntfNodeIndex={}.", ntfNodeId);
                    logger.trace("setResponse(): ntfRunStatus={}.", ntfRunStatus);
                    logger.trace("setResponse(): ntfStatusReply={}.", ntfStatusReply);
                    logger.trace("setResponse(): ntfStatusType={}.", ntfStatusType);
                    logger.trace("setResponse(): ntfStatusCount={}.", ntfStatusCount);
                    logger.trace("setResponse(): ntfMainParameter={}.", ntfCurrentPosition);
                    logger.trace("setResponse(): ntfFunctionalParameters={}.", ntfFunctionalParameters);
                }

                if (!KLF200Response.check4matchingNodeID(logger, reqNodeId, ntfNodeId)) {
                    break;
                }

                switch (ntfRunStatus) {
                    case 1:
                        ntfState = 1;
                        break;
                    case 2:
                        ntfState = 4;
                        break;
                    default:
                        ntfState = 2;
                }

                reqFunctionalParameters.setValues(ntfFunctionalParameters);
                success = true;
                break;

            case GW_SESSION_FINISHED_NTF:
                finished = true;
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 2)) {
                    break;
                }
                int finishedNtfSessionID = responseData.getTwoByteValue(0);
                if (!KLF200Response.check4matchingSessionID(logger, finishedNtfSessionID, reqSessionID)) {
                    break;
                }
                logger.debug("setResponse(): finishedNtfSessionID={}.", finishedNtfSessionID);
                success = true;
                break;

            default:
                KLF200Response.errorLogging(logger, responseCommand);
        }
        KLF200Response.outroLogging(logger, success, finished);
    }

    @Override
    public boolean isCommunicationFinished() {
        return finished;
    }

    @Override
    public boolean isCommunicationSuccessful() {
        return success;
    }

    @Override
    public void setProductId(int nodeId) {
        logger.trace("setProductId({}) called.", nodeId);
        reqNodeId = nodeId;
        return;
    }

    @Override
    public FunctionalParameters getFunctionalParameters() {
        return ntfFunctionalParameters.clone();
    }

    @Override
    public int getNodeId() {
        return ntfNodeId;
    }

    @Override
    public int getCurrentPosition() {
        return ntfCurrentPosition;
    }

    @Override
    public int getState() {
        return ntfState;
    }
}
