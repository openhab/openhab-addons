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
package org.openhab.binding.mybmw.internal.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.MyBMWBridgeConfiguration;

/**
 * 
 * checks if the configuration checker works fine
 * 
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - renamed
 */
@NonNullByDefault
public class MyBMWConfigurationCheckerTest {
    @Test
    void testCheckConfiguration() {
        MyBMWBridgeConfiguration myBMWBridgeConfiguration = new MyBMWBridgeConfiguration();
        assertFalse(MyBMWConfigurationChecker.checkInitialConfiguration(myBMWBridgeConfiguration));
        myBMWBridgeConfiguration.setUserName("a");
        assertFalse(MyBMWConfigurationChecker.checkInitialConfiguration(myBMWBridgeConfiguration));
        myBMWBridgeConfiguration.setPassword("b");
        assertFalse(MyBMWConfigurationChecker.checkInitialConfiguration(myBMWBridgeConfiguration));
        myBMWBridgeConfiguration.setHcaptchatoken("d");
        assertFalse(MyBMWConfigurationChecker.checkInitialConfiguration(myBMWBridgeConfiguration));
        myBMWBridgeConfiguration.setRegion("c");
        assertFalse(MyBMWConfigurationChecker.checkInitialConfiguration(myBMWBridgeConfiguration));
        myBMWBridgeConfiguration.setRegion(BimmerConstants.REGION_NORTH_AMERICA);
        assertTrue(MyBMWConfigurationChecker.checkInitialConfiguration(myBMWBridgeConfiguration));
        myBMWBridgeConfiguration.setRegion(BimmerConstants.REGION_ROW);
        assertTrue(MyBMWConfigurationChecker.checkInitialConfiguration(myBMWBridgeConfiguration));
        myBMWBridgeConfiguration.setRegion(BimmerConstants.REGION_CHINA);
        assertTrue(MyBMWConfigurationChecker.checkInitialConfiguration(myBMWBridgeConfiguration));
    }
}
