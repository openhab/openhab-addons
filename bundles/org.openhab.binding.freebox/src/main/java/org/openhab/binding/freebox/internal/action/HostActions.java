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
package org.openhab.binding.freebox.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.freebox.internal.handler.HostHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {HostActions } class is responsible to call corresponding
 * actions on a given lan host
 *
 * @author Gaël L'hopital - Initial contribution
 */
@ThingActionsScope(name = "freebox")
@NonNullByDefault
public class HostActions implements ThingActions {
    private final static Logger logger = LoggerFactory.getLogger(HostActions.class);
    private @Nullable HostHandler handler;

    public HostActions() {
        logger.info("Freebox Lan Hosts actions service instanciated");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof HostHandler) {
            this.handler = (HostHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "Freebox Lan Host : WOL", description = "Awakes a lan host")
    public void wol() {
        logger.debug("Lan Host WOL called");
        if (handler != null) {
            handler.wol();
        } else {
            logger.warn("LanHost Action service ThingHandler is null!");
        }
    }

    public static void wol(@Nullable ThingActions actions) {
        if (actions instanceof HostActions) {
            ((HostActions) actions).wol();
        } else {
            throw new IllegalArgumentException("Instance is not an LanHostActions class.");
        }
    }
}
