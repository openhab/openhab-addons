/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.service;

import java.io.IOException;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.smarthome.io.net.actions.Ping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkService} handles the connection to the Device
 * 
 * @author Marc Mettke
 */
public class NetworkService {
	private final static Object lockObject = new Object();
	private final static int TASK_CREATING_TIME_IN_MS = 1;
	
	private static ScheduledFuture<?> discoveryJob;
	private static Logger logger = LoggerFactory.getLogger(NetworkService.class);
	
	private String hostname;
	private int port;
	private int retry;
	private long refreshInterval;
	private int timeout;
	private boolean useSystemPing;

	
	public NetworkService() {
		this("", 0, 1, 60000, 5000, false);
	}

	public NetworkService(String hostname, int port, int retry,
			long refreshInterval, int timeout, boolean useSystemPing) {
		super();
		this.hostname = hostname;
		this.port = port;
		this.retry = retry;
		this.refreshInterval = refreshInterval;
		this.timeout = timeout;
		this.useSystemPing = useSystemPing;
	}

	
	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public int getRetry() {
		return retry;
	}
	
	public long getRefreshInterval() {
		return refreshInterval;
	}

	public int getTimeout() {
		return timeout;
	}

	public boolean isUseSystemPing() {
		return useSystemPing;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public void setRetry(int retry) {
		this.retry = retry;
	}

	public void setRefreshInterval(long refreshInterval) {
		this.refreshInterval = refreshInterval;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setUseSystemPing(boolean useSystemPing) {
		this.useSystemPing = useSystemPing;
	}

	/**
	 * Updates one device to a new status
	 */
	public boolean updateDeviceState() throws InvalidConfigurationException {
		int currentTry = 0;
		boolean result;
		
		do {
			result = updateDeviceState(getHostname(), getPort(), getTimeout(), isUseSystemPing());		
			currentTry++;
		} while( !result && currentTry < this.retry);
		
		return result;
	}
	
	/**
	 * Try's to reach the Device by Ping
	 */
	private static boolean updateDeviceState(String hostname, int port, int timeout, boolean useSystemPing) throws InvalidConfigurationException {
		boolean success = false;
		
		try {
			if( !useSystemPing ) {
				success = Ping.checkVitality(hostname, port, timeout);				
			} else {
				Process proc;
				if( SystemUtils.IS_OS_UNIX ) {
					proc = new ProcessBuilder("ping", "-t", String.valueOf((int)(timeout / 1000)), "-c", "1", hostname).start();
				} else if( SystemUtils.IS_OS_WINDOWS) {
					proc = new ProcessBuilder("ping", "-w", String.valueOf(timeout), "-n", "1", hostname).start();
				} else {
					logger.error("The System Ping is not supported on this Operating System");
					throw new InvalidConfigurationException("System Ping not supported");
				}
				
				int exitValue = proc.waitFor();
				success = exitValue == 0;
				if( exitValue != 2 && exitValue != 0 ) {
					logger.debug("Ping stopped with Error Number: " + exitValue + 
							" on Command :" + "ping" + 
							(SystemUtils.IS_OS_UNIX ? " -t " : " -w ") + 
							String.valueOf(timeout) + 
							(SystemUtils.IS_OS_UNIX ? " -c" : " -n") + 
							" 1 " + hostname);
					return false;
				}
			}
			
			logger.debug("established connection [host '{}' port '{}' timeout '{}']", new Object[] {hostname, port, timeout});
		} 
		catch (SocketTimeoutException se) {
			logger.debug("timed out while connecting to host '{}' port '{}' timeout '{}'", new Object[] {hostname, port, timeout});
		}
		catch (IOException ioe) {
			logger.debug("couldn't establish network connection [host '{}' port '{}' timeout '{}']", new Object[] {hostname, port, timeout});
		} catch (InterruptedException e) {
			logger.debug("ping program was interrupted");
		}
		
		return success ? true : false;
		
	}
	
	/**
	 * Handles the whole Discovery
	 */
	public static void discoverNetwork(DiscoveryCallback discoveryCallback, ScheduledExecutorService scheduledExecutorService) {
		TreeSet<String> interfaceIPs;
		Queue<String> networkIPs;

		logger.debug("Starting Device Discovery");
		interfaceIPs = getInterfaceIPs();
		networkIPs = getNetworkIPs(interfaceIPs);
		startDiscovery(networkIPs, discoveryCallback, scheduledExecutorService);
	}
	
	/**
	 * Gets every IPv4 Address on each Interface except the loopback
	 * The Address format is ip/subnet
	 * @return The collected IPv4 Addresses
	 */
	private static TreeSet<String> getInterfaceIPs() {
		TreeSet<String> interfaceIPs = new TreeSet<String>();

		try {
			// For each interface ...
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface networkInterface = en.nextElement();
				if (!networkInterface.isLoopback()) {

					// .. and for each address ...
					for (Iterator<InterfaceAddress> it = networkInterface
							.getInterfaceAddresses().iterator(); it.hasNext();) {
						
						// ... get IP and Subnet 
						InterfaceAddress interfaceAddress = it.next();
						interfaceIPs.add(interfaceAddress.getAddress()
								.getHostAddress()
								+ "/"
								+ interfaceAddress.getNetworkPrefixLength());
					}
				}
			}
		} catch (SocketException e) {
		}

		return interfaceIPs;
	}
	
	/**
	 * Takes the interfaceIPs and fetches every IP which can be assigned on their network
	 * @param networkIPs The IPs which are assigned to the Network Interfaces
	 * @return Every single IP which can be assigned on the Networks the computer is connected to
	 */
	private static Queue<String> getNetworkIPs(TreeSet<String> interfaceIPs) {
		Queue<String> networkIPs = new LinkedBlockingQueue<String>();

		for (Iterator<String> it = interfaceIPs.iterator(); it.hasNext();) {
			try {
				// gets every ip which can be assigned on the given network
				SubnetUtils utils = new SubnetUtils(it.next());
				String[] addresses = utils.getInfo().getAllAddresses();
				for (int i = 0; i < addresses.length; i++) {
					networkIPs.add(addresses[i]);
				}

			} catch (Exception ex) {
			}
		}

		return networkIPs;
	}
	
	/**
	 * Starts the DiscoveryThread for each IP on the Networks
	 * @param allNetworkIPs
	 */
	private static void startDiscovery(final Queue<String> networkIPs, final DiscoveryCallback discoveryCallback, ScheduledExecutorService scheduledExecutorService) {
		
		Runnable runnable = new Runnable() {
			public void run() {
				DiscoveryThread discoveryThread = null;
				
				// ensures that only one thread at  a time access the queue
				synchronized (lockObject) {
					if( networkIPs.isEmpty()) {
						discoveryJob.cancel(false);
					} else {
						discoveryThread = new DiscoveryThread(networkIPs.remove(), discoveryCallback);						
					}
				}
				
				if( discoveryThread != null )
					discoveryThread.start();
			}
		};
		
		/* Every milisecond a new thread will be created. Due to the fact that the PING has a timeout of 1 sec,
		 * only about 1000 Threads will be create at max */
		discoveryJob = scheduledExecutorService.scheduleAtFixedRate(runnable, 0, TASK_CREATING_TIME_IN_MS, TimeUnit.MILLISECONDS);
	}
}
