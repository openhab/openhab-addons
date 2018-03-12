/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.ets.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the default implementation for a listen profile.
 *
 * In contrast to the {@link SystemDefaultProfile} it does not forward any commands or updates to the ThingHandler.
 * Instead, it takes {@link State} and {@link Command}s received from the {@link ThingHandler} to send to the
 * {@link Item}.
 * <p>
 * This allows devices to be operated as "passive informers" for information they receive from Things, i.e. have the
 * framework act as a "listener"
 * <p>
 * The ThingHandler may send commands and updates to the framework, but nothing is forwarded from the framework to the
 * ThingHandler.
 *
 * @author Karel Goderis - Initial contribution
 *
 */
@NonNullByDefault
public class KNXListenProfile implements StateProfile {

    private final Logger logger = LoggerFactory.getLogger(KNXListenProfile.class);

    private final ProfileCallback callback;

    public KNXListenProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return KNXProfiles.LISTEN;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // no-op
    }

    @Override
    public void onCommandFromHandler(Command command) {
        if (!(command instanceof State)) {
            logger.debug("The given command {} could not be transformed to a state", command);
            return;
        }
        State state = (State) command;
        callback.sendUpdate(state);
    }

    @Override
    public void onCommandFromItem(Command command) {
        // no-op
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        callback.sendUpdate(state);
    }

}
