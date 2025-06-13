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
package org.openhab.binding.linky.internal.dto;

import java.util.ArrayList;

/**
 * The {@link PrmDetail} holds detailed informations about prm configuration
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class PrmDetail {
    public record Adresse(String ligne2, String ligne3, String ligne4, String ligne5, String ligne6) {

    }

    public record DicEntry(String code, String libelle) {
    }

    public record Measure(String unite, String valeur) {
    }

    public record AlimentationPrincipale(Object puissanceRaccordementInjection,
            Measure puissanceRaccordementSoutirage) {
    }

    public record Compteur(boolean accessibilite, boolean ticActivee, boolean ticStandard) {
    }

    public record Contrat(DicEntry typeContrat, String referenceContrat) {
    }

    public record Disjoncteur(DicEntry calibre) {
    }

    public record DispositifComptage(DicEntry typeComptage) {
    }

    public record GrilleFournisseur(DicEntry calendrier, Object classeTemporelle) {
    }

    public record InformationsContractuelles(Contrat contrat, DicEntry etatContractuel, SiContractuel siContractuel) {
    }

    public record SiContractuel(DicEntry application) {
    }

    public record SituationAlimentationDto(AlimentationPrincipale alimentationPrincipale) {
    }

    public record SituationComptageDto(ArrayList<Compteur> compteurs, Disjoncteur disjoncteur,
            DispositifComptage dispositifComptage) {
    }

    public record SituationContractuelleDto(InformationsContractuelles informationsContractuelles,
            StructureTarifaire structureTarifaire, String fournisseur, DicEntry segment) {
    }

    public record StructureTarifaire(Measure puissanceSouscrite, GrilleFournisseur grilleFournisseur) {
    }

    public record SyntheseContractuelleDto(DicEntry niveauOuvertureServices) {
    }

    public Adresse adresse;
    public String segment;
    public SyntheseContractuelleDto syntheseContractuelleDto;
    public SituationContractuelleDto[] situationContractuelleDtos;
    public SituationAlimentationDto situationAlimentationDto;
    public SituationComptageDto situationComptageDto;
}
