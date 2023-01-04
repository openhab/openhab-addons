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
package org.openhab.binding.bluetooth;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingUID;

/**
 * Contains general utilities used for bluetooth tests
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class TestUtils {

    public static BluetoothAddress randomAddress() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append(RandomStringUtils.random(2, "0123456789ABCDEF"));
            builder.append(":");
        }
        builder.append(RandomStringUtils.random(2, "0123456789ABCDEF"));
        return new BluetoothAddress(builder.toString());
    }

    public static ThingUID randomThingUID() {
        return new ThingUID(BluetoothBindingConstants.BINDING_ID, RandomStringUtils.randomAlphabetic(6));
    }
}
