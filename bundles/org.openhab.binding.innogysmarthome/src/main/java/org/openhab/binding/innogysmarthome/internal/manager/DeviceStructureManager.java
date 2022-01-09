/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.innogysmarthome.internal.InnogyBindingConstants;
import org.openhab.binding.innogysmarthome.internal.client.InnogyClient;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.link.Link;
import org.openhab.binding.innogysmarthome.internal.client.entity.message.Message;
import org.openhab.binding.innogysmarthome.internal.client.exception.ApiException;
import org.openhab.binding.innogysmarthome.internal.client.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the structure of the {@link Device}s and the calls to the {@link InnogyClient} to load the {@link Device}
 * data from the innogy SmartHome web service.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
@NonNullByDefault
public class DeviceStructureManager {

    private final Logger logger = LoggerFactory.getLogger(DeviceStructureManager.class);

    private final FullDeviceManager deviceManager;
    private final Map<String, Device> deviceMap;
    private final Map<String, Device> capabilityIdToDeviceMap;
    private String bridgeDeviceId = "";

    /**
     * Constructs the {@link DeviceStructureManager}.
     *
     * @param deviceManager the {@link FullDeviceManager}
     */
    public DeviceStructureManager(FullDeviceManager deviceManager) {
        this.deviceManager = deviceManager;
        deviceMap = Collections.synchronizedMap(new HashMap<>());
        capabilityIdToDeviceMap = new ConcurrentHashMap<>();
    }

    /**
     * Returns the {@link #deviceMap}, a map with the device id and the device.
     *
     * @return
     */
    public Map<String, Device> getDeviceMap() {
        return deviceMap;
    }

    /**
     * Loads all device data from the bridge and stores the {@link Device}s and their states in the
     * {@link DeviceStructureManager}.
     *
     * @throws IOException
     * @throws ApiException
     * @throws AuthenticationException
     */
    public void refreshDevices() throws IOException, ApiException, AuthenticationException {
        deviceMap.clear();
        capabilityIdToDeviceMap.clear();
        List<Device> devices = deviceManager.getFullDevices();
        for (Device d : devices) {
            handleRefreshedDevice(d);
        }
    }

    /**
     * Refreshs the {@link Device} with the given id and stores it in the {@link DeviceStructureManager}.
     *
     * @param deviceId
     * @throws IOException
     * @throws ApiException
     * @throws AuthenticationException
     */
    public void refreshDevice(String deviceId) throws IOException, ApiException, AuthenticationException {
        logger.trace("Refreshing Device with id '{}'", deviceId);
        Device d = deviceManager.getFullDeviceById(deviceId);
        handleRefreshedDevice(d);
    }

    /**
     * Stores the newly refreshed {@link Device} in the {@link DeviceStructureManager} structure and logs the
     * {@link Device}s details and state, if the debug logging is enabled.
     *
     * @param d the {@link Device}
     */
    private void handleRefreshedDevice(Device d) {
        if (InnogyBindingConstants.SUPPORTED_DEVICES.contains(d.getType())) {
            addDeviceToStructure(d);
        } else {
            logger.debug("Device {}:'{}' by {} ({}) ignored - UNSUPPORTED.", d.getType(), d.getConfig().getName(),
                    d.getManufacturer(), d.getId());
            logger.debug("====================================");
            return;
        }

        if (d.isController()) {
            bridgeDeviceId = d.getId();
        }

        if (logger.isDebugEnabled()) {
            try {
                logger.debug("Device {}:'{}@{}' by {} ({}) loaded.", d.getType(), d.getConfig().getName(),
                        d.getLocation() != null ? d.getLocation().getName() : "<none>", d.getManufacturer(), d.getId());
                for (Capability c : d.getCapabilityMap().values()) {
                    logger.debug("> CAP: {}/{} ({})", c.getType(), c.getName(), c.getId());
                    if (d.isRadioDevice() && !d.isReachable()) {
                        logger.debug(">> CAP-State: unknown (device NOT REACHABLE).");
                    } else {
                        if (!c.hasState()) {
                            logger.debug(">> CAP-State: unknown (NULL)");
                        }
                    }
                }
            } catch (RuntimeException e) {
                logger.debug("Error during logging: ", e);
            }
            logger.debug("====================================");
        }
    }

    /**
     * Adds the {@link Device} to the structure.
     *
     * @param device
     */
    public void addDeviceToStructure(Device device) {
        if (device.getId() != null) {
            getDeviceMap().put(device.getId(), device);
        }

        for (String cl : device.getCapabilities()) {
            capabilityIdToDeviceMap.put(Link.getId(cl), device);
        }
    }

    /**
     * Returns the {@link Device} with the given id.
     *
     * @param id
     * @return the {@link Device} or null, if it does not exist
     */
    public @Nullable Device getDeviceById(String id) {
        logger.debug("getDeviceById {}:{}", id, getDeviceMap().containsKey(id));
        return getDeviceMap().get(id);
    }

    /**
     * Returns the {@link Device}, that provides the given capability.
     *
     * @param capabilityId
     * @return {@link Device} or null
     */
    public @Nullable Device getDeviceByCapabilityId(String capabilityId) {
        return capabilityIdToDeviceMap.get(capabilityId);
    }

    /**
     * Returns the bridge {@link Device}.
     *
     * @return
     */
    public @Nullable Device getBridgeDevice() {
        return getDeviceMap().get(bridgeDeviceId);
    }

    /**
     * Returns a {@link Collection} of all {@link Device}s handled by the {@link DeviceStructureManager}.
     *
     * @return
     */
    public Collection<Device> getDeviceList() {
        return Collections.unmodifiableCollection(getDeviceMap().values());
    }

    /**
     * Returns the {@link Device}, that has the {@link Message} with the given messageId.
     *
     * @param messageId the id of the {@link Message}
     * @return the {@link Device} or null if none found
     */
    public @Nullable Device getDeviceWithMessageId(String messageId) {
        logger.trace("Getting Device with MessageId '{}'", messageId);
        for (Device d : getDeviceMap().values()) {
            if (d.hasMessages()) {
                for (Message m : d.getMessageList()) {
                    if (messageId.equals(m.getId())) {
                        return d;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the id of the {@link Capability} for {@link Device} with the given id and the given capabilityType.
     *
     * @param deviceId
     * @param capabilityType
     * @return the id of the found {@link Capability} or null
     */
    public @Nullable String getCapabilityId(String deviceId, String capabilityType) {
        Device device = getDeviceMap().get(deviceId);
        for (Capability c : device.getCapabilityMap().values()) {
            if (c.getType().equals(capabilityType)) {
                return c.getId();
            }
        }
        return null;
    }
}
