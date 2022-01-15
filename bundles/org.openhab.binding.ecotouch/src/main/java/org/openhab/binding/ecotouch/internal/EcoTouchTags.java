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
package org.openhab.binding.ecotouch.internal;

import static org.openhab.core.library.unit.MetricPrefix.*;
import static org.openhab.core.library.unit.SIUnits.*;
import static org.openhab.core.library.unit.Units.*;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents all valid commands which could be processed by this binding
 *
 * @author Sebastian Held <sebastian.held@gmx.de> - Initial contribution
 * @since 1.5.0
 */
@NonNullByDefault
public enum EcoTouchTags {

    // German: Außentemperatur
    TYPE_TEMPERATURE_OUTSIDE {
        {
            command = "temperature_outside";
            unit = CELSIUS;
            tagName = "A1";
        }
    },

    // German: Außentemperatur gemittelt über 1h
    TYPE_TEMPERATURE_OUTSIDE_1H {
        {
            command = "temperature_outside_1h";
            unit = CELSIUS;
            tagName = "A2";
        }
    },

    // German: Außentemperatur gemittelt über 24h
    TYPE_TEMPERATURE_OUTSIDE_24H {
        {
            command = "temperature_outside_24h";
            unit = CELSIUS;
            tagName = "A3";
        }
    },

    // German: Quelleneintrittstemperatur
    TYPE_TEMPERATURE_SOURCE_IN {
        {
            command = "temperature_source_in";
            unit = CELSIUS;
            tagName = "A4";
        }
    },

    // German: Quellenaustrittstemperatur
    TYPE_TEMPERATURE_SOURCE_OUT {
        {
            command = "temperature_source_out";
            unit = CELSIUS;
            tagName = "A5";
        }
    },

    // German: Verdampfungstemperatur
    TYPE_TEMPERATURE_EVAPORATION {
        {
            command = "temperature_evaporation";
            unit = CELSIUS;
            tagName = "A6";
        }
    },

    // German: Sauggastemperatur
    TYPE_TEMPERATURE_SUCTION {
        {
            command = "temperature_suction";
            unit = CELSIUS;
            tagName = "A7";
        }
    },

    // German: Verdampfungsdruck
    TYPE_PRESSURE_EVAPORATION {
        {
            command = "pressure_evaporation";
            unit = BAR;
            tagName = "A8";
        }
    },

    // German: Temperatur Rücklauf Soll
    TYPE_TEMPERATURE_RETURN_SET {
        {
            command = "temperature_return_set";
            unit = CELSIUS;
            tagName = "A10";
        }
    },

    // German: Temperatur Rücklauf
    TYPE_TEMPERATURE_RETURN {
        {
            command = "temperature_return";
            unit = CELSIUS;
            tagName = "A11";
        }
    },

    // German: Temperatur Vorlauf
    TYPE_TEMPERATURE_FLOW {
        {
            command = "temperature_flow";
            unit = CELSIUS;
            tagName = "A12";
        }
    },

    // German: Kondensationstemperatur
    TYPE_TEMPERATURE_CONDENSATION {
        {
            command = "temperature_condensation";
            unit = CELSIUS;
            tagName = "A14";
        }
    },

    // German: Kondensationsdruck
    TYPE_PRESSURE_CONDENSATION {
        {
            command = "pressure_condensation";
            unit = BAR;
            tagName = "A15";
        }
    },

    // German: Speichertemperatur
    TYPE_TEMPERATURE_STORAGE {
        {
            command = "temperature_storage";
            unit = CELSIUS;
            tagName = "A16";
        }
    },

    // German: Raumtemperatur
    TYPE_TEMPERATURE_ROOM {
        {
            command = "temperature_room";
            unit = CELSIUS;
            tagName = "A17";
        }
    },

    // German: Raumtemperatur gemittelt über 1h
    TYPE_TEMPERATURE_ROOM_1H {
        {
            command = "temperature_room_1h";
            unit = CELSIUS;
            tagName = "A18";
        }
    },

