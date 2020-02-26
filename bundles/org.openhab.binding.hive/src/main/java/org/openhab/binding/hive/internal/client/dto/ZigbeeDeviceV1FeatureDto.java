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
import org.openhab.binding.hive.internal.client.Eui64;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class ZigbeeDeviceV1FeatureDto extends FeatureDtoBase {
    public @Nullable FeatureAttributeDto<Eui64> eui64;
    public @Nullable FeatureAttributeDto<Integer> averageLQI;
    public @Nullable FeatureAttributeDto<Integer> lastKnownLQI;
    public @Nullable FeatureAttributeDto<Integer> averageRSSI;
    public @Nullable FeatureAttributeDto<Integer> lastKnownRSSI;
}
