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
 * A group object of a device as the project stores it ({@code ComObjectInstanceRef}). Object number, size and
 * the texts of the application program are in the manufacturer files; what is here are the links and what the
 * project overrides on the instance.
 *
 * @param refId the object reference as written: short ({@code O-n_R-n}, module forms included) from ETS 5, the
 *            full {@code M-xxxx_A-…_O-n_R-n} in ETS 4
 * @param groupAddressIds the linked group addresses in file order, the sending one first. The short form
 *            {@code GA-n}, whichever form the file used; {@link KnxProject#groupAddress(String)} resolves it
 * @param sendingGroupAddressId the sending address: the first of the {@code Links}, or the first {@code Send}
 *            connector in ETS 4; {@code null} when nothing is linked or ETS 4 gives no {@code Send}
 * @param text the object text overridden on the instance. May hold {@code {{…}}} placeholders, which are not
 *            filled here
 * @param functionText the function text overridden on the instance
 * @param description the description of the instance
 * @param datapointTypes the datapoint types of the instance, empty when it does not override them
 * @param channelId the channel of the object ({@code CH-n} or a module form), if any
 * @param flags the flag overrides of the instance
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxObjectInstance(String refId, List<String> groupAddressIds, @Nullable String sendingGroupAddressId,
        @Nullable String text, @Nullable String functionText, @Nullable String description,
        List<KnxDatapointType> datapointTypes, @Nullable String channelId, KnxObjectFlags flags) {

    public KnxObjectInstance {
        groupAddressIds = List.copyOf(groupAddressIds);
        datapointTypes = List.copyOf(datapointTypes);
    }

    public boolean isLinked() {
        return !groupAddressIds.isEmpty();
    }
}
