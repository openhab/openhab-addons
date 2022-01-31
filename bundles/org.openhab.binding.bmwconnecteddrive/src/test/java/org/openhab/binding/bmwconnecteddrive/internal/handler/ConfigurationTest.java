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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.utils.BimmerConstants;

/**
 * The {@link ConfigurationTest} test different configurations
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConfigurationTest {

    @Test
    public void testAuthServerMap() {
        ConnectedDriveConfiguration cdc = new ConnectedDriveConfiguration();
        assertFalse(ConnectedDriveBridgeHandler.checkConfiguration(cdc));
        cdc.userName = "a";
        assertFalse(ConnectedDriveBridgeHandler.checkConfiguration(cdc));
        cdc.password = "b";
        assertFalse(ConnectedDriveBridgeHandler.checkConfiguration(cdc));
        cdc.region = "c";
        assertFalse(ConnectedDriveBridgeHandler.checkConfiguration(cdc));
        cdc.region = BimmerConstants.REGION_NORTH_AMERICA;
        assertTrue(ConnectedDriveBridgeHandler.checkConfiguration(cdc));
        cdc.region = BimmerConstants.REGION_ROW;
        assertTrue(ConnectedDriveBridgeHandler.checkConfiguration(cdc));
        cdc.region = BimmerConstants.REGION_CHINA;
        assertTrue(ConnectedDriveBridgeHandler.checkConfiguration(cdc));
    }
}
