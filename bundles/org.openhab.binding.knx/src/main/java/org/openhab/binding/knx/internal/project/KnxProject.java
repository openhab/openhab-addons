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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An ETS project as read from a {@code .knxproj} file. Holds no key, password or other secret of the project.
 * Immutable; the lookups use indexes built once when the project is created. A class and not a record because
 * of those indexes; the accessors are named as record accessors would be.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class KnxProject {

    private static final String FILTER_WORD = "dummy";

    private final KnxProjectInfo info;
    private final String installationName;
    private final List<KnxGroupRange> groupRanges;
    private final List<KnxGroupAddress> groupAddresses;
    private final KnxTopology topology;
    private final List<KnxSpace> locations;
    private final int ignoredInstallations;
    private final List<KnxProduct> products;
    private final List<KnxGroupObject> groupObjects;

    private final Map<Integer, KnxGroupAddress> addressByRaw = new HashMap<>();
    private final Map<String, KnxGroupAddress> addressByShortId = new HashMap<>();
    private final Map<KnxIndividualAddress, KnxDevice> deviceByAddress = new HashMap<>();
    private final Map<String, KnxDevice> deviceById = new HashMap<>();
    private final Map<String, List<KnxLinkedObject>> objectsByAddress = new HashMap<>();
    private final Map<String, KnxSpace> spaceByDeviceId = new HashMap<>();
    private final Map<String, KnxProduct> productById = new HashMap<>();
    private final Map<String, List<KnxGroupObject>> groupObjectsByDevice = new HashMap<>();
    private final Map<String, List<KnxGroupObject>> groupObjectsByAddress = new HashMap<>();

    /**
     * @param info what {@code project.xml} says about the project
     * @param installationName the name of the installation that was read, {@code ""} when it has none
     * @param groupRanges the top level of the group range tree
     * @param groupAddresses every group address of the installation in file order, also those outside a range
     * @param topology areas, lines, segments and devices
     * @param locations the top level of the building tree, {@code Locations} or, in ETS 4, {@code Buildings}
     * @param ignoredInstallations further installations of the project that were not read
     */
    public KnxProject(KnxProjectInfo info, String installationName, List<KnxGroupRange> groupRanges,
            List<KnxGroupAddress> groupAddresses, KnxTopology topology, List<KnxSpace> locations,
            int ignoredInstallations) {
        this(info, installationName, groupRanges, groupAddresses, topology, locations, ignoredInstallations, List.of(),
                List.of());
    }

    /**
     * @param products the products the devices refer to, from the manufacturers' {@code Hardware.xml}
     * @param groupObjects one per object instance of a device, devices in topology order, then instance order
     */
    public KnxProject(KnxProjectInfo info, String installationName, List<KnxGroupRange> groupRanges,
            List<KnxGroupAddress> groupAddresses, KnxTopology topology, List<KnxSpace> locations,
            int ignoredInstallations, List<KnxProduct> products, List<KnxGroupObject> groupObjects) {
        this.info = info;
        this.installationName = installationName;
        this.groupRanges = List.copyOf(groupRanges);
        this.groupAddresses = List.copyOf(groupAddresses);
        this.topology = topology;
        this.locations = List.copyOf(locations);
        this.ignoredInstallations = ignoredInstallations;
        this.products = List.copyOf(products);
        this.groupObjects = List.copyOf(groupObjects);
        buildIndexes();
    }

    /** The same project with what the manufacturer files add to it. */
    KnxProject withManufacturerData(List<KnxProduct> products, List<KnxGroupObject> groupObjects) {
        return new KnxProject(info, installationName, groupRanges, groupAddresses, topology, locations,
                ignoredInstallations, products, groupObjects);
    }

    private void buildIndexes() {
        for (KnxGroupAddress address : groupAddresses) {
            addressByRaw.putIfAbsent(address.rawAddress(), address);
            addressByShortId.putIfAbsent(ProjectIds.shortId(address.id()), address);
        }
        for (KnxDevice device : topology.devices()) {
            deviceById.putIfAbsent(device.id(), device);
            KnxIndividualAddress individual = device.individualAddress();
            if (individual != null) {
                deviceByAddress.putIfAbsent(individual, device);
            }
            for (KnxObjectInstance object : device.objects()) {
                KnxLinkedObject linked = new KnxLinkedObject(device, object);
                for (String id : object.groupAddressIds().stream().distinct().toList()) {
                    List<KnxLinkedObject> list = objectsByAddress.get(ProjectIds.shortId(id));
                    if (list == null) {
                        list = new ArrayList<>();
                        objectsByAddress.put(ProjectIds.shortId(id), list);
                    }
                    list.add(linked);
                }
            }
        }
        for (KnxProduct product : products) {
            productById.putIfAbsent(product.id(), product);
        }
        for (KnxGroupObject object : groupObjects) {
            List<KnxGroupObject> list = groupObjectsByDevice.get(object.deviceId());
            if (list == null) {
                list = new ArrayList<>();
                groupObjectsByDevice.put(object.deviceId(), list);
            }
            list.add(object);
            for (String id : object.groupAddressIds().stream().map(ProjectIds::shortId).distinct().toList()) {
                List<KnxGroupObject> linked = groupObjectsByAddress.get(id);
                if (linked == null) {
                    linked = new ArrayList<>();
                    groupObjectsByAddress.put(id, linked);
                }
                linked.add(object);
            }
        }
        Deque<KnxSpace> pending = new ArrayDeque<>();
        for (int i = locations.size() - 1; i >= 0; i--) {
            pending.push(locations.get(i));
        }
        while (!pending.isEmpty()) {
            KnxSpace space = pending.pop();
            for (String id : space.deviceIds()) {
                spaceByDeviceId.putIfAbsent(id, space);
            }
            for (int i = space.spaces().size() - 1; i >= 0; i--) {
                pending.push(space.spaces().get(i));
            }
        }
    }

    public KnxProjectInfo info() {
        return info;
    }

    /** The name of the installation that was read, {@code ""} when it has none. */
    public String installationName() {
        return installationName;
    }

    /** The top level of the group range tree. */
    public List<KnxGroupRange> groupRanges() {
        return groupRanges;
    }

    /** Every group address of the installation in file order, also those outside a range. */
    public List<KnxGroupAddress> groupAddresses() {
        return groupAddresses;
    }

    /** Areas, lines, segments and devices. */
    public KnxTopology topology() {
        return topology;
    }

    /** The top level of the building tree. */
    public List<KnxSpace> locations() {
        return locations;
    }

    /** Further installations of the project that were not read. */
    public int ignoredInstallations() {
        return ignoredInstallations;
    }

    /** The group address with this raw 16-bit number; the first one if the file holds several. */
    public @Nullable KnxGroupAddress groupAddress(int rawAddress) {
        return addressByRaw.get(rawAddress);
    }

    /** The group address with this id, written in the full ({@code P-xxxx-n_GA-n}) or the short form. */
    public @Nullable KnxGroupAddress groupAddress(String id) {
        return addressByShortId.get(ProjectIds.shortId(id));
    }

    public @Nullable KnxDevice device(KnxIndividualAddress address) {
        return deviceByAddress.get(address);
    }

    /** The device with this full id, {@link KnxDevice#id()}. */
    public @Nullable KnxDevice deviceById(String id) {
        return deviceById.get(id);
    }

    /**
     * The objects linked to a group address, devices in topology order and objects in file order, each object
     * once. Sending and listening objects are not told apart here, see
     * {@link KnxObjectInstance#sendingGroupAddressId()}.
     */
    public List<KnxLinkedObject> objectsLinkedTo(KnxGroupAddress address) {
        List<KnxLinkedObject> linked = objectsByAddress.get(ProjectIds.shortId(address.id()));
        return linked == null ? List.of() : List.copyOf(linked);
    }

    /** The space the device is placed in; {@code null} when it is in none. */
    public @Nullable KnxSpace spaceOf(KnxDevice device) {
        return spaceByDeviceId.get(device.id());
    }

    /** The devices placed in the space itself, not those of the spaces inside it. Unknown ids are left out. */
    public List<KnxDevice> devicesIn(KnxSpace space) {
        List<KnxDevice> devices = new ArrayList<>();
        for (String id : space.deviceIds()) {
            KnxDevice device = deviceById.get(id);
            if (device != null) {
                devices.add(device);
            }
        }
        return List.copyOf(devices);
    }

    /** The functions defined in the space itself, not those of the spaces inside it. */
    public List<KnxFunction> functionsOf(KnxSpace space) {
        return space.functions();
    }

    /** The products the devices refer to, as far as the manufacturer files hold them. */
    public List<KnxProduct> products() {
        return products;
    }

    /** One group object per object instance of a device, devices in topology order, then instance order. */
    public List<KnxGroupObject> groupObjects() {
        return groupObjects;
    }

    /** The group objects of the device, in instance order. */
    public List<KnxGroupObject> groupObjectsOf(KnxDevice device) {
        List<KnxGroupObject> objects = groupObjectsByDevice.get(device.id());
        return objects == null ? List.of() : List.copyOf(objects);
    }

    /**
     * The group objects linked to a group address, in the order of {@link #objectsLinkedTo(KnxGroupAddress)},
     * each object once.
     */
    public List<KnxGroupObject> groupObjectsLinkedTo(KnxGroupAddress address) {
        List<KnxGroupObject> linked = groupObjectsByAddress.get(ProjectIds.shortId(address.id()));
        return linked == null ? List.of() : List.copyOf(linked);
    }

    /**
     * The type of the values of a group object: the datapoint type of the group address it sends on, else the
     * object's own first type, else none; with the object's size in every case.
     */
    public KnxObjectValueType datapointTypeOf(KnxGroupObject object) {
        String sending = object.sendingGroupAddressId();
        KnxGroupAddress address = sending == null ? null : groupAddress(sending);
        KnxDatapointType type = address == null ? null : address.datapointType();
        return new KnxObjectValueType(type != null ? type : object.datapointType(), object.sizeBits());
    }

    /** The product of the device; {@code null} when the manufacturer files do not hold it. */
    public @Nullable KnxProduct productOf(KnxDevice device) {
        String id = device.productRefId();
        return id == null ? null : productById.get(id);
    }

    /**
     * Whether the device is a filter device: no hardware, it only fills a line coupler's filter table and never
     * answers. Told by the product alone, an order number {@code dummy} or the word in the product text; the
     * name and description the installer typed are not looked at.
     */
    public boolean isFilterDevice(KnxDevice device) {
        KnxProduct product = productOf(device);
        if (product == null) {
            return false;
        }
        String orderNumber = product.orderNumber();
        if (orderNumber != null && FILTER_WORD.equalsIgnoreCase(orderNumber.strip())) {
            return true;
        }
        String text = product.text();
        return text != null && holdsFilterWord(text);
    }

    private static boolean holdsFilterWord(String text) {
        int length = FILTER_WORD.length();
        for (int at = 0; at + length <= text.length(); at++) {
            if (text.regionMatches(true, at, FILTER_WORD, 0, length)
                    && (at == 0 || !Character.isLetter(text.charAt(at - 1)))
                    && (at + length == text.length() || !Character.isLetter(text.charAt(at + length)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * The name to show for a device: its own name when it has one, else the product text, else the order
     * number, else {@code ""}. Most devices have no own name.
     */
    public String displayNameOf(KnxDevice device) {
        if (!device.name().isBlank()) {
            return device.name();
        }
        KnxProduct product = productOf(device);
        if (product != null) {
            String text = product.text();
            if (text != null && !text.isBlank()) {
                return text;
            }
            String orderNumber = product.orderNumber();
            if (orderNumber != null && !orderNumber.isBlank()) {
                return orderNumber;
            }
        }
        return "";
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof KnxProject o && info.equals(o.info) && installationName.equals(o.installationName)
                && groupRanges.equals(o.groupRanges) && groupAddresses.equals(o.groupAddresses)
                && topology.equals(o.topology) && locations.equals(o.locations)
                && ignoredInstallations == o.ignoredInstallations && products.equals(o.products)
                && groupObjects.equals(o.groupObjects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(info, installationName, groupRanges, groupAddresses, topology, locations,
                ignoredInstallations, products, groupObjects);
    }

    @Override
    public String toString() {
        return "KnxProject[info=" + info + ", installationName=" + installationName + ", groupRanges=" + groupRanges
                + ", groupAddresses=" + groupAddresses + ", topology=" + topology + ", locations=" + locations
                + ", ignoredInstallations=" + ignoredInstallations + ", products=" + products + ", groupObjects="
                + groupObjects + "]";
    }
}
