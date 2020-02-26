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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hive.internal.client.FeatureType;
import org.openhab.binding.hive.internal.client.HiveApiConstants;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class FeatureTypeGsonAdapterTest extends ComplexEnumGsonAdapterTest<FeatureType, FeatureTypeGsonAdapter> {
    @Override
    protected FeatureTypeGsonAdapter getAdapter() {
        return new FeatureTypeGsonAdapter();
    }

    @Override
    protected List<List<Object>> getGoodParams() {
        return Arrays.asList(
                Arrays.asList(FeatureType.AUTOBOOST_V1, HiveApiConstants.FEATURE_TYPE_AUTOBOOST_V1),
                Arrays.asList(FeatureType.BATTERY_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_BATTERY_DEVICE_V1),
                Arrays.asList(FeatureType.CHILD_LOCK_V1, HiveApiConstants.FEATURE_TYPE_CHILD_LOCK_V1),
                Arrays.asList(FeatureType.DEVICE_MANAGEMENT_V1, HiveApiConstants.FEATURE_TYPE_DEVICE_MANAGEMENT_V1),
                Arrays.asList(FeatureType.DISPLAY_ORIENTATION_V1, HiveApiConstants.FEATURE_TYPE_DISPLAY_ORIENTATION_V1),
                Arrays.asList(FeatureType.ETHERNET_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_ETHERNET_DEVICE_V1),
                Arrays.asList(FeatureType.FROST_PROTECT_V1, HiveApiConstants.FEATURE_TYPE_FROST_PROTECT_V1),
                Arrays.asList(FeatureType.GROUP_V1, HiveApiConstants.FEATURE_TYPE_GROUP_V1),
                Arrays.asList(FeatureType.HEATING_TEMPERATURE_CONTROL_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_HEATING_TEMPERATURE_CONTROL_DEVICE_V1),
                Arrays.asList(FeatureType.HEATING_TEMPERATURE_CONTROL_V1, HiveApiConstants.FEATURE_TYPE_HEATING_TEMPERATURE_CONTROL_V1),
                Arrays.asList(FeatureType.HEATING_THERMOSTAT_V1, HiveApiConstants.FEATURE_TYPE_HEATING_THERMOSTAT_V1),
                Arrays.asList(FeatureType.HIVE_HUB_V1, HiveApiConstants.FEATURE_TYPE_HIVE_HUB_V1),
                Arrays.asList(FeatureType.LIFECYCLE_STATE_V1, HiveApiConstants.FEATURE_TYPE_LIFECYCLE_STATE_V1),
                Arrays.asList(FeatureType.LINKS_V1, HiveApiConstants.FEATURE_TYPE_LINKS_V1),
                Arrays.asList(FeatureType.MOUNTING_MODE_V1, HiveApiConstants.FEATURE_TYPE_MOUNTING_MODE_V1),
                Arrays.asList(FeatureType.ON_OFF_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_ON_OFF_DEVICE_V1),
                Arrays.asList(FeatureType.PI_HEATING_DEMAND_V1, HiveApiConstants.FEATURE_TYPE_PI_HEATING_DEMAND_V1),
                Arrays.asList(FeatureType.PHYSICAL_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_PHYSICAL_DEVICE_V1),
                Arrays.asList(FeatureType.RADIO_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_RADIO_DEVICE_V1),
                Arrays.asList(FeatureType.STANDBY_V1, HiveApiConstants.FEATURE_TYPE_STANDBY_V1),
                Arrays.asList(FeatureType.TEMPERATURE_SENSOR_V1, HiveApiConstants.FEATURE_TYPE_TEMPERATURE_SENSOR_V1),
                Arrays.asList(FeatureType.TRANSIENT_MODE_V1, HiveApiConstants.FEATURE_TYPE_TRANSIENT_MODE_V1),
                Arrays.asList(FeatureType.TRV_CALIBRATION_V1, HiveApiConstants.FEATURE_TYPE_TRV_CALIBRATION_V1),
                Arrays.asList(FeatureType.TRV_ERROR_DIAGNOSTICS_V1, HiveApiConstants.FEATURE_TYPE_TRV_ERROR_DIAGNOSTICS_V1),
                Arrays.asList(FeatureType.WATER_HEATER_V1, HiveApiConstants.FEATURE_TYPE_WATER_HEATER_V1),
                Arrays.asList(FeatureType.ZIGBEE_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_ZIGBEE_DEVICE_V1),
                Arrays.asList(FeatureType.ZIGBEE_ROUTING_DEVICE_V1, HiveApiConstants.FEATURE_TYPE_ZIGBEE_ROUTING_DEVICE_V1)
        );
    }

    @Override
    protected FeatureType getUnexpectedEnum() {
        return FeatureType.UNEXPECTED;
    }

    @Override
    protected String getUnexpectedString() {
        return "http://alertme.com/schema/json/feature/node.feature.something_unexpected.v1.json#";
    }
}
