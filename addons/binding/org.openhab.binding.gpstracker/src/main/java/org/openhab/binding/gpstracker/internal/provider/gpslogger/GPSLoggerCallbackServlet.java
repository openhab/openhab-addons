/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.provider.gpslogger;

import org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService;
import org.openhab.binding.gpstracker.internal.provider.AbstractCallbackServlet;
import org.openhab.binding.gpstracker.internal.provider.TrackerRegistry;

/**
 * Callback servlet for GPSLogger
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class GPSLoggerCallbackServlet extends AbstractCallbackServlet {

    private static final long serialVersionUID = -6992472786850682196L;

    /**
     * Servlet path
     */
    private static final String CALLBACK_PATH = "/gpstracker/gpslogger";

    /**
     * Provider name
     */
    private static final String PROVIDER = "GPSLogger";

    /**
     * Constructor called at binding startup.
     *
     * @param discoveryService Discovery service for new trackers.
     * @param trackerRegistry  Tracker registry
     */
    public GPSLoggerCallbackServlet(TrackerDiscoveryService discoveryService, TrackerRegistry trackerRegistry) {
        super(discoveryService, trackerRegistry);
    }

    @Override
    public String getPath() {
        return CALLBACK_PATH;
    }

    @Override
    protected String getProvider() {
        return PROVIDER;
    }
}
