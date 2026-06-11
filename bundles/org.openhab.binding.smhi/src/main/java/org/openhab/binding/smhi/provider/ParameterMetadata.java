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
package org.openhab.binding.smhi.provider;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Record class for storing parameter metadata.
 *
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public record ParameterMetadata(String name, String shortName, String description, String levelType, BigDecimal level,
        String unit, BigDecimal missingValue) {
}
