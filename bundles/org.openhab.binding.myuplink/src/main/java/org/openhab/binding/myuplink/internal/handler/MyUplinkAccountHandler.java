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
package org.openhab.binding.myuplink.internal.handler;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.myuplink.internal.AtomicReferenceTrait;
import org.openhab.binding.myuplink.internal.command.MyUplinkCommand;
import org.openhab.binding.myuplink.internal.config.MyUplinkConfiguration;
import org.openhab.binding.myuplink.internal.connector.WebInterface;
import org.openhab.binding.myuplink.internal.discovery.MyUplinkDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link myUplinkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
public class MyUplinkAccountHandler extends BaseBridgeHandler implements MyUplinkBridgeHandler, AtomicReferenceTrait {

    private final Logger logger = LoggerFactory.getLogger(MyUplinkAccountHandler.class);

    /**
     * Schedule for polling live data
     */
    private final AtomicReference<@Nullable Future<?>> dataPollingJobReference;

    private @Nullable DiscoveryService discoveryService;

    /**
     * Interface object for querying the NIBE myUplink API.
     */
    private WebInterface webInterface;

    public MyUplinkAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.dataPollingJobReference = new AtomicReference<>(null);
        this.webInterface = new WebInterface(scheduler, this, httpClient, super::updateStatus);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if (CHANNEL_1.equals(channelUID.getId())) {
        // if (command instanceof RefreshType) {
        // // TODO: handle data refresh
        // }

        // // TODO: handle command

        // // Note: if communication with thing fails for some reason,
        // // indicate that by setting the status with detail information:
        // // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // // "Could not control device at IP address x.x.x.x");
        // }
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize myUplink Account");
        MyUplinkConfiguration config = getBridgeConfiguration();
        logger.debug("myUplink Account initialized with configuration: {}", config.toString());

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_LOGIN);
        webInterface.start();
        startPolling();

        enqueueCommand(new GetSite(this, this::updateProperties));
    }

    /**
     * Start the polling.
     */
    private void startPolling() {
        updateJobReference(dataPollingJobReference, scheduler.scheduleWithFixedDelay(this::pollingRun,
                POLLING_INITIAL_DELAY, getBridgeConfiguration().getDataPollingInterval(), TimeUnit.SECONDS));
    }

    /**
     * Poll the Easee Cloud API one time.
     */
    void pollingRun() {
        // String siteId = getConfig().get(THING_CONFIG_SITE_ID).toString();
        // logger.debug("polling site data for {}", siteId);

        // SiteState state = new SiteState(this, siteId, getChildChargerHandlers(), this::updateOnlineStatus);
        // enqueueCommand(state);

        // proceed if site is online
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            // add further polling commands here
        }
    }

    /**
     * Disposes the bridge.
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        cancelJobReference(dataPollingJobReference);
        // TODO: webInterface.dispose();
    }

    @Override
    public MyUplinkConfiguration getBridgeConfiguration() {
        return this.getConfigAs(MyUplinkConfiguration.class);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MyUplinkDiscoveryService.class);
    }

    public void setDiscoveryService(MyUplinkDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public void startDiscovery() {
        DiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.startScan(null);
        }
    }

    @Override
    public void enqueueCommand(MyUplinkCommand command) {
        // TODO: webInterface.enqueueCommand(command);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
