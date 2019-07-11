/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.reader;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * The {@link Frame} class defines common attributes for any Teleinfo frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public abstract class Frame implements Serializable {

    private static final long serialVersionUID = -1934715078822532494L;

    public enum PeriodeTarifaire {
        TH,
        HC,
        HP,
        HN,
        PM
    }

    private UUID id;
    private Date timestamp; // UTC timestamp
    private String ADCO; // Identifiant du compteur
    private int intensiteSouscrite; // Intensité souscrite
    private PeriodeTarifaire periodeTarifaireEnCours; // PTEC : Période tarifaire en cours
    private int intensiteInstantanee; //// IINST : Intensité instantanée (en ampères)
    private Integer avertissementDepassementPuissanceSouscrite; // ADPS : Avertissement de dépassement de puissance
                                                                // souscrite (en ampères)
    private Integer intensiteMaximale; //// IMAX : Intensité maximale (en ampères)
    private int puissanceApparente; // PAPP : Puissance apparente (en Volt.ampères)
    private String motEtat; // MOTDETAT

    public Frame() {
        // default constructor
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getADCO() {
        return ADCO;
    }

    public void setADCO(String aDCO) {
        ADCO = aDCO;
    }

    public int getIntensiteSouscrite() {
        return intensiteSouscrite;
    }

    public void setIntensiteSouscrite(int intensiteSouscrite) {
        this.intensiteSouscrite = intensiteSouscrite;
    }

    public PeriodeTarifaire getPeriodeTarifaireEnCours() {
        return periodeTarifaireEnCours;
    }

    public void setPeriodeTarifaireEnCours(PeriodeTarifaire periodeTarifaireEnCours) {
        this.periodeTarifaireEnCours = periodeTarifaireEnCours;
    }

    public int getIntensiteInstantanee() {
        return intensiteInstantanee;
    }

    public void setIntensiteInstantanee(int intensiteInstantanee) {
        this.intensiteInstantanee = intensiteInstantanee;
    }

    public Integer getAvertissementDepassementPuissanceSouscrite() {
        return avertissementDepassementPuissanceSouscrite;
    }

    public void setAvertissementDepassementPuissanceSouscrite(Integer avertissementDepassementPuissanceSouscrite) {
        this.avertissementDepassementPuissanceSouscrite = avertissementDepassementPuissanceSouscrite;
    }

    public Integer getIntensiteMaximale() {
        return intensiteMaximale;
    }

    public void setIntensiteMaximale(Integer intensiteMaximale) {
        this.intensiteMaximale = intensiteMaximale;
    }

    public int getPuissanceApparente() {
        return puissanceApparente;
    }

    public void setPuissanceApparente(int puissanceApparente) {
        this.puissanceApparente = puissanceApparente;
    }

    public String getMotEtat() {
        return motEtat;
    }

    public void setMotEtat(String motEtat) {
        this.motEtat = motEtat;
    }

}
