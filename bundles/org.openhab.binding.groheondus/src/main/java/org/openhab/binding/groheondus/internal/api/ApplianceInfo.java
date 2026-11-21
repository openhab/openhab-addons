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
package org.openhab.binding.groheondus.internal.api;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Appliance metadata.
 *
 * @param measurementTransmissionInterval interval in seconds at which a Sense Guard uploads measurements, if reported
 * @param lastTransmission time the device last transmitted data to the cloud, if reported
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public record ApplianceInfo(ApplianceRef ref, String name, int type, @Nullable Integer measurementTransmissionInterval,
        @Nullable Instant lastTransmission) {
    public static final int TYPE_SENSE = 101;
    public static final int TYPE_SENSE_GUARD = 103;
}
