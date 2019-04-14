/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.lgwebos.internal.discovery;

import static org.openhab.binding.lgwebos.internal.LGWebOSBindingConstants.*;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.lgwebos.internal.LGWebOSBindingConstants;
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
 * This class provides the bridge between openhab thing discovery and connect sdk device discovery.
 *
 * @author Sebastian Prehn - initial contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class,
        LGWebOSDiscovery.class }, immediate = true, configurationPid = "binding.lgwebos")
public class LGWebOSDiscovery extends AbstractDiscoveryService implements DiscoveryManagerListener, Context {
    private static final int DISCOVERY_TIMEOUT_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(LGWebOSDiscovery.class);

    private @Nullable DiscoveryManager discoveryManager;

    private @Nullable NetworkAddressService networkAddressService;
    private Optional<InetAddress> localInetAddressesOverride = Optional.empty();

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
    protected void activate(@Nullable Map<String, @Nullable Object> configProperties) {
        logger.debug("Config Parameters: {}", configProperties);
        if (configProperties != null) {
            localInetAddressesOverride = evaluateConfigPropertyLocalIP(
                    (String) configProperties.get(BINDING_CONFIGURATION_LOCALIP));
        }
        Util.init(scheduler);

        DiscoveryManager manager = DiscoveryManager.getInstance();
        manager.setPairingLevel(DiscoveryManager.PairingLevel.ON);
        manager.addListener(this);
        discoveryManager = manager;

        super.activate(configProperties); // starts background discovery
    }

    @Override
    protected void deactivate() {
        super.deactivate(); // stops background discovery
        DiscoveryManager manager = discoveryManager;
        if (manager != null) {
            manager.removeListener(this);
        }
        discoveryManager = null;
        DiscoveryManager.destroy();
        Util.uninit();
    }

    // @Override
    @Override
    protected void startScan() {
        // no adhoc scanning. Discovery Service runs in background, but re-discover all known devices in case they were
        // deleted from the inbox.
        DiscoveryManager manager = discoveryManager;
        if (manager != null) {
            manager.getCompatibleDevices().values().forEach(device -> thingDiscovered(createDiscoveryResult(device)));
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        DiscoveryManager manager = discoveryManager;
        if (manager != null) {
            manager.start();
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        DiscoveryManager manager = discoveryManager;
        if (manager != null) {
            manager.stop();
        }
    }

    // DiscoveryManagerListener

    @Override
    public void onDeviceAdded(@Nullable DiscoveryManager manager, @Nullable ConnectableDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("ConnectableDevice must not be null");
        }
        thingDiscovered(createDiscoveryResult(device));
    }

    @Override
    public void onDeviceUpdated(@Nullable DiscoveryManager manager, @Nullable ConnectableDevice device) {
        logger.debug("Device updated: {}", device);
    }

    @Override
    public void onDeviceRemoved(@Nullable DiscoveryManager manager, @Nullable ConnectableDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("ConnectableDevice must not be null");
        }
        logger.debug("Device removed: {}", device);
        thingRemoved(createThingUID(device));
    }

    @Override
    public void onDiscoveryFailed(@Nullable DiscoveryManager manager, @Nullable ServiceCommandError error) {
        logger.warn("Discovery Failed {}", error == null ? "" : error.getMessage());
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

    public @Nullable DiscoveryManager getDiscoveryManager() {
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
    public @Nullable InetAddress getIpAddress() {
        return localInetAddressesOverride.orElseGet(() -> getIpFromNetworkAddressService().orElse(null));
    }

    /**
     * Evaluate local IP optional configuration property.
     *
     * @param localIP optional configuration string
     * @return local ip or <code>empty</code> if property is not set or unparseable.
     */
    private Optional<InetAddress> evaluateConfigPropertyLocalIP(@Nullable String localIP) {
        if (localIP != null && !localIP.trim().isEmpty()) {
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
    @NonNullByDefault({})
    private Optional<InetAddress> getIpFromNetworkAddressService() {
        NetworkAddressService service = networkAddressService;
        if (service == null) {
            throw new IllegalStateException(
                    "NetworkAddressService must be bound before getIpFromNetworkAddressService can be called.");
        }

        String ipAddress = service.getPrimaryIpv4HostAddress();
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
