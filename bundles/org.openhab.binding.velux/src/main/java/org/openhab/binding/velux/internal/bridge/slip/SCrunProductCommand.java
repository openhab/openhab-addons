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
import org.openhab.binding.velux.internal.bridge.common.RunProductCommand;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Send Command to Actuator</B>
 * <P>
 * Common Message semantic: Communication with the bridge and (optionally) storing returned information within the class
 * itself.
 * <P>
 * As 3rd level class it defines informations how to send query and receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the interface {@link org.openhab.binding.velux.internal.bridge.slip.SlipBridgeCommunicationProtocol
 * SlipBridgeCommunicationProtocol}.
 * <P>
 * Methods in addition to the mentioned interface:
 * <UL>
 * <LI>{@link #setNodeAndMainParameter} to define the node and intended parameter value.</LI>
 * </UL>
 *
 * @see RunProductCommand
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SCrunProductCommand extends RunProductCommand implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCrunProductCommand.class);

    private static final String DESCRIPTION = "Send Command to Actuator";
    private static final Command COMMAND = Command.GW_COMMAND_SEND_REQ;

    /*
     * ===========================================================
     * Message Content Parameters
     */

    private int reqSessionID = 0;
    private int reqCommandOriginator = 8; // SAAC
    private int reqPriorityLevel = 5; // Comfort Level 2
    private int reqParameterActive = 0; // Main Parameter
    private int reqFPI1 = 0; // Functional Parameter Indicator 1 set of bits
    private int reqFPI2 = 0; // Functional Parameter Indicator 2 set of bits
    private int reqMainParameter = 0; // for FunctionalParameterValueArray
    private int reqIndexArrayCount = 1; // One node will be addressed
    private int reqIndexArray01 = 1; // This is the node
    private int reqPriorityLevelLock = 0; // Do not set a new lock on priority level
    private int reqPL03 = 0; // unused
    private int reqPL47 = 0; // unused
    private int reqLockTime = 0; // 30 seconds

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

    /*
     * ===========================================================
     * Constructor Method
     */

    public SCrunProductCommand() {
        logger.debug("SCgetProduct(Constructor) called.");
        Random rand = new Random();
        reqSessionID = rand.nextInt(0x0fff);
        logger.debug("SCgetProduct(): starting sessions with the random number {}.", reqSessionID);
    }

    /*
     * ===========================================================
     * Methods required for interface {@link BridgeCommunicationProtocol}.
     */

    @Override
    public String name() {
        return DESCRIPTION;
    }

    @Override
    public CommandNumber getRequestCommand() {
        success = false;
        finished = false;
        logger.debug("getRequestCommand() returns {}.", COMMAND.getCommand());
        return COMMAND.getCommand();
    }

    @Override
    public byte[] getRequestDataAsArrayOfBytes() {
        Packet request = new Packet(new byte[66]);
        reqSessionID = (reqSessionID + 1) & 0xffff;
        request.setTwoByteValue(0, reqSessionID);
        request.setOneByteValue(2, reqCommandOriginator);
        request.setOneByteValue(3, reqPriorityLevel);
        request.setOneByteValue(4, reqParameterActive);
        request.setOneByteValue(5, reqFPI1);
        request.setOneByteValue(6, reqFPI2);
        request.setTwoByteValue(7, reqMainParameter);
        request.setOneByteValue(41, reqIndexArrayCount);
        request.setOneByteValue(42, reqIndexArray01);
        request.setOneByteValue(62, reqPriorityLevelLock);
        request.setOneByteValue(63, reqPL03);
        request.setOneByteValue(64, reqPL47);
        request.setOneByteValue(65, reqLockTime);
        logger.trace("getRequestDataAsArrayOfBytes(): ntfSessionID={}.", reqSessionID);
        logger.trace("getRequestDataAsArrayOfBytes(): reqCommandOriginator={}.", reqCommandOriginator);
        logger.trace("getRequestDataAsArrayOfBytes(): reqPriorityLevel={}.", reqPriorityLevel);
        logger.trace("getRequestDataAsArrayOfBytes(): reqParameterActive={}.", reqParameterActive);
        logger.trace("getRequestDataAsArrayOfBytes(): reqFPI1={}.", reqFPI1);
        logger.trace("getRequestDataAsArrayOfBytes(): reqFPI2={}.", reqFPI2);
        logger.trace("getRequestDataAsArrayOfBytes(): reqMainParameter={}.", reqMainParameter);
        logger.trace("getRequestDataAsArrayOfBytes(): reqIndexArrayCount={}.", reqIndexArrayCount);
        logger.trace("getRequestDataAsArrayOfBytes(): reqIndexArray01={}.", reqIndexArray01);
        logger.trace("getRequestDataAsArrayOfBytes(): reqPriorityLevelLock={}.", reqPriorityLevelLock);
        logger.trace("getRequestDataAsArrayOfBytes(): reqPL03={}.", reqPL03);
        logger.trace("getRequestDataAsArrayOfBytes(): reqPL47={}.", reqPL47);
        logger.trace("getRequestDataAsArrayOfBytes(): reqLockTime={}.", reqLockTime);
        requestData = request.toByteArray();
        logger.trace("getRequestDataAsArrayOfBytes() data is {}.", new Packet(requestData).toString());
        return requestData;
    }

    @Override
    public void setResponse(short responseCommand, byte[] thisResponseData, boolean isSequentialEnforced) {
        KLF200Response.introLogging(logger, responseCommand, thisResponseData);
        success = false;
        finished = false;
        Packet responseData = new Packet(thisResponseData);
        switch (Command.get(responseCommand)) {
            case GW_COMMAND_SEND_CFM:
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
                        } else if (!isSequentialEnforced) {
                            logger.trace(
                                    "setResponse(): skipping wait for more packets as sequential processing is not enforced.");
                            finished = true;
                            success = true;
                        }
                        break;
                    default:
                        logger.warn("setResponse(): returned status={} (not defined).", cfmStatus);
                        finished = true;
                        break;
                }
                break;

            case GW_COMMAND_RUN_STATUS_NTF:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 13)) {
                    finished = true;
                    break;
                }
                int ntfSessionID = responseData.getTwoByteValue(0);
                int ntfStatusiD = responseData.getOneByteValue(2);
                int ntfIndex = responseData.getOneByteValue(3);
                int ntfNodeParameter = responseData.getOneByteValue(4);
                int ntfParameterValue = responseData.getTwoByteValue(5);
                int ntfRunStatus = responseData.getOneByteValue(7);
                int ntfStatusReply = responseData.getOneByteValue(8);
                int ntfInformationCode = responseData.getFourByteValue(9);
                // Extracting information items
                logger.debug("setResponse(): ntfSessionID={} (requested {}).", ntfSessionID, reqSessionID);
                logger.debug("setResponse(): ntfStatusiD={}.", ntfStatusiD);
                logger.debug("setResponse(): ntfIndex={}.", ntfIndex);
                logger.debug("setResponse(): ntfNodeParameter={}.", ntfNodeParameter);
                logger.debug("setResponse(): ntfParameterValue={}.", ntfParameterValue);
                logger.debug("setResponse(): ntfRunStatus={}.", ntfRunStatus);
                logger.debug("setResponse(): ntfStatusReply={}.", ntfStatusReply);
                logger.debug("setResponse(): ntfInformationCode={}.", ntfInformationCode);

                if (!KLF200Response.check4matchingSessionID(logger, ntfSessionID, reqSessionID)) {
                    finished = true;
                }
                switch (ntfRunStatus) {
                    case 0:
                        logger.debug("setResponse(): returned ntfRunStatus: EXECUTION_COMPLETED.");
                        success = true;
                        break;
                    case 1:
                        logger.info("setResponse(): returned ntfRunStatus: EXECUTION_FAILED.");
                        finished = true;
                        break;
                    case 2:
                        logger.debug("setResponse(): returned ntfRunStatus: EXECUTION_ACTIVE.");
                        break;
                    default:
                        logger.warn("setResponse(): returned ntfRunStatus={} (not defined).", ntfRunStatus);
                        finished = true;
                        break;
                }
                if (!isSequentialEnforced) {
                    logger.trace(
                            "setResponse(): skipping wait for more packets as sequential processing is not enforced.");
                    success = true;
                    finished = true;
                }
                break;

            case GW_COMMAND_REMAINING_TIME_NTF:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 6)) {
                    finished = true;
                    break;
                }
                int timeNtfSessionID = responseData.getTwoByteValue(0);
                int timeNtfIndex = responseData.getOneByteValue(2);
                int timeNtfNodeParameter = responseData.getOneByteValue(3);
                int timeNtfSeconds = responseData.getTwoByteValue(4);

                if (!KLF200Response.check4matchingSessionID(logger, timeNtfSessionID, reqSessionID)) {
                    finished = true;
                }

                // Extracting information items
                logger.debug("setResponse(): timeNtfSessionID={}.", timeNtfSessionID);
                logger.debug("setResponse(): timeNtfIndex={}.", timeNtfIndex);
                logger.debug("setResponse(): timeNtfNodeParameter={}.", timeNtfNodeParameter);
                logger.debug("setResponse(): timeNtfSeconds={}.", timeNtfSeconds);
                if (!isSequentialEnforced) {
                    logger.trace(
                            "setResponse(): skipping wait for more packets as sequential processing is not enforced.");
                    success = true;
                    finished = true;
                }
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
                finished = true;
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

    /*
     * ===========================================================
     * Methods in addition to the interface {@link BridgeCommunicationProtocol}
     * and the abstract class {@link RunProductCommand}
     */

    @Override
    public SCrunProductCommand setNodeAndMainParameter(int nodeId, int value) {
        logger.debug("setNodeAndMainParameter({}) called.", nodeId);
        this.reqIndexArray01 = nodeId;
        this.reqMainParameter = value;
        return this;
    }
}
