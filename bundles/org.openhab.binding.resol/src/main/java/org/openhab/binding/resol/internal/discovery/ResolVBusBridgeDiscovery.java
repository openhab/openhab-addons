/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.resol.internal.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.resol.internal.ResolBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.resol.vbus.TcpDataSource;
import de.resol.vbus.TcpDataSourceProvider;

/**
 * The {@link ResolVBusBridgeDiscovery} class provides the DiscoverySerivce to
 * discover Resol VBus-LAN adapters
 *
 * @author Raphael Mack - Initial contribution
 */
public class ResolVBusBridgeDiscovery extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(ResolVBusBridgeDiscovery.class);

    public ResolVBusBridgeDiscovery() throws IllegalArgumentException {
        super(ResolBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS, 35, false);
    }

    @Override
    protected void startScan() {
        logger.trace("Start discovery of Resol VBus-LAN Adapter");
        scheduler.execute(searchRunnable);
    }

    /*
     * The runnable for the search routine.
     */
    private Runnable searchRunnable = new Runnable() {

        @Override
        public void run() {
            logger.trace("Start adapter discovery ");
            logger.debug("Send broadcast message");

            try {
                InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");

                TcpDataSource[] dataSources = TcpDataSourceProvider.discoverDataSources(broadcastAddress, 3, 500,
                        false);

                HashMap<String, TcpDataSource> currentDataSourceById = new HashMap<String, TcpDataSource>();
                for (TcpDataSource ds : dataSources) {
                    InetAddress address = ds.getAddress();
                    String addressId = address.getHostAddress();
                    TcpDataSource dsWithInfo;
                    try {
                        dsWithInfo = TcpDataSourceProvider.fetchInformation(address, 1500);
                        logger.trace("Discovered Resol VBus-LAN interface @{} {} ({})", addressId,
                                dsWithInfo.getDeviceName(), dsWithInfo.getSerial());

                        currentDataSourceById.put(addressId, dsWithInfo);
                        addAdapter(addressId, dsWithInfo);
                        // TODO: add here the detection of Multi-Channel interfaces like DL3
                    } catch (IOException ex) {
                        /* address is no valid adapter */
                    }

                }
            } catch (Exception e) {
                logger.debug("No VBusLAN adapter found!");
            }
        }
    };

    private void addAdapter(String remoteIP, TcpDataSource dsWithInfo) {
        String adapterSerial = dsWithInfo.getSerial();
        Map<@NonNull String, @NonNull Object> properties = new HashMap<>(3);
        properties.put("ipAddress", remoteIP);
        properties.put("port", dsWithInfo.getLivePort());
        properties.put("adapterSerial", adapterSerial);

        ThingUID uid = new ThingUID(ResolBindingConstants.THING_TYPE_UID_BRIDGE, adapterSerial);
        thingDiscovered(
                DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(dsWithInfo.getName()).build());
    }
}
