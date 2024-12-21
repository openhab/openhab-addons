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
package org.openhab.binding.linky.internal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ConsumptionReport} is responsible for holding values
 * returned by API calls
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class ConsumptionReport {

    public class Data {
        public LocalDateTime dateDebut;
        public LocalDateTime dateFin;
        public String valeur;
    }

    public class Datas {
        @SerializedName("donnees")
        public List<Data> datas;
        public String unite;
    }

    public class ChronoData {
        @SerializedName("jour")
        public Datas days;

        @SerializedName("heure")
        public Datas heure;
    }

    public class Consumption {
        public ChronoData aggregats;

        public String grandeurMetier;
        public String grandeurPhysique;
        public LocalDate dateDebut;
        public LocalDate dateFin;
    }

    @SerializedName("cons")
    public Consumption consumptions;

}
