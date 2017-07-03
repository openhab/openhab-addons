/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.discovery;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.sony.internal.net.NetUtilities;
import org.openhab.binding.sony.internal.net.SocketSessionListener;
import org.openhab.binding.sony.internal.simpleip.SimpleIpConfig;
import org.openhab.binding.sony.internal.simpleip.SimpleIpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * This implementation of the {@link UpnpDiscoveryParticipant} provides discovery of Sony Simple IP devices.
 *
 * @author Tim Roberts - Initial contribution
 */
public class SimpleIpDiscoveryParticipant implements UpnpDiscoveryParticipant {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(SimpleIpDiscoveryParticipant.class);

    /**
     * Returns the set of supported things {@link SimpleIpConstants#THING_TYPE_SIMPLEIP}.
     *
     * @return a singleton set to the simple ip thing type
     */
    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(SimpleIpConstants.THING_TYPE_SIMPLEIP);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant#createResult(org.jupnp.model.meta.RemoteDevice)
     */
    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {

            final String ipAddress = device.getIdentity().getDescriptorURL().getHost();
            final CountDownLatch l = new CountDownLatch(1);
            final AtomicBoolean valid = new AtomicBoolean(false);

            // Test to see if it's an actual simpleip service
            try {
                NetUtilities.sendSocketRequest(ipAddress, SimpleIpConstants.PORT, "*SEPOWR################\n",
                        new SocketSessionListener() {
                            @Override
                            public boolean responseReceived(String response) {
                                valid.set(response != null && response.startsWith("*SAPOWR"));
                                l.countDown();
                                return true;
                            }

                            @Override
                            public void responseException(Exception e) {
                                l.countDown();
                            }
                        });
                l.await(10, TimeUnit.SECONDS);
                if (!valid.get()) {
                    logger.debug("SimpleIP device didn't implement the power command - ignoring: {}",
                            device.getIdentity());
                    return null;
                }
            } catch (IOException | InterruptedException e) {
                logger.debug("SimpleIP device exception {}: {}", device.getIdentity(), e.getMessage(), e);
                return null;
            }

            Map<String, Object> properties = new HashMap<>(2);
            properties.put(SimpleIpConfig.IpAddress, device.getIdentity().getDescriptorURL().getHost());
            properties.put(SimpleIpConfig.CommandsMapFile, "simpleip-" + uid.getId() + ".map");

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(device.getDetails().getFriendlyName()).build();
            return result;
        } else {
            return null;
        }
    }

    /**
     * Returns the {@link ThingUID} representing a Sony Simple IP device or null if the {@link RemoteDevice} is not a
     * Sony
     * Simple IP device.
     *
     * @param device a possibly null device to check
     * @return a {@link ThingUID} for the device or null if it's not a Sony Simple IP device
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
            final String modelDescription = details.getModelDetails() == null ? null
                    : details.getModelDetails().getModelDescription();

            if (manufacturer == null || modelDescription == null) {
                return null;
            }

            // Simple IP == bravia for now
            if (manufacturer.toLowerCase().contains("sony") && modelDescription.equalsIgnoreCase("bravia")
                    && device.getIdentity() != null) {
                final String identity = device.getIdentity().toString();
                final int idx = identity.indexOf("uuid:");
                final int idx2 = idx < 0 ? -1 : identity.indexOf(",", idx + 5);
                if (idx2 > 0) {
                    return new ThingUID(SimpleIpConstants.THING_TYPE_SIMPLEIP, identity.substring(idx + 5, idx2));
                }
            }
        }
        return null;
    }

}
