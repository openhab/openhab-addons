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
package org.openhab.binding.eyeonwater.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * GSON DTO representing meter register.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
class RegisterDTO {
    @SerializedName("latest_read")
    @Nullable
    LatestReadDTO latestRead;
    @Nullable
    FlagsDTO flags;
    @Nullable
    LeakDTO leak;
}
