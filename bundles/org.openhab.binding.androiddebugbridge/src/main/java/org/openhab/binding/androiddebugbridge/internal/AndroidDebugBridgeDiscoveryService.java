/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.androiddebugbridge.internal;

import static org.openhab.binding.androiddebugbridge.internal.AndroidDebugBridgeBindingConstants.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AndroidDebugBridgeDiscoveryService} discover Android ADB Instances in the network.
 *
 * @author Miguel Alvarez - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.androiddebugbridge")
public class AndroidDebugBridgeDiscoveryService extends AbstractDiscoveryService {
    static final int TIMEOUT_MS = 60000;
    private static final long DISCOVERY_RESULT_TTL_SEC = 300;
    public static final String LOCAL_INTERFACE_IP = "127.0.0.1";
    public static final int MAX_RETRIES = 2;
    private final Logger logger = LoggerFactory.getLogger(AndroidDebugBridgeDiscoveryService.class);
    private final ConfigurationAdmin admin;
    private boolean discoveryRunning = false;

    @Activate
    public AndroidDebugBridgeDiscoveryService(@Reference ConfigurationAdmin admin) {
        super(SUPPORTED_THING_TYPES, TIMEOUT_MS, false);
        this.admin = admin;
    }

    @Override
    protected void startScan() {
        logger.debug("scan started: searching android devices");
        discoveryRunning = true;
        Enumeration<NetworkInterface> nets;
        AndroidDebugBridgeBindingConfiguration configuration = getConfig();
        if (configuration == null) {
            return;
        }
        try {
            nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    if (!discoveryRunning) {
                        break;
                    }
                    if (!(inetAddress instanceof Inet4Address)
                            || inetAddress.getHostAddress().equals(LOCAL_INTERFACE_IP)) {
                        continue;
                    }
                    String[] ipParts = inetAddress.getHostAddress().split("\\.");
                    for (int i = configuration.discoveryIpRangeMin; i <= configuration.discoveryIpRangeMax; i++) {
                        if (!discoveryRunning) {
                            break;
                        }
                        ipParts[3] = Integer.toString(i);
                        String currentIp = String.join(".", ipParts);
                        try {
                            var currentAddress = InetAddress.getByName(currentIp);
                            logger.debug("address: {}", currentIp);
                            if (currentAddress.isReachable(configuration.discoveryReachableMs)) {
                                logger.debug("Reachable ip: {}", currentIp);
                                int retries = 0;
                                while (retries < MAX_RETRIES) {
                                    try {
                                        discoverWithADB(currentIp, configuration.discoveryPort);
                                    } catch (AndroidDebugBridgeDeviceReadException | TimeoutException e) {
                                        retries++;
                                        if (retries < MAX_RETRIES) {
                                            logger.debug("retrying - pending {}", MAX_RETRIES - retries);
                                            continue;
                                        }
                                        throw e;
                                    }
                                    break;
                                }
                            }
                        } catch (IOException | AndroidDebugBridgeDeviceException | AndroidDebugBridgeDeviceReadException
                                | TimeoutException | ExecutionException e) {
                            logger.debug("Error connecting to device at {}: {}", currentIp, e.getMessage());
                        }
                    }
                }
            }
        } catch (SocketException | InterruptedException e) {
            logger.warn("Error while discovering: {}", e.getMessage());
        }
    }

    private void discoverWithADB(String ip, int port) throws InterruptedException, AndroidDebugBridgeDeviceException,
            AndroidDebugBridgeDeviceReadException, TimeoutException, ExecutionException {
        var device = new AndroidDebugBridgeDevice(scheduler);
        device.configure(ip, port, 10, 0);
        try {
            device.connect();
            logger.debug("connected adb at {}:{}", ip, port);
            String serialNo = device.getSerialNo();
            String model = device.getModel();
            String androidVersion = device.getAndroidVersion();
            String brand = device.getBrand();
            logger.debug("discovered: {} - {} - {} - {}", model, serialNo, androidVersion, brand);
            onDiscoverResult(serialNo, ip, port, model, androidVersion, brand);
        } finally {
            device.disconnect();
        }
    }

    @Override
    protected void stopScan() {
        super.stopScan();
        discoveryRunning = false;
        logger.debug("scan stopped");
    }

    private void onDiscoverResult(String serialNo, String ip, int port, String model, String androidVersion,
            String brand) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNo);
        properties.put(PARAMETER_IP, ip);
        properties.put(PARAMETER_PORT, port);
        properties.put(Thing.PROPERTY_MODEL_ID, model);
        properties.put(Thing.PROPERTY_VENDOR, brand);
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, androidVersion);
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_ANDROID_DEVICE, serialNo))
                .withTTL(DISCOVERY_RESULT_TTL_SEC).withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
                .withProperties(properties).withLabel(String.format("%s (%s)", model, serialNo)).build());
    }

    private @Nullable AndroidDebugBridgeBindingConfiguration getConfig() {
        try {
            Configuration configOnline = admin.getConfiguration(BINDING_CONFIGURATION_PID, null);
            if (configOnline != null) {
                Dictionary<String, Object> props = configOnline.getProperties();
                if (props != null) {
                    Map<String, Object> propMap = Collections.list(props.keys()).stream()
                            .collect(Collectors.toMap(Function.identity(), props::get));
                    return new org.openhab.core.config.core.Configuration(propMap)
                            .as(AndroidDebugBridgeBindingConfiguration.class);
                }
            }
        } catch (IOException e) {
            logger.warn("Unable to read configuration: {}", e.getMessage());
        }
        return null;
    }
}
