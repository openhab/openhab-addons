/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.chamberlainmyq.internal.discovery;

import static org.openhab.binding.chamberlainmyq.ChamberlainMyQBindingConstants.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.chamberlainmyq.config.ChamberlainMyQDeviceConfig;
import org.openhab.binding.chamberlainmyq.handler.ChamberlainMyQGatewayHandler;
import org.openhab.binding.chamberlainmyq.handler.ChamberlainMyQGatewayHandler.RequestCallback;
import org.openhab.binding.chamberlainmyq.internal.ChamberlainMyQHandlerFactory;
import org.openhab.binding.chamberlainmyq.internal.json.Device;
import org.openhab.binding.chamberlainmyq.internal.json.MyqJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Send DiscoveryService Callbacks
 *
 * @author Scott Hanson - Initial contribution
 */

public class ChamberlainMyQDeviceDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(ChamberlainMyQDeviceDiscoveryService.class);
    private ChamberlainMyQGatewayHandler hubHandler;

    public ChamberlainMyQDeviceDiscoveryService(ChamberlainMyQGatewayHandler hubHandler)
            throws IllegalArgumentException {
        super(ChamberlainMyQHandlerFactory.DISCOVERABLE_DEVICE_TYPES_UIDS, 10);

        this.hubHandler = hubHandler;
    }

    private ScheduledFuture<?> scanTask;

    @Override
    protected void startScan() {
        if (this.scanTask == null || this.scanTask.isDone()) {
            this.scanTask = scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        readDeviceDatabase();
                    } catch (Exception e) {
                        logger.error("Error scanning for devices", e);

                        if (scanListener != null) {
                            scanListener.onErrorOccurred(e);
                        }
                    }
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

    protected void addMyQDevice(ThingTypeUID thinkType, ChamberlainMyQDeviceConfig config) {
        logger.debug("New Device {}", config.toString());

        ThingUID hubUID = this.hubHandler.getThing().getUID();
        ThingUID uid = new ThingUID(thinkType, hubUID, config.getDeviceIdString());

        Map<String, Object> properties = config.getProperties();
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(config.getDescription())
                .withProperties(properties).withBridge(hubUID).build();

        thingDiscovered(result);

        logger.debug("Discovered {}", uid);
    }

    protected void enumerateDevices(MyqJson gatewayResponse) {
        logger.debug("Chamberlain MyQ Devices:");

        for (Device myqdevice : gatewayResponse.getDevices()) {
            ChamberlainMyQDeviceConfig config = new ChamberlainMyQDeviceConfig(myqdevice);
            int iDeviceTypeID = myqdevice.getMyQDeviceTypeId();

            logger.debug("New Device {}", config.getDeviceTypeId());
            if (iDeviceTypeID == 2 || iDeviceTypeID == 5 || iDeviceTypeID == 7 || iDeviceTypeID == 17) {
                addMyQDevice(THING_TYPE_DOOR_OPENER, config);
            } else if (iDeviceTypeID == 3) {
                addMyQDevice(THING_TYPE_LIGHT, config);
            }
        }

    }

    private class ListDevicesCallback implements RequestCallback {
        private ChamberlainMyQDeviceDiscoveryService discoveryService;

        public ListDevicesCallback(ChamberlainMyQDeviceDiscoveryService discoveryService) {
            this.discoveryService = discoveryService;
        }

        @Override
        public void parseRequestResult(MyqJson jsonResult) {
            discoveryService.enumerateDevices(jsonResult);
        }

        @Override
        public void onError(String error) {
            discoveryService.logger.error("Error during the device discovery: {}", error);
        }
    }

    private void readDeviceDatabase() throws IOException {
        hubHandler.sendRequestToServer(new ListDevicesCallback(this));
    }
}
