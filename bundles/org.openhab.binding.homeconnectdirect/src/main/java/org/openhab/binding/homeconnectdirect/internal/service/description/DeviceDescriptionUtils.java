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
package org.openhab.binding.homeconnectdirect.internal.service.description;

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.THING_TYPE_WARMING_DRAWER;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.handler.model.Value;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ContentType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DataType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DeviceDescriptionType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ProtocolType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.ContentTypeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.DataTypeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.EnumerationTypeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.feature.model.FeatureMapping;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.ValueData;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for device description processing.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class DeviceDescriptionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceDescriptionUtils.class);

    private DeviceDescriptionUtils() {
        // Utility class
    }

    public static @Nullable List<Value> mapValues(DeviceDescriptionService deviceDescriptionService,
            FeatureMapping featureMapping, Resource resource, @Nullable List<ValueData> valueDataList,
            @Nullable ThingTypeUID thingTypeUID) {
        List<Value> mappedValues = null;
        if (valueDataList != null) {
            var values = valueDataList.stream().map(valueData -> {
                var valueDescription = deviceDescriptionService.getDeviceDescriptionObject(valueData.uid());

                // key
                var key = featureMapping.mapFeatureIdToKey(valueData.uid());

                // type
                var type = valueDescription != null ? valueDescription.type() : DeviceDescriptionType.UNKNOWN;

                // contentType
                ContentType contentType = null;
                if (valueDescription != null
                        && valueDescription.object() instanceof ContentTypeProvider contentTypeProvider) {
                    contentType = contentTypeProvider.contentType();
                }

                // dataType
                DataType dataType = null;
                if (valueDescription != null
                        && valueDescription.object() instanceof DataTypeProvider dataTypeProvider) {
                    dataType = dataTypeProvider.dataType();
                }

                // value
                Object value = valueData.value();
                Integer enumerationTypeId = null;
                if (DeviceDescriptionType.SELECTED_PROGRAM.equals(type)
                        || DeviceDescriptionType.ACTIVE_PROGRAM.equals(type)) {
                    var programUid = mapObjectToInteger(valueData.value());
                    if (programUid != null) {
                        var program = deviceDescriptionService.findProgram(programUid);
                        if (program != null) {
                            value = program.key();
                        }
                    }
                } else if (ContentType.ENUMERATION.equals(contentType)) {
                    if (valueDescription != null
                            && valueDescription.object() instanceof EnumerationTypeProvider enumerationTypeProvider) {
                        var enumTypeId = enumerationTypeProvider.enumerationType();
                        enumerationTypeId = enumTypeId;
                        var enumValue = mapObjectToInteger(valueData.value());
                        if (enumTypeId != null && enumValue != null) {
                            var enumeration = deviceDescriptionService.findEnumeration(enumTypeId, enumValue);
                            if (enumeration != null) {
                                value = enumeration.valueKey();
                            }
                        }
                    }
                }

                // Fixed-point scaling: when protocolType is FLOAT but wire encoding is a raw integer type,
                // the value is a fixed-point integer that needs to be divided by 10.
                // Only applied to warming drawers as they encode temperature values as fixed-point integers.
                if (THING_TYPE_WARMING_DRAWER.equals(thingTypeUID) && contentType != null
                        && ProtocolType.FLOAT.equals(contentType.protocolType) && dataType != null
                        && dataType.isRawInteger() && value instanceof Number number) {
                    LOGGER.debug(
                            "Fixed-point scaling applied: key={}, contentType={}, dataType={}, rawValue={}, scaledValue={}",
                            key, contentType, dataType, number, number.doubleValue() / 10.0);
                    value = number.doubleValue() / 10.0;
                }

                return new Value(valueData.uid(), key, value, valueData.value(), type, contentType, dataType,
                        enumerationTypeId);
            }).toList();

            if (!values.isEmpty()) {
                mappedValues = values;
            }
        }
        return mappedValues;
    }

    public static @Nullable Integer mapObjectToInteger(Object object) {
        return switch (object) {
            case Integer intValue -> intValue;
            case Long longValue -> longValue.intValue();
            case Float floatValue when floatValue % 1 == 0 -> floatValue.intValue();
            case Double doubleValue when doubleValue % 1 == 0 -> doubleValue.intValue();
            default -> null;
        };
    }
}
