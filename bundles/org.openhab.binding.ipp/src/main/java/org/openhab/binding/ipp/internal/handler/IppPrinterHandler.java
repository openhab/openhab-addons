/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ipp.internal.handler;

import static org.openhab.binding.ipp.internal.IppBindingConstants.*;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.cups4j.CupsPrinter;
import org.cups4j.WhichJobsEnum;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.DiscoveryServiceRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IppPrinterHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Tobias Braeutigam - Initial contribution
 */
@NonNullByDefault
public class IppPrinterHandler extends BaseThingHandler implements DiscoveryListener {

    /**
     * Refresh interval defaults to every minute (60s)
     */
    private static final int DEFAULT_REFRESH_INTERVAL_IN_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(IppPrinterHandler.class);

    private @Nullable URL url;
    private @Nullable CupsPrinter printer;

    private @Nullable ScheduledFuture<?> refreshJob;

    private final DiscoveryServiceRegistry discoveryServiceRegistry;

    public IppPrinterHandler(Thing thing, DiscoveryServiceRegistry discoveryServiceRegistry) {
        super(thing);
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        String name = (String) config.get(PRINTER_PARAMETER_NAME);
        try {
            Object obj = config.get(PRINTER_PARAMETER_URL);
            if (obj instanceof URL) {
                url = (URL) obj;
            } else if (obj instanceof String) {
                url = new URL((String) obj);
            }
            printer = new CupsPrinter(null, url, name);
        } catch (MalformedURLException e) {
            logger.error("malformed url {}, printer thing creation failed", config.get(PRINTER_PARAMETER_URL));
        }

        int refresh = DEFAULT_REFRESH_INTERVAL_IN_SECONDS;
        Object obj = config.get(PRINTER_PARAMETER_REFRESH_INTERVAL);
        if (obj != null) {
            BigDecimal ref = (BigDecimal) obj;
            refresh = ref.intValue();
        }

        updateStatus(ThingStatus.UNKNOWN);
        deviceOnlineWatchdog(refresh);
        discoveryServiceRegistry.addDiscoveryListener(this);
    }

    @Override
    public void dispose() {
        stopRefreshJob();
        logger.debug("IppPrinterHandler {} disposed.", url);
        super.dispose();
    }

    private void deviceOnlineWatchdog(int refresh) {
        stopRefreshJob();
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                onDeviceStateChanged(printer);
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            }
        }, 0, refresh, TimeUnit.SECONDS);
    }

    private void stopRefreshJob() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null && !localRefreshJob.isCancelled()) {
            localRefreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            onDeviceStateChanged(printer);
            return;
        }
    }

    public void onDeviceStateChanged(@Nullable CupsPrinter device) {
        if (device != null && device.getPrinterURL().equals(url)) {
            boolean online = false;
            try {
                updateState(JOBS_CHANNEL, new DecimalType(device.getJobs(WhichJobsEnum.ALL, "", false).size()));
                online = true;
            } catch (Exception e) {
                logger.debug("error updating jobs channel, reason: {}", e.getMessage());
            }
            try {
                updateState(WAITING_JOBS_CHANNEL,
                        new DecimalType(device.getJobs(WhichJobsEnum.NOT_COMPLETED, "", false).size()));
                online = true;
            } catch (Exception e) {
                logger.debug("error updating waiting-jobs channel, reason: {}", e.getMessage());
            }
            try {
                updateState(DONE_JOBS_CHANNEL,
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
        if (result.getThingUID().equals(getThing().getUID())) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        if (thingUID.equals(getThing().getUID())) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            @Nullable Collection<ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
        return Set.of();
    }
}
