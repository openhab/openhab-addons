/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceType;
import org.openhab.core.thing.Thing;

/**
 * Helper class extracting thing properties from {@link DeviceState}s received from the Miele cloud.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public final class ThingPropertyExtractor {
    private ThingPropertyExtractor() {
        throw new IllegalStateException(getClass().getName() + " cannot be instantiated");
    }

    public static Map<String, String> extractProperties(DeviceState deviceState) {
        var propertyMap = new HashMap<String, String>();
        propertyMap.put(Thing.PROPERTY_SERIAL_NUMBER, getSerialNumber(deviceState));
        propertyMap.put(Thing.PROPERTY_MODEL_ID, getModelId(deviceState));

        if (deviceState.getRawType() == DeviceType.HOB_INDUCTION
                || deviceState.getRawType() == DeviceType.HOB_HIGHLIGHT) {
            deviceState.getPlateStepCount().ifPresent(plateCount -> propertyMap
                    .put(MieleCloudBindingConstants.PROPERTY_PLATE_COUNT, plateCount.toString()));
        }

        return propertyMap;
    }

    private static String getSerialNumber(DeviceState deviceState) {
        return deviceState.getFabNumber().orElse(deviceState.getDeviceIdentifier());
    }

    private static String getModelId(DeviceState deviceState) {
        return getDeviceAndTechType(deviceState).orElse("Unknown");
    }

    private static Optional<String> getDeviceAndTechType(DeviceState deviceState) {
        Optional<String> deviceType = deviceState.getType();
        Optional<String> techType = deviceState.getTechType();
        if (deviceType.isPresent() && techType.isPresent()) {
            return Optional.of(deviceType.get() + " " + techType.get());
        }
        if (!deviceType.isPresent() && techType.isPresent()) {
            return techType;
        }
        if (deviceType.isPresent() && !techType.isPresent()) {
            return deviceType;
        }
        return Optional.empty();
    }
}
