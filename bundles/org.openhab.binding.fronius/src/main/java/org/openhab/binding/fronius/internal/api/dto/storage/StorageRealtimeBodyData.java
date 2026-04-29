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

import com.google.gson.annotations.SerializedName;

/****
 * The {@link StorageRealtimeBodyData} is responsible for storing
 * the "Data" node of the {@link StorageRealtimeBody}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
public class StorageRealtimeBodyData {
    @SerializedName("Controller")
    private StorageController controller;

    public StorageController getController() {
        return controller;
    }
}
