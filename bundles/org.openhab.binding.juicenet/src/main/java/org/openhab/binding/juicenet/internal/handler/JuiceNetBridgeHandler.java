/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.juicenet.internal.api.JuiceNetApi;
import org.openhab.binding.juicenet.internal.api.JuiceNetApiException;
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
    private final JuiceNetApi api = new JuiceNetApi();

    public JuiceNetApi getApi() {
        return api;
    }

    protected @Nullable ScheduledFuture<?> pollingJob;
    protected List<JuiceNetApi.JuiceNetApiDevice> listDevices = Collections.<JuiceNetApi.JuiceNetApiDevice> emptyList();
    protected @Nullable JuiceNetDiscoveryService discoveryService;

    public JuiceNetBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(JuiceNetBridgeConfiguration.class);

        logger.trace("JuiceNetBridgeHandler:initialize");

        try {
            api.initialize(config.api_token, this.getThing().getUID());
        } catch (JuiceNetApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.toString());
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {

                listDevices = api.queryDeviceList();
            } catch (JuiceNetApiException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.toString());
                return;
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.toString());
                return;
            } catch (InterruptedException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.toString());
                return;
            }

            for (JuiceNetApi.JuiceNetApiDevice dev : listDevices) {
                discoveryService.notifyDiscoveryDevice(dev.unit_id, dev.name, dev.token);
            }

            pollingJob = scheduler.scheduleWithFixedDelay(this::pollDevices, 60, config.refreshInterval,
                    TimeUnit.SECONDS);

            logger.trace("Bridge is ONLINE");
            updateStatus(ThingStatus.ONLINE);
        });
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

    private void pollDevices() {
        logger.debug("pollDevices");

        List<Thing> things = getThing().getThings();

        for (Thing t : things) {
            if (!t.getThingTypeUID().equals(DEVICE_THING_TYPE)) {
                continue;
            }

            JuiceNetDeviceHandler handler = (JuiceNetDeviceHandler) t.getHandler();
            if (handler == null) {
                return;
            }

            try {
                handler.queryDeviceStatusAndInfo();
            } catch (IOException e) {
                logger.debug("Unable to open connection to api host: {}", e.getMessage());
            } catch (InterruptedException e) {
                logger.debug("Unable to open connection to api host: {}", e.getMessage());
            } catch (JuiceNetApiException e) {
                logger.debug("Malformed JuiceNet API error: {}", e.getMessage());
            }
        }
    }
}
