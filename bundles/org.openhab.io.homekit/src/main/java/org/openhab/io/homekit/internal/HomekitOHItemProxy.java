/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.io.homekit.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Proxy class that can collect multiple commands for the same openHAB item and merge them to one command.
 * e.g. Hue and Saturation update for Color Item
 * 
 * @author Eugen Freiter Initial contribution
 *
 */

public class HomekitOHItemProxy {
    private static final Logger logger = LoggerFactory.getLogger(HomekitOHItemProxy.class);
    public static final String HUE_COMMAND = "hue";
    public static final String SATURATION_COMMAND = "saturation";
    public static final String BRIGHTNESS_COMMAND = "brightness";
    public static final String ON_COMMAND = "on";

    // delay, how long wait for further commands.
    // TODO: make it configurable ?
    private final int DELAY = 50;

    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(ThreadPoolManager.THREAD_POOL_NAME_COMMON);
    private ScheduledFuture<?> future;

    private final Item item;

    @Nullable
    private ConcurrentHashMap<String, State> commandCache;

    public HomekitOHItemProxy(final Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    private void sendCommand() {
        if (item instanceof ColorItem) {
            final State currentState = item.getState() instanceof UnDefType ? HSBType.BLACK : item.getState();
            final State hue = commandCache.containsKey(HUE_COMMAND) ? commandCache.remove(HUE_COMMAND)
                    : ((HSBType) currentState).getHue();
            final State saturation = commandCache.containsKey(SATURATION_COMMAND)
                    ? commandCache.remove(SATURATION_COMMAND)
                    : ((HSBType) currentState).getSaturation();
            final State brightness = commandCache.containsKey(BRIGHTNESS_COMMAND)
                    ? commandCache.remove(BRIGHTNESS_COMMAND)
                    : ((HSBType) currentState).getBrightness();
            ((ColorItem) item).send(new HSBType((DecimalType) hue, (PercentType) saturation, (PercentType) brightness));
            logger.trace("send HSB command for item {} with following values h {} s{} b{}", item, hue, saturation,
                    brightness);
        } else if (item instanceof DimmerItem) { // not ColorItem but DimmerItem
            final State on = commandCache.remove(ON_COMMAND);
            final State brightness = commandCache.remove(BRIGHTNESS_COMMAND);

            // if "ON" command received we dont need to send brightness.
            // TODO: make brightness suppression on "On" configurable.
            if (on != null) {
                logger.trace("send OnOff command for item {} with value {}", item, on);
                ((DimmerItem) item).send((OnOffType) on);
            } else {
                if (brightness != null) {
                    logger.trace("send brightness command for item {} with value {}", item, brightness);
                    ((DimmerItem) item).send((PercentType) brightness);
                }
            }
        }
    }

    public synchronized void sendCommandProxy(final String commandType, final State state) {
        if (commandCache == null) {
            // we dont need commandCache for all item types, therefore late instantiation here only for item we use
            // commandProxy
            commandCache = new ConcurrentHashMap<>();
        }
        commandCache.put(commandType, state);

        logger.trace("add command to command cache: item {}, command type {}, command state {}. cache state after: {}",
                this, commandType, state, commandCache);

        // if cache has already HUE+SATURATION or BRIGTHNESS+ON then we dont expect any further relevant command
        // send the command immediately
        if ((commandCache.containsKey(HUE_COMMAND) && commandCache.containsKey(SATURATION_COMMAND))
                || (commandCache.containsKey(BRIGHTNESS_COMMAND) && commandCache.containsKey(ON_COMMAND))) {
            if (future != null)
                future.cancel(true);
            sendCommand();
            return;
        }

        // it timer is not there create one. this will ensure that we will send command out even if no follow up
        // commands were received.
        if (future == null || future.isDone()) {
            future = scheduler.schedule(() -> {
                logger.trace("timer is over, sending the command");
                sendCommand();
            }, DELAY, TimeUnit.MILLISECONDS);
        }
    }
}
