package org.openhab.binding.vigicrues.internal.dto.hubeau;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class HubEauResponse {
    public class StationData {
        @SerializedName("en_service")
        public boolean enService;

        @SerializedName("code_station")
        public String codeStation;

        @SerializedName("libelle_station")
        public String libelleStation;

        @SerializedName("longitude_station")
        public double longitudeStation;

        @SerializedName("latitude_station")
        public double latitudeStation;

        @SerializedName("libelle_cours_eau")
        public String libelleCoursEau;
        /*
         * Currently unused, maybe interesting in the future
         *
         * @SerializedName("code_site")
         * public String codeSite;
         *
         * @SerializedName("libelle_site")
         * public String libelleSite;
         *
         * @SerializedName("type_station")
         * public String typeStation;
         *
         * @SerializedName("coordonnee_x_station")
         * public int coordonneeXStation;
         *
         * @SerializedName("coordonnee_y_station")
         * public int coordonneeYStation;
         *
         * @SerializedName("code_projection")
         * public int codeProjection;
         *
         * @SerializedName("influence_locale_station")
         * public int influenceLocaleStation;
         *
         * @SerializedName("commentaire_station")
         * public String commentaireStation;
         *
         * @SerializedName("altitude_ref_alti_station")
         * public double altitudeRefAltiStation;
         *
         * @SerializedName("code_systeme_alti_site")
         * public int codeSystemeAltiSite;
         *
         * @SerializedName("code_commune_station")
         * public String codeCommuneStation;
         *
         * @SerializedName("libelle_commune")
         * public String libelleCommune;
         *
         * @SerializedName("code_departement")
         * public String codeDepartement;
         *
         * @SerializedName("code_region")
         * public String codeRegion;
         *
         * @SerializedName("libelle_region")
         * public String libelleRegion;
         *
         * @SerializedName("code_cours_eau")
         * public String codeCoursEau;
         *
         * @SerializedName("uri_cours_eau")
         * public String uriCoursEau;
         *
         * @SerializedName("descriptif_station")
         * public String descriptifStation;
         *
         * @SerializedName("date_maj_station")
         * public String dateMajStation;
         *
         * @SerializedName("date_ouverture_station")
         * public String dateOuvertureStation;
         *
         * @SerializedName("date_fermeture_station")
         * public String dateFermetureStation;
         *
         * @SerializedName("commentaire_influence_locale_station")
         * public String commentaireInfluenceLocaleStation;
         *
         * @SerializedName("code_regime_station")
         * public int codeRegimeStation;
         *
         * @SerializedName("qualification_donnees_station")
         * public int qualificationDonneesStation;
         *
         * @SerializedName("code_finalite_station")
         * public int codeFinaliteStation;
         *
         * @SerializedName("type_contexte_loi_stat_station")
         * public int typeContexteLoiStatStation;
         *
         * @SerializedName("type_loi_station")
         * public int typeLoiStation;
         *
         * @SerializedName("code_sandre_reseau_station")
         * public List<String> codeSandreReseauStation;
         *
         * @SerializedName("date_debut_ref_alti_station")
         * public String dateDebutRefAltiStation;
         *
         * @SerializedName("date_activation_ref_alti_station")
         * public String dateActivationRefAltiStation;
         *
         * @SerializedName("date_maj_ref_alti_station")
         * public String dateMajRefAltiStation;
         *
         * @SerializedName("libelle_departement")
         * public String libelleDepartement;
         * public Geometry geometry;
         */
    }

    public int count;
    @SerializedName("data")
    public List<StationData> stations;

    /*
     * Currently unused, maybe interesting in the future
     * public String first;
     * public String last;
     * public String prev;
     * public String next;
     *
     * @SerializedName("api_version")
     * public String apiVersion;
     *
     * public class Crs {
     * public String type;
     * public Properties properties;
     * }
     *
     * public class Properties {
     * public String name;
     * }
     *
     * public class Geometry {
     * public String type;
     * public Crs crs;
     * public List<Double> coordinates;
     * }
     */
}
