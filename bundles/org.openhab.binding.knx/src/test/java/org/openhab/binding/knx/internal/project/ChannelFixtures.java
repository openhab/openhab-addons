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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Builds small made-up projects for the channel text and project check tests from the public model only.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class ChannelFixtures {

    private ChannelFixtures() {
    }

    /** The raw number of {@code main/middle/sub}. */
    public static int raw(int main, int middle, int sub) {
        return (main << 11) | (middle << 8) | sub;
    }

    /** A group address; {@code dpt} is {@code "9.001"}, {@code "9"} or null. The id is {@code P-1_GA-<n>}. */
    public static KnxGroupAddress address(KnxGroupAddressStyle style, int n, int raw, @Nullable String dpt) {
        List<KnxDatapointType> types = dpt == null ? List.of() : List.of(type(dpt));
        return new KnxGroupAddress("P-1_GA-" + n, raw, style.format(raw), "Address " + n, null, types, false,
                List.of());
    }

    public static KnxDatapointType type(String text) {
        int dot = text.indexOf('.');
        return dot < 0 ? new KnxDatapointType(Integer.parseInt(text), null)
                : new KnxDatapointType(Integer.parseInt(text.substring(0, dot)),
                        Integer.parseInt(text.substring(dot + 1)));
    }

    /** One object of a device. */
    public static final class Obj {
        final String id;
        final @Nullable Integer number;
        final String name;
        final @Nullable String functionText;
        final List<Integer> links;
        final @Nullable String dpt;
        boolean read;
        boolean communication = true;
        boolean write;
        boolean transmit;
        boolean update;
        @Nullable
        String channelId;
        @Nullable
        String moduleInstanceId;
        KnxObjectDefinition definition = KnxObjectDefinition.FOUND;
        boolean receiveOnly;

        /** @param links the numbers of the linked addresses, the sending one first */
        public Obj(String id, @Nullable Integer number, String name, @Nullable String functionText, List<Integer> links,
                @Nullable String dpt) {
            this.id = id;
            this.number = number;
            this.name = name;
            this.functionText = functionText;
            this.links = links;
            this.dpt = dpt;
        }

        public Obj read() {
            this.read = true;
            return this;
        }

        public Obj noCommunication() {
            this.communication = false;
            return this;
        }

        public Obj definition(KnxObjectDefinition definition) {
            this.definition = definition;
            return this;
        }

        /** ETS 4 style: the links are receive connectors only, no sending address. */
        public Obj receiveOnly() {
            this.receiveOnly = true;
            return this;
        }

        /** Sets all flags as ETS writes them, {@code CRWTU} with {@code -} for an off flag, e.g. {@code CR-T-}. */
        public Obj flags(String flags) {
            this.communication = flags.indexOf('C') >= 0;
            this.read = flags.indexOf('R') >= 0;
            this.write = flags.indexOf('W') >= 0;
            this.transmit = flags.indexOf('T') >= 0;
            this.update = flags.indexOf('U') >= 0;
            return this;
        }

        /** The device channel the object belongs to. */
        public Obj channel(String channelId) {
            this.channelId = channelId;
            return this;
        }

        /** The module instance the object sits in. */
        public Obj module(String moduleInstanceId) {
            this.moduleInstanceId = moduleInstanceId;
            return this;
        }
    }

    /** One device: its own name ({@code ""} for none), its individual address and its objects. */
    public record Dev(String name, @Nullable KnxIndividualAddress address, List<Obj> objects,
            @Nullable KnxProduct product) {

        public Dev(String name, @Nullable KnxIndividualAddress address, List<Obj> objects) {
            this(name, address, objects, null);
        }

        /** The same device as an instance of the product; the project gets the product too. */
        public Dev of(KnxProduct product) {
            return new Dev(name, address, objects, product);
        }
    }

    /** A made-up product whose order number is the one ETS gives its filter device. */
    public static KnxProduct filterProduct() {
        return new KnxProduct("M-00C8_H-F-1_P-F", "Made-up line filler", "dummy");
    }

    public static Dev device(String name, @Nullable KnxIndividualAddress address, Obj... objects) {
        return new Dev(name, address, List.of(objects));
    }

    /** A group address with its own name. */
    public static KnxGroupAddress address(int n, int raw, @Nullable String dpt, String name) {
        KnxGroupAddress plain = address(n, raw, dpt);
        return new KnxGroupAddress(plain.id(), raw, plain.address(), name, null, plain.datapointTypes(), false,
                List.of());
    }

    /** A space of type {@code Room}; {@code devices} are numbers of the devices, {@code 1} for {@code P-1_DI-1}. */
    public static KnxSpace space(String id, String name, List<KnxSpace> spaces, List<Integer> devices,
            List<KnxFunction> functions) {
        return new KnxSpace(id, name, "Room", null, null, null, spaces,
                devices.stream().map(d -> "P-1_DI-" + d).toList(), functions);
    }

    public static KnxFunction function(String id, String name, KnxFunctionGroupAddress... refs) {
        return new KnxFunction(id, name, "FT-1", List.of(refs));
    }

    /** A function's use of the group address {@code P-1_GA-<n>}. */
    public static KnxFunctionGroupAddress ref(int n, String name, @Nullable String role) {
        return new KnxFunctionGroupAddress("GR-" + n, name, "GA-" + n, role);
    }

    public static Obj object(String id, @Nullable Integer number, String name, @Nullable String functionText,
            @Nullable String dpt, Integer... links) {
        return new Obj(id, number, name, functionText, List.of(links), dpt);
    }

    /** A project with one device that holds the objects. */
    public static KnxProject project(KnxGroupAddressStyle style, List<KnxGroupAddress> addresses, List<Obj> objects) {
        return projectOfDevices(style, addresses, List.of(objects));
    }

    /** A project with one device per list of objects, {@code D-1}, {@code D-2}, ... */
    public static KnxProject projectOfDevices(KnxGroupAddressStyle style, List<KnxGroupAddress> addresses,
            List<List<Obj>> devices) {
        List<Dev> named = new ArrayList<>();
        for (int d = 0; d < devices.size(); d++) {
            named.add(new Dev("Device " + (d + 1), null, devices.get(d)));
        }
        return projectOf(style, addresses, named, List.of());
    }

    /** A project with the devices, {@code P-1_DI-1}, {@code P-1_DI-2}, ..., and the building tree. */
    public static KnxProject projectOf(List<KnxGroupAddress> addresses, List<Dev> devices, List<KnxSpace> locations) {
        return projectOf(KnxGroupAddressStyle.THREE_LEVEL, addresses, devices, locations);
    }

    public static KnxProject projectOf(KnxGroupAddressStyle style, List<KnxGroupAddress> addresses, List<Dev> devices,
            List<KnxSpace> locations) {
        List<KnxDevice> knxDevices = new ArrayList<>();
        List<KnxGroupObject> groupObjects = new ArrayList<>();
        List<KnxProduct> products = new ArrayList<>();
        for (int d = 0; d < devices.size(); d++) {
            String deviceId = "P-1_DI-" + (d + 1);
            Dev dev = devices.get(d);
            List<KnxObjectInstance> instances = new ArrayList<>();
            for (Obj o : dev.objects()) {
                List<String> ids = o.links.stream().map(n -> "GA-" + n).toList();
                @Nullable
                String sending = o.receiveOnly || ids.isEmpty() ? null : ids.get(0);
                KnxObjectInstance instance = new KnxObjectInstance(o.id, ids, sending, null, null, null, List.of(),
                        o.channelId, new KnxObjectFlags(null, null, null, null, null, null));
                instances.add(instance);
                String dpt = o.dpt;
                List<KnxDatapointType> types = dpt == null ? List.of() : List.of(type(dpt));
                KnxGroupObjectFlags flags = new KnxGroupObjectFlags(o.read, o.write, o.communication, o.transmit,
                        o.update, false);
                groupObjects.add(
                        new KnxGroupObject(deviceId, dev.address(), instance, o.number, o.name, o.name, o.functionText,
                                types, null, null, flags, ids, sending, o.channelId, o.moduleInstanceId, o.definition));
            }
            KnxProduct product = dev.product();
            if (product != null) {
                products.add(product);
            }
            knxDevices.add(new KnxDevice(deviceId, dev.address(), dev.name(), null,
                    product == null ? null : product.id(), null, null, instances, List.of(), List.of()));
        }
        KnxProjectInfo info = new KnxProjectInfo("P-1", "Made up", style, null, null, null, null);
        return new KnxProject(info, "Installation", List.of(), addresses, new KnxTopology(List.of(), knxDevices),
                locations, 0, products, groupObjects);
    }

    public static KnxProject project(List<KnxGroupAddress> addresses, List<Obj> objects) {
        return project(KnxGroupAddressStyle.THREE_LEVEL, addresses, objects);
    }

    public static KnxGroupAddress address(int n, int raw, @Nullable String dpt) {
        return address(KnxGroupAddressStyle.THREE_LEVEL, n, raw, dpt);
    }
}
