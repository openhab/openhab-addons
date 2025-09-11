/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.gce.internal.model;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class takes care of interpreting the status.xml file
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class StatusFile {
    private final Logger logger = LoggerFactory.getLogger(StatusFile.class);
    private final Element root;
    private final NodeList childs;

    public StatusFile(Document doc) {
        this.root = doc.getDocumentElement();
        root.normalize();
        this.childs = root.getChildNodes();
    }

    public String getMac() {
        return root.getElementsByTagName("config_mac").item(0).getTextContent();
    }

    public String getVersion() {
        return root.getElementsByTagName("version").item(0).getTextContent();
    }

    public Map<Integer, Double> getPorts(PortDefinition portDefinition) {
        Map<Integer, Double> result = new HashMap<>();

        String searched = portDefinition.nodeName;

        IntStream.range(0, childs.getLength()).boxed().map(childs::item)
                .filter(node -> node.getNodeName().startsWith(searched)).forEach(node -> {
                    try {
                        result.put(Integer.parseInt(node.getNodeName().replace(searched, "")) + 1,
                                Double.parseDouble(node.getTextContent().replace("dn", "1").replace("up", "0")));
                    } catch (NumberFormatException e) {
                        logger.warn("{}", e.getMessage());
                    }
                });
        return result;
    }
}
