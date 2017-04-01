/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dlinkupnpcamera.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.dlinkupnpcamera.DlinkUpnpCameraBindingConstants;
import org.openhab.binding.dlinkupnpcamera.config.DlinkUpnpCameraConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DlinkUpnpCameraDiscoveryParticipant} is responsible processing the
 * results of searches for UPNP devices
 *
 * @author Yacine Ndiaye
 * @author Antoine Blanc
 * @author Christopher Law
 */
public class DlinkUpnpCameraDiscoveryParticipant implements UpnpDiscoveryParticipant {
    private Logger logger = LoggerFactory.getLogger(DlinkUpnpCameraDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return DlinkUpnpCameraBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override // discover the camera
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(3);
            String label = "camera device";
            try {
                label = device.getDetails().getModelDetails().getModelName();
            } catch (Exception e) {
                // ignore and use default label
            }
            // add the properties of the cameras to the properties of the thing
            properties.put(DlinkUpnpCameraConfiguration.UDN, device.getIdentity().getUdn().getIdentifierString());
            properties.put(DlinkUpnpCameraConfiguration.IP, device.getIdentity().getDescriptorURL().getHost());
            properties.put(DlinkUpnpCameraConfiguration.NAME, label);

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                    .build();
            logger.debug("Result {}", result.toString());
            logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'",
                    device.getDetails().getFriendlyName(), device.getIdentity().getUdn().getIdentifierString());
            // returning the result
            return result;
        } else {
            return null;
        }
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        if (device != null) {
            if (device.getDetails().getManufacturerDetails().getManufacturer() != null) {// detect all the D-Link
                                                                                         // cameras
                if (device.getDetails().getManufacturerDetails().getManufacturer().toUpperCase().contains("D-LINK")) {
                    logger.debug("Discovered a camera thing with UDN '{}'",
                            device.getIdentity().getUdn().getIdentifierString());

                    return new ThingUID(DlinkUpnpCameraBindingConstants.CAMERA_THING_TYPE_UID,
                            device.getIdentity().getUdn().getIdentifierString());
                }
            }
        }
        return null;
    }

}