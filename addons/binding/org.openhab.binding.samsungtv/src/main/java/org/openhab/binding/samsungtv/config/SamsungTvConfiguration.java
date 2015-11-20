/**
 * Copyright (c) 2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungtv.config;

/**
 * Configuration class for {@link SamsungTvBinding} device.
 * 
 * @author Pauli Anttila - Initial contribution
 */
public class SamsungTvConfiguration {
	public static final String HOST_NAME = "hostName";
	public static final String PORT = "port";
	public static final String REFRESH_INTERVAL = "refreshInterval";
	
	public String hostName;
	public int port;
	public int refreshInterval;
	
}
