/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonytv.discovery;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.sonytv.SonyTVBindingConstants;
import org.openhab.binding.sonytv.config.BraviaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The {@link BraviaDiscoveryParticipant} is responsible processing the
 * results of searches for UPNP devices
 *
 * @author Mikołaj Siedlarek - Initial contribution
 */
public class BraviaDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DocumentBuilder documentBuilder;
    private final XPathExpression baseUrlXpath;

    public BraviaDiscoveryParticipant() {
        try {
            this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (final ParserConfigurationException exception) {
            throw new AssertionError(exception);
        }

        final XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            baseUrlXpath = xpath.compile("//*[local-name()='X_ScalarWebAPI_BaseURL']");
        } catch (final XPathExpressionException exception) {
            throw new AssertionError(exception);
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(SonyTVBindingConstants.THING_TYPE_BRAVIA);
    }

    @Override
    public DiscoveryResult createResult(final RemoteDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }

        final Document descriptor;
        try {
            descriptor = documentBuilder.parse(device.getIdentity().getDescriptorURL().toString());
        } catch (final SAXException | IOException exception) {
            logger.error("Could not parse UPnP descriptor.", exception);
            return null;
        }

        final String apiUrl;
        try {
            apiUrl = (String) baseUrlXpath.evaluate(descriptor, XPathConstants.STRING);
        } catch (final XPathExpressionException exception) {
            logger.error("Could not extract base URL from UPnP descriptor.", exception);
            return null;
        }

        final Map<String, Object> properties = new HashMap<>(2);
        properties.put(BraviaConfiguration.UDN, device.getIdentity().getUdn().getIdentifierString());
        properties.put(BraviaConfiguration.API_URL, apiUrl);
        return DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withLabel(device.getDetails().getFriendlyName()).withRepresentationProperty(BraviaConfiguration.UDN)
                .build();
    }

    @Override
    public ThingUID getThingUID(final RemoteDevice device) {
        final String manufacturer;
        final String model;
        final String udn;
        try {
            manufacturer = device.getDetails().getManufacturerDetails().getManufacturer().toUpperCase();
            model = device.getDetails().getModelDetails().getModelName().toUpperCase();
            udn = device.getIdentity().getUdn().getIdentifierString();
        } catch (final NullPointerException exception) {
            return null;
        }
        if (manufacturer.contains("SONY") && model.startsWith("KDL-")) {
            return new ThingUID(SonyTVBindingConstants.THING_TYPE_BRAVIA, udn);
        }
        return null;
    }

}