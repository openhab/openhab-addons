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
 * A device of the topology. Product text, application program and group objects are in the manufacturer
 * files; {@link #productRefId()} and {@link #hardware2ProgramRefId()} lead there.
 *
 * @param id the full id, {@code P-xxxx-n_DI-n}
 * @param individualAddress the individual address, {@code null} when the device has no address or sits
 *            outside a line
 * @param name the name the user gave, {@code ""} when none (most devices have none)
 * @param description the description, if any
 * @param productRefId the {@code Product@Id} in the manufacturer's {@code Hardware.xml}
 * @param hardware2ProgramRefId the {@code Hardware2Program@Id} in the manufacturer's {@code Hardware.xml}
 * @param mediumTypeRefId the medium of the segment or line the device sits on, if known
 * @param objects the object instances in file order
 * @param parameters the parameter values stored on the device in file order
 * @param modules the module instances in file order
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxDevice(String id, @Nullable KnxIndividualAddress individualAddress, String name,
        @Nullable String description, @Nullable String productRefId, @Nullable String hardware2ProgramRefId,
        @Nullable String mediumTypeRefId, List<KnxObjectInstance> objects, List<KnxParameterValue> parameters,
        List<KnxModuleInstance> modules) {

    public KnxDevice {
        objects = List.copyOf(objects);
        parameters = List.copyOf(parameters);
        modules = List.copyOf(modules);
    }

    /** The manufacturer id, {@code M-xxxx}: the part of {@link #productRefId()} before the first {@code _}. */
    public @Nullable String manufacturerRefId() {
        String product = productRefId;
        if (product == null) {
            return null;
        }
        int end = product.indexOf('_');
        return end > 0 ? product.substring(0, end) : null;
    }
}
