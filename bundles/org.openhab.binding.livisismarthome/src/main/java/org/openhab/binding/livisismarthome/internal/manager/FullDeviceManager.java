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
package org.openhab.binding.livisismarthome.internal.manager;

import static org.openhab.binding.livisismarthome.internal.LivisiBindingConstants.BATTERY_POWERED_DEVICES;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.livisismarthome.internal.client.LivisiClient;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.link.LinkDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.location.LocationDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.message.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sven Strohschein - Initial contribution
 *
 *         (created by refactoring the LivisiClient class)
 */
@NonNullByDefault
public class FullDeviceManager {

    private final Logger logger = LoggerFactory.getLogger(FullDeviceManager.class);

    private final LivisiClient client;

    public FullDeviceManager(LivisiClient client) {
        this.client = client;
    }

    /**
     * Returns a {@link List} of all {@link DeviceDTO}s with the full configuration details, {@link CapabilityDTO}s and
     * states. Calling this may take a while...
     */
    public List<DeviceDTO> getFullDevices() throws IOException {
        final Map<String, LocationDTO> locationMap = createLocationMap(client);
        final Map<String, CapabilityDTO> capabilityMap = createCapabilityMap(client);
        final Map<String, DeviceStateDTO> deviceStateMap = createDeviceStateMap(client);
        final Map<String, List<MessageDTO>> messageMap = createMessageMap(client);

        final List<DeviceDTO> deviceList = client.getDevices(deviceStateMap.keySet());
        for (final DeviceDTO device : deviceList) {
            final String deviceId = device.getId();
            initializeDevice(device, deviceStateMap.get(deviceId), locationMap, capabilityMap,
                    getMessageList(device, messageMap));
        }
        return deviceList;
    }

    /**
     * Returns the {@link DeviceDTO} with the given deviceId with full configuration details, {@link CapabilityDTO}s and
     * states. Calling this may take a little bit longer...
     */
    public Optional<DeviceDTO> getFullDeviceById(final String deviceId, final boolean isSHCClassic) throws IOException {
        final Map<String, LocationDTO> locationMap = createLocationMap(client);
        final Map<String, CapabilityDTO> capabilityMap = createCapabilityMap(deviceId, client);
        final List<MessageDTO> messageMap = createMessageMap(deviceId, client);

        final Optional<DeviceDTO> device = client.getDeviceById(deviceId);
        if (device.isPresent()) {
            final DeviceStateDTO deviceState = new DeviceStateDTO();
            deviceState.setId(deviceId);
            deviceState.setState(client.getDeviceStateByDeviceId(deviceId, isSHCClassic));

            initializeDevice(device.get(), deviceState, locationMap, capabilityMap, messageMap);
        }
        return device;
    }

    private void initializeDevice(DeviceDTO device, @Nullable DeviceStateDTO deviceState,
            Map<String, LocationDTO> locationMap, Map<String, CapabilityDTO> capabilityMap,
            List<MessageDTO> messageList) {
        device.setDeviceState(deviceState);

        if (isBatteryPowered(device)) {
            device.setIsBatteryPowered(true);
        }

        device.setLocation(locationMap.get(device.getLocationId()));

        device.setCapabilityMap(createDeviceCapabilityMap(device, capabilityMap));

        device.setMessageList(messageList);
    }

    private static boolean isBatteryPowered(DeviceDTO device) {
        return BATTERY_POWERED_DEVICES.contains(device.getType());
    }

    private List<MessageDTO> getMessageList(DeviceDTO device, Map<String, List<MessageDTO>> messageMap) {
        return Objects.requireNonNullElse(messageMap.get(device.getId()), Collections.emptyList());
    }

    private static Map<String, LocationDTO> createLocationMap(LivisiClient client) throws IOException {
        final List<LocationDTO> locationList = client.getLocations();
        final Map<String, LocationDTO> locationMap = new HashMap<>(locationList.size());
        for (final LocationDTO location : locationList) {
            locationMap.put(location.getId(), location);
        }
        return locationMap;
    }

