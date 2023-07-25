/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.discovery;

import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Room;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThingDiscoveryService} is responsible to discover Bosch Smart Home things.
 * The paired SHC BridgeHandler is required to get the lists of rooms and devices.
 * With this data the openhab things are discovered.
 *
 * The order to make this work is
 * 1. SHC bridge is created, e.v via openhab UI
 * 2. Service is instantiated setBridgeHandler of this service is called
 * 3. Service is activated
 * 4. Service registers itself as discoveryLister at the bridge
 * 5. bridge calls startScan after bridge is paired and things can be discovered
 *
 * @author Gerd Zanker - Initial contribution
 */
@NonNullByDefault
public class ThingDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private static final int SEARCH_TIME = 1;

    private final Logger logger = LoggerFactory.getLogger(ThingDiscoveryService.class);
    private @Nullable BridgeHandler shcBridgeHandler;

    protected static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(
            BoschSHCBindingConstants.THING_TYPE_INWALL_SWITCH, BoschSHCBindingConstants.THING_TYPE_TWINGUARD,
            BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT, BoschSHCBindingConstants.THING_TYPE_MOTION_DETECTOR,
            BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL, BoschSHCBindingConstants.THING_TYPE_THERMOSTAT,
            BoschSHCBindingConstants.THING_TYPE_CLIMATE_CONTROL, BoschSHCBindingConstants.THING_TYPE_WALL_THERMOSTAT,
            BoschSHCBindingConstants.THING_TYPE_CAMERA_360, BoschSHCBindingConstants.THING_TYPE_CAMERA_EYES,
            BoschSHCBindingConstants.THING_TYPE_INTRUSION_DETECTION_SYSTEM,
            BoschSHCBindingConstants.THING_TYPE_SMART_PLUG_COMPACT, BoschSHCBindingConstants.THING_TYPE_SMART_BULB,
            BoschSHCBindingConstants.THING_TYPE_SMOKE_DETECTOR);

    // @formatter:off
    protected static final Map<String, ThingTypeUID> DEVICEMODEL_TO_THINGTYPE_MAP = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("BBL", BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL),
            new AbstractMap.SimpleEntry<>("TWINGUARD", BoschSHCBindingConstants.THING_TYPE_TWINGUARD),
            new AbstractMap.SimpleEntry<>("BSM", BoschSHCBindingConstants.THING_TYPE_INWALL_SWITCH),
            new AbstractMap.SimpleEntry<>("PSM", BoschSHCBindingConstants.THING_TYPE_SMART_PLUG_COMPACT),
            new AbstractMap.SimpleEntry<>("PLUG_COMPACT", BoschSHCBindingConstants.THING_TYPE_SMART_PLUG_COMPACT),
            new AbstractMap.SimpleEntry<>("CAMERA_360", BoschSHCBindingConstants.THING_TYPE_CAMERA_360),
            new AbstractMap.SimpleEntry<>("CAMERA_EYES", BoschSHCBindingConstants.THING_TYPE_CAMERA_EYES),
            new AbstractMap.SimpleEntry<>("BWTH", BoschSHCBindingConstants.THING_TYPE_WALL_THERMOSTAT), // wall thermostat
            new AbstractMap.SimpleEntry<>("THB", BoschSHCBindingConstants.THING_TYPE_WALL_THERMOSTAT), // wall thermostat with batteries
            new AbstractMap.SimpleEntry<>("SD", BoschSHCBindingConstants.THING_TYPE_SMOKE_DETECTOR),
            new AbstractMap.SimpleEntry<>("MD", BoschSHCBindingConstants.THING_TYPE_MOTION_DETECTOR),
            new AbstractMap.SimpleEntry<>("ROOM_CLIMATE_CONTROL", BoschSHCBindingConstants.THING_TYPE_CLIMATE_CONTROL),
            new AbstractMap.SimpleEntry<>("INTRUSION_DETECTION_SYSTEM", BoschSHCBindingConstants.THING_TYPE_INTRUSION_DETECTION_SYSTEM),
            new AbstractMap.SimpleEntry<>("HUE_LIGHT", BoschSHCBindingConstants.THING_TYPE_SMART_BULB),
            new AbstractMap.SimpleEntry<>("LEDVANCE_LIGHT", BoschSHCBindingConstants.THING_TYPE_SMART_BULB),
            new AbstractMap.SimpleEntry<>("SWD", BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT),
            new AbstractMap.SimpleEntry<>("TRV", BoschSHCBindingConstants.THING_TYPE_THERMOSTAT)
