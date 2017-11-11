/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jenocean.profiles;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link RockerChannelToDimmerProfile} transforms rocker switch channel events into dimmer commands.
 *
 * @author Jan Kemmler - Initial contribution
 */
public class RockerChannelToDimmerProfile implements TriggerProfile {

    private final ProfileCallback callback;

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("ProfilePool");

    private ScheduledFuture<?> dimmFuture;
    private ScheduledFuture<?> timeoutFuture;

    private long pressedTime = 0;

    final class DimmerIncreaseDecreaseTask implements Runnable {
        private ProfileCallback callback;
        private Command command;

        DimmerIncreaseDecreaseTask(ProfileCallback callback, Command command) {
            this.callback = callback;
            this.command = command;
        }

        @Override
        public void run() {
            callback.sendCommand(command);
        }
    };

    RockerChannelToDimmerProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return JEnOceanProfiles.ROCKER_TO_DIMMER;
    }

    /**
     * Will be called if an item has changed its state and this information should be forwarded to the binding.
     *
     * @param state
     */
    @Override
    public void onStateUpdateFromItem(State state) {

    }

    @Override
    public void onTriggerFromHandler(String event) {
        if (new String("UP_PRESSED").equals(event)) {
            buttonPressed(IncreaseDecreaseType.INCREASE);
        } else if (new String("UP_RELEASED").equals(event)) {
            buttonReleased(OnOffType.ON);
            // scheduler.shutdownNow();
        } else if (new String("DOWN_PRESSED").equals(event)) {
            buttonPressed(IncreaseDecreaseType.DECREASE);
        } else if (new String("DOWN_RELEASED").equals(event)) {
            buttonReleased(OnOffType.OFF);
        }
    }

    private void buttonPressed(Command commandToSend) {
        if (null != timeoutFuture) {
            timeoutFuture.cancel(false);
        }
        if (null != dimmFuture) {
            dimmFuture.cancel(false);
        }

        dimmFuture = scheduler.scheduleWithFixedDelay(new DimmerIncreaseDecreaseTask(callback, commandToSend), 550, 200,
                TimeUnit.MILLISECONDS);
        timeoutFuture = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                dimmFuture.cancel(false);
            }
        }, 10000, TimeUnit.MILLISECONDS);
        pressedTime = System.currentTimeMillis();
    }

    private void buttonReleased(Command commandToSend) {
        if (null != timeoutFuture) {
            timeoutFuture.cancel(false);
        }
        if (null != dimmFuture) {
            dimmFuture.cancel(false);
        }

        if (System.currentTimeMillis() - pressedTime <= 500) {
            callback.sendCommand(commandToSend);
        }
    }

}
