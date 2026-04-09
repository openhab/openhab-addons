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
package org.openhab.binding.hue.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.handler.Clip2BridgeHandler;
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
 * Implementation of the {@link ThingActions} interface for software updating.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SoftwareUpdateActions.class)
@ThingActionsScope(name = "hue")
@NonNullByDefault
public class SoftwareUpdateActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(SoftwareUpdateActions.class);
    private @Nullable ThingHandler handler;

    public static String installUpdate(ThingActions actions) {
        if (actions instanceof SoftwareUpdateActions softwareUpdateActions) {
            return softwareUpdateActions.installUpdate();
        } else {
            throw new IllegalArgumentException("The 'actions' argument is not an instance of SoftwareUpdateActions");
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = handler;
    }

    @RuleAction(label = "@text/install.update.label", description = "@text/install.update.description")
    public @ActionOutput(type = "java.lang.String", label = "@text/install.update.result.label", description = "@text/install.update.result.description") String installUpdate() {
        ThingHandler handler = this.handler;
        if (handler instanceof Clip2BridgeHandler bridgeHandler) {
            return bridgeHandler.installUpdate();
        }
        logger.warn("SoftwareUpdateActions called on unsupported ThingHandler: {}", handler);
        return "@text/install.update.error.unsupported-handler";
    }
}
