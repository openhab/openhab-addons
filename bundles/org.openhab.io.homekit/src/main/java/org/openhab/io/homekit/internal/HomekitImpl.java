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

import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.storage.StorageService;
import org.openhab.io.homekit.Homekit;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
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
    private final HomekitSettings settings = new HomekitSettings();
    private HomekitServer homekit;
    private HomekitRoot bridge;
    private StorageService storageService;
    private final HomekitChangeListener changeListener = new HomekitChangeListener();
    private Logger logger = LoggerFactory.getLogger(HomekitImpl.class);

    @Reference
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void unsetStorageService(StorageService storageService) {
        this.storageService = null;
    }

    @Reference
    public void setItemRegistry(ItemRegistry itemRegistry) {
        changeListener.setSettings(settings);
        changeListener.setItemRegistry(itemRegistry);
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        changeListener.setItemRegistry(null);
    }

    @Activate
    protected synchronized void activate(ComponentContext componentContext) {
        modified(componentContext);
    }

    @Modified
    protected synchronized void modified(ComponentContext componentContext) {
        try {
            settings.fill(componentContext.getProperties());
            changeListener.setSettings(settings);
        } catch (UnknownHostException e) {
            logger.debug("Could not initialize homekit: {}", e.getMessage(), e);
            return;
        }
        try {
            start();
        } catch (IOException | InvalidAlgorithmParameterException e) {
            logger.warn("Could not initialize homekit: {}", e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate() {
        changeListener.clearAccessories();
        if (bridge != null) {
            bridge.stop();
            bridge = null;
        }
        if (homekit != null) {
            homekit.stop();
            homekit = null;
        }

        changeListener.setBridge(null);
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
        homekit = new HomekitServer(settings.getNetworkInterface(), settings.getPort());
        bridge = homekit.createBridge(new HomekitAuthInfoImpl(storageService, settings.getPin()), settings.getName(),
                settings.getManufacturer(), settings.getModel(), settings.getSerialNumber());
        bridge.start();
        changeListener.setBridge(bridge);
    }
}
