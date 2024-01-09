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
package org.openhab.binding.mybmw.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.MyBMWBridgeConfiguration;

/**
 * 
 * checks if the configuration is valid
 * 
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - extracted to own class
 */
@NonNullByDefault
public final class MyBMWConfigurationChecker {
    public static boolean checkConfiguration(MyBMWBridgeConfiguration config) {
        if (config.userName.isBlank() || config.password.isBlank()) {
            return false;
        } else {
            return BimmerConstants.EADRAX_SERVER_MAP.containsKey(config.region);
        }
    }
}
