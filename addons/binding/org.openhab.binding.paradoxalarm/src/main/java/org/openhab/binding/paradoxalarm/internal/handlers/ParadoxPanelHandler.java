/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.handlers;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxBindingException;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxInformation;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxPanelHandler} This is the handler that takes care of the panel related stuff.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
@NonNullByDefault
public class ParadoxPanelHandler extends BaseThingHandler {

    private static final int UPDATE_THING_REFRESH_INTERVAL_SEC = 60;

    private static final int REINITIALIZE_INTERVAL_HRS = 6;

    private static final long INITIAL_DELAY = 0;

    private final Logger logger = LoggerFactory.getLogger(ParadoxPanelHandler.class);

    @Nullable
    ScheduledFuture<?> refreshMemoryMapSchedule;

    @Nullable
    ScheduledFuture<?> reinitializeParadoxPanelSchedule;

    @Nullable
    ScheduledFuture<?> updateThingSchedule;

    @Nullable
    private ParadoxPanelConfiguration config;

    public ParadoxPanelHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (ParadoxAlarmBindingConstants.PANEL_THING_TYPE_UID.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                refreshData();
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.UNKNOWN);
        try {
            initializeModel();
            setupSchedule();
            updateThing();
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error initializing panel handler. Exception: " + e);
        }
        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        HandlersUtil.cancelSchedule(refreshMemoryMapSchedule);
        HandlersUtil.cancelSchedule(reinitializeParadoxPanelSchedule);
        HandlersUtil.cancelSchedule(updateThingSchedule);
        super.dispose();
    }

    private void initializeModel() throws Exception, ParadoxBindingException {
        config = getConfigAs(ParadoxPanelConfiguration.class);
        ParadoxPanel panel = ParadoxPanel.getInstance();
    }

    private void setupSchedule() {
        scheduleRefreshRuntimeInfo();
        scheduleUpdateThing();
        // scheduleReinitialize();
    }

    // TODO think about reconecting procedure
    private void scheduleReinitialize() {
        reinitializeParadoxPanelSchedule = scheduler.scheduleWithFixedDelay(() -> {
            try {
                ParadoxPanel panel = ParadoxPanel.getInstance();
                panel.init();
            } catch (Exception e) {
                logger.error("Unable to retrieve memory map. {}", e);
            }
        }, INITIAL_DELAY, REINITIALIZE_INTERVAL_HRS, TimeUnit.HOURS);
    }

    private void scheduleRefreshRuntimeInfo() {
        logger.debug("Scheduling cache update. Refresh interval: " + config.getRefresh() + "s.");
        refreshMemoryMapSchedule = scheduler.scheduleWithFixedDelay(() -> {
            refreshData();
        }, INITIAL_DELAY, config.getRefresh(), TimeUnit.SECONDS);
    }

    private void scheduleUpdateThing() {
        logger.debug("Scheduling update thing. Refresh interval: " + config.getRefresh() + "s.");
        updateThingSchedule = scheduler.scheduleWithFixedDelay(() -> {
            try {
                updateThing();
            } catch (ParadoxBindingException e) {
                logger.error("Unable to update thing {} due to exception {}", getThing().getLabel(), e);
            }
        }, INITIAL_DELAY, UPDATE_THING_REFRESH_INTERVAL_SEC, TimeUnit.SECONDS);

    }

    private void refreshData() {
        try {
            ParadoxPanel.getInstance().updateEntitiesStates();
        } catch (Exception e) {
            logger.error("Unable to retrieve memory map. {}", e);
        }
    }

    private void updateThing() throws ParadoxBindingException {
        // TODO think how to identify connected/disconnected state
        updateState("state", new StringType("connected"));
        ParadoxInformation panelInformation = ParadoxPanel.getInstance().getPanelInformation();
        if (panelInformation != null) {
            updateState("serialNumber", new StringType(panelInformation.getSerialNumber()));
            updateState("panelType", new StringType(panelInformation.getPanelType().name()));
            updateState("hardwareVersion", new StringType(panelInformation.getHardwareVersion().toString()));
            updateState("applicationVersion", new StringType(panelInformation.getApplicationVersion().toString()));
            updateState("bootloaderVersion", new StringType(panelInformation.getBootLoaderVersion().toString()));
        }

    }
}
