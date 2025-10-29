/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link NhcMeterReading2} represents a Niko Home Control II meter readings. It is used when parsing meter reading
 * responses from http meter reading requests.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
class NhcMeterReading2 {
    static class NhcProperty {
        @Nullable
        String property;
        @Nullable
        String unit;
        @Nullable
        List<NhcMeterValue> values;
    }

    static class NhcMeterValue {
        @Nullable
        String dateTime;
        double value;
    }

    String deviceUuid = "";
    @Nullable
    List<NhcProperty> properties;
}
