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
package org.openhab.io.homekit.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jmdns.JmDNS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mdns.MDNSClient;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.net.CidrAddress;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.storage.StorageService;
import org.openhab.io.homekit.Homekit;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.HomekitAccessory;
import io.github.hapjava.server.impl.HomekitRoot;
import io.github.hapjava.server.impl.HomekitServer;
import io.github.hapjava.server.impl.crypto.HAPSetupCodeUtils;

/**
 * Provides access to openHAB items via the HomeKit API
 *
 * @author Andy Lintner - Initial contribution
 */
@Component(service = { Homekit.class }, configurationPid = HomekitSettings.CONFIG_PID, property = {
        Constants.SERVICE_PID + "=org.openhab.homekit", "port:Integer=9123" })
@ConfigurableService(category = "io", label = "HomeKit Integration", description_uri = "io:homekit")
@NonNullByDefault
public class HomekitImpl implements Homekit, NetworkAddressChangeListener {
    private final Logger logger = LoggerFactory.getLogger(HomekitImpl.class);

    private final NetworkAddressService networkAddressService;
    private final ConfigurationAdmin configAdmin;

    private HomekitAuthInfoImpl authInfo;
    private HomekitSettings settings;
    private @Nullable InetAddress networkInterface;
    private @Nullable HomekitServer homekitServer;
    private @Nullable HomekitRoot bridge;
    private MDNSClient mdnsClient;

    private final HomekitChangeListener changeListener;

    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(ThreadPoolManager.THREAD_POOL_NAME_COMMON);

    @Activate
    public HomekitImpl(@Reference StorageService storageService, @Reference ItemRegistry itemRegistry,
            @Reference NetworkAddressService networkAddressService, @Reference MetadataRegistry metadataRegistry,
            @Reference ConfigurationAdmin configAdmin, @Reference MDNSClient mdnsClient, Map<String, Object> properties)
            throws IOException, InvalidAlgorithmParameterException {
        this.networkAddressService = networkAddressService;
        this.configAdmin = configAdmin;
        this.settings = processConfig(properties);
        this.mdnsClient = mdnsClient;
        networkAddressService.addNetworkAddressChangeListener(this);
        this.changeListener = new HomekitChangeListener(itemRegistry, settings, metadataRegistry, storageService);
        try {
            authInfo = new HomekitAuthInfoImpl(storageService.getStorage(HomekitAuthInfoImpl.STORAGE_KEY), settings.pin,
                    settings.setupId, settings.blockUserDeletion);
            startHomekitServer();
        } catch (IOException | InvalidAlgorithmParameterException e) {
            logger.warn("cannot activate HomeKit binding. {}", e.getMessage());
            throw e;
        }
    }

    private HomekitSettings processConfig(Map<String, Object> properties) {
        HomekitSettings settings = (new Configuration(properties)).as(HomekitSettings.class);
        org.osgi.service.cm.Configuration config = null;
        Dictionary<String, Object> props = null;
        try {
            config = configAdmin.getConfiguration(HomekitSettings.CONFIG_PID);
            props = config.getProperties();
        } catch (IOException e) {
            logger.warn("cannot retrieve config admin {}", e.getMessage());
        }

        if (props == null) { // if null, the configuration is new
            props = new Hashtable<>();
        }

        if (settings.setupId == null) { // generate setupId very first time
            settings.setupId = HAPSetupCodeUtils.generateSetupId();
            props.put("setupId", settings.setupId);
        }

        // QR Code setup URI is always generated from PIN, setup ID and accessory category (1 = bridge)
        String setupURI = HAPSetupCodeUtils.getSetupURI(settings.pin.replaceAll("-", ""), settings.setupId, 1);
        if ((settings.qrCode == null) || (!settings.qrCode.equals(setupURI))) { // QR code was changed
            settings.qrCode = setupURI;
            props.put("qrCode", settings.qrCode);
        }

        if (config != null) {
            try {
                config.updateIfDifferent(props);
            } catch (IOException e) {
                logger.warn("cannot update configuration {}", e.getMessage());
            }
        }
        return settings;
    }

    @Modified
    protected synchronized void modified(Map<String, Object> config) {
        try {
            HomekitSettings oldSettings = settings;
            settings = processConfig(config);
            changeListener.updateSettings(settings);
            if (!oldSettings.networkInterface.equals(settings.networkInterface) || oldSettings.port != settings.port
                    || oldSettings.useOHmDNS != settings.useOHmDNS) {
                // the HomeKit server settings changed. we do a complete re-init
                stopHomekitServer();
                startHomekitServer();
            } else if (!oldSettings.name.equals(settings.name) || !oldSettings.pin.equals(settings.pin)
                    || !oldSettings.setupId.equals(settings.setupId)) {
                stopHomekitServer();
                authInfo.setPin(settings.pin);
                authInfo.setSetupId(settings.setupId);
                startHomekitServer();
            }
        } catch (IOException e) {
            logger.warn("could not initialize HomeKit bridge: {}", e.getMessage());
        }
    }

    private void stopBridge() {
        final @Nullable HomekitRoot bridge = this.bridge;
        if (bridge != null) {
            changeListener.unsetBridge();
            bridge.stop();
            this.bridge = null;
        }
    }

