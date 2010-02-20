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
package org.openhab.binding.astro.internal.action;

import java.time.ZonedDateTime;

import javax.measure.quantity.Angle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;

/**
 * The {@link IAstroActions} defines the interface for all thing actions supported by the binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public interface IAstroActions {
    public @Nullable ZonedDateTime getEventTime(String phaseName, @Nullable ZonedDateTime date,
            @Nullable String moment);

    public @Nullable QuantityType<Angle> getAzimuth(@Nullable ZonedDateTime date);

    public @Nullable QuantityType<Angle> getElevation(@Nullable ZonedDateTime date);
}
