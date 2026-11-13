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
package org.openhab.binding.energycharts.internal.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Response of the Energy-Charts price endpoint.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class EnergyChartsApiData {
    @SerializedName("unix_seconds")
    public List<Long> unixSeconds = List.of();

    @SerializedName("price")
    public List<@Nullable Double> prices = List.of();
}
