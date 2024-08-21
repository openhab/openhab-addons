/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.elroconnects.internal.discovery;

import static org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants;
import org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.ElroDeviceType;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsDevice;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElroConnectsDiscoveryService} discovers devices connected to the ELRO Connects K1 Controller.
 *
 * @author Mark Herwege - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ElroConnectsDiscoveryService.class)
@NonNullByDefault
public class ElroConnectsDiscoveryService extends AbstractThingHandlerDiscoveryService<ElroConnectsBridgeHandler> {

    private final Logger logger = LoggerFactory.getLogger(ElroConnectsDiscoveryService.class);

    private static final int TIMEOUT_S = 5;
    private static final int REFRESH_INTERVAL_S = 60;

    private @Nullable ScheduledFuture<?> discoveryJob;

    public ElroConnectsDiscoveryService() {
        super(ElroConnectsBridgeHandler.class, ElroConnectsBindingConstants.SUPPORTED_DEVICE_TYPES_UIDS, TIMEOUT_S);
        logger.debug("Discovery service started");
    }

    @Override
    protected void startScan() {
        discoverDevices();
    }

    private void discoverDevices() {
        logger.debug("Starting device discovery scan");
        Map<Integer, ElroConnectsDevice> devices = thingHandler.getDevices();
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        devices.entrySet().forEach(e -> {
            String deviceId = e.getKey().toString();
            String deviceName = e.getValue().getDeviceName();
            String deviceType = e.getValue().getDeviceType();
            if (!deviceType.isEmpty()) {
                ElroDeviceType type = TYPE_MAP.get(deviceType);
                if (type != null) {
                    ThingTypeUID thingTypeUID = THING_TYPE_MAP.get(type);
                    if (thingTypeUID != null) {
                        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(thingTypeUID, bridgeUID, deviceId))
                                .withLabel(deviceName).withBridge(bridgeUID).withProperty(CONFIG_DEVICE_ID, deviceId)
                                .withRepresentationProperty(CONFIG_DEVICE_ID).build());
                    }
                }
            }
        });
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void startBackgroundDiscovery() {
        logger.debug("Start device background discovery");
        ScheduledFuture<?> job = discoveryJob;
        if (job == null || job.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::discoverDevices, 0, REFRESH_INTERVAL_S,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop device background discovery");
        ScheduledFuture<?> job = discoveryJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now().toEpochMilli());
    }

    @Override
    public void initialize() {
        thingHandler.setDiscoveryService(this);
        super.initialize();
    }
}
