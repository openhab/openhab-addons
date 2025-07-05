/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.time.Instant;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Room;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.UserDefinedState;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
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
@Component(scope = ServiceScope.PROTOTYPE, service = ThingHandlerService.class)
@NonNullByDefault
public class ThingDiscoveryService extends AbstractThingHandlerDiscoveryService<BridgeHandler> {
    private static final int SEARCH_TIME = 1;

    private final Logger logger = LoggerFactory.getLogger(ThingDiscoveryService.class);

    /**
     * Device model representing logical child devices of Light Control II
     */
    static final String DEVICE_MODEL_LIGHT_CONTROL_CHILD_DEVICE = "MICROMODULE_LIGHT_ATTACHED";

    protected static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(
            BoschSHCBindingConstants.THING_TYPE_INWALL_SWITCH, BoschSHCBindingConstants.THING_TYPE_TWINGUARD,
            BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT, BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT_2,
            BoschSHCBindingConstants.THING_TYPE_MOTION_DETECTOR, BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL,
            BoschSHCBindingConstants.THING_TYPE_THERMOSTAT, BoschSHCBindingConstants.THING_TYPE_CLIMATE_CONTROL,
            BoschSHCBindingConstants.THING_TYPE_WALL_THERMOSTAT, BoschSHCBindingConstants.THING_TYPE_CAMERA_360,
            BoschSHCBindingConstants.THING_TYPE_CAMERA_EYES,
            BoschSHCBindingConstants.THING_TYPE_INTRUSION_DETECTION_SYSTEM,
            BoschSHCBindingConstants.THING_TYPE_SMART_PLUG_COMPACT, BoschSHCBindingConstants.THING_TYPE_SMART_BULB,
            BoschSHCBindingConstants.THING_TYPE_SMOKE_DETECTOR);

    // @formatter:off
    public static final Map<String, ThingTypeUID> DEVICEMODEL_TO_THINGTYPE_MAP = Map.ofEntries(
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
            new AbstractMap.SimpleEntry<>("SWD2", BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT_2),
            new AbstractMap.SimpleEntry<>("SWD2_PLUS", BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT_2_PLUS),
            new AbstractMap.SimpleEntry<>("TRV", BoschSHCBindingConstants.THING_TYPE_THERMOSTAT),
            new AbstractMap.SimpleEntry<>("WRC2", BoschSHCBindingConstants.THING_TYPE_UNIVERSAL_SWITCH),
            new AbstractMap.SimpleEntry<>("SWITCH2", BoschSHCBindingConstants.THING_TYPE_UNIVERSAL_SWITCH_2),
            new AbstractMap.SimpleEntry<>("SMOKE_DETECTOR2", BoschSHCBindingConstants.THING_TYPE_SMOKE_DETECTOR_2),
            new AbstractMap.SimpleEntry<>("MICROMODULE_SHUTTER", BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL_2),
            new AbstractMap.SimpleEntry<>("MICROMODULE_AWNING", BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL_2),
            new AbstractMap.SimpleEntry<>("MICROMODULE_LIGHT_CONTROL", BoschSHCBindingConstants.THING_TYPE_LIGHT_CONTROL_2),
            new AbstractMap.SimpleEntry<>("MICROMODULE_DIMMER", BoschSHCBindingConstants.THING_TYPE_DIMMER),
            new AbstractMap.SimpleEntry<>("WLS", BoschSHCBindingConstants.THING_TYPE_WATER_DETECTOR),
            new AbstractMap.SimpleEntry<>("MICROMODULE_RELAY", BoschSHCBindingConstants.THING_TYPE_RELAY)
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
        super(BridgeHandler.class, SUPPORTED_THING_TYPES, SEARCH_TIME);
    }

    @Override
    public void initialize() {
        logger.trace("initialize");
        thingHandler.registerDiscoveryListener(this);
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        logger.trace("dispose");
        removeOlderResults(Instant.now(), thingHandler.getThing().getUID());
        thingHandler.unregisterDiscoveryListener();

        super.deactivate();
    }

    @Override
    protected void startScan() {
        try {
            doScan();
        } catch (InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop manual scan on bridge {}", thingHandler.getThing().getUID());
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan(), thingHandler.getThing().getUID());
    }

