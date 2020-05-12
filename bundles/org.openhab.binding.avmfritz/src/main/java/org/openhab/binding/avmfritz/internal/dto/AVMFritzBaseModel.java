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
 */
package org.openhab.binding.avmfritz.internal.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * See {@link DeviceListModel}.
 *
 * In the functionbitmask element value the following bits are used:
 *
 * <ol>
 * <li>Bit 0: HAN-FUN Gerät</li>
 * <li>Bit 3: Button</li>
 * <li>Bit 4: Alarm-Sensor</li>
 * <li>Bit 6: Comet DECT, Heizkostenregler</li>
 * <li>Bit 7: Energie Messgerät</li>
 * <li>Bit 8: Temperatursensor</li>
 * <li>Bit 9: Schaltsteckdose</li>
 * <li>Bit 10: AVM DECT Repeater</li>
 * <li>Bit 11: Mikrofon</li>
 * <li>Bit 13: HAN-FUN Unit</li>
 * </ol>
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet DECT
 * @author Christoph Weitkamp - Added support for groups
 */
public abstract class AVMFritzBaseModel {
    protected static final int HAN_FUN_DEVICE_BIT = 1;
    protected static final int HAN_FUN_BUTTON_BIT = 8;
    protected static final int HAN_FUN_ALARM_SENSOR_BIT = 16;
    protected static final int HEATING_THERMOSTAT_BIT = 64;
    protected static final int POWERMETER_BIT = 128;
    protected static final int TEMPSENSOR_BIT = 256;
    protected static final int OUTLET_BIT = 512;
    protected static final int DECT_REPEATER_BIT = 1024;
    protected static final int MICROPHONE_BIT = 2048;
    protected static final int HAN_FUN_UNIT_BIT = 8192;

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

    public boolean isButton() {
        return (bitmask & HAN_FUN_BUTTON_BIT) > 0;
    }

    public boolean isAlarmSensor() {
        return (bitmask & HAN_FUN_ALARM_SENSOR_BIT) > 0;
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

    @Override
    public String toString() {
        return new StringBuilder().append("[ain=").append(ident).append(",bitmask=").append(bitmask)
                .append(",isHANFUNDevice=").append(isHANFUNDevice()).append(",isButton=").append(isButton())
                .append(",isAlarmSensor=").append(isAlarmSensor()).append(",isSwitchableOutlet=")
                .append(isSwitchableOutlet()).append(",isTempSensor=").append(isTempSensor()).append(",isPowermeter=")
                .append(isPowermeter()).append(",isDectRepeater=").append(isDectRepeater())
                .append(",isHeatingThermostat=").append(isHeatingThermostat()).append(",isMicrophone=")
                .append(isMicrophone()).append(",isHANFUNUnit=").append(isHANFUNUnit()).append(",id=").append(deviceId)
                .append(",manufacturer=").append(deviceManufacturer).append(",productname=").append(productName)
                .append(",fwversion=").append(firmwareVersion).append(",present=").append(present).append(",name=")
                .append(name).append(getSwitch()).append(getPowermeter()).append(getHkr()).append("]").toString();
    }
}
