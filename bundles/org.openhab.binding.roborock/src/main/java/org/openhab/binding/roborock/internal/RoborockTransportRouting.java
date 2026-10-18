/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.roborock.internal;

import static org.openhab.binding.roborock.internal.RoborockBindingConstants.COMMAND_GET_MAP;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Transport routing helpers for command handling.
 *
 * @author Maciej Pham - Initial contribution
 */
@NonNullByDefault
public final class RoborockTransportRouting {

    private RoborockTransportRouting() {
    }

    public static boolean isCloudOnlyMethod(String method) {
        return COMMAND_GET_MAP.equals(method);
    }

    public static RoborockCommunicationMode selectTransportMode(RoborockCommunicationMode configuredMode,
            String method) {
        if (isCloudOnlyMethod(method)) {
            return RoborockCommunicationMode.CLOUD;
        }
        return configuredMode;
    }
}
