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
package org.openhab.binding.avmfritz.internal.dto.json;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class Values {
    @SerializedName("ebene")
    public int ebene;
    @SerializedName("anzahl")
    public int anzahl;
    @SerializedName("values")
    public List<Integer> values;
    @SerializedName("times_type")
    public int timesType;
    @SerializedName("ID")
    public int id;
    @SerializedName("datatimestamp")
    public int datatimestamp;
}
