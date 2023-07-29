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

import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.bridge.common.RunProductCommand;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProduct.DataSource;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductState;
import org.openhab.binding.velux.internal.things.VeluxProductName;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;
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
public class SCrunProductCommand extends RunProductCommand implements SlipBridgeCommunicationProtocol {
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
    private @Nullable FunctionalParameters reqFunctionalParameters = null;

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

    private VeluxProduct product = VeluxProduct.UNKNOWN;

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

        FunctionalParameters reqFunctionalParameters = this.reqFunctionalParameters;
        reqFPI1 = reqFunctionalParameters != null ? reqFunctionalParameters.writeArray(request, 9) : 0;

        request.setOneByteValue(5, reqFPI1);
        request.setOneByteValue(6, reqFPI2);
        request.setTwoByteValue(7, reqMainParameter);
        request.setOneByteValue(41, reqIndexArrayCount);
        request.setOneByteValue(42, reqIndexArray01);
        request.setOneByteValue(62, reqPriorityLevelLock);
        request.setOneByteValue(63, reqPL03);
        request.setOneByteValue(64, reqPL47);
        request.setOneByteValue(65, reqLockTime);

        requestData = request.toByteArray();

        if (logger.isTraceEnabled()) {
            logger.trace("getRequestDataAsArrayOfBytes(): ntfSessionID={}.", hex(reqSessionID));
            logger.trace("getRequestDataAsArrayOfBytes(): reqCommandOriginator={}.", hex(reqCommandOriginator));
            logger.trace("getRequestDataAsArrayOfBytes(): reqPriorityLevel={}.", hex(reqPriorityLevel));
            logger.trace("getRequestDataAsArrayOfBytes(): reqParameterActive={}.", hex(reqParameterActive));
            logger.trace("getRequestDataAsArrayOfBytes(): reqFPI1={}.", bin(reqFPI1));
            logger.trace("getRequestDataAsArrayOfBytes(): reqFPI2={}.", bin(reqFPI2));
            logger.trace("getRequestDataAsArrayOfBytes(): reqMainParameter={}.", hex(reqMainParameter));
            logger.trace("getRequestDataAsArrayOfBytes(): reqFunctionalParameters={}.", reqFunctionalParameters);
            logger.trace("getRequestDataAsArrayOfBytes(): reqIndexArrayCount={}.", hex(reqIndexArrayCount));
            logger.trace("getRequestDataAsArrayOfBytes(): reqIndexArray01={} (reqNodeId={}).", reqIndexArray01,
                    reqIndexArray01);
            logger.trace("getRequestDataAsArrayOfBytes(): reqPriorityLevelLock={}.", hex(reqPriorityLevelLock));
            logger.trace("getRequestDataAsArrayOfBytes(): reqPL03={}.", hex(reqPL03));
            logger.trace("getRequestDataAsArrayOfBytes(): reqPL47={}.", hex(reqPL47));
            logger.trace("getRequestDataAsArrayOfBytes(): reqLockTime={}.", hex(reqLockTime));

            logger.trace("getRequestDataAsArrayOfBytes() data is {}.", new Packet(requestData).toString());
        }
        return requestData;
    }

    private String hex(int i) {
        return Integer.toHexString(i);
    }

    private String bin(int i) {
        return Integer.toBinaryString(i);
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

                if (logger.isTraceEnabled()) {
                    logger.trace("setResponse(): ntfSessionID={} (requested {}).", ntfSessionID, reqSessionID);
                    logger.trace("setResponse(): ntfStatusiD={}.", ntfStatusiD);
                    logger.trace("setResponse(): ntfIndex={}.", ntfIndex);
                    logger.trace("setResponse(): ntfNodeParameter={}.", ntfNodeParameter);
                    logger.trace("setResponse(): ntfParameterValue={}.", String.format("0x%04X", ntfParameterValue));
                    logger.trace("setResponse(): ntfRunStatus={}.", ntfRunStatus);
                    logger.trace("setResponse(): ntfStatusReply={}.", ntfStatusReply);
                    logger.trace("setResponse(): ntfInformationCode={}.", ntfInformationCode);
                }

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

                if (logger.isDebugEnabled()) {
                    logger.debug("setResponse(): timeNtfSessionID={}.", timeNtfSessionID);
                    logger.debug("setResponse(): timeNtfIndex={}.", timeNtfIndex);
                    logger.debug("setResponse(): timeNtfNodeParameter={}.", timeNtfNodeParameter);
                    logger.debug("setResponse(): timeNtfSeconds={}.", timeNtfSeconds);
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
    public boolean setNodeIdAndParameters(int nodeId, @Nullable VeluxProductPosition mainParameter,
            @Nullable FunctionalParameters functionalParameters) {
        logger.debug("setNodeIdAndParameters({}) called.", nodeId);

        if ((mainParameter != null) || (functionalParameters != null)) {
            reqIndexArray01 = nodeId;

            reqMainParameter = (mainParameter == null) ? VeluxProductPosition.VPP_VELUX_STOP
                    : mainParameter.getPositionAsVeluxType();

            int setMainParameter = VeluxProductPosition.isValid(reqMainParameter) ? reqMainParameter
                    : VeluxProductPosition.VPP_VELUX_IGNORE;

            reqFunctionalParameters = functionalParameters;

            // create notification product that clones the new command positions
            product = new VeluxProduct(VeluxProductName.UNKNOWN, new ProductBridgeIndex(reqIndexArray01),
                    ProductState.EXECUTING.value, setMainParameter, setMainParameter, reqFunctionalParameters, COMMAND)
                    .overrideDataSource(DataSource.BINDING);

            return true;
        }
        product = VeluxProduct.UNKNOWN;
        return false;
    }

    public VeluxProduct getProduct() {
        logger.trace("getProduct(): returning {}.", product);
        return product;
    }
}
