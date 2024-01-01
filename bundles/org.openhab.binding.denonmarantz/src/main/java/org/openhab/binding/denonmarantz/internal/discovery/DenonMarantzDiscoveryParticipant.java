/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.denonmarantz.internal.discovery;

import static org.openhab.binding.denonmarantz.internal.DenonMarantzBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jmdns.ServiceInfo;

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
 * @author Jan-Willem Veldhuis - Initial contribution
 *
 */
@Component
public class DenonMarantzDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(DenonMarantzDiscoveryParticipant.class);

    // Service type for 'Airplay enabled' receivers
    private static final String RAOP_SERVICE_TYPE = "_raop._tcp.local.";

    /**
     * Match the serial number, vendor and model of the discovered AVR.
     * Input is like "0006781D58B1@Marantz SR5008._raop._tcp.local."
     * A Denon AVR serial (MAC address) starts with 0005CD
     * A Marantz AVR serial (MAC address) starts with 000678
     */
    private static final Pattern DENON_MARANTZ_PATTERN = Pattern
            .compile("^((?:0005CD|000678)[A-Z0-9]+)@(.+)\\._raop\\._tcp\\.local\\.$");

    /**
     * Denon AVRs have a MAC address / serial number starting with 0005CD
     */
    private static final String DENON_MAC_PREFIX = "0005CD";

    /**
     * Marantz AVRs have a MAC address / serial number starting with 000678
     */
    private static final String MARANTZ_MAC_PREFIX = "000678";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_AVR);
    }

    @Override
    public String getServiceType() {
        return RAOP_SERVICE_TYPE;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo serviceInfo) {
        String qualifiedName = serviceInfo.getQualifiedName();
        logger.debug("AVR found: {}", qualifiedName);
        ThingUID thingUID = getThingUID(serviceInfo);
        if (thingUID != null) {
            Matcher matcher = DENON_MARANTZ_PATTERN.matcher(qualifiedName);
            matcher.matches(); // we already know it matches, it was matched in getThingUID
            String serial = matcher.group(1).toLowerCase();

            /**
             * The Vendor is not available from the mDNS result.
             * We assign the Vendor based on our assumptions of the MAC address prefix.
             */
            String vendor = "";
            if (serial.startsWith(MARANTZ_MAC_PREFIX)) {
                vendor = "Marantz";
            } else if (serial.startsWith(DENON_MAC_PREFIX)) {
                vendor = "Denon";
            }

            // 'am=...' property describes the model name
            String model = serviceInfo.getPropertyString("am");
            String friendlyName = matcher.group(2).trim();

            Map<String, Object> properties = new HashMap<>(2);

            if (serviceInfo.getHostAddresses().length == 0) {
                logger.debug("Could not determine IP address for the Denon/Marantz AVR");
                return null;
            }
            String host = serviceInfo.getHostAddresses()[0];

            logger.debug("IP Address: {}", host);

            properties.put(PARAMETER_HOST, host);
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, serial);
            properties.put(Thing.PROPERTY_VENDOR, vendor);
            properties.put(Thing.PROPERTY_MODEL_ID, model);

            String label = friendlyName + " (" + vendor + ' ' + model + ")";
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel(label)
                    .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();

        } else {
            return null;
        }
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        Matcher matcher = DENON_MARANTZ_PATTERN.matcher(service.getQualifiedName());
        if (matcher.matches()) {
            logger.debug("This seems like a supported Denon/Marantz AVR!");
            String serial = matcher.group(1).toLowerCase();
            return new ThingUID(THING_TYPE_AVR, serial);
        } else {
            logger.trace("This discovered device is not supported by the DenonMarantz binding, ignoring..");
        }
        return null;
    }
}
