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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.Gson;

/**
 * The {@link Settings} is the internal class for inverter common settings information
 * (grid and solar charge / battery discharge schedule) from a Sun Synk Account.
 * 
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class Settings {
    private int code;
    private String msg = "";
    private boolean success;
    private Data data = new Data();
    List<Boolean> gridTimeron = new ArrayList<Boolean>();
    List<Integer> batteryCapacity = new ArrayList<Integer>();
    List<Boolean> genTimeron = new ArrayList<Boolean>();
    List<String> timerTime = new ArrayList<String>();
    List<Integer> batteryPowerLimit = new ArrayList<Integer>();

    @SuppressWarnings("unused")
    class Data {
        private boolean time1on;
        private boolean time2on;
        private boolean time3on;
        private boolean time4on;
        private boolean time5on;
        private boolean time6on;
        //
        private int time1On;
        private int time2On;
        private int time3On;
        private int time4On;
        private int time5On;
        private int time6On;
        // 00:05 min reslution; sets time interval of charging, send with quotes e.g. "01:05"
        private String sellTime1 = "";
        private String sellTime2 = "";
        private String sellTime3 = "";
        private String sellTime4 = "";
        private String sellTime5 = "";
        private String sellTime6 = "";
        // false or true; sets gen charging interval on, send without quotes e.g. true
        private boolean genTime1on;
        private boolean genTime2on;
        private boolean genTime3on;
        private boolean genTime4on;
        private boolean genTime5on;
        private boolean genTime6on;
        //
        private String beep = "";
        private String attOverExitFreqStopDelay = "";
        private String exMeterCtSwitch = "";
        private String sdChargeOn = "";
        private String lockInVoltVar = "";
        private String batWarn = "";
        private String wattVarEnable = "";
        private String reconnMinVolt = "";
        private String caFStart = "";
        private String pvMaxLimit = "";
        private String sensorsCheck = "";
        private String wattUnderExitFreq = "";
        private String overVolt1 = "";
        private String overVolt2 = "";
        private String genPeakPower = "";
        private String meterA = "";
        private String meterB = "";
        private String meterC = "";
        private String eeprom = "";
        private String comSet = "";
        private String varQac1 = "";
        private String varQac2 = "";
        private String varQac3 = "";
        private String caVoltPressureEnable = "";
        private String wattUnderFreq1 = "";
        private String solarMaxSellPower = "";
        private String acCoupleOnGridSideEnable = "";
        private String mondayOn = "";
        private String tuesdayOn = "";
        private String wednesdayOn = "";
        private String thursdayOn = "";
        private String fridayOn = "";
        private String saturdayOn = "";
        private String sundayOn = "";
        private String batteryRestartCap = "";
        private String overFreq1Delay = "";
        private String bmsErrStop = "";
        private String checkTime = "";
        private String atsSwitch = "";
        // private String tempUp;
        private String acCurrentUp = "";
        private String sacPowerControl = "";
        private String rsd = "";
        private String batteryOn = "";
        private String volt1 = "";
        private String volt2 = "";
        private String volt3 = "";
        private String volt4 = "";
        private String volt5 = "";
        private String volt6 = "";
        private String volt7 = "";
        private String volt8 = "";
        private String volt9 = "";
        private String volt10 = "";
        private String volt11 = "";
        private String volt12 = "";
        private String wattUnderFreq1StartDelay = "";
        private String rcd = "";
        private String chargeVolt = "";
        private String floatVolt = "";
        private String workState = "";
        private String loadMode = "";
        private int sysWorkMode;
        private String sn = "";
        private String genCoolingTime = "";
        private String genPeakShaving = "";
        private String current4 = "";
        private String current3 = "";
        private String current2 = "";
        private String current1 = "";
        private String current8 = "";
        private String current7 = "";
        private String current6 = "";
        private String current5 = "";
        private String current9 = "";
        private String current10 = "";
        private String current11 = "";
        private String current12 = "";
        private String wattV1 = "";
        private String wattV2 = "";
        private String wattV3 = "";
        private String wattV4 = "";
        private String batteryEfficiency = "";
        private String genAndGridSignal = "";
        private String acFreqLow = "";
        private String reactivePowerControl = "";
        private String batteryEmptyV = "";
        private String open = "";
        private String reconnMaxFreq = "";
        private String standard = "";
        private String wattVarReactive1 = "";
        private String wattVarReactive2 = "";
        private String wattVarReactive3 = "";
        private String wattVarReactive4 = "";
        private String disableFloatCharge = "";
        private String inverterType = "";
        private String solarPSU = "";
        // SOC for battery
        private int cap1;
        private int cap2;
        private int cap3;
        private int cap4;
        private int cap5;
        private int cap6;
        //
        private String generatorForcedStart = "";
        private String overLongVolt = "";
        private String batteryChargeType = "";
        private String genOffVolt = "";
        private String absorptionVolt = "";
        private String genToLoad = "";
        private String mpptNum = "";
        private String underFreq1 = "";
        private String underFreq2 = "";
        private String wattPfEnable = "";
        private String remoteLock = "";
        private String generatorStartCap = "";
        private String batteryMaxCurrentCharge = "";
        private String overFreq1 = "";
        private String overFreq2 = "";
        private String genOnVolt = "";
        private String solar2WindInputEnable = "";
        private String caVStop = "";
        private String battMode = "";
        private String genOnCap = "";
        private String gridAlwaysOn = "";
        private String batteryLowVolt = "";
        private String acFreqUp = "";
        private String chargeLimit = "";
        private String generatorStartVolt = "";
        private String overVolt1Delay = "";
        //
        private int sellTime1Pac;
        private int sellTime2Pac;
        private int sellTime3Pac;
        private int sellTime4Pac;
        private int sellTime5Pac;
        private int sellTime6Pac;
        //
        private String californiaFreqPressureEnable = "";
        private String activePowerControl = "";
        private String batteryRestartVolt = "";
        private String zeroExportPower = "";
        private String overVolt2Delay = "";
        private String equChargeCycle = "";
        private String dischargeCurrent = "";
        private String solarSell = "";
        private String mpptVoltLow = "";
        // private String time3on;
        private String wattVoltEnable = "";
        private String caFwEnable = "";
        private String maxOperatingTimeOfGen = "";
        //
        private String maxExportGridOff = "";
        private String pvLine = "";
        private String three41 = "";
        private String caVwEnable = "";
        private String batteryShutdownVolt = "";
        private String startVoltUp = "";
        private String riso = "";
        private String sellTime1Volt = "";
        private String sellTime2Volt = "";
        private String sellTime3Volt = "";
        private String sellTime4Volt = "";
        private String sellTime5Volt = "";
        private String sellTime6Volt = "";
        private String facLowProtect = "";
        private String wattOverFreq1 = "";
        private String wattPf1 = "";
        private String wattPf2 = "";
        private String wattPf3 = "";
        private String wattPf4 = "";
        private String lowNoiseMode = "";
        private String tempco = "";
        private String arcFactFrz = "";
        private String meterSelect = "";
        private String genChargeOn = "";
        private String externalCtRatio = "";
        private String gridMode = "";
        private String lowThrough = "";
        private String drmEnable = "";
        private String underFreq1Delay = "";
        private String underFreq2Delay = "";
        private int energyMode;
        private String ampm = "";
        private String gridPeakShaving = "";
        private String fac = "";
        private String vacLowProtect = "";
        private String chargeCurrentLimit = "";
        private String caLv3 = "";
        private String specialFunction = "";
        private String batteryImpedance = "";
        private String safetyType = "";
        private String varVolt4 = "";
        private String varVolt3 = "";
        private String varVolt2 = "";
        private String varVolt1 = "";
        private String commAddr = "";
        private String dischargeLimit = "";
        private String atsEnable = "";
        private String exMeterCt = "";
        private String overFreq2Delay = "";
        private String phase = "";
        private String autoDim = "";
        private String batteryWorkStatus = "";
        private String genToLoadOn = "";
        private String timeSync = "";
        private String wattOverWgralFreq = "";
        private String sdBatteryCurrent = "";
        private int peakAndVallery;
        private String batteryEmptyVolt = "";
        private String batteryLowCap = "";
        private String underVolt2Delay = "";
        private String equChargeTime = "";
        private String battType = "";
        private String gridPeakPower = "";
        private String reset = "";
        private String vacHighProtect = "";
        private String pwm = "";
        private String highThrough = "";
        private String lockOutVoltVar = "";
        private String lockInWattPF = "";
        private String caVStart = "";
        private String acVoltUp = "";
        private String wattFreqEnable = "";
        private String wattOverExitFreq = "";
        private String caFStop = "";
        private String lowPowerMode = "";
        private String varVoltEnable = "";
        private String acCoupleFreqUpper = "";
        private String impedanceLow = "";
        private String acType = "";
        private String facHighProtect = "";
        private String recoveryTime = "";
        private String lithiumMode = "";
        private String gridSignal = "";
        private String wattOverFreq1StartDelay = "";
        private String testCommand = "";
        private String signalIslandModeEnable = "";
        private String upsStandard = "";
        private String reconnMinFreq = "";
        private String parallelRegister2 = "";
        private String parallelRegister1 = "";
        private String startVoltLow = "";
        private String smartLoadOpenDelay = "";
        private String wattVarActive1 = "";
        private String wattVarActive2 = "";
        private String wattVarActive3 = "";
        private String wattVarActive4 = "";
        private String genConnectGrid = "";
        private String flag2 = "";
        private String softStart = "";
        private String lockOutWattPF = "";
        private String sdStartCap = "";
        private String gfdi = "";
        private String checkSelfTime = "";
        private String limit = "";
        private String wattW1 = "";
        private String wattW2 = "";
        private String wattW3 = "";
        private String wattW4 = "";
        private String externalCurrent = "";
        private String vnResponseTime = "";
        private String batteryShutdownCap = "";
        private String wattUnderExitFreqStopDelay = "";
        private String offset = "";
        private String wattActivePf1 = "";
        private String wattActivePf2 = "";
        private String wattActivePf3 = "";
        private String wattActivePf4 = "";
        private String dischargeVolt = "";
        private String qvResponseTime = "";
        private String four19 = "";
        private String micExportAll = "";
        private String batteryMaxCurrentDischarge = "";
        private String isletProtect = "";
        private String californiaVoltPressureEnable = "";
        private String equVoltCharge = "";
        private String batteryCap = "";
        private String genOffCap = "";
        private String powerFactor = "";
        private String acCoupleOnLoadSideEnable = "";
        private String sdStartVolt = "";
        private String generatorBatteryCurrent = "";
        private String reconnMaxVolt = "";
        private String modbusSn = "";
        private String inverterOutputVoltage = "";
        private String chargeCurrent = "";
        private String solar1WindInputEnable = "";
        private String dcVoltUp = "";
        private String parallel = "";
        private String limter = "";
        private String batErr = "";
        private String backupDelay = "";
        private String dischargeCurrentLimit = "";
        private String arcFactB = "";
        private String arcFactC = "";
        private String arcFactD = "";
        private String arcFactF = "";
        private String arcFactI = "";
        private String arcFactT = "";
        private String wattUnderWgalFreq = "";
        private String commBaudRate = "";
        private String equipMode = "";
        private String gridSideINVMeter2 = "";
        private String underVolt1Delay = "";
        private String arcFaultType = "";
        private String normalUpwardSlope = "";
        private String pf = "";
        private String genMinSolar = "";
        private String acVoltLow = "";
        private String genSignal = "";
    }

    public void buildLists() {
        this.gridTimeron = Arrays.asList(this.data.time1on, this.data.time2on, this.data.time3on, this.data.time4on,
                this.data.time5on, this.data.time6on);
        this.batteryCapacity = Arrays.asList(this.data.cap1, this.data.cap2, this.data.cap3, this.data.cap4,
                this.data.cap5, this.data.cap6);
        this.genTimeron = Arrays.asList(this.data.genTime1on, this.data.genTime2on, this.data.genTime3on,
                this.data.genTime4on, this.data.genTime5on, this.data.genTime6on);
        this.timerTime = Arrays.asList(this.data.sellTime1, this.data.sellTime2, this.data.sellTime3,
                this.data.sellTime4, this.data.sellTime5, this.data.sellTime6);
        this.batteryPowerLimit = Arrays.asList(this.data.sellTime1Pac, this.data.sellTime2Pac, this.data.sellTime3Pac,
                this.data.sellTime4Pac, this.data.sellTime5Pac, this.data.sellTime6Pac);
    }

    public String getsn() {
        return this.data.sn;
    }

    public String getToken() {
        return APIdata.staticAccessToken;
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

    public Integer getPeakAndValley() {
        return this.data.peakAndVallery;
    }

    public String getEnergyMode() {
        return String.valueOf(this.data.energyMode);
    }

    public String getSysWorkMode() {
        return String.valueOf(this.data.sysWorkMode);
    }

    public String toString() {
        return "Content [code=" + code + ", msg=" + msg + "sucess=" + success + ", data=" + data + "]";
    }

    public void setIntervalGridTimerOn(Boolean state, int interval) {
        this.gridTimeron.set(interval - 1, state);
        return;
    }

    public void setIntervalTime(String state, int interval) {
        this.timerTime.set(interval - 1, asAPITime(state));
        return;
    }

    private String asAPITime(String state) {
        String workerString = state.split("T")[1].substring(0, 5);
        int minsLS = Integer.valueOf(workerString.substring(4, 5));
        if ((minsLS < 5) & (minsLS > 0)) {
            minsLS = 0;
        } else if ((minsLS > 5)) {
            minsLS = 5;
        }
        return workerString.substring(0, 4) + minsLS;
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

    public void setPeakAndValley(int state) {
        this.data.peakAndVallery = state;
    }

    public void setEnergyMode(int state) {
        this.data.energyMode = state;
    }

    public void setSysWorkMode(int state) {
        this.data.sysWorkMode = state;
    }

    public String buildBody() {
        Gson gson = new Gson();
        SettingsCommand commandBody = new SettingsCommand();
        commandBody.sn = this.data.sn;
        commandBody.safetyType = this.data.safetyType;
        commandBody.battMode = this.data.battMode;
        commandBody.solarSell = this.data.solarSell;
        commandBody.pvMaxLimit = this.data.pvMaxLimit;
        commandBody.energyMode = this.data.energyMode;
        commandBody.peakAndVallery = this.data.peakAndVallery;
        commandBody.sysWorkMode = this.data.sysWorkMode;
        commandBody.sellTime1 = this.timerTime.get(0);
        commandBody.sellTime2 = this.timerTime.get(1);
        commandBody.sellTime3 = this.timerTime.get(2);
        commandBody.sellTime4 = this.timerTime.get(3);
        commandBody.sellTime5 = this.timerTime.get(4);
        commandBody.sellTime6 = this.timerTime.get(5);
        commandBody.sellTime1Pac = this.batteryPowerLimit.get(0);
        commandBody.sellTime2Pac = this.batteryPowerLimit.get(1);
        commandBody.sellTime3Pac = this.batteryPowerLimit.get(2);
        commandBody.sellTime4Pac = this.batteryPowerLimit.get(3);
        commandBody.sellTime5Pac = this.batteryPowerLimit.get(4);
        commandBody.sellTime6Pac = this.batteryPowerLimit.get(5);
        commandBody.cap1 = this.batteryCapacity.get(0);
        commandBody.cap2 = this.batteryCapacity.get(1);
        commandBody.cap3 = this.batteryCapacity.get(2);
        commandBody.cap4 = this.batteryCapacity.get(3);
        commandBody.cap5 = this.batteryCapacity.get(4);
        commandBody.cap6 = this.batteryCapacity.get(5);
        commandBody.sellTime1Volt = this.data.sellTime1Volt;
        commandBody.sellTime2Volt = this.data.sellTime2Volt;
        commandBody.sellTime3Volt = this.data.sellTime3Volt;
        commandBody.sellTime4Volt = this.data.sellTime4Volt;
        commandBody.sellTime5Volt = this.data.sellTime5Volt;
        commandBody.sellTime6Volt = this.data.sellTime6Volt;
        commandBody.zeroExportPower = this.data.zeroExportPower;
        commandBody.solarMaxSellPower = this.data.solarMaxSellPower;
        commandBody.mondayOn = this.data.mondayOn;
        commandBody.tuesdayOn = this.data.tuesdayOn;
        commandBody.wednesdayOn = this.data.wednesdayOn;
        commandBody.thursdayOn = this.data.thursdayOn;
        commandBody.fridayOn = this.data.fridayOn;
        commandBody.saturdayOn = this.data.saturdayOn;
        commandBody.sundayOn = this.data.sundayOn;
        commandBody.time1on = this.gridTimeron.get(0);
        commandBody.time2on = this.gridTimeron.get(1);
        commandBody.time3on = this.gridTimeron.get(2);
        commandBody.time4on = this.gridTimeron.get(3);
        commandBody.time5on = this.gridTimeron.get(4);
        commandBody.time6on = this.gridTimeron.get(5);
        commandBody.genTime1on = this.genTimeron.get(0);
        commandBody.genTime2on = this.genTimeron.get(1);
        commandBody.genTime3on = this.genTimeron.get(2);
        commandBody.genTime4on = this.genTimeron.get(3);
        commandBody.genTime5on = this.genTimeron.get(4);
        commandBody.genTime6on = this.genTimeron.get(5);
        return gson.toJson(commandBody);
    }
}
