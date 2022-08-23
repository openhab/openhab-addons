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
import org.openhab.binding.velux.internal.bridge.common.GetProduct;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.StatusReply;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.openhab.binding.velux.internal.things.VeluxProductName;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Retrieve Product Status</B>
 * <p>
 * This implements an alternate API set (vs. the API set used by ScgetProduct) for retrieving a product's status. This
 * alternate API set was added to the code base because, when using ScgetProduct, some products (e.g. Somfy) would
 * produce buggy values in their Functional Parameters when reporting their Vane Position.
 * <p>
 * This API set is the one used (for example) by Home Assistant.
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
public class SCgetProductStatus extends GetProduct implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCgetProductStatus.class);

    private static final String DESCRIPTION = "Retrieve Product Status";
    private static final Command COMMAND = Command.GW_STATUS_REQUEST_REQ;

    /*
     * RunStatus and StatusReply parameter values (from KLF200 API specification)
     */
    private static final int EXECUTION_COMPLETED = 0;// Execution is completed with no errors.
    private static final int EXECUTION_FAILED = 1; // Execution has failed. (Get specifics in the following error code)
    private static final int EXECUTION_ACTIVE = 2;// Execution is still active
    private static final int UNKNOWN_STATUS_REPLY = 0x00; // Used to indicate unknown reply.
    private static final int COMMAND_COMPLETED_OK = 0x01;

    /*
     * ===========================================================
     * Message Content Parameters
     */

    private int reqSessionID = 0; // The session id
    private final int reqIndexArrayCount = 1; // One node will be addressed
    private int reqNodeId = 1; // This is the node id
    private final int reqStatusType = 1; // The current value
    private final int reqFPI1 = 0xF0; // Functional Parameter Indicator 1 bit map (set to fetch { FP1 .. FP4 }
    private final int reqFPI2 = 0; // Functional Parameter Indicator 2 bit map.

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
    private StatusReply statusReply = StatusReply.COMMAND_COMPLETED_OK;

    public SCgetProductStatus() {
        logger.debug("SCgetProductStatus(Constructor) called.");
        Random rand = new Random();
        reqSessionID = rand.nextInt(0x0fff);
        logger.debug("SCgetProductStatus(): starting session with the random number {}.", reqSessionID);
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
        logger.trace("getRequestDataAsArrayOfBytes() returns data for retrieving node with id {}.", reqNodeId);
        reqSessionID = (reqSessionID + 1) & 0xffff;
        Packet request = new Packet(new byte[26]);
        request.setTwoByteValue(0, reqSessionID);
        request.setOneByteValue(2, reqIndexArrayCount);
        request.setOneByteValue(3, reqNodeId);
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
        Command responseCmd = Command.get(responseCommand);
        switch (responseCmd) {
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
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 59)) {
                    finished = true;
                    break;
                }

                // Extracting information items
                int ntfSessionID = responseData.getTwoByteValue(0);
                int ntfStatusID = responseData.getOneByteValue(2);
                int ntfNodeID = responseData.getOneByteValue(3);
                int ntfRunStatus = responseData.getOneByteValue(4);
                int ntfStatusReply = responseData.getOneByteValue(5);
                int ntfStatusType = responseData.getOneByteValue(6);
                int ntfStatusCount = responseData.getOneByteValue(7);
                int ntfFirstParameterIndex = responseData.getOneByteValue(8);
                int ntfFirstParameter = responseData.getTwoByteValue(9);
                FunctionalParameters ntfFunctionalParameters = FunctionalParameters.readArrayIndexed(responseData, 11);

                if (logger.isTraceEnabled()) {
                    logger.trace("setResponse(): ntfSessionID={}.", ntfSessionID);
                    logger.trace("setResponse(): ntfStatusID={}.", ntfStatusID);
                    logger.trace("setResponse(): ntfNodeID={}.", ntfNodeID);
                    logger.trace("setResponse(): ntfRunStatus={}.", ntfRunStatus);
                    logger.trace("setResponse(): ntfStatusReply={}.", ntfStatusReply);
                    logger.trace("setResponse(): ntfStatusType={}.", ntfStatusType);
                    logger.trace("setResponse(): ntfStatusCount={}.", ntfStatusCount);
                    logger.trace("setResponse(): ntfFirstParameterIndex={}.", ntfFirstParameterIndex);
                    logger.trace("setResponse(): ntfFirstParameter={}.", String.format("0x%04X", ntfFirstParameter));
                    logger.trace("setResponse(): ntfFunctionalParameters={}.", ntfFunctionalParameters);
                }

                if (!KLF200Response.check4matchingNodeID(logger, reqNodeId, ntfNodeID)) {
                    break;
                }

                int ntfCurrentPosition;
                if ((ntfStatusCount > 0) && (ntfFirstParameterIndex == 0)) {
                    ntfCurrentPosition = ntfFirstParameter;
                } else {
                    ntfCurrentPosition = VeluxProductPosition.VPP_VELUX_UNKNOWN;
                }

                int ntfState;
                switch (ntfRunStatus) {
                    case EXECUTION_ACTIVE:
                        ntfState = VeluxProduct.ProductState.EXECUTING.value;
                        break;
                    case EXECUTION_COMPLETED:
                        ntfState = VeluxProduct.ProductState.DONE.value;
                        break;
                    case EXECUTION_FAILED:
                    default:
                        switch (ntfStatusReply) {
                            case UNKNOWN_STATUS_REPLY:
                                ntfState = VeluxProduct.ProductState.UNKNOWN.value;
                                break;
                            case COMMAND_COMPLETED_OK:
                                ntfState = VeluxProduct.ProductState.DONE.value;
                                break;
                            default:
                                ntfState = VeluxProduct.ProductState.ERROR.value;
                                statusReply = StatusReply.fromCode(ntfStatusReply);
                        }
                        break;
                }

                // create notification product with the returned values
                product = new VeluxProduct(VeluxProductName.UNKNOWN, new ProductBridgeIndex(ntfNodeID), ntfState,
                        ntfCurrentPosition, VeluxProductPosition.VPP_VELUX_IGNORE, ntfFunctionalParameters, COMMAND);

                success = true;
                if (!isSequentialEnforced) {
                    logger.trace(
                            "setResponse(): skipping wait for more packets as sequential processing is not enforced.");
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
     * and the abstract class {@link GetProduct}
     */

    @Override
    public void setProductId(int nodeId) {
        logger.trace("setProductId({}) called.", nodeId);
        reqNodeId = nodeId;
    }

    @Override
    public VeluxProduct getProduct() {
        logger.trace("getProduct(): returning {}.", product);
        return product;
    }

    @Override
    public StatusReply getStatusReply() {
        return statusReply;
    }
}
