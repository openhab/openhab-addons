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
package org.openhab.binding.knx.internal.projectstore;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxArea;
import org.openhab.binding.knx.internal.project.KnxDatapointType;
import org.openhab.binding.knx.internal.project.KnxDevice;
import org.openhab.binding.knx.internal.project.KnxFunction;
import org.openhab.binding.knx.internal.project.KnxFunctionGroupAddress;
import org.openhab.binding.knx.internal.project.KnxGroupAddress;
import org.openhab.binding.knx.internal.project.KnxGroupAddressStyle;
import org.openhab.binding.knx.internal.project.KnxGroupObject;
import org.openhab.binding.knx.internal.project.KnxGroupObjectFlags;
import org.openhab.binding.knx.internal.project.KnxGroupRange;
import org.openhab.binding.knx.internal.project.KnxIndividualAddress;
import org.openhab.binding.knx.internal.project.KnxLine;
import org.openhab.binding.knx.internal.project.KnxObjectDefinition;
import org.openhab.binding.knx.internal.project.KnxObjectFlags;
import org.openhab.binding.knx.internal.project.KnxObjectInstance;
import org.openhab.binding.knx.internal.project.KnxProduct;
import org.openhab.binding.knx.internal.project.KnxProject;
import org.openhab.binding.knx.internal.project.KnxProjectInfo;
import org.openhab.binding.knx.internal.project.KnxSegment;
import org.openhab.binding.knx.internal.project.KnxSpace;
import org.openhab.binding.knx.internal.project.KnxTopology;
import org.openhab.core.thing.ThingUID;

