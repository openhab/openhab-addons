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
package org.openhab.binding.draytonwiser.internal.discovery;

import static org.openhab.binding.draytonwiser.internal.DraytonWiserBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DraytonWiserDiscoveryService} is used to discover devices that are connected to a Heat Hub.
 *
 * @author Andrew Schofield - Initial contribution
 */
@NonNullByDefault
public class DraytonWiserDiscoveryService extends AbstractDiscoveryService
implements DiscoveryService, ThingHandlerService, DraytonWiserRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(DraytonWiserDiscoveryService.class);

    private @Nullable HeatHubHandler bridgeHandler;
    private @Nullable ThingUID bridgeUID;

    public DraytonWiserDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 30, false);
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        if (bridgeHandler == null) {
            return;
        }
        removeOlderResults(getTimestampOfLastScan());
        bridgeHandler.setDiscoveryService(this);
    }

    @Override
    public void onRefresh(final DraytonWiserDTO domainDTOProxy) {
        logger.debug("Received data from Drayton Wise device. Parsing to discover devices.");
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
            final Map<String, Object> properties = new HashMap<>();

            DraytonWiserPropertyHelper.setControllerProperties(device, properties);
            final DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(THING_TYPE_CONTROLLER, bridgeUID, "controller")).withProperties(properties)
                    .withBridge(bridgeUID).withLabel("Controller").build();

            thingDiscovered(discoveryResult);
        }
    }

    private void onHotWaterAdded(final DraytonWiserDTO domainDTOProxy, final HotWaterDTO hotWater) {
        final DeviceDTO device = domainDTOProxy.getExtendedDeviceProperties(hotWater.getId());

        if (device != null) {
            logger.debug("Hot Water discovered, serialnumber: {}", device.getSerialNumber());
            final RoomDTO assignedRoom = domainDTOProxy.getRoomForDeviceId(hotWater.getId());
            final String assignedRoomName = assignedRoom == null ? "" : assignedRoom.getName();
            final DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(THING_TYPE_HOTWATER, bridgeUID, "hotwater")).withBridge(bridgeUID)
                    .withLabel(assignedRoomName + " - Hot Water").withRepresentationProperty(device.getSerialNumber()).build();

            thingDiscovered(discoveryResult);
        }
    }

    private void onRoomStatAdded(final DraytonWiserDTO domainDTOProxy, final RoomStatDTO roomStat) {
        final DeviceDTO device = domainDTOProxy.getExtendedDeviceProperties(roomStat.getId());

        if (device != null) {
            logger.debug("Smart Value discovered, serialnumber: {}", device.getSerialNumber());
            final Map<String, Object> properties = new HashMap<>();

            DraytonWiserPropertyHelper.setRoomStatProperties(device, properties);
            final RoomDTO assignedRoom = domainDTOProxy.getRoomForDeviceId(roomStat.getId());
            final String assignedRoomName = assignedRoom == null ? "" : assignedRoom.getName();
            final DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(THING_TYPE_ROOMSTAT, bridgeUID, device.getSerialNumber()))
                    .withProperties(properties).withBridge(bridgeUID).withLabel(assignedRoomName + " - Thermostat")
                    .withRepresentationProperty(device.getSerialNumber()).build();

            thingDiscovered(discoveryResult);
        }
    }

    private void onRoomAdded(final RoomDTO room) {
        final Map<String, Object> properties = new HashMap<>();

        logger.debug("Room discovered: {}", room.getName());
        properties.put("name", room.getName());
        final DiscoveryResult discoveryResult = DiscoveryResultBuilder
                .create(new ThingUID(THING_TYPE_ROOM, bridgeUID,
                        room.getName().replaceAll("[^A-Za-z0-9]", "").toLowerCase()))
                .withBridge(bridgeUID).withProperties(properties).withLabel(room.getName()).build();

        thingDiscovered(discoveryResult);
    }

    private void onSmartValveAdded(final DraytonWiserDTO domainDTOProxy, final SmartValveDTO smartValve) {
        final DeviceDTO device = domainDTOProxy.getExtendedDeviceProperties(smartValve.getId());

        if (device != null) {
            logger.debug("Smart Value discovered, serialnumber: {}", device.getSerialNumber());
            final Map<String, Object> properties = new HashMap<>();

            DraytonWiserPropertyHelper.setSmartValveProperties(device, properties);
            final RoomDTO assignedRoom = domainDTOProxy.getRoomForDeviceId(smartValve.getId());
            final String assignedRoomName = assignedRoom == null ? "" : assignedRoom.getName();

            final DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(THING_TYPE_ITRV, bridgeUID, device.getSerialNumber()))
                    .withProperties(properties).withBridge(bridgeUID).withLabel(assignedRoomName + " - TRV")
                    .withRepresentationProperty(device.getSerialNumber()).build();

            thingDiscovered(discoveryResult);
        }
    }

    private void onSmartPlugAdded(final DraytonWiserDTO domainDTOProxy, final SmartPlugDTO smartPlug) {
        final DeviceDTO device = domainDTOProxy.getExtendedDeviceProperties(smartPlug.getId());

        if (device != null) {
            logger.debug("Smart Value discovered, serialnumber: {}", device.getSerialNumber());
            final Map<String, Object> properties = new HashMap<>();

            DraytonWiserPropertyHelper.setSmartPlugProperties(device, properties, smartPlug.getName());
            final DiscoveryResult discoveryResult = DiscoveryResultBuilder
                    .create(new ThingUID(THING_TYPE_SMARTPLUG, bridgeUID,
                            smartPlug.getName().replaceAll("[^A-Za-z0-9]", "").toLowerCase()))
                    .withProperties(properties).withBridge(bridgeUID).withLabel(smartPlug.getName() + " - Smart Plug")
                    .withRepresentationProperty(device.getSerialNumber()).build();

            thingDiscovered(discoveryResult);
        }
    }

    @Override
    public synchronized void stopScan() {
        if (bridgeHandler != null) {
            bridgeHandler.unsetDiscoveryService();
        }
        super.stopScan();
    }

    @Override
    public void setThingHandler(@Nullable final ThingHandler handler) {
        if (handler instanceof HeatHubHandler) {
            bridgeHandler = (HeatHubHandler) handler;
            bridgeUID = bridgeHandler.getThing().getUID();
        } else {
            bridgeHandler = null;
            bridgeUID = null;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }
}
