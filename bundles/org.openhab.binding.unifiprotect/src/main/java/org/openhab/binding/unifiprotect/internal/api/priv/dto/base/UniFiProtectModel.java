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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.base;

import org.openhab.binding.unifiprotect.internal.api.priv.dto.types.ModelType;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for UniFi Protect objects with an ID
 *
 * @author Dan Cunningham - Initial contribution
 */
public abstract class UniFiProtectModel {

    public String id;

    @SerializedName("modelKey")
    public ModelType model;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id='" + id + "'}";
    }
}
