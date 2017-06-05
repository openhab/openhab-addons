/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal.discovery;

import static org.openhab.binding.denonmarantz.DenonMarantzBindingConstants.THING_TYPE_AVR;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jmdns.ServiceInfo;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.openhab.binding.denonmarantz.DenonMarantzBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jan-Willem Veldhuis
 *
 */
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

    private String vendor;

    private String model;

    private String serial;

    private String friendlyName;

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_AVR);
    }

    @Override
    public String getServiceType() {
        return RAOP_SERVICE_TYPE;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo serviceInfo) {
        logger.debug("AVR found: {}", serviceInfo.getQualifiedName());
        ThingUID uid = getThingUID(serviceInfo);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(2);

            if (serviceInfo.getHostAddresses().length == 0) {
                logger.debug("Could not determine IP address for the Denon/Marantz AVR");
                return null;
            }
            String host = serviceInfo.getHostAddresses()[0];

            logger.debug("IP Address: {}", host);

            // try a HTTP request to autoconfigure the HTTP / Telnet parameter
            HttpClient httpClient = new HttpClient();
            ContentResponse response;
            boolean telnetEnable = true;
            try {
                httpClient.start();
                response = httpClient.newRequest("http://" + host + "/goform/Deviceinfo.xml")
                        .timeout(3, TimeUnit.SECONDS).send();
                int status = response.getStatus();
                if (status == 200) {
                    telnetEnable = false;
                    logger.debug("We can access the HTTP API, disabling the Telnet mode by default");
                }
            } catch (Exception e) {
                logger.debug("Error when trying to access AVR using HTTP: {}, reverting to Telnet mode.",
                        e.getMessage());
            }
            httpClient.destroy();
            properties.put(DenonMarantzBindingConstants.PARAMETER_HOST, host);
            properties.put(DenonMarantzBindingConstants.PARAMETER_TELNET_ENABLED, telnetEnable);
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, this.serial);
            properties.put(Thing.PROPERTY_VENDOR, this.vendor);
            properties.put(Thing.PROPERTY_MODEL_ID, this.model);

            String label = this.friendlyName + " (" + this.vendor + ' ' + this.model + ")";
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                    .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();
            return result;

        } else {
            return null;
        }
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        Matcher matcher = DENON_MARANTZ_PATTERN.matcher(service.getQualifiedName());
        if (matcher.matches()) {
            logger.debug("This seems like a supported Denon/Marantz AVR!");
            this.serial = matcher.group(1).toLowerCase();

            /**
             * The Vendor is not available from the mDNS result.
             * We assign the Vendor based on our assumptions of the MAC address prefix.
             */
            this.vendor = "";
            if (this.serial.startsWith(MARANTZ_MAC_PREFIX)) {
                this.vendor = "Marantz";
            } else if (this.serial.startsWith(DENON_MAC_PREFIX)) {
                this.vendor = "Denon";
            }

            // 'am=...' property describes the model name
            this.model = service.getPropertyString("am");

            this.friendlyName = matcher.group(2).trim();

            return new ThingUID(THING_TYPE_AVR, this.serial);

        } else {
            logger.debug("This discovered device is not supported by the DenonMarantz binding, ignoring..");
        }
        return null;
    }

}