    private static Map<String, CapabilityStateDTO> createCapabilityStateMap(LivisiClient client) throws IOException {
        final List<CapabilityStateDTO> capabilityStateList = client.getCapabilityStates();
        final Map<String, CapabilityStateDTO> capabilityStateMap = new HashMap<>(capabilityStateList.size());
        for (final CapabilityStateDTO capabilityState : capabilityStateList) {
            capabilityStateMap.put(capabilityState.getId(), capabilityState);
        }
        return capabilityStateMap;
    }

    private static Map<String, CapabilityDTO> createCapabilityMap(LivisiClient client) throws IOException {
        final Map<String, CapabilityStateDTO> capabilityStateMap = createCapabilityStateMap(client);
        final List<CapabilityDTO> capabilityList = client.getCapabilities();

        return initializeCapabilities(capabilityStateMap, capabilityList);
    }

    private static Map<String, CapabilityDTO> createCapabilityMap(String deviceId, LivisiClient client)
            throws IOException {
        final Map<String, CapabilityStateDTO> capabilityStateMap = createCapabilityStateMap(client);
        final List<CapabilityDTO> capabilityList = client.getCapabilitiesForDevice(deviceId);

        return initializeCapabilities(capabilityStateMap, capabilityList);
    }

    private static Map<String, CapabilityDTO> initializeCapabilities(Map<String, CapabilityStateDTO> capabilityStateMap,
            List<CapabilityDTO> capabilityList) {
        final Map<String, CapabilityDTO> capabilityMap = new HashMap<>(capabilityList.size());
        for (final CapabilityDTO capability : capabilityList) {
            String capabilityId = capability.getId();

            CapabilityStateDTO capabilityState = capabilityStateMap.get(capabilityId);
            capability.setCapabilityState(capabilityState);

            capabilityMap.put(capabilityId, capability);
        }
        return capabilityMap;
    }

    private static Map<String, CapabilityDTO> createDeviceCapabilityMap(DeviceDTO device,
            Map<String, CapabilityDTO> capabilityMap) {
        final HashMap<String, CapabilityDTO> deviceCapabilityMap = new HashMap<>();
        for (final String capabilityValue : device.getCapabilities()) {
            final CapabilityDTO capability = capabilityMap.get(LinkDTO.getId(capabilityValue));
            if (capability != null) {
                final String capabilityId = capability.getId();
                deviceCapabilityMap.put(capabilityId, capability);
            }
        }
        return deviceCapabilityMap;
    }

    private static Map<String, DeviceStateDTO> createDeviceStateMap(LivisiClient client) throws IOException {
        final List<DeviceStateDTO> deviceStateList = client.getDeviceStates();
        final Map<String, DeviceStateDTO> deviceStateMap = new HashMap<>(deviceStateList.size());
        for (final DeviceStateDTO deviceState : deviceStateList) {
            deviceStateMap.put(deviceState.getId(), deviceState);
        }
        return deviceStateMap;
    }

    private List<MessageDTO> createMessageMap(String deviceId, LivisiClient client) throws IOException {
        final List<MessageDTO> messages = client.getMessages();
        final List<MessageDTO> messageList = new ArrayList<>();
        final String deviceIdPath = "/device/" + deviceId;

        for (final MessageDTO message : messages) {
            logger.trace("Message Type {} with ID {}", message.getType(), message.getId());
            if (message.getDevices() != null && !message.getDevices().isEmpty()) {
                for (final String li : message.getDevices()) {
                    if (deviceIdPath.equals(li)) {
                        messageList.add(message);
                    }
                }
            }
        }
        return messageList;
    }

    private static Map<String, List<MessageDTO>> createMessageMap(LivisiClient client) throws IOException {
        final List<MessageDTO> messageList = client.getMessages();
        final Map<String, List<MessageDTO>> deviceMessageMap = new HashMap<>();
        for (final MessageDTO message : messageList) {
            if (message.getDevices() != null && !message.getDevices().isEmpty()) {
                final String deviceId = message.getDevices().get(0).replace("/device/", "");

                // could get optimized with computeIfAbsent, but the non-null checks doesn't understand that and
                // produces compiler warnings...
                List<MessageDTO> ml = deviceMessageMap.get(deviceId);
                if (ml == null) {
                    ml = new ArrayList<>();
                    deviceMessageMap.put(deviceId, ml);
                }
                ml.add(message);
            }
        }
        return deviceMessageMap;
    }
}
