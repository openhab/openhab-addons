/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import io.rudolph.netatmo.api.energy.EnergyConnector;
import io.rudolph.netatmo.api.energy.model.BaseRoom;
import io.rudolph.netatmo.api.energy.model.HomeStatusBody;
import io.rudolph.netatmo.api.energy.model.HomesDataBody;
import io.rudolph.netatmo.api.energy.model.Room;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.CHANNEL_REACHABLE;
import static org.openhab.binding.netatmo.NetatmoBindingConstants.CHANNEL_TEMPERATURE;
import static org.openhab.binding.netatmo.NetatmoBindingConstants.CHANNEL_WINDOW_OPEN;

/**
 * {@link NetatmoRoomHandler} is the handler for a given
 * room accessed through a Netatmo Device
 *
 * @author Michael Rudolph - Initial contribution OH2 version
 */
public class NetatmoRoomHandler extends AbstractNetatmoThingHandler {
    private Logger logger = LoggerFactory.getLogger(NetatmoRoomHandler.class);
    private ScheduledFuture<?> refreshJob;
    @Nullable
    protected Room room;
    private boolean refreshRequired;

    public NetatmoRoomHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        refreshJob = scheduler.schedule(() -> {
            requestParentRefresh();
        }, 5, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    protected void updateChannels() {
        super.updateChannels();

        EnergyConnector api = getBridgeHandler().api
                .getEnergyApi();

        HomesDataBody body = api.getHomesData(getParentId())
                .executeSync();

        if (body == null
                || body.getHomes().get(0) == null
                || body.getHomes().get(0).getRooms().size() == 0) {
            return;
        }

        BaseRoom baseRoom = null;
        for (BaseRoom base: body.getHomes()
                .get(0)
                .getRooms()) {
            if (getId().equals(base.getId())) {
                baseRoom = base;
            } else {
                break;
            }
        }

        if (baseRoom == null) {
            return;
        }

        HomeStatusBody statusBody = api.getHomeStatus(getParentId())
                .executeSync();

        if (statusBody == null
                || statusBody.getHomes().get(0) == null
                || statusBody.getHomes().get(0).getRooms().size() == 0) {
            return;
        }

        Room room = null;
        for (Room base: statusBody.getHomes()
                .get(0)
                .getRooms()) {
            if (getId().equals(base.getId())) {
                room = base;
            } else {
                break;
            }
        }

        updateStatus(ThingStatus.ONLINE);
        this.room = room;
        super.updateChannels();
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        if (room == null) {
            return super.getNAThingProperty(channelId);
        }
        if (channelId.equalsIgnoreCase(CHANNEL_REACHABLE)) {
            return ChannelTypeUtils.toOnOffType(room.isReachable());
        }

        if (channelId.equalsIgnoreCase(CHANNEL_TEMPERATURE)) {
            return ChannelTypeUtils.toDecimalType(room.getThermMeasuredTemperature());
        }

        if (channelId.equalsIgnoreCase(CHANNEL_WINDOW_OPEN)) {
            return ChannelTypeUtils.toOnOffType(room.getOpenWindow());
        }

        return super.getNAThingProperty(channelId);
    }


    protected void requestParentRefresh() {
        setRefreshRequired(true);
        Optional<AbstractNetatmoThingHandler> parent = getBridgeHandler().findNAThing(getParentId());
        parent.ifPresent(AbstractNetatmoThingHandler::updateChannels);
    }


    protected boolean isRefreshRequired() {
        return refreshRequired;
    }

    protected void setRefreshRequired(boolean refreshRequired) {
        this.refreshRequired = refreshRequired;
    }

}
