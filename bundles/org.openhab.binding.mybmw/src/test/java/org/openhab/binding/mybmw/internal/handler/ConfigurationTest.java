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
package org.openhab.binding.mybmw.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.MyBMWConfiguration;
import org.openhab.binding.mybmw.internal.utils.BimmerConstants;

/**
 * The {@link ConfigurationTest} test different configurations
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConfigurationTest {

    @Test
    public void testAuthServerMap() {
        MyBMWConfiguration cdc = new MyBMWConfiguration();
        assertFalse(MyBMWBridgeHandler.checkConfiguration(cdc));
        cdc.userName = "a";
        assertFalse(MyBMWBridgeHandler.checkConfiguration(cdc));
        cdc.password = "b";
        assertFalse(MyBMWBridgeHandler.checkConfiguration(cdc));
        cdc.region = "c";
        assertFalse(MyBMWBridgeHandler.checkConfiguration(cdc));
        cdc.region = BimmerConstants.REGION_NORTH_AMERICA;
        assertTrue(MyBMWBridgeHandler.checkConfiguration(cdc));
        cdc.region = BimmerConstants.REGION_ROW;
        assertTrue(MyBMWBridgeHandler.checkConfiguration(cdc));
        cdc.region = BimmerConstants.REGION_CHINA;
        assertTrue(MyBMWBridgeHandler.checkConfiguration(cdc));
    }
}
