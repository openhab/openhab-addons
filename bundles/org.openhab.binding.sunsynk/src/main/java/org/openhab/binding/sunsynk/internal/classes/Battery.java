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
package org.openhab.binding.sunsynk.internal.classes;

/**
 * The {@link Battery} is the internal class for attery information from the sunsynk Account.
 * Currently only Lithium SunSynk batteries are known to work.
 * 
 * @author Lee Charlton - Initial contribution
 */

// {'time': None, 'etodayChg': '2.6', 'etodayDischg': '2.2', 'emonthChg': '17.6', 'emonthDischg': '10.0', 'eyearChg':
// '593.0',
// 'eyearDischg': '517.3', 'etotalChg': '1962.0', 'etotalDischg': '1744.1', 'type': 1, 'power': -2459, 'capacity':
// '300.0',
// 'correctCap': 300, 'bmsSoc': 79.0, 'bmsVolt': 54.43, 'bmsCurrent': 46.0, 'bmsTemp': 8.8, 'current': '-45.0',
// 'voltage': '54.6',
// 'temp': '8.8', 'soc': '79.0', 'chargeVolt': 56.1, 'dischargeVolt': 0.0, 'chargeCurrentLimit': 62.0,
// 'dischargeCurrentLimit': 156.0, 'maxChargeCurrentLimit': 0.0, 'maxDischargeCurrentLimit': 0.0, 'bms1Version1': 0,
// 'bms1Version2': 0, 'current2': None, 'voltage2': None, 'temp2': None, 'soc2': None, 'chargeVolt2': None,
// 'dischargeVolt2': None,
// 'chargeCurrentLimit2': None, 'dischargeCurrentLimit2': None, 'maxChargeCurrentLimit2': None,
// 'maxDischargeCurrentLimit2': None,
// 'bms2Version1': None, 'bms2Version2': None, 'status': 1, 'batterySoc1': 0.0, 'batteryCurrent1': 0.0, 'batteryVolt1':
// 0.0,
// 'batteryPower1': 0.0, 'batteryTemp1': 0.0, 'batteryStatus2': 0, 'batterySoc2': None, 'batteryCurrent2': None,
// 'batteryVolt2': None, 'batteryPower2': None, 'batteryTemp2': None, 'numberOfBatteries': None, 'batt1Factory': None,
// 'batt2Factory': None}

public class Battery {
    private String time;
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
    private String current2;
    private String voltage2;
    private String temp2;
    private String soc2;
    private String chargeVolt2;
    private String dischargeVolt2;
    private String chargeCurrentLimit2;
    private String dischargeCurrentLimit2;
    private String maxChargeCurrentLimit2;
    private String maxDischargeCurrentLimit2;
    private String bms2Version1;
    private String bms2Version2;
    private int status;
    private double batterySoc1;
    private double batteryCurrent1;
    private double batteryVolt1;
    private double batteryPower1;
    private double batteryTemp1;
    private double batteryStatus2;
    private String batterySoc2;
    private String batteryCurrent2;
    private String batteryVolt2;
    private String batteryPower2;
    private String batteryTemp2;
    private String numberOfBatteries;
    private String batt1Factory;
    private String batt2Factory;

    public double getBatteryVoltage() {
        return this.voltage;
    }

    public double getBatteryCurrent() { // -ve if charging battery.
        return this.current;
    }

    public double getBatteryPower() {
        return this.power;
    }

    public double getBatteryCapacity() {
        return this.capacity;
    }

    public double getBatterySOC() {
        return this.soc;
    }

    public double getBatteryTemperature() {
        return this.temp;
    }
}
