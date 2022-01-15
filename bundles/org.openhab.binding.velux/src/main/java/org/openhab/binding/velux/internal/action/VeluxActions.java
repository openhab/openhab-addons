/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.handler.VeluxBridgeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxActions} implementation of the rule action for rebooting the bridge
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@ThingActionsScope(name = "velux")
@NonNullByDefault
public class VeluxActions implements ThingActions, IVeluxActions {

    private final Logger logger = LoggerFactory.getLogger(VeluxActions.class);

    private @Nullable VeluxBridgeHandler bridgeHandler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof VeluxBridgeHandler) {
            this.bridgeHandler = (VeluxBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.bridgeHandler;
    }

    @Override
    @RuleAction(label = "reboot Bridge", description = "issues a reboot command to the KLF200 bridge")
    public @ActionOutput(name = "executing", type = "java.lang.Boolean", label = "executing", description = "indicates the command was issued") Boolean rebootBridge()
            throws IllegalStateException {
        logger.trace("rebootBridge(): action called");
        VeluxBridgeHandler bridge = bridgeHandler;
        if (bridge == null) {
            throw new IllegalStateException("Bridge instance is null");
        }
        return bridge.runReboot();
    }

    @Override
    @RuleAction(label = "move relative", description = "issues a relative move command to an actuator")
    public @ActionOutput(name = "executing", type = "java.lang.Boolean", label = "executing", description = "indicates the command was issued") Boolean moveRelative(
            @ActionInput(name = "nodeId", required = true, label = "nodeId", description = "actuator id in the bridge", type = "java.lang.String") String nodeId,
            @ActionInput(name = "relativePercent", required = true, label = "relativePercent", description = "position delta from current", type = "java.lang.String") String relativePercent)
            throws NumberFormatException, IllegalStateException {
        logger.trace("moveRelative(): action called");
        VeluxBridgeHandler bridge = bridgeHandler;
        if (bridge == null) {
            throw new IllegalStateException("Bridge instance is null");
        }
        int node = Integer.parseInt(nodeId);
        if (node < 0 || node > 200) {
            throw new NumberFormatException("Node Id out of range");
        }
        int relPct = Integer.parseInt(relativePercent);
        if (Math.abs(relPct) > 100) {
            throw new NumberFormatException("Relative Percent out of range");
        }
        return bridge.moveRelative(node, relPct);
    }

    /**
     * Static method to send a reboot command to a Velux Bridge
     *
     * @param actions ThingActions from the caller
     * @return true if the command was sent
     * @throws IllegalArgumentException if actions is invalid
     * @throws IllegalStateException if anything else is wrong
     */
    public static Boolean rebootBridge(@Nullable ThingActions actions)
            throws IllegalArgumentException, IllegalStateException {
        if (!(actions instanceof IVeluxActions)) {
            throw new IllegalArgumentException("Unsupported action");
        }
        return ((IVeluxActions) actions).rebootBridge();
    }

    /**
     * Static method to send a relative move command to a Velux actuator
     *
     * @param actions ThingActions from the caller
     * @param nodeId the node Id in the bridge
     * @param relativePercent the target position relative to its current position (-100% <= relativePercent <= +100%)
     * @return true if the command was sent
     * @throws IllegalArgumentException if actions is invalid
     * @throws NumberFormatException if either of nodeId or relativePercent is not an integer, or out of range
     * @throws IllegalStateException if anything else is wrong
     */
    public static Boolean moveRelative(@Nullable ThingActions actions, String nodeId, String relativePercent)
            throws IllegalArgumentException, NumberFormatException, IllegalStateException {
        if (!(actions instanceof IVeluxActions)) {
            throw new IllegalArgumentException("Unsupported action");
        }
        return ((IVeluxActions) actions).moveRelative(nodeId, relativePercent);
    }
}
