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
import org.openhab.binding.velux.internal.bridge.common.BridgeCommunicationProtocol;
import org.openhab.binding.velux.internal.bridge.common.GetHouseStatus;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.openhab.binding.velux.internal.things.VeluxProductName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Retrieve House Status</B>
 * <P>
 * Common Message semantic: Communication from the bridge and storing returned information within the class itself.
 * <P>
 * As 3rd level class it defines informations how to receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the interface {@link org.openhab.binding.velux.internal.bridge.slip.SlipBridgeCommunicationProtocol
 * SlipBridgeCommunicationProtocol}.
 * <P>
 * Methods in addition to the mentioned interface:
 * <UL>
 * <LI>{@link #getProduct} to retrieve product type.</LI>
 * <LI>{@link #setCreatorCommand} to set the command id that identifies the API on which 'product' will be created.</LI>
 * </UL>
 * <P>
 * NOTE: the class does NOT define a request as it only works as receiver.
 *
 * @see BridgeCommunicationProtocol
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public class SCgetHouseStatus extends GetHouseStatus
        implements BridgeCommunicationProtocol, SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCgetHouseStatus.class);

    private static final String DESCRIPTION = "Retrieve House Status";
    private static final Command COMMAND = Command.GW_OPENHAB_RECEIVEONLY;

    /*
     * ===========================================================
     * Message Objects
     */

    @SuppressWarnings("unused")
    private byte[] requestData = new byte[0];

    /*
     * ===========================================================
     * Result Objects
     */

    private boolean success = false;
    private boolean finished = false;

    private Command creatorCommand = Command.UNDEFTYPE;
    private VeluxProduct product = VeluxProduct.UNKNOWN;

    /*
     * ===========================================================
     * Methods required for interface {@link SlipBridgeCommunicationProtocol}.
     */

    @Override
    public String name() {
        return DESCRIPTION;
    }

    @Override
    public CommandNumber getRequestCommand() {
        logger.debug("getRequestCommand() returns {} ({}).", COMMAND.name(), COMMAND.getCommand());
        return COMMAND.getCommand();
    }

    @Override
    public byte[] getRequestDataAsArrayOfBytes() {
        return new byte[0];
    }

    @Override
    public void setResponse(short responseCommand, byte[] thisResponseData, boolean isSequentialEnforced) {
        KLF200Response.introLogging(logger, responseCommand, thisResponseData);
        success = false;
        finished = true;
        Packet responseData = new Packet(thisResponseData);
        Command responseCmd = Command.get(responseCommand);
        switch (responseCmd) {
            case GW_NODE_STATE_POSITION_CHANGED_NTF:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 20)) {
                    break;
                }
                int ntfNodeID = responseData.getOneByteValue(0);
                int ntfState = responseData.getOneByteValue(1);
                int ntfCurrentPosition = responseData.getTwoByteValue(2);
                int ntfTarget = responseData.getTwoByteValue(4);
                FunctionalParameters ntfFunctionalParameters = FunctionalParameters.readArray(responseData, 6);
                int ntfRemainingTime = responseData.getTwoByteValue(14);
                int ntfTimeStamp = responseData.getFourByteValue(16);

                if (logger.isTraceEnabled()) {
                    logger.trace("setResponse(): ntfNodeID={}.", ntfNodeID);
                    logger.trace("setResponse(): ntfState={}.", ntfState);
                    logger.trace("setResponse(): ntfCurrentPosition={}.", String.format("0x%04X", ntfCurrentPosition));
                    logger.trace("setResponse(): ntfTarget={}.", String.format("0x%04X", ntfTarget));
                    logger.trace("setResponse(): ntfFunctionalParameters={} (returns null).", ntfFunctionalParameters);
                    logger.trace("setResponse(): ntfRemainingTime={}.", ntfRemainingTime);
                    logger.trace("setResponse(): ntfTimeStamp={}.", ntfTimeStamp);
                }

                // this BCP returns wrong functional parameters on some (e.g. Somfy) devices so return null instead
                ntfFunctionalParameters = null;

                // create notification product with the returned values
                product = new VeluxProduct(VeluxProductName.UNKNOWN, new ProductBridgeIndex(ntfNodeID), ntfState,
                        ntfCurrentPosition, ntfTarget, ntfFunctionalParameters, creatorCommand);

                success = true;
                break;

            default:
                KLF200Response.errorLogging(logger, responseCommand);
        }
        KLF200Response.outroLogging(logger, success, finished);
    }

    @Override
    public boolean isCommunicationFinished() {
        return true;
    }

    @Override
    public boolean isCommunicationSuccessful() {
        return true;
    }

    /*
     * ===========================================================
     * Methods in addition to the interface {@link BridgeCommunicationProtocol}
     */

    public VeluxProduct getProduct() {
        logger.trace("getProduct(): returning {}.", product);
        return product;
    }

    /**
     * Change the command id that identifies the API on which 'product' will be created.
     *
     * @param creatorCommand the API that will be used to create the product instance.
     * @return this
     */
    public SCgetHouseStatus setCreatorCommand(Command creatorCommand) {
        this.creatorCommand = creatorCommand;
        return this;
    }
}
