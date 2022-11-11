/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.io.homekit.internal.HomekitCommandType.*;
import static org.openhab.io.homekit.internal.HomekitDimmerMode.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Proxy class that can collect multiple commands for the same openHAB item and merge them to one command.
 * e.g. Hue and Saturation update for Color Item
 * 
 * @author Eugen Freiter - Initial contribution
 *
 */
@NonNullByDefault
public class HomekitOHItemProxy {
    private final Logger logger = LoggerFactory.getLogger(HomekitOHItemProxy.class);
    private static final int DEFAULT_DELAY = 50; // in ms
    private final Item item;
    private final Item baseItem;
    private final Map<HomekitCommandType, State> commandCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(ThreadPoolManager.THREAD_POOL_NAME_COMMON);
    private @NonNullByDefault({}) ScheduledFuture<?> future;
    private HomekitDimmerMode dimmerMode = DIMMER_MODE_NORMAL;
    // delay, how long wait for further commands. in ms.
    private int delay = DEFAULT_DELAY;

    public static Item getBaseItem(Item item) {
        if (item instanceof GroupItem) {
            final GroupItem groupItem = (GroupItem) item;
            final Item baseItem = groupItem.getBaseItem();
            if (baseItem != null) {
                return baseItem;
            }
        }
        return item;
    }

    public HomekitOHItemProxy(Item item) {
        this.item = item;
        this.baseItem = getBaseItem(item);
    }

    public Item getItem() {
        return item;
    }

    public void setDimmerMode(HomekitDimmerMode mode) {
        dimmerMode = mode;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @SuppressWarnings("null")
    private void sendCommand() {
        if (!(baseItem instanceof DimmerItem)) {
            // currently supports only DimmerItem and ColorItem (which extends DimmerItem)
            logger.debug("unexpected item type {}. Only DimmerItem and ColorItem are supported.", baseItem);
            return;
        }
        final OnOffType on = (OnOffType) commandCache.remove(ON_COMMAND);
        final PercentType brightness = (PercentType) commandCache.remove(BRIGHTNESS_COMMAND);
        final DecimalType hue = (DecimalType) commandCache.remove(HUE_COMMAND);
        final PercentType saturation = (PercentType) commandCache.remove(SATURATION_COMMAND);
        final @Nullable OnOffType currentOnState = ((DimmerItem) item).getStateAs(OnOffType.class);
        if (on != null) {
            // always sends OFF.
            // sends ON only if
            // - DIMMER_MODE_NONE is enabled OR
            // - DIMMER_MODE_FILTER_BRIGHTNESS_100 is enabled OR
            // - DIMMER_MODE_FILTER_ON_EXCEPT100 is not enabled and brightness is null or below 100
            if ((on == OnOffType.OFF) || (dimmerMode == DIMMER_MODE_NORMAL)
                    || (dimmerMode == DIMMER_MODE_FILTER_BRIGHTNESS_100)
                    || ((dimmerMode == DIMMER_MODE_FILTER_ON_EXCEPT_BRIGHTNESS_100) && (currentOnState != OnOffType.ON)
                            && ((brightness == null) || (brightness.intValue() == 100)))) {
                logger.trace("send OnOff command for item {} with value {}", item, on);
                if (item instanceof GroupItem) {
                    ((GroupItem) item).send(on);
                } else {
                    ((DimmerItem) item).send(on);
                }
            }
        }

        // if hue or saturation present, send an HSBType state update. no filter applied for HUE & Saturation
        if ((hue != null) || (saturation != null)) {
            if (baseItem instanceof ColorItem) {
                sendHSBCommand((ColorItem) item, hue, saturation, brightness);
            }
        } else if ((brightness != null) && (baseItem instanceof DimmerItem)) {
            // sends brightness:
            // - DIMMER_MODE_NORMAL
            // - DIMMER_MODE_FILTER_ON
            // - other modes (DIMMER_MODE_FILTER_BRIGHTNESS_100 or DIMMER_MODE_FILTER_ON_EXCEPT_BRIGHTNESS_100) and
            // <100%.
            if ((dimmerMode == DIMMER_MODE_NORMAL) || (dimmerMode == DIMMER_MODE_FILTER_ON)
                    || (brightness.intValue() < 100) || (currentOnState == OnOffType.ON)) {
                logger.trace("send Brightness command for item {} with value {}", item, brightness);
                if (item instanceof ColorItem) {
                    sendHSBCommand((ColorItem) item, hue, saturation, brightness);
                } else if (item instanceof GroupItem) {
                    ((GroupItem) item).send(brightness);
                } else {
                    ((DimmerItem) item).send(brightness);
                }
            }
        }
        commandCache.clear();
    }

    private void sendHSBCommand(Item item, @Nullable DecimalType hue, @Nullable PercentType saturation,
            @Nullable PercentType brightness) {
        final HSBType currentState = item.getState() instanceof UnDefType ? HSBType.BLACK : (HSBType) item.getState();
        // logic for ColorItem = combine hue, saturation and brightness update to one command
        final DecimalType targetHue = hue != null ? hue : currentState.getHue();
        final PercentType targetSaturation = saturation != null ? saturation : currentState.getSaturation();
        final PercentType targetBrightness = brightness != null ? brightness : currentState.getBrightness();
        final HSBType command = new HSBType(targetHue, targetSaturation, targetBrightness);
        if (item instanceof GroupItem) {
            ((GroupItem) item).send(command);
        } else {
            ((ColorItem) item).send(command);
        }
        logger.trace("send HSB command for item {} with following values hue={} saturation={} brightness={}", item,
                targetHue, targetSaturation, targetBrightness);
    }

    public synchronized void sendCommandProxy(HomekitCommandType commandType, State state) {
        commandCache.put(commandType, state);
        logger.trace("add command to command cache: item {}, command type {}, command state {}. cache state after: {}",
                this, commandType, state, commandCache);
        // if cache has already HUE+SATURATION or BRIGHTNESS+ON then we don't expect any further relevant command
        if (((baseItem instanceof ColorItem) && commandCache.containsKey(HUE_COMMAND)
                && commandCache.containsKey(SATURATION_COMMAND))
                || (commandCache.containsKey(BRIGHTNESS_COMMAND) && commandCache.containsKey(ON_COMMAND))) {
            if (future != null) {
                future.cancel(false);
            }
            sendCommand();
            return;
        }
        // if timer is not already set, create a new one to ensure that the command command is send even if no follow up
        // commands are received.
        if (future == null || future.isDone()) {
            future = scheduler.schedule(() -> {
                logger.trace("timer of {} ms is over, sending the command", delay);
                sendCommand();
            }, delay, TimeUnit.MILLISECONDS);
        }
    }
}
