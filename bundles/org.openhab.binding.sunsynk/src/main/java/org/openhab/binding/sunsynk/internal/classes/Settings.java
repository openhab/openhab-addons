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

import java.util.Arrays;
import java.util.List;

/**
 * The {@link Settings} is the internal class for Inverter Common Settings information
 * (grid charge / battery discharge schedule)from the SunSynk Account.
 * 
 * 
 * @author Lee Charlton - Initial contribution
 */

public class Settings {
    // false or true; sets grid charging interval on, send without quotes e.g. true
    private Boolean time1on;
    private Boolean time2on;
    private Boolean time3on;
    private Boolean time4on;
    private Boolean time5on;
    private Boolean time6on;
    List<Boolean> gridTimeron = Arrays.asList(time1on, time2on, time3on, time4on, time5on, time6on);
    //
    private Boolean time1On;
    private Boolean time2On;
    private Boolean time3On;
    private Boolean time4On;
    private Boolean time5On;
    private Boolean time6On;
    // 00:05 reslution; sets time interval of charging, send with quotes e.g. "01:05"
    private String sellTime1;
    private String sellTime2;
    private String sellTime3;
    private String sellTime4;
    private String sellTime5;
    private String sellTime6;
    List<String> timerTime = Arrays.asList(sellTime1, sellTime2, sellTime3, sellTime4, sellTime5, sellTime6);
    //
    // false or true; sets gen charging interval on, send without quotes e.g. true
    private Boolean genTime1on;
    private Boolean genTime2on;
    private Boolean genTime3on;
    private Boolean genTime4on;
    private Boolean genTime5on;
    private Boolean genTime6on;
    List<Boolean> genTimeron = Arrays.asList(genTime1on, genTime2on, genTime3on, genTime4on, genTime5on, genTime6on);
    //
    private String beep;
    private String attOverExitFreqStopDelay;
    private String exMeterCtSwitch;
    private String sdChargeOn;
    private String lockInVoltVar;
    private String batWarn;
    private String wattVarEnable;
    private String reconnMinVolt;
    private String caFStart;
    private String pvMaxLimit;
    private String sensorsCheck;
    private String wattUnderExitFreq;
    private String overVolt1;
    private String overVolt2;
    private String genPeakPower;
    private String meterA;
    private String meterB;
    private String meterC;
    private String eeprom;
    private String comSet;
    private String varQac1;
    private String varQac2;
    private String varQac3;
    private String caVoltPressureEnable;
    private String wattUnderFreq1;
    private String solarMaxSellPower;
    private String acCoupleOnGridSideEnable;
    private String mondayOn;
    private String tuesdayOn;
    private String wednesdayOn;
    private String thursdayOn;
    private String fridayOn;
    private String saturdayOn;
    private String sundayOn;
    private String batteryRestartCap;
    private String overFreq1Delay;
    private String bmsErrStop;
    private String checkTime;
    private String atsSwitch;
    // private String tempUp;
    private String acCurrentUp;
    private String sacPowerControl;
    private String rsd;
    private String batteryOn;
    private String volt1;
    private String volt2;
    private String volt3;
    private String volt4;
    private String volt5;
    private String volt6;
    private String volt7;
    private String volt8;
    private String volt9;
    private String volt10;
    private String volt11;
    private String volt12;
    private String wattUnderFreq1StartDelay;
    private String rcd;
    private String chargeVolt;
    private String floatVolt;
    private String workState;
    private String loadMode;
    private String sysWorkMode;
    private String sn;
    private String genCoolingTime;
    private String genPeakShaving;
    private String current4;
    private String current3;
    private String current2;
    private String current1;
    private String current8;
    private String current7;
    private String current6;
    private String current5;
    private String current9;
    private String current10;
    private String current11;
    private String current12;
    private String wattV1;
    private String wattV2;
    private String wattV3;
    private String wattV4;
    private String batteryEfficiency;
    private String genAndGridSignal;
    private String acFreqLow;
    private String reactivePowerControl;
    private String batteryEmptyV;
    private String open;
    private String reconnMaxFreq;
    private String standard;
    private String wattVarReactive1;
    private String wattVarReactive2;
    private String wattVarReactive3;
    private String wattVarReactive4;
    private String disableFloatCharge;
    private String inverterType;
    private String solarPSU;
    // SOC for battery
    private int cap1;
    private int cap2;
    private int cap3;
    private int cap4;
    private int cap5;
    private int cap6;
    List<Integer> batteryCapacity = Arrays.asList(cap1, cap2, cap3, cap4, cap5, cap6);
    //
    private String generatorForcedStart;
    private String overLongVolt;
    private String batteryChargeType;
    private String genOffVolt;
    private String absorptionVolt;
    private String genToLoad;
    private String mpptNum;
    private String underFreq1;
    private String underFreq2;
    private String wattPfEnable;
    private String remoteLock;
    private String generatorStartCap;
    private String batteryMaxCurrentCharge;
    private String overFreq1;
    private String overFreq2;
    private String genOnVolt;
    private String solar2WindInputEnable;
    private String caVStop;
    private String battMode;
    private String genOnCap;
    private String gridAlwaysOn;
    private String batteryLowVolt;
    private String acFreqUp;
    private String chargeLimit;
    private String generatorStartVolt;
    private String overVolt1Delay;
    //
    private int sellTime1Pac;
    private int sellTime2Pac;
    private int sellTime3Pac;
    private int sellTime4Pac;
    private int sellTime5Pac;
    private int sellTime6Pac;
    List<Integer> batteryPowerLimit = Arrays.asList(sellTime1Pac, sellTime2Pac, sellTime3Pac, sellTime4Pac,
            sellTime5Pac, sellTime6Pac);
    //
    private String californiaFreqPressureEnable;
    private String activePowerControl;
    private String batteryRestartVolt;
    private String zeroExportPower;
    private String overVolt2Delay;
    private String equChargeCycle;
    private String dischargeCurrent;
    private String solarSell;
    private String mpptVoltLow;
    // private String time3on;
    private String wattVoltEnable;
    private String caFwEnable;
    private String maxOperatingTimeOfGen;
    //
    private String maxExportGridOff;
    private String pvLine;
    private String three41;
    private String caVwEnable;
    private String batteryShutdownVolt;
    private String startVoltUp;
    private String riso;
    private String sellTime1Volt;
    private String sellTime2Volt;
    private String sellTime3Volt;
    private String sellTime4Volt;
    private String sellTime5Volt;
    private String sellTime6Volt;
    private String facLowProtect;
    private String wattOverFreq1;
    private String wattPf1;
    private String wattPf2;
    private String wattPf3;
    private String wattPf4;
    private String lowNoiseMode;
    private String tempco;
    private String arcFactFrz;
    private String meterSelect;
    private String genChargeOn;
    private String externalCtRatio;
    private String gridMode;
    private String lowThrough;
    private String drmEnable;
    private String underFreq1Delay;
    private String underFreq2Delay;
    private String energyMode;
    private String ampm;
    private String gridPeakShaving;
    private String fac;
    private String vacLowProtect;
    private String chargeCurrentLimit;
    private String caLv3;
    private String specialFunction;
    private String batteryImpedance;
    private String safetyType;
    private String varVolt4;
    private String varVolt3;
    private String varVolt2;
    private String varVolt1;
    private String commAddr;
    private String dischargeLimit;
    private String atsEnable;
    private String exMeterCt;
    private String overFreq2Delay;
    private String phase;
    private String autoDim;
    private String batteryWorkStatus;
    private String genToLoadOn;
    private String timeSync;
    private String wattOverWgralFreq;
    private String sdBatteryCurrent;
    private String peakAndVallery;
    private String batteryEmptyVolt;
    private String batteryLowCap;
    private String underVolt2Delay;
    private String equChargeTime;
    private String battType;
    private String gridPeakPower;
    private String reset;
    private String vacHighProtect;
    private String pwm;
    private String highThrough;
    private String lockOutVoltVar;
    private String lockInWattPF;
    private String caVStart;
    private String acVoltUp;
    private String wattFreqEnable;
    private String wattOverExitFreq;
    private String caFStop;
    private String lowPowerMode;
    private String varVoltEnable;
    private String acCoupleFreqUpper;
    private String impedanceLow;
    private String acType;
    private String facHighProtect;
    private String recoveryTime;
    private String lithiumMode;
    private String gridSignal;
    private String wattOverFreq1StartDelay;
    private String testCommand;
    private String signalIslandModeEnable;
    private String upsStandard;
    private String reconnMinFreq;
    private String parallelRegister2;
    private String parallelRegister1;
    private String startVoltLow;
    private String smartLoadOpenDelay;
    private String wattVarActive1;
    private String wattVarActive2;
    private String wattVarActive3;
    private String wattVarActive4;
    private String genConnectGrid;
    private String flag2;
    private String softStart;
    private String lockOutWattPF;
    private String sdStartCap;
    private String gfdi;
    private String checkSelfTime;
    private String limit;
    private String wattW1;
    private String wattW2;
    private String wattW3;
    private String wattW4;
    private String externalCurrent;
    private String vnResponseTime;
    private String batteryShutdownCap;
    private String wattUnderExitFreqStopDelay;
    private String offset;
    private String wattActivePf1;
    private String wattActivePf2;
    private String wattActivePf3;
    private String wattActivePf4;
    private String dischargeVolt;
    private String qvResponseTime;
    private String four19;
    private String micExportAll;
    private String batteryMaxCurrentDischarge;
    private String isletProtect;
    private String californiaVoltPressureEnable;
    private String equVoltCharge;
    private String batteryCap;
    private String genOffCap;
    private String powerFactor;
    private String acCoupleOnLoadSideEnable;
    private String sdStartVolt;
    private String generatorBatteryCurrent;
    private String reconnMaxVolt;
    private String modbusSn;
    private String inverterOutputVoltage;
    private String chargeCurrent;
    private String solar1WindInputEnable;
    private String dcVoltUp;
    private String parallel;
    private String limter;
    private String batErr;
    private String backupDelay;
    private String dischargeCurrentLimit;
    private String arcFactB;
    private String arcFactC;
    private String arcFactD;
    private String arcFactF;
    private String arcFactI;
    private String arcFactT;
    private String wattUnderWgalFreq;
    private String commBaudRate;
    private String equipMode;
    private String gridSideINVMeter2;
    private String underVolt1Delay;
    private String arcFaultType;
    private String normalUpwardSlope;
    private String pf;
    private String genMinSolar;
    private String acVoltLow;
    private String genSignal;

