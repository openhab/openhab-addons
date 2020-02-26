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
package org.openhab.binding.hive.internal.client.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class FeaturesDto {
    public @Nullable AutoBoostV1FeatureDto autoboost_v1;
    public @Nullable BatteryDeviceV1FeatureDto battery_device_v1;
    public @Nullable DeviceManagementV1FeatureDto device_management_v1;
    public @Nullable HeatingThermostatV1FeatureDto heating_thermostat_v1;
    public @Nullable LinksV1FeatureDto links_v1;
    public @Nullable OnOffDeviceV1FeatureDto on_off_device_v1;
    public @Nullable PhysicalDeviceV1FeatureDto physical_device_v1;
    public @Nullable TemperatureSensorV1FeatureDto temperature_sensor_v1;
    public @Nullable TransientModeV1FeatureDto transient_mode_v1;
    public @Nullable WaterHeaterV1FeatureDto water_heater_v1;
    public @Nullable ZigbeeDeviceV1FeatureDto zigbee_device_v1;
}
