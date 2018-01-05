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
 * Profile for KNX devices controlling devices of other bindings.
 * <p>
 * As opposed to the {@link KNXDefaultProfile}, {@link Command}s coming from the handler here really are meant to be
 * {@link Command}s.
 * <p>
 * {@link State} updates from items however are forwarded to the handlers as commands.
 * <p>
 * All other communication is ignored.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class KNXControlProfile implements StateProfile {

    public static final ProfileTypeUID UID = new ProfileTypeUID("knx", "control");

    private final ProfileCallback callback;

    public KNXControlProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public @NonNull ProfileTypeUID getProfileTypeUID() {
        return UID;
    }

    @Override
    public void onStateUpdateFromItem(@NonNull State state) {
        if (state instanceof Command) {
            callback.handleCommand((Command) state);
        }
    }

    @Override
    public void onCommandFromItem(@NonNull Command command) {
        // no-op
    }

    @Override
    public void onCommandFromHandler(@NonNull Command command) {
        callback.sendCommand(command);
    }

    @Override
    public void onStateUpdateFromHandler(@NonNull State state) {
        // no-op
    }

}
