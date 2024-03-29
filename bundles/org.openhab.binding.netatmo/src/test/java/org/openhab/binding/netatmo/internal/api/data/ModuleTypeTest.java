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
package org.openhab.binding.netatmo.internal.api.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.BINDING_ID;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ModuleTypeTest {
    public URI getConfigDescription(ModuleType mt) {
        if (mt == ModuleType.WELCOME || mt == ModuleType.PRESENCE || mt == ModuleType.DOORBELL) {
            // This did not exist prior to PR #16492
            return URI.create(BINDING_ID + ":camera");
        }
        if (mt == ModuleType.WEATHER_STATION || mt == ModuleType.HOME_COACH) {
            // This did not exist prior to PR #16492
            return URI.create(BINDING_ID + ":weather");
        }
        // This was previous method for calculating configuration URI
        return URI.create(BINDING_ID + ":"
                + (mt == ModuleType.ACCOUNT ? "api_bridge"
                        : mt == ModuleType.HOME ? "home"
                                : (mt.isLogical() ? "virtual"
                                        : ModuleType.UNKNOWN == mt.getBridge() ? "configurable" : "device")));
    }

    @Test
    public void checkConfigDescription() {
        ModuleType.AS_SET.stream().forEach(mt -> {
            if (mt != ModuleType.WELCOME) {
                URI confDesc = mt.configDescription;
                assertEquals(getConfigDescription(mt), confDesc);
            }
        });
    }
}
