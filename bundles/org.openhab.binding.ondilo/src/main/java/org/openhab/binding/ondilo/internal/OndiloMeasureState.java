/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ondilo.internal;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link OndiloMeasureState} store last value and valueTime for trend calculation.
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
public class OndiloMeasureState {
    public double value;
    public @Nullable Instant time;

    public OndiloMeasureState(double value, @Nullable Instant time) {
        this.value = value;
        this.time = time;
    }
}
