/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.intellicenter2.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public enum HeatMode {

    @SerializedName("0")
    OFF,
    @SerializedName("1")
    FLAME,
    @SerializedName("2")
    SOLAR,
    @SerializedName("3")
    FLAKE,
    @SerializedName("4")
    ULTRA,
    @SerializedName("5")
    HYBRID,
    @SerializedName("6")
    MASTERTEMP,
    @SerializedName("7")
    MAXETEMP;
}
