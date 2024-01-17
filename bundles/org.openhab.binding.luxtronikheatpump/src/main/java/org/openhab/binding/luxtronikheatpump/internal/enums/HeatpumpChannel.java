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
package org.openhab.binding.luxtronikheatpump.internal.enums;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.luxtronikheatpump.internal.exceptions.InvalidChannelException;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * Represents all valid channels which could be processed by this binding
 *
 * @author Stefan Giehl - Initial contribution
 */
@NonNullByDefault
public enum HeatpumpChannel {

    // The constant names are currently based on the variable names used in the heat pumps internal JAVA applet
    // for possible values see https://www.loxwiki.eu/display/LOX/Java+Webinterface
    // or https://github.com/Bouni/Home-Assistant-Luxtronik/blob/master/data.txt

    /**
     * Flow temperature heating circuit
     * (original: Vorlauftemperatur Heizkreis)
     */
    CHANNEL_TEMPERATUR_TVL(10, "temperatureHeatingCircuitFlow", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_VORLAUF),

    /**
     * Return temperature heating circuit
     * (original: Rücklauftemperatur Heizkreis)
     */
    CHANNEL_TEMPERATUR_TRL(11, "temperatureHeatingCircuitReturn", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_RUCKLAUF),

    /**
     * Return set point heating circuit
     * (original: Rücklauf-Soll Heizkreis)
     */
    CHANNEL_SOLLWERT_TRL_HZ(12, "temperatureHeatingCircuitReturnTarget", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_RL_SOLL),

    /**
     * Return temperature in buffer tank
     * (original: Rücklauftemperatur im Trennspeicher)
     */
    CHANNEL_TEMPERATUR_TRL_EXT(13, "temperatureBufferTankReturn", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_RUECKLEXT),

    /**
     * Hot gas temperature
     * (original: Heißgastemperatur)
     */
    CHANNEL_TEMPERATUR_THG(14, "temperatureHotGas", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_HEISSGAS),

    /**
     * Outside temperature
     * (original: Außentemperatur)
     */
    CHANNEL_TEMPERATUR_TA(15, "temperatureOutside", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_AUSSENT),

    /**
     * Average temperature outside over 24 h (heating limit function)
     * (original: Durchschnittstemperatur Außen über 24 h (Funktion Heizgrenze))
     */
    CHANNEL_MITTELTEMPERATUR(16, "temperatureOutsideMean", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_AUSSENT),

    /**
     * Hot water actual temperature
     * (original: Warmwasser Ist-Temperatur)
     */
    CHANNEL_TEMPERATUR_TBW(17, "temperatureHotWater", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_BW_IST),

    /**
     * Hot water target temperature
     * (original: Warmwasser Soll-Temperatur)
     */
    // Not needed as it duplicates writable param (2)
    // CHANNEL_EINST_BWS_AKT(18, "temperatureHotWaterTarget", NumberItem.class, SIUnits.CELSIUS, false, null),

    /**
     * Heat source inlet temperature
     * (original: Wärmequellen-Eintrittstemperatur)
     */
    CHANNEL_TEMPERATUR_TWE(19, "temperatureHeatSourceInlet", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_WQ_EIN),

    /**
     * Heat source outlet temperature
     * (original: Wärmequellen-Austrittstemperatur)
     */
    CHANNEL_TEMPERATUR_TWA(20, "temperatureHeatSourceOutlet", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_WQ_EIN),

    /**
     * Mixing circuit 1 Flow temperature
     * (original: Mischkreis 1 Vorlauftemperatur)
     */
    CHANNEL_TEMPERATUR_TFB1(21, "temperatureMixingCircuit1Flow", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_MK1_VORLAUF),

    /**
     * Mixing circuit 1 Flow target temperature
     * (original: Mischkreis 1 Vorlauf-Soll-Temperatur)
     */
    CHANNEL_SOLLWERT_TVL_MK1(22, "temperatureMixingCircuit1FlowTarget", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_MK1VL_SOLL),

    /**
     * Room temperature room station 1
     * (original: Raumtemperatur Raumstation 1)
     */
    CHANNEL_TEMPERATUR_RFV(23, "temperatureRoomStation", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_RAUMSTATION),

    /**
     * Mixing circuit 2 Flow temperature
     * (original: Mischkreis 2 Vorlauftemperatur)
     */
    CHANNEL_TEMPERATUR_TFB2(24, "temperatureMixingCircuit2Flow", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_MK2_VORLAUF),

    /**
     * Mixing circuit 2 Flow target temperature
     * (original: Mischkreis 2 Vorlauf-Soll-Temperatur)
     */
    CHANNEL_SOLLWERT_TVL_MK2(25, "temperatureMixingCircuit2FlowTarget", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_MK2VL_SOLL),

    /**
     * Solar collector sensor
     * (original: Fühler Solarkollektor)
     */
    CHANNEL_TEMPERATUR_TSK(26, "temperatureSolarCollector", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_SOLARKOLL),

    /**
     * Solar tank sensor
     * (original: Fühler Solarspeicher)
     */
    CHANNEL_TEMPERATUR_TSS(27, "temperatureSolarTank", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_SOLARSP),

    /**
     * Sensor external energy source
     * (original: Fühler externe Energiequelle)
     */
    CHANNEL_TEMPERATUR_TEE(28, "temperatureExternalEnergySource", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_EXT_ENERG),

    /**
     * Input "Defrost end, brine pressure, flow rate"
     * (original: Eingang "Abtauende, Soledruck, Durchfluss")
     */
    CHANNEL_ASDIN(29, "inputASD", SwitchItem.class, null, false, HeatpumpVisibility.IN_ASD),

    /**
     * Input "Domestic hot water thermostat"
     * (original: Eingang "Brauchwarmwasserthermostat")
     */
    CHANNEL_BWTIN(30, "inputHotWaterThermostat", SwitchItem.class, null, false, HeatpumpVisibility.IN_BWT),

    /**
     * Input "EVU lock"
     * (original: Eingang "EVU Sperre")
     */
    CHANNEL_EVUIN(31, "inputUtilityLock", SwitchItem.class, null, false, HeatpumpVisibility.IN_EVU),

    /**
     * Input "High pressure cooling circuit"
     * (original: Eingang "Hochdruck Kältekreis")
     */
    CHANNEL_HDIN(32, "inputHighPressureCoolingCircuit", SwitchItem.class, null, false, HeatpumpVisibility.IN_HD),

    /**
     * Input "Motor protection OK"
     * (original: Eingang "Motorschutz OK")
     */
    CHANNEL_MOTIN(33, "inputMotorProtectionOK", SwitchItem.class, null, false, HeatpumpVisibility.IN_MOT),

    /**
     * Input "Low pressure"
     * (original: Eingang "Niederdruck")
     */
    CHANNEL_NDIN(34, "inputLowPressure", SwitchItem.class, null, false, HeatpumpVisibility.IN_ND),

    /**
     * Input "Monitoring contact for potentiostat"
     * (original: Eingang "Überwachungskontakt für Potentiostat")
     */
    CHANNEL_PEXIN(35, "inputPEX", SwitchItem.class, null, false, HeatpumpVisibility.IN_PEX),

    /**
     * Input "Swimming pool thermostat"
     * (original: Eingang "Schwimmbadthermostat")
     */
    CHANNEL_SWTIN(36, "inputSwimmingPoolThermostat", SwitchItem.class, null, false, HeatpumpVisibility.IN_SWT),

    /**
     * Output "Defrost valve"
     * (original: Ausgang "Abtauventil")
     */
    CHANNEL_AVOUT(37, "outputDefrostValve", SwitchItem.class, null, false, HeatpumpVisibility.OUT_ABTAUVENTIL),

