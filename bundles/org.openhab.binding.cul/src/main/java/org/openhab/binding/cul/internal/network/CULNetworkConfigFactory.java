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

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.cul.internal.CULConfig;
import org.openhab.binding.cul.internal.CULConfigFactory;
import org.openhab.binding.cul.internal.CULMode;

/**
 * Configuration factory for network device handler implementation.
 *
 * @author Patrick Ruckstuhl - Initial contribution
 * @since 1.9.0
 */
@NonNullByDefault
public class CULNetworkConfigFactory implements CULConfigFactory {

    public static final String DEVICE_TYPE = "network";

    public CULConfig create(String deviceType, String deviceAddress, CULMode mode) {
        Hashtable<String, String> config = new Hashtable<>();
        return create(deviceType, deviceAddress, mode, config);
    }

    @Override
    public CULConfig create(String deviceType, String deviceAddress, CULMode mode, Dictionary<String, ?> config) {
        return new CULNetworkConfig(deviceType, deviceAddress, mode);
    }
}
