/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.discovery;

import com.google.common.collect.ImmutableMap;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.supla.handler.SuplaCloudBridgeHandler;
import org.openhab.binding.supla.internal.api.IoDevicesManager;
import org.openhab.binding.supla.internal.di.ApplicationContext;
import org.openhab.binding.supla.internal.supla.entities.SuplaIoDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.openhab.binding.supla.SuplaBindingConstants.*;

public final class SuplaDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(SuplaDiscoveryService.class);
    private final SuplaCloudBridgeHandler suplaCloudBridgeHandler;

    public SuplaDiscoveryService(SuplaCloudBridgeHandler suplaCloudBridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, 10, true);
        this.suplaCloudBridgeHandler = suplaCloudBridgeHandler;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Supla discovery service");
        suplaCloudBridgeHandler.getApplicationContext()
                .map(ApplicationContext::getIoDevicesManager)
                .map(IoDevicesManager::obtainIoDevices)
                .ifPresent(this::addSuplaThing);
    }

    private void addSuplaThing(Collection<SuplaIoDevice> devices) {
        devices.forEach(device ->
                addThing(suplaCloudBridgeHandler.getThing().getUID(),
                        SUPLA_IO_DEVICE_THING_ID,
                        device.getId(),
                        buildThingLabel(device),
                        buildThingProperties(device)));
    }

    private void addThing(ThingUID bridgeUID, String thingType, long thingID, String label, Map<String, Object> properties) {
        findThingUID(bridgeUID, thingType, thingID)
                .map(uid -> createDiscoveryResult(uid, bridgeUID, label, properties))
                .ifPresent(this::thingDiscovered);
    }

    private Optional<ThingUID> findThingUID(ThingUID bridgeUID, String thingType, long thingID) {
        switch (thingType) {
            case SUPLA_IO_DEVICE_THING_ID:
                return Optional.of(new ThingUID(SUPLA_IO_DEVICE_THING_TYPE, bridgeUID, String.valueOf(thingID)));
        }
        return Optional.empty();
    }

    private DiscoveryResult createDiscoveryResult(ThingUID thingUID, ThingUID bridgeUID, String label, Map<String, Object> properties) {
        return DiscoveryResultBuilder.create(thingUID)
                .withBridge(bridgeUID)
                .withProperties(properties)
                .withLabel(label)
                .build();
    }

    private String buildThingLabel(SuplaIoDevice device) {
        final StringBuilder sb = new StringBuilder();

        final String name = device.getName();
        if (!isNullOrEmpty(name)) {
            sb.append(name);

            // comment cannot appear without name
            final String comment = device.getComment();
            if (!isNullOrEmpty(comment)) {
                sb.append("(").append(comment).append(")");
            }
        }

        final String primaryLabel = sb.toString();
        if (!isNullOrEmpty(primaryLabel)) {
            return primaryLabel;
        } else {
            return device.getGuid();
        }
    }

    private Map<String, Object> buildThingProperties(SuplaIoDevice device) {
        return ImmutableMap.<String, Object>builder()
                .put(SUPLA_IO_DEVICE_ID, (int) device.getId())
                .build();
    }
}
