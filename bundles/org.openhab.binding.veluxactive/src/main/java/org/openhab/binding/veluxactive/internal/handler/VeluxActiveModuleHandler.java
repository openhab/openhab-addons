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
package org.openhab.binding.veluxactive.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.veluxactive.internal.ModuleRequestContext;
import org.openhab.binding.veluxactive.internal.api.dto.HomeStatus;
import org.openhab.binding.veluxactive.internal.api.dto.Module;
import org.openhab.binding.veluxactive.internal.config.VeluxActiveModuleConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * Shares account connectivity and lifecycle handling for physical modules.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public abstract class VeluxActiveModuleHandler extends BaseThingHandler {

    private volatile @Nullable VeluxActiveAccountHandler accountHandler;
    private volatile long generation;
    private volatile boolean active;
    private volatile @Nullable VeluxActiveModuleConfiguration configuration;

    protected VeluxActiveModuleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        synchronized (this) {
            active = false;
            configuration = getConfigAs(VeluxActiveModuleConfiguration.class);
            generation++;
            active = true;
            updateStatus(ThingStatus.UNKNOWN);
        }
        refreshFromBridge();
    }

    private void refreshFromBridge() {
        long session = generation;
        VeluxActiveModuleConfiguration config = configuration;
        if (config == null) {
            return;
        }
        Bridge bridge = getBridge();
        VeluxActiveAccountHandler account = account();
        synchronized (this) {
            if (!active || session != generation) {
                return;
            }
            accountHandler = account;
            ModuleRequestContext requestContext = captureRequestContext();
            if (config.moduleId.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error.module.missing-id");
                return;
            }
            if (bridge == null || account == null || requestContext == null) {
                clearState();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
                return;
            }
            if (bridge.getStatus() != ThingStatus.ONLINE) {
                clearState();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                return;
            }
            if (!isRequestContextCurrent(requestContext)) {
                return;
            }
            if (getThing().getStatus() == ThingStatus.OFFLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/status.module.awaiting-status");
            }
        }
        // Never acquire the account monitor while holding the child monitor.
        account.requestPoll();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo status) {
        synchronized (this) {
            if (!active) {
                return;
            }
            generation++;
        }
        refreshFromBridge();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH && captureRequestContext() != null) {
            VeluxActiveAccountHandler account = account();
            if (account != null) {
                account.requestPoll();
            }
        }
    }

    protected @Nullable VeluxActiveAccountHandler account() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            if (bridge.getHandler() instanceof VeluxActiveAccountHandler account
                    && this instanceof VeluxActiveGatewayHandler) {
                return account;
            }
            if (bridge.getHandler() instanceof VeluxActiveGatewayHandler gateway) {
                return gateway.account();
            }
        }
        return null;
    }

    protected @Nullable ModuleRequestContext captureRequestContext() {
        long session = generation;
        VeluxActiveModuleConfiguration config = configuration;
        if (!active || config == null || config.moduleId.isBlank()) {
            return null;
        }
        if (this instanceof VeluxActiveGatewayHandler) {
            VeluxActiveAccountHandler account = accountHandler;
            return account == null ? null
                    : new ModuleRequestContext(this, account.homeId(config.moduleId), config.moduleId, session, null);
        }
        Bridge bridge = getBridge();
        ModuleRequestContext parent = bridge != null && bridge.getHandler() instanceof VeluxActiveGatewayHandler gateway
                ? gateway.captureRequestContext()
                : null;
        return parent == null ? null
                : new ModuleRequestContext(this, parent.homeId(), config.moduleId, session, parent);
    }

    protected boolean isRequestContextCurrent(ModuleRequestContext requestContext) {
        if (!active || generation != requestContext.generation()) {
            return false;
        }
        ModuleRequestContext parent = requestContext.parent();
        // Parent generations invalidate child work without acquiring a parent monitor.
        if (parent != null) {
            return parent.handler().isRequestContextCurrent(parent)
                    && parent.handler().getThing().getStatus() == ThingStatus.ONLINE;
        }
        VeluxActiveAccountHandler account = accountHandler;
        return account != null && requestContext.homeId().equals(account.homeId(requestContext.moduleId()));
    }

    protected boolean checkGateway(ModuleRequestContext requestContext, Module module) {
        ModuleRequestContext parentRequestContext = requestContext.parent();
        String reportedBridge = module.bridge;
        if (parentRequestContext != null && reportedBridge != null
                && !parentRequestContext.moduleId().equals(reportedBridge)) {
            clearState();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.module.wrong-gateway");
            return false;
        }
        return true;
    }

    abstract void update(ModuleRequestContext requestContext, HomeStatus status);

    synchronized void failed(ModuleRequestContext requestContext, @Nullable String message) {
        if (isRequestContextCurrent(requestContext)) {
            clearState();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        }
    }

    protected void missingStatus(String description) {
        clearState();
        // A partial response does not establish loss or restoration of connectivity.
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, description);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, description);
        }
    }

    protected abstract void clearState();

    @Override
    public synchronized void dispose() {
        active = false;
        accountHandler = null;
        generation++;
    }
}
