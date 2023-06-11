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
package org.openhab.binding.adorne.internal.discovery;

import static org.openhab.binding.adorne.internal.AdorneBindingConstants.*;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.adorne.internal.configuration.AdorneHubConfiguration;
import org.openhab.binding.adorne.internal.hub.AdorneHubChangeNotify;
import org.openhab.binding.adorne.internal.hub.AdorneHubController;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.UIDUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AdorneDiscoveryService} discovers things for the Adorne hub and Adorne devices.
 * Discovery is only supported if the hub is accessible via default host and port.
 *
 * @author Mark Theiding - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.adorne")
public class AdorneDiscoveryService extends AbstractDiscoveryService implements AdorneHubChangeNotify {

    private final Logger logger = LoggerFactory.getLogger(AdorneDiscoveryService.class);
    private static final int DISCOVERY_TIMEOUT_SECONDS = 10;
    private static final String DISCOVERY_HUB_LABEL = "Adorne Hub";
    private static final String DISCOVERY_ZONE_ID = "zoneId";
    private @Nullable AdorneHubController adorneHubController;

    /**
     * Creates a AdorneDiscoveryService with disabled auto-discovery.
     */
    public AdorneDiscoveryService() {
        // Passing false as last argument to super constructor turns off background discovery
        super(Collections.singleton(new ThingTypeUID(BINDING_ID, "-")), DISCOVERY_TIMEOUT_SECONDS, false);

        // We create the hub controller with default host and port. In the future we could let users create hubs
        // manually with custom host and port settings and then perform discovery here for those hubs.
        adorneHubController = null;
    }

    /**
     * Kick off discovery of all devices on the hub
     */
    @Override
    protected void startScan() {
        logger.debug("Discovery scan started");

        AdorneHubController adorneHubController = new AdorneHubController(new AdorneHubConfiguration(), scheduler,
                this);
        this.adorneHubController = adorneHubController;

        // Hack - we wrap the ThingUID in an array to make it appear effectively final to the compiler throughout the
        // chain of futures. Passing it through the chain as context would bloat the code.
        ThingUID[] bridgeUID = new ThingUID[1];

        // Future enhancement: Need a timeout for each future execution to recover from bugs in the hub controller, but
        // Java8 doesn't yet offer that
        adorneHubController.start().thenCompose(Void -> {
            // We use the hub's MAC address as its unique identifier
            return adorneHubController.getMACAddress();
        }).thenCompose(macAddress -> {
            String macAddressNoColon = macAddress.replace(':', '-'); // Colons are not allowed in ThingUIDs
            bridgeUID[0] = new ThingUID(THING_TYPE_HUB, macAddressNoColon);
            // We have fully discovered the hub
            thingDiscovered(DiscoveryResultBuilder.create(bridgeUID[0]).withLabel(DISCOVERY_HUB_LABEL).build());
            return adorneHubController.getZones();
        }).thenAccept(zoneIds -> {
            zoneIds.forEach(zoneId -> {
                adorneHubController.getState(zoneId).thenAccept(state -> {
                    String id = UIDUtils.encode(state.name); // Strip zone ID's name to become a valid ThingUID
                    // We have fully discovered a new zone ID
                    thingDiscovered(DiscoveryResultBuilder
                            .create(new ThingUID(state.deviceType, bridgeUID[0], id.toLowerCase()))
                            .withLabel(state.name).withBridge(bridgeUID[0])
                            .withProperty(DISCOVERY_ZONE_ID, state.zoneId).build());
                }).exceptionally(e -> {
                    logger.warn("Discovery of zone ID {} failed ({})", zoneId, e.getMessage());
                    return null;
                });
            });
            adorneHubController.stopWhenCommandsServed(); // Shut down hub once all discovery requests have been served
        }).exceptionally(e -> {
            logger.warn("Discovery failed ({})", e.getMessage());
            return null;
        });
    }

    /**
     * Notification to stop scanning
     */
    @Override
    protected void stopScan() {
        super.stopScan();

        AdorneHubController adorneHubController = this.adorneHubController;
        if (adorneHubController != null) {
            adorneHubController.stop();
            this.adorneHubController = null;
            logger.debug("Discovery timed out. Scan stopped.");
        }
    }

    // Nothing to do on change notifications
    @Override
    public void stateChangeNotify(int zoneId, boolean onOff, int brightness) {
    }

    @Override
    public void connectionChangeNotify(boolean connected) {
    }
}
