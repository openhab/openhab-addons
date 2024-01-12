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
package org.openhab.binding.shelly.internal.discovery;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.core.thing.Thing.PROPERTY_MAC_ADDRESS;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device discovery creates a thing in the inbox for each vehicle
 * found in the data received from {@link ShellyBluDiscoveryService}.
 *
 * @author Markus Michels - Initial Contribution
 *
 */
@NonNullByDefault
public class ShellyBluDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(ShellyBluDiscoveryService.class);

    private final BundleContext bundleContext;
    private final ShellyThingTable thingTable;
    private static final int TIMEOUT = 10;
    private @Nullable ServiceRegistration<?> discoveryService;

    public ShellyBluDiscoveryService(BundleContext bundleContext, ShellyThingTable thingTable) {
        super(SUPPORTED_THING_TYPES_UIDS, TIMEOUT);
        this.bundleContext = bundleContext;
        this.thingTable = thingTable;
    }

    @SuppressWarnings("null")
    public void registerDeviceDiscoveryService() {
        if (discoveryService == null) {
            discoveryService = bundleContext.registerService(DiscoveryService.class.getName(), this, new Hashtable<>());
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting BLU Discovery");
        thingTable.startScan();
    }

    public void discoveredResult(ThingTypeUID tuid, String model, String serviceName, String address,
            Map<String, Object> properties) {
        ThingUID uid = ShellyThingCreator.getThingUID(serviceName, model, "", true);
        logger.debug("Adding discovered thing with id {}", uid.toString());
        properties.put(PROPERTY_MAC_ADDRESS, address);
        String thingLabel = "Shelly BLU " + model + " (" + serviceName + ")";
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withRepresentationProperty(PROPERTY_DEV_NAME).withLabel(thingLabel).build();
        thingDiscovered(result);
    }

    public void unregisterDeviceDiscoveryService() {
        if (discoveryService != null) {
            discoveryService.unregister();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        unregisterDeviceDiscoveryService();
    }
}
