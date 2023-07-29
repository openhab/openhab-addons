/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.gpstracker.internal.provider.gpslogger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService;
import org.openhab.binding.gpstracker.internal.provider.AbstractCallbackServlet;
import org.openhab.binding.gpstracker.internal.provider.TrackerRegistry;

/**
 * Callback servlet for GPSLogger
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
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
     * @param trackerRegistry Tracker registry
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
