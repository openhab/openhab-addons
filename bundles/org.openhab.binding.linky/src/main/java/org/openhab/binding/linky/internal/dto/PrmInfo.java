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

/**
 * The {@link UserInfo} holds informations about energy delivery point
 *
 * @author Gaël L'hopital - Initial contribution
 */

public class PrmInfo {
    public class Adresse {
        public Object adresseLigneUn;
        public String adresseLigneDeux;
        public Object adresseLigneTrois;
        public String adresseLigneQuatre;
        public Object adresseLigneCinq;
        public String adresseLigneSix;
        public String adresseLigneSept;
    }

    public String prmId;
    public String dateFinRole;
    public String segment;
    public Adresse adresse;
    public String typeCompteur;
    public String niveauOuvertureServices;
    public String communiquant;
    public long dateSoutirage;
    public String dateInjection;
    public int departement;
    public int puissanceSouscrite;
    public String codeCalendrier;
    public String codeTitulaire;
    public boolean collecteActivee;
    public boolean multiTitulaire;
}
