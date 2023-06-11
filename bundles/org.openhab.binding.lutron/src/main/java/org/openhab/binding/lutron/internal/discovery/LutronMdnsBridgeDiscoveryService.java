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
package org.openhab.binding.lutron.internal.discovery;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.*;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * The {@link LutronMdnsBridgeDiscoveryService} discovers Lutron Caseta Smart Bridge, Caseta Smart Bridge Pro, RA2
 * Select Main Repeater, and other Lutron devices on the network using mDNS.
 *
 * @author Bob Adair - Initial contribution
 */
@Component
@NonNullByDefault
public class LutronMdnsBridgeDiscoveryService implements MDNSDiscoveryParticipant {

    // Lutron mDNS service <app>.<protocol>.<servicedomain>
    private static final String LUTRON_MDNS_SERVICE_TYPE = "_lutron._tcp.local.";

    private static final String PRODFAM_CASETA = "Caseta";
    private static final String PRODTYP_CASETA_SB = "Smart Bridge";
    private static final String DEVCLASS_CASETA_SB = "08040100";
    private static final String PRODTYP_CASETA_SBP2 = "Smart Bridge Pro 2";
    private static final String DEVCLASS_CASETA_SBP2 = "08050100";

    private static final String PRODFAM_RA2_SELECT = "RA2 Select";
    private static final String PRODTYP_RA2_SELECT = "Main Repeater";
    private static final String DEVCLASS_RA2_SELECT = "080E0401";

    private static final String PRODFAM_RA3 = "RadioRA 3";
    private static final String PRODTYP_RA3 = "Processor";
    private static final String DEVCLASS_RA3 = "081B0101";

    private static final String DEVCLASS_CONNECT_BRIDGE = "08090301";
    private static final String DEFAULT_LABEL = "Unknown Lutron bridge";

    private static final Pattern HOSTNAME_REGEX = Pattern.compile("lutron-([0-9a-f]+)\\."); // ex: lutron-01f1529a.local

    private final Logger logger = LoggerFactory.getLogger(LutronMdnsBridgeDiscoveryService.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_IPBRIDGE);
    }

    @Override
    public String getServiceType() {
        return LUTRON_MDNS_SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        if (!service.hasData()) {
            return null;
        }

        String nice = service.getNiceTextString();
        String qualifiedName = service.getQualifiedName();

        InetAddress[] ipAddresses = service.getInetAddresses();
        String devclass = service.getPropertyString("DEVCLASS");
        String codever = service.getPropertyString("CODEVER");
        String macaddr = service.getPropertyString("MACADDR");

        logger.debug("Lutron mDNS bridge discovery notified of Lutron mDNS service: {}", nice);
        logger.trace("Lutron mDNS service qualifiedName: {}", qualifiedName);
        logger.trace("Lutron mDNS service ipAddresses: {} ({})", ipAddresses, ipAddresses.length);
        logger.trace("Lutron mDNS service property DEVCLASS: {}", devclass);
        logger.trace("Lutron mDNS service property CODEVER: {}", codever);
        logger.trace("Lutron mDNS service property MACADDR: {}", macaddr);

        Map<String, Object> properties = new HashMap<>();
        String label = DEFAULT_LABEL;

        if (ipAddresses.length < 1) {
            return null;
        }
        if (ipAddresses.length > 1) {
            logger.debug("Multiple addresses found for discovered Lutron device. Using only the first.");
        }
        properties.put(HOST, ipAddresses[0].getHostAddress());

        String bridgeHostName = ipAddresses[0].getHostName();
        logger.debug("Lutron mDNS bridge hostname: {}", bridgeHostName);

        if (DEVCLASS_CASETA_SB.equals(devclass)) {
            properties.put(PROPERTY_PRODFAM, PRODFAM_CASETA);
            properties.put(PROPERTY_PRODTYP, PRODTYP_CASETA_SB);
            label = PRODFAM_CASETA + " " + PRODTYP_CASETA_SB;
        } else if (DEVCLASS_CASETA_SBP2.equals(devclass)) {
            properties.put(PROPERTY_PRODFAM, PRODFAM_CASETA);
            properties.put(PROPERTY_PRODTYP, PRODTYP_CASETA_SBP2);
            label = PRODFAM_CASETA + " " + PRODTYP_CASETA_SBP2;
        } else if (DEVCLASS_RA2_SELECT.equals(devclass)) {
            properties.put(PROPERTY_PRODFAM, PRODFAM_RA2_SELECT);
            properties.put(PROPERTY_PRODTYP, PRODTYP_RA2_SELECT);
            label = PRODFAM_RA2_SELECT + " " + PRODTYP_RA2_SELECT;
        } else if (DEVCLASS_RA3.equals(devclass)) {
            properties.put(PROPERTY_PRODFAM, PRODFAM_RA3);
            properties.put(PROPERTY_PRODTYP, PRODTYP_RA3);
            label = PRODFAM_RA3 + " " + PRODTYP_RA3;
        } else if (DEVCLASS_CONNECT_BRIDGE.equals(devclass)) {
            logger.debug("Lutron Connect Bridge discovered. Ignoring.");
            return null;
        } else {
            logger.info("Lutron device with unknown DEVCLASS discovered via mDNS: {}. Configure device manually.",
                    devclass);
            return null; // Exit if service has unknown DEVCLASS
        }

        if (!bridgeHostName.equals(ipAddresses[0].getHostAddress())) {
            label = label + " " + bridgeHostName;
        }

        if (codever != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, codever);
        }

        if (macaddr != null) {
            properties.put(Thing.PROPERTY_MAC_ADDRESS, macaddr);
        }

        String sn = getSerial(service);
        if (sn != null) {
            logger.trace("Lutron mDNS bridge serial number: {}", sn);
            properties.put(SERIAL_NUMBER, sn);
        } else {
            logger.debug("Unable to determine serial number of discovered Lutron bridge device.");
            return null;
        }

        ThingUID uid = getThingUID(service);
        if (uid != null) {
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel(label).withProperties(properties)
                    .withRepresentationProperty(SERIAL_NUMBER).build();
            logger.debug("Discovered Lutron bridge device via mDNS {}", uid);
            return result;
        } else {
            logger.trace("Failed to create uid for discovered Lutron bridge device");
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String serial = getSerial(service);
        String devclass = service.getPropertyString("DEVCLASS");
        if (serial == null) {
            return null;
        } else {
            if (DEVCLASS_CASETA_SB.equals(devclass)) {
                return new ThingUID(THING_TYPE_LEAPBRIDGE, serial);
            } else {
                return new ThingUID(THING_TYPE_IPBRIDGE, serial);
            }
        }
    }

    /**
     * Returns the device serial number for the mDNS service by extracting it from the hostname.
     * Used as unique thing representation property.
     *
     * @param service Lutron mDNS service
     * @return String containing serial number, or null if it cannot be determined
     */
    private @Nullable String getSerial(ServiceInfo service) {
        InetAddress[] ipAddresses = service.getInetAddresses();
        if (ipAddresses.length < 1) {
            return null;
        }
        Matcher matcher = HOSTNAME_REGEX.matcher(ipAddresses[0].getHostName());
        boolean matched = matcher.find();
        String serialnum = null;

        if (matched) {
            serialnum = matcher.group(1);
        }
        if (matched && serialnum != null && !serialnum.isEmpty()) {
            return serialnum;
        } else {
            return null;
        }
    }
}
