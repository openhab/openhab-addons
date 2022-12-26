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
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyMarkerFilter;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.StartLevelService;
import org.openhab.core.storage.Storage;
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
import io.github.hapjava.server.HomekitAccessoryCategories;
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
public class HomekitImpl implements Homekit, NetworkAddressChangeListener, ReadyService.ReadyTracker {
    private final Logger logger = LoggerFactory.getLogger(HomekitImpl.class);

    private final StorageService storageService;
    private final NetworkAddressService networkAddressService;
    private final ConfigurationAdmin configAdmin;
    private final ItemRegistry itemRegistry;
    private final MetadataRegistry metadataRegistry;
    private final ReadyService readyService;

    private final List<HomekitAuthInfoImpl> authInfos = new ArrayList<>();
    private HomekitSettings settings;
    private @Nullable InetAddress networkInterface;
    private final List<HomekitServer> homekitServers = new ArrayList<>();
    private final List<HomekitRoot> bridges = new ArrayList<>();
    private MDNSClient mdnsClient;
    private int currentStartLevel = 0;

    private final List<HomekitChangeListener> changeListeners = new ArrayList<>();

    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(ThreadPoolManager.THREAD_POOL_NAME_COMMON);

    @Activate
    public HomekitImpl(@Reference StorageService storageService, @Reference ItemRegistry itemRegistry,
            @Reference NetworkAddressService networkAddressService, @Reference MetadataRegistry metadataRegistry,
            @Reference ConfigurationAdmin configAdmin, @Reference MDNSClient mdnsClient,
            @Reference ReadyService readyService, Map<String, Object> properties)
            throws IOException, InvalidAlgorithmParameterException {
        this.storageService = storageService;
        this.networkAddressService = networkAddressService;
        this.configAdmin = configAdmin;
        this.settings = processConfig(properties);
        this.mdnsClient = mdnsClient;
        this.itemRegistry = itemRegistry;
        this.metadataRegistry = metadataRegistry;
        this.readyService = readyService;
        networkAddressService.addNetworkAddressChangeListener(this);
        readyService.registerTracker(this, new ReadyMarkerFilter().withType(StartLevelService.STARTLEVEL_MARKER_TYPE)
                .withIdentifier(Integer.toString(StartLevelService.STARTLEVEL_STATES)));
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
            if ((oldSettings == null) || (settings == null)) {
                return;
            }
            if (!oldSettings.name.equals(settings.name) || !oldSettings.pin.equals(settings.pin)
                    || !oldSettings.setupId.equals(settings.setupId)
                    || (oldSettings.networkInterface != null
                            && !oldSettings.networkInterface.equals(settings.networkInterface))
                    || oldSettings.port != settings.port || oldSettings.useOHmDNS != settings.useOHmDNS
                    || oldSettings.instances != settings.instances) {
                // the HomeKit server settings changed. we do a complete re-init
                stopHomekitServer();
                if (currentStartLevel >= StartLevelService.STARTLEVEL_STATES) {
                    startHomekitServer();
                }
            } else {
                for (HomekitChangeListener changeListener : changeListeners) {
                    changeListener.updateSettings(settings);
                }
                if (settings.blockUserDeletion != oldSettings.blockUserDeletion) {
                    for (HomekitAuthInfoImpl authInfo : authInfos) {
                        authInfo.setBlockUserDeletion(settings.blockUserDeletion);
                    }
                }
            }
        } catch (IOException | InvalidAlgorithmParameterException e) {
            logger.warn("could not initialize HomeKit bridge: {}", e.getMessage());
        }
    }

    @Override
    public synchronized void onReadyMarkerAdded(ReadyMarker readyMarker) {
        try {
            startHomekitServer();
        } catch (IOException | InvalidAlgorithmParameterException e) {
            logger.warn("could not initialize HomeKit bridge: {}", e.getMessage());
        }
    }

    @Override
    public synchronized void onReadyMarkerRemoved(ReadyMarker readyMarker) {
        stopHomekitServer();
    }

    private HomekitRoot startBridge(HomekitServer homekitServer, HomekitAuthInfoImpl authInfo,
            HomekitChangeListener changeListener, int instance) throws IOException {
        String name = settings.name;
        if (instance != 1) {
            name += " (" + instance + ")";
        }
        final HomekitRoot bridge = homekitServer.createBridge(authInfo, name, HomekitAccessoryCategories.BRIDGES,
                HomekitSettings.MANUFACTURER, HomekitSettings.MODEL, HomekitSettings.SERIAL_NUMBER,
                FrameworkUtil.getBundle(getClass()).getVersion().toString(), HomekitSettings.HARDWARE_REVISION);
        changeListener.setBridge(bridge);
        bridges.add(bridge);
        bridge.setConfigurationIndex(changeListener.getConfigurationRevision());
        bridge.start();
        return bridge;
    }

    private void startHomekitServer() throws IOException, InvalidAlgorithmParameterException {
        logger.trace("start HomeKit bridge");
        if (homekitServers.isEmpty()) {
            try {
                networkInterface = InetAddress
                        .getByName(((settings.networkInterface != null) && (!settings.networkInterface.isEmpty()))
                                ? settings.networkInterface
                                : networkAddressService.getPrimaryIpv4HostAddress());
            } catch (UnknownHostException e) {
                logger.warn("cannot resolve the Pv4 address / hostname {}.",
                        networkAddressService.getPrimaryIpv4HostAddress());
            }

            for (int i = 0; i < settings.instances; ++i) {
                String storage_key = HomekitAuthInfoImpl.STORAGE_KEY;
                if (i != 0) {
                    storage_key += i;
                }
                Storage<Object> storage = storageService.getStorage(storage_key);
                HomekitAuthInfoImpl authInfo = new HomekitAuthInfoImpl(storage, settings.pin, settings.setupId,
                        settings.blockUserDeletion);

                @Nullable
                HomekitServer homekitServer = null;
                if (settings.useOHmDNS) {
                    for (JmDNS mdns : mdnsClient.getClientInstances()) {
                        if (mdns.getInetAddress().equals(networkInterface)) {
                            logger.trace("suitable mDNS client for IP {} found and will be used for HomeKit",
                                    networkInterface);
                            homekitServer = new HomekitServer(mdns, settings.port + i);
                        }
                    }
                }
                if (homekitServer == null) {
                    if (settings.useOHmDNS) {
                        logger.trace("no suitable mDNS server for IP {} found", networkInterface);
                    }
                    logger.trace("create HomeKit server with dedicated mDNS server");
                    homekitServer = new HomekitServer(networkInterface, settings.port + i);
                }
                homekitServers.add(homekitServer);
                HomekitChangeListener changeListener = new HomekitChangeListener(itemRegistry, settings,
                        metadataRegistry, storage, i + 1);
                changeListeners.add(changeListener);
                bridges.add(startBridge(homekitServer, authInfo, changeListener, i + 1));
                authInfos.add(authInfo);
            }
        } else {
            logger.warn("trying to start HomeKit server but it is already initialized");
        }
    }

    private void stopHomekitServer() {
        logger.trace("stop HomeKit bridge");
        changeListeners.parallelStream().forEach(HomekitChangeListener::stop);
        bridges.parallelStream().forEach(HomekitRoot::stop);
        homekitServers.parallelStream().forEach(HomekitServer::stop);
        homekitServers.clear();
        bridges.clear();
        changeListeners.clear();
        authInfos.clear();
    }

    @Deactivate
    protected void deactivate() {
        networkAddressService.removeNetworkAddressChangeListener(this);
        stopHomekitServer();
    }

    @Override
    public void refreshAuthInfo() throws IOException {
        for (HomekitRoot bridge : bridges) {
            bridge.refreshAuthInfo();
        }
    }

    @Override
    public void allowUnauthenticatedRequests(boolean allow) {
        for (HomekitRoot bridge : bridges) {
            bridge.allowUnauthenticatedRequests(allow);
        }
    }

    @Override
    public List<HomekitAccessory> getAccessories() {
        List<HomekitAccessory> accessories = new ArrayList<>();
        for (HomekitChangeListener changeListener : changeListeners) {
            accessories.addAll(changeListener.getAccessories().values());
        }
        return accessories;
    }

    @Override
    public void clearHomekitPairings() {
        try {
            for (HomekitAuthInfoImpl authInfo : authInfos) {
                authInfo.clear();
            }
            refreshAuthInfo();
        } catch (Exception e) {
            logger.warn("could not clear HomeKit pairings", e);
        }
    }

    @Override
    public void pruneDummyAccessories() {
        for (HomekitChangeListener changeListener : changeListeners) {
            changeListener.pruneDummyAccessories();
        }
    }

    @Override
    public synchronized void onChanged(final List<CidrAddress> added, final List<CidrAddress> removed) {
        logger.trace("HomeKit bridge reacting on network interface changes.");
        if (currentStartLevel < StartLevelService.STARTLEVEL_STATES) {
            return;
        }
        removed.forEach(i -> {
            logger.trace("removed interface {}", i.getAddress().toString());
            if (i.getAddress().equals(networkInterface)) {
                stopHomekitServer();
            }
        });
        if (bridges.isEmpty() && !added.isEmpty()) {
            try {
                startHomekitServer();
            } catch (IOException | InvalidAlgorithmParameterException e) {
                logger.warn("could not initialize HomeKit bridge: {}", e.getMessage());
            }
        }
    }
}
