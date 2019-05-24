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
package org.openhab.io.homekit.internal;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Map;

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

import com.beowulfe.hap.HomekitRoot;
import com.beowulfe.hap.HomekitServer;

/**
 * Provides access to openHAB items via the Homekit API
 *
 * @author Andy Lintner - Initial contribution
 */
@Component(immediate = true, service = { Homekit.class }, configurationPid = "org.openhab.homekit", property = {
        Constants.SERVICE_PID + "=org.openhab.homekit",
        ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=io:homekit",
        ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=io",
        ConfigurableService.SERVICE_PROPERTY_LABEL + "=HomeKit Integration", "port:Integer=9123" })
public class HomekitImpl implements Homekit {
    private final Logger logger = LoggerFactory.getLogger(HomekitImpl.class);
    private final StorageService storageService;
    private final NetworkAddressService networkAddressService;
    private final HomekitChangeListener changeListener;

    private HomekitSettings settings;
    private HomekitServer homekit;
    private HomekitRoot bridge;

    @Activate
    public HomekitImpl(@Reference StorageService storageService, @Reference ItemRegistry itemRegistry,
            @Reference NetworkAddressService networkAddressService, Map<String, Object> config)
            throws IOException, InvalidAlgorithmParameterException {
        this.storageService = storageService;
        this.networkAddressService = networkAddressService;
        this.settings = processConfig(config);
        this.changeListener = new HomekitChangeListener(itemRegistry, settings);
        start();
    }

    private HomekitSettings processConfig(Map<String, Object> config) throws UnknownHostException {
        HomekitSettings settings = (new Configuration(config)).as(HomekitSettings.class);
        settings.process(networkAddressService.getPrimaryIpv4HostAddress());
        return settings;
    }

    @Modified
    protected synchronized void modified(Map<String, Object> config) {
        try {
            this.settings = processConfig(config);
            changeListener.updateSettings(this.settings);
            stop();
            start();
        } catch (IOException | InvalidAlgorithmParameterException e) {
            logger.debug("Could not initialize homekit: {}", e.getMessage());
            return;
        }
    }

    @Deactivate
    protected void deactivate() {
        changeListener.clearAccessories();
        stop();
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

    private void start() throws IOException, InvalidAlgorithmParameterException {
        homekit = new HomekitServer(settings._networkInterface, settings.port);
        bridge = homekit.createBridge(new HomekitAuthInfoImpl(storageService, settings.pin), settings.name,
                HomekitSettings.MANUFACTURER, FrameworkUtil.getBundle(getClass()).getVersion().toString(),
                HomekitSettings.SERIAL_NUMBER);
        bridge.start();
        changeListener.setBridge(bridge);
    }

    private void stop() {
        changeListener.setBridge(null);

        if (bridge != null) {
            bridge.stop();
            bridge = null;
        }
        if (homekit != null) {
            homekit.stop();
            homekit = null;
        }
    }
}