    /**
     * Output "Domestic hot water pump/changeover valve"
     * (original: Ausgang "Brauchwasserpumpe/Umstellventil")
     */
    CHANNEL_BUPOUT(38, "outputBUP", SwitchItem.class, null, false, HeatpumpVisibility.OUT_BUP),

    /**
     * Output "Heating circulation pump"
     * (original: Ausgang "Heizungsumwälzpumpe")
     */
    CHANNEL_HUPOUT(39, "outputHeatingCirculationPump", SwitchItem.class, null, false, HeatpumpVisibility.OUT_HUP),

    /**
     * Output "Mixing circuit 1 Open"
     * (original: Ausgang "Mischkreis 1 Auf")
     */
    CHANNEL_MA1OUT(40, "outputMixingCircuit1Open", SwitchItem.class, null, false, HeatpumpVisibility.OUT_MISCHER1AUF),

    /**
     * Output "Mixing circuit 1 Closed"
     * (original: Ausgang "Mischkreis 1 Zu")
     */
    CHANNEL_MZ1OUT(41, "outputMixingCircuit1Closed", SwitchItem.class, null, false, HeatpumpVisibility.OUT_MISCHER1ZU),

    /**
     * Output "Ventilation"
     * (original: Ausgang "Ventilation (Lüftung)")
     */
    CHANNEL_VENOUT(42, "outputVentilation", SwitchItem.class, null, false, HeatpumpVisibility.OUT_VENTILATION),

    /**
     * Output "Brine pump/fan"
     * (original: Ausgang "Solepumpe/Ventilator")
     */
    CHANNEL_VBOOUT(43, "outputVBO", SwitchItem.class, null, false, null),

    /**
     * Output "Compressor 1"
     * (original: Ausgang "Verdichter 1")
     */
    CHANNEL_VD1OUT(44, "outputCompressor1", SwitchItem.class, null, false, HeatpumpVisibility.OUT_VERDICHTER1),

    /**
     * Output "Compressor 2"
     * (original: Ausgang "Verdichter 2")
     */
    CHANNEL_VD2OUT(45, "outputCompressor2", SwitchItem.class, null, false, HeatpumpVisibility.OUT_VERDICHTER2),

    /**
     * Output "Circulation pump"
     * (original: Ausgang "Zirkulationspumpe")
     */
    CHANNEL_ZIPOUT(46, "outputCirculationPump", SwitchItem.class, null, false, HeatpumpVisibility.OUT_ZIP),

    /**
     * Output "Auxiliary circulation pump"
     * (original: Ausgang "Zusatzumwälzpumpe")
     */
    CHANNEL_ZUPOUT(47, "outputZUP", SwitchItem.class, null, false, HeatpumpVisibility.OUT_ZUP),

    /**
     * Output "Control signal additional heating"
     * (original: Ausgang "Steuersignal Zusatzheizung v. Heizung")
     */
    CHANNEL_ZW1OUT(48, "outputControlSignalAdditionalHeating", SwitchItem.class, null, false,
            HeatpumpVisibility.OUT_ZWE1),

    /**
     * Output "Control signal additional heating/fault signal"
     * (original: Ausgang "Steuersignal Zusatzheizung/Störsignal")
     */
    CHANNEL_ZW2SSTOUT(49, "outputFaultSignalAdditionalHeating", SwitchItem.class, null, false,
            HeatpumpVisibility.OUT_ZWE2_SST),

    /**
     * Output "Auxiliary heater 3"
     * (original: Ausgang "Zusatzheizung 3")
     */
    CHANNEL_ZW3SSTOUT(50, "outputAuxiliaryHeater3", SwitchItem.class, null, false, HeatpumpVisibility.OUT_ZWE3),

    /**
     * Output "Pump mixing circuit 2"
     * (original: Ausgang "Pumpe Mischkreis 2")
     */
    CHANNEL_FP2OUT(51, "outputMixingCircuitPump2", SwitchItem.class, null, false, HeatpumpVisibility.OUT_FUP2),

    /**
     * Output "Solar charge pump"
     * (original: Ausgang "Solarladepumpe")
     */
    CHANNEL_SLPOUT(52, "outputSolarChargePump", SwitchItem.class, null, false, HeatpumpVisibility.OUT_SLP),

    /**
     * Output "Swimming pool pump"
     * (original: Ausgang "Schwimmbadpumpe")
     */
    CHANNEL_OUTPUT_SUP(53, "outputSwimmingPoolPump", SwitchItem.class, null, false, HeatpumpVisibility.OUT_SUP),

    /**
     * Output "Mixing circuit 2 Closed"
     * (original: Ausgang "Mischkreis 2 Zu")
     */
    CHANNEL_MZ2OUT(54, "outputMixingCircuit2Closed", SwitchItem.class, null, false, HeatpumpVisibility.OUT_MISCHER2ZU),

    /**
     * Output "Mixing circuit 2 Open"
     * (original: Ausgang "Mischkreis 2 Auf")
     */
    CHANNEL_MA2OUT(55, "outputMixingCircuit2Open", SwitchItem.class, null, false, HeatpumpVisibility.OUT_MISCHER2AUF),

    /**
     * Operating hours compressor 1
     * (original: Betriebsstunden Verdichter 1)
     */
    CHANNEL_ZAEHLER_BETRZEITVD1(56, "runtimeTotalCompressor1", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.BST_BSTDVD1),

    /**
     * Pulses compressor 1
     * (original: Impulse Verdichter 1)
     */
    CHANNEL_ZAEHLER_BETRZEITIMPVD1(57, "pulsesCompressor1", NumberItem.class, null, false,
            HeatpumpVisibility.BST_IMPVD1),

    /**
     * Operating hours compressor 2
     * (original: Betriebsstunden Verdichter 2)
     */
    CHANNEL_ZAEHLER_BETRZEITVD2(58, "runtimeTotalCompressor2", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.BST_BSTDVD2),

    /**
     * Pulses compressor 2
     * (original: Impulse Verdichter 2)
     */
    CHANNEL_ZAEHLER_BETRZEITIMPVD2(59, "pulsesCompressor2", NumberItem.class, null, false,
            HeatpumpVisibility.BST_IMPVD2),

    /**
     * Operating hours Second heat generator 1
     * (original: Betriebsstunden Zweiter Wärmeerzeuger 1)
     */
    CHANNEL_ZAEHLER_BETRZEITZWE1(60, "runtimeTotalSecondHeatGenerator1", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.BST_BSTDZWE1),

    /**
     * Operating hours Second heat generator 2
     * (original: Betriebsstunden Zweiter Wärmeerzeuger 2)
     */
    CHANNEL_ZAEHLER_BETRZEITZWE2(61, "runtimeTotalSecondHeatGenerator2", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.BST_BSTDZWE2),

    /**
     * Operating hours Second heat generator 3
     * (original: Betriebsstunden Zweiter Wärmeerzeuger 3)
     */
    CHANNEL_ZAEHLER_BETRZEITZWE3(62, "runtimeTotalSecondHeatGenerator3", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.BST_BSTDZWE3),

    /**
     * Operating hours heat pump
     * (original: Betriebsstunden Wärmepumpe)
     */
    CHANNEL_ZAEHLER_BETRZEITWP(63, "runtimeTotalHeatPump", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.BST_BSTDWP),

    /**
     * Operating hours heating
     * (original: Betriebsstunden Heizung)
     */
    CHANNEL_ZAEHLER_BETRZEITHZ(64, "runtimeTotalHeating", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.BST_BSTDHZ),

