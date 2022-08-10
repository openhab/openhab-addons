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
package org.openhab.binding.boschindego.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Garden data.
 * 
 * @author Jacob Laursen - Initial contribution
 */
public class Garden {
    public long id;

    public String name;

    @SerializedName("signal_id")
    public byte signalId;

    public int size;

    @SerializedName("inner_bounds")
    public int innerBounds;

    public int cuts;

    public int runtime;

    public int charge;

    public int bumps;

    public int stops;

    @SerializedName("last_mow")
    public int lastMow;

    @SerializedName("map_cell_size")
    public int mapCellSize;
}
