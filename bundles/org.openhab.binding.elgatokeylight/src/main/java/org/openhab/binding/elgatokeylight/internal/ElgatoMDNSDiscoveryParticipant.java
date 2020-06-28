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
package org.openhab.binding.elgatokeylight.internal;

import static org.openhab.binding.elgatokeylight.internal.ElgatoKeyLightBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.elgatokeylight.internal.ElgatoKeyLightBindingConstants.THING_TYPE_KEY_LIGHT;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElgatorDiscoveryParticipant} is responsible processing the results
 * of searches for mDNS services of type _elg._tcp.local.
 *
 * @author Gunnar Wagenknecht - Initial contribution
 */
@Component(immediate = true, configurationPid = "discovery.elgatokeylight")
public class ElgatoMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(ElgatoMDNSDiscoveryParticipant.class);

    @Override
    public DiscoveryResult createResult(final ServiceInfo info) {
        try {
            ThingUID uid = getThingUID(info);
            if ((uid != null) && THING_TYPE_KEY_LIGHT.equals(getThingTypeUID(info))) {
                InetAddress[] addrs = info.getInetAddresses();
                if (addrs.length > 0) {
                    String ip = addrs[0].getHostAddress();
                    ElgatoKeyLight elgato = new ElgatoKeyLight(ip, info.getPort());

                    Map<String, Object> properties = new HashMap<>();

                    String label = elgato.readDisplayName();
                    if ((label == null) || label.isEmpty()) {
                        label = elgato.readProductName();
                    }

                    if ((label == null) || label.isEmpty()) {
                        label = "Elgato Key Light";
                    }

                    // we expect only one address per device..
                    if (addrs.length > 1) {
                        logger.warn("Elgato device {} ({}) reports multiple addresses - using the first one: {}",
                                info.getName(), label, Arrays.toString(addrs));
                    }

                    properties.put(ElgatoKeyLightConfiguration.HOST, addrs[0].getHostAddress());

                    properties.put(Thing.PROPERTY_VENDOR, "Elgato");

                    String firmwareVersion = elgato.readFirmwareVersion();
                    if ((firmwareVersion != null) && !firmwareVersion.isEmpty()) {
                        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
                    }

                    String hardwareRevision = elgato.readHardwareBoardType();
                    if ((hardwareRevision != null) && !hardwareRevision.isEmpty()) {
                        properties.put(Thing.PROPERTY_HARDWARE_VERSION, hardwareRevision);
                    }

                    String serialNumber = elgato.readSerialNumber();
                    if ((serialNumber != null) && !serialNumber.isEmpty()) {
                        properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
                    }

                    return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).withTTL(600)
                            .build();
                }
            }
        } catch (IOException e) {
            logger.error("Error communicating with device '{}': {}", info, e.getMessage(), e);
        }
        return null;
    }

    private String getSerialNumber(final ServiceInfo info) throws IOException {
        InetAddress[] addrs = info.getInetAddresses();
        if (addrs.length > 0) {
            String ip = addrs[0].getHostAddress();
            return new ElgatoKeyLight(ip, info.getPort()).readSerialNumber();
        }
        return null;
    }

    @Override
    public String getServiceType() {
        return "_elg._tcp.local.";
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    private ThingTypeUID getThingTypeUID(final ServiceInfo info) throws IOException {
        InetAddress[] addrs = info.getInetAddresses();
        if (addrs.length > 0) {
            String ip = addrs[0].getHostAddress();
            String productName = new ElgatoKeyLight(ip, info.getPort()).readProductName();
            if ((productName != null) && productName.equals("Elgato Key Light")) {
                return THING_TYPE_KEY_LIGHT;
            }
            return null;
        }
        return null;
    }

    @Override
    public ThingUID getThingUID(final ServiceInfo info) {
        logger.trace("ServiceInfo: {}", info);
        try {
            ThingTypeUID typeUID = getThingTypeUID(info);
            if (typeUID != null) {
                if (info.getType() != null) {
                    if (info.getType().equals(getServiceType())) {
                        logger.trace("Discovered a Elgato thing with name '{}'", info.getName());
                        String serialNumber = getSerialNumber(info);
                        if (serialNumber != null) {
                            return new ThingUID(typeUID, serialNumber);
                        } else {
                            return null;
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error communicating with device '{}': {}", info, e.getMessage(), e);
        }
        return null;
    }
}
