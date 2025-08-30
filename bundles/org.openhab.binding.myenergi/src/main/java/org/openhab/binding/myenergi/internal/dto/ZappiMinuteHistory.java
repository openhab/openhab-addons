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
 * The {@link ZappiMinuteHistory} is a DTO class used to represent list of minute historic data. It's used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
public class ZappiMinuteHistory {

    public String id;

    public List<ZappiMinuteHistoryEntry> entries = new ArrayList<>();

    @Override
    public String toString() {
        return "ZappiMinuteHistory [id=" + id + ", entries=" + entries + "]";
    }
}
