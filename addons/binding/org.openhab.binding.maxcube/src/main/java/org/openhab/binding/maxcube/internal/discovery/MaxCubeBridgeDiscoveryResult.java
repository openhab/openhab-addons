package org.openhab.binding.maxcube.internal.discovery;
/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */


/**
 * Class for {@link MaxCubeBinding} MAX! Cube discoveries
 * 
 * @author Marcel Verpaalen - Initial contribution
 */

public class MaxCubeBridgeDiscoveryResult {

	private String ipAddress;
	private String serialNumber;
	private String rfAddress;
	private String friendlyName;
	
	public MaxCubeBridgeDiscoveryResult (String ipAddress,String serialNumber, String rfAddress,String friendlyName){
		this.ipAddress = ipAddress;
		this.serialNumber = serialNumber;
		this.rfAddress=rfAddress;
		this.friendlyName = friendlyName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public String getRfAddress() {
		return rfAddress;
	}

	public String getFriendlyName() {
		return friendlyName;
	}
  
}
