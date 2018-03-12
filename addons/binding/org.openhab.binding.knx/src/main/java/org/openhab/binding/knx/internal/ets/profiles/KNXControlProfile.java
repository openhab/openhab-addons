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
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * This is the default implementation for a control profile.
 *
 * In contrast to the {@link SystemDefaultProfile} it does not forward any state updates from the framework to the
 * ThingHandler, or the other way around. Instead, it only accepts {@link Command}s and then forwards those to the
 * {@link ThingHandler} or the framework.
 * <p>
 * This allows devices to be operated in an "execution-only" mode, whereby they only take commands that are explicitly
 * given by the framework and ignore state updates that might come from the Item that is for example linked to other
 * Channels
 *
 * @author Karel Goderis - Initial contribution
 *
 */
@NonNullByDefault
public class KNXControlProfile implements StateProfile {

    private final ProfileCallback callback;

    public KNXControlProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return KNXProfiles.CONTROL;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // no-op
    }

    @Override
    public void onCommandFromHandler(Command command) {
        callback.sendCommand(command);
    }

    @Override
    public void onCommandFromItem(Command command) {
        callback.handleCommand(command);
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        // no-op
    }

}
