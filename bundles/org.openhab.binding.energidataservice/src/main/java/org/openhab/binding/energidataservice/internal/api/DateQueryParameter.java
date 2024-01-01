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
package org.openhab.binding.energidataservice.internal.api;

import java.time.Duration;
import java.time.LocalDate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a query parameter of type {@link LocalDate} or a
 * dynamic date defined as {@link DateQueryParameterType} with an optional offset.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DateQueryParameter {

    public static final DateQueryParameter EMPTY = new DateQueryParameter();

    private @Nullable LocalDate date;
    private @Nullable Duration offset;
    private @Nullable DateQueryParameterType dateType;

    private DateQueryParameter() {
    }

    public DateQueryParameter(LocalDate date) {
        this.date = date;
    }

    public DateQueryParameter(DateQueryParameterType dateType, Duration offset) {
        this.dateType = dateType;
        this.offset = offset;
    }

    public DateQueryParameter(DateQueryParameterType dateType) {
        this.dateType = dateType;
    }

    @Override
    public String toString() {
        LocalDate date = this.date;
        if (date != null) {
            return date.toString();
        }
        DateQueryParameterType dateType = this.dateType;
        if (dateType != null) {
            Duration offset = this.offset;
            if (offset == null || offset.isZero()) {
                return dateType.toString();
            } else {
                return dateType.toString()
                        + (offset.isNegative() ? "-" + offset.abs().toString() : "+" + offset.toString());
            }
        }
        return "null";
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public static DateQueryParameter of(LocalDate localDate) {
        return new DateQueryParameter(localDate);
    }

    public static DateQueryParameter of(DateQueryParameterType dateType, Duration offset) {
        if (offset.isZero()) {
            return new DateQueryParameter(dateType);
        } else {
            return new DateQueryParameter(dateType, offset);
        }
    }

    public static DateQueryParameter of(DateQueryParameter parameter, Duration offset) {
        DateQueryParameterType parameterType = parameter.dateType;
        if (parameterType == null) {
            return parameter;
        }
        Duration parameterOffset = parameter.offset;
        if (parameterOffset == null || parameterOffset.isZero()) {
            return of(parameterType, offset);
        }
        return of(parameterType, parameterOffset.plus(offset));
    }

    public static DateQueryParameter of(DateQueryParameterType dateType) {
        return new DateQueryParameter(dateType);
    }
}
