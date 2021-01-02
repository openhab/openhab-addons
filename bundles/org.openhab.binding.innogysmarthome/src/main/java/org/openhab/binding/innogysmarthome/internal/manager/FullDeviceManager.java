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
package org.openhab.binding.innogysmarthome.internal.manager;

import static org.openhab.binding.innogysmarthome.internal.InnogyBindingConstants.BATTERY_POWERED_DEVICES;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            initializeDevice(device, deviceStateMap.get(device.getId()), locationMap, capabilityMap, messageMap);
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
        final Map<String, List<Message>> messageMap = createMessageMap(deviceId, client);

        final DeviceState deviceState = new DeviceState();
        deviceState.setId(deviceId);
        deviceState.setState(client.getDeviceStateByDeviceId(deviceId));

        final Device device = client.getDeviceById(deviceId);
        initializeDevice(device, deviceState, locationMap, capabilityMap, messageMap);
        return device;
    }

    private void initializeDevice(Device device, @Nullable DeviceState deviceState, Map<String, Location> locationMap,
            Map<String, Capability> capabilityMap, Map<String, List<Message>> messageMap)
            throws ApiException, IOException, AuthenticationException {
        final Map<String, CapabilityState> capabilityStateMap = createCapabilityStateMap(client);

        device.setDeviceState(deviceState);

        if (isBatteryPowered(device)) {
            device.setIsBatteryPowered(true);
        }

        device.setLocation(locationMap.get(device.getLocationId()));

        device.setCapabilityMap(createDeviceCapabilityMap(device, capabilityMap, capabilityStateMap));

        device.setMessageList(messageMap.get(device.getId()));
    }

    private static boolean isBatteryPowered(Device device) {
        return BATTERY_POWERED_DEVICES.contains(device.getType());
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
        final List<Capability> capabilityList = client.getCapabilities();
        final Map<String, Capability> capabilityMap = new HashMap<>(capabilityList.size());
        for (final Capability capability : capabilityList) {
            capabilityMap.put(capability.getId(), capability);
        }
        return capabilityMap;
    }

    private static Map<String, Capability> createCapabilityMap(String deviceId, InnogyClient client)
            throws IOException, ApiException, AuthenticationException {
        final List<Capability> capabilityList = client.getCapabilitiesForDevice(deviceId);
        final Map<String, Capability> capabilityMap = new HashMap<>(capabilityList.size());
        for (final Capability capability : capabilityList) {
            capabilityMap.put(capability.getId(), capability);
        }
        return capabilityMap;
    }

    private static Map<String, Capability> createDeviceCapabilityMap(Device device,
            Map<String, Capability> capabilityMap, Map<String, CapabilityState> capabilityStateMap) {
        final HashMap<String, Capability> deviceCapabilityMap = new HashMap<>();
        for (final String capabilityValue : device.getCapabilities()) {
            final Capability capability = capabilityMap.get(Link.getId(capabilityValue));
            final String capabilityId = capability.getId();
            final CapabilityState capabilityState = capabilityStateMap.get(capabilityId);
            capability.setCapabilityState(capabilityState); // TODO dangerous to change a state in a method called
                                                            // "create...". This should get avoided!
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

    private Map<String, List<Message>> createMessageMap(String deviceId, InnogyClient client)
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

        Map<String, List<Message>> messageMap = new HashMap<>(1);
        messageMap.put(deviceId, messageList);
        return messageMap;
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