    /**
     * Operating hours hot water
     * (original: Betriebsstunden Warmwasser)
     */
    CHANNEL_ZAEHLER_BETRZEITBW(65, "runtimeTotalHotWater", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.BST_BSTDBW),

    /**
     * Operating hours cooling
     * (original: Betriebsstunden Kühlung)
     */
    CHANNEL_ZAEHLER_BETRZEITKUE(66, "runtimeTotalCooling", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.BST_BSTDKUE),

    /**
     * Heat pump running since
     * (original: Wärmepumpe läuft seit)
     */
    CHANNEL_TIME_WPEIN_AKT(67, "runtimeCurrentHeatPump", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.ABLAUFZ_WP_SEIT),

    /**
     * Second heat generator 1 running since
     * (original: Zweiter Wärmeerzeuger 1 läuft seit)
     */
    CHANNEL_TIME_ZWE1_AKT(68, "runtimeCurrentSecondHeatGenerator1", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.ABLAUFZ_ZWE1_SEIT),

    /**
     * Second heat generator 2 running since
     * (original: Zweiter Wärmeerzeuger 2 läuft seit)
     */
    CHANNEL_TIME_ZWE2_AKT(69, "runtimeCurrentSecondHeatGenerator2", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.ABLAUFZ_ZWE2_SEIT),

    /**
     * Mains on delay
     * (original: Netzeinschaltverzögerung)
     */
    CHANNEL_TIMER_EINSCHVERZ(70, "mainsOnDelay", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.ABLAUFZ_NETZEINV),

    /**
     * Switching cycle lock off
     * (original: Schaltspielsperre Aus)
     */
    CHANNEL_TIME_SSPAUS_AKT(71, "switchingCycleLockOff", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.ABLAUFZ_SSP_ZEIT1),

    /**
     * Switching cycle lock on
     * (original: Schaltspielsperre Ein)
     */
    CHANNEL_TIME_SSPEIN_AKT(72, "switchingCycleLockOn", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.ABLAUFZ_SSP_ZEIT1),

    /**
     * Compressor Idle time
     * (original: Verdichter-Standzeit)
     */
    CHANNEL_TIME_VDSTD_AKT(73, "compressorIdleTime", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.ABLAUFZ_VD_STAND),

    /**
     * Heating controller More time
     * (original: Heizungsregler Mehr-Zeit)
     */
    CHANNEL_TIME_HRM_AKT(74, "heatingControllerMoreTime", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.ABLAUFZ_HRM_ZEIT),

    /**
     * Heating controller Less time
     * (original: Heizungsregler Weniger-Zeit)
     */
    CHANNEL_TIME_HRW_AKT(75, "heatingControllerLessTime", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.ABLAUFZ_HRW_ZEIT),

    /**
     * Thermal disinfection running since
     * (original: Thermische Desinfektion läuft seit)
     */
    CHANNEL_TIME_LGS_AKT(76, "runtimeCurrentThermalDisinfection", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.ABLAUFZ_TDI_SEIT),

    /**
     * Hot water lock
     * (original: Sperre Warmwasser)
     */
    CHANNEL_TIME_SBW_AKT(77, "timeHotWaterLock", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.ABLAUFZ_SPERRE_BW),

    // Channel 78 (Code_WP_akt_2) represents the heatpump type, will be handeled as property

    /**
     * Bivalence stage
     * (original: Bivalenzstufe)
     */
    CHANNEL_BIV_STUFE_AKT(79, "bivalenceStage", NumberItem.class, null, false, null),

    /**
     * Operating status
     * (original: Betriebszustand)
     */
    CHANNEL_WP_BZ_AKT(80, "operatingStatus", NumberItem.class, null, false, null),

    // channel 81 - 90 represents the firmware version, will be handeled as property
    // channel 91 represents the IP address, will be handeled as property
    // channel 92 represents the Subnet mask, will be handeled as property
    // channel 93 represents the Broadcast address, will be handeled as property
    // channel 94 represents the Gateway, will be handeled as property

    /**
     * Timestamp error X in memory
     * (original: Zeitstempel Fehler X im Speicher)
     */
    CHANNEL_HEATPUMP_ERROR_TIME0(95, "errorTime0", DateTimeItem.class, Units.SECOND, false, null),
    CHANNEL_HEATPUMP_ERROR_TIME1(96, "errorTime1", DateTimeItem.class, Units.SECOND, false, null),
    CHANNEL_HEATPUMP_ERROR_TIME2(97, "errorTime2", DateTimeItem.class, Units.SECOND, false, null),
    CHANNEL_HEATPUMP_ERROR_TIME3(98, "errorTime3", DateTimeItem.class, Units.SECOND, false, null),
    CHANNEL_HEATPUMP_ERROR_TIME4(99, "errorTime4", DateTimeItem.class, Units.SECOND, false, null),

    /**
     * Error code Error X in memory
     * (original: Fehlercode Fehler X im Speicher)
     */
    CHANNEL_HEATPUMP_ERROR_NR0(100, "errorCode0", NumberItem.class, null, false, null),
    CHANNEL_HEATPUMP_ERROR_NR1(101, "errorCode1", NumberItem.class, null, false, null),
    CHANNEL_HEATPUMP_ERROR_NR2(102, "errorCode2", NumberItem.class, null, false, null),
    CHANNEL_HEATPUMP_ERROR_NR3(103, "errorCode3", NumberItem.class, null, false, null),
    CHANNEL_HEATPUMP_ERROR_NR4(104, "errorCode4", NumberItem.class, null, false, null),

    /**
     * Number of errors in memory
     * (original: Anzahl der Fehler im Speicher)
     */
    CHANNEL_ANZAHLFEHLERINSPEICHER(105, "errorCountInMemory", NumberItem.class, null, false, null),

    /**
     * Reason shutdown X in memory
     * (original: Grund Abschaltung X im Speicher)
     */
    CHANNEL_HEATPUMP_SWITCHOFF_FILE_NR0(106, "shutdownReason0", NumberItem.class, null, false, null),
    CHANNEL_HEATPUMP_SWITCHOFF_FILE_NR1(107, "shutdownReason1", NumberItem.class, null, false, null),
    CHANNEL_HEATPUMP_SWITCHOFF_FILE_NR2(108, "shutdownReason2", NumberItem.class, null, false, null),
    CHANNEL_HEATPUMP_SWITCHOFF_FILE_NR3(109, "shutdownReason3", NumberItem.class, null, false, null),
    CHANNEL_HEATPUMP_SWITCHOFF_FILE_NR4(110, "shutdownReason4", NumberItem.class, null, false, null),

    /**
     * Timestamp shutdown X in memory
     * (original: Zeitstempel Abschaltung X im Speicher)
     */
    CHANNEL_HEATPUMP_SWITCHOFF_FILE_TIME0(111, "shutdownTime0", DateTimeItem.class, Units.SECOND, false, null),
    CHANNEL_HEATPUMP_SWITCHOFF_FILE_TIME1(112, "shutdownTime1", DateTimeItem.class, Units.SECOND, false, null),
    CHANNEL_HEATPUMP_SWITCHOFF_FILE_TIME2(113, "shutdownTime2", DateTimeItem.class, Units.SECOND, false, null),
    CHANNEL_HEATPUMP_SWITCHOFF_FILE_TIME3(114, "shutdownTime3", DateTimeItem.class, Units.SECOND, false, null),
    CHANNEL_HEATPUMP_SWITCHOFF_FILE_TIME4(115, "shutdownTime4", DateTimeItem.class, Units.SECOND, false, null),

