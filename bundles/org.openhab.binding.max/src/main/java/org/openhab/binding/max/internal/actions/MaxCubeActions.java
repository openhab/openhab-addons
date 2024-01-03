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
package org.openhab.binding.max.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.max.internal.handler.MaxCubeBridgeHandler;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCubeActions} class defines rule actions for MAX! Cube
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = MaxCubeActions.class)
@ThingActionsScope(name = "max-cube")
@NonNullByDefault
public class MaxCubeActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(MaxCubeActions.class);

    private @Nullable MaxCubeBridgeHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof MaxCubeBridgeHandler bridgeHandler) {
            this.handler = bridgeHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "backup the Cube data", description = "Creates a backup of the MAX! Cube data.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean backup() {
        MaxCubeBridgeHandler actionsHandler = handler;
        if (actionsHandler == null) {
            logger.info("MaxCubeActions: Action service ThingHandler is null!");
            return false;
        }
        actionsHandler.backup();
        return true;
    }

    public static boolean backup(ThingActions actions) {
        return ((MaxCubeActions) actions).backup();
    }

    @RuleAction(label = "reset the Cube configuration", description = "Resets the MAX! Cube room and device information. Devices will need to be included again!")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean resetConfig() {
        MaxCubeBridgeHandler actionsHandler = handler;
        if (actionsHandler == null) {
            logger.info("MaxCubeActions: Action service ThingHandler is null!");
            return false;
        }
        actionsHandler.cubeConfigReset();
        return true;
    }

    public static boolean reset(ThingActions actions) {
        return ((MaxCubeActions) actions).resetConfig();
    }

    @RuleAction(label = "restart the Cube", description = "Restarts the MAX! Cube.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean reboot() {
        MaxCubeBridgeHandler actionsHandler = handler;
        if (actionsHandler == null) {
            logger.info("MaxCubeActions: Action service ThingHandler is null!");
            return false;
        }
        actionsHandler.cubeReboot();
        return true;
    }

    public static boolean reboot(ThingActions actions) {
        return ((MaxCubeActions) actions).reboot();
    }
}
