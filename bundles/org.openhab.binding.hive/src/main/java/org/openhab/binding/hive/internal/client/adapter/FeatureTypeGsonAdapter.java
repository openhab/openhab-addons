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
import org.openhab.binding.hive.internal.client.FeatureType;
import org.openhab.binding.hive.internal.client.HiveApiConstants;

/**
 * A gson {@link com.google.gson.TypeAdapter} for {@link FeatureType}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class FeatureTypeGsonAdapter extends ComplexEnumGsonTypeAdapterBase<FeatureType> {
    public FeatureTypeGsonAdapter() {
        super(EnumMapper.builder(FeatureType.class)
                .setUnexpectedValue(FeatureType.UNEXPECTED)
                .add(FeatureType.AUTOBOOST_V1, HiveApiConstants.FEATURE_TYPE_AUTOBOOST_V1)
                .add(FeatureType.BATTERY_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_BATTERY_DEVICE_V1)
                .add(FeatureType.CHILD_LOCK_V1, HiveApiConstants.FEATURE_TYPE_CHILD_LOCK_V1)
                .add(FeatureType.DEVICE_MANAGEMENT_V1, HiveApiConstants.FEATURE_TYPE_DEVICE_MANAGEMENT_V1)
                .add(FeatureType.DISPLAY_ORIENTATION_V1, HiveApiConstants.FEATURE_TYPE_DISPLAY_ORIENTATION_V1)
                .add(FeatureType.ETHERNET_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_ETHERNET_DEVICE_V1)
                .add(FeatureType.FROST_PROTECT_V1, HiveApiConstants.FEATURE_TYPE_FROST_PROTECT_V1)
                .add(FeatureType.GROUP_V1, HiveApiConstants.FEATURE_TYPE_GROUP_V1)
                .add(FeatureType.HEATING_TEMPERATURE_CONTROL_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_HEATING_TEMPERATURE_CONTROL_DEVICE_V1)
                .add(FeatureType.HEATING_TEMPERATURE_CONTROL_V1, HiveApiConstants.FEATURE_TYPE_HEATING_TEMPERATURE_CONTROL_V1)
                .add(FeatureType.HEATING_THERMOSTAT_V1, HiveApiConstants.FEATURE_TYPE_HEATING_THERMOSTAT_V1)
                .add(FeatureType.HIVE_HUB_V1, HiveApiConstants.FEATURE_TYPE_HIVE_HUB_V1)
                .add(FeatureType.LIFECYCLE_STATE_V1, HiveApiConstants.FEATURE_TYPE_LIFECYCLE_STATE_V1)
                .add(FeatureType.LINKS_V1, HiveApiConstants.FEATURE_TYPE_LINKS_V1)
                .add(FeatureType.MOUNTING_MODE_V1, HiveApiConstants.FEATURE_TYPE_MOUNTING_MODE_V1)
                .add(FeatureType.ON_OFF_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_ON_OFF_DEVICE_V1)
                .add(FeatureType.PHYSICAL_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_PHYSICAL_DEVICE_V1)
                .add(FeatureType.PI_HEATING_DEMAND_V1, HiveApiConstants.FEATURE_TYPE_PI_HEATING_DEMAND_V1)
                .add(FeatureType.RADIO_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_RADIO_DEVICE_V1)
                .add(FeatureType.STANDBY_V1, HiveApiConstants.FEATURE_TYPE_STANDBY_V1)
                .add(FeatureType.TEMPERATURE_SENSOR_V1, HiveApiConstants.FEATURE_TYPE_TEMPERATURE_SENSOR_V1)
                .add(FeatureType.TRANSIENT_MODE_V1, HiveApiConstants.FEATURE_TYPE_TRANSIENT_MODE_V1)
                .add(FeatureType.TRV_CALIBRATION_V1, HiveApiConstants.FEATURE_TYPE_TRV_CALIBRATION_V1)
                .add(FeatureType.TRV_ERROR_DIAGNOSTICS_V1, HiveApiConstants.FEATURE_TYPE_TRV_ERROR_DIAGNOSTICS_V1)
                .add(FeatureType.WATER_HEATER_V1, HiveApiConstants.FEATURE_TYPE_WATER_HEATER_V1)
                .add(FeatureType.ZIGBEE_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_ZIGBEE_DEVICE_V1)
                .add(FeatureType.ZIGBEE_ROUTING_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_ZIGBEE_ROUTING_DEVICE_V1)
                .build());
    }
}
