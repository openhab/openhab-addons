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
 * A space of the building tree: {@code Space} from ETS 5, {@code BuildingPart} in ETS 4.
 *
 * @param id the id
 * @param name the name
 * @param type the type as written: {@code Building}, {@code BuildingPart}, {@code Floor}, {@code Room},
 *            {@code DistributionBoard}, or any other text a newer ETS writes; {@code ""} when missing
 * @param number the number as written, if any
 * @param description the description, if any
 * @param usage the usage as written ({@code SU-n}); the text for it is in {@code knx_master.xml}
 * @param spaces the spaces inside this one
 * @param deviceIds the full ids of the devices placed here, as {@link KnxDevice#id()}; an id may name a device
 *            the project does not hold
 * @param functions the functions defined directly in this space
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxSpace(String id, String name, String type, @Nullable String number, @Nullable String description,
        @Nullable String usage, List<KnxSpace> spaces, List<String> deviceIds, List<KnxFunction> functions) {

    public KnxSpace {
        spaces = List.copyOf(spaces);
        deviceIds = List.copyOf(deviceIds);
        functions = List.copyOf(functions);
    }
}
