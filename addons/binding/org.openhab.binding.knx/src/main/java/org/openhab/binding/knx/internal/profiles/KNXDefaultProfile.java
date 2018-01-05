/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.profiles;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * Default profile for the KNX binding.
 * <p>
 * Please note that due to the fact that the same handler is used for "standard" and "control" types, the handlers
 * always send {@link Command}s but here they are turned into {@link State}s if possible.
 * <p>
 * {@link State} on the other hand are ignored completely.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class KNXDefaultProfile implements StateProfile {

    public static final ProfileTypeUID UID = new ProfileTypeUID("knx", "default");

    private final ProfileCallback callback;

    public KNXDefaultProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public @NonNull ProfileTypeUID getProfileTypeUID() {
        return UID;
    }

    @Override
    public void onStateUpdateFromItem(@NonNull State state) {
        // no-op
    }

    @Override
    public void onCommandFromItem(@NonNull Command command) {
        callback.handleCommand(command);
    }

    @Override
    public void onCommandFromHandler(@NonNull Command command) {
        if (command instanceof State) {
            callback.sendUpdate((State) command);
        }
    }

    @Override
    public void onStateUpdateFromHandler(@NonNull State state) {
        // no-op
    }

}
