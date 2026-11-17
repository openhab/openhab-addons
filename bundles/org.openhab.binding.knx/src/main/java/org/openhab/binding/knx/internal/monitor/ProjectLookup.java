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
package org.openhab.binding.knx.internal.monitor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * What the ETS project of a bridge says about addresses. The project store gives one per bridge; {@link #NONE}
 * knows nothing.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public interface ProjectLookup {

    /** Knows nothing. */
    ProjectLookup NONE = new ProjectLookup() {
        @Override
        public @Nullable String groupAddressName(GroupAddress address) {
            return null;
        }

        @Override
        public @Nullable String dataType(GroupAddress address) {
            return null;
        }

        @Override
        public @Nullable String deviceName(IndividualAddress address) {
            return null;
        }
    };

    /** {@return the name of the group address, null if unknown} */
    @Nullable
    String groupAddressName(GroupAddress address);

    /** {@return the datapoint type the project gives the group address, e.g. 9.001, null if none} */
    @Nullable
    String dataType(GroupAddress address);

    /** {@return the name of the device, null if unknown} */
    @Nullable
    String deviceName(IndividualAddress address);
}
