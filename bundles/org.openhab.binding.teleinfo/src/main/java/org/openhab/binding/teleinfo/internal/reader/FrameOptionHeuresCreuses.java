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

/**
 * The {@link FrameOptionHeuresCreuses} class defines a Teleinfo frame with HC/HP option.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class FrameOptionHeuresCreuses extends Frame implements Serializable {

    public static enum GroupeHoraire {
        A,
        C,
        D,
        E,
        Y
    };

    private static final long serialVersionUID = -1934715078822532494L;

    private Integer indexHeuresCreuses; // HCHC : Index heures creuses si option = heures creuses (en Wh)
    private Integer indexHeuresPleines; // HCHP : Index heures pleines si option = heures creuses (en Wh)
    private GroupeHoraire groupeHoraire; // HHPHC : Groupe horaire si option = heures creuses ou tempo

    public FrameOptionHeuresCreuses() {
        // default constructor
    }

    public Integer getIndexHeuresCreuses() {
        return indexHeuresCreuses;
    }

    public void setIndexHeuresCreuses(Integer indexHeuresCreuses) {
        this.indexHeuresCreuses = indexHeuresCreuses;
    }

    public Integer getIndexHeuresPleines() {
        return indexHeuresPleines;
    }

    public void setIndexHeuresPleines(Integer indexHeuresPleines) {
        this.indexHeuresPleines = indexHeuresPleines;
    }

    public GroupeHoraire getGroupeHoraire() {
        return groupeHoraire;
    }

    public void setGroupeHoraire(GroupeHoraire groupeHoraire) {
        this.groupeHoraire = groupeHoraire;
    }

    @Override
    public String toString() {
        return "FrameOptionHeuresCreuses [timestamp=" + getTimestamp() + ", ADCO=" + getADCO() + ", indexHeuresCreuses="
                + indexHeuresCreuses + " Wh, indexHeuresPleines=" + indexHeuresPleines + " Wh, intensiteInstantanee="
                + getIntensiteInstantanee() + " A, puissanceApparente=" + getPuissanceApparente()
                + " VA, periodeTarifaireEnCours=" + getPeriodeTarifaireEnCours() + "]";
    }
}
