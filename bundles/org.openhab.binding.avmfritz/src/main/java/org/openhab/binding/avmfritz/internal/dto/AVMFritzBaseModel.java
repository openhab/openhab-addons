/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.dto;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.eclipse.jdt.annotation.Nullable;

/**
 * See {@link DeviceListModel}.
 *
 * In the functionbitmask element value the following bits are used:
 *
 * <ol>
 * <li>Bit 0: HAN-FUN Gerät</li>
 * <li>Bit 2: Licht/Lampe</li>
 * <li>Bit 3: HAN-FUN Button - undocumented</li>
 * <li>Bit 4: Alarm-Sensor</li>
 * <li>Bit 5: AVM-Button</li>
 * <li>Bit 6: Comet DECT, Heizkörperregler</li>
 * <li>Bit 7: Energie Messgerät</li>
 * <li>Bit 8: Temperatursensor</li>
 * <li>Bit 9: Schaltsteckdose</li>
 * <li>Bit 10: AVM DECT Repeater</li>
 * <li>Bit 11: Mikrofon</li>
 * <li>Bit 13: HAN-FUN Unit</li>
 * <li>Bit 15: an-/ausschaltbares Gerät / Steckdose / Lampe / Aktor</li>
 * <li>Bit 16: Gerät mit einstellbarem Dimm-, Höhen- bzw. Niveau-Level</li>
 * <li>Bit 17: Lampe mit einstellbarer Farbe/Farbtemperatur</li>
 * <li>Bit 18: Rollladen - hoch, runter, stop und level 0% bis 100 %</li>
 * </ol>
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet DECT
 * @author Christoph Weitkamp - Added support for groups
 * @author Ulrich Mertin - Added support for HAN-FUN blinds
 */
public abstract class AVMFritzBaseModel implements BatteryModel {
    protected static final int HAN_FUN_DEVICE_BIT = 1; // Bit 0
    protected static final int LIGHT_BIT = 1 << 2; // Bit 2
    protected static final int HAN_FUN_BUTTON_BIT = 1 << 3; // Bit 3 - undocumented
    protected static final int HAN_FUN_ALARM_SENSOR_BIT = 1 << 4; // Bit 4
    protected static final int BUTTON_BIT = 1 << 5; // Bit 5
    protected static final int HEATING_THERMOSTAT_BIT = 1 << 6; // Bit 6
    protected static final int POWERMETER_BIT = 1 << 7; // Bit 7
    protected static final int TEMPERATURE_SENSOR_BIT = 1 << 8; // Bit 8
    protected static final int OUTLET_BIT = 1 << 9; // Bit 9
    protected static final int DECT_REPEATER_BIT = 1 << 10; // Bit 10
    protected static final int MICROPHONE_BIT = 1 << 11; // Bit 11
    protected static final int HAN_FUN_UNIT_BIT = 1 << 13; // Bit 13
    protected static final int HAN_FUN_ON_OFF_BIT = 1 << 15; // Bit 15
    protected static final int DIMMABLE_LIGHT_BIT = 1 << 16; // Bit 16
    protected static final int COLOR_LIGHT_BIT = 1 << 17; // Bit 17
    protected static final int HAN_FUN_BLINDS_BIT = 1 << 18; // Bit 18
    protected static final int HUMIDITY_SENSOR_BIT = 1 << 20; // Bit 20 - undocumented

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

    @XmlElement(name = "simpleonoff")
    private @Nullable SimpleOnOffModel simpleOnOffUnit;

    @XmlElement(name = "powermeter")
    private PowerMeterModel powermeterModel;

    @XmlElement(name = "hkr")
    private HeatingModel heatingModel;

    public @Nullable SimpleOnOffModel getSimpleOnOffUnit() {
        return simpleOnOffUnit;
    }

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

    public boolean isHANFUNAlarmSensor() {
        return (bitmask & HAN_FUN_ALARM_SENSOR_BIT) > 0;
    }

    public boolean isButton() {
        return (bitmask & BUTTON_BIT) > 0;
    }

    public boolean isSwitchableOutlet() {
        return (bitmask & OUTLET_BIT) > 0;
    }

    public boolean isTemperatureSensor() {
        return (bitmask & TEMPERATURE_SENSOR_BIT) > 0;
    }

    public boolean isHumiditySensor() {
        return (bitmask & HUMIDITY_SENSOR_BIT) > 0;
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

    public boolean hasMicrophone() {
        return (bitmask & MICROPHONE_BIT) > 0;
    }

    public boolean isHANFUNUnit() {
        return (bitmask & HAN_FUN_UNIT_BIT) > 0;
    }

    public boolean isHANFUNOnOff() {
        return (bitmask / HAN_FUN_ON_OFF_BIT) > 0;
    }

    public boolean isDimmableLight() {
        return (bitmask & DIMMABLE_LIGHT_BIT) > 0;
    }

    public boolean isColorLight() {
        return (bitmask & COLOR_LIGHT_BIT) > 0;
    }

    public boolean isHANFUNBlinds() {
        return (bitmask & HAN_FUN_BLINDS_BIT) > 0;
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

    @Override
    public BigDecimal getBattery() {
        return battery;
    }

    @Override
    public BigDecimal getBatterylow() {
        return batterylow;
    }

    @Override
    public String toString() {
        return new StringBuilder("[ain=").append(ident).append(",bitmask=").append(bitmask).append(",isHANFUNDevice=")
                .append(isHANFUNDevice()).append(",isHANFUNButton=").append(isHANFUNButton())
                .append(",isHANFUNAlarmSensor=").append(isHANFUNAlarmSensor()).append(",isButton=").append(isButton())
                .append(",isSwitchableOutlet=").append(isSwitchableOutlet()).append(",isTemperatureSensor=")
                .append(isTemperatureSensor()).append(",isHumiditySensor=").append(isHumiditySensor())
                .append(",isPowermeter=").append(isPowermeter()).append(",isDectRepeater=").append(isDectRepeater())
                .append(",isHeatingThermostat=").append(isHeatingThermostat()).append(",hasMicrophone=")
                .append(hasMicrophone()).append(",isHANFUNUnit=").append(isHANFUNUnit()).append(",isHANFUNOnOff=")
                .append(isHANFUNOnOff()).append(",isDimmableLight=").append(isDimmableLight()).append(",isColorLight=")
                .append(isColorLight()).append(",isHANFUNBlind=").append(isHANFUNBlinds()).append(",id=")
                .append(deviceId).append(",manufacturer=").append(deviceManufacturer).append(",productname=")
                .append(productName).append(",fwversion=").append(firmwareVersion).append(",present=").append(present)
                .append(",name=").append(name).append(",battery=").append(getBattery()).append(",batterylow=")
                .append(getBatterylow()).append(",").append(getSwitch()).append(",").append(getSimpleOnOffUnit())
                .append(",").append(getPowermeter()).append(",").append(getHkr()).append(",").toString();
    }
}
