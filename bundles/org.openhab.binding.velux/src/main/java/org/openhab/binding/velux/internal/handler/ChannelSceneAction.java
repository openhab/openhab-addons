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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.VeluxBindingProperties;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeRunScene;
import org.openhab.binding.velux.internal.handler.utils.ThingConfiguration;
import org.openhab.binding.velux.internal.things.VeluxProductVelocity;
import org.openhab.binding.velux.internal.things.VeluxScene;
import org.openhab.binding.velux.internal.things.VeluxScene.SceneName;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <B>Channel-specific retrieval and modification.</B>
 * <P>
 * This class implements the Channel <B>action</B> of the Thing <B>scene</B> :
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
final class ChannelSceneAction extends ChannelHandlerTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelSceneAction.class);

    /*
     * ************************
     * ***** Constructors *****
     */

    // Suppress default constructor for non-instantiability

    private ChannelSceneAction() {
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
            if (command != OnOffType.ON) {
                LOGGER.trace("handleCommand(): ignoring OFF command.");
                break;
            }
            if (!ThingConfiguration.exists(thisBridgeHandler, channelUID, VeluxBindingProperties.PROPERTY_SCENE_NAME)) {
                LOGGER.trace("handleCommand(): aborting processing as scene name is not set.");
                break;
            }
            String sceneName = (String) ThingConfiguration.getValue(thisBridgeHandler, channelUID,
                    VeluxBindingProperties.PROPERTY_SCENE_NAME);
            if (!thisBridgeHandler.bridgeParameters.scenes.getChannel().existingScenes
                    .isRegistered(new SceneName(sceneName))) {
                LOGGER.info("handleCommand({},{}): cannot activate unknown scene: {}.", channelUID.getAsString(),
                        command, sceneName);
                break;
            }
            String velocityName = (String) ThingConfiguration.getValue(thisBridgeHandler, channelUID,
                    VeluxBindingProperties.PROPERTY_SCENE_VELOCITY);
            LOGGER.debug("handleCommand(): activating known scene {}.", sceneName);
            VeluxScene thisScene = thisBridgeHandler.bridgeParameters.scenes.getChannel().existingScenes
                    .get(new SceneName(sceneName));
            LOGGER.trace("handleCommand(): execution of scene {}.", thisScene);
            if (VeluxProductVelocity.getByName(velocityName) == VeluxProductVelocity.UNDEFTYPE) {
                new VeluxBridgeRunScene().execute(thisBridgeHandler.thisBridge,
                        thisScene.getBridgeSceneIndex().toInt());
            } else {
                LOGGER.trace("handleCommand(): using velocity {}.",
                        VeluxProductVelocity.getByName(velocityName).toString());
                new VeluxBridgeRunScene().execute(thisBridgeHandler.thisBridge, thisScene.getBridgeSceneIndex().toInt(),
                        VeluxProductVelocity.getByName(velocityName).getVelocity());
            }
            LOGGER.trace("handleCommand(): execution of scene finished.");
        } while (false); // common exit
        return newValue;
    }
}
