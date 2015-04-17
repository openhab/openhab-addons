/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.protocol;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * @author Thomas Eichstaedt-Engelen (innoQ)
 * @since 2.0.0
 */
public class MoxMessage {
	
	private String hexString;
	
	/** always 0x2 in our application */
	private int priority;
	/** this is a unique number assigned to a LTCOP device in the home system */
	private int oid;
	/** this is the channel information of a LT COP device */
	private int suboid;
	
	private int functionCode;
	private int subFunctionCode;
	
	private MoxStatusCode statusCode;
	private MoxCommandCode commandCode;
	private BigDecimal value;
	
	private int dimmerTime;
	private String eventName;
	
	private static final int MAX_SCALE = 8;
	

	public MoxMessage() {
	}

	
	public String getId() {
		return commandCode == null ?
			oid + ":" + suboid :
			oid + ":" + suboid + ':' + commandCode.name();
	}

	public int getOid() {
		return oid;
	}

	public void setOid(int oid) {
		this.oid = oid;
	}

	public int getSuboid() {
		return suboid;
	}

	public void setSuboid(int suboid) {
		this.suboid = suboid;
	}
	
	
	public String getHexString() {
		return hexString;
	}

	public void setHexString(String hexString) {
		this.hexString = hexString;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	
	public int getFunctionCode() {
		return functionCode;
	}

	public void setFunctionCode(int functionCode) {
		this.functionCode = functionCode;
	}

	public int getSubFunctionCode() {
		return subFunctionCode;
	}

	public void setSubFunctionCode(int subFunctionCode) {
		this.subFunctionCode = subFunctionCode;
	}

	public MoxCommandCode getCommandCode() {
		return commandCode;
	}

	public void setCommandCode(MoxCommandCode commandCode) {
		this.commandCode = commandCode;
	}
	
	public MoxStatusCode getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(MoxStatusCode statusCode) {
		this.statusCode = statusCode;
	}

	public BigDecimal getValue() {
		return value.setScale(MAX_SCALE, RoundingMode.HALF_UP);
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public void setValue(Integer value) {
		this.setValue(new BigDecimal(value));
	}
	
	public int getDimmerTime() {
		return dimmerTime;
	}

	public void setDimmerTime(int dimmerTime) {
		this.dimmerTime = dimmerTime;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	
	public String toStringForTrace() {
		String type = statusCode!=null ? "status" : commandCode!=null ? "command" : "UNKNOWN!";
		return "MoxMessage [type=" + type + ", hexString=" + hexString + "\t, priority=" + priority
				+ ", oid=" + oid + ", suboid=" + suboid + ", subFunctionCode="
				+ subFunctionCode + ", functionCode=" + functionCode + ", value=" + value.setScale(4, BigDecimal.ROUND_HALF_UP);
	}
	
	@Override
	public String toString() {
		return "MoxMessage {" +
				"eventName='" + eventName + '\'' +
				", oid=" + oid +
				", subFunction=" + subFunctionCode +
				", suboid=" + suboid +
				", value=" + value +
				'}';
	}
	

}
