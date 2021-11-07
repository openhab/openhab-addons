/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.guntamatic.internal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GuntamaticBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Weger Michael - Initial contribution
 */
@NonNullByDefault
public class GuntamaticBindingConstants {

    private static final String BINDING_ID = "guntamatic";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GUNTAMATIC = new ThingTypeUID(BINDING_ID, "guntamatic");

    // List of all Channel ids
    public static final String CHANNEL_Betrieb = "Betrieb";
    public static final String CHANNEL_Aussentemperatur = "Aussentemperatur";
    public static final String CHANNEL_Kesselsolltemp = "Kesselsolltemp";
    public static final String CHANNEL_Kesseltemperatur = "Kesseltemperatur";
    public static final String CHANNEL_Rauchgasauslastung = "Rauchgasauslastung";
    public static final String CHANNEL_Leistung = "Leistung";
    public static final String CHANNEL_Ruecklauftemp = "Ruecklauftemp";
    public static final String CHANNEL_CO2Soll = "CO2Soll";
    public static final String CHANNEL_CO2Gehalt = "CO2Gehalt";
    public static final String CHANNEL_RuecklauftempSoll = "RuecklauftempSoll";
    public static final String CHANNEL_Betriebscode = "Betriebscode";
    public static final String CHANNEL_Wirkungsgrad = "Wirkungsgrad";
    public static final String CHANNEL_Saugzuggeblaese = "Saugzuggeblaese";
    public static final String CHANNEL_Austragungsgeblaese = "Austragungsgeblaese";
    public static final String CHANNEL_Austragmotor = "Austragmotor";
    public static final String CHANNEL_G1soll = "G1soll";
    public static final String CHANNEL_Pufferoben = "Pufferoben";
    public static final String CHANNEL_Puffermitte = "Puffermitte";
    public static final String CHANNEL_Pufferunten = "Pufferunten";
    public static final String CHANNEL_PumpeHP0 = "PumpeHP0";
    public static final String CHANNEL_Warmwasser0 = "Warmwasser0";
    public static final String CHANNEL_PWarmwasser0 = "PWarmwasser0";
    public static final String CHANNEL_Warmwasser1 = "Warmwasser1";
    public static final String CHANNEL_PWarmwasser1 = "PWarmwasser1";
    public static final String CHANNEL_Warmwasser2 = "Warmwasser2";
    public static final String CHANNEL_PWarmwasser2 = "PWarmwasser2";
    public static final String CHANNEL_RaumtempHK0 = "RaumtempHK0";
    public static final String CHANNEL_Heizkreis0 = "Heizkreis0";
    public static final String CHANNEL_RaumtempHK1 = "RaumtempHK1";
    public static final String CHANNEL_VorlaufSoll1 = "VorlaufSoll1";
    public static final String CHANNEL_VorlaufIst1 = "VorlaufIst1";
    public static final String CHANNEL_Mischer1 = "Mischer1";
    public static final String CHANNEL_Heizkreis1 = "Heizkreis1";
    public static final String CHANNEL_RaumtempHK2 = "RaumtempHK2";
    public static final String CHANNEL_VorlaufSoll2 = "VorlaufSoll2";
    public static final String CHANNEL_VorlaufIst2 = "VorlaufIst2";
    public static final String CHANNEL_Mischer2 = "Mischer2";
    public static final String CHANNEL_Heizkreis2 = "Heizkreis2";
    public static final String CHANNEL_RaumtempHK3 = "RaumtempHK3";
    public static final String CHANNEL_Heizkreis3 = "Heizkreis3";
    public static final String CHANNEL_RaumtempHK4 = "RaumtempHK4";
    public static final String CHANNEL_VorlaufSoll4 = "VorlaufSoll4";
    public static final String CHANNEL_VorlaufIst4 = "VorlaufIst4";
    public static final String CHANNEL_Mischer4 = "Mischer4";
    public static final String CHANNEL_Heizkreis4 = "Heizkreis4";
    public static final String CHANNEL_RaumtempHK5 = "RaumtempHK5";
    public static final String CHANNEL_VorlaufSoll5 = "VorlaufSoll5";
    public static final String CHANNEL_VorlaufIst5 = "VorlaufIst5";
    public static final String CHANNEL_Mischer5 = "Mischer5";
    public static final String CHANNEL_Heizkreis5 = "Heizkreis5";
    public static final String CHANNEL_RaumtempHK6 = "RaumtempHK6";
    public static final String CHANNEL_Heizkreis6 = "Heizkreis6";
    public static final String CHANNEL_RaumtempHK7 = "RaumtempHK7";
    public static final String CHANNEL_VorlaufSoll7 = "VorlaufSoll7";
    public static final String CHANNEL_VorlaufIst7 = "VorlaufIst7";
    public static final String CHANNEL_Mischer7 = "Mischer7";
    public static final String CHANNEL_Heizkreis7 = "Heizkreis7";
    public static final String CHANNEL_RaumtempHK8 = "RaumtempHK8";
    public static final String CHANNEL_VorlaufSoll8 = "VorlaufSoll8";
    public static final String CHANNEL_VorlaufIst8 = "VorlaufIst8";
    public static final String CHANNEL_Mischer8 = "Mischer8";
    public static final String CHANNEL_Heizkreis8 = "Heizkreis8";
    public static final String CHANNEL_Fuellstand = "Fuellstand";
    public static final String CHANNEL_STB = "STB";
    public static final String CHANNEL_TKS = "TKS";
    public static final String CHANNEL_Kesselfreigabe = "Kesselfreigabe";
    public static final String CHANNEL_Programm = "Programm";
    public static final String CHANNEL_ProgammHK0 = "ProgammHK0";
    public static final String CHANNEL_ProgammHK1 = "ProgammHK1";
    public static final String CHANNEL_ProgammHK2 = "ProgammHK2";
    public static final String CHANNEL_ProgammHK3 = "ProgammHK3";
    public static final String CHANNEL_ProgammHK4 = "ProgammHK4";
    public static final String CHANNEL_ProgammHK5 = "ProgammHK5";
    public static final String CHANNEL_ProgammHK6 = "ProgammHK6";
    public static final String CHANNEL_ProgammHK7 = "ProgammHK7";
    public static final String CHANNEL_ProgammHK8 = "ProgammHK8";
    public static final String CHANNEL_Stoerung0 = "Stoerung0";
    public static final String CHANNEL_Stoerung1 = "Stoerung1";
    public static final String CHANNEL_Serial = "Serial";
    public static final String CHANNEL_Version = "Version";
    public static final String CHANNEL_Betriebszeit = "Betriebszeit";
    public static final String CHANNEL_Servicezeit = "Servicezeit";
    public static final String CHANNEL_Ascheleerenin = "Ascheleerenin";
    public static final String CHANNEL_VorlaufIst0 = "VorlaufIst0";
    public static final String CHANNEL_VorlaufIst3 = "VorlaufIst3";
    public static final String CHANNEL_VorlaufIst6 = "VorlaufIst6";
    public static final String CHANNEL_Brennstoffzaehler = "Brennstoffzaehler";
    public static final String CHANNEL_Pufferladung = "Pufferladung";
    public static final String CHANNEL_Pufferoben0 = "Pufferoben0";
    public static final String CHANNEL_Pufferunten0 = "Pufferunten0";
    public static final String CHANNEL_Pufferoben1 = "Pufferoben1";
    public static final String CHANNEL_Pufferunten1 = "Pufferunten1";
    public static final String CHANNEL_Pufferoben2 = "Pufferoben2";
    public static final String CHANNEL_Pufferunten2 = "Pufferunten2";
    public static final String CHANNEL_PZusatzwarmw0 = "PZusatzwarmw0";
    public static final String CHANNEL_PZusatzwarmw1 = "PZusatzwarmw1";
    public static final String CHANNEL_PZusatzwarmw2 = "PZusatzwarmw2";
    public static final String CHANNEL_Fernpumpe0 = "Fernpumpe0";
    public static final String CHANNEL_Fernpumpe1 = "Fernpumpe1";
    public static final String CHANNEL_Fernpumpe2 = "Fernpumpe2";
    public static final String CHANNEL_KesselzustandNr = "KesselzustandNr";
    public static final String CHANNEL_PufferT5 = "PufferT5";
    public static final String CHANNEL_PufferT6 = "PufferT6";
    public static final String CHANNEL_PufferT7 = "PufferT7";
    public static final String CHANNEL_Zusatzwarmw0 = "Zusatzwarmw0";
    public static final String CHANNEL_Zusatzwarmw1 = "Zusatzwarmw1";
    public static final String CHANNEL_Zusatzwarmw2 = "Zusatzwarmw2";
    public static final String CHANNEL_Rost = "Rost";

