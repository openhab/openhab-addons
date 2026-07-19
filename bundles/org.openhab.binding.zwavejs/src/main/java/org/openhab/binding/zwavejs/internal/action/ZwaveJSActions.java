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
package org.openhab.binding.zwavejs.internal.action;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
 * This class allows interaction with Z-Wave devices through rule actions.
 * It implements the {@link ThingActions} interface and provides methods forn
 * inclusion, exclusion, and sending multicast commands to Z-Wave nodes.
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
    private @Nullable ScheduledFuture<?> actionStopJob;
    private @Nullable ActiveAction activeAction;

    @RuleAction(label = "@text/action.start-inclusion.label", description = "@text/action.start-inclusion.description")
    public synchronized void startInclusion() {
        ZwaveJSBridgeHandler localHandler = handler;
        if (localHandler != null) {
            logger.debug("Inclusion action issued");
            stopActiveAction(localHandler);
            localHandler.startInclusion();
            activeAction = ActiveAction.INCLUSION;
            actionStopJob = SCHEDULER.schedule(() -> stopActiveAction(localHandler, ActiveAction.INCLUSION),
                    INCLUSION_EXCLUSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
    }

    public static void startInclusion(ThingActions actions) {
        ((ZwaveJSActions) actions).startInclusion();
    }

    @RuleAction(label = "@text/action.start-exclusion.label", description = "@text/action.start-exclusion.description")
    public synchronized void startExclusion() {
        ZwaveJSBridgeHandler localHandler = handler;
        if (localHandler != null) {
            logger.debug("Exclusion action issued");
            stopActiveAction(localHandler);
            localHandler.startExclusion();
            activeAction = ActiveAction.EXCLUSION;
            actionStopJob = SCHEDULER.schedule(() -> stopActiveAction(localHandler, ActiveAction.EXCLUSION),
                    INCLUSION_EXCLUSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
    }

    public static void startExclusion(ThingActions actions) {
        ((ZwaveJSActions) actions).startExclusion();
    }

    @RuleAction(label = "@text/action.send-multicast-command.label", description = "@text/action.send-multicast-command.description")
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
    public synchronized void setThingHandler(@Nullable ThingHandler handler) {
        cancelActionStopJob();
        activeAction = null;
        if (handler instanceof ZwaveJSBridgeHandler bridgeHandler) {
            this.handler = bridgeHandler;
        } else {
            this.handler = null;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    private synchronized void stopActiveAction(ZwaveJSBridgeHandler expectedHandler, ActiveAction expectedAction) {
        if (expectedHandler.equals(handler) && activeAction == expectedAction) {
            stopActiveAction(expectedHandler);
        }
    }

    private void stopActiveAction(ZwaveJSBridgeHandler handler) {
        cancelActionStopJob();
        ActiveAction activeAction = this.activeAction;
        this.activeAction = null;
        if (activeAction == ActiveAction.INCLUSION) {
            handler.stopInclusion();
        } else if (activeAction == ActiveAction.EXCLUSION) {
            handler.stopExclusion();
        }
    }

    private void cancelActionStopJob() {
        ScheduledFuture<?> actionStopJob = this.actionStopJob;
        if (actionStopJob != null) {
            actionStopJob.cancel(false);
            this.actionStopJob = null;
        }
    }

    private enum ActiveAction {
        INCLUSION,
        EXCLUSION
    }
}
