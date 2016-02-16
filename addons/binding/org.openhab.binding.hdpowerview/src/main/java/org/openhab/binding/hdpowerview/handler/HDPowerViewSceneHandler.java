/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview.handler;

import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hdpowerview.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.config.HDPowerViewSceneConfiguration;

/**
 * Handles commands for an HD Power View Scene
 *
 * @author Andy Lintner
 */
public class HDPowerViewSceneHandler extends AbstractHubbedThingHandler {

    public HDPowerViewSceneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateState(HDPowerViewBindingConstants.CHANNEL_SCENE_ACTIVATE, OnOffType.OFF);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(HDPowerViewBindingConstants.CHANNEL_SCENE_ACTIVATE)) {
            if (command.equals(OnOffType.ON)) {
                try {
                    activate();
                } finally {
                    scheduler.schedule(new Runnable() {

                        @Override
                        public void run() {
                            updateState(HDPowerViewBindingConstants.CHANNEL_SCENE_ACTIVATE, OnOffType.OFF);
                        }
                    }, 5, TimeUnit.SECONDS);
                }
            }
        }
    }

    public void setOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    private int getSceneId() {
        return getConfigAs(HDPowerViewSceneConfiguration.class).id;
    }

    private void activate() {
        getBridgeHandler().getWebTargets().activateScene(getSceneId());
    }

}
