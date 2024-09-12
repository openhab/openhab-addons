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
package org.openhab.binding.wled.internal;

import static org.openhab.binding.wled.internal.WLedBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wled.internal.api.WledApi;
import org.openhab.binding.wled.internal.handlers.WLedBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link WLedSegmentDiscoveryService} Discovers and adds any Wled segments found by the bridge device.
 *
 * @author Matthew Skinner - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = WLedSegmentDiscoveryService.class)
@NonNullByDefault
public class WLedSegmentDiscoveryService extends AbstractThingHandlerDiscoveryService<WLedBridgeHandler>
        implements DiscoveryService, ThingHandlerService {
    private @Nullable ThingUID bridgeUID;
    private static final int SEARCH_TIME = 10;

    public WLedSegmentDiscoveryService() {
        super(WLedBridgeHandler.class, SUPPORTED_THING_TYPES, SEARCH_TIME);
    }

    public WLedSegmentDiscoveryService(Set<ThingTypeUID> supportedThingTypes, int timeout)
            throws IllegalArgumentException {
        super(WLedBridgeHandler.class, supportedThingTypes, timeout);
    }

    private void buildThing(int segmentIndex, String segmentName) {
        ThingUID localBridgeUID = bridgeUID;
        if (localBridgeUID == null) {
            return;
        }
        String newThingUID = localBridgeUID.getId() + "-" + segmentIndex;
        ThingUID thingUID = new ThingUID(THING_TYPE_SEGMENT, localBridgeUID, newThingUID);
        Map<String, Object> properties = Map.of(Thing.PROPERTY_SERIAL_NUMBER, newThingUID, CONFIG_SEGMENT_INDEX,
                segmentIndex);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(segmentName)
                .withProperties(properties).withBridge(bridgeUID)
                .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    protected void startScan() {
        WledApi localAPI = thingHandler.api;
        if (localAPI != null) {
            List<String> names = localAPI.getSegmentNames();
            for (int count = 0; count < names.size(); count++) {
                buildThing(count, names.get(count));
            }
        }
    }

    @Override
    public void initialize() {
        bridgeUID = thingHandler.getThing().getUID();
        super.initialize();
    }
}
