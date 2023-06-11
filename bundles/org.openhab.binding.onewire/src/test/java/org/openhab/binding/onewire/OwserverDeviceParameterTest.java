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
package org.openhab.binding.onewire;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;

/**
 * Tests cases for {@link SensorId}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class OwserverDeviceParameterTest {
    private final SensorId sensorId = new SensorId("/1F.0123456789ab/main/00.1234567890ab");

    @Test
    public void withoutPrefixTest() {
        OwserverDeviceParameter owserverDeviceParameter = new OwserverDeviceParameter("/humidity");
        assertEquals("/1F.0123456789ab/main/00.1234567890ab/humidity", owserverDeviceParameter.getPath(sensorId));

        owserverDeviceParameter = new OwserverDeviceParameter("humidity");
        assertEquals("/1F.0123456789ab/main/00.1234567890ab/humidity", owserverDeviceParameter.getPath(sensorId));
    }

    public void withPrefixTest() {
        OwserverDeviceParameter owserverDeviceParameter = new OwserverDeviceParameter("uncached", "/humidity");
        assertEquals("/uncached/1F.0123456789ab/main/00.1234567890ab/humidity",
                owserverDeviceParameter.getPath(sensorId));

        owserverDeviceParameter = new OwserverDeviceParameter("uncached", "/humidity");
        assertEquals("/uncached/1F.0123456789ab/main/00.1234567890ab/humidity",
                owserverDeviceParameter.getPath(sensorId));

        owserverDeviceParameter = new OwserverDeviceParameter("/uncached", "/humidity");
        assertEquals("/uncached/1F.0123456789ab/main/00.1234567890ab/humidity",
                owserverDeviceParameter.getPath(sensorId));

        owserverDeviceParameter = new OwserverDeviceParameter("/uncached/", "/humidity");
        assertEquals("/uncached/1F.0123456789ab/main/00.1234567890ab/humidity",
                owserverDeviceParameter.getPath(sensorId));

        owserverDeviceParameter = new OwserverDeviceParameter("uncached/", "/humidity");
        assertEquals("/uncached/1F.0123456789ab/main/00.1234567890ab/humidity",
                owserverDeviceParameter.getPath(sensorId));
    }
}
