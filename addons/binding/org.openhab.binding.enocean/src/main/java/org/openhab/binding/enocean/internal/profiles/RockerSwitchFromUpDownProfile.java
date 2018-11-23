/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.profiles;

import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.enocean.internal.config.EnOceanChannelVirtualRockerSwitchConfig;

/**
 * Profile to convert Rollershutter commands into EnOcean rockerswitch messages
 *
 * @author Daniel Weber - Initial contribution
 */
public class RockerSwitchFromUpDownProfile implements StateProfile {

    private final ProfileCallback callback;
    private final ProfileContext context;

    private final Integer duration;

    private String lastTriggerEvent = null;

    RockerSwitchFromUpDownProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        this.context = context;

        EnOceanChannelVirtualRockerSwitchConfig config = context.getConfiguration()
                .as(EnOceanChannelVirtualRockerSwitchConfig.class);
        duration = config.duration;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return EnOceanProfileTypes.RockerSwitchFromRollershutter;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // do not react on item state updates as they are triggered by this profile

    }

    @Override
    public void onCommandFromItem(Command command) {

        if (command instanceof UpDownType) {
            UpDownType c = (UpDownType) command;

            if (c == UpDownType.UP) {
                lastTriggerEvent = CommonTriggerEvents.DIR1_PRESSED;
                callback.handleCommand(StringType.valueOf(CommonTriggerEvents.DIR1_PRESSED));
                if (duration > 0) {
                    context.getExecutorService().schedule(
                            () -> callback.handleCommand(StringType.valueOf(CommonTriggerEvents.DIR1_RELEASED)),
                            duration, TimeUnit.MILLISECONDS);
                }
            } else {
                lastTriggerEvent = CommonTriggerEvents.DIR2_PRESSED;
                callback.handleCommand(StringType.valueOf(CommonTriggerEvents.DIR2_PRESSED));
                if (duration > 0) {
                    context.getExecutorService().schedule(
                            () -> callback.handleCommand(StringType.valueOf(CommonTriggerEvents.DIR2_RELEASED)),
                            duration, TimeUnit.MILLISECONDS);
                }
            }

        } else if (command instanceof StopMoveType) {
            StopMoveType c = (StopMoveType) command;

            if (c == StopMoveType.STOP) {
                callback.handleCommand(StringType.valueOf(lastTriggerEvent));
                String release = lastTriggerEvent.equals(CommonTriggerEvents.DIR1_PRESSED)
                        ? CommonTriggerEvents.DIR1_RELEASED
                        : CommonTriggerEvents.DIR2_RELEASED;
                if (duration > 0) {
                    context.getExecutorService().schedule(() -> callback.handleCommand(StringType.valueOf(release)),
                            duration, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    @Override
    public void onCommandFromHandler(Command command) {
        // a rocker switch command should not be submitted to item

    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        // Rocker switches do not own a state

    }
}
