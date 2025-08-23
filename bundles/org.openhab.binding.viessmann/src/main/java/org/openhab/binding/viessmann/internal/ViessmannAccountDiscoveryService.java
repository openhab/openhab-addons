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
import org.openhab.binding.viessmann.internal.handler.ViessmannAccountHandler;
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
 * The {@link ViessmannAccountDiscoveryService} handles discovery of devices as they are identified by the bridge
 * handler.
 *
 * @author Ronny Grun - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ViessmannAccountDiscoveryService.class)
@NonNullByDefault
public class ViessmannAccountDiscoveryService extends AbstractThingHandlerDiscoveryService<ViessmannAccountHandler> {

    private final Logger logger = LoggerFactory.getLogger(ViessmannAccountDiscoveryService.class);

    private @Nullable ScheduledFuture<?> scanningJob;
    private @NonNullByDefault({}) ThingUID bridgeUID;

    public ViessmannAccountDiscoveryService() {
        super(ViessmannAccountHandler.class, DISCOVERABLE_DEVICE_TYPE_UIDS, 30, true);
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
        thingHandler.getGatewaysList().forEach(this::buildDiscoveryResult);

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

    private void buildDiscoveryResult(String serial) {
        Map<String, Integer> serialToInstallationId = thingHandler.getSerialToInstallationId();
        String installationId = String.valueOf(serialToInstallationId.get(serial));
        Map<String, String> serialToGatewayType = thingHandler.getSerialToGatewayType();
        String gatewayType = serialToGatewayType.get(serial);
        if (installationId != null) {
            ThingUID uid = new ThingUID(THING_TYPE_GATEWAY, bridgeUID, serial);
            Map<String, Object> properties = Map.ofEntries(entry(GATEWAY_SERIAL, serial),
                    entry(INSTALLATION_ID, installationId));
            String label = "Viessmann Gateway " + gatewayType;
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(properties)
                    .withRepresentationProperty(GATEWAY_SERIAL).withLabel(label).build();
            thingDiscovered(result);
            logger.debug("Discovered Gateway {}", uid);
        }
    }
}
