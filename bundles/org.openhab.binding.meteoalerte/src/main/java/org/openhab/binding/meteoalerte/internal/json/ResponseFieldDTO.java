/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
    public enum AlertLevel {
        UNKNOWN,
        @SerializedName("Vert")
        GREEN,
        @SerializedName("Jaune")
        YELLOW,
        @SerializedName("Orange")
        ORANGE,
        @SerializedName("Rouge")
        RED;
    }

    @SerializedName("nom_reg")
    private String nomReg = "";
    @SerializedName("typeprev")
    private String typePrev = "";
    @SerializedName("etat_canicule")
    private AlertLevel canicule = AlertLevel.UNKNOWN;
    @SerializedName("nom_dept")
    private String nomDept = "";
    @SerializedName("etat_grand_froid")
    private AlertLevel grandFroid = AlertLevel.UNKNOWN;;
    @SerializedName("noversion")
    private String noVersion = "";
    @SerializedName("etat_pluie_inondation")
    private AlertLevel pluieInondation = AlertLevel.UNKNOWN;;
    @SerializedName("etat_neige")
    private AlertLevel neige = AlertLevel.UNKNOWN;;
    @SerializedName("etat_vent")
    private AlertLevel vent = AlertLevel.UNKNOWN;;
    @SerializedName("etat_inondation")
    private AlertLevel inondation = AlertLevel.UNKNOWN;;
    @SerializedName("etat_avalanches")
    private AlertLevel avalanches = AlertLevel.UNKNOWN;;
    @SerializedName("etat_orage")
    private AlertLevel orage = AlertLevel.UNKNOWN;
    private int echeance;
    @SerializedName("etat_vague_submersion")
    private AlertLevel vagueSubmersion = AlertLevel.UNKNOWN;;
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
        return Optional.ofNullable(datePrevue);
    }

    public String getTypePrev() {
        return typePrev;
    }

    public AlertLevel getCanicule() {
        return canicule;
    }

    public String getNomDept() {
        return nomDept;
    }

    public AlertLevel getGrandFroid() {
        return grandFroid;
    }

    public String getNoVersion() {
        return noVersion;
    }

    public AlertLevel getPluieInondation() {
        return pluieInondation;
    }

    public AlertLevel getNeige() {
        return neige;
    }

    public AlertLevel getVent() {
        return vent;
    }

    public Optional<ZonedDateTime> getDateInsert() {
        return Optional.ofNullable(dateInsert);
    }

    public AlertLevel getInondation() {
        return inondation;
    }

    public AlertLevel getAvalanches() {
        return avalanches;
    }

    public AlertLevel getOrage() {
        return orage;
    }

    public int getEcheance() {
        return echeance;
    }

    public AlertLevel getVagueSubmersion() {
        return vagueSubmersion;
    }

    public String getDep() {
        return dep;
    }

    public Optional<ZonedDateTime> getDateRun() {
        return Optional.ofNullable(dateRun);
    }
}
