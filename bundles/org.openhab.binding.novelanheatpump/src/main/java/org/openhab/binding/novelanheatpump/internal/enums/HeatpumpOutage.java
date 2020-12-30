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
package org.openhab.binding.novelanheatpump.internal.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum HeatpumpOutage {
    OUTAGE_0(0, "Waermepumpe Stoerung"),
    OUTAGE_1(1, "Anlagen Stoerung"),
    OUTAGE_2(2, "Betriebsart Zweiter Waermeerzeuger"),
    OUTAGE_3(3, "EVU-Sperre"),
    OUTAGE_4(4, ""),
    OUTAGE_5(5, "Lauftabtau (nur LW-Geraete)"),
    OUTAGE_6(6, "Temperatur Einsatzgrenze maximal"),
    OUTAGE_7(7, "Temperatur Einsatzgrenze minimal"),
    OUTAGE_8(8, "Untere Einsatzgrenze"),
    OUTAGE_9(9, "Keine Anforderung"),
    OUTAGE_UNKNOWN(-1, "Unbekannte Abschaltung");

    private final String name;
    private final Integer code;
    private static final Logger logger = LoggerFactory.getLogger(HeatpumpState.class);

    private HeatpumpOutage(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static final HeatpumpOutage fromCode(Integer code) {
        for (HeatpumpOutage error : HeatpumpOutage.values()) {
            if (error.code.equals(code)) {
                return error;
            }
        }

        logger.info("Unknown heatpump outage code {}", code);
        return OUTAGE_UNKNOWN;
    }

    @Override
    public String toString() {
        return code + ": " + name;
    }
}
