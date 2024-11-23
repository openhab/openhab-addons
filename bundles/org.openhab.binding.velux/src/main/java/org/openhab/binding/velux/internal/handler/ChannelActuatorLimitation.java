/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.VeluxBindingProperties;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeGetLimitation;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeSetLimitation;
import org.openhab.binding.velux.internal.handler.utils.ThingConfiguration;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;
import org.openhab.binding.velux.internal.things.VeluxProductSerialNo;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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
final class ChannelActuatorLimitation extends ChannelHandlerTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelActuatorLimitation.class);

    /*
     * ************************
     * ***** Constructors *****
     */

    // Suppress default constructor for non-instantiability

    private ChannelActuatorLimitation() {
        throw new AssertionError();
    }

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
            boolean setMinimum = channelId.length() == 0;
            if (thisBridgeHandler.bridgeParameters.actuators.autoRefresh(thisBridgeHandler.thisBridge)) {
                LOGGER.trace("handleRefresh(): there are some existing products.");
            }
            if (!ThingConfiguration.exists(thisBridgeHandler, channelUID,
                    VeluxBindingProperties.CONFIG_ACTUATOR_SERIALNUMBER)) {
                LOGGER.trace("handleRefresh(): aborting processing as {} is not set.",
                        VeluxBindingProperties.CONFIG_ACTUATOR_SERIALNUMBER);
                break;
            }
            String actuatorSerial = (String) ThingConfiguration.getValue(thisBridgeHandler, channelUID,
                    VeluxBindingProperties.CONFIG_ACTUATOR_SERIALNUMBER);
            LOGGER.trace("handleRefresh(): actuatorSerial={}", actuatorSerial);

            // Handle value inversion
            boolean propertyInverted = false;
            if (ThingConfiguration.exists(thisBridgeHandler, channelUID,
                    VeluxBindingProperties.PROPERTY_ACTUATOR_INVERTED)) {
                propertyInverted = (boolean) ThingConfiguration.getValue(thisBridgeHandler, channelUID,
                        VeluxBindingProperties.PROPERTY_ACTUATOR_INVERTED);
            }
            boolean isInverted = propertyInverted || VeluxProductSerialNo.indicatesRevertedValues(actuatorSerial);
            LOGGER.trace("handleRefresh(): isInverted={}.", isInverted);
            actuatorSerial = VeluxProductSerialNo.cleaned(actuatorSerial);

            if (!thisBridgeHandler.bridgeParameters.actuators.getChannel().existingProducts
                    .isRegistered(actuatorSerial)) {
                LOGGER.info("handleRefresh(): cannot work on unknown actuator with serial {}.", actuatorSerial);
                break;
            }
            LOGGER.trace("handleRefresh(): fetching actuator for {}.", actuatorSerial);
            VeluxProduct thisProduct = thisBridgeHandler.bridgeParameters.actuators.getChannel().existingProducts
                    .get(actuatorSerial);
            LOGGER.trace("handleRefresh(): found actuator {}.", thisProduct);

            VeluxBridgeGetLimitation getLimitation = new VeluxBridgeGetLimitation();
            boolean success;
            if (setMinimum) {
                success = getLimitation.getMinimumLimitation(thisBridgeHandler.thisBridge,
                        thisProduct.getBridgeProductIndex().toInt());
            } else {
                success = getLimitation.getMaximumLimitation(thisBridgeHandler.thisBridge,
                        thisProduct.getBridgeProductIndex().toInt());
            }
            if (!success) {
                LOGGER.info("handleRefresh(): retrieval failed.");
                break;
            }
            VeluxProductPosition position = getLimitation.getLimitation();
            if (position.isValid()) {
                PercentType positionAsPercent = position.getPositionAsPercentType(isInverted);
                LOGGER.trace("handleRefresh(): found limitation of actuator at level {}.", positionAsPercent);
                newState = positionAsPercent;
            } else {
                LOGGER.trace("handleRefresh(): limitation level of actuator is unknown.");
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
            boolean setMinimum = channelId.length() == 0;
            if (thisBridgeHandler.bridgeParameters.actuators.autoRefresh(thisBridgeHandler.thisBridge)) {
                LOGGER.trace("handleCommand(): there are some existing products.");
            }
            if (!ThingConfiguration.exists(thisBridgeHandler, channelUID,
                    VeluxBindingProperties.CONFIG_ACTUATOR_SERIALNUMBER)) {
                LOGGER.trace("handleCommand(): aborting processing as {} is not set.",
                        VeluxBindingProperties.CONFIG_ACTUATOR_SERIALNUMBER);
                break;
            }
            String actuatorSerial = (String) ThingConfiguration.getValue(thisBridgeHandler, channelUID,
                    VeluxBindingProperties.CONFIG_ACTUATOR_SERIALNUMBER);
            LOGGER.trace("handleCommand(): actuatorSerial={}", actuatorSerial);

            // Handle value inversion
            boolean propertyInverted = false;
            if (ThingConfiguration.exists(thisBridgeHandler, channelUID,
                    VeluxBindingProperties.PROPERTY_ACTUATOR_INVERTED)) {
                propertyInverted = (boolean) ThingConfiguration.getValue(thisBridgeHandler, channelUID,
                        VeluxBindingProperties.PROPERTY_ACTUATOR_INVERTED);
            }
            boolean isInverted = propertyInverted || VeluxProductSerialNo.indicatesRevertedValues(actuatorSerial);
            LOGGER.trace("handleCommand(): isInverted={}.", isInverted);
            actuatorSerial = VeluxProductSerialNo.cleaned(actuatorSerial);

            if (!thisBridgeHandler.bridgeParameters.actuators.getChannel().existingProducts
                    .isRegistered(actuatorSerial)) {
                LOGGER.info("handleCommand(): cannot work on unknown actuator with serial {}.", actuatorSerial);
                break;
            }
            LOGGER.trace("handleCommand(): fetching actuator for {}.", actuatorSerial);
            VeluxProduct thisProduct = thisBridgeHandler.bridgeParameters.actuators.getChannel().existingProducts
                    .get(actuatorSerial);
            LOGGER.trace("handleCommand(): found actuator {}.", thisProduct);

            if (!(command instanceof PercentType)) {
                LOGGER.trace("handleCommand(): aborting processing as command is not of type PercentType.");
            }

            LOGGER.trace("handleCommand(): found command of type PercentType.");
            VeluxProductPosition posCommand = new VeluxProductPosition((PercentType) command, isInverted);
            LOGGER.trace("handleCommand(): found command to set level to {}.", posCommand);

            if (setMinimum) {
                new VeluxBridgeSetLimitation().setMinimumLimitation(thisBridgeHandler.thisBridge,
                        thisProduct.getBridgeProductIndex().toInt(), posCommand);
            } else {
                new VeluxBridgeSetLimitation().setMaximumLimitation(thisBridgeHandler.thisBridge,
                        thisProduct.getBridgeProductIndex().toInt(), posCommand);
            }
        } while (false); // common exit
        LOGGER.trace("handleCommand() returns {}.", newValue);

        return newValue;
    }
}
