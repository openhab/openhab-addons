/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.zoo.internal.servlet;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component registers the Zoo UI Webapp.
 * 
 * @author Sebastian Janzen - Initial contribution
 */
public class ZooUIApp {

    private static final Logger logger = LoggerFactory.getLogger(ZooUIApp.class);
    public static final String WEBAPP_ALIAS = "/zoo";
    public static final String WEB_DIST_FOLDER = "web_dist";
    public static final String WEB_FOLDER = "web";
    public static final String DEBUG_PARAMETER_NAME = "webDebug";

    protected HttpService httpService;

    protected void activate(ComponentContext componentContext) {
        try {
            boolean debugMode = isDebugModeEnabled();
            String serveFolder = debugMode ? WEB_FOLDER : WEB_DIST_FOLDER;
            httpService.registerResources(WEBAPP_ALIAS, serveFolder, null);
            logger.info("Started Zoo UI at {} with debug mode = {}.", WEBAPP_ALIAS, debugMode);
        } catch (Exception e) {
            logger.error("Error during servlet startup", e);
        }
    }

    private boolean isDebugModeEnabled() {
        //            Bundle bundle = componentContext.getBundleContext().getBundle();
        //            Path webDistPath = new Path("/web_dist");
        //            FileLocator.openStream(bundle, webDistPath, false);
        boolean distFolderExists = this.getClass().getResource("/" + WEB_DIST_FOLDER) != null;
        boolean debugParameterSet = Boolean.parseBoolean(System.getProperty(DEBUG_PARAMETER_NAME));
        if (!distFolderExists) {
            logger.debug("Zoo UI has no /{} folder, falling back to debug mode.", WEB_DIST_FOLDER);
        } else if (debugParameterSet) {
            logger.debug("Zoo UI found a /{} folder, but forced to use /{} because of system parameter '{}'.",
                         WEB_DIST_FOLDER, WEB_FOLDER, DEBUG_PARAMETER_NAME);
        }

        return debugParameterSet || !distFolderExists;
    }

    protected void deactivate(ComponentContext componentContext) {
        httpService.unregister(WEBAPP_ALIAS);
        logger.info("Stopped Zoo UI");
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

}
