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
package org.openhab.binding.frenchgovtenergydata.internal.dto;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

    protected final String[] values;
    public final ZonedDateTime dateDebut;
    public final @Nullable ZonedDateTime dateFin;
    public final int puissance;
    public final double fixeHT;
    public final double fixeTTC;

    public Tariff(String line, int lenControl) {
        this.values = line.replace(',', '.').split(";");
        if (values.length == lenControl) {
            try {
                this.dateDebut = LocalDate.parse(values[0], TARIFF_DATE_FORMAT).atStartOfDay(ZoneOffset.UTC);
                this.dateFin = !values[1].isEmpty()
                        ? LocalDate.parse(values[1], TARIFF_DATE_FORMAT).atStartOfDay(ZoneOffset.UTC)
                        : null;
                this.puissance = Integer.parseInt(values[2]);
                this.fixeHT = Double.parseDouble(values[3]);
                this.fixeTTC = Double.parseDouble(values[4]);
            } catch (NumberFormatException | DateTimeParseException e) {
                throw new IllegalArgumentException("Incorrect data in '%s'".formatted(line), e);
            }
        } else {
            throw new IllegalArgumentException("Unexpected number of data, %d expected".formatted(lenControl));
        }
    }

    public boolean isActive() {
        return dateFin == null;
    }
}
