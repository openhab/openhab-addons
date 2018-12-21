/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.veluxklf200.internal.VeluxKLF200BindingConstants;
import org.openhab.binding.veluxklf200.internal.commands.KlfCmdExecuteScene;
import org.openhab.binding.veluxklf200.internal.commands.KlfCmdStopScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles interactions with a Scene configured on the KLF200.
 *
 * @author MFK - Initial Contribution
 */
public class KLF200SceneHandler extends KLF200BaseThingHandler {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(KLF200SceneHandler.class);

    /**
     * Constructor
     *
     * @param thing the thing
     */
    public KLF200SceneHandler(Thing thing) {
        super(thing);
    }

    /*
     * By default the 'trigger_scene' switch is set to be OFF. When a user interacts and turns it on, the scene is
     * executed. The handler blocks until the scene has executed fully and then updates the state of the item to OFF
     * again.
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.ThingHandler#handleCommand(org.eclipse.smarthome.core.thing.ChannelUID,
     * org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling scene state refresh command.");
        if (command == RefreshType.REFRESH) {
            switch (channelUID.getId()) {
                case VeluxKLF200BindingConstants.KLF200_TRIGGER_SCENE: {
                    updateState(channelUID, OnOffType.OFF);
                    break;
                }
            }
        } else {
            switch (channelUID.getId()) {
                case VeluxKLF200BindingConstants.KLF200_TRIGGER_SCENE:
                    Thing thing = getThing();
                    if (command.equals(OnOffType.ON)) {
                        logger.debug("Trigger Scene ID:{} {}", Integer.valueOf(channelUID.getThingUID().getId()),
                                thing.getLabel());
                        KlfCmdExecuteScene execScene = new KlfCmdExecuteScene(
                                (byte) Integer.valueOf(channelUID.getThingUID().getId()).intValue());
                        getKLFCommandProcessor().dispatchCommand(execScene);
                        scheduler.execute(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (execScene) {
                                    try {
                                        execScene.wait();
                                    } catch (InterruptedException e) {
                                        logger.warn("Expected execption while waiting for scene to execute: {}", e);
                                    }
                                    updateState(channelUID, OnOffType.OFF);
                                }
                            }
                        });

                    } else if (command.equals(OnOffType.OFF)) {
                        logger.debug("Stop Scene ID:{} {}", Integer.valueOf(channelUID.getThingUID().getId()),
                                thing.getLabel());
                        getKLFCommandProcessor().executeCommand(new KlfCmdStopScene(
                                (byte) Integer.valueOf(channelUID.getThingUID().getId()).intValue()));
                    }
            }
        }
    }
}
