/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.project;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * The single streaming pass over an installation file ({@code 0.xml}). Each child of {@code Installation}
 * that a {@link InstallationSection} is registered for is handed to it; everything else is passed over.
 * Only the first {@code Installation} element is read.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class InstallationReader {

    static final String FILE = "the installation file";

    private final KnxProjectInfo info;
    private final GroupAddressSection groupAddresses;
    private final ReadBudget budget;
    private final TopologySection topology;
    private final LocationSection locations;
    private final Map<String, InstallationSection> sections;

    InstallationReader(KnxProjectInfo info, ReadBudget budget) {
        this.info = info;
        this.budget = budget;
        this.topology = new TopologySection(budget);
        this.locations = new LocationSection(budget);
        this.groupAddresses = new GroupAddressSection(info.groupAddressStyle(), budget);
        this.sections = Map.of("GroupAddresses", groupAddresses, "Topology", topology, "Locations", locations,
                "Buildings", locations);
    }

    /**
     * @param ignoredFiles installation files of the project that are not read
     */
    KnxProject read(InputStream in, int ignoredFiles) throws KnxProjectException {
        String installationName = null;
        int ignoredElements = 0;
        XMLStreamReader xml = null;
        try {
            xml = XmlInput.open(in, FILE, budget);
            Deque<String> open = new ArrayDeque<>();
            int installationDepth = -1;
            InstallationSection active = null;
            int activeDepth = -1;
            while (xml.hasNext()) {
                int event = XmlInput.next(xml, FILE);
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String element = xml.getLocalName();
                    String parent = open.peek();
                    int depth = open.size();
                    if (depth == 0 && !"KNX".equals(element)) {
                        throw new KnxProjectException(Reason.MALFORMED, FILE + " is not a KNX project file");
                    }
                    InstallationSection section = depth == installationDepth + 1 ? sections.get(element) : null;
                    if (active != null) {
                        active.start(element, parent, depth - activeDepth, xml);
                    } else if (section != null) {
                        active = section;
                        activeDepth = depth;
                        section.start(element, parent, 0, xml);
                    } else if ("Installation".equals(element) && "Installations".equals(parent)) {
                        if (installationName != null) {
                            ignoredElements++;
                            XmlInput.skipElement(xml, FILE);
                            continue;
                        }
                        installationName = XmlInput.text(xml, "Name");
                        installationDepth = depth;
                    }
                    open.push(element);
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    String element = open.pop();
                    int depth = open.size();
                    if (active != null) {
                        active.end(element, depth - activeDepth);
                        if (depth == activeDepth) {
                            active = null;
                        }
                    } else if (depth == installationDepth) {
                        installationDepth = Integer.MIN_VALUE;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw XmlInput.failure(e, FILE, budget);
        } finally {
            XmlInput.close(xml);
        }
        if (installationName == null) {
            throw new KnxProjectException(Reason.MALFORMED, FILE + " holds no installation");
        }
        return new KnxProject(info, installationName, groupAddresses.ranges(), groupAddresses.addresses(),
                topology.topology(), locations.spaces(), ignoredFiles + ignoredElements);
    }
}
