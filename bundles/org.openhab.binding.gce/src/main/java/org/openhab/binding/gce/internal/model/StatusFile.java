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
package org.openhab.binding.gce.internal.model;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class takes care of interpreting the status.xml file
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class StatusFile {

    private final Element root;

    public StatusFile(Document doc) {
        this.root = doc.getDocumentElement();
        root.normalize();
    }

    public String getMac() {
        return root.getElementsByTagName("config_mac").item(0).getTextContent();
    }

    public String getVersion() {
        return root.getElementsByTagName("version").item(0).getTextContent();
    }

    public List<Node> getMatchingNodes(String criteria) {
        NodeList nodeList = root.getChildNodes();
        return IntStream.range(0, nodeList.getLength()).boxed().map(nodeList::item)
                .filter(node -> node.getNodeName().startsWith(criteria)).sorted(Comparator.comparing(Node::getNodeName))
                .toList();
    }

    public int getMaxNumberofNodeType(PortDefinition portDefinition) {
        return getMatchingNodes(portDefinition.nodeName).size();
    }
}
