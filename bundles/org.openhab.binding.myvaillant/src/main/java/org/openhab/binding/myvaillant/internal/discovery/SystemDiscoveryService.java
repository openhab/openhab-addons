/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.myvaillant.internal.discovery;

import static org.openhab.binding.myvaillant.internal.MyVaillantBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myvaillant.internal.api.JsonUtil;
import org.openhab.binding.myvaillant.internal.api.MyVaillantDevice;
import org.openhab.binding.myvaillant.internal.api.SystemData;
import org.openhab.binding.myvaillant.internal.handler.ComponentHandler;
import org.openhab.binding.myvaillant.internal.handler.SystemHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Discovers the zones, circuits, hot water and devices of a system from the system's last update, so it never sends a
 * request itself.
 *
 * @author Marius Korte - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SystemDiscoveryService.class)
@NonNullByDefault
public class SystemDiscoveryService extends AbstractThingHandlerDiscoveryService<SystemHandler> {

    private static final int SCAN_TIMEOUT_SECONDS = 5;

    public SystemDiscoveryService() {
        super(SystemHandler.class, SYSTEM_COMPONENT_THING_TYPES, SCAN_TIMEOUT_SECONDS, true);
    }

    @Override
    protected void startBackgroundDiscovery() {
        thingHandler.setDataListener(this::discover);
        startScan();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        thingHandler.setDataListener(null);
    }

    @Override
    protected void startScan() {
        SystemData data = thingHandler.getData();
        if (data != null) {
            discover(data);
        }
    }

    private void discover(SystemData data) {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        String home = data.getHomeName();
        data.getZones().forEach((index, zone) -> {
            String name = JsonUtil.getString(JsonUtil.getObject(zone, "general"), "name");
            discoverComponent(data, THING_TYPE_ZONE, bridgeUID, "zone" + index, index,
                    home + " " + (name == null || name.isBlank() ? "Zone " + index : name));
        });
        data.getCircuits().keySet().forEach(index -> discoverComponent(data, THING_TYPE_CIRCUIT, bridgeUID,
                "circuit" + index, index, home + " Circuit " + index));
        data.getDomesticHotWater().keySet().forEach(index -> discoverComponent(data, THING_TYPE_DHW, bridgeUID,
                "dhw" + index, index, home + " Hot Water" + (Integer.valueOf(255).equals(index) ? "" : " " + index)));

        Map<String, Integer> typeCounts = new HashMap<>();
        for (MyVaillantDevice device : data.getDevices()) {
            Integer previous = typeCounts.get(device.type());
            int count = previous == null ? 1 : previous + 1;
            typeCounts.put(device.type(), count);
            String thingId = device.type().replace('_', '-') + (count > 1 ? "-" + count : "");
            ThingUID uid = new ThingUID(THING_TYPE_DEVICE, bridgeUID, thingId);
            thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                    .withLabel(home + " " + device.getName()).withProperty(CONFIG_DEVICE_UUID, device.deviceUuid())
                    .withRepresentationProperty(CONFIG_DEVICE_UUID).build());
        }
    }

    private void discoverComponent(SystemData data, ThingTypeUID thingTypeUID, ThingUID bridgeUID, String thingId,
            int index, String label) {
        ThingUID uid = new ThingUID(thingTypeUID, bridgeUID, thingId);
        thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(label)
                .withProperty(CONFIG_INDEX, index)
                .withProperty(PROPERTY_COMPONENT_ID, ComponentHandler.componentId(data.getSystemId(), index))
                .withRepresentationProperty(PROPERTY_COMPONENT_ID).build());
    }
}
