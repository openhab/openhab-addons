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
package org.openhab.binding.livisismarthome.internal.manager;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.livisismarthome.internal.LivisiBindingConstants;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.link.LinkDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.message.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the structure of the {@link DeviceDTO}s and the calls to the
 * {@link org.openhab.binding.livisismarthome.internal.client.LivisiClient} to load the {@link DeviceDTO}
 * data from the LIVISI SmartHome web service.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
@NonNullByDefault
public class DeviceStructureManager {

    private final Logger logger = LoggerFactory.getLogger(DeviceStructureManager.class);

    private final FullDeviceManager deviceManager;
    private final Map<String, DeviceDTO> deviceMap = new ConcurrentHashMap<>();
    private final Map<String, DeviceDTO> capabilityIdToDeviceMap = new ConcurrentHashMap<>();
    private String bridgeDeviceId = "";

    /**
     * Constructs the {@link DeviceStructureManager}.
     *
     * @param deviceManager the {@link FullDeviceManager}
     */
    public DeviceStructureManager(FullDeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    /**
     * Returns the {@link #deviceMap}, a map with the device id and the device.
     *
     * @return map of device id and device
     */
    public Map<String, DeviceDTO> getDeviceMap() {
        return deviceMap;
    }

    /**
     * Loads all device data from the bridge and stores the {@link DeviceDTO}s and their states in the
     * {@link DeviceStructureManager}.
     */
    public void refreshDevices() throws IOException {
        deviceMap.clear();
        capabilityIdToDeviceMap.clear();
        List<DeviceDTO> devices = deviceManager.getFullDevices();
        for (DeviceDTO d : devices) {
            handleRefreshedDevice(d);
        }
    }

    /**
     * Refreshs the {@link DeviceDTO} with the given id and stores it in the {@link DeviceStructureManager}.
     *
     * @param deviceId device id
     */
    public Optional<DeviceDTO> refreshDevice(final String deviceId, final boolean isSHCClassic) throws IOException {
        logger.trace("Refreshing Device with id '{}'", deviceId);
        Optional<DeviceDTO> device = deviceManager.getFullDeviceById(deviceId, isSHCClassic);
        device.ifPresent(this::handleRefreshedDevice);
        return device;
    }

    /**
     * Stores the newly refreshed {@link DeviceDTO} in the {@link DeviceStructureManager} structure and logs the
     * {@link DeviceDTO}s details and state, if the debug logging is enabled.
     *
     * @param d the {@link DeviceDTO}
     */
    private void handleRefreshedDevice(DeviceDTO d) {
        if (LivisiBindingConstants.SUPPORTED_DEVICES.contains(d.getType())) {
            addDeviceToStructure(d);

            if (d.isController()) {
                bridgeDeviceId = d.getId();
            }
        } else {
            logger.debug("Device {}:'{}' by {} ({}) ignored - UNSUPPORTED.", d.getType(), d.getConfig().getName(),
                    d.getManufacturer(), d.getId());
            logger.debug("====================================");
        }
    }

    /**
     * Adds the {@link DeviceDTO} to the structure.
     *
     * @param device device
     */
    private void addDeviceToStructure(DeviceDTO device) {
        if (device.getId() != null) {
            getDeviceMap().put(device.getId(), device);
        }

        for (String capability : device.getCapabilities()) {
            capabilityIdToDeviceMap.put(LinkDTO.getId(capability), device);
        }

        logDeviceLoaded(device);
    }

    /**
     * Returns the {@link DeviceDTO} with the given id.
     *
     * @param id device id
     * @return the {@link DeviceDTO} or null, if it does not exist
     */
    public Optional<DeviceDTO> getDeviceById(String id) {
        logger.debug("getDeviceById {}:{}", id, getDeviceMap().containsKey(id));
        return Optional.ofNullable(getDeviceMap().get(id));
    }

    /**
     * Returns the {@link DeviceDTO}, that provides the given capability.
     *
     * @param capabilityId capability id
     * @return {@link DeviceDTO} or null
     */
    public Optional<DeviceDTO> getDeviceByCapabilityId(String capabilityId) {
        return Optional.ofNullable(capabilityIdToDeviceMap.get(capabilityId));
    }

    /**
     * Returns the bridge {@link DeviceDTO}.
     *
     * @return bridge device
     */
    public Optional<DeviceDTO> getBridgeDevice() {
        return Optional.ofNullable(getDeviceMap().get(bridgeDeviceId));
    }

    /**
     * Returns a {@link Collection} of all {@link DeviceDTO}s handled by the {@link DeviceStructureManager}.
     *
     * @return devices
     */
    public Collection<DeviceDTO> getDeviceList() {
        return Collections.unmodifiableCollection(getDeviceMap().values());
    }

    /**
     * Returns the {@link DeviceDTO}, that has the {@link MessageDTO} with the given messageId.
     *
     * @param messageId the id of the {@link MessageDTO}
     * @return the {@link DeviceDTO} or null if none found
     */
    public Optional<DeviceDTO> getDeviceWithMessageId(String messageId) {
        logger.trace("Getting Device with MessageId '{}'", messageId);
        for (DeviceDTO d : getDeviceMap().values()) {
            if (d.hasMessages()) {
                for (MessageDTO m : d.getMessageList()) {
                    if (messageId.equals(m.getId())) {
                        return Optional.of(d);
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the id of the {@link CapabilityDTO} for {@link DeviceDTO} with the given id and the given capabilityType.
     *
     * @param deviceId device id
     * @param capabilityType capability type
     * @return the id of the found {@link CapabilityDTO} or null
     */
    public Optional<String> getCapabilityId(String deviceId, String capabilityType) {
        DeviceDTO device = getDeviceMap().get(deviceId);
        if (device != null) {
            for (CapabilityDTO c : device.getCapabilityMap().values()) {
                if (c.getType().equals(capabilityType)) {
                    return Optional.of(c.getId());
                }
            }
        }
        return Optional.empty();
    }

    private void logDeviceLoaded(DeviceDTO device) {
        if (logger.isDebugEnabled()) {
            String location = device.getLocationName();
            logger.debug("Device {}:'{}@{}' by {} ({}) loaded.", device.getType(), device.getConfig().getName(),
                    location, device.getManufacturer(), device.getId());
            for (CapabilityDTO c : device.getCapabilityMap().values()) {
                logger.debug("> CAP: {}/{} ({})", c.getType(), c.getName(), c.getId());
                if (device.isRadioDevice() && device.isReachable() != null && !device.isReachable()) {
                    logger.debug(">> CAP-State: unknown (device NOT REACHABLE).");
                } else {
                    if (!c.hasState()) {
                        logger.debug(">> CAP-State: unknown (NULL)");
                    }
                }
            }
            logger.debug("====================================");
        }
    }
}