/**
 * The stored project file: JSON, one object whose first field is {@code version}. Every record of the project
 * is an object with its component names; a null component is left out, an enum is its name. A device is
 * written without its parameter values and module instances.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ProjectFile {

    static final int VERSION = 1;
    /** How deep group ranges and spaces may be nested; the top level is 1. */
    static final int MAX_TREE_DEPTH = 64;
    /** Deep enough for ranges and spaces nested {@link #MAX_TREE_DEPTH} levels, with what they hold. */
    static final int MAX_JSON_DEPTH = 2 * MAX_TREE_DEPTH + 32;

    private ProjectFile() {
    }

    static void write(Writer writer, ThingUID bridge, StoredProject stored) throws IOException {
        new Out(new JsonOut(writer)).file(bridge, stored);
    }

    /** {@return true if the file of the project could be read back: ranges and spaces not nested too deep} */
    static boolean fits(KnxProject project) {
        Deque<Level> pending = new ArrayDeque<>();
        project.groupRanges().forEach(range -> pending.push(new Level(range.ranges(), 1)));
        project.locations().forEach(space -> pending.push(new Level(space.spaces(), 1)));
        while (!pending.isEmpty()) {
            Level level = pending.pop();
            if (level.depth() > MAX_TREE_DEPTH) {
                return false;
            }
            for (Object child : level.children()) {
                if (child instanceof KnxGroupRange range) {
                    pending.push(new Level(range.ranges(), level.depth() + 1));
                } else if (child instanceof KnxSpace space) {
                    pending.push(new Level(space.spaces(), level.depth() + 1));
                }
            }
        }
        return true;
    }

    /** The children of a range or space at this depth. */
    private record Level(List<?> children, int depth) {
    }

    static StoredProject read(Reader reader, ThingUID bridge, long maxChars) throws IOException {
        JsonIn in = new JsonIn(reader, maxChars, MAX_JSON_DEPTH);
        StoredProject stored = new In(in).file(bridge);
        in.end();
        return stored;
    }

    @FunctionalInterface
    private interface ItemWriter<T> {
        void write(T item) throws IOException;
    }

    @FunctionalInterface
    private interface ItemReader<T> {
        T read() throws IOException;
    }

    private static final class Out {
        private final JsonOut out;

        Out(JsonOut out) {
            this.out = out;
        }

        void file(ThingUID bridge, StoredProject stored) throws IOException {
            out.beginObject().name("version").value(VERSION).name("bridge").value(bridge.getAsString())
                    .name("importedAt").value(stored.importedAt().toString()).name("source").value(stored.sourceName())
                    .optional("language", stored.language()).name("project");
            project(stored.project());
            out.endObject();
        }

        private <T> void list(String name, List<T> items, ItemWriter<T> writer) throws IOException {
            out.name(name).beginArray();
            for (T item : items) {
                writer.write(item);
            }
            out.endArray();
        }

        private void strings(String name, List<String> items) throws IOException {
            list(name, items, out::value);
        }

        private void project(KnxProject project) throws IOException {
            out.beginObject().name("info");
            info(project.info());
            out.name("installationName").value(project.installationName());
            list("groupRanges", project.groupRanges(), this::range);
            list("groupAddresses", project.groupAddresses(), this::address);
            out.name("topology");
            topology(project.topology());
            list("locations", project.locations(), this::space);
            out.name("ignoredInstallations").value(project.ignoredInstallations());
            list("products", project.products(), this::product);
            list("groupObjects", project.groupObjects(), this::groupObject);
            out.endObject();
        }

        private void info(KnxProjectInfo info) throws IOException {
            out.beginObject().name("id").value(info.id()).name("name").value(info.name()).name("groupAddressStyle")
                    .value(info.groupAddressStyle().name()).optional("createdBy", info.createdBy())
                    .optional("toolVersion", info.toolVersion()).optional("schemaVersion", info.schemaVersion())
                    .optional("lastModified", info.lastModified()).endObject();
        }

        private void range(KnxGroupRange range) throws IOException {
            out.beginObject().name("id").value(range.id()).name("name").value(range.name())
                    .optional("description", range.description()).optional("rangeStart", range.rangeStart())
                    .optional("rangeEnd", range.rangeEnd());
            list("ranges", range.ranges(), this::range);
            list("groupAddresses", range.groupAddresses(), this::address);
            out.endObject();
        }

        private void address(KnxGroupAddress address) throws IOException {
            out.beginObject().name("id").value(address.id()).name("rawAddress").value(address.rawAddress())
                    .name("address").value(address.address()).name("name").value(address.name())
                    .optional("description", address.description());
            list("datapointTypes", address.datapointTypes(), this::datapointType);
            out.name("secured").value(address.secured());
            strings("rangePath", address.rangePath());
            out.endObject();
        }

        private void datapointType(KnxDatapointType type) throws IOException {
            out.beginObject().name("main").value(type.main()).optional("sub", type.sub()).endObject();
        }

        private void topology(KnxTopology topology) throws IOException {
            out.beginObject();
            list("areas", topology.areas(), this::area);
            list("unplacedDevices", topology.unplacedDevices(), this::device);
            out.endObject();
        }

        private void area(KnxArea area) throws IOException {
            out.beginObject().name("id").value(area.id()).name("address").value(area.address()).name("name")
                    .value(area.name());
            list("lines", area.lines(), this::line);
            out.endObject();
        }

        private void line(KnxLine line) throws IOException {
            out.beginObject().name("id").value(line.id()).name("address").value(line.address()).name("name")
                    .value(line.name()).optional("mediumTypeRefId", line.mediumTypeRefId());
            list("segments", line.segments(), this::segment);
            list("devices", line.devices(), this::device);
            out.endObject();
        }

        private void segment(KnxSegment segment) throws IOException {
            out.beginObject().name("id").value(segment.id()).optional("number", segment.number())
                    .optional("mediumTypeRefId", segment.mediumTypeRefId());
            list("devices", segment.devices(), this::device);
            out.endObject();
        }

        private void device(KnxDevice device) throws IOException {
            out.beginObject().name("id").value(device.id());
            KnxIndividualAddress individual = device.individualAddress();
            if (individual != null) {
                out.name("individualAddress");
                individualAddress(individual);
            }
            out.name("name").value(device.name()).optional("description", device.description())
                    .optional("productRefId", device.productRefId())
                    .optional("hardware2ProgramRefId", device.hardware2ProgramRefId())
                    .optional("mediumTypeRefId", device.mediumTypeRefId());
            list("objects", device.objects(), this::objectInstance);
            out.endObject();
        }

        private void individualAddress(KnxIndividualAddress address) throws IOException {
            out.beginObject().name("area").value(address.area()).name("line").value(address.line()).name("device")
                    .value(address.device()).endObject();
        }

        private void objectInstance(KnxObjectInstance object) throws IOException {
            out.beginObject().name("refId").value(object.refId());
            strings("groupAddressIds", object.groupAddressIds());
            out.optional("sendingGroupAddressId", object.sendingGroupAddressId()).optional("text", object.text())
                    .optional("functionText", object.functionText()).optional("description", object.description());
            list("datapointTypes", object.datapointTypes(), this::datapointType);
            out.optional("channelId", object.channelId()).name("flags");
            objectFlags(object.flags());
            out.endObject();
        }

        private void objectFlags(KnxObjectFlags flags) throws IOException {
            out.beginObject().optional("read", flags.read()).optional("write", flags.write())
                    .optional("communication", flags.communication()).optional("transmit", flags.transmit())
                    .optional("update", flags.update()).optional("readOnInit", flags.readOnInit()).endObject();
        }

        private void space(KnxSpace space) throws IOException {
            out.beginObject().name("id").value(space.id()).name("name").value(space.name()).name("type")
                    .value(space.type()).optional("number", space.number()).optional("description", space.description())
                    .optional("usage", space.usage());
            list("spaces", space.spaces(), this::space);
            strings("deviceIds", space.deviceIds());
            list("functions", space.functions(), this::function);
            out.endObject();
        }

        private void function(KnxFunction function) throws IOException {
            out.beginObject().name("id").value(function.id()).name("name").value(function.name()).name("type")
                    .value(function.type());
            list("groupAddresses", function.groupAddresses(), this::functionAddress);
            out.endObject();
        }

        private void functionAddress(KnxFunctionGroupAddress address) throws IOException {
            out.beginObject().name("id").value(address.id()).name("name").value(address.name()).name("groupAddressId")
                    .value(address.groupAddressId()).optional("role", address.role()).endObject();
        }

        private void product(KnxProduct product) throws IOException {
            out.beginObject().name("id").value(product.id()).optional("text", product.text())
                    .optional("orderNumber", product.orderNumber()).endObject();
        }

        private void groupObject(KnxGroupObject object) throws IOException {
            out.beginObject().name("deviceId").value(object.deviceId());
            KnxIndividualAddress deviceAddress = object.deviceAddress();
            if (deviceAddress != null) {
                out.name("deviceAddress");
                individualAddress(deviceAddress);
            }
            out.name("instance");
            objectInstance(object.instance());
            out.optional("number", object.number()).optional("text", object.text()).name("name").value(object.name())
                    .optional("functionText", object.functionText());
            list("datapointTypes", object.datapointTypes(), this::datapointType);
            out.optional("sizeText", object.sizeText()).optional("sizeBits", object.sizeBits()).name("flags");
            groupObjectFlags(object.flags());
            strings("groupAddressIds", object.groupAddressIds());
            out.optional("sendingGroupAddressId", object.sendingGroupAddressId())
                    .optional("channelId", object.channelId()).optional("moduleInstanceId", object.moduleInstanceId())
                    .name("definition").value(object.definition().name()).endObject();
        }

        private void groupObjectFlags(KnxGroupObjectFlags flags) throws IOException {
            out.beginObject().name("read").value(flags.read()).name("write").value(flags.write()).name("communication")
                    .value(flags.communication()).name("transmit").value(flags.transmit()).name("update")
                    .value(flags.update()).name("readOnInit").value(flags.readOnInit()).endObject();
        }
    }

    private static final class In {
        private final JsonIn in;

        In(JsonIn in) {
            this.in = in;
        }

        private static <T extends Object> T need(@Nullable T value) throws StoredFileException {
            if (value == null) {
                throw new StoredFileException();
            }
            return value;
        }

        private static void deepest(int depth) throws StoredFileException {
            if (depth > MAX_TREE_DEPTH) {
                throw new StoredFileException();
            }
        }

        private void unknown() throws IOException {
            in.skipValue();
        }

        private <T> List<T> list(ItemReader<T> reader) throws IOException {
            List<T> items = new ArrayList<>();
            in.beginArray();
            while (in.hasNext()) {
                items.add(reader.read());
            }
            in.endArray();
            return items;
        }

        private List<String> strings() throws IOException {
            return list(in::nextString);
        }

        StoredProject file(ThingUID bridge) throws IOException {
            in.beginObject();
            if (!in.hasNext() || !"version".equals(in.nextName())) {
                throw new StoredFileException();
            }
            int version = in.nextInt();
            if (version > VERSION) {
                throw new StoredFileException(true);
            } else if (version != VERSION) {
                throw new StoredFileException();
            }
            String bridgeText = null;
            Instant importedAt = null;
            String source = null;
            String language = null;
            KnxProject project = null;
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "version" -> throw new StoredFileException();
                    case "bridge" -> bridgeText = in.nextString();
                    case "importedAt" -> importedAt = instant(in.nextString());
                    case "source" -> source = in.nextString();
                    case "language" -> language = in.nextString();
                    case "project" -> project = project();
                    default -> unknown();
                }
            }
            in.endObject();
            if (!bridge.getAsString().equals(need(bridgeText))) {
                throw new StoredFileException();
            }
            return new StoredProject(need(project), need(importedAt), need(source), language);
        }

        private static Instant instant(String text) throws StoredFileException {
            try {
                return Instant.parse(text);
            } catch (DateTimeException e) {
                throw new StoredFileException();
            }
        }

        private static <E extends Enum<E>> E constant(Class<E> type, String name) throws StoredFileException {
            try {
                return Enum.valueOf(type, name);
            } catch (IllegalArgumentException e) {
                throw new StoredFileException();
            }
        }

        private KnxProject project() throws IOException {
            KnxProjectInfo info = null;
            String installationName = null;
            List<KnxGroupRange> groupRanges = null;
            List<KnxGroupAddress> groupAddresses = null;
            KnxTopology topology = null;
            List<KnxSpace> locations = null;
            Integer ignoredInstallations = null;
            List<KnxProduct> products = null;
            List<KnxGroupObject> groupObjects = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "info" -> info = info();
                    case "installationName" -> installationName = in.nextString();
                    case "groupRanges" -> groupRanges = list(() -> range(1));
                    case "groupAddresses" -> groupAddresses = list(this::address);
                    case "topology" -> topology = topology();
                    case "locations" -> locations = list(() -> space(1));
                    case "ignoredInstallations" -> ignoredInstallations = in.nextInt();
                    case "products" -> products = list(this::product);
                    case "groupObjects" -> groupObjects = list(this::groupObject);
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxProject(need(info), need(installationName), need(groupRanges), need(groupAddresses),
                    need(topology), need(locations), need(ignoredInstallations), need(products), need(groupObjects));
        }

        private KnxProjectInfo info() throws IOException {
            String id = null;
            String name = null;
            KnxGroupAddressStyle style = null;
            String createdBy = null;
            String toolVersion = null;
            Integer schemaVersion = null;
            String lastModified = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "id" -> id = in.nextString();
                    case "name" -> name = in.nextString();
                    case "groupAddressStyle" -> style = constant(KnxGroupAddressStyle.class, in.nextString());
                    case "createdBy" -> createdBy = in.nextString();
                    case "toolVersion" -> toolVersion = in.nextString();
                    case "schemaVersion" -> schemaVersion = in.nextInt();
                    case "lastModified" -> lastModified = in.nextString();
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxProjectInfo(need(id), need(name), need(style), createdBy, toolVersion, schemaVersion,
                    lastModified);
        }

        private KnxGroupRange range(int depth) throws IOException {
            deepest(depth);
            String id = null;
            String name = null;
            String description = null;
            Integer rangeStart = null;
            Integer rangeEnd = null;
            List<KnxGroupRange> ranges = null;
            List<KnxGroupAddress> addresses = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "id" -> id = in.nextString();
                    case "name" -> name = in.nextString();
                    case "description" -> description = in.nextString();
                    case "rangeStart" -> rangeStart = in.nextInt();
                    case "rangeEnd" -> rangeEnd = in.nextInt();
                    case "ranges" -> ranges = list(() -> range(depth + 1));
                    case "groupAddresses" -> addresses = list(this::address);
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxGroupRange(need(id), need(name), description, rangeStart, rangeEnd, need(ranges),
                    need(addresses));
        }

        private KnxGroupAddress address() throws IOException {
            String id = null;
            Integer rawAddress = null;
            String address = null;
            String name = null;
            String description = null;
            List<KnxDatapointType> types = null;
            Boolean secured = null;
            List<String> rangePath = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "id" -> id = in.nextString();
                    case "rawAddress" -> rawAddress = in.nextInt();
                    case "address" -> address = in.nextString();
                    case "name" -> name = in.nextString();
                    case "description" -> description = in.nextString();
                    case "datapointTypes" -> types = list(this::datapointType);
                    case "secured" -> secured = in.nextBoolean();
                    case "rangePath" -> rangePath = strings();
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxGroupAddress(need(id), need(rawAddress), need(address), need(name), description, need(types),
                    need(secured), need(rangePath));
        }

        private KnxDatapointType datapointType() throws IOException {
            Integer main = null;
            Integer sub = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "main" -> main = in.nextInt();
                    case "sub" -> sub = in.nextInt();
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxDatapointType(need(main), sub);
        }

        private KnxTopology topology() throws IOException {
            List<KnxArea> areas = null;
            List<KnxDevice> unplaced = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "areas" -> areas = list(this::area);
                    case "unplacedDevices" -> unplaced = list(this::device);
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxTopology(need(areas), need(unplaced));
        }

        private KnxArea area() throws IOException {
            String id = null;
            Integer address = null;
            String name = null;
            List<KnxLine> lines = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "id" -> id = in.nextString();
                    case "address" -> address = in.nextInt();
                    case "name" -> name = in.nextString();
                    case "lines" -> lines = list(this::line);
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxArea(need(id), need(address), need(name), need(lines));
        }

        private KnxLine line() throws IOException {
            String id = null;
            Integer address = null;
            String name = null;
            String medium = null;
            List<KnxSegment> segments = null;
            List<KnxDevice> devices = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "id" -> id = in.nextString();
                    case "address" -> address = in.nextInt();
                    case "name" -> name = in.nextString();
                    case "mediumTypeRefId" -> medium = in.nextString();
                    case "segments" -> segments = list(this::segment);
                    case "devices" -> devices = list(this::device);
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxLine(need(id), need(address), need(name), medium, need(segments), need(devices));
        }

        private KnxSegment segment() throws IOException {
            String id = null;
            Integer number = null;
            String medium = null;
            List<KnxDevice> devices = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "id" -> id = in.nextString();
                    case "number" -> number = in.nextInt();
                    case "mediumTypeRefId" -> medium = in.nextString();
                    case "devices" -> devices = list(this::device);
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxSegment(need(id), number, medium, need(devices));
        }

        private KnxDevice device() throws IOException {
            String id = null;
            KnxIndividualAddress individual = null;
            String name = null;
            String description = null;
            String product = null;
            String hardware2Program = null;
            String medium = null;
            List<KnxObjectInstance> objects = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "id" -> id = in.nextString();
                    case "individualAddress" -> individual = individualAddress();
                    case "name" -> name = in.nextString();
                    case "description" -> description = in.nextString();
                    case "productRefId" -> product = in.nextString();
                    case "hardware2ProgramRefId" -> hardware2Program = in.nextString();
                    case "mediumTypeRefId" -> medium = in.nextString();
                    case "objects" -> objects = list(this::objectInstance);
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxDevice(need(id), individual, need(name), description, product, hardware2Program, medium,
                    need(objects), List.of(), List.of());
        }

        private KnxIndividualAddress individualAddress() throws IOException {
            Integer area = null;
            Integer line = null;
            Integer device = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "area" -> area = in.nextInt();
                    case "line" -> line = in.nextInt();
                    case "device" -> device = in.nextInt();
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxIndividualAddress(need(area), need(line), need(device));
        }

        private KnxObjectInstance objectInstance() throws IOException {
            String refId = null;
            List<String> addressIds = null;
            String sending = null;
            String text = null;
            String functionText = null;
            String description = null;
            List<KnxDatapointType> types = null;
            String channelId = null;
            KnxObjectFlags flags = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "refId" -> refId = in.nextString();
                    case "groupAddressIds" -> addressIds = strings();
                    case "sendingGroupAddressId" -> sending = in.nextString();
                    case "text" -> text = in.nextString();
                    case "functionText" -> functionText = in.nextString();
                    case "description" -> description = in.nextString();
                    case "datapointTypes" -> types = list(this::datapointType);
                    case "channelId" -> channelId = in.nextString();
                    case "flags" -> flags = objectFlags();
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxObjectInstance(need(refId), need(addressIds), sending, text, functionText, description,
                    need(types), channelId, need(flags));
        }

        private KnxObjectFlags objectFlags() throws IOException {
            String read = null;
            String write = null;
            String communication = null;
            String transmit = null;
            String update = null;
            String readOnInit = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "read" -> read = in.nextString();
                    case "write" -> write = in.nextString();
                    case "communication" -> communication = in.nextString();
                    case "transmit" -> transmit = in.nextString();
                    case "update" -> update = in.nextString();
                    case "readOnInit" -> readOnInit = in.nextString();
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxObjectFlags(read, write, communication, transmit, update, readOnInit);
        }

        private KnxSpace space(int depth) throws IOException {
            deepest(depth);
            String id = null;
            String name = null;
            String type = null;
            String number = null;
            String description = null;
            String usage = null;
            List<KnxSpace> spaces = null;
            List<String> deviceIds = null;
            List<KnxFunction> functions = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "id" -> id = in.nextString();
                    case "name" -> name = in.nextString();
                    case "type" -> type = in.nextString();
                    case "number" -> number = in.nextString();
                    case "description" -> description = in.nextString();
                    case "usage" -> usage = in.nextString();
                    case "spaces" -> spaces = list(() -> space(depth + 1));
                    case "deviceIds" -> deviceIds = strings();
                    case "functions" -> functions = list(this::function);
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxSpace(need(id), need(name), need(type), number, description, usage, need(spaces),
                    need(deviceIds), need(functions));
        }

        private KnxFunction function() throws IOException {
            String id = null;
            String name = null;
            String type = null;
            List<KnxFunctionGroupAddress> addresses = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "id" -> id = in.nextString();
                    case "name" -> name = in.nextString();
                    case "type" -> type = in.nextString();
                    case "groupAddresses" -> addresses = list(this::functionAddress);
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxFunction(need(id), need(name), need(type), need(addresses));
        }

        private KnxFunctionGroupAddress functionAddress() throws IOException {
            String id = null;
            String name = null;
            String groupAddressId = null;
            String role = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "id" -> id = in.nextString();
                    case "name" -> name = in.nextString();
                    case "groupAddressId" -> groupAddressId = in.nextString();
                    case "role" -> role = in.nextString();
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxFunctionGroupAddress(need(id), need(name), need(groupAddressId), role);
        }

        private KnxProduct product() throws IOException {
            String id = null;
            String text = null;
            String orderNumber = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "id" -> id = in.nextString();
                    case "text" -> text = in.nextString();
                    case "orderNumber" -> orderNumber = in.nextString();
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxProduct(need(id), text, orderNumber);
        }

        private KnxGroupObject groupObject() throws IOException {
            String deviceId = null;
            KnxIndividualAddress deviceAddress = null;
            KnxObjectInstance instance = null;
            Integer number = null;
            String text = null;
            String name = null;
            String functionText = null;
            List<KnxDatapointType> types = null;
            String sizeText = null;
            Integer sizeBits = null;
            KnxGroupObjectFlags flags = null;
            List<String> addressIds = null;
            String sending = null;
            String channelId = null;
            String moduleInstanceId = null;
            KnxObjectDefinition definition = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "deviceId" -> deviceId = in.nextString();
                    case "deviceAddress" -> deviceAddress = individualAddress();
                    case "instance" -> instance = objectInstance();
                    case "number" -> number = in.nextInt();
                    case "text" -> text = in.nextString();
                    case "name" -> name = in.nextString();
                    case "functionText" -> functionText = in.nextString();
                    case "datapointTypes" -> types = list(this::datapointType);
                    case "sizeText" -> sizeText = in.nextString();
                    case "sizeBits" -> sizeBits = in.nextInt();
                    case "flags" -> flags = groupObjectFlags();
                    case "groupAddressIds" -> addressIds = strings();
                    case "sendingGroupAddressId" -> sending = in.nextString();
                    case "channelId" -> channelId = in.nextString();
                    case "moduleInstanceId" -> moduleInstanceId = in.nextString();
                    case "definition" -> definition = constant(KnxObjectDefinition.class, in.nextString());
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxGroupObject(need(deviceId), deviceAddress, need(instance), number, text, need(name),
                    functionText, need(types), sizeText, sizeBits, need(flags), need(addressIds), sending, channelId,
                    moduleInstanceId, need(definition));
        }

        private KnxGroupObjectFlags groupObjectFlags() throws IOException {
            Boolean read = null;
            Boolean write = null;
            Boolean communication = null;
            Boolean transmit = null;
            Boolean update = null;
            Boolean readOnInit = null;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "read" -> read = in.nextBoolean();
                    case "write" -> write = in.nextBoolean();
                    case "communication" -> communication = in.nextBoolean();
                    case "transmit" -> transmit = in.nextBoolean();
                    case "update" -> update = in.nextBoolean();
                    case "readOnInit" -> readOnInit = in.nextBoolean();
                    default -> unknown();
                }
            }
            in.endObject();
            return new KnxGroupObjectFlags(need(read), need(write), need(communication), need(transmit), need(update),
                    need(readOnInit));
        }
    }
}
