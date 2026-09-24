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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A boundary-wire sensor measurement returned by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class Sensor {
    private @Nullable String description;
    private @Nullable Integer quality;
    private @Nullable Integer a;
    private @Nullable Integer n;
    private @Nullable Integer f;
    private @Nullable Integer g1;

    public @Nullable String getDescription() {
        return description;
    }

    public @Nullable Integer getQuality() {
        return quality;
    }

    public @Nullable Integer getA() {
        return a;
    }

    public @Nullable Integer getN() {
        return n;
    }

    public @Nullable Integer getF() {
        return f;
    }

    public @Nullable Integer getG1() {
        return g1;
    }
}
