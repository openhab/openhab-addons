/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.opensprinkler.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;

/**
 * @author Chris Graham - Initial contribution
 * @author Florian Schmidt - Refactoring
 */
@NonNullByDefault
public abstract class OpenSprinklerBaseHandler extends BaseThingHandler {
    @Nullable
    OpenSprinklerHttpBridgeHandler bridgeHandler;

    public OpenSprinklerBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        // if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
        // updateStatus(ThingStatus.UNKNOWN);
        // }
    }

    protected @Nullable OpenSprinklerApi getApi() {
        OpenSprinklerHttpBridgeHandler localBridge = bridgeHandler;
        if (localBridge == null) {
            return null;
        }
        try {
            return localBridge.getApi();
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
            return null;
        }
    }

    public void updateChannels() {
        this.getThing().getChannels().forEach(channel -> {
            updateChannel(channel.getUID());
        });
        if (getApi() != null) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "No HTTP Bridge thing selected");
            return;
        }
        bridgeHandler = (OpenSprinklerHttpBridgeHandler) bridge.getHandler();
        updateStatus(ThingStatus.ONLINE);
    }

    protected abstract void updateChannel(ChannelUID uid);
}
