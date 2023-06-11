/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class containing the mappings for all message status codes.
 * 
 * chapter "2 List of all codes" in Technical Interface Description for external Developers
 *
 * @see <a href="https://developers.deutschebahn.com/db-api-marketplace/apis/product/timetables">DB API Marketplace</a>
 *
 * @author Sönke Küper - initial contribution
 */
@NonNullByDefault
public final class MessageCodes {

    private static final Map<Integer, String> CODES = new HashMap<>();
    static {
        CODES.put(0, "keine Verspätungsbegründung");
        CODES.put(2, "Polizeiliche Ermittlung");
        CODES.put(3, "Feuerwehreinsatz an der Strecke");
        CODES.put(4, "kurzfristiger Personalausfall");
        CODES.put(5, "ärztliche Versorgung eines Fahrgastes");
        CODES.put(6, "Betätigen der Notbremse");
        CODES.put(7, "Personen im Gleis");
        CODES.put(8, "Notarzteinsatz am Gleis");
        CODES.put(9, "Streikauswirkungen");
        CODES.put(10, "Tiere im Gleis");
        CODES.put(11, "Unwetter");
        CODES.put(12, "Warten auf ein verspätetes Schiff");
        CODES.put(13, "Pass- und Zollkontrolle");
        CODES.put(14, "Technische Störung am Bahnhof");
        CODES.put(15, "Beeinträchtigung durch Vandalismus");
        CODES.put(16, "Entschärfung einer Fliegerbombe");
        CODES.put(17, "Beschädigung einer Brücke");
        CODES.put(18, "umgestürzter Baum im Gleis");
        CODES.put(19, "Unfall an einem Bahnübergang");
        CODES.put(20, "Tiere im Gleis");
        CODES.put(21, "Warten auf Fahrgäste aus einem anderen Zug");
        CODES.put(22, "Witterungsbedingte Störung");
        CODES.put(23, "Feuerwehreinsatz auf Bahngelände");
        CODES.put(24, "Verspätung im Ausland");
        CODES.put(25, "Warten auf weitere Wagen");
        CODES.put(28, "Gegenstände im Gleis");
        CODES.put(29, "Ersatzverkehr mit Bus ist eingerichtet");
        CODES.put(31, "Bauarbeiten");
        CODES.put(32, "Verzögerung beim Ein-/Ausstieg");
        CODES.put(33, "Oberleitungsstörung");
        CODES.put(34, "Signalstörung");
        CODES.put(35, "Streckensperrung");
        CODES.put(36, "technische Störung am Zug");
        CODES.put(38, "technische Störung an der Strecke");
        CODES.put(39, "Anhängen von zusätzlichen Wagen");
        CODES.put(40, "Stellwerksstörung /-ausfall");
        CODES.put(41, "Störung an einem Bahnübergang");
        CODES.put(42, "außerplanmäßige Geschwindigkeitsbeschränkung");
        CODES.put(43, "Verspätung eines vorausfahrenden Zuges");
        CODES.put(44, "Warten auf einen entgegenkommenden Zug");
        CODES.put(45, "Überholung");
        CODES.put(46, "Warten auf freie Einfahrt");
        CODES.put(47, "verspätete Bereitstellung des Zuges");
        CODES.put(48, "Verspätung aus vorheriger Fahrt");
        CODES.put(55, "technische Störung an einem anderen Zug");
        CODES.put(56, "Warten auf Fahrgäste aus einem Bus");
        CODES.put(57, "Zusätzlicher Halt zum Ein-/Ausstieg für Reisende");
        CODES.put(58, "Umleitung des Zuges");
        CODES.put(59, "Schnee und Eis");
        CODES.put(60, "Reduzierte Geschwindigkeit wegen Sturm");
        CODES.put(61, "Türstörung");
        CODES.put(62, "behobene technische Störung am Zug");
        CODES.put(63, "technische Untersuchung am Zug");
        CODES.put(64, "Weichenstörung");
        CODES.put(65, "Erdrutsch");
        CODES.put(66, "Hochwasser");
        CODES.put(70, "WLAN im gesamten Zug nicht verfügbar");
        CODES.put(71, "WLAN in einem/mehreren Wagen nicht verfügbar");
        CODES.put(72, "Info-/Entertainment nicht verfügbar");
        CODES.put(73, "Heute: Mehrzweckabteil vorne");
        CODES.put(74, "Heute: Mehrzweckabteil hinten");
        CODES.put(75, "Heute: 1. Klasse vorne");
        CODES.put(76, "Heute: 1. Klasse hinten");
        CODES.put(77, "ohne 1. Klasse");
        CODES.put(79, "ohne Mehrzweckabteil");
        CODES.put(80, "andere Reihenfolge der Wagen");
        CODES.put(82, "mehrere Wagen fehlen");
        CODES.put(83, "Störung fahrzeuggebundene Einstiegshilfe");
        CODES.put(84, "Zug verkehrt richtig gereiht");
        CODES.put(85, "ein Wagen fehlt");
        CODES.put(86, "gesamter Zug ohne Reservierung");
        CODES.put(87, "einzelne Wagen ohne Reservierung");
        CODES.put(88, "keine Qualitätsmängel");
        CODES.put(89, "Reservierungen sind wieder vorhanden");
        CODES.put(90, "kein gastronomisches Angebot");
        CODES.put(91, "fehlende Fahrradbeförderung");
        CODES.put(92, "Eingeschränkte Fahrradbeförderung");
        CODES.put(93, "keine behindertengerechte Einrichtung");
        CODES.put(94, "Ersatzbewirtschaftung");
        CODES.put(95, "Ohne behindertengerechtes WC");
        CODES.put(96, "Überbesetzung mit Kulanzleistungen");
        CODES.put(97, "Überbesetzung ohne Kulanzleistungen");
        CODES.put(98, "sonstige Qualitätsmängel");
        CODES.put(99, "Verzögerungen im Betriebsablauf");
    }

    private MessageCodes() {
    }

    /**
     * Returns the message for the given code or emtpy string if not present.
     */
    public static String getMessage(final int code) {
        final String message = CODES.get(code);
        if (message == null) {
            return "";
        } else {
            return message;
        }
    }
}
