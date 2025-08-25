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
package org.openhab.binding.myenergi.internal.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link ZappiHourlyHistory} is a DTO class used to represent list of hourly historic data. It's used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
public class ZappiHourlyHistory {

    public String id;

    public List<ZappiHourlyHistoryEntry> entries = new ArrayList<>();

    @Override
    public String toString() {
        return "ZappiHourlyHistory [id=" + id + ", entries=" + entries + "]";
    }
}