    /**
     * Comfort board installed
     * (original: Comfort Platine installiert)
     */
    CHANNEL_HEATPUMP_COMFORT_EXISTS(116, "comfortBoardInstalled", SwitchItem.class, null, false, null),

    /**
     * Status
     * (original: Status)
     */
    CHANNEL_HEATPUMP_HAUPTMENUSTATUS_ZEILE1(117, "menuStateLine1", NumberItem.class, null, false, null),
    CHANNEL_HEATPUMP_HAUPTMENUSTATUS_ZEILE2(118, "menuStateLine2", NumberItem.class, null, false, null),
    CHANNEL_HEATPUMP_HAUPTMENUSTATUS_ZEILE3(119, "menuStateLine3", NumberItem.class, null, false, null),
    CHANNEL_HEATPUMP_HAUPTMENUSTATUS_ZEIT(120, "menuStateTime", NumberItem.class, Units.SECOND, false, null),

    /**
     * Stage bakeout program
     * (original: Stufe Ausheizprogramm)
     */
    CHANNEL_HAUPTMENUAHP_STUFE(121, "bakeoutProgramStage", NumberItem.class, null, false,
            HeatpumpVisibility.SERVICE_AUSHEIZ),

    /**
     * Temperature bakeout program
     * (original: Temperatur Ausheizprogramm)
     */
    CHANNEL_HAUPTMENUAHP_TEMP(122, "bakeoutProgramTemperature", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.SERVICE_AUSHEIZ),

    /**
     * Runtime bakeout program
     * (original: Laufzeit Ausheizprogramm)
     */
    CHANNEL_HAUPTMENUAHP_ZEIT(123, "bakeoutProgramTime", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.SERVICE_AUSHEIZ),

    /**
     * DHW active/inactive icon
     * (original: Brauchwasser aktiv/inaktiv Symbol)
     */
    CHANNEL_SH_BWW(124, "iconHotWater", SwitchItem.class, null, false, HeatpumpVisibility.BRAUWASSER),

    /**
     * Heater icon
     * (original: Heizung Symbol)
     */
    CHANNEL_SH_HZ(125, "iconHeater", NumberItem.class, null, false, HeatpumpVisibility.HEIZUNG),

    /**
     * Mixing circuit 1 icon
     * (original: Mischkreis 1 Symbol)
     */
    CHANNEL_SH_MK1(126, "iconMixingCircuit1", NumberItem.class, null, false, HeatpumpVisibility.MK1),

    /**
     * Mixing circuit 2 icon
     * (original: Mischkreis 2 Symbol)
     */
    CHANNEL_SH_MK2(127, "iconMixingCircuit2", NumberItem.class, null, false, HeatpumpVisibility.MK2),

    /**
     * Short program setting
     * (original: Einstellung Kurzprogramm)
     */
    CHANNEL_EINST_KURZPROGRAMM(128, "shortProgramSetting", NumberItem.class, null, false, null),

    /**
     * Status Slave X
     * (original: Status Slave X)
     */
    CHANNEL_STATUSSLAVE1(129, "statusSlave1", NumberItem.class, null, false, null),
    CHANNEL_STATUSSLAVE2(130, "statusSlave2", NumberItem.class, null, false, null),
    CHANNEL_STATUSSLAVE3(131, "statusSlave3", NumberItem.class, null, false, null),
    CHANNEL_STATUSSLAVE4(132, "statusSlave4", NumberItem.class, null, false, null),
    CHANNEL_STATUSSLAVE5(133, "statusSlave5", NumberItem.class, null, false, null),

    /**
     * Current time of the heat pump
     * (original: Aktuelle Zeit der Wärmepumpe)
     */
    CHANNEL_AKTUELLETIMESTAMP(134, "currentTimestamp", DateTimeItem.class, Units.SECOND, false,
            HeatpumpVisibility.SERVICE_DATUMUHRZEIT),

    /**
     * Mixing circuit 3 icon
     * (original: Mischkreis 3 Symbol)
     */
    CHANNEL_SH_MK3(135, "iconMixingCircuit3", NumberItem.class, null, false, HeatpumpVisibility.MK3),

    /**
     * Mixing circuit 3 Flow set temperature
     * (original: Mischkreis 3 Vorlauf-Soll-Temperatur)
     */
    CHANNEL_SOLLWERT_TVL_MK3(136, "temperatureMixingCircuit3FlowTarget", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_MK3VL_SOLL),

    /**
     * Mixing circuit 3 Flow temperature
     * (original: Mischkreis 3 Vorlauftemperatur)
     */
    CHANNEL_TEMPERATUR_TFB3(137, "temperatureMixingCircuit3Flow", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_MK3_VORLAUF),

    /**
     * Output "Mixing circuit 3 close"
     * (original: Ausgang "Mischkreis 3 Zu")
     */
    CHANNEL_MZ3OUT(138, "outputMixingCircuit3Close", SwitchItem.class, null, false, HeatpumpVisibility.OUT_MISCHER3ZU),

    /**
     * Output "Mixing circuit 3 open"
     * (original: Ausgang "Mischkreis 3 Auf")
     */
    CHANNEL_MA3OUT(139, "outputMixingCircuit3Open", SwitchItem.class, null, false, HeatpumpVisibility.OUT_MISCHER3AUF),

    /**
     * Pump mixing circuit 3
     * (original: Pumpe Mischkreis 3)
     */
    CHANNEL_FP3OUT(140, "outputMixingCircuitPump3", SwitchItem.class, null, false, HeatpumpVisibility.OUT_FUP3),

    /**
     * Time until defrost
     * (original: Zeit bis Abtauen)
     */
    CHANNEL_TIME_ABTIN(141, "timeUntilDefrost", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.ABLAUFZ_ABTAUIN),

    /**
     * Room temperature room station 2
     * (original: Raumtemperatur Raumstation 2)
     */
    CHANNEL_TEMPERATUR_RFV2(142, "temperatureRoomStation2", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_RAUMSTATION2),

    /**
     * Room temperature room station 3
     * (original: Raumtemperatur Raumstation 3)
     */
    CHANNEL_TEMPERATUR_RFV3(143, "temperatureRoomStation3", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMP_RAUMSTATION3),

    /**
     * Time switch swimming pool icon
     * (original: Schaltuhr Schwimmbad Symbol)
     */
    CHANNEL_SH_SW(144, "iconTimeSwitchSwimmingPool", NumberItem.class, null, false, HeatpumpVisibility.SCHWIMMBAD),

    /**
     * Swimming pool operating hours
     * (original: Betriebsstunden Schwimmbad)
     */
    CHANNEL_ZAEHLER_BETRZEITSW(145, "runtimeTotalSwimmingPool", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.BST_BSTDSW),

    /**
     * Release cooling
     * (original: Freigabe Kühlung)
     */
    CHANNEL_FREIGABKUEHL(146, "coolingRelease", SwitchItem.class, null, false, HeatpumpVisibility.KUHLUNG),

    /**
     * Analog input signal
     * (original: Analoges Eingangssignal)
     */
    CHANNEL_ANALOGIN(147, "inputAnalog", NumberItem.class, Units.VOLT, false, HeatpumpVisibility.IN_ANALOGIN),

    // CHANNEL_SONDERZEICHEN(148, "SonderZeichen", NumberItem.class, null, false, null),

    /**
     * Circulation pumps icon
     * (original: Zirkulationspumpen Symbol)
     */
    CHANNEL_SH_ZIP(149, "iconCirculationPump", NumberItem.class, null, false, null),

    // CHANNEL_WEBSRVPROGRAMMWERTEBEOBARTEN(150, "WebsrvProgrammWerteBeobarten", NumberItem.class, null, false, null),

