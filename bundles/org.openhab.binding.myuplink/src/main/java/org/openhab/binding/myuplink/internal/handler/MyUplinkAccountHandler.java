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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.myuplink.internal.AtomicReferenceTrait;
import org.openhab.binding.myuplink.internal.command.MyUplinkCommand;
import org.openhab.binding.myuplink.internal.config.MyUplinkConfiguration;
import org.openhab.binding.myuplink.internal.discovery.MyUplinkDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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
     * Interface object for querying the Easee web interface
     */
    // TODO: private WebInterface webInterface;

    public MyUplinkAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.dataPollingJobReference = new AtomicReference<>(null);
        // TODO: this.webInterface = new WebInterface(scheduler, this, httpClient, super::updateStatus);
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
        // TODO: ??? config = getConfigAs(MyUplinkConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
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
