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
package org.openhab.binding.notifications.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.notifications.internal.NotificationsHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides static methods that can be used in automation rules
 * for handling of notifications
 *
 * @author Markus Pfleger - Initial contribution
 *
 */
@ThingActionsScope(name = "notifications")
@NonNullByDefault
public class NotificationsAction implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(NotificationsAction.class);

    private @Nullable NotificationsHandler thingHandler;

    @Override
    public void activate() {
        ThingActions.super.activate();

        logger.info("Notifications 2.0 action service has been activated.");
    }

    @Override
    public void deactivate() {
        ThingActions.super.deactivate();
        logger.info("Notifications 2.0 action service has been deactivated.");
    }

    public static void notificationBlinking(ThingActions actions, @ActionInput(name = "group") Item item) {

        if (actions instanceof NotificationsAction) {
            ((NotificationsAction) actions).notificationBlinking(item);
        } else {
            throw new IllegalArgumentException(
                    "Unable to trigger blinking notification ThingActions: " + actions.getClass().getSimpleName());
        }
    }

    /**
     * Triggers a notification for this group by making it "blink". So each
     * item will be turned off and on again several times.
     *
     * Afterwards the initial state will be restored
     *
     * @param group the group where the command should be sent to
     * @param count the number of times the item should blink
     */
    @RuleAction(label = "Trigger notification", description = "Starts a blinking notification for each group member")
    public void notificationBlinking(@ActionInput(name = "group") Item item) {
        notificationBlinking(item, 4);
    }

    @RuleAction(label = "Trigger notification", description = "Starts a blinking notification for each group member")
    public static void notificationBlinking(ThingActions actions, @ActionInput(name = "item") Item eclipseItem,
            @ActionInput(name = "count") int count) {

        if (actions instanceof NotificationsAction) {
            ((NotificationsAction) actions).notificationBlinking(eclipseItem, count);
        } else {
            throw new IllegalArgumentException(
                    "Unable to trigger blinking notification ThingActions: " + actions.getClass().getSimpleName());
        }
    }

    /**
     * Triggers a notification for this group by making it "blink". So each
     * item will be turned off and on again several times.
     *
     * Afterwards the initial state will be restored
     *
     * @param group the group where the command should be sent to
     */
    @RuleAction(label = "Trigger notification", description = "Starts a blinking notification for each group member")
    public void notificationBlinking(@ActionInput(name = "item") Item item, @ActionInput(name = "count") int count) {

        List<SwitchItem> allMembers = new ArrayList<>();
        if (item instanceof GroupItem) {
            Set<Item> groupMembers = ((GroupItem) item).getAllMembers();
            for (Item groupMember : groupMembers) {
                if (groupMember instanceof SwitchItem) {
                    allMembers.add((SwitchItem) groupMember);
                }
            }
        } else if (item instanceof SwitchItem) {
            allMembers.add((SwitchItem) item);
        }

        Map<SwitchItem, State> allMembersOriginalState = new HashMap<>();

        logger.info("Start blinking for group {} containing {} items", item, allMembers.size());

        // 1. keep the original state of all members
        for (SwitchItem curItem : allMembers) {
            allMembersOriginalState.put(curItem, curItem.getState());
        }

        // 2. blink several times
        for (int i = 0; i < count * 2; i++) {
            OnOffType onOffcommand = OnOffType.ON;
            if (i % 2 != 0) {
                onOffcommand = OnOffType.OFF;
            }

            for (SwitchItem curItem : allMembers) {
                curItem.send(onOffcommand);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }

        // 3. restore the original state of all members
        for (Entry<SwitchItem, State> curItem : allMembersOriginalState.entrySet()) {
            State originalState = curItem.getValue();
            if (originalState instanceof OnOffType) {
                // it is simple to restore such types, simply send the command to the item
                curItem.getKey().send((OnOffType) originalState);
            } else {
                // maybe the state was undefined or something like that, make sure it is turned off if we are not sure
                curItem.getKey().send(OnOffType.OFF);
            }
        }

        logger.info("Finished blinking for group {} containing {} items", item, allMembers.size());
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof NotificationsHandler) {
            this.thingHandler = (NotificationsHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return thingHandler;
    }

}
