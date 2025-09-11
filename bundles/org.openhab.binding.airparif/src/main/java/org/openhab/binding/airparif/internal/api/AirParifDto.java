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
package org.openhab.binding.airparif.internal.api;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airparif.internal.api.AirParifApi.Pollen;
import org.openhab.binding.airparif.internal.api.AirParifApi.Scope;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

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
            Instant expiration, //
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

        private State getQuantity(int value) {
            Unit<?> unit = pollutant.unit;
            if (unit != null) {
                return new QuantityType<>(value, unit);
            }
            return UnDefType.NULL;
        }

        public State getMin() {
            return getQuantity(min);
        }

        public State getMax() {
            return getQuantity(max);
        }
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

        public boolean isToday() {
            return previsionDate.equals(LocalDate.now());
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
            @SerializedName("actif") boolean active, //
            Message message, //
            @SerializedName("jour") DailyEpisode today, //
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
        private @Nullable Instant beginValidity;
        private @Nullable Instant endValidity;

        public Optional<Pollens> getData() {
            return Optional.ofNullable(data.isEmpty() ? null : data.get(0));
        }

        private Set<Instant> getValidities() {
            Set<Instant> validities = new TreeSet<>();
            getData().ifPresent(pollens -> {
                Matcher matcher = PATTERN.matcher(pollens.periode);
                while (matcher.find()) {
                    validities.add(LocalDate.parse(matcher.group(), FORMATTER).atStartOfDay(DEFAULT_ZONE).toInstant());
                }
            });

            return validities;
        }

        public Optional<Instant> getBeginValidity() {
            if (beginValidity == null) {
                beginValidity = getValidities().iterator().next();
            }
            return Optional.ofNullable(beginValidity);
        }

        public Optional<Instant> getEndValidity() {
            if (endValidity == null) {
                endValidity = getValidities().stream().reduce((prev, next) -> next).orElse(null);
            }
            return Optional.ofNullable(endValidity);
        }

        public Duration getValidityDuration() {
            return Objects.requireNonNull(getEndValidity().map(end -> {
                Duration duration = Duration.between(Instant.now(), end);
                return duration.isNegative() ? Duration.ZERO : duration;
            }).orElse(Duration.ZERO));
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

    public record Concentration(//
            @SerializedName("polluant") Pollutant pollutant, //
            Instant date, //
            @SerializedName("valeurs") double[] values, //
            @Nullable Message message) {

        public State getMessage() {
            return message != null ? new StringType(message.fr()) : UnDefType.NULL;
        }

        public State getQuantity() {
            Unit<?> unit = pollutant.unit;
            return unit != null ? new QuantityType<>(getValue(), unit) : UnDefType.NULL;
        }

        public State getDate() {
            return new DateTimeType(date);
        }

        public double getValue() {
            return values[0];
        }

        public int getAlertLevel() {
            return pollutant.getAppreciation(getValue()).ordinal();
        }
    }

    public record Route(//
            @SerializedName("dateRequise") Instant requestedDate, //
            double[][] longlats, //
            @SerializedName("resultats") List<Concentration> concentrations, //
            @Nullable Message[] messages) {

    }

    public record ItineraireResponse(@SerializedName("itineraires") Route[] routes) {
    }
}
