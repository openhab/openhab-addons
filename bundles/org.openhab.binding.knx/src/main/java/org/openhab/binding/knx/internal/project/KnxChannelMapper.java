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
import org.openhab.binding.knx.internal.project.AddressTypes.AddressType;
import org.openhab.binding.knx.internal.project.ChannelTypeTable.Row;

/**
 * Makes the channel text of the binding ({@code ga="9.001:<1/2/3"}) from a project, for a group address
 * or for the objects of a device. Pairing a command with its status is not done here.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class KnxChannelMapper {

    public static final String NOTE_NO_DATA_TYPE = "no data type";
    public static final String NOTE_NO_SUB_NUMBER = "type without sub number";
    public static final String NOTE_OBJECTS_DISAGREE = "linked objects disagree on the type";
    public static final String NOTE_NO_LINKED_OBJECT = "no linked object";

    private static final String SEPARATOR = " – ";
    private static final String DEFAULT_KEY = "ga";

    private final KnxProject project;

    public KnxChannelMapper(KnxProject project) {
        this.project = project;
    }

    /** The text for a group address: the address alone, no further address from the links of its objects. */
    public KnxChannelSuggestion forGroupAddress(KnxGroupAddress address) {
        String label = address.name().isBlank() ? address.address() : address.name();
        return suggest(address, label, null);
    }

    /** One entry per linked object of the device, in instance order, for the address the object sends on. */
    public List<KnxChannelSuggestion> forDevice(KnxDevice device) {
        List<KnxChannelSuggestion> entries = new ArrayList<>();
        for (KnxGroupObject object : project.groupObjectsOf(device)) {
            if (!object.isLinked()) {
                continue;
            }
            String id = answeringAddressId(object);
            KnxGroupAddress address = id == null ? null : project.groupAddress(id);
            if (address != null) {
                entries.add(suggest(address, label(object), object));
            }
        }
        return entries;
    }

    private KnxChannelSuggestion suggest(KnxGroupAddress address, String label, @Nullable KnxGroupObject object) {
        AddressType resolved = AddressTypes.resolve(project, address);
        KnxDatapointType type = resolved.type();
        StringBuilder value = new StringBuilder();
        if (resolved.writable() && type != null) {
            value.append(type).append(':');
        }
        if (canRead(address)) {
            value.append('<');
        }
        value.append(address.address());
        Row row = resolved.writable() && type != null ? ChannelTypeTable.lookup(type) : null;
        return new KnxChannelSuggestion(address, row == null ? DEFAULT_KEY : row.key(), value.toString(),
                row == null ? null : row.channelType(), label, object, resolved.notes());
    }

    /** Whether the address is the sending address of a linked object with read and transmit and without write. */
    boolean isStatus(KnxGroupAddress address) {
        String own = ProjectIds.shortId(address.id());
        for (KnxGroupObject object : project.groupObjectsLinkedTo(address)) {
            String sending = object.sendingGroupAddressId();
            if (sending != null && ProjectIds.shortId(sending).equals(own) && object.flags().read()
                    && object.flags().transmit() && !object.flags().write()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether an object answers a read on the address: read and communication flags are enabled and the address is
     * the one the object answers on. A read on a further link is answered on another address, and an object of a
     * filter device answers nothing.
     */
    boolean canRead(KnxGroupAddress address) {
        String own = ProjectIds.shortId(address.id());
        for (KnxGroupObject object : project.groupObjectsLinkedTo(address)) {
            String answering = answeringAddressId(object);
            if (object.flags().read() && object.flags().communication() && answering != null
                    && ProjectIds.shortId(answering).equals(own) && !onFilterDevice(object)) {
                return true;
            }
        }
        return false;
    }

    private boolean onFilterDevice(KnxGroupObject object) {
        KnxDevice device = project.deviceById(object.deviceId());
        return device != null && project.isFilterDevice(device);
    }

    /** The address the object sends and answers on: its sending address, its first link when it has none. */
    private static @Nullable String answeringAddressId(KnxGroupObject object) {
        String sending = object.sendingGroupAddressId();
        if (sending != null) {
            return sending;
        }
        return object.isLinked() ? object.groupAddressIds().get(0) : null;
    }

    static String label(KnxGroupObject object) {
        List<String> parts = new ArrayList<>(3);
        Integer number = object.number();
        if (number != null) {
            parts.add(number.toString());
        }
        if (!object.name().isBlank()) {
            parts.add(object.name());
        }
        String function = object.functionText();
        if (function != null && !function.isBlank()) {
            parts.add(function);
        }
        return String.join(SEPARATOR, parts);
    }
}
