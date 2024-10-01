/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc1;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcMeter;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.MeterType;

/**
 * The {@link NhcMeter1} class represents the meter Niko Home Control communication object. It contains all fields
 * representing a Niko Home Control meter and has methods to receive meter information.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcMeter1 extends NhcMeter {

    private static final Map<String, String> TYPE = Map.of("0", "Global", "1", "Submeasurement", "2", "Producer");

    private final String meterType;

    NhcMeter1(String id, String name, MeterType meterType, @Nullable String location, String type,
            @Nullable LocalDateTime referenceDate, NikoHomeControlCommunication nhcComm,
            ScheduledExecutorService scheduler) {
        super(id, name, meterType, referenceDate, location, nhcComm, scheduler);

        this.meterType = TYPE.getOrDefault(type, "");
    }

    /**
     * @return type of meter: Global, Submeasurement or Producer
     */
    public String getMeterType() {
        return meterType;
    }
}
