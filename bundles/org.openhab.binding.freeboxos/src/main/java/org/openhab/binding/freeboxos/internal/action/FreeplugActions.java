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
import org.openhab.binding.freeboxos.internal.handler.FreeplugHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {FreeplugActions} class is responsible to call corresponding actions on Freeplugs
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = FreeplugActions.class)
@ThingActionsScope(name = "freeboxos")
@NonNullByDefault
public class FreeplugActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(FreeplugActions.class);
    private @Nullable FreeplugHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof FreeplugHandler plugHandler) {
            this.handler = plugHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "@text/action.resetPlug.label", description = "@text/action.resetPlug.description")
    public void resetPlug() {
        logger.debug("Freeplug reset requested");
        FreeplugHandler plugHandler = this.handler;
        if (plugHandler != null) {
            plugHandler.reset();
        } else {
            logger.warn("Freeplug Action service ThingHandler is null");
        }
    }

    public static void resetPlug(ThingActions actions) {
        if (actions instanceof FreeplugActions freeplugActions) {
            freeplugActions.resetPlug();
        } else {
            throw new IllegalArgumentException("actions parameter is not a FreeplugActions class.");
        }
    }
}
