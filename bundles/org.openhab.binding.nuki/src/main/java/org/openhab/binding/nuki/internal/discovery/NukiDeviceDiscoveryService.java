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
package org.openhab.binding.nuki.internal.discovery;

import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nuki.internal.constants.NukiBindingConstants;
import org.openhab.binding.nuki.internal.dataexchange.BridgeListResponse;
import org.openhab.binding.nuki.internal.dto.BridgeApiListDeviceDto;
import org.openhab.binding.nuki.internal.handler.NukiBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service which uses Brige API to find all devices connected to bridges.
 *
 * @author Jan Vybíral - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = NukiDeviceDiscoveryService.class)
@NonNullByDefault
public class NukiDeviceDiscoveryService extends AbstractThingHandlerDiscoveryService<NukiBridgeHandler> {

    private final Logger logger = LoggerFactory.getLogger(NukiDeviceDiscoveryService.class);

    public NukiDeviceDiscoveryService() {
        super(NukiBridgeHandler.class, Set.of(NukiBindingConstants.THING_TYPE_SMARTLOCK), 5, false);
    }

    @Override
    protected void startScan() {
        NukiBridgeHandler bridgeHandler = thingHandler;
        if (bridgeHandler == null) {
            logger.warn("Cannot start Nuki discovery - no bridge available");
            return;
        }

        scheduler.execute(() -> {
            bridgeHandler.withHttpClient(client -> {
                BridgeListResponse list = client.getList();
                list.getDevices().stream().map(device -> createDiscoveryResult(device, bridgeHandler))
                        .flatMap(Optional::stream).forEach(this::thingDiscovered);
            });
        });
    }

    private Optional<DiscoveryResult> createDiscoveryResult(BridgeApiListDeviceDto device,
            NukiBridgeHandler bridgeHandler) {
        ThingUID uid = getUid(device.getNukiId(), device.getDeviceType(), bridgeHandler);
        if (uid == null) {
            logger.warn("Failed to create UID for device '{}' - deviceType '{}' is not supported", device,
                    device.getDeviceType());
            return Optional.empty();
        } else {
            return Optional.of(DiscoveryResultBuilder.create(uid).withBridge(bridgeHandler.getThing().getUID())
                    .withLabel(device.getName()).withRepresentationProperty(NukiBindingConstants.PROPERTY_NUKI_ID)
                    .withProperty(NukiBindingConstants.PROPERTY_NAME, device.getName())
                    .withProperty(NukiBindingConstants.PROPERTY_NUKI_ID, device.getNukiId())
                    .withProperty(NukiBindingConstants.PROPERTY_DEVICE_TYPE, device.getDeviceType())
                    .withProperty(NukiBindingConstants.PROPERTY_FIRMWARE_VERSION, device.getFirmwareVersion()).build());
        }
    }

    @Nullable
    private ThingUID getUid(String nukiId, int deviceType, NukiBridgeHandler bridgeHandler) {
        switch (deviceType) {
            case NukiBindingConstants.DEVICE_OPENER:
                return new ThingUID(NukiBindingConstants.THING_TYPE_OPENER, bridgeHandler.getThing().getUID(), nukiId);
            case NukiBindingConstants.DEVICE_SMART_LOCK:
            case NukiBindingConstants.DEVICE_SMART_DOOR:
            case NukiBindingConstants.DEVICE_SMART_LOCK_3:
                return new ThingUID(NukiBindingConstants.THING_TYPE_SMARTLOCK, bridgeHandler.getThing().getUID(),
                        nukiId);
            default:
                return null;
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }
}
