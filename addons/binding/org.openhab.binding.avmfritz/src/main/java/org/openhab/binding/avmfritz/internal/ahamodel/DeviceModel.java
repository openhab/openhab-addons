/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link DevicelistModel}.
 * 
 * In the functionbitmask element value the following bits are used:
 * 
 * <ol>
 * <li>Bit 7: Energie Messgerät</li>
 * <li>Bit 8: Temperatursensor</li>
 * <li>Bit 9: Schaltsteckdose</li>
 * <li>Bit 10: AVM DECT Repeater</li>
 * </ol>
 * 
 * @author Robert Bausdorf
 * 
 * 
 */
@XmlRootElement(name = "device")
public class DeviceModel {
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
	
	private SwitchModel switchModel;
	
	private PowerMeterModel powermeterModel;

	private TemperatureModel temperatureModel;

	public PowerMeterModel getPowermeter() {
		return powermeterModel;
	}

	public void setPowermeter(PowerMeterModel powermeter) {
		this.powermeterModel = powermeter;
	}

	public TemperatureModel getTemperature() {
		return temperatureModel;
	}

	public void setTemperature(TemperatureModel temperature) {
		this.temperatureModel = temperature;
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

	public String getFirmwareVersion() {
		return firmwareVersion;
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

	public String toString() {
		return new ToStringBuilder(this)
				.append("ain", this.getIdentifier())
				.append("bitmask", this.bitmask)
				.append("isDectRepeater", this.isDectRepeater())
				.append("isPowermeter", this.isPowermeter())
				.append("isTempSensor", this.isTempSensor())
				.append("isSwitchableOutlet", this.isSwitchableOutlet())
				.append("id", this.deviceId)
				.append("manufacturer", this.deviceManufacturer)
				.append("productname", this.getProductName())
				.append("fwversion", this.getFirmwareVersion())
				.append("present", this.present)
				.append("name", this.name)
				.append(this.getSwitch())
				.append(this.getPowermeter())
				.append(this.getTemperature())
				.toString();
	}
}
