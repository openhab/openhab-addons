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
package org.openhab.binding.tado.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.api.client.HomeApi;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;

/**
 * Common base class for home-based thing-handler.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
@NonNullByDefault
public abstract class BaseHomeThingHandler extends BaseThingHandler {

    private int failedReconnectAttempts;

    public BaseHomeThingHandler(Thing thing) {
        super(thing);
    }

    public @Nullable Long getHomeId() {
        TadoHomeHandler handler = getHomeHandler();
        return handler.getHomeId();
    }

    protected TadoHomeHandler getHomeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            throw new IllegalStateException("Bridge not initialized");
        }
        BridgeHandler handler = bridge.getHandler();
        if (!(handler instanceof TadoHomeHandler)) {
            throw new IllegalStateException("Handler not initialized");
        }
        return (TadoHomeHandler) handler;
    }

    protected HomeApi getApi() {
        TadoHomeHandler handler = getHomeHandler();
        return handler.getApi();
    }

    protected void onSuccessfulOperation() {
        // update without error -> we're back online
        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Method overridden to set the failedReconnectAttempts field.
     */
    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        switch (status) {
            case ONLINE: {
                failedReconnectAttempts = 0;
                break;
            }
            case OFFLINE: {
                if (statusDetail == ThingStatusDetail.COMMUNICATION_ERROR) {
                    if (failedReconnectAttempts < Integer.MAX_VALUE) {
                        failedReconnectAttempts++;
                        break;
                    }
                }
            }
            default: {
                failedReconnectAttempts = Integer.MAX_VALUE;
            }
        }
        super.updateStatus(status, statusDetail, description);
    }

    /**
     * Check if the thing shall try to go online again. Prerequisite is that the bridge exists and is online, yet the
     * thing is offline as a result of a communication error, and the maximum number of reconnection attempts has not
     * been exceeded.
     *
     * @param maxReconnectAttempts the maximum allowed number of reconnection attempts.
     * @return true if the thing shall try to go online again.
     */
    public boolean shallTryReconnecting(int maxReconnectAttempts) {
        Bridge bridge = getBridge();
        return (bridge != null) && (bridge.getStatus() == ThingStatus.ONLINE)
                && (thing.getStatus() == ThingStatus.OFFLINE)
                && (thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR)
                && ((maxReconnectAttempts < 0) || (maxReconnectAttempts > failedReconnectAttempts));
    }
}
