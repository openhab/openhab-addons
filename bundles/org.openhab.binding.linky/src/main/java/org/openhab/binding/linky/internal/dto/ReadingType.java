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
package org.openhab.binding.linky.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ReadingType} the type info associate with a meter reading
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

public class ReadingType {
    @SerializedName("measurement_kind")
    public String measurementKind;

    @SerializedName("measuring_period")
    public String measuringPeriod;

    @SerializedName("unit")
    public String unit;

    @SerializedName("aggregate")
    public String aggregate;
}
