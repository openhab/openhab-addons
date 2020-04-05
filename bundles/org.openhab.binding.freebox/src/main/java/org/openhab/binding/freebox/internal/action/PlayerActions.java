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
package org.openhab.binding.freebox.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.freebox.internal.handler.PlayerHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {PlayerActions} class is responsible to call corresponding
 * actions on Freebox Player
 *
 * @author Gaël L'hopital - Initial contribution
 */
@ThingActionsScope(name = "freebox")
@NonNullByDefault
public class PlayerActions implements ThingActions {
    private final static Logger logger = LoggerFactory.getLogger(PlayerActions.class);
    private @Nullable PlayerHandler handler;

    public PlayerActions() {
        logger.info("Freebox Player actions service instanciated");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof PlayerHandler) {
            this.handler = (PlayerHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "Player : Send a key", description = "Sends a given key to the player")
    public void sendKey(@ActionInput(name = "key") String key) {
        logger.debug("Sending key {} to player", key);
        if (handler != null) {
            handler.sendKey(key, false, 1);
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null!");
        }
    }

    public static void sendKey(@Nullable ThingActions actions, String key) {
        if (actions instanceof PlayerActions) {
            ((PlayerActions) actions).sendKey(key);
        } else {
            throw new IllegalArgumentException("Instance is not an PlayerActions class.");
        }
    }

    @RuleAction(label = "Player : Send a long key", description = "Sends a given key to the player and keep it pressed")
    public void sendLongKey(@ActionInput(name = "key") String key) {
        logger.debug("Sending long press key {} to player", key);
        if (handler != null) {
            handler.sendKey(key, true, 1);
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null!");
        }
    }

    public static void sendLongKey(@Nullable ThingActions actions, String key) {
        if (actions instanceof PlayerActions) {
            ((PlayerActions) actions).sendLongKey(key);
        } else {
            throw new IllegalArgumentException("Instance is not an PlayerActions class.");
        }
    }

    @RuleAction(label = "Player : Send multiple keys", description = "Sends multiple keys to the player, comma separated")
    public void sendMultipleKeys(@ActionInput(name = "key") String keys) {
        logger.debug("Sending keys {} to player", keys);
        if (handler != null) {
            handler.sendKey(keys, false, 1);
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null!");
        }
    }

    public static void sendMultipleKeys(@Nullable ThingActions actions, String keys) {
        if (actions instanceof PlayerActions) {
            ((PlayerActions) actions).sendMultipleKeys(keys);
        } else {
            throw new IllegalArgumentException("Instance is not an PlayerActions class.");
        }
    }

    @RuleAction(label = "Player : Send repeating key", description = "Sends a given key multiple times to the player")
    public void sendKeyRepeat(@ActionInput(name = "key") String key, @ActionInput(name = "count") int count) {
        logger.debug("Sending key {} to player {} times", key, count);
        if (handler != null) {
            handler.sendKey(key, false, count);
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null!");
        }
    }

    public static void sendKeyRepeat(@Nullable ThingActions actions, String key, int count) {
        if (actions instanceof PlayerActions) {
            ((PlayerActions) actions).sendKeyRepeat(key, count);
        } else {
            throw new IllegalArgumentException("Instance is not an PlayerActions class.");
        }
    }
}
