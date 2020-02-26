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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.OverrideMode;
import org.openhab.binding.hive.internal.client.WaterHeaterOperatingMode;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class WaterHeaterV1FeatureDto extends FeatureDtoBase {
    public @Nullable FeatureAttributeDto<WaterHeaterOperatingMode> operatingMode;
    public @Nullable FeatureAttributeDto<Boolean> isOn;
    public @Nullable FeatureAttributeDto<OverrideMode> temporaryOperatingModeOverride;
    public @Nullable FeatureAttributeDto<ScheduleDto> schedule;
    public @Nullable FeatureAttributeDto<List<ActionDto>> previousConfiguration;
}
