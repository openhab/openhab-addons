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
package org.openhab.binding.meteofrance.internal.dto;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MeteoFrance {
    public record VigilanceEnCours(//
            @Nullable Product product, //
            @Nullable Meta meta, //
            int code, // error code, 0 if all went well
            @Nullable String message, // error message when code != 0
            @Nullable String detail) {
    }

    public record Meta(//
            String snapshotId, // identifiant du snapshot de la saisie validée
            ZonedDateTime productDatetime, // date/heure de diffusion du produit
            ZonedDateTime generationTimestamp) { // date/heure de début de validité du produit
    }

    public record Product(//
            String warningType, // "vigilance" - produit de la famille Vigilance
            String typeCdp, // "cdp_textes" - nom générique du produit
            String versionVigilance, // "V6" - Version du système développé
            String versionCdp, // "x.y.z" - version du format pour pouvoir gérer d'anciens jeux de données
            ZonedDateTime updateTime, // "AAAA-MM-JJThh:mm:ssZ" - date/heure de diffusion du produit
            @SerializedName("domain_id") Domain domain, //
            @Nullable List<TextBlocItem> textBlocItems, // contenu textuel des bulletins
            Periods periods) { // périodes concernées par la Vigilance

        public Optional<TextBlocItem> getBlocItem(Domain searched) {
            List<TextBlocItem> local = textBlocItems;
            return local != null ? local.stream().filter(ti -> ti.validFor(searched)).findFirst() : Optional.empty();
        }

        public Optional<Period> getPeriod(Term term) {
            return Optional.ofNullable(periods.get(term));
        }
    }

    public static class Periods extends HashMap<Term, @Nullable Period> {
        private static final long serialVersionUID = -4448877461442293135L;
    }

    public static class Timelaps extends HashMap<Domain, @Nullable DomainId> {
        private static final long serialVersionUID = -441522278695388073L;
    }

    public record TextBlocItem( //
            @SerializedName("domain_id") Domain domain, // code du domaine sur lequel s'applique le bloc textuel
            String blocTitle, //
            String blocId, // indique le type de bloc
            List<BlocItem> blocItems) { // rubriques du bulletin : SITUATION,QUALIFICATION, INCERTITUDE,EVOLUTION

        private boolean validFor(Domain searched) {
            return searched.equals(domain);
        }
    }

    public record Period( //
            Term echeance, //
            ZonedDateTime beginValidityTime, //
            ZonedDateTime endValidityTime, //
            TextItems textItems, //
            Timelaps timelaps, //
            List<PhenomenonCount> maxCountItems, //
            List<PerPhenomenonItem> perPhenomenonItems) {//
    }

    private record TextItems( //
            String title, //
            List<String> text) {//
    }

    public record DomainId( //
            @SerializedName("domain_id") Domain domain, //
            int maxColorId, //
            List<PhenomenonItem> phenomenonItems) { //
    }

    public record PhenomenonItem( //
            Hazard phenomenonId, //
            Risk phenomenonMaxColorId, //
            List<TimelapsItem> timelapsItems) { //
    }

    public record TimelapsItem( //
            ZonedDateTime beginTime, //
            ZonedDateTime endTime, //
            Risk colorId) { //

        public boolean contains(ZonedDateTime moment) {
            return beginTime.isBefore(moment) && endTime.isAfter(moment);
        }
    }

    private record PhenomenonCount(// )
            int colorId, //
            String colorName, //
            int count, //
            String textCount) { //
    }

    private record PerPhenomenonItem( //
            String phenomenonId, //
            int anyColorCount, //
            List<PhenomenonCount> phenomenonCounts) { //
    }

    public record BlocItem( //
            String id, // identifiant de la rubrique texte
            @SerializedName("type_group") BlocType type, // code du groupe de la rubrique
            List<TextItem> textItems) { // textes à associer à la carte comme les commentaires
    }

    public record TextItem( //
            @Nullable String hazardCode, // code du phénomène
            String typeCode, // code de l'élément de suivi
            List<TermItem> termItems) {// tableau des éléments de suivi par échéance : J, J1 ou J+J1

        public Hazard getHazard() {
            return hazardCode == null ? Hazard.ALL : Hazard.valueOf(Objects.requireNonNull(hazardCode));
        }
    }

    public record TermItem(//
            @SerializedName("term_names") Term term, // code de l'échéance
            @SerializedName("risk_code") Risk risk, // code du risque
            ZonedDateTime startTime, // date/heure début d'échéance
            ZonedDateTime endTime, // date/heure fin d'échéance
            List<SubdivisionText> subdivisionText) {// sous_textes de même type d'éléments de suivi

        public String getText(ZonedDateTime moment) {
            return startTime.isBefore(moment) && endTime.isAfter(moment) ? subdivisionText.stream()
                    .map(SubdivisionText::getText).collect(Collectors.joining(System.lineSeparator())) : "";
        }
    }

    private record SubdivisionText( //
            String underlineText, // texte devant être souligné dans le bulletin
            List<String> text) { // contenu du texte ligne par ligne

        String getText() {
            return (underlineText.isEmpty() ? "" : underlineText + System.lineSeparator()) + text.stream()
                    .filter(Predicate.not(String::isEmpty)).collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
