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
package org.openhab.binding.mielecloud.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Helper class extracting information related to things from {@link DeviceState}s received from the Miele cloud.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public final class ThingInformationExtractor {
    private ThingInformationExtractor() {
        throw new IllegalStateException(getClass().getName() + " cannot be instantiated");
    }

    /**
     * Extracts thing properties from a {@link DeviceState}.
     *
     * The returned properties always contain {@link Thing#PROPERTY_SERIAL_NUMBER} and {@link Thing#PROPERTY_MODEL_ID}.
     * More might be present depending on the type of device.
     *
     * @param thingTypeUid {@link ThingTypeUID} of the thing to extract properties for.
     * @param deviceState {@link DeviceState} received from the Miele cloud.
     * @return A {@link Map} holding the properties as key-value pairs.
     */
    public static Map<String, String> extractProperties(ThingTypeUID thingTypeUid, DeviceState deviceState) {
        var propertyMap = new HashMap<String, String>();
        propertyMap.put(Thing.PROPERTY_SERIAL_NUMBER, getSerialNumber(deviceState));
        propertyMap.put(Thing.PROPERTY_MODEL_ID, getModelId(deviceState));
        propertyMap.put(MieleCloudBindingConstants.CONFIG_PARAM_DEVICE_IDENTIFIER, deviceState.getDeviceIdentifier());

        if (MieleCloudBindingConstants.THING_TYPE_HOB.equals(thingTypeUid)) {
            deviceState.getPlateStepCount().ifPresent(plateCount -> propertyMap
                    .put(MieleCloudBindingConstants.PROPERTY_PLATE_COUNT, plateCount.toString()));
        }

        return propertyMap;
    }

    private static String getSerialNumber(DeviceState deviceState) {
        return Objects.requireNonNull(deviceState.getFabNumber().orElse(deviceState.getDeviceIdentifier()));
    }

    private static String getModelId(DeviceState deviceState) {
        return Objects.requireNonNull(getDeviceAndTechType(deviceState).orElse("Unknown"));
    }

    /**
     * Formats device type and tech type from the given {@link DeviceState} for the purpose of displaying then to the
     * user.
     *
     * If either of device or tech type is missing then it will be omitted. If both are missing then an empty
     * {@link Optional} will be returned.
     *
     * @param deviceState {@link DeviceState} obtained from the Miele cloud.
     * @return An {@link Optional} holding the formatted value or an empty {@link Optional} if neither device type nor
     *         tech type were present.
     */
    static Optional<String> getDeviceAndTechType(DeviceState deviceState) {
        Optional<String> deviceType = deviceState.getType();
        Optional<String> techType = deviceState.getTechType();
        if (deviceType.isPresent() && techType.isPresent()) {
            return Optional.of(deviceType.get() + " " + techType.get());
        }
        if (deviceType.isEmpty() && techType.isPresent()) {
            return techType;
        }
        if (deviceType.isPresent() && techType.isEmpty()) {
            return deviceType;
        }
        return Optional.empty();
    }
}
