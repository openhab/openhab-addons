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

/**
 * The {@link Fields} is the Java class used to map the JSON
 * response to the webservice request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class Fields {
    private String nom_reg;
    private String dateprevue;
    private String typeprev;
    private String etat_canicule;
    private String nom_dept;
    private String etat_grand_froid;
    private String noversion;
    private String etat_pluie_inondation;
    private String etat_neige;
    private String etat_vent;
    private String dateinsert;
    private String etat_inondation;
    private String etat_avalanches;
    private String etat_orage;
    private int echeance;
    private String etat_vague_submersion;
    private String dep;
    private String daterun;
    private String vigilancecommentaire_texte;

    public String getVigilanceCommentaireTexte() {
        return vigilancecommentaire_texte;
    }

    public String getNomReg() {
        return nom_reg;
    }

    public String getDatePrevue() {
        return dateprevue;
    }

    public String getTypePrev() {
        return typeprev;
    }

    public String getEtatCanicule() {
        return etat_canicule;
    }

    public String getNomDept() {
        return nom_dept;
    }

    public String getEtatGrandFroid() {
        return etat_grand_froid;
    }

    public String getNoVersion() {
        return noversion;
    }

    public String getEtatPluieInondation() {
        return etat_pluie_inondation;
    }

    public String getEtatNeige() {
        return etat_neige;
    }

    public String getEtatVent() {
        return etat_vent;
    }

    public String getDateInsert() {
        return dateinsert;
    }

    public String getEtatInondation() {
        return etat_inondation;
    }

    public String getEtatAvalanches() {
        return etat_avalanches;
    }

    public String getEtatOrage() {
        return etat_orage;
    }

    public int getEcheance() {
        return echeance;
    }

    public String getEtatVagueSubmersion() {
        return etat_vague_submersion;
    }

    public String getDep() {
        return dep;
    }

    public String getDateRun() {
        return daterun;
    }

}
