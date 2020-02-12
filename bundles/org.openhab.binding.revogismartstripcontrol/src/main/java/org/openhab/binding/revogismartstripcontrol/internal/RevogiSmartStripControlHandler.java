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
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.revogismartstripcontrol.internal.api.Status;
import org.openhab.binding.revogismartstripcontrol.internal.api.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.revogismartstripcontrol.internal.RevogiSmartStripControlBindingConstants.PLUG_1_SWITCH;

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
    private @Nullable ScheduledFuture<?> pollingJob;

    private @Nullable RevogiSmartStripControlConfiguration config;

    public RevogiSmartStripControlHandler(Thing thing, StatusService statusService) {
        super(thing);
        this.statusService = statusService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (PLUG_1_SWITCH.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(RevogiSmartStripControlConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(this::updateStripInformation);
        Runnable runnable = RevogiSmartStripControlHandler.this::updateStripInformation;

        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(runnable, 0, 30, TimeUnit.SECONDS);
        }

        logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
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
        Status status = statusService.queryStatus(config.getSerialNumber());
        if (status.isOnline()) {
            updateStatus(ThingStatus.ONLINE);
            for (int i = 0; i < status.getSwitchValue().size(); i++) {
                int plugNumber = i + 1;
                updateState("plug" + plugNumber + "#switch", OnOffType.from(status.getSwitchValue().get(i).toString()));
                updateState("plug" + plugNumber + "#watt", new DecimalType(status.getWatt().get(i) / 1000f));
                updateState("plug" + plugNumber + "#amp", new DecimalType(status.getAmp().get(i) / 1000f));
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Retrieved status code: " + status.getResponseCode());
        }
    }
}
