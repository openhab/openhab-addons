/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mecmeter.internal.discovery;

import static org.openhab.binding.mecmeter.MecMeterBindingConstants.THING_TYPE_METER;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mecmeter.MecMeterBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MecMeterDiscoveryParticipant} is responsible for discovering devices, which are
 * sent to inbox.
 *
 * @author Florian Pazour - Initial contribution
 * @author Klaus Berger - Initial contribution
 * @author Kai Kreuzer - Refactoring for openHAB 3
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class)
public class MecMeterDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(MecMeterDiscoveryParticipant.class);
    private static final String SERVICE_TYPE = "_http._tcp.local.";

    /**
     * Match the serial number, vendor and model of the discovered PowerMeter.
     * Input is like "vpmAA11BB33CC55"
     */
    private static final Pattern MECMETER_PATTERN = Pattern
            .compile("^(vpm|mec)[A-F0-9]{12}\\._http\\._tcp\\.local\\.$");

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return MecMeterBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        String qualifiedName = service.getQualifiedName();
        logger.debug("Device found: {}", qualifiedName);
        ThingUID uid = getThingUID(service);
        if (uid == null) {
            return null;
        }

        String serial = qualifiedName.substring(3, 15);
        String vendor = "MEC";

        InetAddress ip = getIpAddress(service);
        if (ip == null) {
            return null;
        }
        String inetAddress = ip.toString().substring(1);

        Map<String, Object> properties = new HashMap<>(3);
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, serial);
        properties.put(Thing.PROPERTY_VENDOR, vendor);
        properties.put("ip", inetAddress);

        String label = "MEC Power Meter";
        return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();
    }

    private @Nullable InetAddress getIpAddress(ServiceInfo service) {
        if (service.getInet4Addresses().length > 0) {
            return service.getInet4Addresses()[0];
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        Matcher matcher = MECMETER_PATTERN.matcher(service.getQualifiedName());
        if (matcher.matches()) {
            String serial = service.getQualifiedName().substring(3, 15); // Qualified Name like "mecABCDEF123456", we
                                                                         // want "ABCDEF123456"
            return new ThingUID(THING_TYPE_METER, serial);
        } else {
            logger.debug("The discovered device is not supported, ignoring it.");
        }
        return null;
    }
}
