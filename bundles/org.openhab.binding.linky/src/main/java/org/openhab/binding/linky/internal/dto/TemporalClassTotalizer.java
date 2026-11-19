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
 * The {@link TemporalClassTotalizer} holds informations about the available energy calendar
 *
 * @author Laurent Arnal - Initial contribution
 */

public class TemporalClassTotalizer {
    @SerializedName("id_quadrant")
    public String idQuadrant;

    @SerializedName("values")
    public Value[] values;
}