    // German: Warmwassertemperatur
    TYPE_TEMPERATURE_WATER {
        {
            command = "temperature_water";
            unit = CELSIUS;
            tagName = "A19";
        }
    },

    // German: Pooltemperatur
    TYPE_TEMPERATURE_POOL {
        {
            command = "temperature_pool";
            unit = CELSIUS;
            tagName = "A20";
        }
    },

    // German: Solarkollektortemperatur
    TYPE_TEMPERATURE_SOLAR {
        {
            command = "temperature_solar";
            unit = CELSIUS;
            tagName = "A21";
        }
    },

    // German: Solarkreis Vorlauf
    TYPE_TEMPERATURE_SOLAR_FLOW {
        {
            command = "temperature_solar_flow";
            unit = CELSIUS;
            tagName = "A22";
        }
    },

    // German: Ventilöffnung elektrisches Expansionsventil
    TYPE_POSITION_EXPANSION_VALVE {
        {
            command = "position_expansion_valve";
            unit = PERCENT;
            tagName = "A23";
        }
    },

    // German: elektrische Leistung Verdichter
    TYPE_POWER_COMPRESSOR {
        {
            command = "power_compressor";
            unit = KILO(WATT);
            tagName = "A25";
        }
    },

    // German: abgegebene thermische Heizleistung der Wärmepumpe
    TYPE_POWER_HEATING {
        {
            command = "power_heating";
            unit = KILO(WATT);
            tagName = "A26";
        }
    },

    // German: abgegebene thermische KälteLeistung der Wärmepumpe
    TYPE_POWER_COOLING {
        {
            command = "power_cooling";
            unit = KILO(WATT);
            tagName = "A27";
        }
    },

    // German: COP Heizleistung
    TYPE_COP_HEATING {
        {
            command = "cop_heating";
            tagName = "A28";
        }
    },

    // German: COP Kälteleistungleistung
    TYPE_COP_COOLING {
        {
            command = "cop_cooling";
            tagName = "A29";
        }
    },

    // German: Aktuelle Heizkreistemperatur
    TYPE_TEMPERATURE_HEATING {
        {
            command = "temperature_heating_return";
            unit = CELSIUS;
            tagName = "A30";
        }
    },

    // German: Geforderte Temperatur im Heizbetrieb
    TYPE_TEMPERATURE_HEATING_SET {
        {
            command = "temperature_heating_set";
            unit = CELSIUS;
            tagName = "A31";
        }
    },

    // German: Sollwertvorgabe Heizkreistemperatur
    TYPE_TEMPERATURE_HEATING_SET2 {
        {
            command = "temperature_heating_set2";
            unit = CELSIUS;
            tagName = "A32";
        }
    },

    // German: Aktuelle Kühlkreistemperatur
    TYPE_TEMPERATURE_COOLING {
        {
            command = "temperature_cooling_return";
            unit = CELSIUS;
            tagName = "A33";
        }
    },

    // German: Geforderte Temperatur im Kühlbetrieb
    TYPE_TEMPERATURE_COOLING_SET {
        {
            command = "temperature_cooling_set";
            unit = CELSIUS;
            tagName = "A34";
        }
    },

    // German: Sollwertvorgabe Kühlbetrieb
    TYPE_TEMPERATURE_COOLING_SET2 {
        {
            command = "temperature_cooling_set2";
            unit = CELSIUS;
            tagName = "A35";
        }
    },

    // German: Sollwert Warmwassertemperatur
    TYPE_TEMPERATURE_WATER_SET {
        {
            command = "temperature_water_set";
            unit = CELSIUS;
            tagName = "A37";
        }
    },

    // German: Sollwertvorgabe Warmwassertemperatur
    TYPE_TEMPERATURE_WATER_SET2 {
        {
            command = "temperature_water_set2";
            unit = CELSIUS;
            tagName = "A38";
        }
    },

    // German: Sollwert Poolwassertemperatur
    TYPE_TEMPERATURE_POOL_SET {
        {
            command = "temperature_pool_set";
            unit = CELSIUS;
            tagName = "A40";
        }
    },

