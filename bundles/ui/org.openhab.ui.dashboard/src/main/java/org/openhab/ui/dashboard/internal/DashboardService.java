/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.dashboard.internal;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component registers the dashboard.
 * 
 * @author Kai Kreuzer - Initial contribution
 */
public class DashboardService {
	
    public static final String DASHBOARD_ALIAS = "/start";
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    protected HttpService httpService;

    protected void activate(ComponentContext componentContext) {
        try {
            httpService.registerResources(DASHBOARD_ALIAS, "web", null);
            logger.info("Started dashboard at " + DASHBOARD_ALIAS);
        } catch (NamespaceException e) {
            logger.error("Error during servlet startup", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        httpService.unregister(DASHBOARD_ALIAS);
        logger.info("Stopped dashboard");
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

}
