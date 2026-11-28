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

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@code Installation/Topology}: areas, lines, segments and devices. Up to ETS 5 devices sit on the line, from
 * ETS 6 on in a segment of it. A device's {@code Security}, bus interface passwords and other secrets are
 * children of {@code DeviceInstance} and are never read.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class TopologySection implements InstallationSection {

    private static final String FILE = InstallationReader.FILE;

    private final ReadBudget budget;
    private final List<KnxArea> areas = new ArrayList<>();
    private final List<KnxDevice> unplaced = new ArrayList<>();
    private @Nullable AreaBuilder area;
    private @Nullable LineBuilder line;
    private @Nullable SegmentBuilder segment;
    private @Nullable DeviceBuilder device;

    TopologySection(ReadBudget budget) {
        this.budget = budget;
    }

    @Override
    public void start(String element, @Nullable String parent, int depth, XMLStreamReader xml)
            throws KnxProjectException {
        AreaBuilder area = this.area;
        LineBuilder line = this.line;
        SegmentBuilder segment = this.segment;
        DeviceBuilder device = this.device;
        if (device != null) {
            if (device.child(element, parent, depth, xml)) {
                budget.countRecord();
            }
            return;
        }
        if ("Area".equals(element) && depth == 1) {
            budget.countRecord();
            this.area = new AreaBuilder(depth, XmlInput.text(xml, "Id"), XmlInput.number(xml, "Address", 0, 15, FILE),
                    XmlInput.text(xml, "Name"));
        } else if ("Line".equals(element) && area != null && "Area".equals(parent) && line == null) {
            budget.countRecord();
            this.line = new LineBuilder(depth, XmlInput.text(xml, "Id"), XmlInput.number(xml, "Address", 0, 15, FILE),
                    XmlInput.text(xml, "Name"), XmlInput.attribute(xml, "MediumTypeRefId"));
        } else if ("Segment".equals(element) && line != null && "Line".equals(parent) && segment == null) {
            budget.countRecord();
            this.segment = new SegmentBuilder(depth, XmlInput.text(xml, "Id"),
                    XmlInput.optionalNumber(xml, "Number", 0, Integer.MAX_VALUE, FILE),
                    XmlInput.attribute(xml, "MediumTypeRefId"));
        } else if ("DeviceInstance".equals(element)) {
            budget.countRecord();
            this.device = startDevice(xml, parent, depth, area, line, segment, budget);
        }
    }

    private static DeviceBuilder startDevice(XMLStreamReader xml, @Nullable String parent, int depth,
            @Nullable AreaBuilder area, @Nullable LineBuilder line, @Nullable SegmentBuilder segment, ReadBudget budget)
            throws KnxProjectException {
        boolean onLine = line != null && area != null && "Line".equals(parent);
        boolean inSegment = segment != null && line != null && area != null && "Segment".equals(parent);
        Integer number = XmlInput.optionalNumber(xml, "Address", 0, 255, FILE);
        KnxIndividualAddress address = null;
        String medium = null;
        if (line != null && area != null && (onLine || inSegment)) {
            if (number != null) {
                address = new KnxIndividualAddress(area.address, line.address, number);
            }
            String segmentMedium = inSegment && segment != null ? segment.medium : null;
            medium = segmentMedium != null ? segmentMedium : line.medium;
        }
        return new DeviceBuilder(depth, inSegment ? segment : null, onLine ? line : null, XmlInput.text(xml, "Id"),
                address, XmlInput.text(xml, "Name"), XmlInput.attribute(xml, "Description"),
                XmlInput.attribute(xml, "ProductRefId"), XmlInput.attribute(xml, "Hardware2ProgramRefId"), medium,
                budget);
    }

    @Override
    public void end(String element, int depth) {
        DeviceBuilder device = this.device;
        if (device != null) {
            if (device.endChild(element, depth)) {
                return;
            }
            if (device.depth == depth) {
                KnxDevice built = device.build();
                SegmentBuilder inSegment = device.segment;
                LineBuilder onLine = device.line;
                if (inSegment != null) {
                    inSegment.devices.add(built);
                } else if (onLine != null) {
                    onLine.devices.add(built);
                } else {
                    unplaced.add(built);
                }
                this.device = null;
            }
            return;
        }
        SegmentBuilder segment = this.segment;
        LineBuilder line = this.line;
        AreaBuilder area = this.area;
        if (segment != null && segment.depth == depth && line != null) {
            line.segments.add(segment.build());
            this.segment = null;
        } else if (line != null && line.depth == depth && area != null) {
            area.lines.add(line.build());
            this.line = null;
        } else if (area != null && area.depth == depth) {
            areas.add(area.build());
            this.area = null;
        }
    }

    KnxTopology topology() {
        return new KnxTopology(areas, unplaced);
    }

    private static final class AreaBuilder {
        final int depth;
        final String id;
        final int address;
        final String name;
        final List<KnxLine> lines = new ArrayList<>();

        AreaBuilder(int depth, String id, int address, String name) {
            this.depth = depth;
            this.id = id;
            this.address = address;
            this.name = name;
        }

        KnxArea build() {
            return new KnxArea(id, address, name, lines);
        }
    }

    private static final class LineBuilder {
        final int depth;
        final String id;
        final int address;
        final String name;
        final @Nullable String medium;
        final List<KnxSegment> segments = new ArrayList<>();
        final List<KnxDevice> devices = new ArrayList<>();

        LineBuilder(int depth, String id, int address, String name, @Nullable String medium) {
            this.depth = depth;
            this.id = id;
            this.address = address;
            this.name = name;
            this.medium = medium;
        }

        KnxLine build() {
            return new KnxLine(id, address, name, medium, segments, devices);
        }
    }

    private static final class SegmentBuilder {
        final int depth;
        final String id;
        final @Nullable Integer number;
        final @Nullable String medium;
        final List<KnxDevice> devices = new ArrayList<>();

        SegmentBuilder(int depth, String id, @Nullable Integer number, @Nullable String medium) {
            this.depth = depth;
            this.id = id;
            this.number = number;
            this.medium = medium;
        }

        KnxSegment build() {
            return new KnxSegment(id, number, medium, devices);
        }
    }

    /** Open while the pass is inside a {@code DeviceInstance}; its children belong to the device. */
    private static final class DeviceBuilder {
        final int depth;
        final @Nullable SegmentBuilder segment;
        final @Nullable LineBuilder line;
        final String id;
        final @Nullable KnxIndividualAddress address;
        final String name;
        final @Nullable String description;
        final @Nullable String productRefId;
        final @Nullable String hardware2ProgramRefId;
        final @Nullable String medium;
        final List<KnxObjectInstance> objects = new ArrayList<>();
        final List<KnxParameterValue> parameters = new ArrayList<>();
        final List<KnxModuleInstance> modules = new ArrayList<>();
        private final ReadBudget budget;
        private @Nullable ObjectBuilder object;
        private @Nullable ModuleBuilder module;

        DeviceBuilder(int depth, @Nullable SegmentBuilder segment, @Nullable LineBuilder line, String id,
                @Nullable KnxIndividualAddress address, String name, @Nullable String description,
                @Nullable String productRefId, @Nullable String hardware2ProgramRefId, @Nullable String medium,
                ReadBudget budget) {
            this.budget = budget;
            this.depth = depth;
            this.segment = segment;
            this.line = line;
            this.id = id;
            this.address = address;
            this.name = name;
            this.description = description;
            this.productRefId = productRefId;
            this.hardware2ProgramRefId = hardware2ProgramRefId;
            this.medium = medium;
        }

        /**
         * A descendant of the device. Only the children the model keeps are looked at, the rest is passed over.
         *
         * @return whether the element adds a record to the model
         */
        boolean child(String element, @Nullable String parent, int childDepth, XMLStreamReader xml)
                throws KnxProjectException {
            ObjectBuilder object = this.object;
            ModuleBuilder module = this.module;
            if (object != null) {
                if (childDepth == object.depth + 2 && "Connectors".equals(parent)) {
                    if ("Send".equals(element)) {
                        object.sends.add(ProjectIds.shortId(XmlInput.text(xml, "GroupAddressRefId")));
                        return true;
                    } else if ("Receive".equals(element)) {
                        object.receives.add(ProjectIds.shortId(XmlInput.text(xml, "GroupAddressRefId")));
                        return true;
                    }
                }
            } else if (module != null) {
                if (childDepth == module.depth + 2 && "Arguments".equals(parent) && "Argument".equals(element)) {
                    module.arguments
                            .add(new KnxModuleArgument(XmlInput.text(xml, "RefId"), XmlInput.attribute(xml, "Value")));
                    return true;
                }
            } else if (childDepth == depth + 2) {
                if ("ComObjectInstanceRef".equals(element) && "ComObjectInstanceRefs".equals(parent)) {
                    this.object = new ObjectBuilder(childDepth, xml, budget);
                    return true;
                } else if ("ParameterInstanceRef".equals(element) && "ParameterInstanceRefs".equals(parent)) {
                    parameters
                            .add(new KnxParameterValue(XmlInput.text(xml, "RefId"), XmlInput.attribute(xml, "Value")));
                    return true;
                } else if ("ModuleInstance".equals(element) && "ModuleInstances".equals(parent)) {
                    this.module = new ModuleBuilder(childDepth, XmlInput.text(xml, "Id"), XmlInput.text(xml, "RefId"),
                            XmlInput.attribute(xml, "RepeatIndex"));
                    return true;
                }
            }
            return false;
        }

        /** Whether this end tag closed an object or module instance. */
        boolean endChild(String element, int childDepth) {
            ObjectBuilder object = this.object;
            ModuleBuilder module = this.module;
            if (object != null && object.depth == childDepth && "ComObjectInstanceRef".equals(element)) {
                objects.add(object.build());
                this.object = null;
                return true;
            }
            if (module != null && module.depth == childDepth && "ModuleInstance".equals(element)) {
                modules.add(module.build());
                this.module = null;
                return true;
            }
            return false;
        }

        KnxDevice build() {
            return new KnxDevice(id, address, name, description, productRefId, hardware2ProgramRefId, medium, objects,
                    parameters, modules);
        }
    }

    private static final class ObjectBuilder {
        final int depth;
        final String refId;
        final List<String> links = new ArrayList<>();
        final List<String> sends = new ArrayList<>();
        final List<String> receives = new ArrayList<>();
        final @Nullable String text;
        final @Nullable String functionText;
        final @Nullable String description;
        final List<KnxDatapointType> datapointTypes;
        final @Nullable String channelId;
        final KnxObjectFlags flags;

        ObjectBuilder(int depth, XMLStreamReader xml, ReadBudget budget) throws KnxProjectException {
            this.depth = depth;
            refId = XmlInput.text(xml, "RefId");
            String linkText = XmlInput.attribute(xml, "Links");
            if (linkText != null && !linkText.isBlank()) {
                links.addAll(ProjectIds.shortIds(linkText, budget));
            }
            text = XmlInput.attribute(xml, "Text");
            functionText = XmlInput.attribute(xml, "FunctionText");
            description = XmlInput.attribute(xml, "Description");
            datapointTypes = KnxDatapointType.parseList(XmlInput.attribute(xml, "DatapointType"), budget);
            channelId = XmlInput.attribute(xml, "ChannelId");
            flags = new KnxObjectFlags(XmlInput.attribute(xml, "ReadFlag"), XmlInput.attribute(xml, "WriteFlag"),
                    XmlInput.attribute(xml, "CommunicationFlag"), XmlInput.attribute(xml, "TransmitFlag"),
                    XmlInput.attribute(xml, "UpdateFlag"), XmlInput.attribute(xml, "ReadOnInitFlag"));
        }

        /** The {@code Links} attribute when it holds something (ETS 5 on), else the connectors (ETS 4). */
        KnxObjectInstance build() {
            List<String> ids;
            String sending;
            if (!links.isEmpty()) {
                ids = links;
                sending = links.get(0);
            } else {
                ids = new ArrayList<>(sends);
                ids.addAll(receives);
                sending = sends.isEmpty() ? null : sends.get(0);
            }
            return new KnxObjectInstance(refId, ids, sending, text, functionText, description, datapointTypes,
                    channelId, flags);
        }
    }

    private static final class ModuleBuilder {
        final int depth;
        final String id;
        final String refId;
        final @Nullable String repeatIndex;
        final List<KnxModuleArgument> arguments = new ArrayList<>();

        ModuleBuilder(int depth, String id, String refId, @Nullable String repeatIndex) {
            this.depth = depth;
            this.id = id;
            this.refId = refId;
            this.repeatIndex = repeatIndex;
        }

        KnxModuleInstance build() {
            return new KnxModuleInstance(id, refId, repeatIndex, arguments);
        }
    }
}
