/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.profiles;

import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.eclipse.smarthome.core.types.State;

/**
 * Profile to convert rockerswitch events into RollershutterItem commands (up, down)
 *
 * @author Daniel Weber - Initial contribution
 */
public class RockerSwitchToRollershutterProfile implements TriggerProfile {

    private final ProfileCallback callback;

    RockerSwitchToRollershutterProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return EnOceanProfileTypes.RockerSwitchToRollershutter;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // nothing to do here => channel is readonly

    }

    @Override
    public void onTriggerFromHandler(String event) {
        if (event.equalsIgnoreCase(CommonTriggerEvents.DIR1_PRESSED)) {
            callback.sendUpdate(UpDownType.UP);
        } else if (event.equalsIgnoreCase(CommonTriggerEvents.DIR2_PRESSED)) {
            callback.sendCommand(UpDownType.DOWN);
        }
    }

}
