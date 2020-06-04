/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.meteoalerte.internal.json;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ResponseFieldDTO} is the Java class used to map the JSON
 * response to the webservice request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ResponseFieldDTO {
    @SerializedName("nom_reg")
    private String nomReg = "";
    @SerializedName("typeprev")
    private String typePrev = "";
    @SerializedName("etat_canicule")
    private String canicule = "";
    @SerializedName("nom_dept")
    private String nomDept = "";
    @SerializedName("etat_grand_froid")
    private String grandFroid = "";
    @SerializedName("noversion")
    private String noVersion = "";
    @SerializedName("etat_pluie_inondation")
    private String pluieInondation = "";
    @SerializedName("etat_neige")
    private String neige = "";
    @SerializedName("etat_vent")
    private String vent = "";
    @SerializedName("etat_inondation")
    private String inondation = "";
    @SerializedName("etat_avalanches")
    private String avalanches = "";
    @SerializedName("etat_orage")
    private String orage = "";
    private int echeance;
    @SerializedName("etat_vague_submersion")
    private String vagueSubmersion = "";
    private String dep = "";
    @SerializedName("vigilancecommentaire_texte")
    private String vigilanceComment = "";
    @SerializedName("dateprevue")
    private @Nullable ZonedDateTime datePrevue;
    @SerializedName("dateinsert")
    private @Nullable ZonedDateTime dateInsert;
    @SerializedName("daterun")
    private @Nullable ZonedDateTime dateRun;

    public String getVigilanceComment() {
        return vigilanceComment;
    }

    public String getNomReg() {
        return nomReg;
    }

    public Optional<ZonedDateTime> getDatePrevue() {
        ZonedDateTime datePrevue = this.datePrevue;
        if (datePrevue != null) {
            return Optional.of(datePrevue);
        }
        return Optional.empty();
    }

    public String getTypePrev() {
        return typePrev;
    }

    public String getCanicule() {
        return canicule;
    }

    public String getNomDept() {
        return nomDept;
    }

    public String getGrandFroid() {
        return grandFroid;
    }

    public String getNoVersion() {
        return noVersion;
    }

    public String getPluieInondation() {
        return pluieInondation;
    }

    public String getNeige() {
        return neige;
    }

    public String getVent() {
        return vent;
    }

    public Optional<ZonedDateTime> getDateInsert() {
        ZonedDateTime dateInsert = this.dateInsert;
        if (dateInsert != null) {
            return Optional.of(dateInsert);
        }
        return Optional.empty();
    }

    public String getInondation() {
        return inondation;
    }

    public String getAvalanches() {
        return avalanches;
    }

    public String getOrage() {
        return orage;
    }

    public int getEcheance() {
        return echeance;
    }

    public String getVagueSubmersion() {
        return vagueSubmersion;
    }

    public String getDep() {
        return dep;
    }

    public Optional<ZonedDateTime> getDateRun() {
        ZonedDateTime dateRun = this.dateRun;
        if (dateRun != null) {
            return Optional.of(dateRun);
        }
        return Optional.empty();
    }

}
