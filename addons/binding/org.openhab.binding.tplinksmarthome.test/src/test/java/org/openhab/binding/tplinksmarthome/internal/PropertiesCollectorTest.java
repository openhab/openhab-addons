/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.junit.Test;
import org.openhab.binding.tplinksmarthome.internal.model.GetSysinfo;
import org.openhab.binding.tplinksmarthome.internal.model.GsonUtil;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;

import com.google.gson.Gson;

/**
 * Test class for {@link PropertiesCollector} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class PropertiesCollectorTest {

    private final Gson gson = GsonUtil.createGson();

    /**
     * Tests if properties for a bulb device are correctly parsed.
     *
     * @throws IOException exception in case device not reachable
     */
    @Test
    public void testBulbProperties() throws IOException {
        assertProperties("bulb_get_sysinfo_response", TPLinkSmartHomeThingType.LB130, 11);
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

    private void assertProperties(@NonNull String responseFile, @NonNull TPLinkSmartHomeThingType thingType,
            int expectedSize) throws IOException {
        ThingTypeUID thingTypeUID = thingType.thingTypeUID();
        Map<String, Object> props = PropertiesCollector.collectProperties(thingTypeUID, "localhost",
                ModelTestUtil.toJson(gson, responseFile, GetSysinfo.class).getSysinfo());

        assertEquals("Number of properties not as expected for properties", expectedSize, props.size());
        props.entrySet().stream().forEach(
                entry -> assertNotNull("Property '" + entry.getKey() + "' should not be null", entry.getValue()));
    }
}
