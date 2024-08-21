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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.VeluxBindingProperties;
import org.openhab.binding.velux.internal.VeluxItemType;
import org.openhab.binding.velux.internal.handler.utils.StateUtils;
import org.openhab.binding.velux.internal.handler.utils.ThingConfiguration;
import org.openhab.binding.velux.internal.things.VeluxScene;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <B>Channel-specific retrieval and modification.</B>
 * <P>
 * This class implements the Channel <B>check</B> of the Thing <B>klf200</B> :
 * <UL>
 * <LI><I>Velux</I> <B>bridge</B> &rarr; <B>OpenHAB</B>:
 * <P>
 * Information retrieval by method {@link #handleRefresh}.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
final class ChannelBridgeCheck extends ChannelHandlerTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelBridgeCheck.class);

    /*
     * ************************
     * ***** Constructors *****
     */

    // Suppress default constructor for non-instantiability

    private ChannelBridgeCheck() {
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
            LOGGER.trace("handleCommand(): loop through all existing scenes.");
            List<String> unusedScenes = new ArrayList<>();
            for (VeluxScene scene : thisBridgeHandler.bridgeParameters.scenes.getChannel().existingScenes.values()) {
                boolean found = false;
                LOGGER.trace("handleCommand(): .loop through all handled channels.");
                for (ChannelUID thisChannelUID : BridgeChannels.getAllChannelUIDs(thisBridgeHandler)) {
                    LOGGER.trace("handleCommand(): evaluating ChannelUID {}.", thisChannelUID);
                    VeluxItemType thisItemType = VeluxItemType.getByThingAndChannel(
                            thisBridgeHandler.thingTypeUIDOf(thisChannelUID), thisChannelUID.getId());
                    if (!thisItemType.equals(VeluxItemType.SCENE_ACTION)) {
                        LOGGER.trace("handleCommand(): ignoring non SCENE_ACTION.");
                        continue;
                    }
                    if (!ThingConfiguration.exists(thisBridgeHandler, thisChannelUID,
                            VeluxBindingProperties.PROPERTY_SCENE_NAME)) {
                        LOGGER.trace("handleCommand(): aborting processing as scene name is not set.");
                        break;
                    }
                    String sceneName = (String) ThingConfiguration.getValue(thisBridgeHandler, thisChannelUID,
                            VeluxBindingProperties.PROPERTY_SCENE_NAME);
                    LOGGER.trace("handleCommand(): comparing {} with {}.", scene.getName().toString(), sceneName);
                    if (scene.getName().toString().equals(sceneName)) {
                        LOGGER.trace("handleCommand(): scene {} used within item {}.", scene.getName(),
                                thisChannelUID.getAsString());
                        found = true;
                    }
                }
                if (!found) {
                    unusedScenes.add(scene.getName().toString());
                    LOGGER.trace("handleCommand(): scene {} is currently unused.", scene.getName());
                }
            }
            String result;
            if (!unusedScenes.isEmpty()) {
                result = thisBridgeHandler.localization.getText("channelValue.check-integrity-failed")
                        .concat(unusedScenes.toString());
            } else {
                result = thisBridgeHandler.localization.getText("channelValue.check-integrity-ok");
            }
            LOGGER.debug("{}", result);
            newState = StateUtils.createState(result);
        } while (false); // common exit
        return newState;
    }
}
