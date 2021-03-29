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
package org.openhab.binding.solarmax.internal.connector;

/**
 * The {@link SolarMaxCommandKey} enum defines the commands that are understood by the SolarMax device
 *
 * @author Jamie Townsend - Initial contribution
 */
public enum SolarMaxCommandKey {

    // Valid commands which returned a non-null value during testing
    // DeviceAddress("ADR"), // device number - only used if the devices are linked serially
    // UNKNOWN_AMM("AMM"), //
    BuildNumber("BDN"), //
    Startups("CAC"), //
    // UNKNOWN_CID("CID"), //
    // UNKNOWN_CPG("CPG"), //
    // UNKNOWN_CPL("CPL"), //
    // UNKNOWN_CP1("CP1"), //
    // UNKNOWN_CP2("CP2"), //
    // UNKNOWN_CP3("CP3"), //
    // UNKNOWN_CP4("CP4"), //
    // UNKNOWN_CP5("CP5"), //
    // UNKNOWN_CYC("CYC"), //
    // UNKNOWN_DIN("DIN"), //
    // UNKNOWN_DMO("DMO"), //
    // UNKNOWN_ETH("ETH"), //
    // UNKNOWN_FH2("FH2"), //
    // UNKNOWN_FQR("FQR"), //
    // UNKNOWN_FWV("FWV"), //
    // UNKNOWN_IAA("IAA"), //
    // UNKNOWN_IED("IED"), //
    // UNKNOWN_IEE("IEE"), //
    // UNKNOWN_IEM("IEM"), //
    // UNKNOWN_ILM("ILM"), //
    AcPhase1Current("IL1"), //
    AcPhase2Current("IL2"), //
    AcPhase3Current("IL3"), //
    // UNKNOWN_IP4("IP4"), //
    // UNKNOWN_ISL("ISL"), //
    // UNKNOWN_ITS("ITS"), //
    EnergyGeneratedToday("KDY"), //
    // UNKNOWN_KFS("KFS"), //
    OperatingHours("KHR"), //
    // UNKNOWN_KHS("KHS"), //
    EnergyGeneratedYesterday("KLD"), //
    EnergyGeneratedLastMonth("KLM"), //
    EnergyGeneratedLastYear("KLY"), //
    EnergyGeneratedThisMonth("KMT"), //
    // UNKNOWN_KTS("KTS"), //
    EnergyGeneratedTotal("KT0"), //
    EnergyGeneratedThisYear("KYR"), //
    // Language("LAN"), //
    // MacAddress("MAC"), //
    CurrentPowerGenerated("PAC"), //
    // UNKNOWN_PAE("PAE"), //
    // UNKNOWN_PAM("PAM"), //
    // UNKNOWN_PDA("PDA"), //
    // UNKNOWN_PDC("PDC"), //
    // UNKNOWN_PFA("PFA"), //
    // PowerInstalled("PIN"), //
    // UNKNOWN_PLR("PLR"), //
    // UNKNOWN_PPC("PPC"), //
    // AcPowerPercent("PRL"), //
    // UNKNOWN_PSF("PSF"), //
    // UNKNOWN_PSR("PSR"), //
    // UNKNOWN_PSS("PSS"), //
    // UNKNOWN_QAC("QAC"), //
    // UNKNOWN_QMO("QMO"), //
    // UNKNOWN_QUC("QUC"), //
    // UNKNOWN_RA1("RA1"), //
    // UNKNOWN_RA2("RA2"), //
    // UNKNOWN_RB1("RB1"), //
    // UNKNOWN_RB2("RB2"), //
    // UNKNOWN_REL("REL"), //
    // UNKNOWN_RH1("RH1"), //
    // UNKNOWN_RH2("RH2"), //
    // UNKNOWN_RPR("RPR"), //
    // UNKNOWN_RSD("RSD"), //
    // UNKNOWN_SAC("SAC"), //
    // UNKNOWN_SAL("SAL"), //
    // UNKNOWN_SAM("SAM"), //
    // UNKNOWN_SCH("SCH"), //
    // UNKNOWN_SNM("SNM"), // IP Broadcast Address??
    // UNKNOWN_SPS("SPS"), //
    // UNKNOWN_SRD("SRD"), //
    // UNKNOWN_SRS("SRS"), //
    SoftwareVersion("SWV"), //
    // OperatingState("SYS"), //
    // UNKNOWN_TCP("TCP"), // probably port number (12345)
    // UNKNOWN_TI1("TI1"), //
    HeatSinkTemperature("TKK"), //
    // UNKNOWN_TL1("TL1"), //
    // UNKNOWN_TL3("TL3"), //
    // UNKNOWN_TND("TND"), //
    AcFrequency("TNF"), //
    // UNKNOWN_TNH("TNH"), //
    // UNKNOWN_TNL("TNL"), //
    // UNKNOWN_TP1("TP1"), //
    // UNKNOWN_TP2("TP2"), //
    // UNKNOWN_TP3("TP3"), //
    // UNKNOWN_TV0("TV0"), //
    // UNKNOWN_TV1("TV1"), //
    // Type("TYP"), //
    // UNKNOWN_UA2("UA2"), //
    // UNKNOWN_UB2("UB2"), //
    // UNKNOWN_UGD("UGD"), //
    // UNKNOWN_UI1("UI1"), //
    // UNKNOWN_UI2("UI2"), //
    // UNKNOWN_UI3("UI3"), //
    // UNKNOWN_ULH("ULH"), //
    // UNKNOWN_ULL("ULL"), //
    AcPhase1Voltage("UL1"), //
    AcPhase2Voltage("UL2"), //
    AcPhase3Voltage("UL3"), //
    // UNKNOWN_UMX("UMX"), //
    // UNKNOWN_UM1("UM1"), //
    // UNKNOWN_UM2("UM2"), //
    // UNKNOWN_UM3("UM3"), //
    // UNKNOWN_UPD("UPD"), //
    // UNKNOWN_UZK("UZK"), //
    // UNKNOWN_VCM("VCM"), //
    UNKNOWN("UNKNOWN") // really unknown - shouldn't ever be sent to the device
    ;

    // Valid commands which returned a null/empty value during testing
    // FFK, FRT, GCP, ITN, PLD, PLE, PLF, PLS, PPO, TV2, VLE, VLI, VLO

    private String commandKey;

    private SolarMaxCommandKey(String commandKey) {
        this.commandKey = commandKey;
    }

    public String getCommandKey() {
        return this.commandKey;
    }

    public static SolarMaxCommandKey getKeyFromString(String commandKey) {

        for (SolarMaxCommandKey key : SolarMaxCommandKey.values()) {
            if (key.commandKey.equals(commandKey)) {
                return key;
            }
        }
        return UNKNOWN;
    }
}
