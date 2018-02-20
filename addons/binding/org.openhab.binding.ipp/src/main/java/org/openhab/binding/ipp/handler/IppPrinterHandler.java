/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ipp.handler;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.cups4j.CupsPrinter;
import org.cups4j.WhichJobsEnum;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.ipp.IppBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IppPrinterHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Tobias Braeutigam - Initial contribution
 */
public class IppPrinterHandler extends BaseThingHandler implements DiscoveryListener {

    private Logger logger = LoggerFactory.getLogger(IppPrinterHandler.class);

    private URL url;
    private String name;
    private CupsPrinter printer;

    private int refresh = 60; // refresh every minute as default
    ScheduledFuture<?> refreshJob;

    private DiscoveryServiceRegistry discoveryServiceRegistry;

    public IppPrinterHandler(Thing thing, DiscoveryServiceRegistry discoveryServiceRegistry) {
        super(thing);
        if (discoveryServiceRegistry != null) {
            this.discoveryServiceRegistry = discoveryServiceRegistry;
        }
    }

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        try {
            Object obj = config.get(IppBindingConstants.PRINTER_PARAMETER_URL);
            name = (String) config.get(IppBindingConstants.PRINTER_PARAMETER_NAME);
            if (config.get(IppBindingConstants.PRINTER_PARAMETER_REFRESH_INTERVAL) != null) {
                BigDecimal ref = (BigDecimal) config.get(IppBindingConstants.PRINTER_PARAMETER_REFRESH_INTERVAL);
                refresh = ref.intValue();
            }
            if (obj instanceof URL) {
                url = (URL) obj;
            } else if (obj instanceof String) {
                url = new URL((String) obj);
            }
            printer = new CupsPrinter(url, name, false);
        } catch (MalformedURLException e) {
            logger.error("malformed url {}, printer thing creation failed",
                    config.get(IppBindingConstants.PRINTER_PARAMETER_URL));
        }
        // until we get an update put the Thing offline
        updateStatus(ThingStatus.OFFLINE);
        deviceOnlineWatchdog();
        if (this.discoveryServiceRegistry != null) {
            this.discoveryServiceRegistry.addDiscoveryListener(this);
        }
    }

    @Override
    public void dispose() {
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
        logger.debug("IppPrinterHandler {} disposed.", url);
        super.dispose();
    }

    private void deviceOnlineWatchdog() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    onDeviceStateChanged(printer);
                } catch (Exception e) {
                    logger.debug("Exception occurred during execution: {}", e.getMessage(), e);

                }
            }
        };
        refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, refresh, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            onDeviceStateChanged(printer);
            return;
        }
    }

    public void onDeviceStateChanged(CupsPrinter device) {
        if (device.getPrinterURL().equals(url)) {
            boolean online = false;
            try {
                updateState(new ChannelUID(getThing().getUID(), IppBindingConstants.JOBS_CHANNEL),
                        new DecimalType(device.getJobs(WhichJobsEnum.ALL, "", false).size()));
                online = true;
            } catch (Exception e) {
                logger.debug("error updating jobs channel, reason: {}", e.getMessage());
            }
            try {
                updateState(new ChannelUID(getThing().getUID(), IppBindingConstants.WAITING_JOBS_CHANNEL),
                        new DecimalType(device.getJobs(WhichJobsEnum.NOT_COMPLETED, "", false).size()));
                online = true;
            } catch (Exception e) {
                logger.debug("error updating waiting-jobs channel, reason: {}", e.getMessage());
            }
            try {
                updateState(new ChannelUID(getThing().getUID(), IppBindingConstants.DONE_JOBS_CHANNEL),
                        new DecimalType(device.getJobs(WhichJobsEnum.COMPLETED, "", false).size()));
                online = true;
            } catch (Exception e) {
                logger.debug("error updating done-jobs channel, reason: {}", e.getMessage());
            }
            if (online) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        if (result.getThingUID().equals(this.getThing().getUID())) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        if (thingUID.equals(this.getThing().getUID())) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs, ThingUID bridgeUID) {
        return Collections.emptyList();
    }

}
