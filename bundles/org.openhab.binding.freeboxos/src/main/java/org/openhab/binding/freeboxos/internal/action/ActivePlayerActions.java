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

    @RuleAction(label = "reboot freebox player", description = "Reboots the Freebox Player")
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
}