    // German: Sollwertvorgabe Poolwassertemperatur
    TYPE_TEMPERATURE_POOL_SET2 {
        {
            command = "temperature_pool_set2";
            unit = CELSIUS;
            tagName = "A41";
        }
    },

    // German: geforderte Verdichterleistung
    TYPE_COMPRESSOR_POWER {
        {
            command = "compressor_power";
            unit = PERCENT;
            tagName = "A50";
        }
    },

    // German: % Heizungsumwälzpumpe
    TYPE_PERCENT_HEAT_CIRC_PUMP {
        {
            command = "percent_heat_circ_pump";
            unit = PERCENT;
            tagName = "A51";
        }
    },

    // German: % Quellenpumpe
    TYPE_PERCENT_SOURCE_PUMP {
        {
            command = "percent_source_pump";
            unit = PERCENT;
            tagName = "A52";
        }
    },

    // German: % Leistung Verdichter
    TYPE_PERCENT_COMPRESSOR {
        {
            command = "percent_compressor";
            unit = PERCENT;
            tagName = "A58";
        }
    },

    // German: Hysterese Heizung
    TYPE_HYSTERESIS_HEATING {
        {
            command = "hysteresis_heating";
            unit = CELSIUS;
            tagName = "A61";
        }
    },

    // German: Außentemperatur gemittelt über 1h (scheinbar identisch zu A2)
    TYPE_TEMPERATURE2_OUTSIDE_1H {
        {
            command = "temperature2_outside_1h";
            unit = CELSIUS;
            tagName = "A90";
        }
    },

    // German: Heizkurve - nviNormAussen
    TYPE_NVINORMAUSSEN {
        {
            command = "nviNormAussen";
            unit = CELSIUS;
            tagName = "A91";
        }
    },

    // German: Heizkurve - nviHeizkreisNorm
    TYPE_NVIHEIZKREISNORM {
        {
            command = "nviHeizkreisNorm";
            unit = CELSIUS;
            tagName = "A92";
        }
    },

    // German: Heizkurve - nviTHeizgrenze
    TYPE_NVITHEIZGRENZE {
        {
            command = "nviTHeizgrenze";
            unit = CELSIUS;
            tagName = "A93";
        }
    },

    // German: Heizkurve - nviTHeizgrenzeSoll
    TYPE_NVITHEIZGRENZESOLL {
        {
            command = "nviTHeizgrenzeSoll";
            unit = CELSIUS;
            tagName = "A94";
        }
    },

    // German: undokumentiert: Heizkurve max. VL-Temp (??)
    TYPE_MAX_VL_TEMP {
        {
            command = "maxVLTemp";
            unit = CELSIUS;
            tagName = "A95";
        }
    },

    // German: undokumentiert: Heizkreis Soll-Temp bei 0° Aussen
    TYPE_TEMP_SET_0DEG {
        {
            command = "tempSet0Deg";
            unit = CELSIUS;
            tagName = "A97";
        }
    },

    // German: Raum Soll
    TYPE_TEMP_ROOM_SET {
        {
            command = "tempRoomSet";
            unit = CELSIUS;
            tagName = "A100";
        }
    },

    // German: undokumentiert: Kühlen Einschalt-Temp. Aussentemp (??)
    TYPE_COOLENABLETEMP {
        {
            command = "coolEnableTemp";
            unit = CELSIUS;
            tagName = "A108";
        }
    },

    // German: Heizkurve - nviSollKuehlen
    TYPE_NVITSOLLKUEHLEN {
        {
            command = "nviSollKuehlen";
            unit = CELSIUS;
            tagName = "A109";
        }
    },

    // German: Temperaturveränderung Heizkreis bei PV-Ertrag
    TYPE_TEMPCHANGE_HEATING_PV {
        {
            command = "tempchange_heating_pv";
            unit = CELSIUS;
            tagName = "A682";
        }
    },

