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

    public static final int DIMMER_MODE_NONE = 0;
    public static final int DIMMER_MODE_FILTER_BRIGHTNESS_100 = 1;
    public static final int DIMMER_MODE_FILTER_ON = 2;
    public static final int DIMMER_MODE_FILTER_ON_EXCEPT_BRIGHTNESS_100 = 3;

    private static final int DEFAULT_DELAY = 50;

    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(ThreadPoolManager.THREAD_POOL_NAME_COMMON);
    private ScheduledFuture<?> future;

    private final Item item;

    private int dimmerMode = DIMMER_MODE_NONE;

    // delay, how long wait for further commands. in ms.
    private int delay = DEFAULT_DELAY;
    private ConcurrentHashMap<String, State> commandCache = new ConcurrentHashMap<>();

    public HomekitOHItemProxy(final Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public void setDimmerMode(int mode) {
        dimmerMode = mode;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    private void sendCommand() {
        final OnOffType on = (OnOffType) commandCache.remove(ON_COMMAND);
        final PercentType brightness = (PercentType) commandCache.remove(BRIGHTNESS_COMMAND);
        final DecimalType hue = (DecimalType) commandCache.remove(HUE_COMMAND);
        final PercentType saturation = (PercentType) commandCache.remove(SATURATION_COMMAND);

        if (on != null) {
            // always sends OFF.
            // sends ON only if
            // - DIMMER_MODE_NONE is enabled OR
            // - DIMMER_MODE_FILTER_BRIGHTNESS_100 is enabled OR
            // - DIMMER_MODE_FILTER_ON_EXCEPT100 is not enabled and brightness is null or below 100
            if ((on == OnOffType.OFF) || (dimmerMode == DIMMER_MODE_NONE)
                    || (dimmerMode == DIMMER_MODE_FILTER_BRIGHTNESS_100)
                    || ((dimmerMode == DIMMER_MODE_FILTER_ON_EXCEPT_BRIGHTNESS_100)
                            && ((brightness == null) || (brightness.intValue() == 100)))) {
                logger.trace("send OnOff command for item {} with value {}", item, on);
                ((DimmerItem) item).send(on);
            }
        }

        // if hue or saturation present, send an HSBType state update. no filter applied for HUE & Saturation
        if ((hue != null) || (saturation != null)) {
            if (item instanceof ColorItem) {
                // logic for ColorItem = combine hue, saturation and brightness update to one command
                final HSBType currentState = item.getState() instanceof UnDefType ? HSBType.BLACK
                        : (HSBType) item.getState();
                ((ColorItem) item).send(new HSBType(hue != null ? hue : currentState.getHue(),
                        saturation != null ? saturation : currentState.getSaturation(),
                        brightness != null ? brightness : currentState.getBrightness()));
                logger.trace("send HSB command for item {} with following values hue={} saturation={} brightness={}",
                        item, hue, saturation, brightness);
            }
        } else if ((brightness != null) && (item instanceof DimmerItem)) {
            // sends brightness:
            // - DIMMER_MODE_NONE
            // - DIMMER_MODE_FILTER_ON
            // - other modes (DIMMER_MODE_FILTER_BRIGHTNESS_100 or DIMMER_MODE_FILTER_ON_EXCEPT_BRIGHTNESS_100) and
            // <100%.
            if ((dimmerMode == DIMMER_MODE_NONE) || (dimmerMode == DIMMER_MODE_FILTER_ON)
                    || (brightness.intValue() < 100)) {
                logger.trace("send brightness command for item {} with value {}", item, brightness);
                ((DimmerItem) item).send(brightness);
            }
        }
        commandCache.clear();
    }

    public synchronized void sendCommandProxy(final String commandType, final State state) {
        commandCache.put(commandType, state);
        logger.trace("add command to command cache: item {}, command type {}, command state {}. cache state after: {}",
                this, commandType, state, commandCache);

        // if cache has already HUE+SATURATION or BRIGHTNESS+ON then we don't expect any further relevant command
        if ((commandCache.containsKey(HUE_COMMAND) && commandCache.containsKey(SATURATION_COMMAND))
                || (commandCache.containsKey(BRIGHTNESS_COMMAND) && commandCache.containsKey(ON_COMMAND))) {
            if (future != null)
                future.cancel(true);
            sendCommand();
            return;
        }

        // if timer is not already set, create a new one to ensure that the command command is send even if no follow up
        // commands are received.
        if (future == null || future.isDone()) {
            future = scheduler.schedule(() -> {
                logger.trace("timer is over, sending the command");
                sendCommand();
            }, delay, TimeUnit.MILLISECONDS);
        }
    }
}
