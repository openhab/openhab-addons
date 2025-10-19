/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sunsynk.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PlantSummary} is the internal class for plant summary information
 * from a Sun Synk Connect Account.
 * 
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class PlantSummary {
    private int code;
    private String msg = "";
    private boolean success;
    private Data data = new Data();

    class Data {
        private int pac;
        private double efficiency;
        private double etoday;
        private double emonth;
        private double eyear;
        private double etotal;
        private double totalPower;
        private Currency currency = new Currency();
        private double invest;
        private double income;
        private String updateAt = "";

        class Currency {
            private int id;
            private String code = "";
            private String text = "";

            public String toString() {
                return "Currency [id=" + id + ", code=" + code + ", text=" + text + "]";
            }
        }

        public String toString() {
            return "Data [pac=" + pac + ", efficiency=" + efficiency + ", etoday=" + etoday + ", emonth=" + emonth
                    + ", eyear=" + eyear + ", etotal=" + etotal + ", totalPower=" + totalPower + ", currency="
                    + currency.toString() + ", invest=" + invest + ", income=" + income + ", updateAt=" + updateAt
                    + "]";
        }
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public double getEtoday() {
        return this.data.etoday;
    }

    public double getEmonth() {
        return this.data.emonth;
    }

    public double getEyear() {
        return this.data.eyear;
    }

    public double getEtotal() {
        return this.data.etotal;
    }

    public double getEfficiency() {

        return getPac() / getCapacity() * 100.0; // Return as a percentage
    }

    public double getCapacity() {
        return this.data.totalPower * 1000.0; // Convert kW to W
    }

    public double getPac() {
        return this.data.pac;
    }

    public String toString() {
        return "PlantSummary [code=" + code + ", msg=" + msg + ", success=" + success + ", data=" + data.toString()
                + "]";
    }
}
