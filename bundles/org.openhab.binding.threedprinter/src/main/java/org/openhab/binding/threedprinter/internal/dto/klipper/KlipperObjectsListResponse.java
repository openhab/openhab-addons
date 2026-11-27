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
package org.openhab.binding.threedprinter.internal.dto.klipper;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for the Moonraker GET /printer/objects/list response, used to discover which optional printer objects
 * (e.g. additional extruders on a multi-toolhead machine) are present before polling for their status.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class KlipperObjectsListResponse {

    @SerializedName("result")
    public @Nullable KlipperObjectsListResult result;

    public static class KlipperObjectsListResult {
        @SerializedName("objects")
        public @Nullable List<String> objects;
    }
}
