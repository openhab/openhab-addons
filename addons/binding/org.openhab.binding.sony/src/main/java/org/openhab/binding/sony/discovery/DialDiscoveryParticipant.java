/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.discovery;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.meta.RemoteService;
import org.jupnp.model.types.ServiceId;
import org.jupnp.model.types.UDN;
import org.openhab.binding.sony.internal.dial.DialConfig;
import org.openhab.binding.sony.internal.dial.DialConstants;
import org.openhab.binding.sony.internal.dial.models.DialState;
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.NetUtilities;
import org.openhab.binding.sony.internal.simpleip.SimpleIpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation of the {@link UpnpDiscoveryParticipant} provides discovery of Sony DIAL protocol devices.
 *
 * @author Tim Roberts - Initial contribution
 */
public class DialDiscoveryParticipant implements UpnpDiscoveryParticipant {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(DialDiscoveryParticipant.class);

    /**
     * Returns the set of supported things {@link SimpleIpConstants#THING_TYPE_SIMPLEIP}.
     *
     * @return a singleton set to the ircc thing type
     */
    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(DialConstants.THING_TYPE_DIAL);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant#createResult(org.jupnp.model.meta.RemoteDevice)
     */
    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid != null) {
            try {
                return createDialDevice(device, uid);
            } catch (Exception e) {
                logger.error("Exception getting DIAL URL: {}", e.getMessage(), e);
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Creates the dial device.
     *
     * @param device the device
     * @param uid the uid
     * @return the discovery result
     * @throws Exception the exception
     */
    private DiscoveryResult createDialDevice(RemoteDevice device, ThingUID uid) throws Exception {
        final RemoteDeviceIdentity identity = device.getIdentity();
        final URL dialURL = identity.getDescriptorURL();

        try (final HttpRequest request = NetUtilities.createHttpRequest()) {
            try (final DialState dialState = new DialState(request, dialURL.toURI().toString())) {
                if (!dialState.hasDialService()) {
                    logger.debug("DIAL device didn't implement any device infos - ignoring: {}", identity);
                    return null;
                }
            }
        } catch (Exception e) {
            logger.debug("DIAL device exception {}: {}", device.getIdentity(), e.getMessage(), e);
            return null;
        }

        final Map<String, Object> properties = new HashMap<>(100);

        final String macAddress = NetUtilities.getMacAddress(identity.getWakeOnLANBytes());
        if (macAddress != null) {
            properties.put(DialConfig.DeviceMacAddress, macAddress);
        }

        properties.put(DialConfig.DialUri, dialURL.toURI().toString());
        properties.put(DialConfig.Refresh, -1); // let user decide

        final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withLabel(device.getDetails().getFriendlyName()).build();
        return result;
    }

    /**
     * Returns the {@link ThingUID} representing a Sony Ircc device or null if the {@link RemoteDevice} is not a Sony
     * ircc device.
     *
     * @param device a possibly null device to check
     * @return a {@link ThingUID} for the device or null if it's not a sony ircc device
     */
    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        if (device == null) {
            return null;
        }

        final DeviceDetails details = device.getDetails();

        if (details != null) {
            final String manufacturer = details.getManufacturerDetails() == null ? null
                    : details.getManufacturerDetails().getManufacturer();
            final String modelName = details.getModelDetails() == null ? null
                    : details.getModelDetails().getModelName();

            if (manufacturer == null || modelName == null) {
                return null;
            }

            if (manufacturer.toLowerCase().contains("sony")) {
                final RemoteService dialService = device.findService(new ServiceId("dial-multiscreen-org", "dial"));
                if (dialService != null) {
                    final RemoteDeviceIdentity identity = device.getIdentity();
                    if (identity != null) {

                        final UDN udn = device.getIdentity().getUdn();
                        logger.debug("Found Sony DIAL service: {}", udn);
                        return new ThingUID(DialConstants.THING_TYPE_DIAL, udn.getIdentifierString());
                    } else {
                        logger.debug("Found Sony DIAL service but it had no identity!");
                    }
                }
            }
        }
        return null;
    }

}
