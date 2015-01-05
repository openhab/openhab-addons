/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.networkhealth.discovery;

import static org.openhab.binding.networkhealth.NetworkHealthBindingConstants.*;

import java.io.IOException;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.io.net.actions.Ping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkHealthDiscoveryService} is responsible for discovering devices on 
 * the current Network. It uses every Network Interface which is connect to a Network
 * 
 * @author Marc Mettke - Initial contribution
 */

public class NetworkHealthDiscoveryService extends AbstractDiscoveryService {
	private final static int TASK_CREATING_TIME_IN_MS = 1;
	private final static Object lockObject = new Object();

	private ScheduledFuture<?> discoveryJob;
	private final Logger logger = LoggerFactory.getLogger(NetworkHealthDiscoveryService.class);

	
	public NetworkHealthDiscoveryService() {
		super(SUPPORTED_THING_TYPES_UIDS, 300);
	}

	/**
	 * Handles the whole Discovery
	 */
	private void discoverNetwork() {
		TreeSet<String> interfaceIPs;
		Queue<String> networkIPs;

		logger.debug("Starting Device Discovery");
		interfaceIPs = getInterfaceIPs();
		networkIPs = getNetworkIPs(interfaceIPs);
		startDiscovery(networkIPs);
	}

	/**
	 * Gets every IPv4 Address on each Interface except the loopback
	 * The Address format is ip/subnet
	 * @return The collected IPv4 Addresses
	 */
	private TreeSet<String> getInterfaceIPs() {
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
	private Queue<String> getNetworkIPs(TreeSet<String> interfaceIPs) {
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

	public Set<ThingTypeUID> getSupportedThingTypes() {
		return SUPPORTED_THING_TYPES_UIDS;
	}

	@Override
	protected void startBackgroundDiscovery() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				/* Devices are only discovered on users request */
			}
		}).start();
	}
	
	/**
	 * Starts the DiscoveryThread for each IP on the Networks
	 * @param allNetworkIPs
	 */
	private void startDiscovery(final Queue<String> networkIPs) {
		
		Runnable runnable = new Runnable() {
			public void run() {
				DiscoveryThread discoveryThread = null;
				DiscoveryThreadResult discoveryThreadResult = new DiscoveryThreadResult() {
					@Override
					public void newDevice(String ip) {
						submitDiscoveryResults(ip);
					}
				};
				
				// ensures that only one thread at  a time access the queue
				synchronized (lockObject) {
					if( networkIPs.isEmpty()) {
						discoveryJob.cancel(false);
					} else {
						discoveryThread = new DiscoveryThread(networkIPs.remove(), discoveryThreadResult);						
					}
				}
				
				if( discoveryThread != null )
					discoveryThread.start();
			}
		};
		
		/* Every milisecond a new thread will be created. Due to the fact that the PING has a timeout of 1 sec,
		 * only about 1000 Threads will be create at max */
		discoveryJob = scheduler.scheduleAtFixedRate(runnable, 0, TASK_CREATING_TIME_IN_MS, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void startScan() {
		discoverNetwork();
	}

	/**
	 * Submit the discovered Devices to the Smarthome inbox,
	 * 
	 * @param ip The Device IP
	 */
	private void submitDiscoveryResults(String ip) {

		// uid must not contains dots
		ThingUID uid = new ThingUID(THING_TYPE_DEVICE, ip.replace('.', '_') ); 	
		
		if(uid!=null) { 
			Map<String, Object> properties = new HashMap<>(1); 
			properties.put(PARAMETER_HOSTNAME ,ip);
			DiscoveryResult result = DiscoveryResultBuilder.create(uid)
					.withProperties(properties)
					.withLabel("Network Device (" + ip +")").build();
			thingDiscovered(result); 
		}
		 
	}

}

/**
 * Runs a Ping in its own Thread
 * @author Marc Mettke - Initial contribution
 */
class DiscoveryThread extends Thread {
	private final static int PING_TIMEOUT_IN_MS = 1000;
	
	private DiscoveryThreadResult discoveryResult;
	private String ip;

	public DiscoveryThread(String ip, DiscoveryThreadResult discoveryResult) {
		this.ip = ip;
		this.discoveryResult = discoveryResult;
	}

	@Override
	public void run() {
		try {
			if( Ping.checkVitality(this.ip, 0, PING_TIMEOUT_IN_MS) ) {
				this.discoveryResult.newDevice(this.ip);
			}
		} 
		catch (SocketTimeoutException se) {
		}
		catch (IOException ioe) {
		}
	}
}

/**
 * Callback for a new Device to be committed to Homematic
 * @author Marc Mettke - Initial contribution
 */
interface DiscoveryThreadResult {
	public void newDevice(String ip);
}
