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
package org.openhab.binding.digitalstrom.internal.discovery;

import static org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants.BINDING_ID;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants;
import org.openhab.binding.digitalstrom.internal.handler.BridgeHandler;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.GeneralDeviceInformation;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputModeEnum;
import org.openhab.binding.digitalstrom.internal.providers.DsDeviceThingTypeProvider;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceDiscoveryService} discovers all digitalSTROM-Devices, of one supported device-color-type. The
 * device-color-type has to be given to the {@link #DeviceDiscoveryService(BridgeHandler, ThingTypeUID)} as
 * {@link ThingTypeUID}. The supported {@link ThingTypeUID} can be found at
 * {@link DeviceHandler#SUPPORTED_THING_TYPES}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DeviceDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(DeviceDiscoveryService.class);

    private final BridgeHandler bridgeHandler;
    private final String deviceType;
    private final ThingUID bridgeUID;

    public static final int TIMEOUT = 10;

    /**
     * Creates a new {@link DeviceDiscoveryService} for the given supported {@link ThingTypeUID}.
     *
     * @param bridgeHandler (must not be null)
     * @param supportedThingType (must not be null)
     * @throws IllegalArgumentException see {@link AbstractDiscoveryService#AbstractDiscoveryService(int)}
     */
    public DeviceDiscoveryService(BridgeHandler bridgeHandler, ThingTypeUID supportedThingType)
            throws IllegalArgumentException {
        super(new HashSet<>(Arrays.asList(supportedThingType)), TIMEOUT, true);
        this.deviceType = supportedThingType.getId();
        this.bridgeHandler = bridgeHandler;
        bridgeUID = bridgeHandler.getThing().getUID();
    }

    /**
     * Deactivates the {@link DeviceDiscoveryService} and removes the {@link DiscoveryResult}s.
     */
    @Override
    public void deactivate() {
        logger.debug("deactivate discovery service for device type {} thing types are: {}", deviceType,
                super.getSupportedThingTypes().toString());
        removeOlderResults(new Date().getTime());
    }

    @Override
    protected void startScan() {
        if (bridgeHandler != null) {
            if (!DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString().equals(deviceType)) {
                List<Device> devices = bridgeHandler.getDevices();
                if (devices != null) {
                    for (Device device : devices) {
                        onDeviceAddedInternal(device);
                    }
                }
            } else {
                List<Circuit> circuits = bridgeHandler.getCircuits();
                if (circuits != null) {
                    for (Circuit circuit : circuits) {
                        onDeviceAddedInternal(circuit);
                    }
                }
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    private void onDeviceAddedInternal(GeneralDeviceInformation device) {
        boolean isSupported = false;
        if (device instanceof Device) {
            Device tempDevice = (Device) device;
            if ((tempDevice.isSensorDevice() && deviceType.equals(tempDevice.getHWinfo().replaceAll("-", "")))
                    || (deviceType.equals(tempDevice.getHWinfo().substring(0, 2))
                            && (tempDevice.isDeviceWithOutput() || tempDevice.isBinaryInputDevice())
                            && tempDevice.isPresent())) {
                isSupported = true;
            }
        } else if (device instanceof Circuit
                && DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString().equals(deviceType)) {
            isSupported = true;
        }
        if (isSupported) {
            ThingUID thingUID = getThingUID(device);
            if (thingUID != null) {
                Map<String, Object> properties = new HashMap<>(1);
                properties.put(DigitalSTROMBindingConstants.DEVICE_DSID, device.getDSID().getValue());
                String deviceName = device.getName();
                if (deviceName == null || deviceName.isBlank()) {
                    // if no name is set, the dSID will be used as name
                    deviceName = device.getDSID().getValue();
                }
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(bridgeUID).withLabel(deviceName).build();

                thingDiscovered(discoveryResult);
            } else {
                if (device instanceof Device) {
                    logger.debug("Discovered unsupported device hardware type '{}' with uid {}",
                            ((Device) device).getHWinfo(), device.getDSUID());
                }
            }
        } else {
            if (device instanceof Device) {
                logger.debug(
                        "Discovered device with disabled or no output mode. Device was not added to inbox. "
                                + "Device information: hardware info: {}, dSUID: {}, device-name: {}, output value: {}",
                        ((Device) device).getHWinfo(), device.getDSUID(), device.getName(),
                        ((Device) device).getOutputMode());
            }
        }
    }

    private ThingUID getThingUID(GeneralDeviceInformation device) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = null;
        if (device instanceof Device) {
            Device tempDevice = (Device) device;
            thingTypeUID = new ThingTypeUID(BINDING_ID, tempDevice.getHWinfo().substring(0, 2));
            if (tempDevice.isSensorDevice() && deviceType.equals(tempDevice.getHWinfo().replaceAll("-", ""))) {
                thingTypeUID = new ThingTypeUID(BINDING_ID, deviceType);
            }
        } else {
            thingTypeUID = new ThingTypeUID(BINDING_ID,
                    DsDeviceThingTypeProvider.SupportedThingTypes.circuit.toString());
        }
        if (getSupportedThingTypes().contains(thingTypeUID)) {
            String thingDeviceId = device.getDSID().toString();
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, thingDeviceId);
            return thingUID;
        } else {
            return null;
        }
    }

    /**
     * Removes the {@link Thing} of the given {@link Device}.
     *
     * @param device (must not be null)
     */
    public void onDeviceRemoved(GeneralDeviceInformation device) {
        ThingUID thingUID = getThingUID(device);

        if (thingUID != null) {
            thingRemoved(thingUID);
        }
    }

    /**
     * Creates a {@link DiscoveryResult} for the given {@link Device}, if the {@link Device} is supported and the
     * {@link Device#getOutputMode()} is unequal {@link OutputModeEnum#DISABLED}.
     *
     * @param device (must not be null)
     */
    public void onDeviceAdded(GeneralDeviceInformation device) {
        if (super.isBackgroundDiscoveryEnabled()) {
            onDeviceAddedInternal(device);
        }
    }

    /**
     * Returns the ID of this {@link DeviceDiscoveryService}.
     *
     * @return id of the service
     */
    public String getID() {
        return deviceType;
    }
}
