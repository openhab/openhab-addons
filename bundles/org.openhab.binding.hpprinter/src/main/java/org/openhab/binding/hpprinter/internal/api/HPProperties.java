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
package org.openhab.binding.hpprinter.internal.api;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hpprinter.internal.HPPrinterConfiguration;
import org.openhab.core.thing.Thing;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The {@link HPProperties} is responsible for returning the
 * reading of data from the HP Embedded Web Server.
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class HPProperties {
    public static final String ENDPOINT = "/DevMgmt/ProductConfigDyn.xml";
    private final Map<String, String> properties = new HashMap<>();

    public HPProperties(Document document) {
        NodeList nodes = document.getDocumentElement().getElementsByTagName("prdcfgdyn:ProductInformation");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            properties.put(Thing.PROPERTY_SERIAL_NUMBER,
                    element.getElementsByTagName("dd:SerialNumber").item(0).getTextContent());
            properties.put(Thing.PROPERTY_MODEL_ID,
                    element.getElementsByTagName("dd:ProductNumber").item(0).getTextContent());
            properties.put(HPPrinterConfiguration.UUID,
                    element.getElementsByTagName("dd:UUID").item(0).getTextContent());
            Node firmwareDate = element.getElementsByTagName("dd:Version").item(0);
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareDate.getChildNodes().item(0).getTextContent());
        }
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
