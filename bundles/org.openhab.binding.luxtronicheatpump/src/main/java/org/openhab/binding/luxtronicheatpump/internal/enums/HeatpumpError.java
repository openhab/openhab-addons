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
 * 
 * @author Stefan Giehl - Initial contribution
 */
package org.openhab.binding.luxtronicheatpump.internal.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum HeatpumpError {
    ERROR_701(701, "Niederdruckstoerung - Bitte Inst. rufen"),
    ERROR_702(702, "Niederdrucksperre - RESET automatisch"),
    ERROR_703(703, "Frostschutz - Bitte Inst. rufen"),
    ERROR_704(704, "Heissgasstoerung - Reset in hh:mm"),
    ERROR_705(705, "Motorschutz VEN - Bitte Inst. rufen"),
    ERROR_706(706, "Motorschutz BSUP - Bitte Inst. rufen"),
    ERROR_707(707, "Codierung WP - Bitte Inst. rufen"),
    ERROR_708(708, "Fuehler Ruecklauf - Bitte Inst. rufen"),
    ERROR_709(709, "Fuehler Vorlauf - Bitte Inst. rufen"),
    ERROR_710(710, "Fuehler Heissgas - Bitte Inst. rufen"),
    ERROR_711(711, "Fuehler Aussentemp. - Bitte Inst. rufen"),
    ERROR_712(712, "Fuehler Warmwasser - Bitte Inst. rufen"),
    ERROR_713(713, "Fuehler WQ-Ein - Bitte Inst. rufen"),
    ERROR_714(714, "Heissgas WW - Reset in hh:mm"),
    ERROR_715(715, "Hochdruck-Abschalt. - RESET automatisch"),
    ERROR_716(716, "Hochdruckstoerung - Bitte Inst rufen"),
    ERROR_717(717, "Durchfluss-WQ - Bitte Inst. rufen"),
    ERROR_718(718, "Max. Aussentemp. - RESET automatisch"),
    ERROR_719(719, "Min. Aussentemp. - RESET automatisch"),
    ERROR_720(720, "WQ-Temperatur - RESET automatisch in hh:mm"),
    ERROR_721(721, "Niederdruckabschaltung - RESET automatisch"),
    ERROR_722(722, "Tempdiff Heizwasser - Bitte Inst. rufen"),
    ERROR_723(723, "Tempdiff Warmwasser - Bitte Inst. rufen"),
    ERROR_724(724, "Tempdiff Abtauen - Bitte Inst. rufen"),
    ERROR_725(725, "Anlagefehler WW - Bitte Inst. rufen"),
    ERROR_726(726, "Fuehler Mischkreis 1 - Bitte Inst. rufen"),
    ERROR_727(727, "Soledruck - Bitte Inst. rufen"),
    ERROR_728(728, "Fuehler WQ-Aus - Bitte Inst. rufen"),
    ERROR_729(729, "Drehfeldfehler - Bitte Inst. rufen"),
    ERROR_730(730, "Leistung Ausheizen - Bitte Inst. rufen"),
    ERROR_732(732, "Stoerung Kuehlung - Bitte Inst. rufen"),
    ERROR_733(733, "Stoerung Anode - Bitte Inst. rufen"),
    ERROR_734(734, "Stoerung Anode - Bitte Inst. rufen"),
    ERROR_735(735, "Fuehler Ext. Energiequelle - Bitte Inst. rufen"),
    ERROR_736(736, "Fuehler Solarkollektor - Bitte Inst. rufen"),
    ERROR_737(737, "Fuehler Solarspeicher - Bitte Inst. rufen"),
    ERROR_738(738, "Fuehler Mischkreis2 - Bitte Inst. rufen"),
    ERROR_750(750, "Fuehler Ruecklauf extern - Bitte Inst. rufen"),
    ERROR_751(751, "Phasenueberwachungsfehler"),
    ERROR_752(752, "Phasenueberwachungs / Durchflussfehler"),
    ERROR_755(755, "Verbindung zu Slave verloren - Bitte Inst. rufen"),
    ERROR_756(756, "Verbindung zu Master verloren - Bitte Inst. rufen"),
    ERROR_757(757, "ND-Stoerung bei WW-Geraet"),
    ERROR_758(758, "Stoerung Abtauung"),
    ERROR_759(759, "Meldung TDI"),
    ERROR_760(760, "Stoerung Abtauung"),
    ERROR_761(761, "LIN-Verbindung unterbrochen"),
    ERROR_762(762, "Fuehler Ansaug-Verdichter"),
    ERROR_763(763, "Fuehler Ansaug-Verdampfer"),
    ERROR_764(764, "Fuehler Verdichterheizung"),
    ERROR_765(765, "Ueberhitzung"),
    ERROR_766(766, "Einsatzgrenzen-VD"),
    ERROR_767(767, "STB E-Stab"),
    ERROR_770(770, "Niedrige Ueberhitzung"),
    ERROR_771(771, "Hohe Ueberhitzung"),
    ERROR_776(776, "Einsatzgrenzen-VD"),
    ERROR_777(777, "Expansionsventil"),
    ERROR_778(778, "Fuehler Niederdruck"),
    ERROR_779(779, "Fuehler Hochdruck"),
    ERROR_780(780, "Fuehler EVI"),
    ERROR_781(781, "Fuehler Fluessig, vor Ex-Ventil"),
    ERROR_782(782, "Fuehler EVI Sauggas"),
    ERROR_783(783, "Kommunikation SEC-Inverter"),
    ERROR_784(784, "VSS gesperrt"),
    ERROR_785(785, "SEC-Board defekt"),
    ERROR_786(786, "Kommunikation SEC-Inverter"),
    ERROR_787(787, "VD Alarm"),
    ERROR_788(788, "Schwerw. Inverter Fehler"),
    ERROR_789(789, "LIN/Kodierung nicht vorhanden"),
    ERROR_790(790, "Schwerw. Inverter Fehler"),
    ERROR_791(791, "ModBus Verbindung verloren"),
    ERROR_792(792, "LIN-Verbindung unterbrochen"),
    ERROR_793(793, "Schwerw. Inverter Fehler"),
    ERROR_UNKNOWN(-1, "Unbekannter Fehler");

    private final String name;
    private final Integer code;
    private static final Logger logger = LoggerFactory.getLogger(HeatpumpState.class);

    private HeatpumpError(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static final HeatpumpError fromCode(Integer code) {
        for (HeatpumpError error : HeatpumpError.values()) {
            if (error.code.equals(code)) {
                return error;
            }
        }

        logger.info("Unknown heatpump error code {}", code);
        return ERROR_UNKNOWN;
    }

    @Override
    public String toString() {
        return code + ": " + name;
    }
}
