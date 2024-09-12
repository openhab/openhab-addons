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
import org.openhab.binding.velux.internal.bridge.VeluxBridgeDetectProducts;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <B>Channel-specific retrieval and modification.</B>
 * <P>
 * This class implements the Channel <B>doDetection</B> of the Thing <B>klf200</B> :
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
final class ChannelBridgeDoDetection extends ChannelHandlerTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelBridgeDoDetection.class);

    /*
     * ************************
     * ***** Constructors *****
     */

    // Suppress default constructor for non-instantiability

    private ChannelBridgeDoDetection() {
        throw new AssertionError();
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
        if (command == OnOffType.ON) {
            LOGGER.trace("handleCommand(): about to activate veluxBridge detection mode.");
            new VeluxBridgeDetectProducts().detectProducts(thisBridgeHandler.thisBridge);
        } else {
            LOGGER.trace("handleCommand(): ignoring OFF command.");
        }
        return null;
    }
}