    /**
     * Heat meter heating
     * (original: Wärmemengenzähler Heizung)
     */
    CHANNEL_WMZ_HEIZUNG(151, "heatMeterHeating", NumberItem.class, Units.KILOWATT_HOUR, false,
            HeatpumpVisibility.HEIZUNG),

    /**
     * Heat meter domestic water
     * (original: Wärmemengenzähler Brauchwasser)
     */
    CHANNEL_WMZ_BRAUCHWASSER(152, "heatMeterHotWater", NumberItem.class, Units.KILOWATT_HOUR, false,
            HeatpumpVisibility.BRAUWASSER),

    /**
     * Heat meter swimming pool
     * (original: Wärmemengenzähler Schwimmbad)
     */
    CHANNEL_WMZ_SCHWIMMBAD(153, "heatMeterSwimmingPool", NumberItem.class, Units.KILOWATT_HOUR, false,
            HeatpumpVisibility.SCHWIMMBAD),

    /**
     * Total heat meter (since reset)
     * (original: Wärmemengenzähler seit Reset)
     */
    CHANNEL_WMZ_SEIT(154, "heatMeterTotalSinceReset", NumberItem.class, Units.KILOWATT_HOUR, false, null),

    /**
     * Heat meter flow rate
     * (original: Wärmemengenzähler Durchfluss)
     */
    CHANNEL_WMZ_DURCHFLUSS(155, "heatMeterFlowRate", NumberItem.class, Units.LITRE_PER_MINUTE, false, null),

    /**
     * Analog output 1
     * (original: Analog Ausgang 1)
     */
    CHANNEL_ANALOGOUT1(156, "outputAnalog1", NumberItem.class, Units.VOLT, false, HeatpumpVisibility.OUT_ANALOG_1),

    /**
     * Analog output 2
     * (original: Analog Ausgang 2)
     */
    CHANNEL_ANALOGOUT2(157, "outputAnalog2", NumberItem.class, Units.VOLT, false, HeatpumpVisibility.OUT_ANALOG_2),

    /**
     * Lock second compressor hot gas
     * (original: Sperre zweiter Verdichter Heissgas)
     */
    CHANNEL_TIME_HEISSGAS(158, "timeLockSecondHotGasCompressor", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.ABLAUFZ_HG_SPERRE),

    /**
     * Supply air temperature
     * (original: Zulufttemperatur)
     */
    CHANNEL_TEMP_LUEFTUNG_ZULUFT(159, "temperatureSupplyAir", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMPERATUR_LUEFTUNG_ZULUFT),

    /**
     * Exhaust air temperature
     * (original: Ablufttemperatur)
     */
    CHANNEL_TEMP_LUEFTUNG_ABLUFT(160, "temperatureExhaustAir", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.TEMPERATUR_LUEFTUNG_ABLUFT),

    /**
     * Operating hours solar
     * (original: Betriebstundenzähler Solar)
     */
    CHANNEL_ZAEHLER_BETRZEITSOLAR(161, "runtimeTotalSolar", NumberItem.class, Units.SECOND, false,
            HeatpumpVisibility.SOLAR),

    /**
     * Analog output 3
     * (original: Analog Ausgang 3)
     */
    CHANNEL_ANALOGOUT3(162, "outputAnalog3", NumberItem.class, Units.VOLT, false, HeatpumpVisibility.OUT_ANALOG_3),

    /**
     * Analog output 4
     * (original: Analog Ausgang 4)
     */
    CHANNEL_ANALOGOUT4(163, "outputAnalog4", NumberItem.class, Units.VOLT, false, HeatpumpVisibility.OUT_ANALOG_4),

    /**
     * Supply air fan (defrost function)
     * (original: Zuluft Ventilator (Abtaufunktion))
     */
    CHANNEL_OUT_VZU(164, "outputSupplyAirFan", NumberItem.class, Units.VOLT, false, HeatpumpVisibility.OUT_VZU),

    /**
     * Exhaust fan
     * (original: Abluft Ventilator)
     */
    CHANNEL_OUT_VAB(165, "outputExhaustFan", NumberItem.class, Units.VOLT, false, HeatpumpVisibility.OUT_VAB),

    /**
     * Output VSK
     * (original: Ausgang VSK)
     */
    CHANNEL_OUT_VSK(166, "outputVSK", SwitchItem.class, null, false, HeatpumpVisibility.OUT_VSK),

    /**
     * Output FRH
     * (original: Ausgang FRH)
     */
    CHANNEL_OUT_FRH(167, "outputFRH", SwitchItem.class, null, false, HeatpumpVisibility.OUT_FRH),

    /**
     * Analog input 2
     * (original: Analog Eingang 2)
     */
    CHANNEL_ANALOGIN2(168, "inputAnalog2", NumberItem.class, Units.VOLT, false, HeatpumpVisibility.IN_ANALOG_2),

    /**
     * Analog input 3
     * (original: Analog Eingang 3)
     */
    CHANNEL_ANALOGIN3(169, "inputAnalog3", NumberItem.class, Units.VOLT, false, HeatpumpVisibility.IN_ANALOG_3),

    /**
     * Input SAX
     * (original: Eingang SAX)
     */
    CHANNEL_SAXIN(170, "inputSAX", SwitchItem.class, null, false, HeatpumpVisibility.IN_SAX),

    /**
     * Input SPL
     * (original: Eingang SPL)
     */
    CHANNEL_SPLIN(171, "inputSPL", SwitchItem.class, null, false, HeatpumpVisibility.IN_SPL),

    /**
     * Ventilation board installed
     * (original: Lüftungsplatine verbaut)
     */
    CHANNEL_COMPACT_EXISTS(172, "ventilationBoardInstalled", SwitchItem.class, null, false, null),

    /**
     * Flow rate heat source
     * (original: Durchfluss Wärmequelle)
     */
    CHANNEL_DURCHFLUSS_WQ(173, "flowRateHeatSource", NumberItem.class, Units.LITRE_PER_MINUTE, false, null),

    /**
     * LIN BUS installed
     * (original: LIN BUS verbaut)
     */
    CHANNEL_LIN_EXISTS(174, "linBusInstalled", SwitchItem.class, null, false, null),

    /**
     * Temperature suction evaporator
     * (original: Temperatur Ansaug Verdampfer)
     */
    CHANNEL_LIN_ANSAUG_VERDAMPFER(175, "temperatureSuctionEvaporator", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.LIN_ANSAUG_VERDAMPFER),

    /**
     * Temperature suction compressor
     * (original: Temperatur Ansaug Verdichter)
     */
    CHANNEL_LIN_ANSAUG_VERDICHTER(176, "temperatureSuctionCompressor", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.LIN_ANSAUG_VERDICHTER),

    /**
     * Temperature compressor heating
     * (original: Temperatur Verdichter Heizung)
     */
    CHANNEL_LIN_VDH(177, "temperatureCompressorHeating", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.LIN_VDH),

    /**
     * Overheating
     * (original: Überhitzung)
     */
    CHANNEL_LIN_UH(178, "temperatureOverheating", NumberItem.class, Units.KELVIN, false, HeatpumpVisibility.LIN_UH),

    /**
     * Overheating target
     * (original: Überhitzung Soll)
     */
    CHANNEL_LIN_UH_SOLL(179, "temperatureOverheatingTarget", NumberItem.class, Units.KELVIN, false,
            HeatpumpVisibility.LIN_UH),

