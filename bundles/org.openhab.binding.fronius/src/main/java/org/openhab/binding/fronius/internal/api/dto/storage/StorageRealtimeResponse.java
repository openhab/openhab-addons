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
package org.openhab.binding.fronius.internal.api.dto.storage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fronius.internal.api.dto.BaseFroniusResponse;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link StorageRealtimeResponse} stores the response from the
 * GetStorageRealtimeData request.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class StorageRealtimeResponse extends BaseFroniusResponse {
    @SerializedName("Body")
    private @Nullable StorageRealtimeBody body;

    public @Nullable StorageRealtimeBody getBody() {
        return body;
    }
}
