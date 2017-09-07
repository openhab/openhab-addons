/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.handler;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nibeuplink.UplinkDataChannels;
import org.openhab.binding.nibeuplink.config.NibeUplinkConfiguration;
import org.openhab.binding.nibeuplink.internal.connector.UplinkWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UplinkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author afriese - Initial contribution
 */
public class UplinkHandler extends BaseThingHandler implements NibeUplinkHandler {

    private final Logger logger = LoggerFactory.getLogger(UplinkHandler.class);

    /**
     * Refresh interval which is used to poll values from the FRITZ!Box web interface (optional, defaults to 60 s)
     */
    private int refreshInterval;

    /**
     * Interface object for querying the FRITZ!Box web interface
     */
    private UplinkWebInterface webInterface;

    /**
     * Job which will do the FRITZ!Box polling
     */
    private final UplinkPolling pollingRunnable;

    /**
     * Schedule for polling
     */
    private ScheduledFuture<?> pollingJob;

    public UplinkHandler(@NonNull Thing thing) {
        super(thing);
        this.pollingRunnable = new UplinkPolling(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("command for {}: {}", channelUID, command);

        // TODO: necessary??? will be read only in the first place
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize NibeUplink");
        Thing thing = this.getThing();
        NibeUplinkConfiguration config = getConfiguration();

        logger.debug("Discovered NibeUplink initialized: {}", config);

        this.refreshInterval = config.getPollingInterval();
        this.webInterface = new UplinkWebInterface(config, this);

        if (config.getPassword() != null) {
            this.onUpdate();
        } else {
            thing.setStatusInfo(
                    new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no password set"));
        }
    }

    /**
     * Start the polling.
     */
    private synchronized void onUpdate() {
        // TODO: implement onUpdate
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.debug("start polling job at intervall {}", refreshInterval);
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 1, refreshInterval, TimeUnit.SECONDS);
        } else {
            logger.debug("pollingJob active");
        }
    }

    /**
     * Disposes the bridge.
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        if (pollingJob != null && !pollingJob.isCancelled()) {
            logger.debug("stop polling job");
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public UplinkWebInterface getWebInterface() {
        return webInterface;
    }

    @Override
    public void updateChannelStatus(Map<String, String> values) {
        logger.debug("Handling channel update.");
        for (String key : values.keySet()) {
            UplinkDataChannels channel = UplinkDataChannels.fromId(key);
            if (channel != null) {
                logger.debug("Channel is to be updated: {}", channel.toString());
                if (channel.getType().equals(Double.class)) {
                    updateState(channel.toString(),
                            new DecimalType(values.get(key).replaceAll(",", ".").replaceAll("[^0-9.]", "")));
                } else {
                    updateState(channel.toString(), new StringType(values.get(key)));
                }
            }
        }
    }

    @Override
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public NibeUplinkConfiguration getConfiguration() {
        return this.getConfigAs(NibeUplinkConfiguration.class);
    }

}
