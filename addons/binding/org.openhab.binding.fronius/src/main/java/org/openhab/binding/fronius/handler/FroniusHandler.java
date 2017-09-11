/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.handler;

import static org.openhab.binding.fronius.FroniusBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.fronius.internal.FroniusHandlerConfiguration;
import org.openhab.binding.fronius.internal.configuration.ServiceConfiguration;
import org.openhab.binding.fronius.internal.configuration.ServiceConfigurationFactory;
import org.openhab.binding.fronius.internal.model.InverterRealtimeData;
import org.openhab.binding.fronius.internal.service.ActiveDeviceInfoService;
import org.openhab.binding.fronius.internal.service.InterverRealtimeDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FroniusHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gerrit Beine - Initial contribution
 */
public class FroniusHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusHandler.class);

    protected FroniusHandlerConfiguration handlerConfiguration;
    protected ActiveDeviceInfoService activeDeviceInfoService;
    protected InterverRealtimeDataService interverRealtimeDataService;

    private ScheduledFuture<?> refreshJob;

    public FroniusHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
    }

    @Override
    public void initialize() {
        handlerConfiguration = getConfigAs(FroniusHandlerConfiguration.class);
        logger.debug("Initializing Fronius: {}", handlerConfiguration.hostname);
        ServiceConfiguration configuration = new ServiceConfigurationFactory()
                .createConnectionConfiguration(handlerConfiguration);
        activeDeviceInfoService = new ActiveDeviceInfoService(configuration);
    }

    protected void startAutomaticRefresh(final Runnable runnable) {
        refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, handlerConfiguration.refreshInterval.longValue(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * Fetch and update data.
     */
    protected void refresh() {
        refreshRealtimeData();
    }

    /**
     * Fetch and update data for {@link InterverRealtimeDataService}.
     */
    private void refreshRealtimeData() {
        if (interverRealtimeDataService != null) {
            InverterRealtimeData data = interverRealtimeDataService.getData();
            if (data.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } else {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_DAY_ENERGY), data.getDayEnergy());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_YEAR_ENERGY), data.getYearEnergy());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_TOTAL_ENERGY), data.getTotalEnergy());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PAC), data.getPac());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_IAC), data.getIac());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_UAC), data.getUac());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_FAC), data.getFac());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_IDC), data.getIdc());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_UDC), data.getUdc());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_STATUS_CODE), data.getCode());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_TIMESTAMP), data.getTimestamp());
            }
        }
    }
}
