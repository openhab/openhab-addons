/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tplinksmarthome.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tplinksmarthome.internal.model.GetSysinfo;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;

/**
 * Test class for {@link PropertiesCollector} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class PropertiesCollectorTest {

    /**
     * Tests if properties for a bulb device are correctly parsed.
     *
     * @throws IOException exception in case device not reachable
     */
    @Test
    public void testBulbProperties() throws IOException {
        assertProperties("bulb_get_sysinfo_response_on", TPLinkSmartHomeThingType.LB130, 11);
    }

    /**
     * Tests if properties for a switch device are correctly parsed.
     *
     * @throws IOException exception in case device not reachable
     */
    @Test
    public void testSwitchProperties() throws IOException {
        assertProperties("plug_get_sysinfo_response", TPLinkSmartHomeThingType.HS100, 12);
    }

    /**
     * Tests if properties for a range extender device are correctly parsed.
     *
     * @throws IOException exception in case device not reachable
     */
    @Test
    public void testRangeExtenderProperties() throws IOException {
        assertProperties("rangeextender_get_sysinfo_response", TPLinkSmartHomeThingType.RE270K, 11);
    }

    private void assertProperties(String responseFile, TPLinkSmartHomeThingType thingType, int expectedSize)
            throws IOException {
        final Map<String, Object> props = PropertiesCollector.collectProperties(thingType, "localhost",
                ModelTestUtil.jsonFromFile(responseFile, GetSysinfo.class).getSysinfo());

        assertEquals(expectedSize, props.size(), "Number of properties not as expected for properties: " + props);
        props.entrySet().stream().forEach(
                entry -> assertNotNull(entry.getValue(), "Property '" + entry.getKey() + "' should not be null"));
    }
}