    // German: Temperaturveränderung Kühlkreis bei PV-Ertrag
    TYPE_TEMPCHANGE_COOLING_PV {
        {
            command = "tempchange_cooling_pv";
            unit = CELSIUS;
            tagName = "A683";
        }
    },

    // German: Temperaturveränderung Warmwasser bei PV-Ertrag
    TYPE_TEMPCHANGE_WARMWATER_PV {
        {
            command = "tempchange_warmwater_pv";
            unit = CELSIUS;
            tagName = "A684";
        }
    },

    // German: Temperaturveränderung Pool bei PV-Ertrag
    TYPE_TEMPCHANGE_POOL_PV {
        {
            command = "tempchange_pool_pv";
            unit = CELSIUS;
            tagName = "A685";
        }
    },

    // German: undokumentiert: Firmware-Version Regler
    // value 10401 => 01.04.01
    TYPE_VERSION_CONTROLLER {
        {
            command = "version_controller";
            divisor = 1;
            tagName = "I1";
            type = Type.Word;
        }
    },

    // German: undokumentiert: Firmware-Build Regler
    TYPE_VERSION_CONTROLLER_BUILD {
        {
            command = "version_controller_build";
            divisor = 1;
            tagName = "I2";
            type = Type.Word;
        }
    },

    // German: undokumentiert: BIOS-Version
    // value 620 => 06.20
    TYPE_VERSION_BIOS {
        {
            command = "version_bios";
            divisor = 1;
            tagName = "I3";
            type = Type.Word;
        }
    },

    // German: undokumentiert: Datum: Tag
    TYPE_DATE_DAY {
        {
            command = "date_day";
            divisor = 1;
            tagName = "I5";
            type = Type.Word;
        }
    },

    // German: undokumentiert: Datum: Monat
    TYPE_DATE_MONTH {
        {
            command = "date_month";
            divisor = 1;
            tagName = "I6";
            type = Type.Word;
        }
    },

    // German: undokumentiert: Datum: Jahr
    TYPE_DATE_YEAR {
        {
            command = "date_year";
            divisor = 1;
            tagName = "I7";
            type = Type.Word;
        }
    },

    // German: undokumentiert: Uhrzeit: Stunde
    TYPE_TIME_HOUR {
        {
            command = "time_hour";
            divisor = 1;
            tagName = "I8";
            type = Type.Word;
        }
    },

    // German: undokumentiert: Uhrzeit: Minute
    TYPE_TIME_MINUTE {
        {
            command = "time_minute";
            divisor = 1;
            tagName = "I9";
            type = Type.Word;
        }
    },

    // German: Betriebsstunden Verdichter 1
    TYPE_OPERATING_HOURS_COMPRESSOR1 {
        {
            command = "operating_hours_compressor1";
            divisor = 1;
            unit = HOUR;
            tagName = "I10";
            type = Type.Word;
        }
    },

    // German: Betriebsstunden Verdichter 2
    TYPE_OPERATING_HOURS_COMPRESSOR2 {
        {
            command = "operating_hours_compressor2";
            divisor = 1;
            unit = HOUR;
            tagName = "I14";
            type = Type.Word;
        }
    },

    // German: Betriebsstunden Heizungsumwälzpumpe
    TYPE_OPERATING_HOURS_CIRCULATION_PUMP {
        {
            command = "operating_hours_circulation_pump";
            divisor = 1;
            unit = HOUR;
            tagName = "I18";
            type = Type.Word;
        }
    },

    // German: Betriebsstunden Quellenpumpe
    TYPE_OPERATING_HOURS_SOURCE_PUMP {
        {
            command = "operating_hours_source_pump";
            divisor = 1;
            unit = HOUR;
            tagName = "I20";
            type = Type.Word;
        }
    },

    // German: Betriebsstunden Solarkreis
    TYPE_OPERATING_HOURS_SOLAR {
        {
            command = "operating_hours_solar";
            divisor = 1;
            unit = HOUR;
            tagName = "I22";
            type = Type.Word;
        }
    },

