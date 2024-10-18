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
package org.openhab.binding.airparif.internal.api;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airparif.internal.api.AirParifApi.Pollen;
import org.openhab.binding.airparif.internal.api.AirParifApi.Scope;

import com.google.gson.annotations.SerializedName;

/**
 * {@link AirParifDto} class defines DTO used to interact with server api
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class AirParifDto {
    public record Version(//
            String version) {
    }

    public record KeyInfo(//
            ZonedDateTime expiration, //
            @SerializedName("droits") Set<Scope> scopes) {
    }

    private record Message(//
            String fr, //
            @Nullable String en) {
    }

    public record PollutantConcentration(//
            Pollutant pollutant, //
            int min, //
            int max) {
    }

    public record PollutantEpisode(//
            @SerializedName("nom") Pollutant pollutant, //
            @SerializedName("niveau") String level) {
    }

    public record DailyBulletin(//
            @SerializedName("date") LocalDate previsionDate, //
            @SerializedName("date_previ") LocalDate productionDate, //
            @SerializedName("disponible") boolean available, //
            Message bulletin, //
            Set<PollutantConcentration> concentrations) {
        public String dayDescription() {
            return bulletin.fr;
        }
    }

    public record DailyEpisode(//
            @SerializedName("actif") boolean active, //
            @SerializedName("polluants") Set<PollutantEpisode> pollutants) {
    }

    public record Bulletin( //
            @SerializedName("jour") DailyBulletin today, //
            @SerializedName("demain") DailyBulletin tomorrow) {
    }

    public record Episode( //
            @SerializedName("actif") boolean active, Message message, @SerializedName("jour") DailyEpisode today, //
            @SerializedName("demain") DailyEpisode tomorrow) {
    }

    public record Pollens(//
            Pollen[] taxons, //
            Map<String, PollenAlertLevel[]> valeurs, //
            String commentaire, //
            String periode) {

        private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");
        private static Pattern PATTERN = Pattern.compile("\\d{2}.\\d{2}.\\d{2}");

        private static @Nullable LocalDate getValidity(String periode, boolean begin) {
            Matcher matcher = PATTERN.matcher(periode);
            if (matcher.find()) {
                String extractedDate = matcher.group();
                if (begin) {
                    return LocalDate.parse(extractedDate, FORMATTER);
                }
                if (matcher.find()) {
                    extractedDate = matcher.group();
                    return LocalDate.parse(extractedDate, FORMATTER);
                }
            }
            return null;
        }

        public @Nullable LocalDate beginValidity() {
            return getValidity(periode, true);
        }

        public @Nullable LocalDate endValidity() {
            return getValidity(periode, false);
        }

    }

    public record PollensResponse(ArrayList<Pollens> data) {
    }

}
