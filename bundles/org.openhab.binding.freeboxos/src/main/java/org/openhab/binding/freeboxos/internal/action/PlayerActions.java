/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.handler.PlayerHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {PlayerActions} class is responsible to call corresponding actions on Freebox Player
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = PlayerActions.class)
@ThingActionsScope(name = "freeboxos")
@NonNullByDefault
public class PlayerActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(PlayerActions.class);
    protected @Nullable PlayerHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof PlayerHandler playerHandler) {
            this.handler = playerHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "@text/action.sendKey.label", description = "@text/action.sendKey.description")
    public void sendKey(@ActionInput(name = "key", label = "@text/action.input.key.label") String key) {
        logger.debug("Sending key {} to player", key);
        PlayerHandler playerHandler = this.handler;
        if (playerHandler != null) {
            playerHandler.sendKey(key, false, 1);
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null");
        }
    }

    @RuleAction(label = "@text/action.sendLongKey.label", description = "@text/action.sendLongKey.description")
    public void sendLongKey(@ActionInput(name = "key", label = "@text/action.input.key.label") String key) {
        logger.debug("Sending long press key {} to player", key);
        PlayerHandler playerHandler = this.handler;
        if (playerHandler != null) {
            playerHandler.sendKey(key, true, 1);
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null");
        }
    }

    @RuleAction(label = "@text/action.sendMultipleKeys.label", description = "@text/action.sendMultipleKeys.description")
    public void sendMultipleKeys(
            @ActionInput(name = "keys", label = "@text/action.sendMultipleKeys.input.keys.label", description = "@text/action.sendMultipleKeys.input.keys.description") String keys) {
        logger.debug("Sending keys {} to player", keys);
        PlayerHandler playerHandler = this.handler;
        if (playerHandler != null) {
            playerHandler.sendMultipleKeys(keys);
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null");
        }
    }

    @RuleAction(label = "@text/action.sendKeyRepeat.label", description = "@text/action.sendKeyRepeat.description")
    public void sendKeyRepeat(@ActionInput(name = "key", label = "@text/action.input.key.label") String key,
            @ActionInput(name = "count", label = "@text/action.sendKeyRepeat.input.count.label", description = "@text/action.sendKeyRepeat.input.count.description") int count) {
        logger.debug("Sending key {} to player {} times", key, count);
        PlayerHandler playerHandler = this.handler;
        if (playerHandler != null) {
            playerHandler.sendKey(key, false, count);
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null");
        }
    }

    public static void sendKey(ThingActions actions, String key) {
        if (actions instanceof PlayerActions playerActions) {
            playerActions.sendKey(key);
        } else {
            throw new IllegalArgumentException("actions parameter is not a PlayerActions class.");
        }
    }

    public static void sendLongKey(ThingActions actions, String key) {
        if (actions instanceof PlayerActions playerActions) {
            playerActions.sendLongKey(key);
        } else {
            throw new IllegalArgumentException("actions parameter is not a PlayerActions class.");
        }
    }

    public static void sendMultipleKeys(ThingActions actions, String keys) {
        if (actions instanceof PlayerActions playerActions) {
            playerActions.sendMultipleKeys(keys);
        } else {
            throw new IllegalArgumentException("actions parameter is not a PlayerActions class.");
        }
    }

    public static void sendKeyRepeat(ThingActions actions, String key, int count) {
        if (actions instanceof PlayerActions playerActions) {
            playerActions.sendKeyRepeat(key, count);
        } else {
            throw new IllegalArgumentException("actions parameter is not a PlayerActions class.");
        }
    }
}