    // German: Handabschaltung Heizbetrieb
    TYPE_ENABLE_HEATING {
        {
            command = "enable_heating";
            divisor = 1;
            tagName = "I30";
            type = Type.Word;
        }
    },

    // German: Handabschaltung Kühlbetrieb
    TYPE_ENABLE_COOLING {
        {
            command = "enable_cooling";
            divisor = 1;
            tagName = "I31";
            type = Type.Word;
        }
    },

    // German: Handabschaltung Warmwasserbetrieb
    TYPE_ENABLE_WARMWATER {
        {
            command = "enable_warmwater";
            divisor = 1;
            tagName = "I32";
            type = Type.Word;
        }
    },

    // German: Handabschaltung Pool_Heizbetrieb
    TYPE_ENABLE_POOL {
        {
            command = "enable_pool";
            divisor = 1;
            tagName = "I33";
            type = Type.Word;
        }
    },

    // German: undokumentiert: vermutlich Betriebsmodus PV 0=Aus, 1=Auto, 2=Ein
    TYPE_ENABLE_PV {
        {
            command = "enable_pv";
            divisor = 1;
            tagName = "I41";
            type = Type.Word;
        }
    },

    // German: Status der Wärmepumpenkomponenten
    TYPE_STATE {
        {
            command = "state";
            divisor = 1;
            tagName = "I51";
            type = Type.Word;
        }
    },

    // German: Status der Wärmepumpenkomponenten: Quellenpumpe
    TYPE_STATE_SOURCEPUMP {
        {
            command = "state_sourcepump";
            tagName = "I51";
            type = Type.Bitfield;
            bitnum = 0;
        }
    },

    // German: Status der Wärmepumpenkomponenten: Heizungsumwälzpumpe
    TYPE_STATE_HEATINGPUMP {
        {
            command = "state_heatingpump";
            tagName = "I51";
            type = Type.Bitfield;
            bitnum = 1;
        }
    },

    // German: Status der Wärmepumpenkomponenten: Freigabe Regelung EVD /
    // Magnetventil
    TYPE_STATE_EVD {
        {
            command = "state_evd";
            tagName = "I51";
            type = Type.Bitfield;
            bitnum = 2;
        }
    },

    // German: Status der Wärmepumpenkomponenten: Verdichter 1
    TYPE_STATE_compressor1 {
        {
            command = "state_compressor1";
            tagName = "I51";
            type = Type.Bitfield;
            bitnum = 3;
        }
    },

    // German: Status der Wärmepumpenkomponenten: Verdichter 2
    TYPE_STATE_compressor2 {
        {
            command = "state_compressor2";
            tagName = "I51";
            type = Type.Bitfield;
            bitnum = 4;
        }
    },

    // German: Status der Wärmepumpenkomponenten: externer Wärmeerzeuger
    TYPE_STATE_extheater {
        {
            command = "state_extheater";
            tagName = "I51";
            type = Type.Bitfield;
            bitnum = 5;
        }
    },

    // German: Status der Wärmepumpenkomponenten: Alarmausgang
    TYPE_STATE_alarm {
        {
            command = "state_alarm";
            tagName = "I51";
            type = Type.Bitfield;
            bitnum = 6;
        }
    },

    // German: Status der Wärmepumpenkomponenten: Motorventil Kühlbetrieb
    TYPE_STATE_cooling {
        {
            command = "state_cooling";
            tagName = "I51";
            type = Type.Bitfield;
            bitnum = 7;
        }
    },

    // German: Status der Wärmepumpenkomponenten: Motorventil Warmwasser
    TYPE_STATE_water {
        {
            command = "state_water";
            tagName = "I51";
            type = Type.Bitfield;
            bitnum = 8;
        }
    },

    // German: Status der Wärmepumpenkomponenten: Motorventil Pool
    TYPE_STATE_pool {
        {
            command = "state_pool";
            tagName = "I51";
            type = Type.Bitfield;
            bitnum = 9;
        }
    },

