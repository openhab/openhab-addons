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
package org.openhab.ui.homebuilder.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The resource registering for Home Builder
 *
 * @author Kuba Wolanin - Initial contribution
 *
 */
@Component(immediate = true)
public class HomeBuilderServlet {

    public static final String HOMEBUILDER_ALIAS = "/homebuilder";

    private final Logger logger = LoggerFactory.getLogger(HomeBuilderServlet.class);

    protected HttpService httpService;

    @Activate
    protected void activate() {
        try {
            httpService.registerResources(HOMEBUILDER_ALIAS, "web", null);
            logger.info("Started Home Builder at {}", HOMEBUILDER_ALIAS);
        } catch (NamespaceException e) {
            logger.error("Error during Home Builder startup: {}", e.getMessage());
        }
    }

    @Deactivate
    protected void deactivate() {
        httpService.unregister(HOMEBUILDER_ALIAS);
        logger.info("Stopped Home Builder");
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

}
