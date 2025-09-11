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
package org.openhab.binding.boschshc.internal.devices;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.PROPERTY_LOCATION;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.PROPERTY_LOCATION_LEGACY;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for physical Bosch devices with configurable IDs (as opposed to system services, which have static IDs).
 * <p>
 * The device ID of physical devices has to be configured in the thing configuration.
 * <p>
 * Examples for physical device IDs are:
 *
 * <pre>
 * hdm:Cameras:d20354de-44b5-3acc-924c-24c98d59da42
 * hdm:ZigBee:000d6f0016d1cdae
 * </pre>
 *
 * @author Stefan Kästle - Initial contribution
 * @author Christian Oeing - refactorings of e.g. server registration
 * @author David Pace - Handler abstraction
 *
 */
@NonNullByDefault
public abstract class BoschSHCDeviceHandler extends BoschSHCHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Bosch SHC configuration loaded from openHAB configuration.
     */
    private @Nullable BoschSHCConfiguration config;

    protected BoschSHCDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(BoschSHCConfiguration.class);

        String deviceId = config.id;
        @Nullable
        Device deviceInfo = validateDeviceId(deviceId);
        if (deviceInfo == null) {
            return;
        }

        if (!processDeviceInfo(deviceInfo)) {
            return;
        }

        super.initialize();
    }

    /**
     * Allows the handler to process the device info that was obtained from a REST
     * call to the Smart Home Controller at <code>/devices/{deviceId}</code>.
     * 
     * @param deviceInfo the device info obtained from the controller, guaranteed to be non-null
     * @return <code>true</code> if the device info is valid and the initialization should proceed, <code>false</code>
     *         otherwise
     */
    protected boolean processDeviceInfo(Device deviceInfo) {
        try {
            updateLocationPropertiesIfApplicable(deviceInfo);
        } catch (BoschSHCException e) {
            logger.warn("Error while updating location properties for thing {}.", getThing().getUID(), e);
        }
        // do not cancel thing initialization if location properties cannot be updated
        return true;
    }

    private void updateLocationPropertiesIfApplicable(Device deviceInfo) throws BoschSHCException {
        Map<String, String> thingProperties = getThing().getProperties();
        removeLegacyLocationPropertyIfApplicable(thingProperties);
        updateLocationPropertyIfApplicable(thingProperties, deviceInfo);
    }

    private void updateLocationPropertyIfApplicable(Map<String, String> thingProperties, Device deviceInfo)
            throws BoschSHCException {
        String roomName = getBridgeHandler().resolveRoomId(deviceInfo.roomId);
        if (roomName != null) {
            String currentLocation = thingProperties.get(PROPERTY_LOCATION);
            if (!roomName.equals(currentLocation)) {
                logger.debug("Updating property '{}' of thing {} to '{}'.", PROPERTY_LOCATION, getThing().getUID(),
                        roomName);
                updateProperty(PROPERTY_LOCATION, roomName);
            }
        }
    }

    private void removeLegacyLocationPropertyIfApplicable(Map<String, String> thingProperties) {
        if (thingProperties.containsKey(PROPERTY_LOCATION_LEGACY)) {
            logger.debug("Removing legacy property '{}' from thing {}.", PROPERTY_LOCATION_LEGACY, getThing().getUID());
            // null value indicates that the property should be removed
            updateProperty(PROPERTY_LOCATION_LEGACY, null);
        }
    }

    /**
     * Attempts to obtain information about the device with the specified ID via a REST call.
     * <p>
     * If the REST call is successful, the device ID is considered to be valid and the resulting {@link Device} object
     * is returned.
     * <p>
     * If the device ID is not configured/empty or the REST call is not successful, the device ID is considered invalid
     * and <code>null</code> is returned.
     * 
     * @param deviceId the device ID to check
     * @return the {@link Device} info object if the REST call was successful, <code>null</code> otherwise
     */
    @Nullable
    protected Device validateDeviceId(@Nullable String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.empty-device-id");
            return null;
        }

        // Try to get device info to make sure the device exists
        try {
            var bridgeHandler = this.getBridgeHandler();
            var deviceInfo = bridgeHandler.getDeviceInfo(deviceId);
            logger.trace("Device validated and initialized:\n{}", deviceInfo);
            return deviceInfo;
        } catch (TimeoutException | ExecutionException | BoschSHCException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }

        return null;
    }

    /**
     * Returns the unique id of the Bosch device.
     *
     * @return Unique id of the Bosch device.
     */
    @Override
    public @Nullable String getBoschID() {
        return config != null ? config.id : null;
    }
}
