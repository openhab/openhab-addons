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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@code Installation/GroupAddresses}: the group range tree and the group addresses. Of
 * {@code GroupAddress@Key} only its presence is noted.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class GroupAddressSection implements InstallationSection {

    private static final String FILE = InstallationReader.FILE;

    private final KnxGroupAddressStyle style;
    private final ReadBudget budget;
    private final Deque<RangeBuilder> open = new ArrayDeque<>();
    private final List<KnxGroupRange> roots = new ArrayList<>();
    private final List<KnxGroupAddress> addresses = new ArrayList<>();

    GroupAddressSection(KnxGroupAddressStyle style, ReadBudget budget) {
        this.style = style;
        this.budget = budget;
    }

    @Override
    public void start(String element, @Nullable String parent, int depth, XMLStreamReader xml)
            throws KnxProjectException {
        if ("GroupRange".equals(element)) {
            budget.countRecord();
            open.push(new RangeBuilder(depth, XmlInput.text(xml, "Id"), XmlInput.text(xml, "Name"),
                    XmlInput.attribute(xml, "Description"), XmlInput.optionalNumber(xml, "RangeStart", 0, 0xFFFF, FILE),
                    XmlInput.optionalNumber(xml, "RangeEnd", 0, 0xFFFF, FILE)));
        } else if ("GroupAddress".equals(element)) {
            budget.countRecord();
            int raw = XmlInput.number(xml, "Address", 0, 0xFFFF, FILE);
            KnxGroupAddress address = new KnxGroupAddress(XmlInput.text(xml, "Id"), raw, style.format(raw),
                    XmlInput.text(xml, "Name"), XmlInput.attribute(xml, "Description"),
                    KnxDatapointType.parseList(XmlInput.attribute(xml, "DatapointType"), budget),
                    XmlInput.has(xml, "Key"), rangePath());
            addresses.add(address);
            if (!open.isEmpty()) {
                open.peek().addresses.add(address);
            }
        }
    }

    @Override
    public void end(String element, int depth) {
        if (open.isEmpty() || open.peek().depth != depth || !"GroupRange".equals(element)) {
            return;
        }
        KnxGroupRange built = open.pop().build();
        if (!open.isEmpty()) {
            open.peek().ranges.add(built);
        } else {
            roots.add(built);
        }
    }

    private List<String> rangePath() {
        List<String> path = new ArrayList<>();
        for (Iterator<RangeBuilder> i = open.descendingIterator(); i.hasNext();) {
            path.add(i.next().name);
        }
        return path;
    }

    List<KnxGroupRange> ranges() {
        return roots;
    }

    List<KnxGroupAddress> addresses() {
        return addresses;
    }

    private static final class RangeBuilder {
        final int depth;
        final String id;
        final String name;
        final @Nullable String description;
        final @Nullable Integer rangeStart;
        final @Nullable Integer rangeEnd;
        final List<KnxGroupRange> ranges = new ArrayList<>();
        final List<KnxGroupAddress> addresses = new ArrayList<>();

        RangeBuilder(int depth, String id, String name, @Nullable String description, @Nullable Integer rangeStart,
                @Nullable Integer rangeEnd) {
            this.depth = depth;
            this.id = id;
            this.name = name;
            this.description = description;
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
        }

        KnxGroupRange build() {
            return new KnxGroupRange(id, name, description, rangeStart, rangeEnd, ranges, addresses);
        }
    }
}
