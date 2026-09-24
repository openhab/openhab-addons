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
 * Aggregate operating statistics returned by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class HourGeneral {
    private @Nullable Integer run;
    private @Nullable Integer mow;
    private @Nullable Integer search;
    private @Nullable Integer charge;
    private @Nullable Integer charges;
    private @Nullable Integer errors;

    public @Nullable Integer getRun() {
        return run;
    }

    public @Nullable Integer getMow() {
        return mow;
    }

    public @Nullable Integer getSearch() {
        return search;
    }

    public @Nullable Integer getCharge() {
        return charge;
    }

    public @Nullable Integer getCharges() {
        return charges;
    }

    public @Nullable Integer getErrors() {
        return errors;
    }
}
