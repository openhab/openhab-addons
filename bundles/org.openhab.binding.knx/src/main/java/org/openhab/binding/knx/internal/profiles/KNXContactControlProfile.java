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
package org.openhab.binding.knx.internal.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of a specialized profile for KNX contact-control-items.
 *
 * In contrast to the profile {@code FOLLOW} from {@link org.openhab.core.thing.profiles.SystemProfiles}
 * used for other *-control items, it sends to the bus also for contact items.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public class KNXContactControlProfile implements StateProfile {

    private final Logger logger = LoggerFactory.getLogger(KNXContactControlProfile.class);
    private final ProfileCallback callback;
    private final ThingRegistry thingRegistry;

    public KNXContactControlProfile(ProfileCallback callback, ThingRegistry thingRegistry) {
        this.callback = callback;
        this.thingRegistry = thingRegistry;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return KNXProfileFactory.UID_CONTACT_CONTROL;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        ChannelUID linkedChannelUID = callback.getItemChannelLink().getLinkedUID();
        logger.trace("onStateUpdateFromItem({}) to {}", state.toString(), linkedChannelUID);

        if (!(state instanceof Command)) {
            logger.debug("The given state {} could not be transformed to a command", state);
            return;
        }
        Command command = (Command) state;

        // this does not have effect for contact items
        // callback.handleCommand(command);
        // workaround is to call handleCommand of the Thing directly
        @Nullable
        Thing linkedThing = thingRegistry.get(linkedChannelUID.getThingUID());
        if (linkedThing != null) {
            @Nullable
            ThingHandler linkedThingHandler = linkedThing.getHandler();
            if (linkedThingHandler != null) {
                linkedThingHandler.handleCommand(linkedChannelUID, command);
            } else {
                logger.warn("Failed to send to {}, no ThingHandler", linkedChannelUID);
            }
        } else {
            logger.warn("Failed to send to {}, no linked Thing", linkedChannelUID);
        }
    }

    @Override
    public void onCommandFromHandler(Command command) {
        logger.trace("onCommandFromHandler {}", command.toString());
        callback.sendCommand(command);
    }

    @Override
    public void onCommandFromItem(Command command) {
        logger.trace("onCommandFromItem {}", command.toString());
        // no-op
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        logger.trace("onStateUpdateFromHandler {}", state.toString());
        // no-op
    }
}
