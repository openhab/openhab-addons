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
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeRunProductCommand;
import org.openhab.binding.velux.internal.bridge.common.GetProduct;
import org.openhab.binding.velux.internal.handler.utils.Thing2VeluxActuator;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;
import org.openhab.binding.velux.internal.things.VeluxProductType.ActuatorType;
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
            GetProduct bcp = thisBridgeHandler.thisBridge.bridgeAPI().getProduct();
            if (bcp == null) {
                LOGGER.trace("handleRefresh(): aborting processing as handler is null.");
                break;
            }
            bcp.setProductId(veluxActuator.getProductBridgeIndex().toInt());
            if (thisBridgeHandler.thisBridge.bridgeCommunicate(bcp) && bcp.isCommunicationSuccessful()) {
                try {
                    VeluxProduct product = bcp.getProduct();

                    // update vane position
                    if (CHANNEL_VANE_POSITION.equals(channelId)) {
                        int vanePosition = VeluxProductPosition.VPP_VELUX_UNKNOWN;
                        int[] functionalParameters = product.getFunctionalParameters();
                        ActuatorType actuatorType = product.getActuatorType();
                        switch (actuatorType) {
                            case BLIND_1_0:
                                if (functionalParameters.length > 0) {
                                    vanePosition = functionalParameters[0];
                                }
                                break;
                            case ROLLERSHUTTER_2_1:
                            case BLIND_17:
                            case BLIND_18:
                                if (functionalParameters.length > 2) {
                                    vanePosition = functionalParameters[2];
                                }
                                break;
                            default:
                                LOGGER.info("handleRefresh(): actuator type '{}' ({}) does not support channel '{}'.",
                                        actuatorType.getNodeType(), actuatorType.getDescription(), channelId);
                        }
                        if ((vanePosition >= VeluxProductPosition.VPP_VELUX_MIN)
                                && (vanePosition <= VeluxProductPosition.VPP_VELUX_MAX)) {
                            VeluxProductPosition productVanePosition = new VeluxProductPosition(vanePosition);
                            newState = productVanePosition.getPositionAsPercentType(false);
                            LOGGER.trace("handleRefresh(): position of vane is {}%.", newState);
                        } else {
                            newState = UnDefType.UNDEF;
                            LOGGER.trace("handleRefresh(): position of vane is 'UNDEFINED'.");
                        }
                        break;
                    }

                    // update actuator position resp. state
                    VeluxProductPosition position = new VeluxProductPosition(product.getDisplayPosition());
                    if (position.isValid()) {
                        if (CHANNEL_ACTUATOR_POSITION.equals(channelId)) {
                            newState = position.getPositionAsPercentType(veluxActuator.isInverted());
                            LOGGER.trace("handleRefresh(): position of actuator is {}%.", newState);
                            break;
                        } else if (CHANNEL_ACTUATOR_STATE.equals(channelId)) {
                            newState = OnOffType.from(
                                    position.getPositionAsPercentType(veluxActuator.isInverted()).intValue() > 50);
                            LOGGER.trace("handleRefresh(): state of actuator is {}.", newState);
                            break;
                        }
                    }
                    LOGGER.trace("handleRefresh(): position of actuator is 'UNDEFINED'.");
                    newState = UnDefType.UNDEF;
                } catch (Exception e) {
                    LOGGER.warn("handleRefresh(): getProducts() exception: {}.", e.getMessage());
                }
            }
        } while (false); // common exit
        LOGGER.trace("handleRefresh() returns {}.", newState);
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

            // command the vane position
            if (CHANNEL_VANE_POSITION.equals(channelId) && (command instanceof PercentType)) {
                LOGGER.trace("handleCommand(): found vane position PercentType.{} command", command);
                int newVanePosition = new VeluxProductPosition((PercentType) command).getPositionAsVeluxType();
                ProductBridgeIndex bridgeIndex = veluxActuator.getProductBridgeIndex();
                VeluxProduct targetProduct = thisBridgeHandler.existingProducts().get(bridgeIndex);
                int[] functionalParameters = targetProduct.getFunctionalParameters();
                boolean setVanePosition = false;
                ActuatorType actuatorType = targetProduct.getActuatorType();
                switch (actuatorType) {
                    case BLIND_1_0:
                        if (functionalParameters.length > 0) {
                            functionalParameters[0] = newVanePosition;
                            setVanePosition = true;
                        }
                        break;
                    case ROLLERSHUTTER_2_1:
                    case BLIND_17:
                    case BLIND_18:
                        if (functionalParameters.length > 2) {
                            functionalParameters[2] = newVanePosition;
                            setVanePosition = true;
                        }
                        break;
                    default:
                        LOGGER.info("handleCommand(): actuator type '{}' ({}) does not support channel '{}'.",
                                actuatorType.getNodeType(), actuatorType.getDescription(), channelId);
                }
                if (setVanePosition) {
                    LOGGER.debug("handleCommand(): sending command to set vane position to {}.", newVanePosition);
                    new VeluxBridgeRunProductCommand().sendCommand(thisBridgeHandler.thisBridge, bridgeIndex.toInt(),
                            new VeluxProductPosition(VeluxProductPosition.VPP_VELUX_IGNORE), functionalParameters);
                    LOGGER.trace("handleCommand(): vane position will be updated through the house status events.");
                    if (thisBridgeHandler.bridgeParameters.actuators.autoRefresh(thisBridgeHandler.thisBridge)) {
                        LOGGER.trace("handleCommand(): vane positions will be refreshed.");
                    }
                }
                break;
            }

            // command the actuator position resp. state
            VeluxProductPosition targetLevel = VeluxProductPosition.UNKNOWN;
            if (CHANNEL_ACTUATOR_POSITION.equals(channelId)) {
                if (command instanceof UpDownType) {
                    LOGGER.trace("handleCommand(): found UpDownType.{} command.", command);
                    targetLevel = UpDownType.UP.equals(command) ^ veluxActuator.isInverted()
                            ? new VeluxProductPosition(PercentType.ZERO)
                            : new VeluxProductPosition(PercentType.HUNDRED);
                } else if (command instanceof StopMoveType) {
                    LOGGER.trace("handleCommand(): found StopMoveType.{} command.", command);
                    targetLevel = StopMoveType.STOP.equals(command) ? new VeluxProductPosition() : targetLevel;
                } else if (command instanceof PercentType) {
                    LOGGER.trace("handleCommand(): found PercentType.{} command", command);
                    PercentType ptCommand = (PercentType) command;
                    if (veluxActuator.isInverted()) {
                        ptCommand = new PercentType(PercentType.HUNDRED.intValue() - ptCommand.intValue());
                    }
                    LOGGER.trace("handleCommand(): found command to set level to {}.", ptCommand);
                    targetLevel = new VeluxProductPosition(ptCommand);
                }
            } else if (CHANNEL_ACTUATOR_STATE.equals(channelId)) {
                if (command instanceof OnOffType) {
                    LOGGER.trace("handleCommand(): found OnOffType.{} command.", command);
                    targetLevel = OnOffType.OFF.equals(command) ^ veluxActuator.isInverted()
                            ? new VeluxProductPosition(PercentType.ZERO)
                            : new VeluxProductPosition(PercentType.HUNDRED);
                }
            }
            if (targetLevel.equals(VeluxProductPosition.UNKNOWN)) {
                LOGGER.info("handleCommand({},{}): ignoring command.", channelUID.getAsString(), command);
                break;
            }
            LOGGER.debug("handleCommand(): sending command with target level {}.", targetLevel);
            new VeluxBridgeRunProductCommand().sendCommand(thisBridgeHandler.thisBridge,
                    veluxActuator.getProductBridgeIndex().toInt(), targetLevel,
                    VeluxBindingConstants.newFunctionalParameterArray());
            LOGGER.trace("handleCommand(): The new shutter level will be send through the home monitoring events.");
            if (thisBridgeHandler.bridgeParameters.actuators.autoRefresh(thisBridgeHandler.thisBridge)) {
                LOGGER.trace("handleCommand(): position of actuators are updated.");
            }
        } while (false); // common exit
        return newValue;
    }
}
