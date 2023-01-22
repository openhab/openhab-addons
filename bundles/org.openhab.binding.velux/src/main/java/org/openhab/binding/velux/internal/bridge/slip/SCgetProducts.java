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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.bridge.common.GetProducts;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.openhab.binding.velux.internal.things.VeluxProductName;
import org.openhab.binding.velux.internal.things.VeluxProductSerialNo;
import org.openhab.binding.velux.internal.things.VeluxProductType;
import org.openhab.binding.velux.internal.things.VeluxProductType.ActuatorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Retrieve Products</B>
 * <P>
 * Common Message semantic: Communication with the bridge and (optionally) storing returned information within the class
 * itself.
 * <P>
 * As 3rd level class it defines informations how to send query and receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the {@link org.openhab.binding.velux.internal.bridge.slip.SlipBridgeCommunicationProtocol
 * SlipBridgeCommunicationProtocol}.
 * <P>
 * Methods in addition to the mentioned interface:
 * <UL>
 * <LI>{@link #getProducts()} to retrieve the currently registered products.</LI>
 * </UL>
 *
 * @see GetProducts
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SCgetProducts extends GetProducts implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCgetProducts.class);

    private static final String DESCRIPTION = "Retrieve Products";
    private static final Command COMMAND = Command.GW_GET_ALL_NODES_INFORMATION_REQ;

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

    private VeluxProduct[] productArray = new VeluxProduct[0];
    private int totalNumberOfProducts = 0;
    private int nextProductArrayItem = 0;

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
        requestData = new byte[0];
        return requestData;
    }

    @Override
    public void setResponse(short responseCommand, byte[] thisResponseData, boolean isSequentialEnforced) {
        KLF200Response.introLogging(logger, responseCommand, thisResponseData);
        success = false;
        finished = false;
        Packet responseData = new Packet(thisResponseData);
        switch (Command.get(responseCommand)) {
            case GW_GET_ALL_NODES_INFORMATION_CFM:
                logger.trace("setResponse(): got GW_GET_ALL_NODES_INFORMATION_CFM.");
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 2)) {
                    finished = true;
                    break;
                }
                int cfmStatus = responseData.getOneByteValue(0);
                int cfmTotalNumberOfNodes = responseData.getOneByteValue(1);
                logger.trace("setResponse(): status={}.", cfmStatus);
                logger.trace("setResponse(): TotalNumberOfNodes={}.", cfmTotalNumberOfNodes);

                // Initialize storage area
                productArray = new VeluxProduct[0];
                nextProductArrayItem = 0;
                switch (cfmStatus) {
                    case 0:
                        logger.trace("setResponse(): returned status: OK - Request accepted.");
                        totalNumberOfProducts = cfmTotalNumberOfNodes;
                        productArray = new VeluxProduct[totalNumberOfProducts];
                        break;
                    case 1:
                        logger.trace("setResponse(): returned status: Error – System table empty.");
                        finished = true;
                        break;
                    default:
                        finished = true;
                        logger.warn("setResponse({}): returned status={} (Reserved/unknown).",
                                Command.get(responseCommand).toString(), cfmStatus);
                        break;
                }
                break;
            case GW_GET_ALL_NODES_INFORMATION_NTF:
                logger.trace("setResponse(): got GW_GET_ALL_NODES_INFORMATION_NTF.");
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 124)) {
                    finished = true;
                    break;
                }
                if (productArray.length == 0) {
                    logger.warn("setResponse({}): sequence of answers unexpected.",
                            Command.get(responseCommand).toString());
                    finished = true;
                    break;
                }
                // Extracting information items
                int ntfNodeID = responseData.getOneByteValue(0);
                int ntfOrder = responseData.getTwoByteValue(1);
                int ntfPlacement = responseData.getOneByteValue(3);
                String ntfName = responseData.getString(4, 64);
                int ntfVelocity = responseData.getOneByteValue(68);
                int ntfNodeTypeSubType = responseData.getTwoByteValue(69);
                int ntfProductGroup = responseData.getOneByteValue(71);
                int ntfProductType = responseData.getOneByteValue(72);
                int ntfNodeVariation = responseData.getOneByteValue(73);
                int ntfPowerMode = responseData.getOneByteValue(74);
                int ntfBuildNumber = responseData.getOneByteValue(75);
                byte[] ntfSerialNumber = responseData.getByteArray(76, 8);
                int ntfState = responseData.getOneByteValue(84);
                int ntfCurrentPosition = responseData.getTwoByteValue(85);
                int ntfTarget = responseData.getTwoByteValue(87);
                FunctionalParameters ntfFunctionalParameters = FunctionalParameters.readArray(responseData, 89);
                int ntfRemainingTime = responseData.getTwoByteValue(97);
                int ntfTimeStamp = responseData.getFourByteValue(99);
                int ntfNbrOfAlias = responseData.getOneByteValue(103);
                int ntfAliasOne = responseData.getFourByteValue(104);
                int ntfAliasTwo = responseData.getFourByteValue(108);
                int ntfAliasThree = responseData.getFourByteValue(112);
                int ntfAliasFour = responseData.getFourByteValue(116);
                int ntfAliasFive = responseData.getFourByteValue(120);

                if (logger.isTraceEnabled()) {
                    logger.trace("setResponse(): ntfNodeID={}.", ntfNodeID);
                    logger.trace("setResponse(): ntfOrder={}.", ntfOrder);
                    logger.trace("setResponse(): ntfPlacement={}.", ntfPlacement);
                    logger.trace("setResponse(): ntfName={}.", ntfName);
                    logger.trace("setResponse(): ntfVelocity={}.", ntfVelocity);
                    logger.trace("setResponse(): ntfNodeTypeSubType={} ({}).", ntfNodeTypeSubType,
                            VeluxProductType.get(ntfNodeTypeSubType));
                    logger.trace("setResponse(): derived product description={}.",
                            VeluxProductType.toString(ntfNodeTypeSubType));
                    logger.trace("setResponse(): ntfProductGroup={}.", ntfProductGroup);
                    logger.trace("setResponse(): ntfProductType={}.", ntfProductType);
                    logger.trace("setResponse(): ntfNodeVariation={}.", ntfNodeVariation);
                    logger.trace("setResponse(): ntfPowerMode={}.", ntfPowerMode);
                    logger.trace("setResponse(): ntfBuildNumber={}.", ntfBuildNumber);
                    logger.trace("setResponse(): ntfSerialNumber={}.", VeluxProductSerialNo.toString(ntfSerialNumber));
                    logger.trace("setResponse(): ntfState={}.", ntfState);
                    logger.trace("setResponse(): ntfCurrentPosition={}.", String.format("0x%04X", ntfCurrentPosition));
                    logger.trace("setResponse(): ntfTarget={}.", String.format("0x%04X", ntfTarget));
                    logger.trace("setResponse(): ntfFunctionalParameters={}.", ntfFunctionalParameters);
                    logger.trace("setResponse(): ntfRemainingTime={}.", ntfRemainingTime);
                    logger.trace("setResponse(): ntfTimeStamp={}.", ntfTimeStamp);
                    logger.trace("setResponse(): ntfNbrOfAlias={}.", ntfNbrOfAlias);
                    logger.trace("setResponse(): ntfAliasOne={}.", ntfAliasOne);
                    logger.trace("setResponse(): ntfAliasTwo={}.", ntfAliasTwo);
                    logger.trace("setResponse(): ntfAliasThree={}.", ntfAliasThree);
                    logger.trace("setResponse(): ntfAliasFour={}.", ntfAliasFour);
                    logger.trace("setResponse(): ntfAliasFive={}.", ntfAliasFive);
                }

                if ((ntfName.length() == 0) || ntfName.startsWith("_")) {
                    ntfName = "#".concat(String.valueOf(ntfNodeID));
                    logger.debug("setResponse(): device provided invalid name, using '{}' instead.", ntfName);
                }

                String commonSerialNumber = VeluxProductSerialNo.toString(ntfSerialNumber);
                if (VeluxProductSerialNo.isInvalid(ntfSerialNumber)) {
                    commonSerialNumber = new String(ntfName);
                    logger.debug("setResponse(): device provided invalid serial number, using name '{}' instead.",
                            commonSerialNumber);
                }

                VeluxProduct product = new VeluxProduct(new VeluxProductName(ntfName),
                        VeluxProductType.get(ntfNodeTypeSubType), ActuatorType.get(ntfNodeTypeSubType),
                        new ProductBridgeIndex(ntfNodeID), ntfOrder, ntfPlacement, ntfVelocity, ntfNodeVariation,
                        ntfPowerMode, commonSerialNumber, ntfState, ntfCurrentPosition, ntfTarget,
                        ntfFunctionalParameters, ntfRemainingTime, ntfTimeStamp, COMMAND);
                if (nextProductArrayItem < totalNumberOfProducts) {
                    productArray[nextProductArrayItem++] = product;
                } else {
                    logger.warn("setResponse(): expected {} products, received one more, ignoring it.",
                            totalNumberOfProducts);
                }
                success = true;
                break;

            case GW_GET_ALL_NODES_INFORMATION_FINISHED_NTF:
                logger.trace("setResponse(): got GW_GET_ALL_NODES_INFORMATION_FINISHED_NTF.");
                logger.debug("setResponse(): finished-packet received.");
                success = true;
                finished = true;
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
     * and the abstract class {@link GetProducts}
     */

    @Override
    public VeluxProduct[] getProducts() {
        if (success && finished) {
            logger.trace("getProducts(): returning array of {} products.", productArray.length);
            return productArray;
        } else {
            logger.trace("getProducts(): returning null.");
            return new VeluxProduct[0];
        }
    }
}
