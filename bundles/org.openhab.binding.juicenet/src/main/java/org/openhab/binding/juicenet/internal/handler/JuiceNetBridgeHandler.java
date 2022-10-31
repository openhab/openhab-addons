/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.juicenet.internal.JuiceNetBindingConstants.DEVICE_THING_TYPE;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.juicenet.internal.api.JuiceNetApi;
import org.openhab.binding.juicenet.internal.api.JuiceNetApiException;
import org.openhab.binding.juicenet.internal.api.dto.JuiceNetApiDevice;
import org.openhab.binding.juicenet.internal.config.JuiceNetBridgeConfiguration;
import org.openhab.binding.juicenet.internal.discovery.JuiceNetDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
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

    protected JuiceNetBridgeConfiguration config = new JuiceNetBridgeConfiguration();
    private final JuiceNetApi api;

    public JuiceNetApi getApi() {
        return api;
    }

    protected @Nullable ScheduledFuture<?> pollingJob;
    protected List<JuiceNetApiDevice> listDevices = Collections.<JuiceNetApiDevice> emptyList();
    protected @Nullable JuiceNetDiscoveryService discoveryService;

    public JuiceNetBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);

        this.api = new JuiceNetApi(httpClient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(JuiceNetBridgeConfiguration.class);

        logger.trace("JuiceNetBridgeHandler:initialize");

        api.initialize(config.apiToken, this.getThing().getUID());

        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollDevices, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
    }

    public void setDiscoveryService(JuiceNetDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
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
        } else if (e instanceof IOException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.toString());
        } else if (e instanceof InterruptedException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.toString());
            Thread.currentThread().interrupt();
        } else if (e instanceof TimeoutException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.toString());
        } else if (e instanceof ExecutionException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.toString());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE, e.toString());
        }
    }

    private void goOnline() {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            return;
        }

        scheduler.execute(() -> {
            try {
                listDevices = api.queryDeviceList();
            } catch (JuiceNetApiException | IOException | InterruptedException | TimeoutException
                    | ExecutionException e) {
                handleApiException(e);
                return;
            }

            for (JuiceNetApiDevice dev : listDevices) {
                discoveryService.notifyDiscoveryDevice(dev.unit_id, dev.name, dev.token);
            }

            updateStatus(ThingStatus.ONLINE);

            queryDevices();
        });
    }

    private void queryDevices() {
        List<Thing> things = getThing().getThings();

        for (Thing t : things) {
            if (!t.getThingTypeUID().equals(DEVICE_THING_TYPE)) {
                continue;
            }

            JuiceNetDeviceHandler handler = (JuiceNetDeviceHandler) t.getHandler();
            if (handler == null) {
                return;
            }

            handler.queryDeviceStatusAndInfo();
        }
    }

    private void pollDevices() {
        logger.debug("pollDevices");

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            goOnline();
            // queryDevices is called in goOnline after successfully going online
        } else {
            queryDevices();
        }
    }
}
