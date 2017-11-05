/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal.classes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link RobotInfoData} is the internal class for storing Information Data for the vacuum cleaneer.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class RobotInfoData {

    @SerializedName("modelName")
    @Expose
    private String modelName;
    @SerializedName("CPUMACID")
    @Expose
    private String cPUMACID;
    @SerializedName("MainBrdMfgDate")
    @Expose
    private String mainBrdMfgDate;
    @SerializedName("RobotMfgDate")
    @Expose
    private String robotMfgDate;
    @SerializedName("BoardRev")
    @Expose
    private Integer boardRev;
    @SerializedName("ChassisRev")
    @Expose
    private Integer chassisRev;
    @SerializedName("BatteryType")
    @Expose
    private Integer batteryType;
    @SerializedName("WheelPodType")
    @Expose
    private Integer wheelPodType;
    @SerializedName("DropSensorType")
    @Expose
    private Integer dropSensorType;
    @SerializedName("MagSensorType")
    @Expose
    private Integer magSensorType;
    @SerializedName("WallSensorType")
    @Expose
    private Integer wallSensorType;
    @SerializedName("LDSMotorType")
    @Expose
    private Integer lDSMotorType;
    @SerializedName("Locale")
    @Expose
    private Integer locale;
    @SerializedName("USMode")
    @Expose
    private Integer uSMode;
    @SerializedName("InternalModelName")
    @Expose
    private String internalModelName;
    @SerializedName("NeatoServer")
    @Expose
    private String neatoServer;
    @SerializedName("CartID")
    @Expose
    private Integer cartID;
    @SerializedName("brushSpeed")
    @Expose
    private Integer brushSpeed;
    @SerializedName("brushSpeedEco")
    @Expose
    private Integer brushSpeedEco;
    @SerializedName("vacuumSpeed")
    @Expose
    private Integer vacuumSpeed;
    @SerializedName("vacuumPwrPercent")
    @Expose
    private Integer vacuumPwrPercent;
    @SerializedName("vacuumPwrPercentEco")
    @Expose
    private Integer vacuumPwrPercentEco;
    @SerializedName("runTime")
    @Expose
    private Integer runTime;
    @SerializedName("BrushPresent")
    @Expose
    private Integer brushPresent;
    @SerializedName("VacuumPresent")
    @Expose
    private Integer vacuumPresent;
    @SerializedName("PadPresent")
    @Expose
    private Integer padPresent;
    @SerializedName("PlatenPresent")
    @Expose
    private Integer platenPresent;
    @SerializedName("BrushDirection")
    @Expose
    private Integer brushDirection;
    @SerializedName("VacuumDirection")
    @Expose
    private Integer vacuumDirection;
    @SerializedName("PadDirection")
    @Expose
    private Integer padDirection;
    @SerializedName("CumulativeCartridgeTimeInSecs")
    @Expose
    private Integer cumulativeCartridgeTimeInSecs;
    @SerializedName("nCleaningsStartedWhereDustBinWasFull")
    @Expose
    private Integer nCleaningsStartedWhereDustBinWasFull;
    @SerializedName("BlowerType")
    @Expose
    private Integer blowerType;
    @SerializedName("BrushMotorType")
    @Expose
    private Integer brushMotorType;
    @SerializedName("SideBrushType")
    @Expose
    private Integer sideBrushType;
    @SerializedName("SideBrushPower")
    @Expose
    private Integer sideBrushPower;
    @SerializedName("nAutoCycleCleaningsStarted")
    @Expose
    private Integer nAutoCycleCleaningsStarted;
    @SerializedName("hardware_version_major")
    @Expose
    private Integer hardwareVersionMajor;
    @SerializedName("hardware_version_minor")
    @Expose
    private Integer hardwareVersionMinor;
    @SerializedName("software_version_major")
    @Expose
    private Integer softwareVersionMajor;
    @SerializedName("software_version_minor")
    @Expose
    private Integer softwareVersionMinor;
    @SerializedName("max_voltage")
    @Expose
    private Integer maxVoltage;
    @SerializedName("max_current")
    @Expose
    private Integer maxCurrent;
    @SerializedName("voltage_multiplier")
    @Expose
    private Integer voltageMultiplier;
    @SerializedName("current_multiplier")
    @Expose
    private Integer currentMultiplier;
    @SerializedName("capacity_mode")
    @Expose
    private Integer capacityMode;
    @SerializedName("design_capacity")
    @Expose
    private Integer designCapacity;
    @SerializedName("design_voltage")
    @Expose
    private Integer designVoltage;
    @SerializedName("mfg_day")
    @Expose
    private Integer mfgDay;
    @SerializedName("mfg_month")
    @Expose
    private Integer mfgMonth;
    @SerializedName("mfg_year")
    @Expose
    private Integer mfgYear;
    @SerializedName("serial_number")
    @Expose
    private Integer serialNumber;
    @SerializedName("sw_ver")
    @Expose
    private Integer swVer;
    @SerializedName("data_ver")
    @Expose
    private Integer dataVer;
    @SerializedName("mfg_access")
    @Expose
    private Integer mfgAccess;
    @SerializedName("mfg_name")
    @Expose
    private String mfgName;
    @SerializedName("device_name")
    @Expose
    private String deviceName;
    @SerializedName("chemistry_name")
    @Expose
    private String chemistryName;
    @SerializedName("Major")
    @Expose
    private Integer major;
    @SerializedName("Minor")
    @Expose
    private Integer minor;
    @SerializedName("Build")
    @Expose
    private Integer build;
    @SerializedName("ldsVer")
    @Expose
    private String ldsVer;
    @SerializedName("ldsSerial")
    @Expose
    private String ldsSerial;
    @SerializedName("ldsCPU")
    @Expose
    private String ldsCPU;
    @SerializedName("ldsBuildNum")
    @Expose
    private String ldsBuildNum;
    @SerializedName("bootLoaderVersion")
    @Expose
    private Integer bootLoaderVersion;
    @SerializedName("uiBoardSWVer")
    @Expose
    private Integer uiBoardSWVer;
    @SerializedName("uiBoardHWVer")
    @Expose
    private Integer uiBoardHWVer;
    @SerializedName("qaState")
    @Expose
    private Integer qaState;
    @SerializedName("manufacturer")
    @Expose
    private Integer manufacturer;
    @SerializedName("driverVersion")
    @Expose
    private Integer driverVersion;
    @SerializedName("driverID")
    @Expose
    private Integer driverID;
    @SerializedName("ultrasonicSW")
    @Expose
    private Integer ultrasonicSW;
    @SerializedName("ultrasonicHW")
    @Expose
    private Integer ultrasonicHW;
    @SerializedName("blowerHW")
    @Expose
    private Integer blowerHW;
    @SerializedName("blowerSWMajor")
    @Expose
    private Integer blowerSWMajor;
    @SerializedName("blowerSWMinor")
    @Expose
    private Integer blowerSWMinor;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getCPUMACID() {
        return cPUMACID;
    }

    public void setCPUMACID(String cPUMACID) {
        this.cPUMACID = cPUMACID;
    }

    public String getMainBrdMfgDate() {
        return mainBrdMfgDate;
    }

    public void setMainBrdMfgDate(String mainBrdMfgDate) {
        this.mainBrdMfgDate = mainBrdMfgDate;
    }

    public String getRobotMfgDate() {
        return robotMfgDate;
    }

    public void setRobotMfgDate(String robotMfgDate) {
        this.robotMfgDate = robotMfgDate;
    }

    public Integer getBoardRev() {
        return boardRev;
    }

    public void setBoardRev(Integer boardRev) {
        this.boardRev = boardRev;
    }

    public Integer getChassisRev() {
        return chassisRev;
    }

    public void setChassisRev(Integer chassisRev) {
        this.chassisRev = chassisRev;
    }

    public Integer getBatteryType() {
        return batteryType;
    }

    public void setBatteryType(Integer batteryType) {
        this.batteryType = batteryType;
    }

    public Integer getWheelPodType() {
        return wheelPodType;
    }

    public void setWheelPodType(Integer wheelPodType) {
        this.wheelPodType = wheelPodType;
    }

    public Integer getDropSensorType() {
        return dropSensorType;
    }

    public void setDropSensorType(Integer dropSensorType) {
        this.dropSensorType = dropSensorType;
    }

    public Integer getMagSensorType() {
        return magSensorType;
    }

    public void setMagSensorType(Integer magSensorType) {
        this.magSensorType = magSensorType;
    }

    public Integer getWallSensorType() {
        return wallSensorType;
    }

    public void setWallSensorType(Integer wallSensorType) {
        this.wallSensorType = wallSensorType;
    }

    public Integer getLDSMotorType() {
        return lDSMotorType;
    }

    public void setLDSMotorType(Integer lDSMotorType) {
        this.lDSMotorType = lDSMotorType;
    }

    public Integer getLocale() {
        return locale;
    }

    public void setLocale(Integer locale) {
        this.locale = locale;
    }

    public Integer getUSMode() {
        return uSMode;
    }

    public void setUSMode(Integer uSMode) {
        this.uSMode = uSMode;
    }

    public String getInternalModelName() {
        return internalModelName;
    }

    public void setInternalModelName(String internalModelName) {
        this.internalModelName = internalModelName;
    }

    public String getNeatoServer() {
        return neatoServer;
    }

    public void setNeatoServer(String neatoServer) {
        this.neatoServer = neatoServer;
    }

    public Integer getCartID() {
        return cartID;
    }

    public void setCartID(Integer cartID) {
        this.cartID = cartID;
    }

    public Integer getBrushSpeed() {
        return brushSpeed;
    }

    public void setBrushSpeed(Integer brushSpeed) {
        this.brushSpeed = brushSpeed;
    }

    public Integer getBrushSpeedEco() {
        return brushSpeedEco;
    }

    public void setBrushSpeedEco(Integer brushSpeedEco) {
        this.brushSpeedEco = brushSpeedEco;
    }

    public Integer getVacuumSpeed() {
        return vacuumSpeed;
    }

    public void setVacuumSpeed(Integer vacuumSpeed) {
        this.vacuumSpeed = vacuumSpeed;
    }

    public Integer getVacuumPwrPercent() {
        return vacuumPwrPercent;
    }

    public void setVacuumPwrPercent(Integer vacuumPwrPercent) {
        this.vacuumPwrPercent = vacuumPwrPercent;
    }

    public Integer getVacuumPwrPercentEco() {
        return vacuumPwrPercentEco;
    }

    public void setVacuumPwrPercentEco(Integer vacuumPwrPercentEco) {
        this.vacuumPwrPercentEco = vacuumPwrPercentEco;
    }

    public Integer getRunTime() {
        return runTime;
    }

    public void setRunTime(Integer runTime) {
        this.runTime = runTime;
    }

    public Integer getBrushPresent() {
        return brushPresent;
    }

    public void setBrushPresent(Integer brushPresent) {
        this.brushPresent = brushPresent;
    }

    public Integer getVacuumPresent() {
        return vacuumPresent;
    }

    public void setVacuumPresent(Integer vacuumPresent) {
        this.vacuumPresent = vacuumPresent;
    }

    public Integer getPadPresent() {
        return padPresent;
    }

    public void setPadPresent(Integer padPresent) {
        this.padPresent = padPresent;
    }

    public Integer getPlatenPresent() {
        return platenPresent;
    }

    public void setPlatenPresent(Integer platenPresent) {
        this.platenPresent = platenPresent;
    }

    public Integer getBrushDirection() {
        return brushDirection;
    }

    public void setBrushDirection(Integer brushDirection) {
        this.brushDirection = brushDirection;
    }

    public Integer getVacuumDirection() {
        return vacuumDirection;
    }

    public void setVacuumDirection(Integer vacuumDirection) {
        this.vacuumDirection = vacuumDirection;
    }

    public Integer getPadDirection() {
        return padDirection;
    }

    public void setPadDirection(Integer padDirection) {
        this.padDirection = padDirection;
    }

    public Integer getCumulativeCartridgeTimeInSecs() {
        return cumulativeCartridgeTimeInSecs;
    }

    public void setCumulativeCartridgeTimeInSecs(Integer cumulativeCartridgeTimeInSecs) {
        this.cumulativeCartridgeTimeInSecs = cumulativeCartridgeTimeInSecs;
    }

    public Integer getNCleaningsStartedWhereDustBinWasFull() {
        return nCleaningsStartedWhereDustBinWasFull;
    }

    public void setNCleaningsStartedWhereDustBinWasFull(Integer nCleaningsStartedWhereDustBinWasFull) {
        this.nCleaningsStartedWhereDustBinWasFull = nCleaningsStartedWhereDustBinWasFull;
    }

    public Integer getBlowerType() {
        return blowerType;
    }

    public void setBlowerType(Integer blowerType) {
        this.blowerType = blowerType;
    }

    public Integer getBrushMotorType() {
        return brushMotorType;
    }

    public void setBrushMotorType(Integer brushMotorType) {
        this.brushMotorType = brushMotorType;
    }

    public Integer getSideBrushType() {
        return sideBrushType;
    }

    public void setSideBrushType(Integer sideBrushType) {
        this.sideBrushType = sideBrushType;
    }

    public Integer getSideBrushPower() {
        return sideBrushPower;
    }

    public void setSideBrushPower(Integer sideBrushPower) {
        this.sideBrushPower = sideBrushPower;
    }

    public Integer getNAutoCycleCleaningsStarted() {
        return nAutoCycleCleaningsStarted;
    }

    public void setNAutoCycleCleaningsStarted(Integer nAutoCycleCleaningsStarted) {
        this.nAutoCycleCleaningsStarted = nAutoCycleCleaningsStarted;
    }

    public Integer getHardwareVersionMajor() {
        return hardwareVersionMajor;
    }

    public void setHardwareVersionMajor(Integer hardwareVersionMajor) {
        this.hardwareVersionMajor = hardwareVersionMajor;
    }

    public Integer getHardwareVersionMinor() {
        return hardwareVersionMinor;
    }

    public void setHardwareVersionMinor(Integer hardwareVersionMinor) {
        this.hardwareVersionMinor = hardwareVersionMinor;
    }

    public Integer getSoftwareVersionMajor() {
        return softwareVersionMajor;
    }

    public void setSoftwareVersionMajor(Integer softwareVersionMajor) {
        this.softwareVersionMajor = softwareVersionMajor;
    }

    public Integer getSoftwareVersionMinor() {
        return softwareVersionMinor;
    }

    public void setSoftwareVersionMinor(Integer softwareVersionMinor) {
        this.softwareVersionMinor = softwareVersionMinor;
    }

    public Integer getMaxVoltage() {
        return maxVoltage;
    }

    public void setMaxVoltage(Integer maxVoltage) {
        this.maxVoltage = maxVoltage;
    }

    public Integer getMaxCurrent() {
        return maxCurrent;
    }

    public void setMaxCurrent(Integer maxCurrent) {
        this.maxCurrent = maxCurrent;
    }

    public Integer getVoltageMultiplier() {
        return voltageMultiplier;
    }

    public void setVoltageMultiplier(Integer voltageMultiplier) {
        this.voltageMultiplier = voltageMultiplier;
    }

    public Integer getCurrentMultiplier() {
        return currentMultiplier;
    }

    public void setCurrentMultiplier(Integer currentMultiplier) {
        this.currentMultiplier = currentMultiplier;
    }

    public Integer getCapacityMode() {
        return capacityMode;
    }

    public void setCapacityMode(Integer capacityMode) {
        this.capacityMode = capacityMode;
    }

    public Integer getDesignCapacity() {
        return designCapacity;
    }

    public void setDesignCapacity(Integer designCapacity) {
        this.designCapacity = designCapacity;
    }

    public Integer getDesignVoltage() {
        return designVoltage;
    }

    public void setDesignVoltage(Integer designVoltage) {
        this.designVoltage = designVoltage;
    }

    public Integer getMfgDay() {
        return mfgDay;
    }

    public void setMfgDay(Integer mfgDay) {
        this.mfgDay = mfgDay;
    }

    public Integer getMfgMonth() {
        return mfgMonth;
    }

    public void setMfgMonth(Integer mfgMonth) {
        this.mfgMonth = mfgMonth;
    }

    public Integer getMfgYear() {
        return mfgYear;
    }

    public void setMfgYear(Integer mfgYear) {
        this.mfgYear = mfgYear;
    }

    public Integer getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(Integer serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Integer getSwVer() {
        return swVer;
    }

    public void setSwVer(Integer swVer) {
        this.swVer = swVer;
    }

    public Integer getDataVer() {
        return dataVer;
    }

    public void setDataVer(Integer dataVer) {
        this.dataVer = dataVer;
    }

    public Integer getMfgAccess() {
        return mfgAccess;
    }

    public void setMfgAccess(Integer mfgAccess) {
        this.mfgAccess = mfgAccess;
    }

    public String getMfgName() {
        return mfgName;
    }

    public void setMfgName(String mfgName) {
        this.mfgName = mfgName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getChemistryName() {
        return chemistryName;
    }

    public void setChemistryName(String chemistryName) {
        this.chemistryName = chemistryName;
    }

    public Integer getMajor() {
        return major;
    }

    public void setMajor(Integer major) {
        this.major = major;
    }

    public Integer getMinor() {
        return minor;
    }

    public void setMinor(Integer minor) {
        this.minor = minor;
    }

    public Integer getBuild() {
        return build;
    }

    public void setBuild(Integer build) {
        this.build = build;
    }

    public String getLdsVer() {
        return ldsVer;
    }

    public void setLdsVer(String ldsVer) {
        this.ldsVer = ldsVer;
    }

    public String getLdsSerial() {
        return ldsSerial;
    }

    public void setLdsSerial(String ldsSerial) {
        this.ldsSerial = ldsSerial;
    }

    public String getLdsCPU() {
        return ldsCPU;
    }

    public void setLdsCPU(String ldsCPU) {
        this.ldsCPU = ldsCPU;
    }

    public String getLdsBuildNum() {
        return ldsBuildNum;
    }

    public void setLdsBuildNum(String ldsBuildNum) {
        this.ldsBuildNum = ldsBuildNum;
    }

    public Integer getBootLoaderVersion() {
        return bootLoaderVersion;
    }

    public void setBootLoaderVersion(Integer bootLoaderVersion) {
        this.bootLoaderVersion = bootLoaderVersion;
    }

    public Integer getUiBoardSWVer() {
        return uiBoardSWVer;
    }

    public void setUiBoardSWVer(Integer uiBoardSWVer) {
        this.uiBoardSWVer = uiBoardSWVer;
    }

    public Integer getUiBoardHWVer() {
        return uiBoardHWVer;
    }

    public void setUiBoardHWVer(Integer uiBoardHWVer) {
        this.uiBoardHWVer = uiBoardHWVer;
    }

    public Integer getQaState() {
        return qaState;
    }

    public void setQaState(Integer qaState) {
        this.qaState = qaState;
    }

    public Integer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Integer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getDriverVersion() {
        return driverVersion;
    }

    public void setDriverVersion(Integer driverVersion) {
        this.driverVersion = driverVersion;
    }

    public Integer getDriverID() {
        return driverID;
    }

    public void setDriverID(Integer driverID) {
        this.driverID = driverID;
    }

    public Integer getUltrasonicSW() {
        return ultrasonicSW;
    }

    public void setUltrasonicSW(Integer ultrasonicSW) {
        this.ultrasonicSW = ultrasonicSW;
    }

    public Integer getUltrasonicHW() {
        return ultrasonicHW;
    }

    public void setUltrasonicHW(Integer ultrasonicHW) {
        this.ultrasonicHW = ultrasonicHW;
    }

    public Integer getBlowerHW() {
        return blowerHW;
    }

    public void setBlowerHW(Integer blowerHW) {
        this.blowerHW = blowerHW;
    }

    public Integer getBlowerSWMajor() {
        return blowerSWMajor;
    }

    public void setBlowerSWMajor(Integer blowerSWMajor) {
        this.blowerSWMajor = blowerSWMajor;
    }

    public Integer getBlowerSWMinor() {
        return blowerSWMinor;
    }

    public void setBlowerSWMinor(Integer blowerSWMinor) {
        this.blowerSWMinor = blowerSWMinor;
    }
}
