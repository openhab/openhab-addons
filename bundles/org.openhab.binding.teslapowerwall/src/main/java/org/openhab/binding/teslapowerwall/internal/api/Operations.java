/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.teslapowerwall.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class for holding the set of parameters used to read the battery mode/reserver.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class Operations {
    @SerializedName("real_mode")
    public String realMode = "";

    @SerializedName("reactive_mode")
    public String reactiveMode = "";

    @SerializedName("backup_reserve_percent")
    public float backupReservePercent;

    private Operations() {
    }
}
