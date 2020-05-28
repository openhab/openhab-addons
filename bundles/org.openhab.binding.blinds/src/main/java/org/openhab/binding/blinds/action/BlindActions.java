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
package org.openhab.binding.blinds.action;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.script.ScriptServiceUtil;
import org.eclipse.smarthome.model.script.engine.action.ParamDoc;
import org.joda.time.base.AbstractInstant;
import org.openhab.binding.blinds.action.internal.BlindItem;
import org.openhab.binding.blinds.action.internal.BrightnessHistory;
import org.openhab.binding.blinds.action.internal.MoveBlindJob;
import org.openhab.binding.blinds.action.internal.MoveBlindsThread;
import org.openhab.binding.blinds.action.internal.util.History;
import org.openhab.binding.blinds.internal.BlindsConfiguration;
import org.openhab.binding.blinds.internal.BlindsHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides static methods that can be used in automation rules
 * for controllings blinds
 *
 * @author Markus Pfleger - Initial contribution
 * @since 1.6.0
 *
 */
@ThingActionsScope(name = "blinds")
@NonNullByDefault
public class BlindActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(BlindActions.class);

    private final Lock lock = new ReentrantLock();
    private AtomicInteger nextBlindId = new AtomicInteger(0);
    private final Map<String, Integer> blindNameToId = new HashMap<>();
    private final Map<Integer, BlindItem> idToBlind = new HashMap<>();

    private final BrightnessHistory history = new BrightnessHistory();

    private AtomicBoolean suspended = new AtomicBoolean(false);

    public @Nullable MoveBlindsThread blindsThread;
    private @Nullable BlindsHandler thingHandler;

    private @Nullable ScheduledExecutorService executor;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof BlindsHandler) {
            this.thingHandler = (BlindsHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return thingHandler;
    }

    @Override
    public void activate() {
        ThingActions.super.activate();

        blindsThread = new MoveBlindsThread();
        blindsThread.start();
        logger.info("Blinds 2.0 action service has been activated.");

        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> history.nextSlot(System.currentTimeMillis()), 1, 1, TimeUnit.MINUTES);
    }

    private @Nullable ItemRegistry getItemRegistry() {
        return ScriptServiceUtil.getItemRegistry();
    }

    @Override
    public void deactivate() {
        ThingActions.super.deactivate();

        // deallocate Resources here that are no longer needed and
        // should be reset when activating this binding again
        if (blindsThread != null) {
            blindsThread.shutdown();
        }

        history.clear();

        if (executor != null) {
            executor.shutdown();
        }
        logger.info("Blinds 2.0 action service has been deactivated.");

    }

    ///////////////// BRIGHTNESS ///////////////////

    @RuleAction(label = "Persist the value in memory")
    public static void persistValue(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "An item for which to persist the value") final org.eclipse.smarthome.core.items.Item item) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).persistValue(item);
        }
    }

    @RuleAction(label = "Persist the value in memory")
    public void persistValue(
            @ActionInput(name = "item", description = "An item for which to persist the value") final org.eclipse.smarthome.core.items.Item item) {

        DecimalType state = item.getStateAs(DecimalType.class);
        if (state != null) {
            logger.debug("Persisting item {} using value {}", item.getName(), state.intValue());

            history.assureItemRegistered(item.getName(), System.currentTimeMillis());
            history.add(item.getName(), state.intValue());
        }
    }

    @RuleAction(label = "Persist the value in memory")
    public static Optional<DecimalType> getPersistedMaximumSince(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "An item for which to persist the value") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "time", description = "The point in time from where the maximum should be retrieved") final AbstractInstant time) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getPersistedMaximumSince(item, time);
        }

        return Optional.empty();
    }

    @RuleAction(label = "Get the maximum value of the item since the given start time")
    public Optional<DecimalType> getPersistedMaximumSince(
            @ActionInput(name = "item", description = "An item for which to persist the value") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "time", description = "The point in time from where the maximum should be retrieved") final AbstractInstant time) {

        Optional<History> itemHistory = history.getHistory(item.getName());
        if (!itemHistory.isPresent()) {
            logger.warn("Unable to get historic data of item {} as no value was persisted for this item",
                    item.getName());
            return Optional.empty();
        }

        Optional<Integer> result = itemHistory.get().getMaximumSince(time.getMillis());
        if (!result.isPresent()) {
            logger.warn("Unable to get maximum of item {} since {}. Not enough history available...", item.getName(),
                    time);
            return Optional.empty();
        }

        logger.debug("Fetching maximum of item {} since {}: {}", item.getName(), time, result.get());

        return Optional.of(new DecimalType(result.get()));
    }

    @RuleAction(label = "Get the minimum value of the item since the given start time")
    public static Optional<DecimalType> getPersistedMinimumSince(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "An item for which to persist the value") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "time", description = "The point in time from where the maximum should be retrieved") final AbstractInstant time) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getPersistedMinimumSince(item, time);
        }

        return Optional.empty();
    }

    @RuleAction(label = "Persist the value in memory")
    public Optional<DecimalType> getPersistedMinimumSince(
            @ActionInput(name = "item", description = "An item for which to persist the value") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "time", description = "The point in time from where the maximum should be retrieved") final AbstractInstant time) {

        Optional<History> itemHistory = history.getHistory(item.getName());
        if (!itemHistory.isPresent()) {
            logger.warn("Unable to get historic data of item {} as no value was persisted for this item",
                    item.getName());
            return Optional.empty();
        }

        Optional<Integer> result = itemHistory.get().getMinimumSince(time.getMillis());
        if (!result.isPresent()) {
            logger.warn("Unable to get minimum of item {} since {}. Not enough history available...", item.getName(),
                    time);
            return Optional.empty();
        }

        logger.debug("Fetching minimum of item {} since {}: {}", item.getName(), time, result.get());

        return Optional.of(new DecimalType(result.get()));
    }

    ///////////////// BLINDS ///////////////////

    public static void clearBlindItems(@Nullable ThingActions actions) {
        if (actions instanceof BlindActions) {
            ((BlindActions) actions).clearBlindItems();
        }
    }

    @RuleAction(label = "Removes all registered blind items.")
    public void clearBlindItems() {
        lock.lock();
        try {
            nextBlindId.set(0);
            blindNameToId.clear();
            idToBlind.clear();
        } finally {
            lock.unlock();
        }
    }

    public static int createBlindItem(ThingActions actions,
            org.eclipse.smarthome.core.library.items.RollershutterItem rollershutterItem,
            org.eclipse.smarthome.core.library.items.DimmerItem slatItem) {
        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).createBlindItem(rollershutterItem, slatItem);
        } else {
            throw new IllegalArgumentException(
                    "Unable to create blind item for ThingActions: " + actions.getClass().getSimpleName());
        }
    }

    @RuleAction(label = "Creates a BlindItem", description = "Creates a BlindItem which is bunch of items related to a single blind.")
    public int createBlindItem(
            @ActionInput(name = "rollershutterItem", description = "The rolershutter item that can be used to set a new value and to read the current state value ") org.eclipse.smarthome.core.library.items.RollershutterItem rollershutterItem,
            @ActionInput(name = "slatItem", description = "A dimmer item that is used to set the slat position to a new value and to read the current slat state") org.eclipse.smarthome.core.library.items.DimmerItem slatItem) {

        return createBlindItem(rollershutterItem, slatItem, null, null);
    }

    @RuleAction(label = "Creates a BlindItem", description = "Creates a BlindItem which is bunch of items related to a single blind.")
    public static int createBlindItem(ThingActions actions,
            @ActionInput(name = "rollershutterItem", description = "The rollershutter item that can be used to set a new value and to read the current state value ") org.eclipse.smarthome.core.library.items.RollershutterItem rollershutterItem,
            @ActionInput(name = "slatItem", description = "A dimmer item that is used to set the slat position to a new value and to read the current slat state") org.eclipse.smarthome.core.library.items.DimmerItem slatItem,
            @ActionInput(name = "windowContact", description = "A contact of a related window. The state of the related value can be checked when the blind is moved to a new position. Can be null") org.eclipse.smarthome.core.library.items.@Nullable ContactItem windowContact,
            @ActionInput(name = "temperatureItem", description = "A number item that gives access to a related temperature. A related temperature is typically the temperature of the room of the blind") org.eclipse.smarthome.core.library.items.@Nullable NumberItem temperatureItem) {
        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).createBlindItem(rollershutterItem, slatItem, windowContact,
                    temperatureItem);
        } else {
            throw new IllegalArgumentException(
                    "Unable to create blind item for ThingActions: " + actions.getClass().getSimpleName());
        }
    }

    @RuleAction(label = "Creates a BlindItem", description = "Creates a BlindItem which is bunch of items related to a single blind.")
    public int createBlindItem(
            @ActionInput(name = "rollershutterItem", description = "The rollershutter item that can be used to set a new value and to read the current state value ") org.eclipse.smarthome.core.library.items.RollershutterItem rollershutterItem,
            @ActionInput(name = "slatItem", description = "A dimmer item that is used to set the slat position to a new value and to read the current slat state") org.eclipse.smarthome.core.library.items.DimmerItem slatItem,
            @ActionInput(name = "windowContact", description = "A contact of a related window. The state of the related value can be checked when the blind is moved to a new position. Can be null") org.eclipse.smarthome.core.library.items.@Nullable ContactItem windowContact,
            @ActionInput(name = "temperatureItem", description = "A number item that gives access to a related temperature. A related temperature is typically the temperature of the room of the blind") org.eclipse.smarthome.core.library.items.@Nullable NumberItem temperatureItem) {

        return createBlindItem(rollershutterItem, rollershutterItem, slatItem, windowContact, temperatureItem);
    }

    @RuleAction(label = "Creates a BlindItem", description = "Creates a BlindItem which is bunch of items related to a single blind.")
    public static int createBlindItem(ThingActions actions,
            @ActionInput(name = "rollershutterItem", description = "The rollershutter item that can be used to set a new value and to read the current state value") org.eclipse.smarthome.core.library.items.RollershutterItem rollershutterItem,
            @ActionInput(name = "rollershutterStateItem", description = "A specific rollershutter item to read the current rollershutter state from. Can be null ") org.eclipse.smarthome.core.library.items.RollershutterItem rollershutterStateItem,
            @ActionInput(name = "slatItem", description = "A dimmer item that is used to set the slat position to a new value and to read the current slat state") org.eclipse.smarthome.core.library.items.DimmerItem slatItem,
            @ActionInput(name = "windowContact", description = "A contact of a related window. The state of the related value can be checked when the blind is moved to a new position. Can be null") org.eclipse.smarthome.core.library.items.ContactItem windowContact,
            @ActionInput(name = "temperatureItem", description = "A number item that gives access to a related temperature. A related temperature is typically the temperature of the room of the blind") org.eclipse.smarthome.core.library.items.NumberItem temperatureItem) {
        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).createBlindItem(rollershutterItem, rollershutterStateItem, slatItem,
                    windowContact, temperatureItem);
        } else {
            throw new IllegalArgumentException(
                    "Unable to create blind item for ThingActions: " + actions.getClass().getSimpleName());
        }
    }

    @RuleAction(label = "Creates a BlindItem", description = "Creates a BlindItem which is bunch of items related to a single blind.")
    public int createBlindItem(
            @ActionInput(name = "rollershutterItem", description = "The rollershutter item that can be used to set a new value and to read the current state value") org.eclipse.smarthome.core.library.items.RollershutterItem rollershutterItem,
            @ActionInput(name = "rollershutterStateItem", description = "A specific rollershutter item to read the current rollershutter state from. Can be null ") org.eclipse.smarthome.core.library.items.RollershutterItem rollershutterStateItem,
            @ActionInput(name = "slatItem", description = "A dimmer item that is used to set the slat position to a new value and to read the current slat state") org.eclipse.smarthome.core.library.items.DimmerItem slatItem,
            @ActionInput(name = "windowContact", description = "A contact of a related window. The state of the related value can be checked when the blind is moved to a new position. Can be null") org.eclipse.smarthome.core.library.items.@Nullable ContactItem windowContact,
            @ActionInput(name = "temperatureItem", description = "A number item that gives access to a related temperature. A related temperature is typically the temperature of the room of the blind") org.eclipse.smarthome.core.library.items.@Nullable NumberItem temperatureItem) {

        if (rollershutterStateItem == null) {
            rollershutterStateItem = rollershutterItem;
        }

        ItemRegistry itemRegistry = getItemRegistry();
        if (itemRegistry == null) {
            logger.warn("Unable to register blind {} as no ItemRegistry is available", rollershutterItem.getName());
            return -1;
        }

        lock.lock();
        try {
            Integer existingId = blindNameToId.remove(rollershutterItem.getName());
            if (existingId != null) {
                // existing items are removed and replaced by the new ones
                idToBlind.remove(existingId);
            } else {
                // new item
                existingId = nextBlindId.incrementAndGet();
            }
            BlindItem blind = new BlindItem(rollershutterItem.getName(), rollershutterStateItem.getName(),
                    slatItem.getName(), windowContact == null ? null : windowContact.getName(),
                    temperatureItem == null ? null : temperatureItem.getName(), itemRegistry);
            blindNameToId.put(rollershutterItem.getName(), existingId);
            idToBlind.put(existingId, blind);

            logger.info("Registered blind {} with id {}.", rollershutterItem.getName(), existingId);
            return existingId;
        } finally {
            lock.unlock();
        }
    }

    @RuleAction(label = "Set blind sun range", description = "Sets the sun range of this blind. The sun range means the azimut where the sun hits the window of this blind.")
    public static boolean setBlindSunRange(@Nullable ThingActions actions,
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId,
            @ActionInput(name = "azimutLowerBound", description = "The lower bound of the azimut") int azimutLowerBound,
            @ActionInput(name = "azimutUpperBound", description = "The upper bound of the azimut") int azimutUpperBound) {
        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).setBlindSunRange(blindId, azimutLowerBound, azimutUpperBound);
        }
        return false;
    }

    @RuleAction(label = "Set blind sun range", description = "Sets the sun range of this blind. The sun range means the azimut where the sun hits the window of this blind.")
    public boolean setBlindSunRange(@ActionInput(name = "blindId", description = "The id of the blind") int blindId,
            @ActionInput(name = "azimutLowerBound", description = "The lower bound of the azimut") int azimutLowerBound,
            @ActionInput(name = "azimutUpperBound", description = "The upper bound of the azimut") int azimutUpperBound) {
        return setBlindSunRange(blindId, azimutLowerBound, azimutUpperBound, -1, -1);
    }

    @RuleAction(label = "Set blind sun range", description = "Sets the sun range of this blind. The sun range means the azimut where the sun hits the window of this blind.")
    public static boolean setBlindSunRange(@Nullable ThingActions actions,
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId,
            @ActionInput(name = "azimutLowerBound", description = "The lower bound of the azimut") int azimutLowerBound,
            @ActionInput(name = "azimutUpperBound", description = "The upper bound of the azimut") int azimutUpperBound,
            @ActionInput(name = "elevationLowerBound", description = "The lower bound of the elevation") int elevationLowerBound,
            @ActionInput(name = "elevationUpperBound", description = "The upper bound of the elevation") int elevationUpperBound) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).setBlindSunRange(blindId, azimutLowerBound, azimutUpperBound,
                    elevationLowerBound, elevationUpperBound);
        }
        return false;
    }

    @RuleAction(label = "Set blind sun range", description = "Sets the sun range of this blind. The sun range means the azimut where the sun hits the window of this blind.")
    public boolean setBlindSunRange(@ActionInput(name = "blindId", description = "The id of the blind") int blindId,
            @ActionInput(name = "azimutLowerBound", description = "The lower bound of the azimut") int azimutLowerBound,
            @ActionInput(name = "azimutUpperBound", description = "The upper bound of the azimut") int azimutUpperBound,
            @ActionInput(name = "elevationLowerBound", description = "The lower bound of the elevation") int elevationLowerBound,
            @ActionInput(name = "elevationUpperBound", description = "The upper bound of the elevation") int elevationUpperBound) {

        BlindItem blind = getBlindItem(blindId);
        if (blind == null) {
            logger.warn("Unable to set sunrange for blind with id {}. The blind is not registered!", blindId);
            return false;
        }

        blind.getConfig().setSunRange(azimutLowerBound, azimutUpperBound, elevationLowerBound, elevationUpperBound);
        return true;
    }

    @RuleAction(label = "Set temperature range", description = "Sets the temperature range of this blind. The temperature range means a range where the temperature related to the blind is considered to be fine.")
    public static boolean setBlindTemperatureRange(@Nullable ThingActions actions,
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId,
            @ActionInput(name = "temperatureLowerBound", description = "The lower bound of the related temperature") double temperatureLowerBound,
            @ActionInput(name = "temperatureUpperBound", description = "The upper bound of the related temperature") double temperatureUpperBound) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).setBlindTemperatureRange(blindId, temperatureLowerBound,
                    temperatureUpperBound);
        }
        return false;
    }

    @RuleAction(label = "Set temperature range", description = "Sets the temperature range of this blind. The temperature range means a range where the temperature related to the blind is considered to be fine.")
    public boolean setBlindTemperatureRange(
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId,
            @ActionInput(name = "temperatureLowerBound", description = "The lower bound of the related temperature") double temperatureLowerBound,
            @ActionInput(name = "temperatureUpperBound", description = "The upper bound of the related temperature") double temperatureUpperBound) {

        BlindItem blind = getBlindItem(blindId);
        if (blind == null) {
            logger.warn("Unable to define temperature bounds for blind with id {}. The blind is not registered!",
                    blindId);
            return false;
        }

        blind.getConfig().setTemperatureRange(temperatureLowerBound, temperatureUpperBound);
        return true;
    }

    @RuleAction(label = "Set blind limit", description = "Limits the blind to the specified position")
    public static void setBlindPositionLimit(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "A rollershutter item where we would like to move the related blind up. If the item is a group item, all members will be checked. If the item is null, all registered blinds willbe limited") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "limit", description = "The limit of the blind position. The blind will not move further than that limit") int limit) {
        if (actions instanceof BlindActions) {
            ((BlindActions) actions).setBlindPositionLimit(item, limit);
        }
    }

    @RuleAction(label = "Set blind limit", description = "Limits the blind to the specified position")
    public void setBlindPositionLimit(
            @ActionInput(name = "item", description = "A rollershutter item where we would like to move the related blind up. If the item is a group item, all members will be checked. If the item is null, all registered blinds willbe limited") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "limit", description = "The limit of the blind position. The blind will not move further than that limit") int limit) {
        setBlindPositionLimit(getBlindSet(item), limit);
    }

    @RuleAction(label = "Set blind limit", description = "Limits the blind to the specified position")
    public static void setBlindPositionLimit(@Nullable ThingActions actions,
            @ActionInput(name = "blindIds", description = "A set containing all blind ids that should be limited") final Set<Integer> input,
            @ActionInput(name = "limit", description = "The limit of the blind position. The blind will not move further than that limit") int limit) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).setBlindPositionLimit(input, limit);
        }
    }

    @RuleAction(label = "Set blind limit", description = "Limits the blind to the specified position")
    public void setBlindPositionLimit(
            @ActionInput(name = "blindIds", description = "A set containing all blind ids that should be limited") final Set<Integer> input,
            @ActionInput(name = "limit", description = "The limit of the blind position. The blind will not move further than that limit") int limit) {

        for (Integer curIndex : input) {
            setBlindPositionLimit(curIndex, limit);
        }
    }

    @RuleAction(label = "Set blind limit", description = "Limits the blind to the specified position")
    public static void setBlindPositionLimit(@Nullable ThingActions actions,
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId,
            @ActionInput(name = "limit", description = "The limit of the blind position. The blind will not move further than that limit") int limit) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).setBlindPositionLimit(blindId, limit);
        }
    }

    @RuleAction(label = "Set blind limit", description = "Limits the blind to the specified position")
    public void setBlindPositionLimit(@ActionInput(name = "blindId", description = "The id of the blind") int blindId,
            @ActionInput(name = "limit", description = "The limit of the blind position. The blind will not move further than that limit") int limit) {

        BlindItem blind = getBlindItem(blindId);
        if (blind == null) {
            logger.warn("Unable to define limit for blind with id {}. The blind is not registered!", blindId);
            return;
        }

        blind.getConfig().setBlindPositionLimit(limit);
    }

    private @Nullable BlindItem getBlindItem(int blindId) {
        BlindItem blind = null;
        lock.lock();
        try {
            blind = idToBlind.get(blindId);
        } finally {
            lock.unlock();
        }
        return blind;
    }

    @RuleAction(label = "Start automatic program", description = "Starts the automatic program for this blind.")
    public static boolean startBlindAutomaticProgram(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "A rollershutter item where we would like to start the automatic program. If the item is a group item, all members will be checked. If the item is null, all registered blinds will move up") final org.eclipse.smarthome.core.items.Item item) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).startBlindAutomaticProgram(item);
        }
        return false;
    }

    @RuleAction(label = "Start automatic program", description = "Starts the automatic program for this blind.")
    public boolean startBlindAutomaticProgram(
            @ActionInput(name = "item", description = "A rollershutter item where we would like to start the automatic program. If the item is a group item, all members will be checked. If the item is null, all registered blinds will move up") final org.eclipse.smarthome.core.items.Item item) {

        boolean allTrue = true;
        Set<Integer> blindIds = getBlindSet(item);
        for (Integer blindId : blindIds) {
            allTrue &= startBlindAutomaticProgram(blindId);
        }
        return allTrue;
    }

    @RuleAction(label = "Start automatic program", description = "Starts the automatic program for this blind. Only blinds that take part of the automatic program are considered later")
    public static boolean startBlindAutomaticProgram(@Nullable ThingActions actions,
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).startBlindAutomaticProgram(blindId);
        }
        return false;
    }

    @RuleAction(label = "Start automatic program", description = "Starts the automatic program for this blind. Only blinds that take part of the automatic program are considered later")
    public boolean startBlindAutomaticProgram(
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId) {

        BlindItem blind = getBlindItem(blindId);
        if (blind == null) {
            logger.warn("Unable to start automatic blinds for blind with id {}. The blind is not yet registered!",
                    blindId);
            return false;
        }

        RollershutterItem rollershutterItem = blind.getRollershutterItem();
        DimmerItem slatItem = blind.getSlatItem();

        blind.storeState(rollershutterItem.getState(), slatItem.getState());

        blind.getConfig().setAutomaticProgramEnabled(true);
        return true;
    }

    @RuleAction(label = "Limit blind direction up", description = "Limits a blind to either only move up, or only move down or allow all directions")
    public static void limitBlindDirectionToUp(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "A blind item where the allowed direction should be changed") final org.eclipse.smarthome.core.items.Item item) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).limitBlindDirection(item, BlindDirection.UP);
        }
    }

    @RuleAction(label = "Limit blind direction down", description = "Limits a blind to either only move up, or only move down or allow all directions")
    public static void limitBlindDirectionToDown(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "A blind item where the allowed direction should be changed") final org.eclipse.smarthome.core.items.Item item) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).limitBlindDirection(item, BlindDirection.DOWN);
        }
    }

    @RuleAction(label = "Unimit blind direction", description = "Limits a blind to either only move up, or only move down or allow all directions")
    public static void unlimitBlindDirection(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "A blind item where the allowed direction should be changed") final org.eclipse.smarthome.core.items.Item item) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).limitBlindDirection(item, BlindDirection.ANY);
        }
    }

    @RuleAction(label = "Limit blind direction", description = "Limits a blind to either only move up, or only move down or allow all directions")
    public void limitBlindDirection(
            @ActionInput(name = "item", description = "A blind item where the allowed direction should be changed") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "direction", description = "The allowed direction for this blind") BlindDirection direction) {

        Set<Integer> blindIds = getBlindSet(item);
        for (Integer blindId : blindIds) {
            limitBlindDirection(blindId, direction);
        }
    }

    @RuleAction(label = "Limit blind direction up", description = "Limits a blind to either only move up, or only move down or allow all directions")
    public static void limitBlindDirectionToUp(@Nullable ThingActions actions,
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).limitBlindDirection(blindId, BlindDirection.UP);
        }
    }

    @RuleAction(label = "Limit blind direction down", description = "Limits a blind to either only move up, or only move down or allow all directions")
    public static void limitBlindDirectionToDown(@Nullable ThingActions actions,
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).limitBlindDirection(blindId, BlindDirection.DOWN);
        }
    }

    @RuleAction(label = "Unlimit blind direction", description = "Limits a blind to either only move up, or only move down or allow all directions")
    public static void unlimitBlindDirection(@Nullable ThingActions actions,
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).limitBlindDirection(blindId, BlindDirection.ANY);
        }
    }

    @RuleAction(label = "Limit blind direction", description = "Limits a blind to either only move up, or only move down or allow all directions")
    public boolean limitBlindDirection(@ActionInput(name = "blindId", description = "The id of the blind") int blindId,
            @ActionInput(name = "direction", description = "The allowed direction for this blind") BlindDirection direction) {

        BlindItem blind = getBlindItem(blindId);
        if (blind == null) {
            logger.warn("Unable to start limit direction of blind with id {}. The blind is not yet registered!",
                    blindId);
            return false;
        }

        blind.getConfig().setAllowedBlindDirection(direction);
        return true;
    }

    @RuleAction(label = "Suspend blind manager", description = "Suspends the blind manager. Will ignore all move commands while suspended.")
    public static void suspend(@Nullable ThingActions actions) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).suspend();
        }
    }

    @RuleAction(label = "Suspend blind manager", description = "Suspends the blind manager. Will ignore all move commands while suspended.")
    public void suspend() {
        suspended.set(true);
    }

    @RuleAction(label = "Resume blind manager", description = "Resumes the blind manager.")
    public static void resume(@Nullable ThingActions actions) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).resume();
        }
    }

    @RuleAction(label = "Resume blind manager", description = "Resumes the blind manager.")
    public void resume() {
        suspended.set(false);
    }

    @RuleAction(label = "Stop automatic program", description = "Stops the automatic program for this blind.")
    public static boolean stopBlindAutomaticProgram(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "A rollershutter item where we would like to move the related blind up. If the item is a group item, all members will be checked. If the item is null, all registered blinds will move up") final org.eclipse.smarthome.core.items.Item item) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).stopBlindAutomaticProgram(item);
        }
        return false;
    }

    @RuleAction(label = "Stop automatic program", description = "Stops the automatic program for this blind.")
    public boolean stopBlindAutomaticProgram(
            @ActionInput(name = "item", description = "A rollershutter item where we would like to move the related blind up. If the item is a group item, all members will be checked. If the item is null, all registered blinds will move up") final org.eclipse.smarthome.core.items.Item item) {

        boolean allTrue = true;
        Set<Integer> blindIds = getBlindSet(item);
        for (Integer blindId : blindIds) {
            allTrue &= stopBlindAutomaticProgram(blindId);
        }
        return allTrue;
    }

    @RuleAction(label = "Stop automatic program", description = "Stops the automatic program for this blind.")
    public static boolean stopBlindAutomaticProgram(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "A rollershutter item where we would like to move the related blind up. If the item is a group item, all members will be checked. If the item is null, all registered blinds will move up") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "restorePosition", description = "True if the position of the blind before the automatic program should be restored") boolean restorePosition) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).stopBlindAutomaticProgram(item, restorePosition);
        }
        return false;
    }

    @RuleAction(label = "Stop automatic program", description = "Stops the automatic program for this blind.")
    public boolean stopBlindAutomaticProgram(
            @ActionInput(name = "item", description = "A rollershutter item where we would like to move the related blind up. If the item is a group item, all members will be checked. If the item is null, all registered blinds will move up") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "restorePosition", description = "True if the position of the blind before the automatic program should be restored") boolean restorePosition) {

        boolean allTrue = true;
        Set<Integer> blindIds = getBlindSet(item);
        for (Integer blindId : blindIds) {
            allTrue &= stopBlindAutomaticProgram(blindId, restorePosition);
        }
        return allTrue;
    }

    @RuleAction(label = "Stop automatic program", description = "Stops the automatic program for this blind.")
    public static boolean stopBlindAutomaticProgram(@Nullable ThingActions actions,
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).stopBlindAutomaticProgram(blindId);
        }
        return false;
    }

    @RuleAction(label = "Stop automatic program", description = "Stops the automatic program for this blind.")
    public boolean stopBlindAutomaticProgram(
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId) {
        return stopBlindAutomaticProgram(blindId, true);
    }

    @RuleAction(label = "Stop automatic program", description = "Stops the automatic program for this blind.")
    public static boolean stopBlindAutomaticProgram(@Nullable ThingActions actions,
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId,
            @ActionInput(name = "restorePosition", description = "True if the position of the blind before the automatic program should be restored") boolean restorePosition) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).stopBlindAutomaticProgram(blindId, restorePosition);
        }
        return false;
    }

    @RuleAction(label = "Stop automatic program", description = "Stops the automatic program for this blind.")
    public boolean stopBlindAutomaticProgram(
            @ActionInput(name = "blindId", description = "The id of the blind") int blindId,
            @ActionInput(name = "restorePosition", description = "True if the position of the blind before the automatic program should be restored") boolean restorePosition) {
        BlindItem blind = getBlindItem(blindId);
        if (blind == null) {
            logger.warn("Unable to stop automatic blinds for blind with id {}. The blind is not yet registered!",
                    blindId);
            return false;
        }

        blind.getConfig().setAutomaticProgramEnabled(false);

        if (restorePosition) {
            State storedRollershutterState = blind.getStoredRollershutterState();
            State storedSlatState = blind.getStoredSlatState();

            if (storedRollershutterState instanceof DecimalType && storedSlatState instanceof DecimalType) {
                logger.warn("Restoring state of blind {}. Rollershutter state: {}, Slat state: {}",
                        blind.getAutoRollershutterItemName(), storedRollershutterState, storedSlatState);
                moveBlindTo(blind, ((DecimalType) storedRollershutterState).intValue(),
                        ((DecimalType) storedSlatState).intValue(), OpenClosedType.CLOSED, BlindDirection.ANY);
            } else {
                logger.warn(
                        "Not restoring state of blind {} as the stored state is not a decimal type (rollershutter state: {}, slat state: {})",
                        blind.getAutoRollershutterItemName(), storedRollershutterState, storedSlatState);
            }
        }
        return true;
    }

    /**
     * A set containing all names of the items that are members of the specified group item. null if not group is
     * specified
     *
     * @param groupItem
     * @return
     */
    private Set<String> getGroupMemberNames(org.eclipse.smarthome.core.items.GroupItem groupItem) {
        Set<String> allMembers = null;
        if (groupItem != null) {
            allMembers = new HashSet<>();
            Set<org.eclipse.smarthome.core.items.Item> allMemberItems = groupItem.getAllMembers();
            allMembers = new HashSet<>();
            for (org.eclipse.smarthome.core.items.Item item : allMemberItems) {
                allMembers.add(item.getName());
            }
        }
        return allMembers;
    }

    private Set<Integer> getBlindSet(org.eclipse.smarthome.core.items.@Nullable Item item) {
        Set<Integer> set = new HashSet<Integer>();

        if (item == null) {
            set.addAll(idToBlind.keySet());
        } else {
            if (item instanceof org.eclipse.smarthome.core.items.GroupItem) {
                // if the item is a group item, fetch all registered blinds that are part of the group
                Set<String> allGroupMembers = getGroupMemberNames((org.eclipse.smarthome.core.items.GroupItem) item);
                for (Entry<Integer, BlindItem> entry : idToBlind.entrySet()) {
                    Integer blindId = entry.getKey();
                    BlindItem blind = entry.getValue();
                    if (allGroupMembers == null || allGroupMembers.contains(blind.getRollershutterItemName())) {
                        set.add(blindId);
                    }
                }
            } else {
                // if it is no group item, check if the item is registered as blind
                Integer blindId = blindNameToId.get(item.getName());
                if (blindId != null) {
                    set.add(blindId);
                }
            }
        }
        return set;
    }

    @RuleAction(label = "Get all blinds", description = "Returns a Set where all registered blind ids are set")
    public static Set<Integer> getBlinds(@Nullable ThingActions actions) {
        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlinds();
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get all blinds", description = "Returns a Set where all registered blind ids are set")
    public Set<Integer> getBlinds() {
        return getBlindSet(null);
    }

    @RuleAction(label = "Returns some blinds", description = "Returns a set where the blind id(s) of the specified item is set. If the item does not specify a registered blind the resulting"
            + "         set will be empty. If the item is a single registered blind the id is set in the result. If the item defines a group item, all registered"
            + "         blinds will be set")
    public static Set<Integer> getBlinds(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "The item for which the appropriate set should be fetched") org.eclipse.smarthome.core.items.Item item) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlinds(item);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Returns some blinds", description = "Returns a set where the blind id(s) of the specified item is set. If the item does not specify a registered blind the resulting"
            + "set will be empty. If the item is a single registered blind the id is set in the result. If the item defines a group item, all registered"
            + "blinds will be set")
    public Set<Integer> getBlinds(
            @ActionInput(name = "item", description = "The item for which the appropriate set should be fetched") org.eclipse.smarthome.core.items.@Nullable Item item) {
        return getBlindSet(item);
    }

    @RuleAction(label = "Get all automatic blinds", description = "Fetches a set where only the blinds that are part of the automatic blind program are returned")
    public static Set<Integer> getAutomaticBlinds(@Nullable ThingActions actions) {
        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getAutomaticBlinds();
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get all automatic blinds", description = "Fetches a set where only the blinds that are part of the automatic blind program are returned")
    public Set<Integer> getAutomaticBlinds() {
        return getAutomaticBlinds((org.eclipse.smarthome.core.items.Item) null);
    }

    @RuleAction(label = "Get some automatic blinds", description = "Fetches a  set where only the blinds that are part of the automatic blind program are returned")
    public static Set<Integer> getAutomaticBlinds(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "The item for which the appropriate set should be fetched") org.eclipse.smarthome.core.items.Item item) {
        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getAutomaticBlinds(item);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get some automatic blinds", description = "Fetches a  set where only the blinds that are part of the automatic blind program are returned")
    public Set<Integer> getAutomaticBlinds(
            @ActionInput(name = "item", description = "The item for which the appropriate set should be fetched") org.eclipse.smarthome.core.items.@Nullable Item item) {
        Set<Integer> input = getBlindSet(item);
        Set<Integer> result = new HashSet<Integer>();
        if (input != null) {
            for (Integer curIndex : input) {
                BlindItem blind = getBlindItem(curIndex);
                if (blind != null) {
                    if (blind.getConfig().isAutomaticProgramEnabled()) {
                        result.add(curIndex);
                    }
                }
            }
        }
        return result;
    }

    @RuleAction(label = "Get blinds not moved for some time", description = "Fetches a set of blinds that have not been moved for a specified number of minutes")
    public static Set<Integer> getBlindsNotMovedFor(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "The input item which should be checked. For groups all members are checked") org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "numberOfMinutes", description = "The number of minutes the blind should not have moved") int numberOfMinutes) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsNotMovedFor(item, numberOfMinutes);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds not moved for some time", description = "Fetches a set of blinds that have not been moved for a specified number of minutes")
    public Set<Integer> getBlindsNotMovedFor(
            @ActionInput(name = "item", description = "The input item which should be checked. For groups all members are checked") org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "numberOfMinutes", description = "The number of minutes the blind should not have moved") int numberOfMinutes) {

        return getBlindsNotMovedFor(getBlindSet(item), numberOfMinutes);
    }

    @RuleAction(label = "Get blinds not moved for some time", description = "Fetches a set of blinds that have not been moved for a specified number of minutes")
    public static Set<Integer> getBlindsNotMovedFor(@Nullable ThingActions actions,
            @ActionInput(name = "input", description = "The input set on which the evaluation should be performed") Set<Integer> input,
            @ActionInput(name = "numberOfMinutes", description = "The number of minutes the blind should not have moved") int numberOfMinutes) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsNotMovedFor(input, numberOfMinutes);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds not moved for some time", description = "Fetches a set of blinds that have not been moved for a specified number of minutes")
    public Set<Integer> getBlindsNotMovedFor(
            @ActionInput(name = "input", description = "The input set on which the evaluation should be performed") Set<Integer> input,
            @ActionInput(name = "numberOfMinutes", description = "The number of minutes the blind should not have moved") int numberOfMinutes) {

        Set<Integer> result = new HashSet<Integer>();
        if (input != null) {
            for (Integer curIndex : input) {
                BlindItem blind = getBlindItem(curIndex);
                if (blind != null) {
                    long msSinceLastBlindMove = blind.getMsSinceLastBlindMove();
                    if (msSinceLastBlindMove > numberOfMinutes * 60 * 1000) {
                        result.add(curIndex);
                    }
                    // RollershutterItem blindItem = getRollershutterItem(blind);
                    // if (!PersistenceExtensions.changedSince(blindItem, DateTime.now().minusMinutes(numberOfMinutes)))
                    // {
                    // result.add(curIndex);
                    // }

                }
            }
        }
        return result;
    }

    @RuleAction(label = "Get blinds near sun range", description = "Returns a set of blinds where the sun position is currently near the blind.")
    public static Set<Integer> getBlindsNearSunRange(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "The input item which should be checked. For groups all members are checked") org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "azimut", description = "The current azimut") int azimut,
            @ActionInput(name = "azimutTolerance", description = "The tolerance of the azimut") int azimutTolerance,
            @ActionInput(name = "elevation", description = "The current elevation") int elevation,
            @ActionInput(name = "elevationTolerance", description = "The tolerance of the elevation") int elevationTolerance) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsNearSunRange(item, azimut, azimutTolerance, elevation,
                    elevationTolerance);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds near sun range", description = "Returns a set of blinds where the sun position is currently near the blind.")
    public Set<Integer> getBlindsNearSunRange(
            @ActionInput(name = "item", description = "The input item which should be checked. For groups all members are checked") org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "azimut", description = "The current azimut") int azimut,
            @ActionInput(name = "azimutTolerance", description = "The tolerance of the azimut") int azimutTolerance,
            @ActionInput(name = "elevation", description = "The current elevation") int elevation,
            @ActionInput(name = "elevationTolerance", description = "The tolerance of the elevation") int elevationTolerance) {

        return getBlindsNearSunRange(getBlindSet(item), azimut, azimutTolerance, elevation, elevationTolerance);
    }

    @RuleAction(label = "Get blinds near sun range", description = "Returns a set of blinds where the sun position is currently near the blind.")
    public static Set<Integer> getBlindsNearSunRange(@Nullable ThingActions actions,
            @ActionInput(name = "input", description = "The input set on which the evaluation should be performed") Set<Integer> input,
            @ActionInput(name = "azimut", description = "The current azimut") int azimut,
            @ActionInput(name = "azimutTolerance", description = "The tolerance of the azimut") int azimutTolerance,
            @ActionInput(name = "elevation", description = "The current elevation") int elevation,
            @ActionInput(name = "elevationTolerance", description = "The tolerance of the elevation") int elevationTolerance) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsNearSunRange(input, azimut, azimutTolerance, elevation,
                    elevationTolerance);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds near sun range", description = "Returns a set of blinds where the sun position is currently near the blind.")
    public Set<Integer> getBlindsNearSunRange(
            @ActionInput(name = "input", description = "The input set on which the evaluation should be performed") Set<Integer> input,
            @ActionInput(name = "azimut", description = "The current azimut") int azimut,
            @ActionInput(name = "azimutTolerance", description = "The tolerance of the azimut") int azimutTolerance,
            @ActionInput(name = "elevation", description = "The current elevation") int elevation,
            @ActionInput(name = "elevationTolerance", description = "The tolerance of the elevation") int elevationTolerance) {

        return getBlindsInSunRangeTolerance(input, azimut, azimutTolerance, elevation, elevationTolerance);
    }

    @RuleAction(label = "Get blinds in sun range", description = "Returns a set of blinds where the sun position is currently in the blind sun range.")
    public static Set<Integer> getBlindsInSunRange(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "The input item which should be checked. For groups all members are checked") org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "azimut", description = "The current azimut") int azimut,
            @ActionInput(name = "elevation", description = "The current elevation") int elevation) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsInSunRange(item, azimut, elevation);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds in sun range", description = "Returns a set of blinds where the sun position is currently in the blind sun range.")
    public Set<Integer> getBlindsInSunRange(
            @ActionInput(name = "item", description = "The input item which should be checked. For groups all members are checked") org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "azimut", description = "The current azimut") int azimut,
            @ActionInput(name = "elevation", description = "The current elevation") int elevation) {
        return getBlindsInSunRangeTolerance(getBlindSet(item), azimut, 0, elevation, 0);
    }

    @RuleAction(label = "Get blinds in sun range", description = "Returns a set of blinds where the sun position is currently in the blind sun range.")
    public static Set<Integer> getBlindsInSunRange(@Nullable ThingActions actions,
            @ActionInput(name = "input", description = "The input set on which the evaluation should be performed") Set<Integer> input,
            @ActionInput(name = "azimut", description = "The current azimut") int azimut,
            @ActionInput(name = "elevation", description = "The current elevation") int elevation) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsInSunRange(input, azimut, elevation);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds in sun range", description = "Returns a set of blinds where the sun position is currently in the blind sun range.")
    public Set<Integer> getBlindsInSunRange(
            @ActionInput(name = "input", description = "The input set on which the evaluation should be performed") Set<Integer> input,
            @ActionInput(name = "azimut", description = "The current azimut") int azimut,
            @ActionInput(name = "elevation", description = "The current elevation") int elevation) {
        return getBlindsInSunRangeTolerance(input, azimut, 0, elevation, 0);
    }

    private Set<Integer> getBlindsInSunRangeTolerance(Set<Integer> input, int azimut, int toleranceAzimut,
            int elevation, int toleranceElevation) {
        Set<Integer> result = new HashSet<Integer>();

        if (input != null) {
            for (Integer curIndex : input) {
                BlindItem blind = getBlindItem(curIndex);
                if (blind != null && blind.getConfig().isSunRangeSet()
                        && (!blind.getConfig().isAzimutRangeSet() || blind.azimutMatches(azimut, toleranceAzimut))
                        && (!blind.getConfig().isElevationRangeSet()
                                || blind.elevationMatches(elevation, toleranceElevation))) {

                    result.add(curIndex);
                }
            }
        }
        return result;
    }

    @RuleAction(label = "Get blinds near upper temperature limit", description = "Returns a set of blinds where the temperature is near the defined upper bound")
    public static Set<Integer> getBlindsNearUpperTemperatureRange(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "The item on which the evaluation should be performed. For group items all members are checked") org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "maxDistanceFromUpperBound", description = "The maximum distance from the upper bound which should be regarded to be near") double maxDistanceFromUpperBound) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsNearUpperTemperatureRange(item, maxDistanceFromUpperBound);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds near upper temperature limit", description = "Returns a set of blinds where the temperature is near the defined upper bound")
    public Set<Integer> getBlindsNearUpperTemperatureRange(
            @ActionInput(name = "item", description = "The item on which the evaluation should be performed. For group items all members are checked") org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "maxDistanceFromUpperBound", description = "The maximum distance from the upper bound which should be regarded to be near") double maxDistanceFromUpperBound) {
        return getBlindsNearUpperTemperatureRange(getBlindSet(item), maxDistanceFromUpperBound);
    }

    @RuleAction(label = "Get blinds near upper temperature limit", description = "Returns a set of blinds where the temperature is near the defined upper bound")
    public static Set<Integer> getBlindsNearUpperTemperatureRange(@Nullable ThingActions actions,
            @ActionInput(name = "input", description = "The input bit set on which the evaluation should be performed") Set<Integer> input,
            @ActionInput(name = "maxDistanceFromUpperBound", description = "The maximum distance from the upper bound which should be regarded to be near") double maxDistanceFromUpperBound) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsNearUpperTemperatureRange(input, maxDistanceFromUpperBound);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds near upper temperature limit", description = "Returns a set of blinds where the temperature is near the defined upper bound")
    public Set<Integer> getBlindsNearUpperTemperatureRange(
            @ActionInput(name = "input", description = "The input bit set on which the evaluation should be performed") Set<Integer> input,
            @ActionInput(name = "maxDistanceFromUpperBound", description = "The maximum distance from the upper bound which should be regarded to be near") double maxDistanceFromUpperBound) {

        Set<Integer> result = new HashSet<Integer>();

        if (input != null) {
            for (Integer curIndex : input) {
                BlindItem blind = getBlindItem(curIndex);
                if (blind != null) {

                    if (blind.getConfig().isTemperatureRangeSet()) {
                        double currentTemperature = getCurrentTemperature(blind);

                        if (currentTemperature < blind.getConfig().getTemperatureUpperBound()
                                && currentTemperature > blind.getConfig().getTemperatureUpperBound()
                                        - maxDistanceFromUpperBound) {

                            result.add(curIndex);
                        }

                    }
                }
            }
        }
        return result;
    }

    @RuleAction(label = "Get blinds near lower temperature limit", description = "Returns a set of blinds where the temperature is near the defined lower bound")
    public static Set<Integer> getBlindsNearLowerTemperatureRange(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "The item on which the evaluation should be performed. For group items all members are checked") org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "maxDistanceFromLowerBound", description = "The maximum distance from the lower bound which should be regarded to be near") double maxDistanceFromLowerBound) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsNearLowerTemperatureRange(item, maxDistanceFromLowerBound);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds near lower temperature limit", description = "Returns a set of blinds where the temperature is near the defined lower bound")
    public Set<Integer> getBlindsNearLowerTemperatureRange(
            @ActionInput(name = "item", description = "The item on which the evaluation should be performed. For group items all members are checked") org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "maxDistanceFromLowerBound", description = "The maximum distance from the lower bound which should be regarded to be near") double maxDistanceFromLowerBound) {
        return getBlindsNearUpperTemperatureRange(getBlindSet(item), maxDistanceFromLowerBound);
    }

    @RuleAction(label = "Get blinds near lower temperature limit", description = "Returns a set of blinds where the temperature is near the defined lower bound")
    public static Set<Integer> getBlindsNearLowerTemperatureRange(@Nullable ThingActions actions,
            @ActionInput(name = "input", description = "The input bit set on which the evaluation should be performed") Set<Integer> input,
            @ActionInput(name = "maxDistanceFromLowerBound", description = "The maximum distance from the lower bound which should be regarded to be near") double maxDistanceFromLowerBound) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsNearLowerTemperatureRange(input, maxDistanceFromLowerBound);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds near lower temperature limit", description = "Returns a set of blinds where the temperature is near the defined lower bound")
    public Set<Integer> getBlindsNearLowerTemperatureRange(
            @ActionInput(name = "input", description = "The input bit set on which the evaluation should be performed") Set<Integer> input,
            @ActionInput(name = "maxDistanceFromLowerBound", description = "The maximum distance from the lower bound which should be regarded to be near") double maxDistanceFromLowerBound) {

        Set<Integer> result = new HashSet<Integer>();

        if (input != null) {
            for (Integer curIndex : input) {
                BlindItem blind = getBlindItem(curIndex);
                if (blind != null) {

                    if (blind.getConfig().isTemperatureRangeSet()) {
                        double currentTemperature = getCurrentTemperature(blind);

                        if (currentTemperature > blind.getConfig().getTemperatureLowerBound()
                                && currentTemperature < blind.getConfig().getTemperatureLowerBound()
                                        + maxDistanceFromLowerBound) {

                            result.add(curIndex);
                        }

                    }
                }
            }
        }
        return result;
    }

    @RuleAction(label = "Get blinds above temperature range", description = "Returns a set of blinds where the current temperature is above the temperature range.")
    public static Set<Integer> getBlindsAboveTemperatureRange(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "The item on which the evaluation should be performed. For group items all members are checked") org.eclipse.smarthome.core.items.Item item) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsAboveTemperatureRange(item);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds above temperature range", description = "Returns a set of blinds where the current temperature is above the temperature range.")
    public Set<Integer> getBlindsAboveTemperatureRange(
            @ActionInput(name = "item", description = "The item on which the evaluation should be performed. For group items all members are checked") org.eclipse.smarthome.core.items.Item item) {
        return getBlindsAboveTemperatureRange(getBlindSet(item));
    }

    @RuleAction(label = "Get blinds above temperature range", description = "Returns a set of blinds where the current temperature is above the temperature range.")
    public static Set<Integer> getBlindsAboveTemperatureRange(@Nullable ThingActions actions,
            @ActionInput(name = "input", description = "The input bit set on which the evaluation should be performed") Set<Integer> input) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsAboveTemperatureRange(input);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds above temperature range", description = "Returns a set of blinds where the current temperature is above the temperature range.")
    public Set<Integer> getBlindsAboveTemperatureRange(
            @ActionInput(name = "input", description = "The input bit set on which the evaluation should be performed") Set<Integer> input) {

        Set<Integer> result = new HashSet<Integer>();

        if (input != null) {
            for (Integer curIndex : input) {
                BlindItem blind = getBlindItem(curIndex);
                if (blind != null) {

                    if (blind.getConfig().isTemperatureRangeSet()
                            && getCurrentTemperature(blind) > blind.getConfig().getTemperatureUpperBound()) {
                        result.add(curIndex);
                    }
                }
            }
        }
        return result;
    }

    private double getCurrentTemperature(BlindItem blind) {
        ItemRegistry itemRegistry = getItemRegistry();
        if (itemRegistry == null) {
            logger.warn("Unable to get current temperature for blind {}", blind.getAutoRollershutterItemName());
            return Double.NaN;
        }

        try {

            NumberItem numberItem = (NumberItem) itemRegistry.getItem(blind.getTemperatureItemName());
            if (numberItem != null) {
                State currentValue = numberItem.getStateAs(DecimalType.class);
                if (currentValue != null) {
                    return ((DecimalType) currentValue).doubleValue();
                }
            }
        } catch (ItemNotFoundException e) {
            logger.warn("Unable to find temperature item with name {}", blind.getTemperatureItemName());
        }
        return Double.NaN;
    }

    @RuleAction(label = "Get blinds below temperature range", description = "Returns a set of blinds where the current temperature is below the temperature range.")
    public static Set<Integer> getBlindsBelowTemperatureRange(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "The item on which the evaluation should be performed. For group items all members are checked") org.eclipse.smarthome.core.items.Item item) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsBelowTemperatureRange(item);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds below temperature range", description = "Returns a set of blinds where the current temperature is below the temperature range.")
    public Set<Integer> getBlindsBelowTemperatureRange(
            @ActionInput(name = "item", description = "The item on which the evaluation should be performed. For group items all members are checked") org.eclipse.smarthome.core.items.Item item) {

        return getBlindsBelowTemperatureRange(getBlindSet(item));
    }

    @RuleAction(label = "Get blinds below temperature range", description = "Returns a set of blinds where the current temperature is below the temperature range.")
    public static Set<Integer> getBlindsBelowTemperatureRange(@Nullable ThingActions actions,
            @ActionInput(name = "input", description = "The input set on which the evaluation should be performed") Set<Integer> input) {

        if (actions instanceof BlindActions) {
            return ((BlindActions) actions).getBlindsBelowTemperatureRange(input);
        }
        return Collections.emptySet();
    }

    @RuleAction(label = "Get blinds below temperature range", description = "Returns a set of blinds where the current temperature is below the temperature range.")
    public Set<Integer> getBlindsBelowTemperatureRange(
            @ActionInput(name = "input", description = "The input set on which the evaluation should be performed") Set<Integer> input) {

        Set<Integer> result = new HashSet<Integer>();

        if (input != null) {
            for (Integer curIndex : input) {
                BlindItem blind = getBlindItem(curIndex);
                if (blind != null) {

                    if (blind.getConfig().isTemperatureRangeSet()
                            && getCurrentTemperature(blind) < blind.getConfig().getTemperatureLowerBound()) {
                        result.add(curIndex);
                    }
                }
            }
        }
        return result;
    }

    @RuleAction(label = "Move blinds up", description = "Moves the blind up to the specified positions if required")
    public static void moveBlindsUp(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "A rollershutter item where we would like to move the related blind up. If the item is a group item, all members will be checked. If the item is null, all registered blinds will move up") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "rollershutterPosition", description = "The target rollershutter position") final int rollershutterPosition,
            @ActionInput(name = "slatPosition", description = "The target slat position") final int targetSlatPosition) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).moveBlindsUp(item, rollershutterPosition, targetSlatPosition);
        }
    }

    @RuleAction(label = "Move blinds up", description = "Moves the blind up to the specified positions if required")
    public void moveBlindsUp(
            @ActionInput(name = "item", description = "A rollershutter item where we would like to move the related blind up. If the item is a group item, all members will be checked. If the item is null, all registered blinds will move up") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "rollershutterPosition", description = "The target rollershutter position") final int rollershutterPosition,
            @ActionInput(name = "slatPosition", description = "The target slat position") final int targetSlatPosition) {
        moveBlindsUp(getBlindSet(item), rollershutterPosition, targetSlatPosition);
    }

    @RuleAction(label = "Move blinds up", description = "Moves the blind up to the specified positions if required")
    public static void moveBlindsUp(@Nullable ThingActions actions,
            @ActionInput(name = "blindIds", description = "A set containing all blind ids that should be moved") final Set<Integer> input,
            @ActionInput(name = "rollershutterPosition", description = "The target rollershutter position") final int rollershutterPosition,
            @ActionInput(name = "slatPosition", description = "The target slat position") final int targetSlatPosition) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).moveBlindsUp(input, rollershutterPosition, targetSlatPosition);
        }
    }

    @RuleAction(label = "Move blinds up", description = "Moves the blind up to the specified positions if required")
    public void moveBlindsUp(
            @ActionInput(name = "blindIds", description = "A set containing all blind ids that should be moved") final Set<Integer> input,
            @ActionInput(name = "rollershutterPosition", description = "The target rollershutter position") final int rollershutterPosition,
            @ActionInput(name = "slatPosition", description = "The target slat position") final int targetSlatPosition) {

        for (Integer curIndex : input) {
            moveBlindUp(curIndex, rollershutterPosition, targetSlatPosition);
        }
    }

    @RuleAction(label = "Move blind up", description = "Moves the blind with the specified id up to the specified position if required")
    public static void moveBlindUp(@Nullable ThingActions actions, @ParamDoc(name = "blindId") final int blindId,
            @ActionInput(name = "rollershutterPosition") final int rollershutterPosition,
            @ActionInput(name = "slatPosition") final int targetSlatPosition) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).moveBlindUp(blindId, rollershutterPosition, targetSlatPosition);
        }
    }

    @RuleAction(label = "Move blind up", description = "Moves the blind with the specified id up to the specified position if required")
    public void moveBlindUp(@ParamDoc(name = "blindId") final int blindId,
            @ActionInput(name = "rollershutterPosition") final int rollershutterPosition,
            @ActionInput(name = "slatPosition") final int targetSlatPosition) {

        BlindItem blind = getBlindItem(blindId);
        if (blind == null) {
            logger.warn("Unable to find a registered blind for id {}", blindId);
            return;
        }

        moveBlindTo(blind, rollershutterPosition, targetSlatPosition, null, BlindDirection.UP);
    }

    @RuleAction(label = "Move blinds down", description = "Moves the blind down to the specified positions if required")
    public static void moveBlindsDown(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "A rollershutter item where we would like to move the related blind down. If the item is a group item, all members will be checked. If the item is null, all registered blinds will move down") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "rollershutterPosition", description = "The target rollershutter position") final int rollershutterPosition,
            @ActionInput(name = "slatPosition", description = "The target slat position") final int targetSlatPosition) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).moveBlindsDown(item, rollershutterPosition, targetSlatPosition);
        }
    }

    @RuleAction(label = "Move blinds down", description = "Moves the blind down to the specified positions if required")
    public void moveBlindsDown(
            @ActionInput(name = "item", description = "A rollershutter item where we would like to move the related blind down. If the item is a group item, all members will be checked. If the item is null, all registered blinds will move down") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "rollershutterPosition", description = "The target rollershutter position") final int rollershutterPosition,
            @ActionInput(name = "slatPosition", description = "The target slat position") final int targetSlatPosition) {

        moveBlindsDown(getBlindSet(item), rollershutterPosition, targetSlatPosition);
    }

    @RuleAction(label = "Move blinds down", description = "Moves the blind down to the specified positions if required")
    public static void moveBlindsDown(@Nullable ThingActions actions,
            @ActionInput(name = "blindIds", description = "A set containing all blind ids that should be moved") final Set<Integer> input,
            @ActionInput(name = "rollershutterPosition", description = "The target rollershutter position") final int rollershutterPosition,
            @ActionInput(name = "slatPosition", description = "The target slat position") final int targetSlatPosition) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).moveBlindsDown(input, rollershutterPosition, targetSlatPosition);
        }
    }

    @RuleAction(label = "Move blinds down", description = "Moves the blind down to the specified positions if required")
    public void moveBlindsDown(
            @ActionInput(name = "blindIds", description = "A set containing all blind ids that should be moved") final Set<Integer> input,
            @ActionInput(name = "rollershutterPosition", description = "The target rollershutter position") final int rollershutterPosition,
            @ActionInput(name = "slatPosition", description = "The target slat position") final int targetSlatPosition) {

        for (Integer curIndex : input) {
            moveBlindDown(curIndex, rollershutterPosition, targetSlatPosition);
        }
    }

    @RuleAction(label = "Move blind down", description = "Moves the blind with the specified id down to the specified position if required")
    public static void moveBlindDown(@Nullable ThingActions actions, @ActionInput(name = "blindId") final int blindId,
            @ActionInput(name = "rollershutterPosition") final int rollershutterPosition,
            @ActionInput(name = "slatPosition") final int targetSlatPosition) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).moveBlindDown(blindId, rollershutterPosition, targetSlatPosition);
        }
    }

    @RuleAction(label = "Move blind down", description = "Moves the blind with the specified id down to the specified position if required")
    public void moveBlindDown(@ActionInput(name = "blindId") final int blindId,
            @ActionInput(name = "rollershutterPosition") final int rollershutterPosition,
            @ActionInput(name = "slatPosition") final int targetSlatPosition) {

        BlindItem blind = getBlindItem(blindId);
        if (blind == null) {
            logger.warn("Unable to find a registered blind for id {}", blindId);
            return;
        }

        moveBlindTo(blind, rollershutterPosition, targetSlatPosition, OpenClosedType.CLOSED, BlindDirection.DOWN);
    }

    @RuleAction(label = "Move blinds to", description = "Sends changes the blind to the specified position")
    public static void moveBlindsTo(@Nullable ThingActions actions,
            @ActionInput(name = "item", description = "A rollershutter item where we would like to move the related blind. If the item is a group item, all members will be checked. If the item is null, all registered blinds will move to the defined position") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "rollershutterPosition", description = "The target rollershutter position") final int rollershutterPosition,
            @ActionInput(name = "slatPosition", description = "The target slat position") final int targetSlatPosition) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).moveBlindsTo(item, rollershutterPosition, targetSlatPosition);
        }
    }

    @RuleAction(label = "Move blinds to", description = "Sends changes the blind to the specified position")
    public void moveBlindsTo(
            @ActionInput(name = "item", description = "A rollershutter item where we would like to move the related blind. If the item is a group item, all members will be checked. If the item is null, all registered blinds will move to the defined position") final org.eclipse.smarthome.core.items.Item item,
            @ActionInput(name = "rollershutterPosition", description = "The target rollershutter position") final int rollershutterPosition,
            @ActionInput(name = "slatPosition", description = "The target slat position") final int targetSlatPosition) {

        moveBlindsTo(getBlindSet(item), rollershutterPosition, targetSlatPosition);
    }

    @RuleAction(label = "Move blinds to", description = "Sends changes the blind to the specified position")
    public static void moveBlindsTo(@Nullable ThingActions actions,
            @ActionInput(name = "blindIds", description = "A set containing all blind ids that should be moved") final Set<Integer> input,
            @ActionInput(name = "rollershutterPosition", description = "The target rollershutter position") final int rollershutterPosition,
            @ActionInput(name = "slatPosition", description = "The target slat position") final int targetSlatPosition) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).moveBlindsTo(input, rollershutterPosition, targetSlatPosition);
        }
    }

    @RuleAction(label = "Move blinds to", description = "Sends changes the blind to the specified position")
    public void moveBlindsTo(
            @ActionInput(name = "blindIds", description = "A set containing all blind ids that should be moved") final Set<Integer> input,
            @ActionInput(name = "rollershutterPosition", description = "The target rollershutter position") final int rollershutterPosition,
            @ActionInput(name = "slatPosition", description = "The target slat position") final int targetSlatPosition) {

        for (Integer curIndex : input) {
            moveBlindTo(curIndex, rollershutterPosition, targetSlatPosition);
        }
    }

    @RuleAction(label = "Move blinds to", description = "Moves the blind with the specified id to the specified position")
    public static void moveBlindTo(@Nullable ThingActions actions, @ActionInput(name = "blind") final int blindId,
            @ActionInput(name = "rollershutterPosition") final int rollershutterPosition,
            @ActionInput(name = "slatPosition") final int targetSlatPosition) {

        if (actions instanceof BlindActions) {
            ((BlindActions) actions).moveBlindTo(blindId, rollershutterPosition, targetSlatPosition);
        }
    }

    @RuleAction(label = "Move blinds to", description = "Moves the blind with the specified id to the specified position")
    public void moveBlindTo(@ActionInput(name = "blind") final int blindId,
            @ActionInput(name = "rollershutterPosition") final int rollershutterPosition,
            @ActionInput(name = "slatPosition") final int targetSlatPosition) {

        BlindItem blind = getBlindItem(blindId);
        if (blind == null) {
            logger.warn("Unable to find a registered blind for id {}", blindId);
            return;
        }

        moveBlindTo(blind, rollershutterPosition, targetSlatPosition, OpenClosedType.CLOSED, BlindDirection.ANY);
    }

    private void moveBlindTo(final BlindItem blind, int rollershutterPositionInput, int targetSlatPositionInput,
            @Nullable State expectedRelatedState, BlindDirection direction) {

        int rollershutterPosition = rollershutterPositionInput;
        int targetSlatPosition = targetSlatPositionInput;

        if (suspended.get()) {
            logger.debug("Not changing position of blind {} as the blind manager is supspended",
                    blind.getAutoRollershutterItemName());
            return;
        }

        ItemRegistry itemRegistry = getItemRegistry();
        if (itemRegistry == null) {
            logger.warn("Unable to update blind {} to position to {} and slat position to {} "
                    + "as there is no item registry available. It seems that the action is not set up correctly",
                    blind.getAutoRollershutterItemName(), rollershutterPosition, targetSlatPosition);
            return;
        }

        BlindsConfiguration blindsConfiguration = thingHandler.getBlindsConfiguration();
        if (blindsConfiguration == null) {
            logger.warn("Unable to update blind {} to position to {} and slat position to {} "
                    + "as there is no valid configuration available. It seems that the action is not set up correctly",
                    blind.getAutoRollershutterItemName(), rollershutterPosition, targetSlatPosition);
            return;
        }

        ContactItem windowContactItem = null;

        try {
            windowContactItem = blind.getWindowContactItem();
        } catch (ClassCastException e) {
            logger.warn("Wrong item type registered: {}", e.getMessage());
            return;
        }

        if (expectedRelatedState != null && windowContactItem != null
                && !windowContactItem.getState().equals(expectedRelatedState)) {
            logger.info(
                    "Not changing position of blind {} as the related item {} has an unexpected state. Current state is {}, expected state is {}",
                    blind.getAutoRollershutterItemName(), blind.getWindowContactName(), windowContactItem.getState(),
                    expectedRelatedState);
            return;
        }

        logger.debug(
                "Created new job to update blind position of {}"
                        + " to position {} and slat position to {} (config={})",
                blind.getAutoRollershutterItemName(), rollershutterPosition, targetSlatPosition, blindsConfiguration);

        MoveBlindJob job = new MoveBlindJob(blind, rollershutterPosition, targetSlatPosition, direction,
                blindsConfiguration);
        blindsThread.scheduleJob(job);
    }

}
