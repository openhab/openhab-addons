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
package org.openhab.binding.innogysmarthome.internal.manager;

import static org.openhab.binding.innogysmarthome.internal.InnogyBindingConstants.BATTERY_POWERED_DEVICES;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.innogysmarthome.internal.client.InnogyClient;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.CapabilityState;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.DeviceState;
import org.openhab.binding.innogysmarthome.internal.client.entity.link.Link;
import org.openhab.binding.innogysmarthome.internal.client.entity.location.Location;
import org.openhab.binding.innogysmarthome.internal.client.entity.message.Message;
import org.openhab.binding.innogysmarthome.internal.client.exception.ApiException;
import org.openhab.binding.innogysmarthome.internal.client.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sven Strohschein - Initial contribution (but only created by refactoring the InnogyClient class)
 */
@NonNullByDefault
public class FullDeviceManager {

    private final Logger logger = LoggerFactory.getLogger(FullDeviceManager.class);

    private final InnogyClient client;

    public FullDeviceManager(InnogyClient client) {
        this.client = client;
    }

    /**
     * Returns a {@link List} of all {@link Device}s with the full configuration details, {@link Capability}s and
     * states. Calling this may take a while...
     */
    public List<Device> getFullDevices() throws IOException, ApiException, AuthenticationException {
        final Map<String, Location> locationMap = createLocationMap(client);
        final Map<String, Capability> capabilityMap = createCapabilityMap(client);
        final Map<String, DeviceState> deviceStateMap = createDeviceStateMap(client);
        final Map<String, List<Message>> messageMap = createMessageMap(client);

        final List<Device> deviceList = client.getDevices(deviceStateMap.keySet());
        for (final Device device : deviceList) {
            final String deviceId = device.getId();
            initializeDevice(device, deviceStateMap.get(deviceId), locationMap, capabilityMap,
                    getMessageList(device, messageMap));
        }
        return deviceList;
    }

    /**
     * Returns the {@link Device} with the given deviceId with full configuration details, {@link Capability}s and
     * states. Calling this may take a little bit longer...
     */
    public Device getFullDeviceById(final String deviceId) throws IOException, ApiException, AuthenticationException {
        final Map<String, Location> locationMap = createLocationMap(client);
        final Map<String, Capability> capabilityMap = createCapabilityMap(deviceId, client);
        final List<Message> messageMap = createMessageMap(deviceId, client);

        final DeviceState deviceState = new DeviceState();
        deviceState.setId(deviceId);
        deviceState.setState(client.getDeviceStateByDeviceId(deviceId));

        final Device device = client.getDeviceById(deviceId);
        initializeDevice(device, deviceState, locationMap, capabilityMap, messageMap);
        return device;
    }

    private void initializeDevice(Device device, @Nullable DeviceState deviceState, Map<String, Location> locationMap,
            Map<String, Capability> capabilityMap, List<Message> messageList) {
        device.setDeviceState(deviceState);

        if (isBatteryPowered(device)) {
            device.setIsBatteryPowered(true);
        }

        device.setLocation(locationMap.get(device.getLocationId()));

        device.setCapabilityMap(createDeviceCapabilityMap(device, capabilityMap));

        device.setMessageList(messageList);
    }

    private static boolean isBatteryPowered(Device device) {
        return BATTERY_POWERED_DEVICES.contains(device.getType());
    }

    private List<Message> getMessageList(Device device, Map<String, List<Message>> messageMap) {
        return Objects.requireNonNullElse(messageMap.get(device.getId()), Collections.emptyList());
    }

    private static Map<String, Location> createLocationMap(InnogyClient client)
            throws IOException, ApiException, AuthenticationException {
        final List<Location> locationList = client.getLocations();
        final Map<String, Location> locationMap = new HashMap<>(locationList.size());
        for (final Location location : locationList) {
            locationMap.put(location.getId(), location);
        }
        return locationMap;
    }

