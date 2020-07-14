/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.tr064.internal.Tr064BindingConstants.THING_TYPE_SUBDEVICE;
import static org.openhab.binding.tr064.internal.Tr064BindingConstants.THING_TYPE_SUBDEVICE_LAN;

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.util.UIDUtils;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Tr064DiscoveryService} discovers sub devices of a root device.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Tr064DiscoveryService extends AbstractDiscoveryService {
    private static final int SEARCH_TIME = 5;
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_SUBDEVICE);

    private final Logger logger = LoggerFactory.getLogger(Tr064DiscoveryService.class);

    private final Tr064RootHandler bridgeHandler;

    public Tr064DiscoveryService(Tr064RootHandler bridgeHandler) {
        super(SEARCH_TIME);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime(), bridgeHandler.getThing().getUID());
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void startScan() {
        List<SCPDDeviceType> devices = bridgeHandler.getAllSubDevices();
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        devices.forEach(device -> {
            logger.trace("Trying to add {} to discovery results on {}", device, bridgeUID);
            String udn = device.getUDN();
            if (udn != null) {
                ThingTypeUID thingTypeUID;
                switch (device.getDeviceType()) {
                    case "urn:dslforum-org:device:LANDevice:1":
                        thingTypeUID = THING_TYPE_SUBDEVICE_LAN;
                        break;
                    default:
                        thingTypeUID = THING_TYPE_SUBDEVICE;
                }
                ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, UIDUtils.encode(udn));

                Map<String, Object> properties = new HashMap<>(2);
                properties.put("uuid", udn);
                properties.put("deviceType", device.getDeviceType());

                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel(device.getFriendlyName())
                        .withBridge(bridgeHandler.getThing().getUID()).withProperties(properties).build();
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
