package org.openhab.binding.maxcube.config;
/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */



/**
 * Configuration class for {@link MaxCubeBinding} 
 * used to connect to the maxCube device.
 * 
 * @author Marcel Verpaalen - Initial contribution
 */

public class MaxCubeConfiguration {

	public static final String SERIAL_NUMBER = "serialNumber";
	public static final String RFADDRESS = "rfAddress";
	public static final String FRIENDLY_NAME = "friendlyName";


	/** The unique serial number for a device */
	public String serialNumber;
    
  
}
