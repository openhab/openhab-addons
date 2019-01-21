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
package org.openhab.ui.paper.internal;

import org.eclipse.smarthome.io.http.HttpContextFactoryService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component registers the Paper UI Webapp.
 *
 * @author Dennis Nobel - Initial contribution
 */
@Component()
public class PaperUIApp {

    public static final String WEBAPP_ALIAS = "/paperui";
    private final Logger logger = LoggerFactory.getLogger(PaperUIApp.class);

    protected HttpService httpService;

    private HttpContextFactoryService httpContextFactoryService;

    protected void activate(BundleContext bundleContext) {
        try {
            Bundle paperuiBundle = bundleContext.getBundle();
            httpService.registerResources(WEBAPP_ALIAS, "web",
                    httpContextFactoryService.createDefaultHttpContext(paperuiBundle));
            logger.info("Started Paper UI at " + WEBAPP_ALIAS);
        } catch (NamespaceException e) {
            logger.error("Error during servlet startup", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        httpService.unregister(WEBAPP_ALIAS);
        logger.info("Stopped Paper UI");
    }

    @Reference(policy = ReferencePolicy.STATIC)
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    @Reference(policy = ReferencePolicy.STATIC)
    public void setHttpContextFactoryService(HttpContextFactoryService httpContextFactoryService) {
        this.httpContextFactoryService = httpContextFactoryService;
    }

    public void unsetHttpContextFactoryService(HttpContextFactoryService httpContextFactoryService) {
        this.httpContextFactoryService = null;
    }

}
