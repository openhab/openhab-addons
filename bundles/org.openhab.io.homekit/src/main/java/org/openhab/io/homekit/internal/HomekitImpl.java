/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.storage.StorageService;
import org.openhab.io.homekit.Homekit;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.HomekitRoot;
import io.github.hapjava.HomekitServer;

/**
 * Provides access to openHAB items via the HomeKit API
 *
 * @author Andy Lintner - Initial contribution
 */
@Component(immediate = true, service = { Homekit.class }, configurationPid = "org.openhab.homekit", property = {
        Constants.SERVICE_PID + "=org.openhab.homekit",
        ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=io:homekit",
        ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=io",
        ConfigurableService.SERVICE_PROPERTY_LABEL + "=HomeKit Integration", "port:Integer=9123" })
@NonNullByDefault
public class HomekitImpl implements Homekit {
    private final Logger logger = LoggerFactory.getLogger(HomekitImpl.class);
    private final StorageService storageService;
    private final NetworkAddressService networkAddressService;
    private final HomekitChangeListener changeListener;

    private HomekitSettings settings;
    private @Nullable InetAddress networkInterface;
    private @Nullable HomekitServer homekitServer;
    private @Nullable HomekitRoot bridge;

    @Activate
    public HomekitImpl(@Reference StorageService storageService, @Reference ItemRegistry itemRegistry,
            @Reference NetworkAddressService networkAddressService, Map<String, Object> config)
            throws IOException, InvalidAlgorithmParameterException {
        this.storageService = storageService;
        this.networkAddressService = networkAddressService;
        this.settings = processConfig(config);
        this.changeListener = new HomekitChangeListener(itemRegistry, settings);
        startHomekitServer();
    }

    private HomekitSettings processConfig(Map<String, Object> config) throws UnknownHostException {
        HomekitSettings settings = (new Configuration(config)).as(HomekitSettings.class);
        settings.process();
        if (settings.networkInterface == null) {
            settings.networkInterface = networkAddressService.getPrimaryIpv4HostAddress();
        }
        return settings;
    }

    @Modified
    protected synchronized void modified(Map<String, Object> config) {
        try {
            HomekitSettings oldSettings = settings;
            settings = processConfig(config);
            changeListener.updateSettings(settings);
            if (!oldSettings.networkInterface.equals(settings.networkInterface) || oldSettings.port != settings.port) {
                // the HomeKit server settings changed. we do a complete re-init
                stopHomekitServer();
                startHomekitServer();
            } else if (!oldSettings.name.equals(settings.name) || !oldSettings.pin.equals(settings.pin)) {
                // we change the root bridge only
                stopBridge();
                startBridge();
            }
        } catch (IOException | InvalidAlgorithmParameterException e) {
            logger.debug("Could not initialize HomeKit connector: {}", e.getMessage());
            return;
        }
    }

    private void stopBridge() {
        final HomekitRoot bridge = this.bridge;
        if (bridge != null) {
            changeListener.unsetBridge();
            bridge.stop();
            this.bridge = null;
        }
    }

    private void startBridge() throws InvalidAlgorithmParameterException, IOException {
        if (homekitServer != null && bridge == null) {
            final HomekitRoot bridge = homekitServer.createBridge(new HomekitAuthInfoImpl(storageService, settings.pin),
                    settings.name, HomekitSettings.MANUFACTURER,
                    FrameworkUtil.getBundle(getClass()).getVersion().toString(), HomekitSettings.SERIAL_NUMBER);
            changeListener.setBridge(bridge);
            bridge.start();
            this.bridge = bridge;
        } else {
            logger.warn(
                    "trying to start bridge but HomeKit server is not initialized or bridge is already initialized");
        }
    }

    private void startHomekitServer() throws InvalidAlgorithmParameterException, IOException {
        if (homekitServer == null) {
            networkInterface = InetAddress.getByName(settings.networkInterface);
            homekitServer = new HomekitServer(networkInterface, settings.port);
            startBridge();
        } else {
            logger.warn("trying to start HomeKit server but it is already initialized");
        }
    }

    private void stopHomekitServer() {
        final HomekitServer homekit = this.homekitServer;
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
        changeListener.clearAccessories();
        stopHomekitServer();
        changeListener.stop();
    }

    @Override
    public void refreshAuthInfo() throws IOException {
        if (bridge != null) {
            bridge.refreshAuthInfo();
        }
    }

    @Override
    public void allowUnauthenticatedRequests(boolean allow) {
        if (bridge != null) {
            bridge.allowUnauthenticatedRequests(allow);
        }
    }
}
