/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link DevicelistModel}.
 *
 * In the functionbitmask element value the following bits are used:
 *
 * <ol>
 * <li>Bit 4: Alarm-Sensor</li>
 * <li>Bit 6: Comet DECT, Heizkostenregler</li>
 * <li>Bit 7: Energie Messgerät</li>
 * <li>Bit 8: Temperatursensor</li>
 * <li>Bit 9: Schaltsteckdose</li>
 * <li>Bit 10: AVM DECT Repeater</li>
 * </ol>
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 * @author Christoph Weitkamp - Added support for groups
 */
public abstract class AVMFritzBaseModel {

    public static final int ALARM_SENSOR_BIT = 16;
    public static final int HEATING_THERMOSTAT_BIT = 64;
    public static final int POWERMETER_BIT = 128;
    public static final int TEMPSENSOR_BIT = 256;
    public static final int SWITCH_BIT = 512;
    public static final int DECT_REPEATER_BIT = 1024;

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

    public void setHkr(HeatingModel heating) {
        this.heatingModel = heating;
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

    public boolean isSwitchableOutlet() {
        return (bitmask & DeviceModel.SWITCH_BIT) > 0;
    }

    public boolean isTempSensor() {
        return (bitmask & DeviceModel.TEMPSENSOR_BIT) > 0;
    }

    public boolean isPowermeter() {
        return (bitmask & DeviceModel.POWERMETER_BIT) > 0;
    }

    public boolean isDectRepeater() {
        return (bitmask & DeviceModel.DECT_REPEATER_BIT) > 0;
    }

    public boolean isHeatingThermostat() {
        return (bitmask & DeviceModel.HEATING_THERMOSTAT_BIT) > 0;
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
        return new ToStringBuilder(this).append("ain", getIdentifier()).append("bitmask", bitmask)
                .append("isDectRepeater", isDectRepeater()).append("isPowermeter", isPowermeter())
                .append("isTempSensor", isTempSensor()).append("isSwitchableOutlet", isSwitchableOutlet())
                .append("isHeatingThermostat", isHeatingThermostat()).append("id", getDeviceId())
                .append("manufacturer", getManufacturer()).append("productname", getProductName())
                .append("fwversion", getFirmwareVersion()).append("present", getPresent()).append("name", getName())
                .append(getSwitch()).append(getPowermeter()).append(getHkr()).toString();
    }
}
