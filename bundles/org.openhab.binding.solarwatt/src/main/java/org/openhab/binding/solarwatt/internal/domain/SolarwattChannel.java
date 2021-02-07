/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.solarwatt.internal.domain;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class SolarwattChannel {
    private final String tagName;
    private final @Nullable Unit<?> unit;
    private final String category;

    public SolarwattChannel(String tagName, String category) {
        this.tagName = tagName;
        this.unit = null;
        this.category = category;
    }

    public SolarwattChannel(String tagName, Unit<?> unit, String category) {
        this.tagName = tagName;
        this.unit = unit;
        this.category = category;
    }

    public String getTagName() {
        return this.tagName;
    }

    public @Nullable Unit<?> getUnit() {
        return this.unit;
    }

    public String getCategory() {
        return this.category;
    }
}
