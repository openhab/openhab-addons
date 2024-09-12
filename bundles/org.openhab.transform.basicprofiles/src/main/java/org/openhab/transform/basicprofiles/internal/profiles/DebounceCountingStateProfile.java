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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.DEBOUNCE_COUNTING_UID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.transform.basicprofiles.internal.config.DebounceCountingStateProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debounces a {@link State} by counting.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class DebounceCountingStateProfile implements StateProfile {

    private final Logger logger = LoggerFactory.getLogger(DebounceCountingStateProfile.class);

    private final ProfileCallback callback;

    private final int numberOfChanges;
    private int counter;

    private @Nullable State previousState;

    public DebounceCountingStateProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        DebounceCountingStateProfileConfig config = context.getConfiguration()
                .as(DebounceCountingStateProfileConfig.class);
        logger.debug("Configuring profile with parameters: [numberOfChanges='{}']", config.numberOfChanges);

        if (config.numberOfChanges < 0) {
            throw new IllegalArgumentException(String
                    .format("numberOfChanges has to be a non-negative integer but was '%d'.", config.numberOfChanges));
        }

        this.numberOfChanges = config.numberOfChanges;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return DEBOUNCE_COUNTING_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        this.previousState = state;
    }

    @Override
    public void onCommandFromItem(Command command) {
        // no-op
    }

    @Override
    public void onCommandFromHandler(Command command) {
        // no-op
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        logger.debug("Received state update from Handler");
        State localPreviousState = previousState;
        if (localPreviousState == null) {
            callback.sendUpdate(state);
            previousState = state;
        } else {
            if (state.equals(localPreviousState.as(state.getClass()))) {
                logger.debug("Item state back to previous state, reset counter");
                callback.sendUpdate(localPreviousState);
                counter = 0;
            } else {
                logger.debug("Item state changed, counting");
                counter++;
                if (numberOfChanges < counter) {
                    logger.debug("Item state has changed {} times, send new state to Item", counter);
                    callback.sendUpdate(state);
                    previousState = state;
                    counter = 0;
                } else {
                    callback.sendUpdate(localPreviousState);
                }
            }
        }
    }
}
