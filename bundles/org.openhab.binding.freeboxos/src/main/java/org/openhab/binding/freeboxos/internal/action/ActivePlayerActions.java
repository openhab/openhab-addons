/**
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {ActivePlayerActions} class is responsible to call corresponding actions on Freebox Player with API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@ThingActionsScope(name = "freeboxos")
@NonNullByDefault
public class ActivePlayerActions extends PlayerActions {
    private final Logger logger = LoggerFactory.getLogger(ActivePlayerActions.class);

    @RuleAction(label = "reboot freebox player", description = "Reboots the Freebox Player")
    public void reboot() {
        logger.debug("Player reboot called");
        PlayerHandler localHandler = this.handler;
        if (localHandler instanceof ActivePlayerHandler apHandler) {
            apHandler.reboot();
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null");
        }
    }

    public static void reboot(ThingActions actions) {
        ((ActivePlayerActions) actions).reboot();
=======
 * Copyright (c) 2010-2022 Contributors to the openHAB project
=======
 * Copyright (c) 2010-2023 Contributors to the openHAB project
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {ActivePlayerActions} class is responsible to call corresponding actions on Freebox Player with API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@ThingActionsScope(name = "freeboxos")
@NonNullByDefault
public class ActivePlayerActions extends PlayerActions {
    private final Logger logger = LoggerFactory.getLogger(ActivePlayerActions.class);

    @RuleAction(label = "reboot freebox player", description = "Reboots the Freebox Player")
    public void reboot() {
        logger.debug("Player reboot called");
        PlayerHandler playerHandler = this.handler;
        if (playerHandler instanceof ActivePlayerHandler) {
            playerHandler.reboot();
        } else {
            logger.warn("Freebox Player Action service ThingHandler is null!");
        }
>>>>>>> 46dadb1 SAT warnings handling
    }

    public static void reboot(ThingActions actions) {
        ((ActivePlayerActions) actions).reboot();
    }
}
