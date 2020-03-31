/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.revogismartstripcontrol.internal;

import static org.openhab.binding.revogismartstripcontrol.internal.RevogiSmartStripControlBindingConstants.ALL_PLUGS;
import static org.openhab.binding.revogismartstripcontrol.internal.RevogiSmartStripControlBindingConstants.PLUG_1_SWITCH;
import static org.openhab.binding.revogismartstripcontrol.internal.RevogiSmartStripControlBindingConstants.PLUG_2_SWITCH;
import static org.openhab.binding.revogismartstripcontrol.internal.RevogiSmartStripControlBindingConstants.PLUG_3_SWITCH;
import static org.openhab.binding.revogismartstripcontrol.internal.RevogiSmartStripControlBindingConstants.PLUG_4_SWITCH;
import static org.openhab.binding.revogismartstripcontrol.internal.RevogiSmartStripControlBindingConstants.PLUG_5_SWITCH;
import static org.openhab.binding.revogismartstripcontrol.internal.RevogiSmartStripControlBindingConstants.PLUG_6_SWITCH;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.revogismartstripcontrol.internal.api.Status;
import org.openhab.binding.revogismartstripcontrol.internal.api.StatusService;
import org.openhab.binding.revogismartstripcontrol.internal.api.SwitchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RevogiSmartStripControlHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class RevogiSmartStripControlHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RevogiSmartStripControlHandler.class);
    private final StatusService statusService;
    private final SwitchService switchService;
    private @Nullable ScheduledFuture<?> pollingJob;

    private @Nullable RevogiSmartStripControlConfiguration config;

    public RevogiSmartStripControlHandler(Thing thing, StatusService statusService, SwitchService switchService) {
        super(thing);
        this.statusService = statusService;
        this.switchService = switchService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case PLUG_1_SWITCH:
                switchPlug(command, 1);
                break;
            case PLUG_2_SWITCH:
                switchPlug(command, 2);
                break;
            case PLUG_3_SWITCH:
                switchPlug(command, 3);
                break;
            case PLUG_4_SWITCH:
                switchPlug(command, 4);
                break;
            case PLUG_5_SWITCH:
                switchPlug(command, 5);
                break;
            case PLUG_6_SWITCH:
                switchPlug(command, 6);
                break;
            case ALL_PLUGS:
                switchPlug(command, 0);
                break;
        }
    }

    private void switchPlug(Command command, int port) {
        if (config == null) {
            logger.warn("No config available, config object was null");
            return;
        }
        if (command instanceof OnOffType) {
            int state = convertOnOffTypeToState(command);
            switchService.switchPort(config.getSerialNumber(), config.ipAddress, port, state);
        }
    }

    private int convertOnOffTypeToState(Command command) {
        if (command == OnOffType.ON) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(RevogiSmartStripControlConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(this::updateStripInformation);
        Runnable runnable = RevogiSmartStripControlHandler.this::updateStripInformation;

        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(runnable, 0, config.getPollInterval(), TimeUnit.SECONDS);
        }

        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        super.dispose();
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private void updateStripInformation() {
        if (config == null) {
            logger.warn("No config available, config object was null");
            return;
        }
        Status status = statusService.queryStatus(config.getSerialNumber(), config.getIpAddress());
        if (status.isOnline()) {
            updateStatus(ThingStatus.ONLINE);
            handleAllPlugsInformation(status);
            handleSinglePlugInformation(status);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                    "Retrieved status code: " + status.getResponseCode());
        }
    }

    private void handleSinglePlugInformation(Status status) {
        for (int i = 0; i < status.getSwitchValue().size(); i++) {
            int plugNumber = i + 1;
            updateState("plug" + plugNumber + "#switch", OnOffType.from(status.getSwitchValue().get(i).toString()));
            updateState("plug" + plugNumber + "#watt", new DecimalType(status.getWatt().get(i) / 1000f));
            updateState("plug" + plugNumber + "#amp", new DecimalType(status.getAmp().get(i) / 1000f));
        }
    }

    private void handleAllPlugsInformation(Status status) {
        long onCount = status.getSwitchValue().stream().filter(statusValue -> statusValue == 1).count();
        if (onCount == 6) {
            updateState(ALL_PLUGS, OnOffType.ON);
        } else {
            updateState(ALL_PLUGS, OnOffType.OFF);
        }
    }
}