    // German: Status der Wärmepumpenkomponenten: Solarbetrieb
    TYPE_STATE_solar {
        {
            command = "state_solar";
            tagName = "I51";
            type = Type.Bitfield;
            bitnum = 10;
        }
    },

    // German: Status der Wärmepumpenkomponenten: 4-Wegeventil im Kältekreis
    TYPE_STATE_cooling4way {
        {
            command = "state_cooling4way";
            tagName = "I51";
            type = Type.Bitfield;
            bitnum = 11;
        }
    },

    // German: Meldungen von Ausfällen F0xx die zum Wärmepumpenausfall führen
    TYPE_ALARM {
        {
            command = "alarm";
            divisor = 1;
            tagName = "I52";
            type = Type.Word;
        }
    },

    // German: Unterbrechungen
    TYPE_INTERRUPTIONS {
        {
            command = "interruptions";
            divisor = 1;
            tagName = "I53";
            type = Type.Word;
        }
    },

    // German: Serviceebene (0: normal, 1: service)
    TYPE_STATE_SERVICE {
        {
            command = "state_service";
            divisor = 1;
            tagName = "I135";
            type = Type.Word;
        }
    },

    // German: Temperaturanpassung für die Heizung
    TYPE_ADAPT_HEATING {
        {
            command = "adapt_heating";
            divisor = 1;
            tagName = "I263";
            type = Type.Word; // value range 0..8 => -2K .. +2K
        }
    },

    // German: Raumeinfluss
    TYPE_TEMP_ROOM_INFLUENCE {
        {
            command = "tempRoomInfluence";
            divisor = 1;
            tagName = "I264";
            type = Type.Word; // value range 0..4 => 0%, 50%, 100%, 150%, 200%
        }
    },

    // German: Handschaltung Heizungspumpe (H-0-A)
    // H:Handschaltung Ein 0:Aus A:Automatik
    // Kodierung: 0:? 1:? 2:Automatik
    TYPE_MANUAL_HEATINGPUMP {
        {
            command = "manual_heatingpump";
            divisor = 1;
            tagName = "I1270";
            type = Type.Word;
        }
    },

    // German: Handschaltung Quellenpumpe (H-0-A)
    TYPE_MANUAL_SOURCEPUMP {
        {
            command = "manual_sourcepump";
            divisor = 1;
            tagName = "I1281";
            type = Type.Word;
        }
    },

    // German: Handschaltung Solarpumpe 1 (H-0-A)
    TYPE_MANUAL_SOLARPUMP1 {
        {
            command = "manual_solarpump1";
            divisor = 1;
            tagName = "I1287";
            type = Type.Word;
        }
    },

    // German: Handschaltung Solarpumpe 2 (H-0-A)
    TYPE_MANUAL_SOLARPUMP2 {
        {
            command = "manual_solarpump2";
            divisor = 1;
            tagName = "I1289";
            type = Type.Word;
        }
    },

    // German: Handschaltung Speicherladepumpe (H-0-A)
    TYPE_MANUAL_TANKPUMP {
        {
            command = "manual_tankpump";
            divisor = 1;
            tagName = "I1291";
            type = Type.Word;
        }
    },

    // German: Handschaltung Brauchwasserventil (H-0-A)
    TYPE_MANUAL_VALVE {
        {
            command = "manual_valve";
            divisor = 1;
            tagName = "I1293";
            type = Type.Word;
        }
    },

    // German: Handschaltung Poolventil (H-0-A)
    TYPE_MANUAL_POOLVALVE {
        {
            command = "manual_poolvalve";
            divisor = 1;
            tagName = "I1295";
            type = Type.Word;
        }
    },

    // German: Handschaltung Kühlventil (H-0-A)
    TYPE_MANUAL_COOLVALVE {
        {
            command = "manual_coolvalve";
            divisor = 1;
            tagName = "I1297";
            type = Type.Word;
        }
    },

    // German: Handschaltung Vierwegeventil (H-0-A)
    TYPE_MANUAL_4WAYVALVE {
        {
            command = "manual_4wayvalve";
            divisor = 1;
            tagName = "I1299";
            type = Type.Word;
        }
    },

