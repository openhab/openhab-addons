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
package org.openhab.binding.hive.internal.discovery;

import static org.openhab.binding.hive.internal.HiveBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.ScanListener;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.hive.internal.dto.HiveNode;
import org.openhab.binding.hive.internal.handler.HiveBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Chris Foot - Initial contribution
 */
@NonNullByDefault
public class HiveDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {

    @Nullable protected ScheduledFuture<?> hiveDiscoveryJob;
    private final Logger logger = LoggerFactory.getLogger(HiveDiscoveryService.class);
    @Nullable private HiveBridgeHandler hiveBridgeHandler;

    public HiveDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 30);
    }

    public void addDevice(HiveNode node) {
        if (node.attributes.nodeType.reportedValue.equals(THERMOSTAT_NODE_TYPE)) {
            ThingUID bridgeUID = hiveBridgeHandler.getThing().getUID();
            ThingUID thingUID = new ThingUID(THERMOSTAT_THING_TYPE, node.id);

            Map<String, Object> properties = new HashMap<>(1);
            properties.put("linkedDevice", node.linkedNode);
            properties.put(Thing.PROPERTY_VENDOR, "Hive Limited");
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, node.firmwareVersion);
            properties.put(Thing.PROPERTY_MAC_ADDRESS, node.macAddress);
            properties.put(Thing.PROPERTY_MODEL_ID, node.model);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withThingType(THERMOSTAT_THING_TYPE).withBridge(bridgeUID).withLabel("Thermostat")
                    .withProperties(properties).withRepresentationProperty(node.id).build();
            thingDiscovered(discoveryResult);
        }
    }

    @Override
    protected void startScan() {
        // Only scan if there is a bridge
        if (hiveBridgeHandler == null) {
            stopScan();
            return;
        }

        hiveBridgeHandler.checkForDevices();
    }

    @Override
    public synchronized void startScan(@Nullable ScanListener listener) {
        // Only scan if there is a bridge
        if (hiveBridgeHandler == null) {
            listener.onFinished();
            return;
        }

        super.startScan(listener);
    }

    @Override
    public void startBackgroundDiscovery() {
        logger.debug("Start Hive device background discovery");
        if (hiveDiscoveryJob == null || hiveDiscoveryJob.isCancelled()) {
            hiveDiscoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 0, 240, TimeUnit.SECONDS);
        }
    }

    @Override
    public void stopBackgroundDiscovery() {
        logger.debug("Stop Hive device background discovery");
        if (hiveDiscoveryJob != null && !hiveDiscoveryJob.isCancelled()) {
            hiveDiscoveryJob.cancel(true);
            hiveDiscoveryJob = null;
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof HiveBridgeHandler) {
            this.hiveBridgeHandler = (HiveBridgeHandler) handler;
            this.hiveBridgeHandler.setDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return hiveBridgeHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

}
