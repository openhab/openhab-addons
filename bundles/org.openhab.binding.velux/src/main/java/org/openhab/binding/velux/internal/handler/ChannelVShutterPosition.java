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

import static org.openhab.binding.velux.internal.VeluxBindingConstants.CHANNEL_VSHUTTER_POSITION;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.VeluxBindingProperties;
import org.openhab.binding.velux.internal.VeluxItemType;
import org.openhab.binding.velux.internal.VeluxRSBindingConfig;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeRunScene;
import org.openhab.binding.velux.internal.handler.utils.ThingConfiguration;
import org.openhab.binding.velux.internal.handler.utils.ThingProperty;
import org.openhab.binding.velux.internal.things.VeluxScene;
import org.openhab.binding.velux.internal.things.VeluxScene.SceneName;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <B>Channel-specific retrieval and modification.</B>
 * <P>
 * This class implements the Channel <B>position</B> of the Thing <B>vshutter</B> :
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
final class ChannelVShutterPosition extends ChannelHandlerTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelVShutterPosition.class);

    /*
     * ************************
     * ***** Constructors *****
     */

    // Suppress default constructor for non-instantiability

    private ChannelVShutterPosition() {
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
        assert (channelId == CHANNEL_VSHUTTER_POSITION);
        State newState = null;
        do { // just for common exit
            if (!ThingConfiguration.exists(thisBridgeHandler, channelUID,
                    VeluxBindingProperties.PROPERTY_VSHUTTER_CURRENTLEVEL)) {
                LOGGER.trace("handleRefresh(): aborting processing as current scene level is not set.");
                break;
            }
            // Don't know why OH2 returns BigDecimal.
            BigDecimal rollershutterLevelBC = (BigDecimal) ThingConfiguration.getValue(thisBridgeHandler, channelUID,
                    VeluxBindingProperties.PROPERTY_VSHUTTER_CURRENTLEVEL);
            int rollershutterLevel = rollershutterLevelBC.intValue();
            LOGGER.trace("handleRefresh(): current level is {}.", rollershutterLevel);
            newState = new PercentType(rollershutterLevel);
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
        // ThingProperty sceneLevels
        if (!ThingConfiguration.exists(thisBridgeHandler, channelUID,
                VeluxBindingProperties.PROPERTY_VSHUTTER_SCENELEVELS)) {
            LOGGER.trace("handleCommand(): aborting processing as scene levels are not set.");
            return newValue;
        }
        String sceneLevels = (String) ThingConfiguration.getValue(thisBridgeHandler, channelUID,
                VeluxBindingProperties.PROPERTY_VSHUTTER_SCENELEVELS);
        // ThingProperty currentLevel
        if (!ThingConfiguration.exists(thisBridgeHandler, channelUID,
                VeluxBindingProperties.PROPERTY_VSHUTTER_CURRENTLEVEL)) {
            LOGGER.trace("handleCommand(): aborting processing as current scene level is not set.");
            return newValue;
        }
        // Don't know why OH2 returns BigDecimal.
        BigDecimal rollershutterLevelBC = (BigDecimal) ThingConfiguration.getValue(thisBridgeHandler, channelUID,
                VeluxBindingProperties.PROPERTY_VSHUTTER_CURRENTLEVEL);
        int currentLevel = rollershutterLevelBC.intValue();
        LOGGER.trace("handleCommand(): current level is {}.", currentLevel);

        VeluxRSBindingConfig thisRSBindingConfig = new VeluxRSBindingConfig(VeluxItemType.VSHUTTER_POSITION,
                sceneLevels, currentLevel);

        if ((UpDownType) command == UpDownType.UP) {
            currentLevel = thisRSBindingConfig.getNextDescendingLevel();
        } else if ((UpDownType) command == UpDownType.DOWN) {
            currentLevel = thisRSBindingConfig.getNextAscendingLevel();
        } else {
            LOGGER.info("handleCommand({},{}): ignoring command.", channelUID.getAsString(), command);
            return newValue;
        }
        LOGGER.trace("handleCommand(): next level is {}.", currentLevel);
        String sceneName = thisRSBindingConfig.getSceneName();
        LOGGER.trace("handleCommand(): scene name is {}.", sceneName);
        VeluxScene thisScene2 = thisBridgeHandler.bridgeParameters.scenes.getChannel().existingScenes
                .get(new SceneName(sceneName));
        if (VeluxScene.UNKNOWN.equals(thisScene2)) {
            LOGGER.warn(
                    "handleCommand(): aborting command as scene with name {} is not registered; please check your KLF scene definitions.",
                    sceneName);
            return newValue;
        }
        newValue = new PercentType(currentLevel);
        LOGGER.trace("handleCommand(): executing scene {} with index {}.", thisScene2,
                thisScene2.getBridgeSceneIndex().toInt());
        new VeluxBridgeRunScene().execute(thisBridgeHandler.thisBridge, thisScene2.getBridgeSceneIndex().toInt());

        LOGGER.trace("handleCommand(): updating level to {}.", currentLevel);
        ThingProperty.setValue(thisBridgeHandler, channelUID, VeluxBindingProperties.PROPERTY_VSHUTTER_CURRENTLEVEL,
                thisRSBindingConfig.getLevel().toString());
        LOGGER.trace("handleCommand() returns {}.", newValue);
        return newValue;
    }
}
