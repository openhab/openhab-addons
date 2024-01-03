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

    @RuleAction(label = "send a key to player", description = "Sends a given key to the player")
    public void sendKey(@ActionInput(name = "key") String key) {
        logger.debug("Sending key {} to player", key);
        PlayerHandler playerHandler = this.handler;
        if (playerHandler != null) {
            playerHandler.sendKey(key, false, 1);
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null");
        }
    }

    @RuleAction(label = "send a long key to player", description = "Sends a given key to the player and keep it pressed")
    public void sendLongKey(@ActionInput(name = "key") String key) {
        logger.debug("Sending long press key {} to player", key);
        PlayerHandler playerHandler = this.handler;
        if (playerHandler != null) {
            playerHandler.sendKey(key, true, 1);
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null");
        }
    }

    @RuleAction(label = "send multiple keys to player", description = "Sends multiple keys to the player, comma separated")
    public void sendMultipleKeys(@ActionInput(name = "keys") String keys) {
        logger.debug("Sending keys {} to player", keys);
        PlayerHandler playerHandler = this.handler;
        if (playerHandler != null) {
            playerHandler.sendMultipleKeys(keys);
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null");
        }
    }

    @RuleAction(label = "send repeating key to player", description = "Sends a given key multiple times to the player")
    public void sendKeyRepeat(@ActionInput(name = "key") String key, @ActionInput(name = "count") int count) {
        logger.debug("Sending key {} to player {} times", key, count);
        PlayerHandler playerHandler = this.handler;
        if (playerHandler != null) {
            playerHandler.sendKey(key, false, count);
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null");
        }
    }
}
