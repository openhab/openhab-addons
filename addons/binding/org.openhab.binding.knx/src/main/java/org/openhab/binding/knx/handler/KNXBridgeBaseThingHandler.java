/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.knx.client.KNXClient;
import org.openhab.binding.knx.client.StatusUpdateCallback;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.mgmt.Destination;

/**
 * The {@link KNXBridgeBaseThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Simon Kaufmann - Initial contribution and API
 */
@NonNullByDefault
public abstract class KNXBridgeBaseThingHandler extends BaseBridgeHandler implements StatusUpdateCallback {

    protected ConcurrentHashMap<IndividualAddress, Destination> destinations = new ConcurrentHashMap<>();
    private final ScheduledExecutorService knxScheduler = ThreadPoolManager.getScheduledPool("knx");
    private final ScheduledExecutorService backgroundScheduler = Executors.newSingleThreadScheduledExecutor();

    public KNXBridgeBaseThingHandler(Bridge bridge) {
        super(bridge);
    }

    protected abstract KNXClient getClient();

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        // Nothing to do here
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do here
    }

    public ScheduledExecutorService getScheduler() {
        return knxScheduler;
    }

    public ScheduledExecutorService getBackgroundScheduler() {
        return backgroundScheduler;
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

}
