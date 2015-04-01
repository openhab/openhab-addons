package org.openhab.binding.openwebnetvdes.configuration;

public class Ip2WireBridgeConfiguration {

	/** The IP address of the IP Interface (LAN-2Wire gateway) */
	public String ipAddress;

	/**
	 * The port of the IP Interface (LAN-2Wire gateway)
	 */
	public Integer port;

	/** The refresh interval in ms which is used to poll given IP Interface (LAN-2Wire gateway) */
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

