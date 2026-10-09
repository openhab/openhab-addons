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
package org.openhab.binding.philipsair.internal;

import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;

/**
 * The knowledge about models that the status of a device does not tell.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
final class PhilipsAirModels {
    /**
     * Model id prefixes of the devices that can show the gas (TVOC) index on the display, as offered by the Philips
     * app.
     */
    private static final List<String> GAS_INDEX_MODELS = List.of("AC45", "AC6675", "AC56", "MS3", "MS4");
    private static final List<String> TEXT_THRESHOLD_MODELS = List.of("AC4373", "AC4375");

    private PhilipsAirModels() {
    }

    static boolean supportsGasIndex(@Nullable PhilipsAirPurifierDeviceDTO deviceInfo,
            @Nullable PhilipsAirPurifierDataDTO data) {
        String model = getModel(deviceInfo);
        if (model != null && GAS_INDEX_MODELS.stream().anyMatch(model::startsWith)) {
            return true;
        }
        // models unknown to the Philips app are recognized by their gas sensor
        return data != null && data.getTvoc() != null;
    }

    static boolean hasTextThresholds(@Nullable PhilipsAirPurifierDeviceDTO deviceInfo) {
        String model = getModel(deviceInfo);
        return model != null && TEXT_THRESHOLD_MODELS.stream().anyMatch(model::startsWith);
    }

    /**
     * @return the upper case model id, or the device type if the device reports no model id
     */
    private static @Nullable String getModel(@Nullable PhilipsAirPurifierDeviceDTO deviceInfo) {
        if (deviceInfo == null) {
            return null;
        }
        String model = deviceInfo.getModelId();
        if (model == null || model.isBlank()) {
            model = deviceInfo.getType();
        }
        return model != null ? model.toUpperCase(Locale.ROOT) : null;
    }
}
