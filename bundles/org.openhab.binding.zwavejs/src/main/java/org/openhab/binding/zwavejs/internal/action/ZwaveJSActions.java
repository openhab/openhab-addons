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
package org.openhab.binding.zwavejs.internal.action;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.BindingConstants;
import org.openhab.binding.zwavejs.internal.handler.ZwaveJSBridgeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Leo Siepel - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ZwaveJSActions.class)
@ThingActionsScope(name = "zwavejs")
@NonNullByDefault
public class ZwaveJSActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(ZwaveJSActions.class);
    private static final ScheduledExecutorService SCHEDULER = ThreadPoolManager
            .getScheduledPool(BindingConstants.BINDING_ID);
    private static final int INCLUSION_EXCLUSION_TIMEOUT_SECONDS = 30;
    private @Nullable ZwaveJSBridgeHandler handler;

    @RuleAction(label = "start inclusion", description = "Put the controller for 30s in network wide inclusion mode")
    public void startInclusion() {
        ZwaveJSBridgeHandler localHandler = handler;
        if (localHandler != null) {
            logger.debug("Inclusion action issued");
            localHandler.startInclusion();
            SCHEDULER.schedule(() -> {
                localHandler.stopInclusion();
            }, INCLUSION_EXCLUSION_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    public static void startInclusion(ThingActions actions) {
        ((ZwaveJSActions) actions).startInclusion();
    }

    @RuleAction(label = "start exclusion", description = "Put the controller for 30s in network wide exclusion mode")
    public void startExclusion() {
        ZwaveJSBridgeHandler localHandler = handler;
        if (localHandler != null) {
            logger.debug("Exclusion action issued");
            localHandler.startExclusion();
            SCHEDULER.schedule(() -> {
                localHandler.stopExclusion();
            }, INCLUSION_EXCLUSION_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    public static void startExclusion(ThingActions actions) {
        ((ZwaveJSActions) actions).startExclusion();
    }

    @RuleAction(label = "send multicast command", description = "Send command to multiple nodes")
    public void sendMulticastCommand(
            @ActionInput(name = "nodeIDs", label = "node IDs", description = "Comma separated list of Z-Wave node IDs") String nodeIDs,
            @ActionInput(name = "commandClass", label = "command class", description = "Z-Wave command class") Integer commandClass,
            @ActionInput(name = "endpoint", label = "endpoint", description = "Z-Wave endpoint") Integer endpoint,
            @ActionInput(name = "property", label = "property", description = "Z-Wave write property") String property,
            @ActionInput(name = "value", label = "value", description = "Value to write") String value) {
        ZwaveJSBridgeHandler localHandler = handler;
        if (localHandler != null) {
            logger.debug("Multicast action issued");
            localHandler.sendMulticastCommand(nodeIDs, commandClass, endpoint, property, value);
        }
    }

    public static void sendMulticastCommand(ThingActions actions, String nodeIDs, Integer commandClass,
            Integer endpoint, String property, String value) {
        ((ZwaveJSActions) actions).sendMulticastCommand(nodeIDs, commandClass, endpoint, property, value);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ZwaveJSBridgeHandler bridgeHandler) {
            this.handler = bridgeHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
