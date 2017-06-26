/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roku.discovery;

import static org.openhab.binding.roku.RokuBindingConstants.THING_TYPE_ROKU;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.roku.RokuBindingConstants;
import org.openhab.binding.roku.internal.RokuState;
import org.openhab.binding.roku.internal.protocol.RokuCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RokuBindingDiscovery} class implements the abstract discovery service,
 * for automatically detecting roku devices on your network.
 *
 * @author Jarod Peters - Initial contribution
 * @author Shawn Wilsher - Implementing UpnpDiscoveryParticipant
 */
public class RokuDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(RokuDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_ROKU);
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }
        String ipAddress = device.getIdentity().getDescriptorURL().getHost();
        int port = device.getIdentity().getDescriptorURL().getPort();
        RokuState state = new RokuState(new RokuCommunication(ipAddress, port));
        try {
            state.updateDeviceInformation();
        } catch (IOException e) {
            logger.debug("Roku discoverd '{}:{}' but is not communicating", ipAddress, port, e);
            return null;
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put(RokuBindingConstants.IP_ADDRESS, ipAddress);
        properties.put(RokuBindingConstants.PORT, port);
        properties.put(RokuBindingConstants.REFRESH_INTERVAL, 30);
        properties.put(Thing.PROPERTY_VENDOR, state.getVendorName().toFullString());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, state.getSerialNumber().toFullString());
        String label = "Roku Device";
        if (!state.getUserDeviceName().toFullString().equals("")) {
            label = state.getUserDeviceName().toFullString();
        }
        return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).build();
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (details == null) {
            return null;
        }
        if (!details.getManufacturerDetails().getManufacturer().equals("Roku")) {
            return null;
        }
        return new ThingUID(THING_TYPE_ROKU, details.getSerialNumber());
    }
}