    // List of all ChannelType ids
    public static final String CHANNEL_TYPE_Betrieb = "Betrieb";
    public static final String CHANNEL_TYPE_Aussentemperatur = "Aussentemperatur";
    public static final String CHANNEL_TYPE_Kesselsolltemp = "Kesselsolltemp";
    public static final String CHANNEL_TYPE_Kesseltemperatur = "Kesseltemperatur";
    public static final String CHANNEL_TYPE_Rauchgasauslastung = "Rauchgasauslastung";
    public static final String CHANNEL_TYPE_Leistung = "Leistung";
    public static final String CHANNEL_TYPE_Ruecklauftemp = "Ruecklauftemp";
    public static final String CHANNEL_TYPE_CO2Soll = "CO2Soll";
    public static final String CHANNEL_TYPE_CO2Gehalt = "CO2Gehalt";
    public static final String CHANNEL_TYPE_RuecklauftempSoll = "RuecklauftempSoll";
    public static final String CHANNEL_TYPE_Betriebscode = "Betriebscode";
    public static final String CHANNEL_TYPE_Wirkungsgrad = "Wirkungsgrad";
    public static final String CHANNEL_TYPE_Saugzuggeblaese = "Saugzuggeblaese";
    public static final String CHANNEL_TYPE_Austragungsgeblaese = "Austragungsgeblaese";
    public static final String CHANNEL_TYPE_Austragmotor = "Austragmotor";
    public static final String CHANNEL_TYPE_G1soll = "G1soll";
    public static final String CHANNEL_TYPE_Pufferoben = "Pufferoben";
    public static final String CHANNEL_TYPE_Puffermitte = "Puffermitte";
    public static final String CHANNEL_TYPE_Pufferunten = "Pufferunten";
    public static final String CHANNEL_TYPE_PumpeHP0 = "PumpeHP0";
    public static final String CHANNEL_TYPE_Warmwasser0 = "Warmwasser0";
    public static final String CHANNEL_TYPE_PWarmwasser0 = "PWarmwasser0";
    public static final String CHANNEL_TYPE_Warmwasser1 = "Warmwasser1";
    public static final String CHANNEL_TYPE_PWarmwasser1 = "PWarmwasser1";
    public static final String CHANNEL_TYPE_Warmwasser2 = "Warmwasser2";
    public static final String CHANNEL_TYPE_PWarmwasser2 = "PWarmwasser2";
    public static final String CHANNEL_TYPE_RaumtempHK0 = "RaumtempHK0";
    public static final String CHANNEL_TYPE_Heizkreis0 = "Heizkreis0";
    public static final String CHANNEL_TYPE_RaumtempHK1 = "RaumtempHK1";
    public static final String CHANNEL_TYPE_VorlaufSoll1 = "VorlaufSoll1";
    public static final String CHANNEL_TYPE_VorlaufIst1 = "VorlaufIst1";
    public static final String CHANNEL_TYPE_Mischer1 = "Mischer1";
    public static final String CHANNEL_TYPE_Heizkreis1 = "Heizkreis1";
    public static final String CHANNEL_TYPE_RaumtempHK2 = "RaumtempHK2";
    public static final String CHANNEL_TYPE_VorlaufSoll2 = "VorlaufSoll2";
    public static final String CHANNEL_TYPE_VorlaufIst2 = "VorlaufIst2";
    public static final String CHANNEL_TYPE_Mischer2 = "Mischer2";
    public static final String CHANNEL_TYPE_Heizkreis2 = "Heizkreis2";
    public static final String CHANNEL_TYPE_RaumtempHK3 = "RaumtempHK3";
    public static final String CHANNEL_TYPE_Heizkreis3 = "Heizkreis3";
    public static final String CHANNEL_TYPE_RaumtempHK4 = "RaumtempHK4";
    public static final String CHANNEL_TYPE_VorlaufSoll4 = "VorlaufSoll4";
    public static final String CHANNEL_TYPE_VorlaufIst4 = "VorlaufIst4";
    public static final String CHANNEL_TYPE_Mischer4 = "Mischer4";
    public static final String CHANNEL_TYPE_Heizkreis4 = "Heizkreis4";
    public static final String CHANNEL_TYPE_RaumtempHK5 = "RaumtempHK5";
    public static final String CHANNEL_TYPE_VorlaufSoll5 = "VorlaufSoll5";
    public static final String CHANNEL_TYPE_VorlaufIst5 = "VorlaufIst5";
    public static final String CHANNEL_TYPE_Mischer5 = "Mischer5";
    public static final String CHANNEL_TYPE_Heizkreis5 = "Heizkreis5";
    public static final String CHANNEL_TYPE_RaumtempHK6 = "RaumtempHK6";
    public static final String CHANNEL_TYPE_Heizkreis6 = "Heizkreis6";
    public static final String CHANNEL_TYPE_RaumtempHK7 = "RaumtempHK7";
    public static final String CHANNEL_TYPE_VorlaufSoll7 = "VorlaufSoll7";
    public static final String CHANNEL_TYPE_VorlaufIst7 = "VorlaufIst7";
    public static final String CHANNEL_TYPE_Mischer7 = "Mischer7";
    public static final String CHANNEL_TYPE_Heizkreis7 = "Heizkreis7";
    public static final String CHANNEL_TYPE_RaumtempHK8 = "RaumtempHK8";
    public static final String CHANNEL_TYPE_VorlaufSoll8 = "VorlaufSoll8";
    public static final String CHANNEL_TYPE_VorlaufIst8 = "VorlaufIst8";
    public static final String CHANNEL_TYPE_Mischer8 = "Mischer8";
    public static final String CHANNEL_TYPE_Heizkreis8 = "Heizkreis8";
    public static final String CHANNEL_TYPE_Fuellstand = "Fuellstand";
    public static final String CHANNEL_TYPE_STB = "STB";
    public static final String CHANNEL_TYPE_TKS = "TKS";
    public static final String CHANNEL_TYPE_Kesselfreigabe = "Kesselfreigabe";
    public static final String CHANNEL_TYPE_Programm = "Programm";
    public static final String CHANNEL_TYPE_ProgammHK0 = "ProgammHK0";
    public static final String CHANNEL_TYPE_ProgammHK1 = "ProgammHK1";
    public static final String CHANNEL_TYPE_ProgammHK2 = "ProgammHK2";
    public static final String CHANNEL_TYPE_ProgammHK3 = "ProgammHK3";
    public static final String CHANNEL_TYPE_ProgammHK4 = "ProgammHK4";
    public static final String CHANNEL_TYPE_ProgammHK5 = "ProgammHK5";
    public static final String CHANNEL_TYPE_ProgammHK6 = "ProgammHK6";
    public static final String CHANNEL_TYPE_ProgammHK7 = "ProgammHK7";
    public static final String CHANNEL_TYPE_ProgammHK8 = "ProgammHK8";
    public static final String CHANNEL_TYPE_Stoerung0 = "Stoerung0";
    public static final String CHANNEL_TYPE_Stoerung1 = "Stoerung1";
    public static final String CHANNEL_TYPE_Serial = "Serial";
    public static final String CHANNEL_TYPE_Version = "Version";
    public static final String CHANNEL_TYPE_Betriebszeit = "Betriebszeit";
    public static final String CHANNEL_TYPE_Servicezeit = "Servicezeit";
    public static final String CHANNEL_TYPE_Ascheleerenin = "Ascheleerenin";
    public static final String CHANNEL_TYPE_VorlaufIst0 = "VorlaufIst0";
    public static final String CHANNEL_TYPE_VorlaufIst3 = "VorlaufIst3";
    public static final String CHANNEL_TYPE_VorlaufIst6 = "VorlaufIst6";
    public static final String CHANNEL_TYPE_Brennstoffzaehler = "Brennstoffzaehler";
    public static final String CHANNEL_TYPE_Pufferladung = "Pufferladung";
    public static final String CHANNEL_TYPE_Pufferoben0 = "Pufferoben0";
    public static final String CHANNEL_TYPE_Pufferunten0 = "Pufferunten0";
    public static final String CHANNEL_TYPE_Pufferoben1 = "Pufferoben1";
    public static final String CHANNEL_TYPE_Pufferunten1 = "Pufferunten1";
    public static final String CHANNEL_TYPE_Pufferoben2 = "Pufferoben2";
    public static final String CHANNEL_TYPE_Pufferunten2 = "Pufferunten2";
    public static final String CHANNEL_TYPE_PZusatzwarmw0 = "PZusatzwarmw0";
    public static final String CHANNEL_TYPE_PZusatzwarmw1 = "PZusatzwarmw1";
    public static final String CHANNEL_TYPE_PZusatzwarmw2 = "PZusatzwarmw2";
    public static final String CHANNEL_TYPE_Fernpumpe0 = "Fernpumpe0";
    public static final String CHANNEL_TYPE_Fernpumpe1 = "Fernpumpe1";
    public static final String CHANNEL_TYPE_Fernpumpe2 = "Fernpumpe2";
    public static final String CHANNEL_TYPE_KesselzustandNr = "KesselzustandNr";
    public static final String CHANNEL_TYPE_PufferT5 = "PufferT5";
    public static final String CHANNEL_TYPE_PufferT6 = "PufferT6";
    public static final String CHANNEL_TYPE_PufferT7 = "PufferT7";
    public static final String CHANNEL_TYPE_Zusatzwarmw0 = "Zusatzwarmw0";
    public static final String CHANNEL_TYPE_Zusatzwarmw1 = "Zusatzwarmw1";
    public static final String CHANNEL_TYPE_Zusatzwarmw2 = "Zusatzwarmw2";
    public static final String CHANNEL_TYPE_Rost = "Rost";

