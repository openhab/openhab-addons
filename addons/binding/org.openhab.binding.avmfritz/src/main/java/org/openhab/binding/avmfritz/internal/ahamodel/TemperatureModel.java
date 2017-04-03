/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link DevicelistModel}.
 * 
 * @author Robert Bausdorf
 * 
 * 
 */
@XmlRootElement(name = "temperature")
@XmlType(propOrder = { "celsius", "offset" })
public class TemperatureModel {
	public static final BigDecimal TEMP_FACTOR = new BigDecimal("0.1");
	
	private BigDecimal celsius;
	private BigDecimal offset;

	public BigDecimal getCelsius() {
		return celsius != null ? celsius.multiply(TEMP_FACTOR) : BigDecimal.ZERO;
	}

	public void setCelsius(BigDecimal celsius) {
		this.celsius = celsius;
	}

	public BigDecimal getOffset() {
		return offset != null ? offset.multiply(TEMP_FACTOR) : BigDecimal.ZERO;
	}

	public void setOffset(BigDecimal offset) {
		this.offset = offset;
	}

	public String toString() {
		return new ToStringBuilder(this)
		.append("celsius", this.getCelsius())
		.append("offset", this.getOffset())
		.toString();
	}
}
