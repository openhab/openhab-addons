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
package org.openhab.binding.sunsynk.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Battery} is the internal class for battery information
 * from a Sun Synk Connect Account.
 * Currently only Lithium SunSynk batteries are known to work.
 * 
 * @author Lee Charlton - Initial contribution
 */

@SuppressWarnings("unused")
@NonNullByDefault
public class Battery {
    private int code;
    private String msg = "";
    private boolean success;
    private Data data = new Data();

    class Data {
        private String time = "";
        private double etodayChg;
        private double etodayDischg;
        private double emonthChg;
        private double emonthDischg;
        private double eyearChg;
        private double eyearDischg;
        private double etotalChg;
        private double etotalDischg;
        private int type;
        private int power;
        private double capacity;
        private double correctCap;
        private double bmsSoc;
        private double bmsVolt;
        private double bmsCurrent;
        private double bmsTemp;
        private double current;
        private double voltage;
        private double temp;
        private double soc;
        private double chargeVolt;
        private double dischargeVolt;
        private double chargeCurrentLimit;
        private double dischargeCurrentLimit;
        private double maxChargeCurrentLimit;
        private double maxDischargeCurrentLimit;
        private int bms1Version1;
        private int bms1Version2;
        private String current2 = "";
        private String voltage2 = "";
        private String temp2 = "";
        private String soc2 = "";
        private String chargeVolt2 = "";
        private String dischargeVolt2 = "";
        private String chargeCurrentLimit2 = "";
        private String dischargeCurrentLimit2 = "";
        private String maxChargeCurrentLimit2 = "";
        private String maxDischargeCurrentLimit2 = "";
        private String bms2Version1 = "";
        private String bms2Version2 = "";
        private int status;
        private double batterySoc1;
        private double batteryCurrent1;
        private double batteryVolt1;
        private double batteryPower1;
        private double batteryTemp1;
        private double batteryStatus2;
        private String batterySoc2 = "";
        private String batteryCurrent2 = "";
        private String batteryVolt2 = "";
        private String batteryPower2 = "";
        private String batteryTemp2 = "";
        private String numberOfBatteries = "";
        private String batt1Factory = "";
        private String batt2Factory = "";
    }

    public double getBatteryVoltage() {
        return this.data.voltage;
    }

    public double getBatteryCurrent() { // -ve if charging battery.
        return this.data.current;
    }

    public double getBatteryPower() {
        return this.data.power;
    }

    public double getBatteryCapacity() {
        return this.data.capacity;
    }

    public double getBatterySOC() {
        return this.data.soc;
    }

    public double getBatteryTemperature() {
        return this.data.temp;
    }

    public String toString() {
        return "Content [code=" + code + ", msg=" + msg + "sucess=" + success + ", data=" + data + "]";
    }
}
