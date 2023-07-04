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
package org.openhab.binding.toyota.internal.dto;

import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.reflect.TypeToken;

public class Metrics {
    public static Type LIST_CLASS = new TypeToken<List<Metrics>>() {
    }.getType();

    public String type;
    public double value;
    public @Nullable String unit;
}
