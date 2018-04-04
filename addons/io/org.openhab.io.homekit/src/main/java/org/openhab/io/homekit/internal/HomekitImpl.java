/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.storage.StorageService;
import org.openhab.io.homekit.Homekit;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitRoot;
import com.beowulfe.hap.HomekitServer;

/**
 * Provides access to openHAB items via the Homekit API
 *
 * @author Andy Lintner
 */
public class HomekitImpl implements Homekit {

    private final HomekitSettings settings = new HomekitSettings();
    private HomekitServer homekit;
    private HomekitRoot bridge;
    private StorageService storageService;
    private final HomekitChangeListener changeListener = new HomekitChangeListener();
    private Logger logger = LoggerFactory.getLogger(HomekitImpl.class);

    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        changeListener.setSettings(settings);
        changeListener.setItemRegistry(itemRegistry);
    }

    protected synchronized void activate(ComponentContext componentContext) {
        modified(componentContext);
    }

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
        } catch (Exception e) {
            logger.error("Could not initialize homekit: {}", e.getMessage(), e);
        }
    }

    protected void deactivate() {
        changeListener.clearAccessories();
        if (bridge != null) {
            bridge.stop();
            bridge = null;
        }
        if (homekit != null){
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
