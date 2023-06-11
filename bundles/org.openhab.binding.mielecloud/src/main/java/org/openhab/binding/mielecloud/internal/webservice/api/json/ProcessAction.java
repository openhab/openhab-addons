/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a process action.
 *
 * @author Roland Edelhoff - Initial contribution
 */
@NonNullByDefault
public enum ProcessAction {
    /**
     * {@StateType} for unknown states.
     */
    UNKNOWN,

    @SerializedName("1")
    START,

    @SerializedName("2")
    STOP,

    @SerializedName("3")
    PAUSE,

    @SerializedName("4")
    START_SUPERFREEZING,

    @SerializedName("5")
    STOP_SUPERFREEZING,

    @SerializedName("6")
    START_SUPERCOOLING,

    @SerializedName("7")
    STOP_SUPERCOOLING,
}
