/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.transform.vat.internal.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * DTO representing a VAT rate in a specific validity period.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public record VATPeriod(Instant start, Instant end, BigDecimal percentage) {

    @Override
    public Instant start() {
        return Objects.isNull(start) ? Instant.MIN : start;
    }

    @Override
    public Instant end() {
        return Objects.isNull(end) ? Instant.MAX : end;
    }

    @Override
    public String toString() {
        return "VATPeriod{start='" + start() + "', end='" + end() + "', percentage=" + percentage + '}';
    }
}
