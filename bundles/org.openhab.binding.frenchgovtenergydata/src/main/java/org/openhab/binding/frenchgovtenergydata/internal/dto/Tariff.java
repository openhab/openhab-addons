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
package org.openhab.binding.frenchgovtenergydata.internal.dto;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Tariff} is the base class holding common information for Tariffs
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Tariff {
    protected static final DateTimeFormatter TARIFF_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    protected final @NonNull String[] values;
    public final ZonedDateTime dateDebut;
    public final @Nullable ZonedDateTime dateFin;
    public final int puissance;
    public final double fixeHT;
    public final double fixeTTC;

    public Tariff(String line, int lenControl) {
        this.values = line.replace(',', '.').split(";", -1);
        try {
            if (values.length == lenControl) {
                this.dateDebut = LocalDate.parse(values[0], TARIFF_DATE_FORMAT).atStartOfDay(ZoneOffset.UTC);
                this.dateFin = !values[1].isEmpty()
                        ? LocalDate.parse(values[1], TARIFF_DATE_FORMAT).atStartOfDay(ZoneOffset.UTC)
                        : null;
                this.puissance = Integer.parseInt(values[2]);
                this.fixeHT = parseDouble(values[3]);
                this.fixeTTC = parseDouble(values[4]);
            } else {
                throw new IllegalArgumentException("Unexpected number of data, %d expected".formatted(lenControl));
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException("Incorrect data in '%s'".formatted(line), e);
        }
    }

    public static double parseDouble(String input) {
        if (input.isBlank()) {
            return 0;
        }
        return Double.parseDouble(input);
    }

    public boolean isActive() {
        return dateFin == null;
    }
}
