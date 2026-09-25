/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hasslink.internal.action;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
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
 * Exposes Home Assistant bridge actions (service calls, scripts, scenes, buttons) to openHAB rules.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@Component(service = HassLinkActions.class, scope = ServiceScope.PROTOTYPE)
@ThingActionsScope(name = "hasslink")
@NonNullByDefault
public class HassLinkActions implements ThingActions {

    private static final Logger logger = LoggerFactory.getLogger(HassLinkActions.class);
    private @Nullable HassLinkBridgeHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof HassLinkBridgeHandler bridgeHandler) {
            this.handler = bridgeHandler;
        } else {
            this.handler = null;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "Call Service", description = "Calls any Home Assistant service")
    public void callService( //
            @ActionInput(name = "domain", label = "Domain") String domain, //
            @ActionInput(name = "service", label = "Service") String service, //
            @ActionInput(name = "entityId", label = "Entity ID") @Nullable String entityId, //
            @ActionInput(name = "payload", label = "Payload") @Nullable Map<String, Object> payload) {

        HassLinkBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.callService(domain, service, entityId, payload);
        } else {
            logger.warn("HassLink Bridge handler is not available. Cannot call service {}.{}", domain, service);
        }
    }

    @RuleAction(label = "Press Button", description = "Triggers a Home Assistant button entity")
    public void pressButton( //
            @ActionInput(name = "entityId", label = "Entity ID") String entityId) {

        HassLinkBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.pressButton(entityId);
        } else {
            logger.warn("HassLink Bridge handler is not available. Cannot press button {}", entityId);
        }
    }

    @RuleAction(label = "Run Script", description = "Executes a Home Assistant script")
    public void runScript( //
            @ActionInput(name = "scriptId", label = "Script ID") String scriptId, //
            @ActionInput(name = "variables", label = "Variables") @Nullable Map<String, Object> variables) {

        HassLinkBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.runScript(scriptId, variables);
        } else {
            logger.warn("HassLink Bridge handler is not available. Cannot run script {}", scriptId);
        }
    }

    @RuleAction(label = "Activate Scene", description = "Activates a Home Assistant scene")
    public void activateScene(@ActionInput(name = "sceneId", label = "Scene ID") String sceneId) {

        HassLinkBridgeHandler localHandler = this.handler;
        if (localHandler != null) {
            localHandler.activateScene(sceneId);
        } else {
            logger.warn("HassLink Bridge handler is not available. Cannot activate scene {}", sceneId);
        }
    }
}