    public String getsn() {
        return this.sn;
    }

    public String getToken() {
        return APIdata.static_access_token;
    }

    public List<Boolean> getIntervalGridTimerOn() {
        return this.gridTimeron;
    }

    public List<String> getIntervalTime() {
        return this.timerTime;
    }

    public List<Boolean> getIntervalGenTimerOn() {
        return this.genTimeron;
    }

    public List<Integer> getIntervalBatteryCapacity() {
        return this.batteryCapacity;
    }

    public List<Integer> getIntervalBatteryPowerLimit() {
        return this.batteryPowerLimit;
    }

    public void setIntervalGridTimerOn(Boolean state, int interval) {
        this.gridTimeron.set(interval - 1, state);
        return;
    }

    public void setIntervalTime(String state, int interval) {
        this.timerTime.set(interval - 1, state);
        return;
    }

    public void setIntervalGenTimerOn(Boolean state, int interval) {
        this.genTimeron.set(interval - 1, state);
        return;
    }

    public void setIntervalBatteryCapacity(int state, int interval) {
        this.batteryCapacity.set(interval - 1, state);
        return;
    }

    public void setIntervalBatteryPowerLimit(int state, int interval) {
        this.batteryPowerLimit.set(interval - 1, state);
        return;
    }

    public String buildBody() {

        String body = "{";
        body = body + "\"sn\":\"" + this.sn + "\",";
        body = body + "\"safetyType\":\"" + this.safetyType + "\",";
        body = body + "\"battMode\":\"" + this.battMode + "\",";
        body = body + "\"solarSell\":\"" + this.solarSell + "\",";
        body = body + "\"pvMaxLimit\":\"" + this.pvMaxLimit + "\",";
        body = body + "\"energyMode\":\"" + this.energyMode + "\",";
        body = body + "\"peakAndVallery\":\"" + this.peakAndVallery + "\",";
        body = body + "\"sysWorkMode\":\"" + this.sysWorkMode + "\",";
        body = body + "\"sellTime1\":\"" + this.timerTime.get(0) + "\",";
        body = body + "\"sellTime2\":\"" + this.timerTime.get(1) + "\",";
        body = body + "\"sellTime3\":\"" + this.timerTime.get(2) + "\",";
        body = body + "\"sellTime4\":\"" + this.timerTime.get(3) + "\",";
        body = body + "\"sellTime5\":\"" + this.timerTime.get(4) + "\",";
        body = body + "\"sellTime6\":\"" + this.timerTime.get(5) + "\",";
        body = body + "\"sellTime1Pac\":\"" + this.batteryPowerLimit.get(0) + "\",";
        body = body + "\"sellTime2Pac\":\"" + this.batteryPowerLimit.get(1) + "\",";
        body = body + "\"sellTime3Pac\":\"" + this.batteryPowerLimit.get(2) + "\",";
        body = body + "\"sellTime4Pac\":\"" + this.batteryPowerLimit.get(3) + "\",";
        body = body + "\"sellTime5Pac\":\"" + this.batteryPowerLimit.get(4) + "\",";
        body = body + "\"sellTime6Pac\":\"" + this.batteryPowerLimit.get(5) + "\",";
        body = body + "\"cap1\":\"" + this.batteryCapacity.get(0) + "\",";
        body = body + "\"cap2\":\"" + this.batteryCapacity.get(1) + "\",";
        body = body + "\"cap3\":\"" + this.batteryCapacity.get(2) + "\",";
        body = body + "\"cap4\":\"" + this.batteryCapacity.get(3) + "\",";
        body = body + "\"cap5\":\"" + this.batteryCapacity.get(4) + "\",";
        body = body + "\"cap6\":\"" + this.batteryCapacity.get(5) + "\",";
        body = body + "\"sellTime1Volt\":\"" + this.sellTime1Volt + "\",";
        body = body + "\"sellTime2Volt\":\"" + this.sellTime2Volt + "\",";
        body = body + "\"sellTime3Volt\":\"" + this.sellTime3Volt + "\",";
        body = body + "\"sellTime4Volt\":\"" + this.sellTime4Volt + "\",";
        body = body + "\"sellTime5Volt\":\"" + this.sellTime5Volt + "\",";
        body = body + "\"sellTime6Volt\":\"" + this.sellTime6Volt + "\",";
        body = body + "\"zeroExportPower\":\"" + this.zeroExportPower + "\",";
        body = body + "\"solarMaxSellPower\":\"" + this.solarMaxSellPower + "\",";
        body = body + "\"mondayOn\":" + this.mondayOn + ",";
        body = body + "\"tuesdayOn\":" + this.tuesdayOn + ",";
        body = body + "\"wednesdayOn\":" + this.wednesdayOn + ",";
        body = body + "\"thursdayOn\":" + this.thursdayOn + ",";
        body = body + "\"fridayOn\":" + this.fridayOn + ",";
        body = body + "\"saturdayOn\":" + this.saturdayOn + ",";
        body = body + "\"sundayOn\":" + this.sundayOn + ",";
        body = body + "\"time1on\":" + this.gridTimeron.get(0) + ",";
        body = body + "\"time2on\":" + this.gridTimeron.get(1) + ",";
        body = body + "\"time3on\":" + this.gridTimeron.get(2) + ",";
        body = body + "\"time4on\":" + this.gridTimeron.get(3) + ",";
        body = body + "\"time5on\":" + this.gridTimeron.get(4) + ",";
        body = body + "\"time6on\":" + this.gridTimeron.get(5) + ",";
        body = body + "\"genTime1on\":" + this.genTimeron.get(0) + ",";
        body = body + "\"genTime2on\":" + this.genTimeron.get(1) + ",";
        body = body + "\"genTime3on\":" + this.genTimeron.get(2) + ",";
        body = body + "\"genTime4on\":" + this.genTimeron.get(3) + ",";
        body = body + "\"genTime5on\":" + this.genTimeron.get(4) + ",";
        body = body + "\"genTime6on\":" + this.genTimeron.get(5) + "}";
        return body;
    }
}
