/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser.internal.discovery;

import static org.openhab.binding.draytonwiser.DraytonWiserBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.draytonwiser.DraytonWiserBindingConstants;
import org.openhab.binding.draytonwiser.handler.HeatHubHandler;
import org.openhab.binding.draytonwiser.internal.config.Device;
import org.openhab.binding.draytonwiser.internal.config.Room;
import org.openhab.binding.draytonwiser.internal.config.RoomStat;
import org.openhab.binding.draytonwiser.internal.config.SmartValve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DraytonWiserDiscoveryService} is used to discover devices that are connected to a Heat Hub.
 *
 * @author Andrew Schofield - Initial contribution
 */
public class DraytonWiserDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(DraytonWiserDiscoveryService.class);

    private HeatHubHandler bridgeHandler;

    public DraytonWiserDiscoveryService(HeatHubHandler bridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, 30, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    /**
     * Called on component activation.
     */
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        onControllerAdded();
        List<Room> rooms = bridgeHandler.getRooms();
        for (Room r : rooms) {
            onRoomAdded(r);
        }
        List<RoomStat> roomStats = bridgeHandler.getRoomStats();
        for (RoomStat r : roomStats) {
            onRoomStatAdded(r);
        }
        List<SmartValve> smartValves = bridgeHandler.getSmartValves();
        for (SmartValve v : smartValves) {
            onSmartValveAdded(v);
        }
    }

    private void onControllerAdded() {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        Map<String, Object> properties = new HashMap<>();
        Device device = bridgeHandler.getExtendedDeviceProperties(0);
        if (device != null) {
            properties.put("Device Type", device.getProductIdentifier());
            properties.put("Firmware Version", device.getActiveFirmwareVersion());
            properties.put("Manufacturer", device.getManufacturer());
            properties.put("Model", device.getModelIdentifier());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(DraytonWiserBindingConstants.THING_TYPE_CONTROLLER, bridgeUID, "controller"))
                    .withProperties(properties).withBridge(bridgeUID).withLabel("Controller").build();

            thingDiscovered(discoveryResult);
        }
    }

    private void onRoomStatAdded(RoomStat r) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        Map<String, Object> properties = new HashMap<>();
        Device device = bridgeHandler.getExtendedDeviceProperties(r.getId());
        if (device != null) {
            properties.put("serialNumber", device.getSerialNumber());
            properties.put("Device Type", device.getModelIdentifier());
            properties.put("Firmware Version", device.getActiveFirmwareVersion());
            properties.put("Manufacturer", device.getManufacturer());
            properties.put("Model", device.getProductModel());
            properties.put("Serial Number", device.getSerialNumber());

            Room assignedRoom = bridgeHandler.getRoomForDeviceId(r.getId());
            String assignedRoomName = assignedRoom == null ? "" : assignedRoom.getName();

            DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(DraytonWiserBindingConstants.THING_TYPE_ROOMSTAT, bridgeUID,
                            device.getSerialNumber().toString()))
                    .withProperties(properties).withBridge(bridgeUID).withLabel(assignedRoomName + " - Thermostat")
                    .withRepresentationProperty(device.getSerialNumber()).build();

            thingDiscovered(discoveryResult);
        }
    }

    private void onRoomAdded(Room r) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        Map<String, Object> properties = new HashMap<>();
        properties.put("roomName", r.getName());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder
                .create(new ThingUID(DraytonWiserBindingConstants.THING_TYPE_ROOM, bridgeUID,
                        r.getName().replaceAll("[^A-Za-z0-9]", "").toLowerCase()))
                .withBridge(bridgeUID).withProperties(properties).withLabel(r.getName()).build();

        thingDiscovered(discoveryResult);
    }

    private void onSmartValveAdded(SmartValve r) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        Map<String, Object> properties = new HashMap<>();
        Device device = bridgeHandler.getExtendedDeviceProperties(r.getId());
        if (device != null) {
            properties.put("serialNumber", device.getSerialNumber());
            properties.put("Device Type", device.getModelIdentifier());
            properties.put("Firmware Version", device.getActiveFirmwareVersion());
            properties.put("Manufacturer", device.getManufacturer());
            properties.put("Model", device.getProductModel());
            properties.put("Serial Number", device.getSerialNumber());

            Room assignedRoom = bridgeHandler.getRoomForDeviceId(r.getId());
            String assignedRoomName = assignedRoom == null ? "" : assignedRoom.getName();

            DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(DraytonWiserBindingConstants.THING_TYPE_ITRV, bridgeUID,
                            device.getSerialNumber().toString()))
                    .withProperties(properties).withBridge(bridgeUID).withLabel(assignedRoomName + " - TRV")
                    .withRepresentationProperty(device.getSerialNumber()).build();

            thingDiscovered(discoveryResult);
        }
    }

    @Override
    public synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }
}