    // German: Handschaltung Multiausgang Ext. (H-0-A)
    TYPE_MANUAL_MULTIEXT {
        {
            command = "manual_multiext";
            divisor = 1;
            tagName = "I1319";
            type = Type.Word;
        }
    },

    // German: Umgebung
    TYPE_TEMPERATURE_SURROUNDING {
        {
            command = "temperature_surrounding";
            unit = CELSIUS;
            tagName = "I2020";
            type = Type.Analog;
            divisor = 100;
        }
    },

    // German: Sauggas
    TYPE_TEMPERATURE_SUCTION_AIR {
        {
            command = "temperature_suction_air";
            unit = CELSIUS;
            tagName = "I2021";
            type = Type.Analog;
            divisor = 100;
        }
    },

    // German: Ölsumpf
    TYPE_TEMPERATURE_SUMP {
        {
            command = "temperature_sump";
            unit = CELSIUS;
            tagName = "I2023";
            type = Type.Analog;
            divisor = 100;
        }
    },

    //
    // The following tags are only available, if an Ecovent System is attached to the Ecotouch
    //

    // German: Abluft
    // Waterkotte reference: airventilation-temperature-1
    TYPE_ECOVENT_TEMP_EXHAUST_AIR {
        {
            command = "ecovent_temp_exhaust_air";
            unit = CELSIUS;
            tagName = "3:HREG400000";
            type = Type.Float;
            divisor = 1;
        }
    },

    // German: Fortluft
    // Waterkotte reference: airventilation-temperature-2
    TYPE_ECOVENT_TEMP_EXIT_AIR {
        {
            command = "ecovent_temp_exit_air";
            unit = CELSIUS;
            tagName = "3:HREG400002";
            type = Type.Float;
            divisor = 1;
        }
    },

    // German: Außenluft
    // Waterkotte reference: airventilation-temperature-3
    TYPE_ECOVENT_TEMP_OUTDOOR_AIR {
        {
            command = "ecovent_temp_outdoor_air";
            unit = CELSIUS;
            tagName = "3:HREG400004";
            type = Type.Float;
            divisor = 1;
        }
    },

    // German: Zuluft
    // Waterkotte reference: airventilation-temperature-4
    TYPE_ECOVENT_TEMP_SUPPLY_AIR {
        {
            command = "ecovent_temp_supply_air";
            unit = CELSIUS;
            tagName = "3:HREG400006";
            type = Type.Float;
            divisor = 1;
        }
    },

    // German: CO2
    // Waterkotte reference: airventilation-co2-value
    TYPE_ECOVENT_CO2_VALUE {
        {
            command = "ecovent_CO2_value";
            unit = PARTS_PER_MILLION;
            tagName = "3:HREG400008";
            type = Type.Float;
            divisor = 1;
        }
    },

    // German: Luftfeuchtigkeit
    // Waterkotte reference: airventilation-air-moisture-value
    TYPE_ECOVENT_MOISTURE_VALUE {
        {
            command = "ecovent_moisture_value";
            unit = PARTS_PER_MILLION;
            tagName = "3:HREG400010";
            type = Type.Float;
            divisor = 1;
        }
    },

    // German: Lüfterdrehzahl
    // Waterkotte reference: airventilation-analog-output-y1
    TYPE_ECOVENT_OUTPUT_Y1 {
        {
            command = "ecovent_output_y1";
            unit = PERCENT;
            tagName = "3:HREG400014";
            type = Type.Float;
            divisor = 1;
        }
    },

    TYPE_ECOVENT_MODE {
        {
            command = "ecovent_mode";
            tagName = "I4582";
            type = Type.Word; // Type.Enum;
            divisor = 1;
            stringEnum = new String[] { "Day Mode", "Night Mode", "Timer Mode", "Party Mode", "Vacation Mode",
                    "Bypass Mode" };
        }
    },

    ;

    /**
     * Represents the heatpump command as it will be used in *.items
     * configuration
     */
    String command = "";
    /**
     * Represents the internal raw heatpump command as it will be used in
     * querying the heat pump
     */
    String tagName = "";

