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
package org.openhab.binding.max.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.max.internal.handler.MaxCubeBridgeHandler;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCubeActions} class defines rule actions for MAX! Cube
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@ThingActionsScope(name = "max-cube")
@NonNullByDefault
public class MaxCubeActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(MaxCubeActions.class);

    private @Nullable MaxCubeBridgeHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof MaxCubeBridgeHandler) {
            this.handler = (MaxCubeBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "Backup Cube Data", description = "Creates a backup of the MAX! Cube data.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean backup() {
        MaxCubeBridgeHandler localHandler = handler;
        if (localHandler == null) {
            logger.info("MaxCubeActions: Action service ThingHandler is null!");
            return false;
        }
        localHandler.backup();
        return true;
    }

    public static boolean backup(@Nullable ThingActions actions) {
        if (actions instanceof MaxCubeActions) {
            return ((MaxCubeActions) actions).backup();
        } else {
            throw new IllegalArgumentException("Instance is not of class MaxCubeActions.");
        }
    }

    @RuleAction(label = "Reset Cube Configuration", description = "Resets the MAX! Cube room and device information. Devices will need to be included again!")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean resetConfig() {
        MaxCubeBridgeHandler localHandler = handler;
        if (localHandler == null) {
            logger.info("MaxCubeActions: Action service ThingHandler is null!");
            return false;
        }
        localHandler.cubeConfigReset();
        return true;
    }

    public static boolean reset(@Nullable ThingActions actions) {
        if (actions instanceof MaxCubeActions) {
            return ((MaxCubeActions) actions).resetConfig();
        } else {
            throw new IllegalArgumentException("Instance is not of class MaxCubeActions.");
        }
    }

    @RuleAction(label = "Restart Cube", description = "Restarts the MAX! Cube.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean reboot() {
        MaxCubeBridgeHandler localHandler = handler;
        if (localHandler == null) {
            logger.info("MaxCubeActions: Action service ThingHandler is null!");
            return false;
        }
        localHandler.cubeReboot();
        return true;
    }

    public static boolean reboot(@Nullable ThingActions actions) {
        if (actions instanceof MaxCubeActions) {
            return ((MaxCubeActions) actions).reboot();
        } else {
            throw new IllegalArgumentException("Instance is not of class MaxCubeActions.");
        }
    }
}
