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
package org.openhab.binding.panasonictv.internal.discovery;

import static org.openhab.binding.panasonictv.internal.PanasonicTvBindingConstants.THING_TYPE_PANASONICTV;

import java.net.URL;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.*;
import org.jupnp.model.types.DeviceType;
import org.jupnp.model.types.UDN;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link DeviceInformation} is a wrapper for device information
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DeviceInformation {
    public @Nullable ThingUID thingUID;
    public String manufacturer;
    public String serviceType;

    @Override
    public String toString() {
        return "DeviceInformation{" + "thingUID=" + thingUID + ", manufacturer='" + manufacturer + '\''
                + ", serviceType='" + serviceType + '\'' + ", udn='" + udn + '\'' + ", host='" + host + '\''
                + ", friendlyName='" + friendlyName + '\'' + ", modelName='" + modelName + '\'' + ", serialNumber='"
                + serialNumber + '\'' + '}';
    }

    public String udn;
    public String host;
    public @Nullable String friendlyName;
    public @Nullable String modelName;
    public @Nullable String serialNumber;

    private DeviceInformation(@Nullable ThingUID thingUID, String manufacturer, String serviceType, String udn,
            String host, @Nullable String friendlyName, @Nullable String modelName, @Nullable String serialNumber) {
        this.thingUID = thingUID;
        this.manufacturer = manufacturer;
        this.serviceType = serviceType;
        this.udn = udn;
        this.host = host;
        this.friendlyName = friendlyName;
        this.modelName = modelName;
        this.serialNumber = serialNumber;
    }

    /**
     * get the device information from an UPnP device
     *
     * @param device a UPnP device
     * @return Optional of the device information (empty if not a remote device or information not available)
     */
    public static Optional<DeviceInformation> fromDevice(Device<?, ?, ?> device) {
        if (device instanceof RemoteDevice) {
            return fromDevice((RemoteDevice) device);
        } else {
            return Optional.empty();
        }
    }

    /**
     * get the device information from an UPnP RemoteDevice
     *
     * @param device a UPnP RemoteDevice
     * @return Optional of the device information (empty if information not available)
     */
    public static Optional<DeviceInformation> fromDevice(RemoteDevice device) {
        DeviceDetails deviceDetails = device.getDetails();
        DeviceType deviceType = device.getType();
        RemoteDeviceIdentity deviceIdentity = device.getIdentity();
        if (deviceDetails == null || deviceType == null || deviceIdentity == null) {
            return Optional.empty();
        }

        ManufacturerDetails manufacturerDetails = deviceDetails.getManufacturerDetails();
        UDN udn = deviceIdentity.getUdn();
        URL url = deviceIdentity.getDescriptorURL();
        if (manufacturerDetails == null || udn == null || url == null) {
            return Optional.empty();
        }

        ModelDetails modelDetails = deviceDetails.getModelDetails();
        String modelName = modelDetails == null ? null : modelDetails.getModelName();

        ThingUID thingUID;
        if (!manufacturerDetails.getManufacturer().toUpperCase().contains("PANASONIC")
                || !deviceType.getType().equals("MediaRenderer")) {
            thingUID = null;
        } else {
            thingUID = new ThingUID(THING_TYPE_PANASONICTV, udn.getIdentifierString().replace("-", "_"));
        }

        return Optional.of(new DeviceInformation(thingUID, manufacturerDetails.getManufacturer(), deviceType.getType(),
                udn.getIdentifierString(), url.getHost(), deviceDetails.getFriendlyName(), modelName,
                deviceDetails.getSerialNumber()));
    }
}
