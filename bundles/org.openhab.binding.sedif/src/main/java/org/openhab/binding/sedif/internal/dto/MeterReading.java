package org.openhab.binding.sedif.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class MeterReading extends Value {
    public class Data {
        public class CONSOMMATION {
            @SerializedName("CONSOMMATION")
            public float consommation;

            @SerializedName("DATE_INDEX")
            public String dateIndex;

            @SerializedName("DEBIT_PERMANENT")
            public String debitPermanent;

            @SerializedName("FLAG_ESTIMATION")
            public boolean flagEstimation;

            @SerializedName("FLAG_ESTIMATION_INDEX")
            public boolean flagEstimationIndex;

            @SerializedName("VALEUR_INDEX")
            public float valeurIndex;
        }

        @SerializedName("CONSOMMATION")
        public @Nullable List<CONSOMMATION> consommation = new ArrayList<CONSOMMATION>();

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

    }

    public @Nullable Data data;
    public boolean showDebitPermanent;
    public float seuilDebitPermanet;
    public float prixMoyenEau;
    public boolean canCompareMonth;
    public String numeroCompteur;

}
