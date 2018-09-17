/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import io.rudolph.netatmo.api.energy.EnergyConnector;
import io.rudolph.netatmo.api.energy.model.Home;
import io.rudolph.netatmo.api.energy.model.HomesDataBody;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

/**
 * {@link NetatmoHomeHandler} is the handler for a given
 * room accessed through a Netatmo Device
 *
 * @author Michael Rudolph - Initial contribution OH2 version
 */
public class NetatmoHomeHandler extends AbstractNetatmoThingHandler {
    private Logger logger = LoggerFactory.getLogger(NetatmoHomeHandler.class);
    private ScheduledFuture<?> refreshJob;
    @Nullable
    protected Home home;
    private boolean refreshRequired;

    public NetatmoHomeHandler(@NonNull Thing thing) {
        super(thing);
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

        if (getId() == null) {
            updateStatus(ThingStatus.OFFLINE);
        }

        EnergyConnector api = getBridgeHandler().api
                .getEnergyApi();

        HomesDataBody body = api.getHomesData(getId())
                .executeSync();

        if (body == null
                || body.getHomes().get(0) == null) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        this.home = body.getHomes().get(0);
        super.updateChannels();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateChannels();
            return;
        }
        switch (channelUID.getId()) {

        }
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        logger.error("channelId: {}", channelId);
        if (home == null) {
            return super.getNAThingProperty(channelId);
        }
        if (channelId.equalsIgnoreCase(CHANNEL_THERM_MODE)) {
            logger.error("TherMode {}", home.getThermMode());
            if (home.getThermMode() == null) {
                return ChannelTypeUtils.toStringType(null);
            }
            return ChannelTypeUtils.toStringType(home.getThermMode().getValue());
        }

        if (channelId.equalsIgnoreCase(CHANNEL_THERM_SETPOINT_DURATION)) {
            return ChannelTypeUtils.toQuantityType(home.getThermSetpointDefaultDuration(), API_TIME_UNIT_SECOND);
        }

        if (channelId.equalsIgnoreCase(CHANNEL_ROOM_COUNT)) {
            return ChannelTypeUtils.toQuantityType(home.getRooms().size(), null);
        }

        if (channelId.equalsIgnoreCase(CHANNEL_ACTIVE_SCHEDULE)) {
            return ChannelTypeUtils.toStringType(home.getActiveSchedule().getId());
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
