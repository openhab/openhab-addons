/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink;

/**
 * list of all available channels
 *
 * @author afriese
 *
 */
public enum UplinkDataChannels {

    // Raumzieltemperatur
    CH_47398_TEMP_ROOM_TARGET("47398", "Raumzieltemperatur", Double.class),

    // Raumtemperatur BT50
    CH_40033_TEMP_ROOM_BT50("40033", "Raumtemperatur aktuell", Double.class),

    // berechn. Vorlauf Heizung S1
    CH_43009_REQ_TEMP_FORWARD_S1("43009", "berechn. Vorlauf Heizung S1", Double.class),

    // berechn. Vorlauf Heizung S1
    CH_44270_REQ_TEMP_FORWARD_COOL_S1("44270", "berechn. Vorlauf Kühlung S1", Double.class),

    // Vorlauf Heizung BT2
    CH_40008_TEMP_FORWARD_BT2("40008", "Vorlauf Heizung BT2", Double.class),

    // Rücklauf Heizung BT3
    CH_40012_TEMP_RETURN_BT3("40012", "Rücklauf Heizung BT3", Double.class),

    // Brauchwasser oben BT7
    CH_40013_HOT_WATER_TOP_BT7("40013", "Brauchwasser oben BT7", Double.class),

    // Brauchwasserbereitung BT6
    CH_40014_HOT_WATER_HEATER_BT6("40014", "Brauchwasserbereitung BT6", Double.class),

    // Gradminuten
    CH_43005_DEGREE_MINUTES("43005", "Gradminuten", Double.class),

    // Pumpengeschw. Wärmetr. GP1
    CH_44396_SPEED_GP1("44396", "Pumpengeschw. Wärmetr. GP1", Double.class),

    // BW, inkl. int. ZH
    CH_44298_HW_INCL_ADD("44298", "BW, inkl. int. ZH", Double.class),

    // BW, nur Verd.
    CH_44306_HW_COMP_ONLY("44306", "BW, nur Verd.", Double.class),

    // (44300 oder 44308)
    CH_44300_HEAT_INCL_ADD("44300", "Heizung, inkl. int. Zusatz", Double.class),

    // (44300 oder 44308)
    CH_44308_HEAT_COMP_ONLY("44308", "Heizung, nur Verd.", Double.class),

    // (44302 oder 44304)
    CH_44302_COOL_COMP_ONLY("44302", "Kühlung, nur Verd.", Double.class),

    // (44302 oder 44304)
    CH_44304_POOL_COMP_ONLY("44304", "Pool, nur Verd.", Double.class),

    // Außentemperatur
    CH_40004_TEMP_OUT_BT1("40004", "Außentemperatur", Double.class),

    // Außentemperatur
    CH_40067_TEMP_OUT_AVG_BT1("40067", "durchschn. Außentemperatur", Double.class),

    // akt. Leistung Zusatzheizung
    CH_43084_CUR_POW_INT_ADD("43084", "akt. Leistung Zusatzheizung", Double.class),

    // max Leistung Zusatzheizung
    CH_47212_MAX_POW_INT_ADD("47212", "max Leistung Zusatzheizung", Double.class),

    /* EB-101 spezifisch */

    // Kond.vorlauf EB101-BT12
    CH_44058_TEMP_EB101_BT12("44058", "Kond.vorlauf EB101-BT12", Double.class),

    // Rücklauftemp. EB101-BT3
    CH_44055_TEMP_EB101_BT3("44055", "Rücklauftemp. EB101-BT3", Double.class),

    // Betriebszeit Verdichter Brauchwasser EB101
    CH_44073_HOURS_EB101_COMP_HW("44073", "Betriebszeit Verdichter Brauchwasser EB101", Double.class),

    // Betriebszeit Verdichter gesamt EB101
    CH_44071_HOURS_EB101_COMP_HEAT("44071", "Betriebszeit Verdichter gesamt EB101", Double.class),

    // Betriebszeit Verdichter Kühlung EB101
    CH_40737_HOURS_EB101_COMP_COOL("40737", "Betriebszeit Verdichter Kühlung EB101", Double.class),

    // Verdichter Starts EB101
    CH_44069_COUNT_EB101_COMP_STARTS("44069", "Verdichter Starts EB101", Double.class),

    // Verdichter Starts EB101
    CH_44701_CUR_SPEED_EB101_COMP("44701", "aktuelle Verdichterfrequenz EB101", Double.class),

    // Verdichter Starts EB101
    CH_40782_REQ_SPEED_EB101_COMP("40782", "angef. Verdichterfrequenz EB101", Double.class),

    /* ERS 20-250 spezifisch */

    /* Abluft BT20 */
    CH_40025_TEMP_ERS20250_BT20("40025", "Temperatur Abluft", Double.class),

    /* Fortluft BT21 */
    CH_40026_TEMP_ERS20250_BT21("40026", "Temperatur Fortluft", Double.class),

    /* Zuluft BT22 */
    CH_40075_TEMP_ERS20250_BT22("40075", "Temperatur Zuluft", Double.class),

    /* Außentemperatur AZ30-BT23 */
    CH_40183_TEMP_ERS20250_BT23("40183", "Temperatur Frischluft", Double.class),

    /* Ventilatordrehzahl GQ2 */
    CH_40311_SPEED_FAN_ERS20250_GQ2("40311", "Ventilator Abluft", Double.class),

    /* Ventilatordrehzahl GQ3 */
    CH_40312_SPEED_FAN_ERS20250_GQ3("40312", "Ventilator Zuluft", Double.class),

    /* END */
    ;

    private final String id;
    private final String name;
    private final Class<?> type;

    /**
     * Constructor
     *
     * @param id
     * @param name
     * @param type
     */
    UplinkDataChannels(String id, String name, Class<?> type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public static UplinkDataChannels fromId(String id) {
        for (UplinkDataChannels channel : UplinkDataChannels.values()) {
            if (channel.id.equals(id)) {
                return channel;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Class<?> getType() {
        return type;
    }

}
