/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.viessmann.internal;

import static java.util.Map.entry;
import static org.openhab.binding.viessmann.internal.ViessmannBindingConstants.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.viessmann.internal.dto.device.DeviceData;
import org.openhab.binding.viessmann.internal.handler.ViessmannGatewayHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ViessmannGatewayDiscoveryService} handles discovery of devices as they are identified by the bridge
 * handler.
 *
 * @author Ronny Grun - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ViessmannGatewayDiscoveryService.class)
@NonNullByDefault
public class ViessmannGatewayDiscoveryService extends AbstractThingHandlerDiscoveryService<ViessmannGatewayHandler> {

    private final Logger logger = LoggerFactory.getLogger(ViessmannGatewayDiscoveryService.class);

    private @Nullable ScheduledFuture<?> scanningJob;
    private @NonNullByDefault({}) ThingUID bridgeUID;

    public ViessmannGatewayDiscoveryService() {
        super(ViessmannGatewayHandler.class, DISCOVERABLE_DEVICE_TYPE_UIDS, 30, true);
    }

    @Override
    public void initialize() {
        this.bridgeUID = thingHandler.getThing().getUID();
        super.initialize();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return DISCOVERABLE_DEVICE_TYPE_UIDS;
    }

    @Override
    protected void startScan() {
        stopScan();
        thingHandler.getDiscoveredDeviceList().forEach(this::buildDiscoveryResult);

        // we clear all older results, they are not valid any longer, and we created new results
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    protected void startBackgroundDiscovery() {
        final ScheduledFuture<?> scanningJob = this.scanningJob;
        if (scanningJob == null || scanningJob.isCancelled()) {
            this.scanningJob = scheduler.scheduleWithFixedDelay(this::startScan, 0, 15, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        final ScheduledFuture<?> scanningJob = this.scanningJob;
        if (scanningJob != null) {
            scanningJob.cancel(true);
            this.scanningJob = null;
        }
    }

    private void buildDiscoveryResult(DeviceData device) {
        ThingUID uid = new ThingUID(THING_TYPE_DEVICE, bridgeUID, device.id);
        Map<String, Object> properties = Map.ofEntries(entry(PROPERTY_ID, device.id),
                entry(REPRESENTATION_ID, device.gatewaySerial + ":" + device.id),
                entry(GATEWAY_SERIAL, device.gatewaySerial), entry(INSTALLATION_ID, device.installationId));
        String label = "Viessmann Device " + device.id;
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(properties)
                .withRepresentationProperty(REPRESENTATION_ID).withLabel(label).build();
        thingDiscovered(result);
        logger.debug("Discovered Device {}", uid);
    }
}
