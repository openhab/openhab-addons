/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.geofence.internal.provider.gpslogger;


import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.openhab.binding.geofence.internal.discovery.DeviceDiscoveryService;
import org.openhab.binding.geofence.internal.provider.AbstractCallbackServlet;
import org.osgi.service.http.HttpService;

/**
 * Callback servlet for GPSLogger
 *
 * @author Gabor Bicskei - - Initial contribution and API
 */
public class GPSLoggerCallbackServlet extends AbstractCallbackServlet {
    /**
     * Servlet path
     */
    private static final String CALLBACK_PATH = "/geofence/gpslogger";

    /**
     * Provider name
     */
    private static final String PROVIDER = "GPSLogger";

    /**
     * Constructor called at binding startup.
     *
     * @param httpService      HTTP service that runs the servlet.
     * @param thingRegistry    Thing registry.
     * @param discoveryService Discovery service for new devices.
     */
    public GPSLoggerCallbackServlet(HttpService httpService, ThingRegistry thingRegistry, DeviceDiscoveryService discoveryService) {
        super(httpService, thingRegistry, discoveryService);
    }

    @Override
    protected String getPath() {
        return CALLBACK_PATH;
    }

    @Override
    protected String getProvider() {
        return PROVIDER;
    }
}
