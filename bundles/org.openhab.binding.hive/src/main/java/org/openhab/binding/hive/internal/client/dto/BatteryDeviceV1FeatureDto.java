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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class BatteryDeviceV1FeatureDto extends FeatureDtoBase {
    public @Nullable FeatureAttributeDto<Integer> batteryLevel;
    public @Nullable FeatureAttributeDto<String> batteryState;
    public @Nullable FeatureAttributeDto<BigDecimal> batteryVoltage;
    public @Nullable FeatureAttributeDto<String> notificationState;
}
