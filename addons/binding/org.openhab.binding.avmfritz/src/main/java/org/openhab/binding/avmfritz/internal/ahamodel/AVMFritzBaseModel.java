/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.ahamodel;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link DeviceListModel}.
 *
 * In the functionbitmask element value the following bits are used:
 *
 * <ol>
 *     <li>Bit 0: HAN-FUN Gerät</li>
 *     <li>Bit 4: Alarm-Sensor</li>
 *     <li>Bit 6: Comet DECT, Heizkörperregler</li>
 *     <li>Bit 7: Energie Messgerät</li>
 *     <li>Bit 8: Temperatursensor</li>
 *     <li>Bit 9: Schaltsteckdose</li>
 *     <li>Bit 10: AVM DECT Repeater</li>
 *     <li>Bit 11: Mikrofon</li>
 *     <li>Bit 13: HAN-FUN Unit</li>
 * </ol>
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet DECT
 * @author Christoph Weitkamp - Added support for groups
 */
public abstract class AVMFritzBaseModel {
    protected static final int HAN_FUN_DEVICE_BIT = 1; // Bit 0
    protected static final int HAN_FUN_BUTTON_BIT = 8; // Bit 3 -- undocumented
    protected static final int HAN_FUN_ALARM_SENSOR_BIT = 16; // Bit 4
    protected static final int BUTTON_BIT = 32; // Bit 5 - undocumented
    protected static final int HEATING_THERMOSTAT_BIT = 64; // Bit 6
    protected static final int POWERMETER_BIT = 128; // Bit 7
    protected static final int TEMPSENSOR_BIT = 256; // Bit 8
    protected static final int OUTLET_BIT = 512; // Bit 9
    protected static final int DECT_REPEATER_BIT = 1024; // Bit 10
    protected static final int MICROPHONE_BIT = 2048; // Bit 11
    protected static final int HAN_FUN_UNIT_BIT = 8192; // Bit 13

    @XmlAttribute(name = "identifier")
    private String ident;

    @XmlAttribute(name = "id")
    private String deviceId;

    @XmlAttribute(name = "functionbitmask")
    private int bitmask;

    @XmlAttribute(name = "fwversion")
    private String firmwareVersion;

    @XmlAttribute(name = "manufacturer")
    private String deviceManufacturer;

    @XmlAttribute(name = "productname")
    private String productName;

    @XmlElement(name = "present")
    private Integer present;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "battery")
    private BigDecimal battery;

    @XmlElement(name = "batterylow")
    private BigDecimal batterylow;

    @XmlElement(name = "switch")
    private SwitchModel switchModel;

    @XmlElement(name = "powermeter")
    private PowerMeterModel powermeterModel;

    @XmlElement(name = "hkr")
    private HeatingModel heatingModel;

    public PowerMeterModel getPowermeter() {
        return powermeterModel;
    }

    public void setPowermeter(PowerMeterModel powermeter) {
        this.powermeterModel = powermeter;
    }

    public HeatingModel getHkr() {
        return heatingModel;
    }

    public void setHkr(HeatingModel heatingModel) {
        this.heatingModel = heatingModel;
    }

    public SwitchModel getSwitch() {
        return switchModel;
    }

    public void setSwitch(SwitchModel switchModel) {
        this.switchModel = switchModel;
    }

    public String getIdentifier() {
        return ident != null ? ident.replace(" ", "") : null;
    }

    public void setIdentifier(String identifier) {
        this.ident = identifier;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isHANFUNDevice() {
        return (bitmask & HAN_FUN_DEVICE_BIT) > 0;
    }

    public boolean isHANFUNButton() {
        return (bitmask & HAN_FUN_BUTTON_BIT) > 0;
    }

    public boolean isAlarmSensor() {
        return (bitmask & HAN_FUN_ALARM_SENSOR_BIT) > 0;
    }

    public boolean isButton() {
        return (bitmask & BUTTON_BIT) > 0;
    }

    public boolean isSwitchableOutlet() {
        return (bitmask & OUTLET_BIT) > 0;
    }

    public boolean isTempSensor() {
        return (bitmask & TEMPSENSOR_BIT) > 0;
    }

    public boolean isPowermeter() {
        return (bitmask & POWERMETER_BIT) > 0;
    }

    public boolean isDectRepeater() {
        return (bitmask & DECT_REPEATER_BIT) > 0;
    }

    public boolean isHeatingThermostat() {
        return (bitmask & HEATING_THERMOSTAT_BIT) > 0;
    }

    public boolean isMicrophone() {
        return (bitmask & MICROPHONE_BIT) > 0;
    }

    public boolean isHANFUNUnit() {
        return (bitmask & HAN_FUN_UNIT_BIT) > 0;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getManufacturer() {
        return deviceManufacturer;
    }

    public String getProductName() {
        return productName;
    }

    public int getPresent() {
        return present;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBattery() {
        return battery;
    }

    public void setBattery(BigDecimal battery) {
        this.battery = battery;
    }

    public BigDecimal getBatterylow() {
        return batterylow;
    }

    public void setBatterylow(BigDecimal batterylow) {
        this.batterylow = batterylow;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("ain", getIdentifier()).append("bitmask", bitmask)
                .append("isHANFUNDevice", isHANFUNDevice()).append("isHANFUNButton", isHANFUNButton())
                .append("isAlarmSensor", isAlarmSensor()).append("isButton", isButton())
                .append("isSwitchableOutlet", isSwitchableOutlet()).append("isTempSensor", isTempSensor())
                .append("isPowermeter", isPowermeter()).append("isDectRepeater", isDectRepeater())
                .append("isHeatingThermostat", isHeatingThermostat()).append("isMicrophone", isMicrophone())
                .append("isHANFUNUnit", isHANFUNUnit()).append("id", getDeviceId())
                .append("manufacturer", getManufacturer()).append("productname", getProductName())
                .append("fwversion", getFirmwareVersion()).append("present", getPresent()).append("name", getName())
                .append("battery", getBattery()).append("batterylow", getBatterylow()).append(getSwitch())
                .append(getPowermeter()).append(getHkr()).toString();
    }
}