    private static Map<String, CapabilityState> createCapabilityStateMap(InnogyClient client)
            throws IOException, ApiException, AuthenticationException {
        final List<CapabilityState> capabilityStateList = client.getCapabilityStates();
        final Map<String, CapabilityState> capabilityStateMap = new HashMap<>(capabilityStateList.size());
        for (final CapabilityState capabilityState : capabilityStateList) {
            capabilityStateMap.put(capabilityState.getId(), capabilityState);
        }
        return capabilityStateMap;
    }

    private static Map<String, Capability> createCapabilityMap(InnogyClient client)
            throws IOException, ApiException, AuthenticationException {
        final Map<String, CapabilityState> capabilityStateMap = createCapabilityStateMap(client);
        final List<Capability> capabilityList = client.getCapabilities();

        return initializeCapabilities(capabilityStateMap, capabilityList);
    }

    private static Map<String, Capability> createCapabilityMap(String deviceId, InnogyClient client)
            throws IOException, ApiException, AuthenticationException {
        final Map<String, CapabilityState> capabilityStateMap = createCapabilityStateMap(client);
        final List<Capability> capabilityList = client.getCapabilitiesForDevice(deviceId);

        return initializeCapabilities(capabilityStateMap, capabilityList);
    }

    private static Map<String, Capability> initializeCapabilities(Map<String, CapabilityState> capabilityStateMap,
            List<Capability> capabilityList) {
        final Map<String, Capability> capabilityMap = new HashMap<>(capabilityList.size());
        for (final Capability capability : capabilityList) {
            String capabilityId = capability.getId();

            CapabilityState capabilityState = capabilityStateMap.get(capabilityId);
            capability.setCapabilityState(capabilityState);

            capabilityMap.put(capabilityId, capability);
        }
        return capabilityMap;
    }

    private static Map<String, Capability> createDeviceCapabilityMap(Device device,
            Map<String, Capability> capabilityMap) {
        final HashMap<String, Capability> deviceCapabilityMap = new HashMap<>();
        for (final String capabilityValue : device.getCapabilities()) {
            final Capability capability = capabilityMap.get(Link.getId(capabilityValue));
            final String capabilityId = capability.getId();
            deviceCapabilityMap.put(capabilityId, capability);
        }
        return deviceCapabilityMap;
    }

    private static Map<String, DeviceState> createDeviceStateMap(InnogyClient client)
            throws IOException, ApiException, AuthenticationException {
        final List<DeviceState> deviceStateList = client.getDeviceStates();
        final Map<String, DeviceState> deviceStateMap = new HashMap<>(deviceStateList.size());
        for (final DeviceState deviceState : deviceStateList) {
            deviceStateMap.put(deviceState.getId(), deviceState);
        }
        return deviceStateMap;
    }

    private List<Message> createMessageMap(String deviceId, InnogyClient client)
            throws IOException, ApiException, AuthenticationException {
        final List<Message> messages = client.getMessages();
        final List<Message> messageList = new ArrayList<>();
        final String deviceIdPath = "/device/" + deviceId;

        for (final Message message : messages) {
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

    private static Map<String, List<Message>> createMessageMap(InnogyClient client)
            throws IOException, ApiException, AuthenticationException {
        final List<Message> messageList = client.getMessages();
        final Map<String, List<Message>> deviceMessageMap = new HashMap<>();
        for (final Message message : messageList) {
            if (message.getDevices() != null && !message.getDevices().isEmpty()) {
                final String deviceId = message.getDevices().get(0).replace("/device/", "");
                List<Message> ml;
                if (deviceMessageMap.containsKey(deviceId)) {
                    ml = deviceMessageMap.get(deviceId);
                } else {
                    ml = new ArrayList<>();
                }
                ml.add(message);
                deviceMessageMap.put(deviceId, ml);
            }
        }
        return deviceMessageMap;
    }
}