    Unit<?> unit = ONE;

    /**
     * The heatpump always returns 16-bit integers encoded as ASCII. They need
     * to be interpreted according to the context.
     * The EcoVent unit returns floating point numbers.
     */
    public enum Type {
        Analog,
        Word,
        Bitfield,
        Float,
        Enum
    };

    /**
     * The format of the response of the heat pump
     */
    Type type = Type.Analog;

    /**
     * If \c type is Type.Bitfield, this determines the bit number (0-based)
     */
    int bitnum = 0;

    /**
     * If type is anything else than {@link Type#Bitfield} this is used as divisor for the scaled integer.
     * Defaults to 10 and should be a power of 10 (e.g. 10, 100, 1000).
     */
    int divisor = 10;

    /**
     * If \c type is Type.Enum, this defines the meaning of the values (0-based)
     */
    String @Nullable [] stringEnum = null;

    /**
     * @return command name (uses in *.items files)
     */
    public String getCommand() {
        return command;
    }

    /**
     * @return tag name (raw communication with heat pump)
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * @return type: how to interpret the response from the heat pump
     */
    public Type getType() {
        return type;
    }

    /**
     * @return bitnum: if the value is a bit field, this indicates the bit
     *         number (0-based)
     */
    public int getBitNum() {
        return bitnum;
    }

    /**
     * @return Divisor for scaled integer analog values.
     */
    public int getDivisor() {
        return divisor;
    }

    public Unit<?> getUnit() {
        return unit;
    }

    /**
     *
     * @param bindingConfig
     *            command e.g. TYPE_TEMPERATURE_OUTSIDE,..
     * @param itemClass
     *            class to validate
     * @return true if item class can bound to heatpumpCommand
     */
    // public static boolean validateBinding(EcoTouchTags bindingConfig, Class<? extends Item> itemClass) {
    // boolean ret = false;
    // for (EcoTouchTags c : EcoTouchTags.values()) {
    // if (c.getCommand().equals(bindingConfig.getCommand()) && c.getItemClass().equals(itemClass)) {
    // ret = true;
    // break;
    // }
    // }
    // return ret;
    // }

    /**
     * Decode a raw value from the heat pump's ethernet interface into a scaled value
     * 
     * @param rawValue
     * @return
     */
    public BigDecimal decodeValue(String rawValue) {
        BigDecimal raw = new BigDecimal(rawValue);
        if (type == Type.Bitfield) {
            // ignore any scaling from \ref divisor
            int value = raw.intValue();
            if ((value & (1 << bitnum)) != 0) {
                return BigDecimal.ONE;
            } else {
                return BigDecimal.ZERO;
            }
        }
        BigDecimal result = raw.divide(new BigDecimal(divisor));
        return result;
    }

    /**
     * Searches the available heat pump commands and returns the matching one.
     *
     * @param heatpumpCommand
     *            command string e.g. "temperature_outside"
     * @return matching EcoTouchTags instance, if available
     */
    public static @Nullable EcoTouchTags fromString(String heatpumpCommand) {
        if (heatpumpCommand.isEmpty()) {
            return null;
        }
        for (EcoTouchTags c : EcoTouchTags.values()) {
            if (c.getCommand().equals(heatpumpCommand)) {
                return c;
            }
        }

        throw new IllegalArgumentException("cannot find EcoTouch tag for '" + heatpumpCommand + "'");
    }

    /**
     * Searches the available heat pump commands and returns the first matching
     * one.
     *
     * @param tag
     *            raw heatpump tag e.g. "A1"
     * @return first matching EcoTouchTags instance, if available
     */
    public static List<EcoTouchTags> fromTag(String tag) {
        List<EcoTouchTags> result = new LinkedList<EcoTouchTags>();
        for (EcoTouchTags c : EcoTouchTags.values()) {
            if (c.getTagName().equals(tag)) {
                result.add(c);
            }
        }

        return result;
    }
}
