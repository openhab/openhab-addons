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

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.HeatingThermostatOperatingMode;
import org.openhab.binding.hive.internal.client.HeatingThermostatOperatingState;
import org.openhab.binding.hive.internal.client.OverrideMode;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class HeatingThermostatV1FeatureDto extends FeatureDtoBase {
    public @Nullable FeatureAttributeDto<HeatingThermostatOperatingMode> operatingMode;
    public @Nullable FeatureAttributeDto<HeatingThermostatOperatingState> operatingState;
    public @Nullable FeatureAttributeDto<OverrideMode> temporaryOperatingModeOverride;
    public @Nullable FeatureAttributeDto<BigDecimal> targetHeatTemperature;
    public @Nullable FeatureAttributeDto<BigDecimal> maxHeatTargetTemperature;
    public @Nullable FeatureAttributeDto<BigDecimal> minHeatTargetTemperature;
    public @Nullable FeatureAttributeDto<ScheduleDto> heatSchedule;
    public @Nullable FeatureAttributeDto<String> operatingStateReason;
    public @Nullable FeatureAttributeDto<List<ActionDto>> previousConfiguration;
}