    /**
     * High pressure
     * (original: Hochdruck)
     */
    CHANNEL_LIN_HD(180, "highPressure", NumberItem.class, Units.BAR, false, HeatpumpVisibility.LIN_DRUCK),

    /**
     * Low pressure
     * (original: Niederdruck)
     */
    CHANNEL_LIN_ND(181, "lowPressure", NumberItem.class, Units.BAR, false, HeatpumpVisibility.LIN_DRUCK),

    /**
     * Output compressor heating
     * (original: Ausgang Verdichterheizung)
     */
    CHANNEL_LIN_VDH_OUT(182, "outputCompressorHeating", SwitchItem.class, null, false, null),

    /**
     * Control signal circulating pump
     * (original: Steuersignal Umwälzpumpe)
     */
    CHANNEL_HZIO_PWM(183, "controlSignalCirculatingPump", NumberItem.class, Units.PERCENT, false, null),

    /**
     * Fan speed
     * (original: Ventilator Drehzahl)
     */
    CHANNEL_HZIO_VEN(184, "fanSpeed", NumberItem.class, null, false, null),

    /**
     * EVU 2
     * (original: EVU 2)
     */
    // CHANNEL_HZIO_EVU2(185, "HZIO_EVU2", NumberItem.class, null, false, null),

    /**
     * Safety tempearture limiter floor heating
     * (original: Sicherheits-Tempeartur-Begrenzer Fussbodenheizung)
     */
    CHANNEL_HZIO_STB(186, "temperatureSafetyLimitFloorHeating", SwitchItem.class, null, false, null),

    /**
     * Power target value
     * (original: Leistung Sollwert)
     */
    CHANNEL_SEC_QH_SOLL(187, "powerTargetValue", NumberItem.class, Units.KILOWATT_HOUR, false, null),

    /**
     * Power actual value
     * (original: Leistung Istwert)
     */
    CHANNEL_SEC_QH_IST(188, "powerActualValue", NumberItem.class, Units.KILOWATT_HOUR, false, null),

    /**
     * Temperature flow set point
     * (original: Temperatur Vorlauf Soll)
     */
    CHANNEL_SEC_TVL_SOLL(189, "temperatureFlowTarget", NumberItem.class, SIUnits.CELSIUS, false, null),

    /**
     * Software version SEC Board
     * (original: Software Stand SEC Board)
     */
    // CHANNEL_SEC_SOFTWARE(190, "SEC_Software", NumberItem.class, null, false, null),

    /**
     * SEC Board operating status
     * (original: Betriebszustand SEC Board)
     */
    CHANNEL_SEC_BZ(191, "operatingStatusSECBoard", NumberItem.class, null, false, HeatpumpVisibility.SEC),

    /**
     * Four-way valve
     * (original: Vierwegeventil)
     */
    CHANNEL_SEC_VWV(192, "fourWayValve", NumberItem.class, null, false, HeatpumpVisibility.SEC),

    /**
     * Compressor speed
     * (original: Verdichterdrehzahl)
     */
    CHANNEL_SEC_VD(193, "compressorSpeed", NumberItem.class, null, false, HeatpumpVisibility.SEC),

    /**
     * Compressor temperature EVI (Enhanced Vapour Injection)
     * (original: Verdichtertemperatur EVI)
     */
    CHANNEL_SEC_VERDEVI(194, "temperatureCompressorEVI", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.SEC),

    /**
     * Intake temperature EVI
     * (original: Ansaugtemperatur EVI)
     */
    CHANNEL_SEC_ANSEVI(195, "temperatureIntakeEVI", NumberItem.class, SIUnits.CELSIUS, false, HeatpumpVisibility.SEC),

    /**
     * Overheating EVI
     * (original: Überhitzung EVI)
     */
    CHANNEL_SEC_UEH_EVI(196, "temperatureOverheatingEVI", NumberItem.class, Units.KELVIN, false,
            HeatpumpVisibility.SEC),

    /**
     * Overheating EVI target
     * (original: Überhitzung EVI Sollwert)
     */
    CHANNEL_SEC_UEH_EVI_S(197, "temperatureOverheatingTargetEVI", NumberItem.class, Units.KELVIN, false,
            HeatpumpVisibility.SEC),

    /**
     * Condensation temperature
     * (original: Kondensationstemperatur)
     */
    CHANNEL_SEC_KONDTEMP(198, "temperatureCondensation", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.SEC),

    /**
     * Liquid temperature EEV (electronic expansion valve)
     * (original: Flüssigtemperatur EEV (elektronisches Expansionsventil))
     */
    CHANNEL_SEC_FLUSSIGEX(199, "temperatureLiquidEEV", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.SEC),

    /**
     * Hypothermia EEV
     * (original: Unterkühlung EEV)
     */
    CHANNEL_SEC_UK_EEV(200, "temperatureHypothermiaEEV", NumberItem.class, SIUnits.CELSIUS, false,
            HeatpumpVisibility.SEC),

    /**
     * Pressure EVI
     * (original: Druck EVI)
     */
    CHANNEL_SEC_EVI_DRUCK(201, "pressureEVI", NumberItem.class, Units.BAR, false, HeatpumpVisibility.SEC),

    /**
     * Voltage inverter
     * (original: Spannung Inverter)
     */
    CHANNEL_SEC_U_INV(202, "voltageInverter", NumberItem.class, Units.VOLT, false, HeatpumpVisibility.SEC),

    /**
     * Hot gas temperature sensor 2
     * (original: Temperarturfühler Heissgas 2)
     */
    CHANNEL_TEMPERATUR_THG_2(203, "temperatureHotGas2", NumberItem.class, SIUnits.CELSIUS, false, null),

    /**
     * Temperature sensor heat source inlet 2
     * (original: Temperaturfühler Wärmequelleneintritt 2)
     */
    CHANNEL_TEMPERATUR_TWE_2(204, "temperatureHeatSourceInlet2", NumberItem.class, SIUnits.CELSIUS, false, null),

    /**
     * Intake temperature evaporator 2
     * (original: Ansaugtemperatur Verdampfer 2)
     */
    CHANNEL_LIN_ANSAUG_VERDAMPFER_2(205, "temperatureIntakeEvaporator2", NumberItem.class, SIUnits.CELSIUS, false,
            null),

    /**
     * Intake temperature compressor 2
     * (original: Ansaugtemperatur Verdichter 2)
     */
    CHANNEL_LIN_ANSAUG_VERDICHTER_2(206, "temperatureIntakeCompressor2", NumberItem.class, SIUnits.CELSIUS, false,
            null),

    /**
     * Temperature compressor 2 heating
     * (original: Temperatur Verdichter 2 Heizung)
     */
    CHANNEL_LIN_VDH_2(207, "temperatureCompressor2Heating", NumberItem.class, SIUnits.CELSIUS, false, null),

    /**
     * Overheating 2
     * (original: Überhitzung 2)
     */
    CHANNEL_LIN_UH_2(208, "temperatureOverheating2", NumberItem.class, Units.KELVIN, false, HeatpumpVisibility.LIN_UH),

    /**
     * Overheating target 2
     * (original: Überhitzung Soll 2)
     */
    CHANNEL_LIN_UH_SOLL_2(209, "temperatureOverheatingTarget2", NumberItem.class, Units.KELVIN, false,
            HeatpumpVisibility.LIN_UH),

    /**
     * High pressure 2
     * (original: Hochdruck 2)
     */
    CHANNEL_LIN_HD_2(210, "highPressure2", NumberItem.class, Units.BAR, false, HeatpumpVisibility.LIN_DRUCK),

