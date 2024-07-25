/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.fronius.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for a response-object from the Fronius Solar API (v1).
 *
 * @author Thomas Rokohl - Initial contribution
 */
@NonNullByDefault
public class BaseFroniusResponse {
    @SerializedName("Head")
    private @Nullable Head head;

    public @Nullable Head getHead() {
        return head;
    }
}
