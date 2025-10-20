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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;

/**
 * The {@link SmartHomeTO} encapsulate the GSON data of a smarthome graphql query
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class SmartHomeTO {
    public @Nullable Data data;

    public static class Data {
        public @Nullable Endpoints endpoints;
    }

    public static class Endpoints {
        public @Nullable List<EndPointItemTO> items;
    }

    public List<JsonSmartHomeDevice> getLegacySmartHomeDevices() {
        if (data instanceof Data data && data.endpoints != null && data.endpoints instanceof Endpoints endpoints
                && endpoints.items instanceof List<EndPointItemTO> items) {

            return items.stream().map(item -> item.legacyAppliance).filter(java.util.Objects::nonNull)
                    .map(device -> (@NonNull JsonSmartHomeDevice) device).toList();
        }
        return List.of();
    }
}
