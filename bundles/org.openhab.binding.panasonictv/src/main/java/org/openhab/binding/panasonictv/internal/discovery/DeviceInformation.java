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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.*;
import org.jupnp.model.types.DeviceType;
import org.jupnp.model.types.UDN;
import org.openhab.binding.panasonictv.internal.service.MediaRendererService;
import org.openhab.binding.panasonictv.internal.service.RemoteControllerService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.UIDUtils;

/**
 * The {@link DeviceInformation} is a wrapper for device information
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DeviceInformation {
    public @Nullable ThingUID thingUid;
    public String manufacturer;
    public Map<String, String> services = new HashMap<>();
    public String host;
    public @Nullable String friendlyName;
    public @Nullable String modelName;
    public @Nullable String serialNumber;

    private DeviceInformation(@Nullable ThingUID thingUid, String manufacturer, String serviceType, String udn,
            String host, @Nullable String friendlyName, @Nullable String modelName, @Nullable String serialNumber) {
        this.thingUid = thingUid;
        this.manufacturer = manufacturer;
        this.services.put(serviceType, udn);
        this.host = host;
        this.friendlyName = friendlyName;
        this.modelName = modelName;
        this.serialNumber = serialNumber;
    }

    public boolean isComplete() {
        return services.containsKey(MediaRendererService.SERVICE_NAME)
                && services.containsKey(RemoteControllerService.SERVICE_NAME);
    }

    public DeviceInformation merge(@Nullable DeviceInformation deviceInformation) {
        if (deviceInformation != null) {
            this.services.putAll(deviceInformation.services);
            if (deviceInformation.thingUid != null) {
                thingUid = deviceInformation.thingUid;
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return "DeviceInformation{" + "thingUid=" + thingUid + ", manufacturer='" + manufacturer + '\'' + ", services="
                + services + ", host='" + host + '\'' + ", friendlyName='" + friendlyName + '\'' + ", modelName='"
                + modelName + '\'' + ", serialNumber='" + serialNumber + '\'' + '}';
    }

    /**
     * get the device information from an UPnP RemoteDevice
     *
     * @param device a UPnP RemoteDevice
     * @return Optional of the device information (empty if information not available)
     */
    public static @Nullable DeviceInformation fromDevice(RemoteDevice device) {
        DeviceDetails deviceDetails = device.getDetails();
        DeviceType deviceType = device.getType();
        RemoteDeviceIdentity deviceIdentity = device.getIdentity();
        if (deviceDetails == null || deviceType == null || deviceIdentity == null) {
            return null;
        }

        ManufacturerDetails manufacturerDetails = deviceDetails.getManufacturerDetails();
        UDN udn = deviceIdentity.getUdn();
        URL url = deviceIdentity.getDescriptorURL();
        if (udn == null || url == null) {
            return null;
        }

        ModelDetails modelDetails = deviceDetails.getModelDetails();
        String modelName = modelDetails == null ? null : modelDetails.getModelName();

        // only generate the ThingUID for the media-renderer UDN
        ThingUID thingUid = MediaRendererService.SERVICE_NAME.equals(deviceType.getType())
                ? new ThingUID(THING_TYPE_PANASONICTV, UIDUtils.encode(udn.getIdentifierString()))
                : null;

        return new DeviceInformation(thingUid, manufacturerDetails.getManufacturer(), deviceType.getType(),
                udn.getIdentifierString(), url.getHost(), deviceDetails.getFriendlyName(), modelName,
                deviceDetails.getSerialNumber());
    }
}
