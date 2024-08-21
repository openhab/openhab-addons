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
package org.openhab.binding.mybmw.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.utils.RemoteServiceUtils;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.CommandOption;

/**
 * The {@link VehiclePropertiesTest} tests stored fingerprint responses from BMW API
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - move test to myBMWProxyTest
 */
@NonNullByDefault
public class VehiclePropertiesTest {

    @Test
    public void testChannelUID() {
        ThingTypeUID thingTypePHEV = new ThingTypeUID("mybmw", "plugin-hybrid-vehicle");
        assertEquals("plugin-hybrid-vehicle", thingTypePHEV.getId(), "Vehicle Type");
    }

    @Test
    public void testRemoteServiceOptions() {
        String commandReference = "[CommandOption [command=light-flash, label=Flash Lights], CommandOption [command=vehicle-finder, label=Vehicle Finder], CommandOption [command=door-lock, label=Door Lock], CommandOption [command=door-unlock, label=Door Unlock], CommandOption [command=horn-blow, label=Horn Blow], CommandOption [command=climate-now-start, label=Start Climate], CommandOption [command=climate-now-stop, label=Stop Climate], CommandOption [command=start-charging, label=Start Charging], CommandOption [command=stop-charging, label=Stop Charging]]";
        List<CommandOption> l = RemoteServiceUtils.getOptions(true);
        assertEquals(commandReference, l.toString(), "Commad Options");
    }
}
