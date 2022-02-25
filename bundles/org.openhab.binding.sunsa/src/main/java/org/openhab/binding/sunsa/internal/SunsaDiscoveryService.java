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
package org.openhab.binding.sunsa.internal;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sunsa.internal.client.SunsaService;
import org.openhab.binding.sunsa.internal.client.SunsaService.SunsaException;
import org.openhab.binding.sunsa.internal.device.SunsaDeviceConfiguration;
import org.openhab.binding.sunsa.internal.domain.Device;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for Sunsa devices.
 * 
 * @author jirom - Initial contribution
 */
@NonNullByDefault
public class SunsaDiscoveryService extends AbstractDiscoveryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SunsaDiscoveryService.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(SunsaBindingConstants.THING_TYPE_DEVICE);
    private static final int TIMEOUT_SECS = 15;

    private final SunsaService sunsaService;
    private final ThingUID bridgeUID;

    public SunsaDiscoveryService(final SunsaService sunsaService, final ThingUID bridgeUID)
            throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES, TIMEOUT_SECS, false);
        this.sunsaService = requireNonNull(sunsaService);
        this.bridgeUID = requireNonNull(bridgeUID);
    }

    @Override
    protected void startScan() {
        try {
            final List<Device> devices = sunsaService.getDevices();
            devices.stream().map(device -> {
                final ThingUID thingUID = new ThingUID(SunsaBindingConstants.THING_TYPE_DEVICE, bridgeUID,
                        device.getId());
                return DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withLabel("Sunsa Device: " + device.getName())
                        .withRepresentationProperty(SunsaDeviceConfiguration.KEY_ID)
                        .withProperty(SunsaDeviceConfiguration.KEY_ID, device.getId()).build();
            }).forEach(this::thingDiscovered);
        } catch (SunsaException e) {
            LOGGER.error("Discovery of devices failed.", e);
        }
    }
}
