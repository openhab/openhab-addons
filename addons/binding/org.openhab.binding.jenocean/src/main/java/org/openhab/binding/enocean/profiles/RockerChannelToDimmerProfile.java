/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.profiles;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.enocean.handler.EnOceanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RockerChannelToDimmerProfile} transforms rocker switch channel events into dimmer commands.
 *
 * @author Jan Kemmler - Initial contribution
 */
@NonNullByDefault
public class RockerChannelToDimmerProfile implements TriggerProfile {

    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(EnOceanHandler.class);

    private final ProfileCallback callback;

    private final ScheduledExecutorService scheduler;

    @Nullable
    private ScheduledFuture<?> dimmFuture;
    @Nullable
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

    @SuppressWarnings("null")
    RockerChannelToDimmerProfile(ProfileCallback callback) {
        this.callback = callback;

        try {
            this.scheduler = ThreadPoolManager.getScheduledPool("ProfilePool");
        } catch (Exception e) {
            this.logger.warn("Unable to create rocker channel to dimmer profile. {}", e);
            throw e;
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return EnOceanProfiles.ROCKER_TO_DIMMER;
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
                if (null != dimmFuture) {
                    dimmFuture.cancel(false);
                }
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
