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
package org.openhab.binding.atagone.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Gson DTO for the {@code control} block in an {@code update_message}.
 * <p>
 * Only non-null fields are serialized by Gson, so set only the fields you want to change.
 * {@code ch_mode} alone is sufficient to activate extend or fireplace mode — the device falls back
 * to whatever duration is already stored ({@code extend_duration}/{@code fireplace_duration}).
 * Holiday mode is the exception: {@code ch_mode} alone never activates it, regardless of
 * {@code vacation_duration} — {@code configuration.start_vacation} must be sent in the same
 * request. To cancel any active timed preset, {@code ch_mode_duration} must be explicitly zeroed;
 * the mode-specific duration fields do not need to be touched. See
 * {@link org.openhab.binding.atagone.internal.AtagOneHandler} for how each of these writes is
 * composed ({@code composeExtendActivation}, {@code composeFireplaceActivation},
 * {@code composeVacationActivation}, {@code composeCancel}).
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault({})
public class ControlUpdateDTO {
    public @Nullable Integer ch_control_mode;
    public @Nullable Integer ch_mode;
    public @Nullable Long ch_mode_duration;
    public @Nullable Double ch_mode_temp;
    public @Nullable Long extend_duration;
    public @Nullable Long fireplace_duration;
    public @Nullable Long vacation_duration;
}
