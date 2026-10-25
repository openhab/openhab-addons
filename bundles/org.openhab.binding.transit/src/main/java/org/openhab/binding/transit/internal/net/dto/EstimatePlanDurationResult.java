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
package org.openhab.binding.transit.internal.net.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link EstimatePlanDurationResult} is the response of the {@code estimate_plan_duration} endpoint.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class EstimatePlanDurationResult {
    // estimated duration of the trip in seconds
    private @Nullable Double duration;
    // type of trip plan (Transit, Walk, Bike, Car_Microtransit)
    private @Nullable String type;

    public @Nullable Double getDuration() {
        return duration;
    }

    public @Nullable String getType() {
        return type;
    }
}
