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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * One group object of a device: an object instance of the project together with what the manufacturer's
 * application program defines for it. Each value is taken from the first level that has it: the instance, then
 * the object reference ({@code ComObjectRef}), then the object ({@code ComObject}). Only when
 * {@link #definition()} is {@link KnxObjectDefinition#FOUND} do the program levels take part.
 *
 * @param deviceId the device's full id; {@link KnxProject#deviceById(String)} gives the device
 * @param deviceAddress the device's individual address, {@code null} when it has none
 * @param instance the object instance as the project stores it
 * @param number the object number of the device, as ETS shows it; inside a module the value of the object's
 *            {@code BaseNumber} argument plus the number in the module, {@code null} when the program gives no
 *            number or the base cannot be worked out, never the bare number inside the module
 * @param text the text, placeholders such as <code>{{0:A}}</code> left as they are, in the program's default
 *            language
 * @param name the name to show: the program's text in the chosen language with its placeholders filled; the
 *            instance's text when a placeholder cannot be filled or the program has no text; {@code ""} when
 *            there is none
 * @param functionText the function text, a program's text in the chosen language
 * @param datapointTypes the object's own datapoint types
 * @param sizeText the object size as written, for example {@code 1 Bit} or {@code 2 Bytes}
 * @param sizeBits the object size in bits, {@code null} when {@link #sizeText()} has another form
 * @param flags the effective flags
 * @param groupAddressIds the linked group addresses, short ids, the sending one first
 * @param sendingGroupAddressId the group address the object sends on
 * @param channelId the channel of the device the object belongs to
 * @param moduleInstanceId the module instance of an object inside a module, for example {@code MD-1_M-1_MI-2}
 * @param definition whether the program defines the object
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxGroupObject(String deviceId, @Nullable KnxIndividualAddress deviceAddress, KnxObjectInstance instance,
        @Nullable Integer number, @Nullable String text, String name, @Nullable String functionText,
        List<KnxDatapointType> datapointTypes, @Nullable String sizeText, @Nullable Integer sizeBits,
        KnxGroupObjectFlags flags, List<String> groupAddressIds, @Nullable String sendingGroupAddressId,
        @Nullable String channelId, @Nullable String moduleInstanceId, KnxObjectDefinition definition) {

    public KnxGroupObject {
        datapointTypes = List.copyOf(datapointTypes);
        groupAddressIds = List.copyOf(groupAddressIds);
    }

    /** The first own datapoint type, or {@code null} when no level gives one. */
    public @Nullable KnxDatapointType datapointType() {
        return datapointTypes.isEmpty() ? null : datapointTypes.get(0);
    }

    public boolean isLinked() {
        return !groupAddressIds.isEmpty();
    }
}
