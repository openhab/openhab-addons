/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.lgwebos.LGWebOSBindingConstants;
import org.osgi.service.component.annotations.Component;
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
 * @since 1.8.0
 */
@Component(service = { DiscoveryService.class, LGWebOSDiscovery.class }, immediate = true, name = "binding.lgwebos")
public class LGWebOSDiscovery extends AbstractDiscoveryService implements DiscoveryManagerListener, Context {
    private Logger logger = LoggerFactory.getLogger(LGWebOSDiscovery.class);

    private DiscoveryManager discoveryManager;

    private InetAddress localInetAddresses;

    public LGWebOSDiscovery() {
        super(LGWebOSBindingConstants.SUPPORTED_THING_TYPES_UIDS, 60, true);
        DiscoveryManager.init(this);
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        logger.debug("Config Parameters: {}", configProperties);
        localInetAddresses = findLocalInetAddresses((String) configProperties.get("localIP"));
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
        // no adhoc scanning. Discovery Service runs in background, but re-discover all known devices incase they were
        // deleted from the inbox.
        for (ConnectableDevice d : discoveryManager.getCompatibleDevices().values()) {
            thingDiscovered(createDiscoveryResult(d));
        }
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
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID)
                .withProperty(PROPERTY_IP_ADDRESS, device.getIpAddress()).withLabel(device.getFriendlyName()).build();
        return result;
    }

    private ThingUID createThingUID(ConnectableDevice device) {
        return new ThingUID(THING_TYPE_WEBOSTV, device.getIpAddress().replace('.', '_'));
    }

    public DiscoveryManager getDiscoveryManager() {
        return this.discoveryManager;
    }

    // Context for connect sdk
    private final String DATA_DIR = new File("etc" + File.separator + "connect_sdk").getAbsolutePath();

    @Override
    public String getDataDir() {
        return DATA_DIR;
    }

    @Override
    public String getApplicationName() {
        return "Openhab";
    }

    @Override
    public String getPackageName() {
        return "org.openhab";
    }

    @Override
    public InetAddress getIpAddress() {
        return localInetAddresses;
    }

    /**
     * Get local IP either through configuration or auto detection.
     * Method will ignore loopback addresses.
     *
     * @param localIP optional configuration string
     * @return local ip or <code>null</code> if detection was not possible.
     */
    private InetAddress findLocalInetAddresses(String localIP) {
        // evaluate optional localIP parameter, can be configured through config admin (lgwebos.cfg)
        if (localIP != null && !localIP.trim().isEmpty()) {
            try {
                logger.debug("localIP parameter explicitly set to: {}", localIP);
                return InetAddress.getByName(localIP.trim());
            } catch (UnknownHostException e) {
                logger.warn("localIP config parameter could not be parsed: {}", localIP);
            }
        }

        // try to find IP via Java method (one some systems this returns the loopback interface though)
        try {
            final InetAddress inetAddress = InetAddress.getLocalHost();
            if (!inetAddress.isLoopbackAddress()) {
                logger.debug("Autodetected (via getLocalHost) local IP: {}", inetAddress);
                return inetAddress;
            }
        } catch (UnknownHostException ex) {
            logger.warn("Unable to resolve your hostname", ex);
        }

        // try to find the single non-loop back interface available
        final List<InetAddress> interfaces = new ArrayList<InetAddress>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    for (InterfaceAddress adr : networkInterface.getInterfaceAddresses()) {
                        InetAddress inadr = adr.getAddress();
                        interfaces.add(inadr);
                    }
                }
            }

            if (interfaces.size() == 1) { // found exactly one interface, good
                logger.debug("Autodetected (via getNetworkInterfaces) local IP: {}", interfaces.get(0));
                return interfaces.get(0);
            } else {
                logger.warn(
                        "Autodetection of local IP (via getNetworkInterfaces) failed, as multiple interfaces where detected: {}",
                        interfaces);
            }
        } catch (SocketException e) {
            logger.warn("Failed to detect network interfaces and addresses", e);
        }

        return null;
    }
}
