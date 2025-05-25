/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.homeassistant.internal.handler.HomeAssistantThingHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the automation engine action handler service for the update action.
 *
 * @author Cody Cutrer - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = HomeAssistantUpdateThingActions.class)
@ThingActionsScope(name = "mqtt")
@NonNullByDefault
public class HomeAssistantUpdateThingActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(HomeAssistantUpdateThingActions.class);
    private @Nullable HomeAssistantThingHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (HomeAssistantThingHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/updateActionLabel", description = "@text/updateActionDesc")
    public void update() {
        HomeAssistantThingHandler handler = this.handler;
        if (handler == null) {
            logger.warn("Home Assistant Update Action Service ThingHandler is null!");
            return;
        }
        handler.doUpdate();
    }

    public static void update(ThingActions actions) {
        ((HomeAssistantUpdateThingActions) actions).update();
    }
}
