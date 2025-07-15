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
package org.openhab.binding.tr064.internal;

import static org.openhab.binding.tr064.internal.Tr064BindingConstants.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDDeviceType;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.UIDUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Tr064DiscoveryService} discovers sub devices of a root device.
 *
 * @author Jan N. Klug - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = Tr064DiscoveryService.class)
@NonNullByDefault
public class Tr064DiscoveryService extends AbstractThingHandlerDiscoveryService<Tr064RootHandler> {
    private static final int SEARCH_TIME = 5;
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_SUBDEVICE);

    private final Logger logger = LoggerFactory.getLogger(Tr064DiscoveryService.class);

    public Tr064DiscoveryService() {
        super(Tr064RootHandler.class, SEARCH_TIME);
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now(), thingHandler.getThing().getUID());
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void startScan() {
        List<SCPDDeviceType> devices = thingHandler.getAllSubDevices();
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        devices.forEach(device -> {
            logger.trace("Trying to add {} to discovery results on {}", device, bridgeUID);
            String udn = device.getUDN();
            if (udn != null) {
                ThingTypeUID thingTypeUID;
                if ("urn:dslforum-org:device:LANDevice:1".equals(device.getDeviceType())) {
                    thingTypeUID = THING_TYPE_SUBDEVICE_LAN;
                } else {
                    thingTypeUID = THING_TYPE_SUBDEVICE;
                }
                ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, UIDUtils.encode(udn));

                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID) //
                        .withLabel(device.getFriendlyName()) //
                        .withBridge(bridgeUID) //
                        .withProperties(Map.of("uuid", udn, "deviceType", device.getDeviceType())) //
                        .withRepresentationProperty("uuid") //
                        .build();
                thingDiscovered(result);
            }
        });
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }
}
