/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.dto;

import java.time.ZonedDateTime;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ConsumptionReport} is responsible for holding values
 * returned by API calls
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class ConsumptionReport {
    public class Period {
        public String grandeurPhysiqueEnum;
        public ZonedDateTime dateDebut;
        public ZonedDateTime dateFin;
    }

    public class Aggregate {
        public List<String> labels;
        public List<Period> periodes;
        public List<Double> datas;
    }

    public class ChronoData {
        @SerializedName("JOUR")
        public Aggregate days;
        @SerializedName("SEMAINE")
        public Aggregate weeks;
        @SerializedName("MOIS")
        public Aggregate months;
        @SerializedName("ANNEE")
        public Aggregate years;
    }

    public class Consumption {
        public ChronoData aggregats;
        public String grandeurMetier;
        public String grandeurPhysique;
        public String unite;
    }

    public class FirstLevel {
        @SerializedName("CONS")
        public Consumption consumptions;
    }

    @SerializedName("1")
    public FirstLevel firstLevel;
}
