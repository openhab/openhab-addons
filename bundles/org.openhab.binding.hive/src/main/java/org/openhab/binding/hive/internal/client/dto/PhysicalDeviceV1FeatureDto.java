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

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class PhysicalDeviceV1FeatureDto extends FeatureDtoBase {
    public @Nullable FeatureAttributeDto<String> nativeIdentifier;
    public @Nullable FeatureAttributeDto<String> hardwareIdentifier;
    public @Nullable FeatureAttributeDto<String> model;
    public @Nullable FeatureAttributeDto<String> manufacturer;
    public @Nullable FeatureAttributeDto<String> powerSupply;
    public @Nullable FeatureAttributeDto<String> softwareVersion;
    public @Nullable FeatureAttributeDto<String> latestSoftwareVersion;
    public @Nullable FeatureAttributeDto<Integer> upgradeProgress;
    public @Nullable FeatureAttributeDto<String> upgradeStatus;
    public @Nullable FeatureAttributeDto<ZonedDateTime> lastSeen;
}
