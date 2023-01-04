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
package org.openhab.binding.velux.internal.handler;

import static org.openhab.binding.velux.internal.VeluxBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.bridge.common.GetProduct;
import org.openhab.binding.velux.internal.bridge.common.RunProductCommand;
import org.openhab.binding.velux.internal.bridge.slip.FunctionalParameters;
import org.openhab.binding.velux.internal.bridge.slip.SCrunProductCommand;
import org.openhab.binding.velux.internal.handler.utils.Thing2VeluxActuator;
import org.openhab.binding.velux.internal.things.StatusReply;
import org.openhab.binding.velux.internal.things.VeluxExistingProducts;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductState;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <B>Channel-specific retrieval and modification.</B>
 * <P>
 * This class implements the Channel <B>position</B> of the Thing <B>actuator</B>:
 * <UL>
 * <LI><I>Velux</I> <B>bridge</B> &rarr; <B>OpenHAB</B>:
 * <P>
 * Information retrieval by method {@link #handleRefresh}.</LI>
 * </UL>
 * <UL>
 * <LI><B>OpenHAB</B> Event Bus &rarr; <I>Velux</I> <B>bridge</B>
 * <P>
 * Sending commands and value updates by method {@link #handleCommand}.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution.
 * @author Andrew Fiddian-Green - Refactoring and use alternate API set for Vane Position.
 */
@NonNullByDefault
final class ChannelActuatorPosition extends ChannelHandlerTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelActuatorPosition.class);

    // Constructors

    /**
     * Suppress default constructor for non-instantiability.
     */
    private ChannelActuatorPosition() {
        throw new AssertionError();
    }

    // Public methods

    /**
     * Communication method to retrieve information to update the channel value.
     *
     * @param channelUID The item passed as type {@link ChannelUID} for which a refresh is intended.
     * @param channelId The same item passed as type {@link String} for which a refresh is intended.
     * @param thisBridgeHandler The Velux bridge handler with a specific communication protocol which provides
     *            information for this channel.
     * @return newState The value retrieved for the passed channel, or <I>null</I> in case if there is no (new) value.
     */
    static @Nullable State handleRefresh(ChannelUID channelUID, String channelId,
            VeluxBridgeHandler thisBridgeHandler) {
        LOGGER.debug("handleRefresh({},{},{}) called.", channelUID, channelId, thisBridgeHandler);
        State newState = null;
        do { // just for common exit
            if (thisBridgeHandler.bridgeParameters.actuators.autoRefresh(thisBridgeHandler.thisBridge)) {
                LOGGER.trace("handleRefresh(): there are some existing products.");
            }

            Thing2VeluxActuator veluxActuator = thisBridgeHandler.channel2VeluxActuator.get(channelUID);
            if (veluxActuator == null || !veluxActuator.isKnown()) {
                LOGGER.warn("handleRefresh(): unknown actuator.");
                break;
            }

            GetProduct bcp = null;
            switch (channelId) {
                case CHANNEL_ACTUATOR_POSITION:
                case CHANNEL_ACTUATOR_STATE:
                case CHANNEL_VANE_POSITION:
                    bcp = thisBridgeHandler.thisBridge.bridgeAPI().getProductStatus();
                default:
                    // unknown channel, will exit
            }

            if (bcp == null) {
                LOGGER.trace("handleRefresh(): aborting processing as handler is null.");
                break;
            }

            bcp.setProductId(veluxActuator.getProductBridgeIndex().toInt());
            if ((!thisBridgeHandler.thisBridge.bridgeCommunicate(bcp)) || (!bcp.isCommunicationSuccessful())) {
                LOGGER.trace("handleRefresh(): bridge communication request failed.");
                break;
            }

            VeluxProduct newProduct = bcp.getProduct();
            ProductBridgeIndex productBridgeIndex = newProduct.getBridgeProductIndex();
            VeluxExistingProducts existingProducts = thisBridgeHandler.existingProducts();
            VeluxProduct existingProduct = existingProducts.get(productBridgeIndex);
            ProductState productState = newProduct.getProductState();
            switch (productState) {
                case DONE:
                case EXECUTING:
                case MANUAL:
                case UNKNOWN:
                    if (!VeluxProduct.UNKNOWN.equals(existingProduct)) {
                        switch (channelId) {
                            case CHANNEL_VANE_POSITION:
                            case CHANNEL_ACTUATOR_POSITION:
                            case CHANNEL_ACTUATOR_STATE: {
                                if (existingProducts.update(newProduct)) {
                                    existingProduct = existingProducts.get(productBridgeIndex);
                                    int posValue = VeluxProductPosition.VPP_VELUX_UNKNOWN;
                                    switch (channelId) {
                                        case CHANNEL_VANE_POSITION:
                                            posValue = existingProduct.getVaneDisplayPosition();
                                            break;
                                        case CHANNEL_ACTUATOR_POSITION:
                                        case CHANNEL_ACTUATOR_STATE:
                                            posValue = existingProduct.getDisplayPosition();
                                    }
                                    VeluxProductPosition position = new VeluxProductPosition(posValue);
                                    if (position.isValid()) {
                                        switch (channelId) {
                                            case CHANNEL_VANE_POSITION:
                                                newState = position.getPositionAsPercentType(false);
                                                break;
                                            case CHANNEL_ACTUATOR_POSITION:
                                                newState = position
                                                        .getPositionAsPercentType(veluxActuator.isInverted());
                                                break;
                                            case CHANNEL_ACTUATOR_STATE:
                                                newState = OnOffType.from(
                                                        position.getPositionAsPercentType(veluxActuator.isInverted())
                                                                .intValue() > 50);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case WAITING_FOR_POWER:
                case ERROR:
                    StatusReply statusReply = productState == ProductState.WAITING_FOR_POWER
                            ? StatusReply.NODE_WAITING_FOR_POWER
                            : bcp.getStatusReply();
                    if (statusReply.isError()) {
                        String id = VeluxProduct.UNKNOWN.equals(existingProduct)
                                ? newProduct.getBridgeProductIndex().toString()
                                : existingProduct.getProductUniqueIndex();
                        if (statusReply.isCriticalError()) {
                            LOGGER.warn("Product Id:{} encountered an error with StatusReply:{}", id, statusReply);
                        } else {
                            LOGGER.info("Product Id:{} encountered an error with StatusReply:{}", id, statusReply);
                        }
                    }
                default:
            }

            if (newState == null) {
                newState = UnDefType.UNDEF;
            }
        } while (false); // common exit
        LOGGER.trace("handleRefresh(): new state for channel id '{}' is '{}'.", channelId, newState);
        return newState;
    }

    /**
     * Communication method to update the real world according to the passed channel value (or command).
     *
     * @param channelUID The item passed as type {@link ChannelUID} for which to following command is addressed to.
     * @param channelId The same item passed as type {@link String} for which a refresh is intended.
     * @param command The command passed as type {@link Command} for the mentioned item.
     * @param thisBridgeHandler The Velux bridge handler with a specific communication protocol which provides
     *            information for this channel.
     * @return newValue ...
     */
    static @Nullable Command handleCommand(ChannelUID channelUID, String channelId, Command command,
            VeluxBridgeHandler thisBridgeHandler) {
        LOGGER.debug("handleCommand({},{},{},{}) called.", channelUID, channelId, command, thisBridgeHandler);
        Command newValue = null;
        do { // just for common exit
            if (thisBridgeHandler.bridgeParameters.actuators.autoRefresh(thisBridgeHandler.thisBridge)) {
                LOGGER.trace("handleCommand(): there are some existing products.");
            }

            Thing2VeluxActuator veluxActuator = thisBridgeHandler.channel2VeluxActuator.get(channelUID);
            if (veluxActuator == null || !veluxActuator.isKnown()) {
                LOGGER.warn("handleRefresh(): unknown actuator.");
                break;
            }

            VeluxProductPosition mainParameter = null;
            FunctionalParameters functionalParameters = null;
            VeluxExistingProducts existingProducts = thisBridgeHandler.existingProducts();
            ProductBridgeIndex productBridgeIndex = veluxActuator.getProductBridgeIndex();

            switch (channelId) {
                case CHANNEL_VANE_POSITION:
                    if (command instanceof PercentType) {
                        VeluxProduct existingProductClone = existingProducts.get(productBridgeIndex).clone();
                        existingProductClone.setVanePosition(
                                new VeluxProductPosition((PercentType) command).getPositionAsVeluxType());
                        functionalParameters = existingProductClone.getFunctionalParameters();
                    }
                    break;

                case CHANNEL_ACTUATOR_POSITION:
                    if (command instanceof UpDownType) {
                        mainParameter = UpDownType.UP.equals(command) ^ veluxActuator.isInverted()
                                ? new VeluxProductPosition(PercentType.ZERO)
                                : new VeluxProductPosition(PercentType.HUNDRED);
                    } else if (command instanceof StopMoveType) {
                        mainParameter = StopMoveType.STOP.equals(command) ? new VeluxProductPosition() : mainParameter;
                    } else if (command instanceof PercentType) {
                        PercentType ptCommand = (PercentType) command;
                        if (veluxActuator.isInverted()) {
                            ptCommand = new PercentType(PercentType.HUNDRED.intValue() - ptCommand.intValue());
                        }
                        mainParameter = new VeluxProductPosition(ptCommand);
                    }
                    break;

                case CHANNEL_ACTUATOR_STATE:
                    if (command instanceof OnOffType) {
                        mainParameter = OnOffType.OFF.equals(command) ^ veluxActuator.isInverted()
                                ? new VeluxProductPosition(PercentType.ZERO)
                                : new VeluxProductPosition(PercentType.HUNDRED);
                    }
                    break;

                default:
                    // unknown channel => do nothing..
            }

            if ((mainParameter != null) || (functionalParameters != null)) {
                LOGGER.debug("handleCommand(): sending command '{}' for channel id '{}'.", command, channelId);
                RunProductCommand bcp = thisBridgeHandler.thisBridge.bridgeAPI().runProductCommand();
                boolean success = false;
                if (bcp instanceof SCrunProductCommand) {
                    synchronized (bcp) {
                        if (bcp.setNodeIdAndParameters(productBridgeIndex.toInt(), mainParameter, functionalParameters)
                                && thisBridgeHandler.thisBridge.bridgeCommunicate(bcp)
                                && bcp.isCommunicationSuccessful()) {
                            success = true;
                            if (thisBridgeHandler.bridgeParameters.actuators
                                    .autoRefresh(thisBridgeHandler.thisBridge)) {
                                LOGGER.trace("handleCommand(): actuator position will be updated via polling.");
                            }
                            if (existingProducts.update(((SCrunProductCommand) bcp).getProduct())) {
                                LOGGER.trace("handleCommand(): actuator position immediate update requested.");
                            }
                        }
                    }
                }
                LOGGER.debug("handleCommand(): sendCommand() finished {}.",
                        (success ? "successfully" : "with failure"));
            } else {
                LOGGER.info("handleCommand(): ignoring command '{}' for channel id '{}'.", command, channelId);
            }
        } while (false); // common exit
        return newValue;
    }
}
