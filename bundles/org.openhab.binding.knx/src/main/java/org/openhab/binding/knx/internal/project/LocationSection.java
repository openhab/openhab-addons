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
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@code Installation/Locations} (ETS 5 on) or {@code Installation/Buildings} (ETS 4): the tree of spaces
 * ({@code Space}, {@code BuildingPart} in ETS 4) with the devices placed in them and their functions. Both
 * element names are accepted in both sections.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class LocationSection implements InstallationSection {

    private final ReadBudget budget;
    private final Deque<SpaceBuilder> open = new ArrayDeque<>();
    private final List<KnxSpace> roots = new ArrayList<>();
    private @Nullable FunctionBuilder function;

    LocationSection(ReadBudget budget) {
        this.budget = budget;
    }

    @Override
    public void start(String element, @Nullable String parent, int depth, XMLStreamReader xml)
            throws KnxProjectException {
        @Nullable
        SpaceBuilder space = top();
        @Nullable
        FunctionBuilder function = this.function;
        if (function != null) {
            if ("GroupAddressRef".equals(element) && depth == function.depth + 1) {
                budget.countRecord();
                function.groupAddresses
                        .add(new KnxFunctionGroupAddress(XmlInput.text(xml, "Id"), XmlInput.text(xml, "Name"),
                                ProjectIds.shortId(XmlInput.text(xml, "RefId")), XmlInput.attribute(xml, "Role")));
            }
        } else if (isSpace(element)) {
            if (depth == (space == null ? 1 : space.depth + 1)) {
                budget.countRecord();
                open.push(new SpaceBuilder(depth, XmlInput.text(xml, "Id"), XmlInput.text(xml, "Name"),
                        XmlInput.text(xml, "Type"), XmlInput.attribute(xml, "Number"),
                        XmlInput.attribute(xml, "Description"), XmlInput.attribute(xml, "Usage")));
            }
        } else if (space != null && depth == space.depth + 1) {
            if ("DeviceInstanceRef".equals(element)) {
                space.deviceIds.add(XmlInput.text(xml, "RefId"));
            } else if ("Function".equals(element)) {
                budget.countRecord();
                this.function = new FunctionBuilder(depth, XmlInput.text(xml, "Id"), XmlInput.text(xml, "Name"),
                        XmlInput.text(xml, "Type"));
            }
        }
    }

    @Override
    public void end(String element, int depth) {
        @Nullable
        SpaceBuilder space = top();
        @Nullable
        FunctionBuilder function = this.function;
        if (function != null && "Function".equals(element) && function.depth == depth && space != null) {
            space.functions.add(function.build());
            this.function = null;
        } else if (function == null && space != null && isSpace(element) && space.depth == depth) {
            KnxSpace built = open.pop().build();
            @Nullable
            SpaceBuilder parent = top();
            if (parent != null) {
                parent.spaces.add(built);
            } else {
                roots.add(built);
            }
        }
    }

    private @Nullable SpaceBuilder top() {
        return open.isEmpty() ? null : open.peek();
    }

    private static boolean isSpace(String element) {
        return "Space".equals(element) || "BuildingPart".equals(element);
    }

    List<KnxSpace> spaces() {
        return roots;
    }

    private static final class SpaceBuilder {
        final int depth;
        final String id;
        final String name;
        final String type;
        final @Nullable String number;
        final @Nullable String description;
        final @Nullable String usage;
        final List<KnxSpace> spaces = new ArrayList<>();
        final List<String> deviceIds = new ArrayList<>();
        final List<KnxFunction> functions = new ArrayList<>();

        SpaceBuilder(int depth, String id, String name, String type, @Nullable String number,
                @Nullable String description, @Nullable String usage) {
            this.depth = depth;
            this.id = id;
            this.name = name;
            this.type = type;
            this.number = number;
            this.description = description;
            this.usage = usage;
        }

        KnxSpace build() {
            return new KnxSpace(id, name, type, number, description, usage, spaces, deviceIds, functions);
        }
    }

    private static final class FunctionBuilder {
        final int depth;
        final String id;
        final String name;
        final String type;
        final List<KnxFunctionGroupAddress> groupAddresses = new ArrayList<>();

        FunctionBuilder(int depth, String id, String name, String type) {
            this.depth = depth;
            this.id = id;
            this.name = name;
            this.type = type;
        }

        KnxFunction build() {
            return new KnxFunction(id, name, type, groupAddresses);
        }
    }
}
