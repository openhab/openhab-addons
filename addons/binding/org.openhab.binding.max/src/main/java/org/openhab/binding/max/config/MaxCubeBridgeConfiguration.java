/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.config;

import org.openhab.binding.max.MaxBinding;

/**
 * Configuration class for {@link MaxBinding} bridge used to connect to the
 * maxCube device.
 * 
 * @author Marcel Verpaalen - Initial contribution
 */

public class MaxCubeBridgeConfiguration {

	/** The IP address of the MAX!Cube LAN gateway */
	public String ipAddress;

	/**
	 * The port of the MAX! Cube LAN gateway as provided at
	 * http://www.elv.de/controller.aspx?cid=824&detail=10&detail2=3484
	 */
	public Integer port;

	/** The refresh interval in seconds which is used to poll given MAX! Cube */
	public Integer refreshInterval;

	/** The unique serial number for a device */
	public String serialNumber;

	/**
	 * If set to true, the binding will leave the connection to the cube open
	 * and just request new informations. This allows much higher poll rates and
	 * causes less load than the non-exclusive polling but has the drawback that
	 * no other apps (i.E. original software) can use the cube while this
	 * binding is running.
	 */
	public boolean exclusive = false;

	/**
	 * in exclusive mode, how many requests are allowed until connection is
	 * closed and reopened
	 */
	public Integer maxRequestsPerConnection;
}
