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
import org.openhab.binding.freeboxos.internal.handler.ActivePlayerHandler;
import org.openhab.binding.freeboxos.internal.handler.PlayerHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {ActivePlayerActions} class is responsible to call corresponding actions on Freebox Player with API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ActivePlayerActions.class)
@ThingActionsScope(name = "freeboxos")
@NonNullByDefault
public class ActivePlayerActions extends PlayerActions {
    private final Logger logger = LoggerFactory.getLogger(ActivePlayerActions.class);

    @RuleAction(label = "@text/action.rebootPlayer.label", description = "@text/action.rebootPlayer.description")
    public void rebootPlayer() {
        logger.debug("Player reboot called");
        PlayerHandler localHandler = this.handler;
        if (localHandler instanceof ActivePlayerHandler apHandler) {
            apHandler.reboot();
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null");
        }
    }

    public static void rebootPlayer(ThingActions actions) {
        if (actions instanceof ActivePlayerActions activePlayerActions) {
            activePlayerActions.rebootPlayer();
        } else {
            throw new IllegalArgumentException("actions parameter is not an ActivePlayerActions class.");
        }
    }

    @Override
    @RuleAction(label = "@text/action.sendKey.label", description = "@text/action.sendKey.description")
    public void sendKey(@ActionInput(name = "key", label = "@text/action.input.key.label") String key) {
        super.sendKey(key);
    }

    @Override
    @RuleAction(label = "@text/action.sendLongKey.label", description = "@text/action.sendLongKey.description")
    public void sendLongKey(@ActionInput(name = "key", label = "@text/action.input.key.label") String key) {
        super.sendLongKey(key);
    }

    @Override
    @RuleAction(label = "@text/action.sendMultipleKeys.label", description = "@text/action.sendMultipleKeys.description")
    public void sendMultipleKeys(
            @ActionInput(name = "keys", label = "@text/action.sendMultipleKeys.input.keys.label", description = "@text/action.sendMultipleKeys.input.keys.description") String keys) {
        super.sendMultipleKeys(keys);
    }

    @Override
    @RuleAction(label = "@text/action.sendKeyRepeat.label", description = "@text/action.sendKeyRepeat.description")
    public void sendKeyRepeat(@ActionInput(name = "key", label = "@text/action.input.key.label") String key,
            @ActionInput(name = "count", label = "@text/action.sendKeyRepeat.input.count.label", description = "@text/action.sendKeyRepeat.input.count.description") int count) {
        super.sendKeyRepeat(key, count);
    }
}
