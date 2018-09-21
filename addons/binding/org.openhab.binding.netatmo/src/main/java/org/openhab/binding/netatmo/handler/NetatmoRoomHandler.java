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
import io.rudolph.netatmo.api.energy.model.*;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.quantity.Temperature;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

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

        EnergyConnector api = getBridgeHandler().api
                .getEnergyApi();

        HomesDataBody body = api.getHomesData(getParentId())
                .executeSync();

        if (body == null
                || body.getHomes().get(0) == null
                || body.getHomes().get(0).getRooms().size() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "home not found");
            return;
        }

        BaseRoom baseRoom = null;
        List<BaseRoom> rooms = body.getHomes()
                .get(0)
                .getRooms();
        for (BaseRoom base : rooms) {
            if (getId().equals(base.getId())) {
                baseRoom = base;
                break;
            }
        }

        if (baseRoom == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "room status not found");
            return;
        }

        HomeStatusBody statusBody = api.getHomeStatus(getParentId())
                .executeSync();

        if (statusBody == null
                || statusBody.getHomes().get(0) == null
                || statusBody.getHomes().get(0).getRooms().size() == 0) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "room status not found");
            return;
        }

        Room room = null;
        for (Room base : statusBody.getHomes()
                .get(0)
                .getRooms()) {
            if (getId().equals(base.getId())) {
                room = base;
                break;
            }
        }

        if (room == null) {
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        this.room = room;

        super.updateChannels();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateChannels();
            return;
        }
        if (command instanceof RefreshType) {
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_ROOM_SETPOINT_TEMPERATURE: {
                BigDecimal spTemp = null;
                if (command instanceof QuantityType) {
                    QuantityType<Temperature> quantity = ((QuantityType<Temperature>) command)
                            .toUnit(API_TEMPERATURE_UNIT);
                    if (quantity != null) {
                        spTemp = quantity.toBigDecimal().setScale(1, RoundingMode.HALF_UP);
                    }
                } else {
                    spTemp = new BigDecimal(command.toString()).setScale(1, RoundingMode.HALF_UP);
                }
                if (spTemp != null) {
                    pushSetpointUpdate(ThermPointMode.MANUAL, LocalDateTime.now().plusHours(2), spTemp.floatValue());

                }
            }
        }

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
            return ChannelTypeUtils.toQuantityType(room.getThermMeasuredTemperature(), API_TEMPERATURE_UNIT);
        }

        if (channelId.equalsIgnoreCase(CHANNEL_ROOM_WINDOW_OPEN)) {
            return ChannelTypeUtils.toOnOffType(room.getOpenWindow());
        }

        if (channelId.equalsIgnoreCase(CHANNEL_ROOM_HEATING_POWER_REQUEST)) {
            return ChannelTypeUtils.toQuantityType(room.getHeatingPowerRequest(), API_TEMPERATURE_UNIT);
        }

        if (channelId.equalsIgnoreCase(CHANNEL_ROOM_SETPOINT_TEMPERATURE)) {
            return ChannelTypeUtils.toQuantityType(room.getThermSetpointTemperature(), API_TEMPERATURE_UNIT);
        }

        if (channelId.equalsIgnoreCase(CHANNEL_ROOM_SETPOINT_MODE)) {
            return ChannelTypeUtils.toStringType(room.getThermSetpointMode().getValue());
        }

        if (channelId.equalsIgnoreCase(CHANNEL_ROOM_SETPOINT_START_TIME)) {
            return ChannelTypeUtils.toDateTimeType(room.getThermSetpointStartTime());
        }

        if (channelId.equalsIgnoreCase(CHANNEL_ROOM_SETPOINT_END_TIME)) {
            return ChannelTypeUtils.toDateTimeType(room.getThermSetpointEndTime());
        }


        Optional<State> result = measurableChannels.getNAThingProperty(channelId);

        return result.orElse(UnDefType.UNDEF);
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

    private void pushSetpointUpdate(ThermPointMode thermMode, LocalDateTime setpointEndtime, Float setpointTemp) {
        getBridgeHandler().api.getEnergyApi().setRoomThermPoint(getParentId(), getId(), thermMode, setpointTemp, setpointEndtime);
    }


}
