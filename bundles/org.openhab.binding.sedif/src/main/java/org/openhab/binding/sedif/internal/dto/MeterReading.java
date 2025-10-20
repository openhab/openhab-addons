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
package org.openhab.binding.sedif.internal.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MeterReading} holds Meter reading values
 *
 * @author Laurent Arnal - Initial contribution
 */
public class MeterReading extends Value {
    public class Data {
        public Data() {
            hasModifications = false;
            consoMap = new HashMap<String, Consommation>();
        }

        public class Consommation {
            @SerializedName("CONSOMMATION")
            public float consommation;

            @SerializedName("DATE_INDEX")
            public LocalDateTime dateIndex;

            @SerializedName("DEBIT_PERMANENT")
            public String debitPermanent;

            @SerializedName("FLAG_ESTIMATION")
            public boolean flagEstimation;

            @SerializedName("FLAG_ESTIMATION_INDEX")
            public boolean flagEstimationIndex;

            @SerializedName("VALEUR_INDEX")
            public float valeurIndex;
        }

        private transient Map<String, Consommation> consoMap;

        public void putEntries(String key, Consommation conso) {
            if (consoMap == null) {
                consoMap = new HashMap<String, MeterReading.Data.Consommation>();
            }
            consoMap.put(key, conso);
        }

        public Consommation getEntries(String key) {
            return consoMap.get(key);
        }

        @SerializedName("CONSOMMATION")
        public @Nullable Consommation[] consommation;
        public @Nullable Consommation[] weekConso;
        public @Nullable Consommation[] monthConso;
        public @Nullable Consommation[] yearConso;

        @SerializedName("CONSOMMATION_MAX")
        public float consommationMax;

        @SerializedName("CONSOMMATION_MOYENNE")
        public float consommationMoyenne;

        @SerializedName("DATE_CONSOMMATION_MAX")
        public String dateConsommationMax;

        @SerializedName("DATE_DEBUT")
        public String dateDebut;

        @SerializedName("DATE_FIN")
        public String dateFin;

        @SerializedName("ID_PDS")
        public String idPds;

        @SerializedName("NUMERO_COMPTEUR")
        public String numeroCompteur;

        public boolean hasModifications;

        public boolean hasModifications() {
            return hasModifications;
        }
    }

    public Data data;
    public boolean showDebitPermanent;
    public float seuilDebitPermanet;
    public float prixMoyenEau;
    public boolean canCompareMonth;
    public String numeroCompteur;

    public MeterReading() {
        data = new Data();
    }

}
