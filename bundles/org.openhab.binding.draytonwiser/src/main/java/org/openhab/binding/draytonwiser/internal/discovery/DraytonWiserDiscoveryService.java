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
package org.openhab.binding.draytonwiser.internal.discovery;

import static org.openhab.binding.draytonwiser.internal.DraytonWiserBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.draytonwiser.internal.DraytonWiserRefreshListener;
import org.openhab.binding.draytonwiser.internal.handler.DraytonWiserPropertyHelper;
import org.openhab.binding.draytonwiser.internal.handler.HeatHubHandler;
import org.openhab.binding.draytonwiser.internal.model.DeviceDTO;
import org.openhab.binding.draytonwiser.internal.model.DraytonWiserDTO;
import org.openhab.binding.draytonwiser.internal.model.HotWaterDTO;
import org.openhab.binding.draytonwiser.internal.model.RoomDTO;
import org.openhab.binding.draytonwiser.internal.model.RoomStatDTO;
import org.openhab.binding.draytonwiser.internal.model.SmartPlugDTO;
import org.openhab.binding.draytonwiser.internal.model.SmartValveDTO;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DraytonWiserDiscoveryService} is used to discover devices that are connected to a Heat Hub.
 *
 * @author Andrew Schofield - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = DraytonWiserDiscoveryService.class)
@NonNullByDefault
public class DraytonWiserDiscoveryService extends AbstractThingHandlerDiscoveryService<HeatHubHandler>
        implements DraytonWiserRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(DraytonWiserDiscoveryService.class);

    private @Nullable ThingUID bridgeUID;

    public DraytonWiserDiscoveryService() {
        super(HeatHubHandler.class, SUPPORTED_THING_TYPES_UIDS, 30, false);
    }

    @Override
    protected void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        thingHandler.setDiscoveryService(this);
    }

    @Override
    public void onRefresh(final DraytonWiserDTO domainDTOProxy) {
        logger.debug("Received data from Drayton Wiser device. Parsing to discover devices.");
        onControllerAdded(domainDTOProxy);
        domainDTOProxy.getRooms().forEach(this::onRoomAdded);
        domainDTOProxy.getRoomStats().forEach(r -> onRoomStatAdded(domainDTOProxy, r));
        domainDTOProxy.getSmartValves().forEach(sv -> onSmartValveAdded(domainDTOProxy, sv));
        domainDTOProxy.getHotWater().forEach(hw -> onHotWaterAdded(domainDTOProxy, hw));
        domainDTOProxy.getSmartPlugs().forEach(sp -> onSmartPlugAdded(domainDTOProxy, sp));
    }

    private void onControllerAdded(final DraytonWiserDTO domainDTOProxy) {
        final DeviceDTO device = domainDTOProxy.getExtendedDeviceProperties(0);

        if (device != null) {
            logger.debug("Controller discovered, model: {}", device.getModelIdentifier());
            onThingWithId(THING_TYPE_CONTROLLER, "controller", device, "Controller");
        }
    }

    private void onHotWaterAdded(final DraytonWiserDTO domainDTOProxy, final HotWaterDTO hotWater) {
        final Integer hotWaterId = hotWater.getId();
        final String roomName = getRoomName(domainDTOProxy, hotWaterId);

        onThingWithId(THING_TYPE_HOTWATER, "hotwater" + hotWaterId, null,
                (roomName.isEmpty() ? "" : (roomName + " - ")) + "Hot Water");
    }

    private void onThingWithId(final ThingTypeUID deviceType, final String deviceTypeId,
            @Nullable final DeviceDTO device, final String name) {
        logger.debug("{} discovered: {}", deviceTypeId, name);
        ThingUID localBridgeUID = this.bridgeUID;
        if (localBridgeUID != null) {
            final Map<String, Object> properties = new HashMap<>();

            properties.put(PROP_ID, deviceTypeId);
            if (device != null) {
                DraytonWiserPropertyHelper.setGeneralDeviceProperties(device, properties);
            }
            final DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(deviceType, localBridgeUID, deviceTypeId)).withBridge(localBridgeUID)
                    .withProperties(properties).withRepresentationProperty(PROP_ID).withLabel(name).build();

            thingDiscovered(discoveryResult);
        }
    }

    private void onRoomStatAdded(final DraytonWiserDTO domainDTOProxy, final RoomStatDTO roomStat) {
        final Integer roomStatId = roomStat.getId();
        final DeviceDTO device = domainDTOProxy.getExtendedDeviceProperties(roomStatId);

        if (device != null) {
            onThingWithSerialNumber(THING_TYPE_ROOMSTAT, "Thermostat", device, getRoomName(domainDTOProxy, roomStatId));
        }
    }

    private void onRoomAdded(final RoomDTO room) {
        ThingUID localBridgeUID = this.bridgeUID;
        if (localBridgeUID != null) {
            final Map<String, Object> properties = new HashMap<>();

            logger.debug("Room discovered: {}", room.getName());
            properties.put(PROP_NAME, room.getName());
            final DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(THING_TYPE_ROOM, localBridgeUID,
                            room.getName().replaceAll("[^A-Za-z0-9]", "").toLowerCase()))
                    .withBridge(localBridgeUID).withProperties(properties).withRepresentationProperty(PROP_NAME)
                    .withLabel(room.getName()).build();

            thingDiscovered(discoveryResult);
        }
    }

    private void onSmartValveAdded(final DraytonWiserDTO domainDTOProxy, final SmartValveDTO smartValve) {
        final Integer smartValueId = smartValve.getId();
        final DeviceDTO device = domainDTOProxy.getExtendedDeviceProperties(smartValueId);

        if (device != null) {
            onThingWithSerialNumber(THING_TYPE_ITRV, "TRV", device, getRoomName(domainDTOProxy, smartValueId));
        }
    }

    private void onSmartPlugAdded(final DraytonWiserDTO domainDTOProxy, final SmartPlugDTO smartPlug) {
        final DeviceDTO device = domainDTOProxy.getExtendedDeviceProperties(smartPlug.getId());

        if (device != null) {
            onThingWithSerialNumber(THING_TYPE_SMARTPLUG, "Smart Plug", device, smartPlug.getName());
        }
    }

    private String getRoomName(final DraytonWiserDTO domainDTOProxy, final Integer roomId) {
        final RoomDTO assignedRoom = domainDTOProxy.getRoomForDeviceId(roomId);
        return assignedRoom == null ? "" : assignedRoom.getName();
    }

    private void onThingWithSerialNumber(final ThingTypeUID deviceType, final String deviceTypeName,
            final DeviceDTO device, final String name) {
        final String serialNumber = device.getSerialNumber();
        logger.debug("{} discovered, serialnumber: {}", deviceTypeName, serialNumber);
        ThingUID localBridgeUID = this.bridgeUID;
        if (localBridgeUID != null) {
            final Map<String, Object> properties = new HashMap<>();

            DraytonWiserPropertyHelper.setPropertiesWithSerialNumber(device, properties);
            final DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(deviceType, localBridgeUID, serialNumber)).withBridge(localBridgeUID)
                    .withProperties(properties).withRepresentationProperty(PROP_SERIAL_NUMBER)
                    .withLabel((name.isEmpty() ? "" : (name + " - ")) + deviceTypeName).build();

            thingDiscovered(discoveryResult);
        }
    }

    @Override
    public synchronized void stopScan() {
        thingHandler.unsetDiscoveryService();
        super.stopScan();
    }

    @Override
    public void initialize() {
        bridgeUID = thingHandler.getThing().getUID();
        super.initialize();
    }
}