    public void doScan() throws InterruptedException {
        logger.debug("Start manual scan on bridge {}", thingHandler.getThing().getUID());
        // use shcBridgeHandler to getDevices()
        List<Room> rooms = thingHandler.getRooms();
        logger.debug("SHC has {} rooms", rooms.size());
        List<Device> devices = thingHandler.getDevices();
        logger.debug("SHC has {} devices", devices.size());
        List<UserDefinedState> userStates = thingHandler.getUserStates();
        logger.debug("SHC has {} user-defined states", userStates.size());

        // Write found devices into openhab.log to support manual configuration
        for (Device d : devices) {
            logger.debug("Found device: name={} id={}", d.name, d.id);
            if (d.deviceServiceIds != null) {
                for (String s : d.deviceServiceIds) {
                    logger.debug(".... service: {}", s);
                }
            }
        }
        for (UserDefinedState userState : userStates) {
            logger.debug("Found user-defined state: name={} id={} state={}", userState.getName(), userState.getId(),
                    userState.isState());
        }

        addDevices(devices, rooms);
        addUserStates(userStates);
    }

    protected void addUserStates(List<UserDefinedState> userStates) {
        for (UserDefinedState userState : userStates) {
            addUserState(userState);
        }
    }

    private void addUserState(UserDefinedState userState) {
        logger.trace("Discovering user-defined state {}", userState.getName());
        logger.trace("- details: id {}, state {}", userState.getId(), userState.isState());

        ThingTypeUID thingTypeUID = new ThingTypeUID(BoschSHCBindingConstants.BINDING_ID,
                BoschSHCBindingConstants.THING_TYPE_USER_DEFINED_STATE.getId());

        logger.trace("- got thingTypeID '{}' for user-defined state '{}'", thingTypeUID.getId(), userState.getName());

        ThingUID thingUID = new ThingUID(thingTypeUID, thingHandler.getThing().getUID(),
                userState.getId().replace(':', '_'));

        logger.trace("- got thingUID '{}' for user-defined state: '{}'", thingUID, userState);

        DiscoveryResultBuilder discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperty("id", userState.getId()).withLabel(userState.getName());

        discoveryResult.withBridge(thingHandler.getThing().getUID());

        thingDiscovered(discoveryResult.build());

        logger.debug("Discovered user-defined state '{}' with thingTypeUID={}, thingUID={}, id={}, state={}",
                userState.getName(), thingUID, thingTypeUID, userState.getId(), userState.isState());
    }

    protected void addDevices(List<Device> devices, List<Room> rooms) {
        for (Device device : devices) {
            addDevice(device, getRoomNameForDevice(device, rooms));
        }
    }

    protected String getRoomNameForDevice(Device device, List<Room> rooms) {
        return Objects.requireNonNull(
                rooms.stream().filter(room -> room.id.equals(device.roomId)).findAny().map(r -> r.name).orElse(""));
    }

    protected void addDevice(Device device, String roomName) {
        // see startScan for the runtime null check of shcBridgeHandler
        logger.trace("Discovering device {}", device.name);
        logger.trace("- details: id {}, roomId {}, deviceModel {}", device.id, device.roomId, device.deviceModel);

        ThingTypeUID thingTypeUID = getThingTypeUID(device);
        if (thingTypeUID == null) {
            return;
        }

        logger.trace("- got thingTypeID '{}' for deviceModel '{}'", thingTypeUID.getId(), device.deviceModel);

        ThingUID thingUID = new ThingUID(thingTypeUID, thingHandler.getThing().getUID(),
                buildCompliantThingID(device.id));

        logger.trace("- got thingUID '{}' for device: '{}'", thingUID, device);

        DiscoveryResultBuilder discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperty("id", device.id).withLabel(getNiceName(device.name, roomName));
        discoveryResult.withBridge(thingHandler.getThing().getUID());

        if (!roomName.isEmpty()) {
            discoveryResult.withProperty(BoschSHCBindingConstants.PROPERTY_LOCATION, roomName);
        }
        thingDiscovered(discoveryResult.build());

        logger.debug("Discovered device '{}' with thingTypeUID={}, thingUID={}, id={}, deviceModel={}", device.name,
                thingUID, thingTypeUID, device.id, device.deviceModel);
    }

    /**
     * Translates a Bosch device ID to an openHAB-compliant thing ID.
     * <p>
     * Characters that are not allowed in thing IDs are replaced by underscores.
     * 
     * @param deviceId the Bosch device ID
     * @return the translated openHAB-compliant thing ID
     */
    private String buildCompliantThingID(String deviceId) {
        return deviceId.replace(':', '_').replace('#', '_');
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

        if (DEVICE_MODEL_LIGHT_CONTROL_CHILD_DEVICE.equals(device.deviceModel)) {
            // Light Control II exposes a parent device and two child devices.
            // We only add one thing for the parent device and the child devices are logically included.
            // Therefore we do not need to add separate things for the child devices and need to suppress the
            // log entry about the unknown device model.
            return null;
        }

        logger.debug("Unknown deviceModel '{}'! Please create a support request issue for this unknown device model.",
                device.deviceModel);
        return null;
    }
}
