/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.internal.discovery;

import static org.openhab.binding.lgwebos.LGWebOSBindingConstants.*;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
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
    private Optional<InetAddress> localInetAddressesOverride;

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
        localInetAddressesOverride = evaluateConfigPropertyLocalIP((String) configProperties.get("localIP"));
        Util.init(scheduler);
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
        return DiscoveryResultBuilder.create(thingUID).withLabel(device.getFriendlyName())
                .withProperty(PROPERTY_DEVICE_ID, device.getId()).withRepresentationProperty(PROPERTY_DEVICE_ID)
                .build();
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
        return localInetAddressesOverride.orElseGet(() -> getIpFromNetworkAddressService().orElse(null));
    }

    /**
     * Evaluate local IP optional configuration property.
     *
     * @param localIP optional configuration string
     * @return local ip or <code>empty</code> if property is not set or unparseable.
     */
    private Optional<InetAddress> evaluateConfigPropertyLocalIP(String localIP) {
        if (StringUtils.isNotBlank(localIP)) {
            try {
                logger.debug("localIP property was explicitly set to: {}", localIP);
                return Optional.ofNullable(InetAddress.getByName(localIP.trim()));
            } catch (UnknownHostException e) {
                logger.warn("localIP property could not be parsed: {} Details: {}", localIP, e.getMessage());
            }
        }

        return Optional.empty();
    }

    /**
     * Uses OpenHAB's NetworkAddressService to determine the local primary network interface.
     *
     * @return local ip or <code>empty</code> if configured primary IP is not set or could not be parsed.
     */
    private Optional<InetAddress> getIpFromNetworkAddressService() {
        String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
        if (ipAddress == null) {
            logger.warn("No network interface could be found.");
            return Optional.empty();
        }
        try {
            return Optional.of(InetAddress.getByName(ipAddress));
        } catch (UnknownHostException e) {
            logger.warn("Configured primary IP cannot be parsed: {} Details: {}", ipAddress, e.getMessage());
            return Optional.empty();
        }
    }

}
