/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.arcam.internal.devices;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.arcam.internal.connection.ArcamCommandData;

/**
 * This class provides constants that are used by multiple devices.
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
public class ArcamDeviceConstants {
    public static final List<ArcamCommandData> ROOM_EQ = new ArrayList<>(List.of( //
            new ArcamCommandData("ROOM_EQ_OFF", (byte) 0x00), //
            new ArcamCommandData("ROOM_EQ_1", (byte) 0x01), //
            new ArcamCommandData("ROOM_EQ_2", (byte) 0x02), //
            new ArcamCommandData("ROOM_EQ_3", (byte) 0x03) //
    ));
}
