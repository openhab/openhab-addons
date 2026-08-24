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
 * Mode and its duration parameters must always be sent together in a single message — sending
 * {@code ch_mode} alone leaves the device at its previous (often hardcoded) duration.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault({})
public class ControlUpdateDTO {
    public @Nullable Integer ch_control_mode;
    public @Nullable Integer ch_mode;
    public @Nullable Long ch_mode_duration;
    public @Nullable Double ch_mode_temp;
    public @Nullable Double dhw_temp_setp;
    public @Nullable Integer dhw_mode;
    public @Nullable Long extend_duration;
    public @Nullable Long fireplace_duration;
    public @Nullable Long vacation_duration;
}
