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
package org.openhab.binding.sony.internal.simpleip;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.types.UDN;
import org.openhab.binding.sony.internal.AbstractDiscoveryParticipant;
import org.openhab.binding.sony.internal.SonyBindingConstants;
import org.openhab.binding.sony.internal.UidUtils;
import org.openhab.binding.sony.internal.net.NetUtil;
import org.openhab.binding.sony.internal.net.SocketSessionListener;
import org.openhab.binding.sony.internal.providers.SonyDefinitionProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This implementation of the {@link UpnpDiscoveryParticipant} provides discovery of Sony SIMPLE IP protocol
 * devices.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, configurationPid = "discovery.sony-simpleip")
public class SimpleIpDiscoveryParticipant extends AbstractDiscoveryParticipant implements UpnpDiscoveryParticipant {
    /**
     * Constructs the participant
     * 
     * @param sonyDefinitionProvider a non-null sony definition provider
     */
    @Activate
    public SimpleIpDiscoveryParticipant(final @Reference SonyDefinitionProvider sonyDefinitionProvider) {
        super(SonyBindingConstants.SIMPLEIP_THING_TYPE_PREFIX, sonyDefinitionProvider);
    }

    @Override
    protected boolean getDiscoveryEnableDefault() {
        return false;
    }

    @Override
    public @Nullable DiscoveryResult createResult(final RemoteDevice device) {
        Objects.requireNonNull(device, "device cannot be null");

        if (!isDiscoveryEnabled()) {
            return null;
        }

        final ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }

        final String ipAddress = device.getIdentity().getDescriptorURL().getHost();

        final CountDownLatch l = new CountDownLatch(1);
        final AtomicBoolean valid = new AtomicBoolean(false);

        // Test to see if it's an actual simpleip service
        try {
            NetUtil.sendSocketRequest(ipAddress, SimpleIpConstants.PORT, "*SEPOWR################\n",
                    new SocketSessionListener() {
                        @Override
                        public boolean responseReceived(final String response) {
                            valid.set(response.startsWith("*SAPOWR"));
                            l.countDown();
                            return true;
                        }

                        @Override
                        public void responseException(final IOException e) {
                            l.countDown();
                        }
                    });
            l.await(10, TimeUnit.SECONDS);
            if (!valid.get()) {
                logger.debug("SimpleIP device didn't implement the power command - ignoring: {}", device.getIdentity());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            // Normal to get an IO exception if device doesn't support - log as a trace
            logger.trace("SimpleIP device exception {}: {}", device.getIdentity(), e.getMessage(), e);
            return null;
        }

        final SimpleIpConfig config = new SimpleIpConfig();
        config.setDeviceAddress(ipAddress);

        final RemoteDeviceIdentity identity = device.getIdentity();
        config.setDiscoveredMacAddress(getMacAddress(identity, uid));
        config.setDiscoveredCommandsMapFile("simpleip-" + uid.getId() + ".map");

        final String thingId = UidUtils.getThingId(identity.getUdn());
        return DiscoveryResultBuilder.create(uid).withProperties(config.asProperties())
                .withProperty("SimpleUDN", StringUtils.defaultIfEmpty(thingId, uid.getId()))
                .withRepresentationProperty("SimpleUDN").withLabel(getLabel(device, "Simple IP")).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(final RemoteDevice device) {
        Objects.requireNonNull(device, "device cannot be null");

        if (!isDiscoveryEnabled()) {
            return null;
        }

        final String modelDescription = getModelDescription(device);

        // Simple IP == bravia
        if (isSonyDevice(device) && StringUtils.containsIgnoreCase(modelDescription, "bravia")) {
            if (isScalarThingType(device)) {
                logger.debug("Found a SCALAR thing type for this SIMPLEIP thing - ignoring SIMPLEIP");
                return null;
            }

            final String modelName = getModelName(device);
            if (modelName == null || StringUtils.isEmpty(modelName)) {
                logger.debug("Found Sony device but it has no model name - ignoring");
                return null;
            }

            final RemoteDeviceIdentity identity = device.getIdentity();
            if (identity != null) {
                final UDN udn = device.getIdentity().getUdn();
                logger.debug("Found Sony SimpleIP service: {}", udn);
                final ThingTypeUID modelUID = getThingTypeUID(modelName);
                return UidUtils.createThingUID(modelUID == null ? SimpleIpConstants.THING_TYPE_SIMPLEIP : modelUID,
                        udn);
            } else {
                logger.debug("Found Sony SimpleIP service but it had no identity!");
            }
        }
        return null;
    }
}
