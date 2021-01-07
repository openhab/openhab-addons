/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.internal.network;

import org.openhab.binding.cul.internal.CULConfig;
import org.openhab.binding.cul.internal.CULMode;

/**
 * Configuration for network device handler implementation.
 *
 * @author Patrick Ruckstuhl - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.9.0
 */
public class CULNetworkConfig extends CULConfig {

    public CULNetworkConfig(String deviceType, String deviceAddress, CULMode mode) {
        super(deviceType, deviceAddress, mode);
    }
}
