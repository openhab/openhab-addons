package org.openhab.binding.maxcube.config;
/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */



/**
 * Configuration class for {@link MaxCubeBinding} bridge
 * used to connect to the maxCube device.
 * 
 * @author Marcel Verpaalen - Initial contribution
 */

public class MaxCubeBridgeConfiguration {


	public static final String IP_ADDRESS = "ipAddress";
	public static final String PORT = "port";
	public static final String REFRESH_INTERVAL = "refreshInterval";
	

	/** The IP address of the MAX!Cube LAN gateway */
	public String ipAddress;
    
    /**
	 * The port of the MAX!Cube LAN gateway as provided at
	 * http://www.elv.de/controller.aspx?cid=824&detail=10&detail2=3484
	 */
	public int port = (int) 62910;
	
	/** The refresh interval in ms which is used to poll given MAX!Cube */
	public Long refreshInterval =(long) 60000;
	
	/** The unique serial number for a device */
	public String serialNumber;

}
