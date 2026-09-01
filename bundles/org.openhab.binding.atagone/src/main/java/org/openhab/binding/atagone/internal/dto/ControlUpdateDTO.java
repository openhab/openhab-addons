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
 * Gson DTO for the {@code control} block in an {@code update_message}. Only non-null fields are
 * serialized, so set only the fields you want to change. {@code ch_mode} alone activates extend or
 * fireplace mode (falling back to the stored duration), but never holiday mode — that additionally
 * needs {@code configuration.start_vacation} in the same request. Cancelling any timed preset
 * requires zeroing {@code ch_mode_duration} specifically. See
 * {@link org.openhab.binding.atagone.internal.AtagOneHandler}'s {@code composeXxxActivation}/
 * {@code composeCancel} methods for how each write is composed.
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