    private void startBridge() throws IOException {
        final @Nullable HomekitServer homekitServer = this.homekitServer;
        if (homekitServer != null && bridge == null) {
            final HomekitRoot bridge = homekitServer.createBridge(authInfo, settings.name, HomekitSettings.MANUFACTURER,
                    HomekitSettings.MODEL, HomekitSettings.SERIAL_NUMBER,
                    FrameworkUtil.getBundle(getClass()).getVersion().toString(), HomekitSettings.HARDWARE_REVISION);
            changeListener.setBridge(bridge);
            this.bridge = bridge;
            bridge.setConfigurationIndex(changeListener.getConfigurationRevision());
            bridge.refreshAuthInfo();
            final int lastAccessoryCount = changeListener.getLastAccessoryCount();
            int currentAccessoryCount = changeListener.getAccessories().size();
            if (currentAccessoryCount < lastAccessoryCount) {
                logger.debug(
                        "it looks like not all items were initialized yet. Old configuration had {} accessories, the current one has only {} accessories. Delay HomeKit bridge start for {} seconds.",
                        lastAccessoryCount, currentAccessoryCount, settings.startDelay);
                scheduler.schedule(() -> {
                    if (currentAccessoryCount < lastAccessoryCount) {
                        // the number of items is still different, maybe it is desired.
                        // make new configuration revision.
                        changeListener.makeNewConfigurationRevision();
                    }
                    bridge.start();
                }, settings.startDelay, TimeUnit.SECONDS);
            } else { // start bridge immediately.
                bridge.start();
            }
        } else {
            logger.warn(
                    "trying to start bridge but HomeKit server is not initialized or bridge is already initialized");
        }
    }

    private void startHomekitServer() throws IOException {
        logger.trace("start HomeKit bridge");
        if (homekitServer == null) {
            try {
                networkInterface = InetAddress
                        .getByName(((settings.networkInterface != null) && (!settings.networkInterface.isEmpty()))
                                ? settings.networkInterface
                                : networkAddressService.getPrimaryIpv4HostAddress());
                if (settings.useOHmDNS) {
                    for (JmDNS mdns : mdnsClient.getClientInstances()) {
                        if (mdns.getInetAddress().equals(networkInterface)) {
                            logger.trace("suitable mDNS client for IP {} found and will be used for HomeKit",
                                    networkInterface);
                            homekitServer = new HomekitServer(mdns, settings.port);
                        }
                    }
                }
                if (homekitServer == null) {
                    if (settings.useOHmDNS) {
                        logger.trace("no suitable mDNS server for IP {} found", networkInterface);
                    }
                    logger.trace("create HomeKit server with dedicated mDNS server");
                    homekitServer = new HomekitServer(networkInterface, settings.port);
                }
                startBridge();
            } catch (UnknownHostException e) {
                logger.warn("cannot resolve the Pv4 address / hostname {}.",
                        networkAddressService.getPrimaryIpv4HostAddress());
            }
        } else {
            logger.warn("trying to start HomeKit server but it is already initialized");
        }
    }

    private void stopHomekitServer() {
        logger.trace("stop HomeKit bridge");
        final @Nullable HomekitServer homekit = this.homekitServer;
        if (homekit != null) {
            if (bridge != null) {
                stopBridge();
            }
            homekit.stop();
            this.homekitServer = null;
        }
    }

    @Deactivate
    protected void deactivate() {
        networkAddressService.removeNetworkAddressChangeListener(this);
        changeListener.clearAccessories();
        stopHomekitServer();
        changeListener.stop();
    }

    @Override
    public void refreshAuthInfo() throws IOException {
        final @Nullable HomekitRoot bridge = this.bridge;
        if (bridge != null) {
            bridge.refreshAuthInfo();
        }
    }

    @Override
    public void allowUnauthenticatedRequests(boolean allow) {
        final @Nullable HomekitRoot bridge = this.bridge;
        if (bridge != null) {
            bridge.allowUnauthenticatedRequests(allow);
        }
    }

    @Override
    public List<HomekitAccessory> getAccessories() {
        return new ArrayList<>(this.changeListener.getAccessories().values());
    }

    @Override
    public void clearHomekitPairings() {
        try {
            authInfo.clear();
            refreshAuthInfo();
        } catch (Exception e) {
            logger.warn("could not clear HomeKit pairings", e);
        }
    }

    @Override
    public synchronized void onChanged(final List<CidrAddress> added, final List<CidrAddress> removed) {
        logger.trace("HomeKit bridge reacting on network interface changes.");
        removed.forEach(i -> {
            logger.trace("removed interface {}", i.getAddress().toString());
            if (i.getAddress().equals(networkInterface)) {
                final @Nullable HomekitRoot bridge = this.bridge;
                if (this.bridge != null) {
                    bridge.stop();
                    this.bridge = null;
                }
                final @Nullable HomekitServer homekitServer = this.homekitServer;
                if (homekitServer != null) {
                    homekitServer.stop();
                    this.homekitServer = null;
                }
                logger.trace("bridge stopped");
            }
        });
        if ((this.bridge == null) && (!added.isEmpty())) {
            try {
                startHomekitServer();
            } catch (IOException e) {
                logger.warn("could not initialize HomeKit bridge: {}", e.getMessage());
            }
        }
    }
}
