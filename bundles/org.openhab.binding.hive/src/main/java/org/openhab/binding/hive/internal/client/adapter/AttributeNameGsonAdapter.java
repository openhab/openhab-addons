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
package org.openhab.binding.hive.internal.client.adapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hive.internal.client.AttributeName;
import org.openhab.binding.hive.internal.client.HiveApiConstants;

/**
 * A gson {@link com.google.gson.TypeAdapter} for {@link AttributeName}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class AttributeNameGsonAdapter extends ComplexEnumGsonTypeAdapterBase<AttributeName> {
    public AttributeNameGsonAdapter() {
        super(EnumMapper.builder(AttributeName.class)
                .setUnexpectedValue(AttributeName.UNEXPECTED)
                .add(
                        AttributeName.HEATING_THERMOSTAT_TARGET_HEAT_TEMPERATURE,
                        HiveApiConstants.ATTRIBUTE_NAME_HEATING_THERMOSTAT_V1_TARGET_HEAT_TEMPERATURE
                )
                .add(
                        AttributeName.ON_OFF_DEVICE_MODE,
                        HiveApiConstants.ATTRIBUTE_NAME_ON_OFF_DEVICE_V1_MODE
                )
                .add(
                        AttributeName.WATER_HEATER_HEATING_OPERATING_MODE,
                        HiveApiConstants.ATTRIBUTE_NAME_WATER_HEATER_V1_OPERATING_MODE
                )
                .build());
    }
}