    public static final String DAQDATA_URL = "/daqdata.cgi";
    public static final String DAQDESC_URL = "/daqdesc.cgi";

    public static final List<String> CHANNELS = Arrays.asList(CHANNEL_Betrieb, CHANNEL_Aussentemperatur,
            CHANNEL_Kesselsolltemp, CHANNEL_Kesseltemperatur, CHANNEL_Rauchgasauslastung, CHANNEL_Leistung,
            CHANNEL_Ruecklauftemp, CHANNEL_CO2Soll, CHANNEL_CO2Gehalt, CHANNEL_RuecklauftempSoll, CHANNEL_Betriebscode,
            CHANNEL_Wirkungsgrad, CHANNEL_Saugzuggeblaese, CHANNEL_Austragungsgeblaese, CHANNEL_Austragmotor,
            CHANNEL_G1soll, CHANNEL_Pufferoben, CHANNEL_Puffermitte, CHANNEL_Pufferunten, CHANNEL_PumpeHP0,
            CHANNEL_Warmwasser0, CHANNEL_PWarmwasser0, CHANNEL_Warmwasser1, CHANNEL_PWarmwasser1, CHANNEL_Warmwasser2,
            CHANNEL_PWarmwasser2, CHANNEL_RaumtempHK0, CHANNEL_Heizkreis0, CHANNEL_RaumtempHK1, CHANNEL_VorlaufSoll1,
            CHANNEL_VorlaufIst1, CHANNEL_Mischer1, CHANNEL_Heizkreis1, CHANNEL_RaumtempHK2, CHANNEL_VorlaufSoll2,
            CHANNEL_VorlaufIst2, CHANNEL_Mischer2, CHANNEL_Heizkreis2, CHANNEL_RaumtempHK3, CHANNEL_Heizkreis3,
            CHANNEL_RaumtempHK4, CHANNEL_VorlaufSoll4, CHANNEL_VorlaufIst4, CHANNEL_Mischer4, CHANNEL_Heizkreis4,
            CHANNEL_RaumtempHK5, CHANNEL_VorlaufSoll5, CHANNEL_VorlaufIst5, CHANNEL_Mischer5, CHANNEL_Heizkreis5,
            CHANNEL_RaumtempHK6, CHANNEL_Heizkreis6, CHANNEL_RaumtempHK7, CHANNEL_VorlaufSoll7, CHANNEL_VorlaufIst7,
            CHANNEL_Mischer7, CHANNEL_Heizkreis7, CHANNEL_RaumtempHK8, CHANNEL_VorlaufSoll8, CHANNEL_VorlaufIst8,
            CHANNEL_Mischer8, CHANNEL_Heizkreis8, CHANNEL_Fuellstand, CHANNEL_STB, CHANNEL_TKS, CHANNEL_Kesselfreigabe,
            CHANNEL_Programm, CHANNEL_ProgammHK0, CHANNEL_ProgammHK1, CHANNEL_ProgammHK2, CHANNEL_ProgammHK3,
            CHANNEL_ProgammHK4, CHANNEL_ProgammHK5, CHANNEL_ProgammHK6, CHANNEL_ProgammHK7, CHANNEL_ProgammHK8,
            CHANNEL_Stoerung0, CHANNEL_Stoerung1, CHANNEL_Serial, CHANNEL_Version, CHANNEL_Betriebszeit,
            CHANNEL_Servicezeit, CHANNEL_Ascheleerenin, CHANNEL_VorlaufIst0, CHANNEL_VorlaufIst3, CHANNEL_VorlaufIst6,
            CHANNEL_Brennstoffzaehler, CHANNEL_Pufferladung, CHANNEL_Pufferoben0, CHANNEL_Pufferunten0,
            CHANNEL_Pufferoben1, CHANNEL_Pufferunten1, CHANNEL_Pufferoben2, CHANNEL_Pufferunten2, CHANNEL_PZusatzwarmw0,
            CHANNEL_PZusatzwarmw1, CHANNEL_PZusatzwarmw2, CHANNEL_Fernpumpe0, CHANNEL_Fernpumpe1, CHANNEL_Fernpumpe2,
            CHANNEL_KesselzustandNr, CHANNEL_PufferT5, CHANNEL_PufferT6, CHANNEL_PufferT7, CHANNEL_Zusatzwarmw0,
            CHANNEL_Zusatzwarmw1, CHANNEL_Zusatzwarmw2, CHANNEL_Rost

    );
}
