/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal.discovery;

import static org.openhab.binding.denonmarantz.DenonMarantzBindingConstants.THING_TYPE_AVR;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jmdns.ServiceInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.denonmarantz.DenonMarantzBindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Jan-Willem Veldhuis - Initial contribution
 *
 */
@Component(service = MDNSDiscoveryParticipant.class, immediate = true)
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
        return Collections.singleton(THING_TYPE_AVR);
    }

    @Override
    public String getServiceType() {
        return RAOP_SERVICE_TYPE;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo serviceInfo) {
        String qualifiedName = serviceInfo.getQualifiedName();
        logger.debug("AVR found: {}", qualifiedName);
        ThingUID uid = getThingUID(serviceInfo);
        if (uid != null) {
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

            // try a HTTP request to autoconfigure the HTTP / Telnet parameter
            HttpClient httpClient = new HttpClient();
            ContentResponse response;
            boolean telnetEnable = true;
            int httpPort = 80;
            boolean httpApiUsable = false;

            // try to reach the HTTP API at port 80 (most models, except Denon ...H should respond.
            try {
                httpClient.start();
                response = httpClient.newRequest("http://" + host + "/goform/Deviceinfo.xml")
                        .timeout(3, TimeUnit.SECONDS).send();
                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                    logger.debug("We can access the HTTP API, disabling the Telnet mode by default.");
                    telnetEnable = false;
                    httpApiUsable = true;
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.debug("Error when trying to access AVR using HTTP on port 80: {}, reverting to Telnet mode.",
                        e.getMessage());
            } catch (Exception e) { // generic Exception is thrown by httpClient.start()
                logger.debug("Could not start the httpClient: {}", e.getMessage());
            }

            if (telnetEnable) {
                // the above attempt failed. Let's try on port 8080, as for some models a subset of the HTTP API is
                // available
                try {
                    response = httpClient.newRequest("http://" + host + ":8080/goform/Deviceinfo.xml")
                            .timeout(3, TimeUnit.SECONDS).send();
                    if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                        logger.debug(
                                "This model responds to HTTP port 8080, we use this port to retrieve the number of zones.");
                        httpPort = 8080;
                        httpApiUsable = true;
                    }
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    logger.debug("Additionally tried to connect to port 8080, this also failed: {}", e.getMessage());
                }

            }

            // default zone count
            int zoneCount = 2;

            // try to determine the zone count by checking the Deviceinfo.xml file
            if (httpApiUsable) {
                int status = 0;
                response = null;
                try {
                    response = httpClient.newRequest("http://" + host + ":" + httpPort + "/goform/Deviceinfo.xml")
                            .timeout(3, TimeUnit.SECONDS).send();
                    status = response.getStatus();
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    logger.debug("Failed in fetching the Deviceinfo.xml to determine zone count: {}", e.getMessage());
                }

                if (status == HttpURLConnection.HTTP_OK && response != null) {
                    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder;
                    try {
                        builder = domFactory.newDocumentBuilder();
                        Document dDoc = builder.parse(new InputSource(new StringReader(response.getContentAsString())));
                        XPath xPath = XPathFactory.newInstance().newXPath();
                        Node node = (Node) xPath.evaluate("/Device_Info/DeviceZones/text()", dDoc, XPathConstants.NODE);
                        String nodeValue = node.getNodeValue();
                        logger.trace("/Device_Info/DeviceZones/text() = {}", nodeValue);
                        zoneCount = Integer.parseInt(nodeValue);
                        logger.debug("Discovered number of zones: {}", zoneCount);
                    } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException
                            | NumberFormatException e) {
                        logger.debug("Something went wrong with looking up the zone count in Deviceinfo.xml: {}",
                                e.getMessage());
                    }
                }
            }

            httpClient.destroy();
            properties.put(DenonMarantzBindingConstants.PARAMETER_HOST, host);
            properties.put(DenonMarantzBindingConstants.PARAMETER_HTTP_PORT, httpPort);
            properties.put(DenonMarantzBindingConstants.PARAMETER_TELNET_ENABLED, telnetEnable);
            properties.put(DenonMarantzBindingConstants.PARAMETER_ZONE_COUNT, zoneCount);
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, serial);
            properties.put(Thing.PROPERTY_VENDOR, vendor);
            properties.put(Thing.PROPERTY_MODEL_ID, model);

            String label = friendlyName + " (" + vendor + ' ' + model + ")";
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
            String serial = matcher.group(1).toLowerCase();
            return new ThingUID(THING_TYPE_AVR, serial);
        } else {
            logger.trace("This discovered device is not supported by the DenonMarantz binding, ignoring..");
        }
        return null;
    }

}
