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
package org.openhab.binding.tr064.internal;

import static org.openhab.binding.tr064.internal.Tr064BindingConstants.*;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDDeviceType;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Tr064DiscoveryService} discovers sub devices of a root device.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Tr064DiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private static final int SEARCH_TIME = 5;
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_SUBDEVICE);

    private final Logger logger = LoggerFactory.getLogger(Tr064DiscoveryService.class);
    private @Nullable Tr064RootHandler bridgeHandler;

    public Tr064DiscoveryService() {
        super(SEARCH_TIME);
    }

    @Override
    public void setThingHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof Tr064RootHandler) {
            this.bridgeHandler = (Tr064RootHandler) thingHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void deactivate() {
        BridgeHandler bridgeHandler = this.bridgeHandler;
        if (bridgeHandler == null) {
            logger.warn("Bridgehandler not found, could not cleanup discovery results.");
            return;
        }
        removeOlderResults(new Date().getTime(), bridgeHandler.getThing().getUID());
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void startScan() {
        Tr064RootHandler bridgeHandler = this.bridgeHandler;
        if (bridgeHandler == null) {
            logger.warn("Could not start discovery, bridge handler not set");
            return;
        }
        List<SCPDDeviceType> devices = bridgeHandler.getAllSubDevices();
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
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

                Map<String, Object> properties = new HashMap<>(2);
                properties.put("uuid", udn);
                properties.put("deviceType", device.getDeviceType());

                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withLabel(device.getFriendlyName())
                        .withBridge(bridgeHandler.getThing().getUID()).withProperties(properties)
                        .withRepresentationProperty("uuid").build();
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
