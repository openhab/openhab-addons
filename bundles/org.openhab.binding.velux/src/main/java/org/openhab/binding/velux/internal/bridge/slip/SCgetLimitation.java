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
import org.openhab.binding.velux.internal.bridge.common.GetProductLimitation;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Retrieve Product Limitations</B>
 * <P>
 * Common Message semantic: Communication with the bridge and (optionally) storing returned information within the class
 * itself.
 * <P>
 * As 3rd level class it defines informations how to send query and receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the interface {@link SlipBridgeCommunicationProtocol}.
 * <P>
 * Methods in addition to the mentioned interface:
 * <UL>
 * <LI>{@link #setActuatorIdAndLimitationType(int,boolean)} to define the one specific product.</LI>
 * </UL>
 *
 * @see GetProductLimitation
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SCgetLimitation extends GetProductLimitation implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCgetLimitation.class);

    private static final String DESCRIPTION = "Retrieve Actuator Limitation";
    private static final Command COMMAND = Command.GW_GET_LIMITATION_STATUS_REQ;

    /*
     * ===========================================================
     * Message Content Parameters
     */

    private int reqSessionID = 0;
    private int reqIndexArrayCount = 1; // One node will be addressed
    private int reqIndexArray01 = 1; // This is the node
    private int reqParameterID = 0; // MP = Main parameter
    private int reqLimitationType = 0; // Resulting minimum limitation. 1= maximum.

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

    private int limitationValue = 0;

    /*
     * ===========================================================
     * Constructor Method
     */

    public SCgetLimitation() {
        logger.debug("SCgetLimitation(Constructor) called.");
        Random rand = new Random();
        reqSessionID = rand.nextInt(0x0fff);
        logger.debug("SCgetLimitation(): starting sessions with the random number {}.", reqSessionID);
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
        logger.debug("getRequestCommand() returns {} ({}).", COMMAND.name(), COMMAND.getCommand());
        return COMMAND.getCommand();
    }

    @Override
    public byte[] getRequestDataAsArrayOfBytes() {
        Packet request = new Packet(new byte[25]);
        reqSessionID = (reqSessionID + 1) & 0xffff;
        request.setTwoByteValue(0, reqSessionID);
        request.setOneByteValue(2, reqIndexArrayCount);
        request.setOneByteValue(3, reqIndexArray01);
        request.setOneByteValue(23, reqParameterID);
        request.setOneByteValue(24, reqLimitationType);
        logger.trace("getRequestDataAsArrayOfBytes(): ntfSessionID={}.", reqSessionID);
        logger.trace("getRequestDataAsArrayOfBytes(): reqIndexArrayCount={}.", reqIndexArrayCount);
        logger.trace("getRequestDataAsArrayOfBytes(): reqIndexArray01={}.", reqIndexArray01);
        logger.trace("getRequestDataAsArrayOfBytes(): reqParameterID={}.", reqParameterID);
        logger.trace("getRequestDataAsArrayOfBytes(): reqLimitationType={}.", reqLimitationType);
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
            case GW_GET_LIMITATION_STATUS_CFM:
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

            case GW_LIMITATION_STATUS_NTF:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 10)) {
                    break;
                }
                // Extracting information items
                int ntfSessionID = responseData.getTwoByteValue(0);
                int ntfNodeID = responseData.getOneByteValue(2);
                int ntfParameterID = responseData.getOneByteValue(3);
                int ntfMinValue = responseData.getTwoByteValue(4);
                int ntfMaxValue = responseData.getTwoByteValue(6);
                int ntfLimitationOriginator = responseData.getOneByteValue(8);
                int ntfLimitationTime = responseData.getOneByteValue(9);

                if (!KLF200Response.check4matchingSessionID(logger, ntfSessionID, reqSessionID)) {
                    finished = true;
                    break;
                }

                logger.trace("setResponse(): nodeId={}.", ntfNodeID);
                logger.trace("setResponse(): ntfParameterID={}.", ntfParameterID);
                logger.trace("setResponse(): ntfMinValue={}.", ntfMinValue);
                logger.trace("setResponse(): ntfMaxValue={}.", ntfMaxValue);
                logger.trace("setResponse(): ntfLimitationOriginator={}.", ntfLimitationOriginator);
                logger.trace("setResponse(): ntfLimitationTime={}.", ntfLimitationTime);

                // Determine the returned value
                limitationValue = (reqLimitationType == 0) ? ntfMinValue : ntfMaxValue;
                logger.debug("setResponse(): {} limitation for node {} is {}.",
                        (reqLimitationType == 0) ? "minimum" : "maximum", reqIndexArray01, limitationValue);

                success = true;
                if (!isSequentialEnforced) {
                    logger.trace(
                            "setResponse(): skipping wait for more packets as sequential processing is not enforced.");
                    finished = true;
                }
                break;

            case GW_COMMAND_RUN_STATUS_NTF:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 13)) {
                    finished = true;
                    break;
                }
                ntfSessionID = responseData.getTwoByteValue(0);
                int ntfStatusiD = responseData.getOneByteValue(2);
                int ntfIndex = responseData.getOneByteValue(3);
                int ntfNodeParameter = responseData.getOneByteValue(4);
                int ntfParameterValue = responseData.getTwoByteValue(5);
                int ntfRunStatus = responseData.getOneByteValue(7);
                int ntfStatusReply = responseData.getOneByteValue(8);
                int ntfInformationCode = responseData.getFourByteValue(9);
                // Extracting information items
                logger.trace("setResponse(): ntfSessionID={} (requested {}).", ntfSessionID, reqSessionID);
                logger.trace("setResponse(): ntfStatusiD={}.", ntfStatusiD);
                logger.trace("setResponse(): ntfIndex={}.", ntfIndex);
                logger.trace("setResponse(): ntfNodeParameter={}.", ntfNodeParameter);
                logger.trace("setResponse(): ntfParameterValue={}.", ntfParameterValue);
                logger.trace("setResponse(): ntfRunStatus={}.", ntfRunStatus);
                logger.trace("setResponse(): ntfStatusReply={}.", ntfStatusReply);
                logger.trace("setResponse(): ntfInformationCode={}.", ntfInformationCode);

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

    /*
     * ===========================================================
     * Methods in addition to the interface {@link BridgeCommunicationProtocol}
     * and the abstract class {@link GetProductLimitation}
     */

    @Override
    public void setActuatorIdAndLimitationType(int nodeId, boolean limitationMinimum) {
        logger.trace("setProductId({},{}) called.", nodeId, limitationMinimum);
        this.reqIndexArray01 = nodeId;
        this.reqLimitationType = limitationMinimum ? 0 : 1;
        return;
    }

    @Override
    public int getLimitation() {
        return limitationValue;
    }
}
