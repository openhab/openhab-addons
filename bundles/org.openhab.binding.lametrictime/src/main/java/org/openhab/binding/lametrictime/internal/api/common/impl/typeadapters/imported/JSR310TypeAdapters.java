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
/*
 * Imported from https://github.com/google-gson/typeadapters/tree/master/jsr310/src
 * and repackaged to avoid the default package.
 */
package org.openhab.binding.lametrictime.internal.api.common.impl.typeadapters.imported;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.GsonBuilder;

/**
 * Helper methods to register JSR310 type adapters.
 *
 * @author Christophe Bornet - Initial contribution
 */
@NonNullByDefault
public class JSR310TypeAdapters {

    private JSR310TypeAdapters() {
    }

    public static GsonBuilder registerDurationTypeAdapter(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());
    }

    public static GsonBuilder registerInstantTypeAdapter(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapter(Instant.class, new InstantTypeAdapter());
    }

    public static GsonBuilder registerLocalDateTimeTypeAdapter(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter());
    }

    public static GsonBuilder registerLocalDateTypeAdapter(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter());
    }

    public static GsonBuilder registerLocalTimeTypeAdapter(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapter(LocalTime.class, new LocalTimeTypeAdapter());
    }

    public static GsonBuilder registerMonthDayTypeAdapter(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapter(MonthDay.class, new MonthDayTypeAdapter());
    }

    public static GsonBuilder registerOffsetDateTimeTypeAdapter(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeTypeAdapter());
    }

    public static GsonBuilder registerOffsetTimeTypeAdapter(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapter(OffsetTime.class, new OffsetTimeTypeAdapter());
    }

    public static GsonBuilder registerPeriodTypeAdapter(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapter(Period.class, new PeriodTypeAdapter());
    }

    public static GsonBuilder registerYearMonthTypeAdapter(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapter(YearMonth.class, new YearMonthTypeAdapter());
    }

    public static GsonBuilder registerYearTypeAdapter(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapter(Year.class, new YearTypeAdapter());
    }

    public static GsonBuilder registerZonedDateTimeTypeAdapter(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter());
    }

    /**
     * Helper method to register all the available JSR310 adapters at once.
     *
     * @param gsonBuilder the gsonBuilder on which all the JSR310 adapters must be registered.
     * @return the gsonBuilder with the JSR310 adapters registered.
     */
    public static GsonBuilder registerJSR310TypeAdapters(GsonBuilder gsonBuilder) {
        registerDurationTypeAdapter(gsonBuilder);
        registerInstantTypeAdapter(gsonBuilder);
        registerLocalDateTimeTypeAdapter(gsonBuilder);
        registerLocalDateTypeAdapter(gsonBuilder);
        registerLocalTimeTypeAdapter(gsonBuilder);
        registerMonthDayTypeAdapter(gsonBuilder);
        registerOffsetDateTimeTypeAdapter(gsonBuilder);
        registerOffsetTimeTypeAdapter(gsonBuilder);
        registerPeriodTypeAdapter(gsonBuilder);
        registerYearMonthTypeAdapter(gsonBuilder);
        registerYearTypeAdapter(gsonBuilder);
        registerZonedDateTimeTypeAdapter(gsonBuilder);

        return gsonBuilder;
    }
}