// Future Extension: map deviceModel names to BoschSHC Thing Types when they are supported
//            new AbstractMap.SimpleEntry<>("SMOKE_DETECTION_SYSTEM", BoschSHCBindingConstants.),
//            new AbstractMap.SimpleEntry<>("PRESENCE_SIMULATION_SERVICE", BoschSHCBindingConstants.),
//            new AbstractMap.SimpleEntry<>("VENTILATION_SERVICE", BoschSHCBindingConstants.),
//            new AbstractMap.SimpleEntry<>("HUE_BRIDGE", BoschSHCBindingConstants.)
//            new AbstractMap.SimpleEntry<>("HUE_BRIDGE_MANAGER*", BoschSHCBindingConstants.)
//            new AbstractMap.SimpleEntry<>("HUE_LIGHT_ROOM_CONTROL", BoschSHCBindingConstants.)
            );
    // @formatter:on

    public ThingDiscoveryService() {
        super(SUPPORTED_THING_TYPES, SEARCH_TIME);
    }

    @Override
    public void activate() {
        logger.trace("activate");
        final BridgeHandler handler = shcBridgeHandler;
        if (handler != null) {
            handler.registerDiscoveryListener(this);
        }
    }

    @Override
    public void deactivate() {
        logger.trace("deactivate");
        final BridgeHandler handler = shcBridgeHandler;
        if (handler != null) {
            removeOlderResults(new Date().getTime(), handler.getThing().getUID());
            handler.unregisterDiscoveryListener();
        }

        super.deactivate();
    }

    @Override
    protected void startScan() {
        if (shcBridgeHandler == null) {
            logger.debug("The shcBridgeHandler is empty, no manual scan is currently possible");
            return;
        }

        try {
            doScan();
        } catch (InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop manual scan on bridge {}",
                shcBridgeHandler != null ? shcBridgeHandler.getThing().getUID() : "?");
        super.stopScan();
        final BridgeHandler handler = shcBridgeHandler;
        if (handler != null) {
            removeOlderResults(getTimestampOfLastScan(), handler.getThing().getUID());
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof BridgeHandler bridgeHandler) {
            logger.trace("Set bridge handler {}", handler);
            shcBridgeHandler = bridgeHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return shcBridgeHandler;
    }

    public void doScan() throws InterruptedException {
        logger.debug("Start manual scan on bridge {}", shcBridgeHandler.getThing().getUID());
        // use shcBridgeHandler to getDevices()
        List<Room> rooms = shcBridgeHandler.getRooms();
        logger.debug("SHC has {} rooms", rooms.size());
        List<Device> devices = shcBridgeHandler.getDevices();
        logger.debug("SHC has {} devices", devices.size());

        // Write found devices into openhab.log to support manual configuration
        for (Device d : devices) {
            logger.debug("Found device: name={} id={}", d.name, d.id);
            if (d.deviceServiceIds != null) {
                for (String s : d.deviceServiceIds) {
                    logger.debug(".... service: {}", s);
                }
            }
        }

        addDevices(devices, rooms);
    }

    protected void addDevices(List<Device> devices, List<Room> rooms) {
        for (Device device : devices) {
            addDevice(device, getRoomNameForDevice(device, rooms));
        }
    }

    protected String getRoomNameForDevice(Device device, List<Room> rooms) {
        return rooms.stream().filter(room -> room.id.equals(device.roomId)).findAny().map(r -> r.name).orElse("");
    }

    protected void addDevice(Device device, String roomName) {
        // see startScan for the runtime null check of shcBridgeHandler
        assert shcBridgeHandler != null;

        logger.trace("Discovering device {}", device.name);
        logger.trace("- details: id {}, roomId {}, deviceModel {}", device.id, device.roomId, device.deviceModel);

        ThingTypeUID thingTypeUID = getThingTypeUID(device);
        if (thingTypeUID == null) {
            return;
        }

        logger.trace("- got thingTypeID '{}' for deviceModel '{}'", thingTypeUID.getId(), device.deviceModel);

        ThingUID thingUID = new ThingUID(thingTypeUID, shcBridgeHandler.getThing().getUID(),
                device.id.replace(':', '_'));

        logger.trace("- got thingUID '{}' for device: '{}'", thingUID, device);

        DiscoveryResultBuilder discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperty("id", device.id).withLabel(getNiceName(device.name, roomName));
        if (null != shcBridgeHandler) {
            discoveryResult.withBridge(shcBridgeHandler.getThing().getUID());
        }
        if (!roomName.isEmpty()) {
            discoveryResult.withProperty("Location", roomName);
        }
        thingDiscovered(discoveryResult.build());

        logger.debug("Discovered device '{}' with thingTypeUID={}, thingUID={}, id={}, deviceModel={}", device.name,
                thingUID, thingTypeUID, device.id, device.deviceModel);
    }

    private String getNiceName(String name, String roomName) {
        if (!name.startsWith("-")) {
            return name;
        }

        // convert "-IntrusionDetectionSystem-" into "Intrusion Detection System"
        // convert "-RoomClimateControl-" into "Room Climate Control myRoomName"
        final char[] chars = name.toCharArray();
        StringBuilder niceNameBuilder = new StringBuilder(32);
        for (int pos = 0; pos < chars.length; pos++) {
            // skip "-"
            if (chars[pos] == '-') {
                continue;
            }
            // convert "CamelCase" into "Camel Case", skipping the first Uppercase after the "-"
            if (pos > 1 && Character.getType(chars[pos]) == Character.UPPERCASE_LETTER) {
                niceNameBuilder.append(" ");
            }
            niceNameBuilder.append(chars[pos]);
        }
        // append roomName for "Room Climate Control", because it appears for each room with a thermostat
        if (!roomName.isEmpty() && niceNameBuilder.toString().startsWith("Room Climate Control")) {
            niceNameBuilder.append(" ").append(roomName);
        }
        return niceNameBuilder.toString();
    }

    protected @Nullable ThingTypeUID getThingTypeUID(Device device) {
        @Nullable
        ThingTypeUID thingTypeId = DEVICEMODEL_TO_THINGTYPE_MAP.get(device.deviceModel);
        if (thingTypeId != null) {
            return new ThingTypeUID(BoschSHCBindingConstants.BINDING_ID, thingTypeId.getId());
        }
        logger.debug("Unknown deviceModel '{}'! Please create a support request issue for this unknown device model.",
                device.deviceModel);
        return null;
    }
}
