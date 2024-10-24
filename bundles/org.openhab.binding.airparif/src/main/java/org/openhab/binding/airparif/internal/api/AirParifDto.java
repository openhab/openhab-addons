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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
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

    public record Message(//
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
    }

    public class PollensResponse {
        private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");
        private static Pattern PATTERN = Pattern.compile("\\d{2}.\\d{2}.\\d{2}");
        private static ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Paris");

        public List<Pollens> data = List.of();

        public Optional<Pollens> getData() {
            return Optional.ofNullable(data.isEmpty() ? null : data.get(0));
        }

        private Set<ZonedDateTime> getValidities() {
            Set<ZonedDateTime> result = new TreeSet<>();
            getData().ifPresent(pollens -> {
                Matcher matcher = PATTERN.matcher(pollens.periode);
                while (matcher.find()) {
                    result.add(LocalDate.parse(matcher.group(), FORMATTER).atStartOfDay(DEFAULT_ZONE));
                }
            });
            return result;
        }

        public Optional<ZonedDateTime> getBeginValidity() {
            return Optional.ofNullable(getValidities().iterator().next());
        }

        public Optional<ZonedDateTime> getEndValidity() {
            return Optional.ofNullable(getValidities().stream().reduce((prev, next) -> next).orElse(null));
        }

        public Optional<String> getComment() {
            return getData().map(pollens -> pollens.commentaire);
        }

        public Map<Pollen, PollenAlertLevel> getDepartment(String id) {
            Map<Pollen, PollenAlertLevel> result = new HashMap<>();
            Optional<Pollens> donnees = getData();
            if (donnees.isPresent()) {
                Pollens depts = donnees.get();
                PollenAlertLevel[] valeurs = depts.valeurs.get(id);
                if (valeurs != null) {
                    for (int i = 0; i < valeurs.length; i++) {
                        result.put(depts.taxons[i], valeurs[i]);
                    }
                }
            }
            return result;
        }
    }

    public record Result(//
            @SerializedName("polluant") Pollutant pollutant, //
            ZonedDateTime date, //
            @SerializedName("valeurs") double[] values, //
            Message message) {
    }

    public record Route(//
            @SerializedName("dateRequise") ZonedDateTime requestedDate, //
            double[][] longlats, //
            @SerializedName("resultats") Result[] results, //
            @Nullable Message[] messages) {

    }

    public record ItineraireResponse(@SerializedName("itineraires") Route[] routes) {
    }
}
