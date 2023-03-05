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
package org.openhab.binding.juicenet.internal.handler;

import static org.openhab.binding.juicenet.internal.JuiceNetBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.juicenet.internal.api.JuiceNetApi;
import org.openhab.binding.juicenet.internal.api.JuiceNetApiException;
import org.openhab.binding.juicenet.internal.api.dto.JuiceNetApiDevice;
import org.openhab.binding.juicenet.internal.config.JuiceNetBridgeConfiguration;
import org.openhab.binding.juicenet.internal.discovery.JuiceNetDiscoveryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JuiceNetBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(JuiceNetBridgeHandler.class);

    private JuiceNetBridgeConfiguration config = new JuiceNetBridgeConfiguration();
    private final JuiceNetApi api;

    public JuiceNetApi getApi() {
        return api;
    }

    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable JuiceNetDiscoveryService discoveryService;

    public JuiceNetBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);

        this.api = new JuiceNetApi(httpClient, getThing().getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(JuiceNetBridgeConfiguration.class);

        logger.trace("Bridge initialized: {}", Objects.requireNonNull(getThing()).getUID());

        api.setApiToken(config.apiToken);

        updateStatus(ThingStatus.UNKNOWN);
        // Bridge will go online after the first successful API call in iterateApiDevices. iterateApiDevices will be
        // called when a child device attempts to goOnline and needs to retrieve the api token

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollDevices, 10, config.refreshInterval, TimeUnit.SECONDS);

        // Call here in order to discover any devices.
        iterateApiDevices();
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
    }

    public void setDiscoveryService(JuiceNetDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        // Call here to set the Api Token for any newly initialized Child devices
        iterateApiDevices();
    }

    /**
     * Get the services registered for this bridge. Provides the discovery service.
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(JuiceNetDiscoveryService.class);
    }

    public void handleApiException(Exception e) {
        if (e instanceof JuiceNetApiException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.toString());
        } else if (e instanceof InterruptedException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.toString());
            Thread.currentThread().interrupt();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE, e.toString());
        }
    }

    @Nullable
    public Thing getThingById(String id) {
        List<Thing> childThings = getThing().getThings();

        for (Thing childThing : childThings) {
            Configuration configuration = childThing.getConfiguration();

            String childId = configuration.get(PARAMETER_UNIT_ID).toString();

            if (childId.equals(id)) {
                return childThing;
            }
        }

        return null;
    }

    // This function will query the list of devices from the API and then set the name/token in the child handlers. If a
    // child does not exist, it will notify the Discovery service. If it is successful, it will ensure the bridge status
    // is updated
    // to ONLINE.
    public void iterateApiDevices() {
        List<JuiceNetApiDevice> listDevices;

        try {
            listDevices = api.queryDeviceList();
        } catch (JuiceNetApiException | InterruptedException e) {
            handleApiException(e);
            return;
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        JuiceNetDiscoveryService discoveryService = this.discoveryService;
        for (JuiceNetApiDevice dev : listDevices) {
            Thing childThing = getThingById(dev.unitId);
            if (childThing == null) {
                if (discoveryService != null) {
                    discoveryService.notifyDiscoveryDevice(dev.unitId, dev.name);
                }
            } else {
                JuiceNetDeviceHandler childHandler = (JuiceNetDeviceHandler) childThing.getHandler();
                if (childHandler != null) {
                    childHandler.setNameAndToken(dev.name, dev.token);
                }
            }
        }
    }

    private void pollDevices() {
        List<Thing> things = getThing().getThings();

        for (Thing t : things) {
            if (!t.getThingTypeUID().equals(DEVICE_THING_TYPE)) {
                continue;
            }

            JuiceNetDeviceHandler handler = (JuiceNetDeviceHandler) t.getHandler();
            if (handler == null) {
                logger.trace("no handler for thing: {}", t.getUID());
                continue;
            }

            handler.queryDeviceStatusAndInfo();
        }
    }
}
