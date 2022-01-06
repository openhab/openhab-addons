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
 * @see https://developer.deutschebahn.com/store/apis/info?name=Timetables&version=v1&provider=DBOpenData&#tab1
 *
 * @author Sönke Küper - initial contribution
 */
@NonNullByDefault
public final class MessageCodes {

    private static Map<Integer, String> codes = new HashMap<>();
    static {
        codes.put(0, "keine Verspätungsbegründung");
        codes.put(2, "Polizeiliche Ermittlung");
        codes.put(3, "Feuerwehreinsatz an der Strecke");
        codes.put(4, "kurzfristiger Personalausfall");
        codes.put(5, "ärztliche Versorgung eines Fahrgastes");
        codes.put(6, "Betätigen der Notbremse");
        codes.put(7, "Personen im Gleis");
        codes.put(8, "Notarzteinsatz am Gleis");
        codes.put(9, "Streikauswirkungen");
        codes.put(10, "Tiere im Gleis");
        codes.put(11, "Unwetter");
        codes.put(12, "Warten auf ein verspätetes Schiff");
        codes.put(13, "Pass- und Zollkontrolle");
        codes.put(14, "Technische Störung am Bahnhof");
        codes.put(15, "Beeinträchtigung durch Vandalismus");
        codes.put(16, "Entschärfung einer Fliegerbombe");
        codes.put(17, "Beschädigung einer Brücke");
        codes.put(18, "umgestürzter Baum im Gleis");
        codes.put(19, "Unfall an einem Bahnübergang");
        codes.put(20, "Tiere im Gleis");
        codes.put(21, "Warten auf Fahrgäste aus einem anderen Zug");
        codes.put(22, "Witterungsbedingte Störung");
        codes.put(23, "Feuerwehreinsatz auf Bahngelände");
        codes.put(24, "Verspätung im Ausland");
        codes.put(25, "Warten auf weitere Wagen");
        codes.put(28, "Gegenstände im Gleis");
        codes.put(29, "Ersatzverkehr mit Bus ist eingerichtet");
        codes.put(31, "Bauarbeiten");
        codes.put(32, "Verzögerung beim Ein-/Ausstieg");
        codes.put(33, "Oberleitungsstörung");
        codes.put(34, "Signalstörung");
        codes.put(35, "Streckensperrung");
        codes.put(36, "technische Störung am Zug");
        codes.put(38, "technische Störung an der Strecke");
        codes.put(39, "Anhängen von zusätzlichen Wagen");
        codes.put(40, "Stellwerksstörung /-ausfall");
        codes.put(41, "Störung an einem Bahnübergang");
        codes.put(42, "außerplanmäßige Geschwindigkeitsbeschränkung");
        codes.put(43, "Verspätung eines vorausfahrenden Zuges");
        codes.put(44, "Warten auf einen entgegenkommenden Zug");
        codes.put(45, "Überholung");
        codes.put(46, "Warten auf freie Einfahrt");
        codes.put(47, "verspätete Bereitstellung des Zuges");
        codes.put(48, "Verspätung aus vorheriger Fahrt");
        codes.put(55, "technische Störung an einem anderen Zug");
        codes.put(56, "Warten auf Fahrgäste aus einem Bus");
        codes.put(57, "Zusätzlicher Halt zum Ein-/Ausstieg für Reisende");
        codes.put(58, "Umleitung des Zuges");
        codes.put(59, "Schnee und Eis");
        codes.put(60, "Reduzierte Geschwindigkeit wegen Sturm");
        codes.put(61, "Türstörung");
        codes.put(62, "behobene technische Störung am Zug");
        codes.put(63, "technische Untersuchung am Zug");
        codes.put(64, "Weichenstörung");
        codes.put(65, "Erdrutsch");
        codes.put(66, "Hochwasser");
        codes.put(70, "WLAN im gesamten Zug nicht verfügbar");
        codes.put(71, "WLAN in einem/mehreren Wagen nicht verfügbar");
        codes.put(72, "Info-/Entertainment nicht verfügbar");
        codes.put(73, "Heute: Mehrzweckabteil vorne");
        codes.put(74, "Heute: Mehrzweckabteil hinten");
        codes.put(75, "Heute: 1. Klasse vorne");
        codes.put(76, "Heute: 1. Klasse hinten");
        codes.put(77, "ohne 1. Klasse");
        codes.put(79, "ohne Mehrzweckabteil");
        codes.put(80, "andere Reihenfolge der Wagen");
        codes.put(82, "mehrere Wagen fehlen");
        codes.put(83, "Störung fahrzeuggebundene Einstiegshilfe");
        codes.put(84, "Zug verkehrt richtig gereiht");
        codes.put(85, "ein Wagen fehlt");
        codes.put(86, "gesamter Zug ohne Reservierung");
        codes.put(87, "einzelne Wagen ohne Reservierung");
        codes.put(88, "keine Qualitätsmängel");
        codes.put(89, "Reservierungen sind wieder vorhanden");
        codes.put(90, "kein gastronomisches Angebot");
        codes.put(91, "fehlende Fahrradbeförderung");
        codes.put(92, "Eingeschränkte Fahrradbeförderung");
        codes.put(93, "keine behindertengerechte Einrichtung");
        codes.put(94, "Ersatzbewirtschaftung");
        codes.put(95, "Ohne behindertengerechtes WC");
        codes.put(96, "Überbesetzung mit Kulanzleistungen");
        codes.put(97, "Überbesetzung ohne Kulanzleistungen");
        codes.put(98, "sonstige Qualitätsmängel");
        codes.put(99, "Verzögerungen im Betriebsablauf");
    }

    private MessageCodes() {
    }

    /**
     * Returns the message for the given code or emtpy string if not present.
     */
    public static String getMessage(final int code) {
        final String message = codes.get(code);
        if (message == null) {
            return "";
        } else {
            return message;
        }
    }
}