    /**
     * Low pressure 2
     * (original: Niederdruck 2)
     */
    CHANNEL_LIN_ND_2(211, "lowPressure2", NumberItem.class, Units.BAR, false, HeatpumpVisibility.LIN_DRUCK),

    /**
     * Input pressure switch high pressure 2
     * (original: Eingang Druckschalter Hochdruck 2)
     */
    CHANNEL_HDIN_2(212, "inputSwitchHighPressure2", SwitchItem.class, null, false, HeatpumpVisibility.IN_HD),

    /**
     * Output defrost valve 2
     * (original: Ausgang Abtauventil 2)
     */
    CHANNEL_AVOUT_2(213, "outputDefrostValve2", SwitchItem.class, null, false, HeatpumpVisibility.OUT_ABTAUVENTIL),

    /**
     * Output brine pump/fan 2
     * (original: Ausgang Solepumpe/Ventilator 2)
     */
    CHANNEL_VBOOUT_2(214, "outputVBO2", SwitchItem.class, null, false, null),

    /**
     * Compressor output 1 / 2
     * (original: Ausgang Verdichter 1 / 2)
     */
    CHANNEL_VD1OUT_2(215, "outputCompressor1_2", SwitchItem.class, null, false, null),

    /**
     * Compressor output heating 2
     * (original: Ausgang Verdichter Heizung 2)
     */
    CHANNEL_LIN_VDH_OUT_2(216, "outputCompressorHeating2", SwitchItem.class, null, false, null),

    /**
     * Reason shutdown X in memory 2
     * (original: Grund Abschaltung X im Speicher 2)
     */
    CHANNEL_SWITCHOFF2_FILE_NR0(217, "secondShutdownReason0", NumberItem.class, null, false, null),
    CHANNEL_SWITCHOFF2_FILE_NR1(218, "secondShutdownReason1", NumberItem.class, null, false, null),
    CHANNEL_SWITCHOFF2_FILE_NR2(219, "secondShutdownReason2", NumberItem.class, null, false, null),
    CHANNEL_SWITCHOFF2_FILE_NR3(220, "secondShutdownReason3", NumberItem.class, null, false, null),
    CHANNEL_SWITCHOFF2_FILE_NR4(221, "secondShutdownReason4", NumberItem.class, null, false, null),

    /**
     * Timestamp shutdown X in memory 2
     * (original: Zeitstempel Abschaltung X im Speicher 2)
     */
    CHANNEL_SWITCHOFF2_FILE_TIME0(222, "secondShutdownTime0", DateTimeItem.class, Units.SECOND, false, null),
    CHANNEL_SWITCHOFF2_FILE_TIME1(223, "secondShutdownTime1", DateTimeItem.class, Units.SECOND, false, null),
    CHANNEL_SWITCHOFF2_FILE_TIME2(224, "secondShutdownTime2", DateTimeItem.class, Units.SECOND, false, null),
    CHANNEL_SWITCHOFF2_FILE_TIME3(225, "secondShutdownTime3", DateTimeItem.class, Units.SECOND, false, null),
    CHANNEL_SWITCHOFF2_FILE_TIME4(226, "secondShutdownTime4", DateTimeItem.class, Units.SECOND, false, null),

    /**
     * Room temperature actual value
     * (original: Raumtemperatur Istwert)
     */
    CHANNEL_RBE_RT_IST(227, "temperatureRoom", NumberItem.class, SIUnits.CELSIUS, false, null),

    /**
     * Room temperature set point
     * (original: Raumtemperatur Sollwert)
     */
    CHANNEL_RBE_RT_SOLL(228, "temperatureRoomTarget", NumberItem.class, SIUnits.CELSIUS, false, null),

    /**
     * Temperature domestic water top
     * (original: Temperatur Brauchwasser Oben)
     */
    CHANNEL_TEMPERATUR_BW_OBEN(229, "temperatureHotWaterTop", NumberItem.class, SIUnits.CELSIUS, false, null),

    // DE: Channel 230 (Code_WP_akt_2) represent the heatpump type 2

    /**
     * Compressor frequency
     * (original: Verdichterfrequenz)
     */
    CHANNEL_CODE_FREQ_VD(231, "frequencyCompressor", NumberItem.class, Units.HERTZ, false, null),

    // For Heatpumps with a software version > 3.X the socket serves up to 260 values
    // As those heatpumps do no longer serve a java applet, but use a web ui, that uses
    // a web socket connection instead it's not possible to look up the channels in the
    // code. The following channels are determined based on their values and which value
    // they match on a heat pump

    CHANNEL_232(232, "channel232", NumberItem.class, null, false, null),
    CHANNEL_233(233, "channel233", NumberItem.class, null, false, null),
    CHANNEL_234(234, "channel234", NumberItem.class, null, false, null),
    CHANNEL_235(235, "channel235", NumberItem.class, null, false, null),
    CHANNEL_236(236, "frequencyCompressorTarget", NumberItem.class, Units.HERTZ, false, null),
    CHANNEL_237(237, "channel237", NumberItem.class, null, false, null),
    CHANNEL_238(238, "channel238", NumberItem.class, null, false, null),
    CHANNEL_239(239, "channel239", NumberItem.class, null, false, null),
    CHANNEL_240(240, "channel240", NumberItem.class, null, false, null),
    CHANNEL_241(241, "channel241", NumberItem.class, null, false, null),
    CHANNEL_242(242, "channel242", NumberItem.class, null, false, null),
    CHANNEL_243(243, "channel243", NumberItem.class, null, false, null),
    CHANNEL_244(244, "channel244", NumberItem.class, null, false, null),
    CHANNEL_245(245, "channel245", NumberItem.class, null, false, null),
    CHANNEL_246(246, "channel246", NumberItem.class, null, false, null),
    CHANNEL_247(247, "channel247", NumberItem.class, null, false, null),
    CHANNEL_248(248, "channel248", NumberItem.class, null, false, null),
    CHANNEL_249(249, "channel249", NumberItem.class, null, false, null),
    CHANNEL_250(250, "channel250", NumberItem.class, null, false, null),
    CHANNEL_251(251, "channel251", NumberItem.class, null, false, null),
    CHANNEL_252(252, "channel252", NumberItem.class, null, false, null),
    CHANNEL_253(253, "channel253", NumberItem.class, null, false, null),
    CHANNEL_254(254, "flowRateHeatSource2", NumberItem.class, Units.LITRE_PER_MINUTE, false, null),
    CHANNEL_255(255, "channel255", NumberItem.class, null, false, null),
    CHANNEL_256(256, "channel256", NumberItem.class, null, false, null),
    CHANNEL_257(257, "heatingPowerActualValue", NumberItem.class, Units.WATT, false, null),
    CHANNEL_258(258, "channel258", NumberItem.class, null, false, null),
    CHANNEL_259(259, "channel259", NumberItem.class, null, false, null),
    CHANNEL_260(260, "channel260", NumberItem.class, null, false, null),

    // Changeable Parameters
    // https://www.loxwiki.eu/display/LOX/Java+Webinterface?preview=/13306044/13307658/3003.txt

    /**
     * Heating temperature (parallel shift)
     * (original: Heizung Temperatur (Parallelverschiebung))
     */
    CHANNEL_EINST_WK_AKT(1, "temperatureHeatingParallelShift", NumberItem.class, SIUnits.CELSIUS, true,
            HeatpumpVisibility.HEIZUNG),

    /**
     * Coverage Heat pump (Hot Water)
     * (original: Deckung Wärmepumpe)
     */
    CHANNEL_EINST_BWS_AKT(2, "temperatureHotWaterCoverage", NumberItem.class, SIUnits.CELSIUS, true,
            HeatpumpVisibility.BRAUWASSER),

