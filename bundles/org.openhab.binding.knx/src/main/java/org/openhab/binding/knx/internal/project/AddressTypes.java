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
 * The datapoint type of a group address as the channel text and the project check see it. One rule for both.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class AddressTypes {

    /**
     * @param type the type found, with or without sub number; {@code null} when none
     * @param writable whether the channel text can hold the type: it has a sub number in the written range
     * @param notes why the type is missing or incomplete, in the words of {@link KnxChannelMapper}
     */
    record AddressType(@Nullable KnxDatapointType type, boolean writable, List<String> notes) {
    }

    private AddressTypes() {
    }

    /**
     * The address's own type; else the type the linked objects agree on (all objects with an own type have the
     * same first type). A type without sub number is replaced by a full type of the same main number that the
     * objects agree on.
     */
    static AddressType resolve(KnxProject project, KnxGroupAddress address) {
        List<KnxGroupObject> linked = project.groupObjectsLinkedTo(address);
        KnxDatapointType agreed = null;
        boolean disagree = false;
        for (KnxGroupObject object : linked) {
            KnxDatapointType own = object.datapointType();
            if (own == null) {
                continue;
            }
            if (agreed == null) {
                agreed = own;
            } else if (!agreed.equals(own)) {
                disagree = true;
            }
        }
        if (disagree) {
            agreed = null;
        }
        List<String> notes = new ArrayList<>(2);
        KnxDatapointType type;
        KnxDatapointType ownType = address.datapointType();
        if (ownType != null) {
            if (ownType.sub() != null) {
                type = ownType;
            } else if (agreed != null && agreed.sub() != null && agreed.main() == ownType.main()) {
                type = agreed;
            } else {
                type = ownType;
                notes.add(KnxChannelMapper.NOTE_NO_SUB_NUMBER);
                if (disagree) {
                    notes.add(KnxChannelMapper.NOTE_OBJECTS_DISAGREE);
                }
            }
        } else if (disagree) {
            type = null;
            notes.add(KnxChannelMapper.NOTE_OBJECTS_DISAGREE);
        } else if (agreed == null) {
            type = null;
            notes.add(KnxChannelMapper.NOTE_NO_DATA_TYPE);
        } else {
            type = agreed;
            if (type.sub() == null) {
                notes.add(KnxChannelMapper.NOTE_NO_SUB_NUMBER);
            }
        }
        boolean writable = type != null && fits(type);
        if (type != null && type.sub() != null && !writable) {
            notes.add(KnxChannelMapper.NOTE_NO_DATA_TYPE);
        }
        if (linked.isEmpty()) {
            notes.add(KnxChannelMapper.NOTE_NO_LINKED_OBJECT);
        }
        return new AddressType(type, writable, notes);
    }

    /** The range the binding's text format reads: 1 to 3 digits of main number, 3 to 5 of sub number. */
    private static boolean fits(KnxDatapointType type) {
        Integer sub = type.sub();
        return sub != null && type.main() >= 0 && type.main() <= 999 && sub >= 0 && sub <= 99999;
    }
}
