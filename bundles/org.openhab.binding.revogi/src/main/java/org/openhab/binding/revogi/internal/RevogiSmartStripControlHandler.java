/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.revogi.internal;

import static org.openhab.core.library.unit.MetricPrefix.MILLI;
import static org.openhab.core.library.unit.Units.AMPERE;
import static org.openhab.core.library.unit.Units.WATT;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.revogi.internal.api.StatusDTO;
import org.openhab.binding.revogi.internal.api.StatusService;
import org.openhab.binding.revogi.internal.api.SwitchService;
import org.openhab.binding.revogi.internal.udp.DatagramSocketWrapper;
import org.openhab.binding.revogi.internal.udp.UdpSenderService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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

    private RevogiSmartStripControlConfiguration config;

    public RevogiSmartStripControlHandler(Thing thing) {
        super(thing);
        config = getConfigAs(RevogiSmartStripControlConfiguration.class);
        UdpSenderService udpSenderService = new UdpSenderService(new DatagramSocketWrapper(), scheduler);
        this.statusService = new StatusService(udpSenderService);
        this.switchService = new SwitchService(udpSenderService);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case RevogiSmartStripControlBindingConstants.PLUG_1_SWITCH:
                switchPlug(command, 1);
                break;
            case RevogiSmartStripControlBindingConstants.PLUG_2_SWITCH:
                switchPlug(command, 2);
                break;
            case RevogiSmartStripControlBindingConstants.PLUG_3_SWITCH:
                switchPlug(command, 3);
                break;
            case RevogiSmartStripControlBindingConstants.PLUG_4_SWITCH:
                switchPlug(command, 4);
                break;
            case RevogiSmartStripControlBindingConstants.PLUG_5_SWITCH:
                switchPlug(command, 5);
                break;
            case RevogiSmartStripControlBindingConstants.PLUG_6_SWITCH:
                switchPlug(command, 6);
                break;
            case RevogiSmartStripControlBindingConstants.ALL_PLUGS:
                switchPlug(command, 0);
                break;
            default:
                logger.debug("Something went wrong, we've got a message for {}", channelUID.getId());
        }
    }

    private void switchPlug(Command command, int port) {
        RevogiSmartStripControlConfiguration localConfig = this.config;
        if (command instanceof OnOffType) {
            int state = convertOnOffTypeToState(command);
            switchService.switchPort(localConfig.serialNumber, localConfig.ipAddress, port, state);
        }
        if (command instanceof RefreshType) {
            updateStripInformation();
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

        pollingJob = scheduler.scheduleWithFixedDelay(this::updateStripInformation, 0, config.pollInterval,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        super.dispose();
        ScheduledFuture<?> localPollingJob = this.pollingJob;
        if (localPollingJob != null) {
            localPollingJob.cancel(true);
            this.pollingJob = null;
        }
    }

    private void updateStripInformation() {
        CompletableFuture<StatusDTO> futureStatus = statusService.queryStatus(config.serialNumber, config.ipAddress);
        futureStatus.thenAccept(this::updatePlugStatus);
    }

    private void updatePlugStatus(StatusDTO status) {
        if (status.isOnline()) {
            updateStatus(ThingStatus.ONLINE);
            handleAllPlugsInformation(status);
            handleSinglePlugInformation(status);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                    "Retrieved status code: " + status.getResponseCode());
        }
    }

    private void handleSinglePlugInformation(StatusDTO status) {
        for (int i = 0; i < status.getSwitchValue().size(); i++) {
            int plugNumber = i + 1;
            updateState("plug" + plugNumber + "#switch", OnOffType.from(status.getSwitchValue().get(i).toString()));
            updateState("plug" + plugNumber + "#watt", new QuantityType<>(status.getWatt().get(i), MILLI(WATT)));
            updateState("plug" + plugNumber + "#amp", new QuantityType<>(status.getAmp().get(i), MILLI(AMPERE)));
        }
    }

    private void handleAllPlugsInformation(StatusDTO status) {
        long onCount = status.getSwitchValue().stream().filter(statusValue -> statusValue == 1).count();
        if (onCount == 6) {
            updateState(RevogiSmartStripControlBindingConstants.ALL_PLUGS, OnOffType.ON);
        } else {
            updateState(RevogiSmartStripControlBindingConstants.ALL_PLUGS, OnOffType.OFF);
        }
    }
}