    /**
     * Heating mode
     * (original: Heizung Betriebsart)
     */
    CHANNEL_BA_HZ_AKT(3, "heatingMode", NumberItem.class, null, true, HeatpumpVisibility.HEIZUNG),

    /**
     * Hot water operating mode
     * (original: Warmwasser Betriebsart)
     */
    CHANNEL_BA_BW_AKT(4, "hotWaterMode", NumberItem.class, null, true, HeatpumpVisibility.BRAUWASSER),

    /**
     * Target heating return temperature if heat pump is set to fixed temperature
     * (will directly set the target return temperature, no automatic changes depending on outside temperature)
     * (original: Rücklauf FestwerteHK)
     */
    CHANNEL_EINST_HZFTRL_AKT(17, "temperatureHeatingFixedReturnTarget", NumberItem.class, SIUnits.CELSIUS, true,
            HeatpumpVisibility.HEIZUNG),

    /**
     * Thermal disinfection (Monday)
     * (original: Thermische Desinfektion (Montag))
     */
    CHANNEL_EINST_BWTDI_AKT_MO(20, "thermalDisinfectionMonday", SwitchItem.class, null, true,
            HeatpumpVisibility.THERMDESINFEKT),
    /**
     * Thermal disinfection (Tuesday)
     * (original: Thermische Desinfektion (Dienstag))
     */
    CHANNEL_EINST_BWTDI_AKT_DI(21, "thermalDisinfectionTuesday", SwitchItem.class, null, true,
            HeatpumpVisibility.THERMDESINFEKT),
    /**
     * Thermal disinfection (Wednesday)
     * (original: Thermische Desinfektion (Mittwoch))
     */
    CHANNEL_EINST_BWTDI_AKT_MI(22, "thermalDisinfectionWednesday", SwitchItem.class, null, true,
            HeatpumpVisibility.THERMDESINFEKT),
    /**
     * Thermal disinfection (Thursday)
     * (original: Thermische Desinfektion (Donnerstag))
     */
    CHANNEL_EINST_BWTDI_AKT_DO(23, "thermalDisinfectionThursday", SwitchItem.class, null, true,
            HeatpumpVisibility.THERMDESINFEKT),
    /**
     * Thermal disinfection (Friday)
     * (original: Thermische Desinfektion (Freitag))
     */
    CHANNEL_EINST_BWTDI_AKT_FR(24, "thermalDisinfectionFriday", SwitchItem.class, null, true,
            HeatpumpVisibility.THERMDESINFEKT),
    /**
     * Thermal disinfection (Saturday)
     * (original: Thermische Desinfektion (Samstag))
     */
    CHANNEL_EINST_BWTDI_AKT_SA(25, "thermalDisinfectionSaturday", SwitchItem.class, null, true,
            HeatpumpVisibility.THERMDESINFEKT),
    /**
     * Thermal disinfection (Sunday)
     * (original: Thermische Desinfektion (Sonntag))
     */
    CHANNEL_EINST_BWTDI_AKT_SO(26, "thermalDisinfectionSunday", SwitchItem.class, null, true,
            HeatpumpVisibility.THERMDESINFEKT),
    /**
     * Thermal disinfection (Permanent)
     * (original: Thermische Desinfektion (Dauerbetrieb))
     */
    CHANNEL_EINST_BWTDI_AKT_AL(27, "thermalDisinfectionPermanent", SwitchItem.class, null, true,
            HeatpumpVisibility.THERMDESINFEKT),

    /**
     * Hot water target temperature
     * (original: Warmwasser Soll Temperatur)
     */
    CHANNEL_SOLL_BWS_AKT(105, "temperatureHotWaterTarget", NumberItem.class, SIUnits.CELSIUS, true,
            HeatpumpVisibility.BRAUWASSER),

    /**
     * Comfort cooling mode
     * (original: Comfort Kühlung Betriebsart)
     */
    CHANNEL_EINST_BWSTYP_AKT(108, "comfortCoolingMode", NumberItem.class, null, true, HeatpumpVisibility.KUHLUNG),

    /**
     * Comfort cooling AT release
     * (original: Comfort Kühlung AT-Freigabe)
     */
    CHANNEL_EINST_KUCFTL_AKT(110, "temperatureComfortCoolingATRelease", NumberItem.class, SIUnits.CELSIUS, true,
            HeatpumpVisibility.KUHLUNG),

    /**
     * Comfort cooling AT release target
     * (original: Comfort Kühlung AT-Freigabe Sollwert)
     */
    CHANNEL_SOLLWERT_KUCFTL_AKT(132, "temperatureComfortCoolingATReleaseTarget", NumberItem.class, SIUnits.CELSIUS,
            true, HeatpumpVisibility.KUHLUNG),

    /**
     * Temperature heating limit
     * (original: Temperatur Heizgrenze)
     */
    CHANNEL_EINST_HEIZGRENZE_TEMP(700, "temperatureHeatingLimit", NumberItem.class, SIUnits.CELSIUS, true,
            HeatpumpVisibility.HEIZUNG),

    /**
     * AT Excess
     * (original: AT-Überschreitung)
     */
    CHANNEL_EINST_KUHL_ZEIT_EIN_AKT(850, "comfortCoolingATExcess", NumberItem.class, Units.HOUR, true,
            HeatpumpVisibility.KUHLUNG),

    /**
     * AT undercut
     * (original: AT-Unterschreitung)
     */
    CHANNEL_EINST_KUHL_ZEIT_AUS_AKT(851, "comfortCoolingATUndercut", NumberItem.class, Units.HOUR, true,
            HeatpumpVisibility.KUHLUNG),

    /**
     * Channel holding complete (localized) status message
     */
    CHANNEL_HEATPUMP_STATUS(null, "menuStateFull", StringItem.class, null, false, null);

    private @Nullable Integer channelId;
    private String command;
    private Class<? extends Item> itemClass;
    private @Nullable Unit<?> unit;
    private boolean isParameter;
    private @Nullable HeatpumpVisibility requiredVisibility;

    private HeatpumpChannel(@Nullable Integer channelId, String command, Class<? extends Item> itemClass,
            @Nullable Unit<?> unit, boolean isParameter, @Nullable HeatpumpVisibility requiredVisibility) {
        this.channelId = channelId;
        this.command = command;
        this.itemClass = itemClass;
        this.unit = unit;
        this.isParameter = isParameter;
        this.requiredVisibility = requiredVisibility;
    }

    public @Nullable Integer getChannelId() {
        return channelId;
    }

    public String getCommand() {
        return command;
    }

    public Class<? extends Item> getItemClass() {
        return itemClass;
    }

    public @Nullable Unit<?> getUnit() {
        return unit;
    }

    public boolean isWritable() {
        return isParameter == Boolean.TRUE;
    }

    protected @Nullable HeatpumpVisibility getVisibility() {
        return requiredVisibility;
    }

    public boolean isVisible(Integer[] visibilityValues) {
        HeatpumpVisibility visiblity = getVisibility();

        if (visiblity == null) {
            return true;
        }

        int code = visiblity.getCode();

        return (visibilityValues.length < code || visibilityValues[code] == 1);
    }

    public static HeatpumpChannel fromString(String heatpumpCommand) throws InvalidChannelException {
        for (HeatpumpChannel c : HeatpumpChannel.values()) {

            if (c.getCommand().equals(heatpumpCommand)) {
                return c;
            }
        }

        throw new InvalidChannelException("cannot find LuxtronikHeatpump channel for '" + heatpumpCommand + "'");
    }

    @Override
    public String toString() {
        return getCommand();
    }
}
