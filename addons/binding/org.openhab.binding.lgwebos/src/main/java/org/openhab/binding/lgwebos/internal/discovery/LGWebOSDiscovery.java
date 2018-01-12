/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.internal.discovery;

import static org.openhab.binding.lgwebos.LGWebOSBindingConstants.THING_TYPE_WEBOSTV;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.lgwebos.LGWebOSBindingConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.core.Context;
import com.connectsdk.core.Util;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.discovery.DiscoveryManagerListener;
import com.connectsdk.service.command.ServiceCommandError;

/**
 * This class provides the bridges between openhab thing discovery and connect sdk device discovery.
 *
 * @author Sebastian Prehn
 */
@Component(service = { DiscoveryService.class,
        LGWebOSDiscovery.class }, immediate = true, configurationPid = "binding.lgwebos")
public class LGWebOSDiscovery extends AbstractDiscoveryService implements DiscoveryManagerListener, Context {
    private static final int DISCOVERY_TIMEOUT_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(LGWebOSDiscovery.class);

    private DiscoveryManager discoveryManager;

    private NetworkAddressService networkAddressService;

    public LGWebOSDiscovery() {
        super(LGWebOSBindingConstants.SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS, true);
        DiscoveryManager.init(this);
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        logger.debug("Config Parameters: {}", configProperties);
        Util.init(AbstractDiscoveryService.scheduler);
        discoveryManager = DiscoveryManager.getInstance();
        discoveryManager.setPairingLevel(DiscoveryManager.PairingLevel.ON);
        discoveryManager.addListener(this);
        super.activate(configProperties); // starts background discovery
    }

    @Override
    protected void deactivate() {
        super.deactivate(); // stops background discovery
        discoveryManager.removeListener(this);
        discoveryManager = null;
        DiscoveryManager.destroy();
        Util.uninit();
    }

    // @Override
    @Override
    protected void startScan() {
        // no adhoc scanning. Discovery Service runs in background, but re-discover all known devices in case they were
        // deleted from the inbox.
        discoveryManager.getCompatibleDevices().values()
                .forEach(device -> thingDiscovered(createDiscoveryResult(device)));
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoveryManager.start();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        discoveryManager.stop();
    }

    // DiscoveryManagerListener

    @Override
    public void onDeviceAdded(DiscoveryManager manager, ConnectableDevice device) {
        thingDiscovered(createDiscoveryResult(device));
    }

    @Override
    public void onDeviceUpdated(DiscoveryManager manager, ConnectableDevice device) {
        logger.debug("Device updated: {}", device);
        thingRemoved(createThingUID(device));
        thingDiscovered(createDiscoveryResult(device));
    }

    @Override
    public void onDeviceRemoved(DiscoveryManager manager, ConnectableDevice device) {
        logger.debug("Device removed: {}", device);
        thingRemoved(createThingUID(device));
    }

    @Override
    public void onDiscoveryFailed(DiscoveryManager manager, ServiceCommandError error) {
        logger.warn("Discovery Failed {}", error.getMessage());
    }

    // Helpers for DiscoveryManagerListener Impl
    private DiscoveryResult createDiscoveryResult(ConnectableDevice device) {
        ThingUID thingUID = createThingUID(device);
        return DiscoveryResultBuilder.create(thingUID).withLabel(device.getFriendlyName()).build();
    }

    private ThingUID createThingUID(ConnectableDevice device) {
        return new ThingUID(THING_TYPE_WEBOSTV, device.getId());
    }

    public DiscoveryManager getDiscoveryManager() {
        return this.discoveryManager;
    }

    @Override
    public String getDataDir() {
        return ConfigConstants.getUserDataFolder() + File.separator + "lgwebos";
    }

    @Override
    public String getApplicationName() {
        return "openHAB";
    }

    @Override
    public String getPackageName() {
        // In combination with ApplicationName this is used by LG TVs to identify the client application.
        // We don't want the actual package name. We need a constant namespace here.
        return "org.openhab";
    }

    @Override
    public InetAddress getIpAddress() {
        String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
        if (ipAddress == null) {
            logger.warn("No network interface could be found.");
            return null;
        }
        try {
            return InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            logger.warn("Configured primary IP cannot be parsed: {} Details: {}", ipAddress, e.getMessage());
            return null;
        }
    }
}
