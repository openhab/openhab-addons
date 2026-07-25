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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Overrides a measurand's {@link MeasureType} for a specific {@link ParserCustomizationType}.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class ParserCustomization {
    private final ParserCustomizationType type;
    private final MeasureType measureType;

    public ParserCustomization(ParserCustomizationType type, MeasureType measureType) {
        this.type = type;
        this.measureType = measureType;
    }

    public ParserCustomizationType getType() {
        return type;
    }

    public MeasureType getMeasureType() {
        return measureType;
    }
}
